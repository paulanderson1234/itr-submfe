/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import auth.{AuthorisedAndEnrolledForTAVC, TAVCUser}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import config.FrontendGlobal.internalServerErrorTemplate
import connectors.{ComplianceStatementConnector, EnrolmentConnector, S4LConnector}
import controllers.Helpers.ControllerHelpers
import models.ApplicationHubModel
import models.submission.SchemeTypesModel
import play.api.mvc.Result
import services.{RegistrationDetailsService, SubmissionService, SubscriptionService}
import play.Logger
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.introduction.{ApplicationHub, _}
import views.html.hubPartials.{ApplicationHubExisting, _}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.Future

object ApplicationHubController extends ApplicationHubController{
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val s4lConnector = S4LConnector
  val subscriptionService: SubscriptionService = SubscriptionService
  val registrationDetailsService: RegistrationDetailsService = RegistrationDetailsService
  val submissionService: SubmissionService = SubmissionService
  val complianceStatementConnector: ComplianceStatementConnector = ComplianceStatementConnector
}

trait ApplicationHubController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq()
  val complianceStatementConnector: ComplianceStatementConnector

  val subscriptionService: SubscriptionService
  val registrationDetailsService: RegistrationDetailsService
  val submissionService: SubmissionService

  def show(tokenId:Option[String]):Action[AnyContent] = AuthorisedAndEnrolled.async ( { implicit user => implicit request =>

    def routeRequest(applicationHubModel: Option[ApplicationHubModel], hasPreviousSubmissions: Boolean): Future[Result] = {
      if (applicationHubModel.nonEmpty) {
        val statusInfo = s4lConnector.fetchAndGetFormData[Boolean](KeystoreKeys.applicationInProgress).flatMap {
          case Some(true) => Future.successful(true, Constants.advanceAssurance,
            Some(ControllerHelpers.schemeDescriptionFromTypes(applicationHubModel.get.schemeTypes)))
          case _ =>
            for{
              csApplication <- complianceStatementConnector.getComplianceStatementApplication()
            }yield (csApplication.inProgress, Constants.complianceStatement, csApplication.schemeType)
        }

        statusInfo.map{
          case (true, Constants.advanceAssurance, Some(desc)) => Ok(ApplicationHub(applicationHubModel.get,
            ApplicationHubExisting(applicationHubModel.get.schemeTypes.fold(controllers.eis.routes.NatureOfBusinessController.show().url)
            (ControllerHelpers.routeToScheme), desc, None), hasPreviousSubmissions))
          case (true, Constants.complianceStatement, Some(desc)) => Ok(ApplicationHub(applicationHubModel.get,
            ApplicationHubExisting(ControllerHelpers.routeToCSScheme(desc), ControllerHelpers.schemeDescriptionFromCSTypes(desc), Some(desc)),
            hasPreviousSubmissions))
          case (false, _, _) => Ok(ApplicationHub(applicationHubModel.get, ApplicationHubNew(), hasPreviousSubmissions))
        }
      }
      else Future.successful(InternalServerError(internalServerErrorTemplate))
    }

    def getApplicationHubModel(tavcRef:String)(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[ApplicationHubModel]] = (for {
      registrationDetailsModel <- registrationDetailsService.getRegistrationDetails(tavcRef)
      subscriptionDetailsModel <- subscriptionService.getSubscriptionContactDetails(tavcRef)
      schemeTypesModel <-   s4lConnector.fetchAndGetFormData[SchemeTypesModel](KeystoreKeys.selectedSchemes)
    } yield Some(ApplicationHubModel(registrationDetailsModel.get.organisationName, registrationDetailsModel.get.addressModel,
      subscriptionDetailsModel.get, schemeTypesModel))).recover {
      case _ =>
        Logger.warn(s"[ApplicationHubController][getApplicationModel] - ApplicationHubModel components not found")
        None
    }

    (for {
      tavcRef <- getTavCReferenceNumber()
      applicationHubModel <- getApplicationHubModel(tavcRef)
      hasPreviousSubmissions <- submissionService.hasPreviousSubmissions(tavcRef)
      route <- routeRequest(applicationHubModel, hasPreviousSubmissions)
    } yield route ) recover{
      case e: Exception => {
        Logger.warn(s"[ApplicationHubController][getApplicationModel] - Exception occurred: ${e.getMessage}")
        InternalServerError(internalServerErrorTemplate)
      }
    }
  },tokenId)

  val newApplication = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    Future.successful(Redirect(controllers.hubGuidance.routes.WhoCanUseNewServiceController.show()))
  }

  val delete = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    Future.successful(Redirect(routes.ConfirmDeleteApplicationController.show()))
  }

  val newCSApplication = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    Future.successful(Redirect(FrontendAppConfig.submissionCSFrontendServiceBaseUrl))
  }
}
