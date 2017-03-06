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

package controllers.eisseis

import auth.{AuthorisedAndEnrolledForTAVC,SEIS, EIS, VCT}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.predicates.FeatureSwitch
import forms.ConfirmContactDetailsForm._
import models.{ConfirmContactDetailsModel, ContactDetailsModel}
import services.SubscriptionService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eisseis.contactInformation.ConfirmContactDetails
import config.FrontendGlobal.internalServerErrorTemplate
import play.api.mvc.Result
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object ConfirmContactDetailsController extends ConfirmContactDetailsController{
  val subscriptionService = SubscriptionService
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ConfirmContactDetailsController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(EIS,SEIS,VCT),Seq(SEIS,VCT), Seq(EIS,SEIS))

  val subscriptionService: SubscriptionService

  val show = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>

      def getContactDetails: Future[Option[ContactDetailsModel]] = for {
        tavcRef <- getTavCReferenceNumber()
        contactDetails <- subscriptionService.getSubscriptionContactDetails(tavcRef)
      } yield contactDetails

      def routeRequest: Option[ConfirmContactDetailsModel] => Future[Result] = {
        case Some(savedData) => Future.successful(Ok(ConfirmContactDetails(confirmContactDetailsForm.fill(savedData))))
        case _ => getContactDetails.map {
          case Some(data) => Ok(ConfirmContactDetails(confirmContactDetailsForm.fill(ConfirmContactDetailsModel("", data))))
          case _ => InternalServerError(internalServerErrorTemplate)
        }
      }

      for {
        confirmContactAddress <- s4lConnector.fetchAndGetFormData[ConfirmContactDetailsModel](KeystoreKeys.confirmContactDetails)
        route <- routeRequest(confirmContactAddress)
      } yield route
    }
  }

  val submit = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      confirmContactDetailsForm.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(ConfirmContactDetails(formWithErrors)))
        },
        validFormData => {
          s4lConnector.saveFormData(KeystoreKeys.confirmContactDetails, validFormData)
          validFormData.contactDetailsUse match {
            case Constants.StandardRadioButtonYesValue => {
              s4lConnector.saveFormData(KeystoreKeys.backLinkConfirmCorrespondence, routes.ConfirmContactDetailsController.show().url)
              s4lConnector.saveFormData(KeystoreKeys.contactDetails, validFormData.contactDetails)
              Future.successful(Redirect(routes.ConfirmCorrespondAddressController.show()))
            }
            case Constants.StandardRadioButtonNoValue => {
              s4lConnector.saveFormData(KeystoreKeys.backLinkConfirmCorrespondence, routes.ContactDetailsController.show().url)
              Future.successful(Redirect(routes.ContactDetailsController.show()))
            }
          }
        }
      )
    }
  }

}
