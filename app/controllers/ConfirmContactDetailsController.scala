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

import auth.AuthorisedAndEnrolledForTAVC
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import forms.ConfirmContactDetailsForm._
import models.ConfirmContactDetailsModel
import services.SubscriptionService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.contactInformation.ConfirmContactDetails
import config.FrontendGlobal.internalServerErrorTemplate
import scala.concurrent.Future

object ConfirmContactDetailsController extends ConfirmContactDetailsController{
  val subscriptionService = SubscriptionService
  val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ConfirmContactDetailsController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  val s4lConnector: S4LConnector
  val subscriptionService: SubscriptionService

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def getStoredConfirmContactDetails: Future[Option[ConfirmContactDetailsModel]] = {
      for {
        confirmContactDetails <- s4lConnector.fetchAndGetFormData[ConfirmContactDetailsModel](KeystoreKeys.confirmContactDetails)
      } yield confirmContactDetails
    }

    def getEtmpContactDetails: Option[ConfirmContactDetailsModel] => Future[Option[ConfirmContactDetailsModel]] = {
      case Some(storedContactDetails) => Future.successful(Some(storedContactDetails))
      case _ => for {
        tavcRef <- getTavCReferenceNumber()
        subscriptionDetails <- subscriptionService.getEtmpSubscriptionDetails(tavcRef)
      } yield subscriptionDetails match {
        case Some(subscriptionData) =>
          s4lConnector.saveFormData(KeystoreKeys.confirmContactDetails, subscriptionData.contactDetails)
          s4lConnector.saveFormData(KeystoreKeys.confirmContactAddress, subscriptionData.contactAddress)
          Some(ConfirmContactDetailsModel("", subscriptionData.contactDetails))
        case _ => None
      }
    }

    for {
      storedContactDetails <- getStoredConfirmContactDetails
      etmpContactDetails <- getEtmpContactDetails(storedContactDetails) //Only calls DES if no stored details passed to it
    } yield (storedContactDetails, etmpContactDetails) match {
      case (Some(storedData), _) => Ok(ConfirmContactDetails(confirmContactDetailsForm.fill(storedData)))
      case (_, Some(etmpData)) => Ok(ConfirmContactDetails(confirmContactDetailsForm.fill(etmpData)))
      case _ => InternalServerError(internalServerErrorTemplate)
    }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
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
            Future.successful(Redirect(routes.ContactDetailsController.show()))
          }
        }
      }
    )
  }
}
