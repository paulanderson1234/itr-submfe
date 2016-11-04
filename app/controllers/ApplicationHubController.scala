/*
 * Copyright 2016 HM Revenue & Customs
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
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import models.ApplicationHubModel
import play.api.mvc.Result
import services.{RegistrationDetailsService, SubscriptionService}
import play.Logger
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.introduction._
import views.html.hubPartials._

import scala.concurrent.Future

/**
  * Created by jade on 31/10/16.
  */
object ApplicationHubController extends ApplicationHubController{
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  val s4lConnector: S4LConnector = S4LConnector
}


trait ApplicationHubController extends FrontendController with AuthorisedAndEnrolledForTAVC{

  val s4lConnector: S4LConnector

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def routeRequest(applicationHubModel: Option[ApplicationHubModel]): Future[Result] = {
      if(applicationHubModel.isDefined) {
        s4lConnector.fetchAndGetFormData[Boolean](KeystoreKeys.applicationInProgress).map {
          case Some(true) => Ok(ApplicationHub(applicationHubModel.get,ApplicationHubExisting()))
          case _ => Ok(ApplicationHub(applicationHubModel.get,ApplicationHubNew()))
        }
      }
      else Future.successful(InternalServerError)
    }


    def getApplicationHubModel()(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[ApplicationHubModel]] = {
      (for{
        tavcRef <- getTavCReferenceNumber()
        registrationDetailsModel <- RegistrationDetailsService.getRegistrationDetails(tavcRef)
        subscriptionDetailsModel <- SubscriptionService.getSubscriptionContactDetails(tavcRef)
      }yield Some(ApplicationHubModel(registrationDetailsModel.get.organisationName,registrationDetailsModel.get.addressModel,subscriptionDetailsModel.get))).recover {
        case _ =>
          Logger.warn(s"[ApplicationHubController][getApplicationModel] - ApplicationHubModel components not found")
          None
      }
    }

    for {
      applicationHubModel <- getApplicationHubModel()
      route <- routeRequest(applicationHubModel)
    } yield route


  }

  val newApplication = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.saveFormData(KeystoreKeys.applicationInProgress, true)
    Future.successful(Redirect(routes.YourCompanyNeedController.show()))
  }

  val delete = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.clearCache().map{
      case _ => Redirect(routes.ApplicationHubController.show())
    }
  }

}
