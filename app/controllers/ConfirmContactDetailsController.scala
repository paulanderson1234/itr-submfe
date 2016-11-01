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
import models.{ConfirmContactDetailsModel, ContactDetailsModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.contactInformation.ConfirmContactDetails

import scala.concurrent.Future

object ConfirmContactDetailsController extends ConfirmContactDetailsController{
  val s4lConnector: S4LConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ConfirmContactDetailsController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  val s4lConnector: S4LConnector

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def getContactDetailModels = {
      for {
        confirmContactDetails <- s4lConnector.fetchAndGetFormData[ConfirmContactDetailsModel](KeystoreKeys.confirmContactDetails)
      } yield confirmContactDetails
    }

    getContactDetailModels.map {
      case Some(confirmContactDetails) =>
        Ok(ConfirmContactDetails(confirmContactDetailsForm.fill(confirmContactDetails)))
      case _ =>
        Ok(ConfirmContactDetails(confirmContactDetailsForm.fill(ConfirmContactDetailsModel("", getContactDetails))))
    }
  }

  //TODO: get the address below from ETMP when play this story
  def getContactDetails: ContactDetailsModel = {
    ContactDetailsModel("Forename", "Surname", Some("01234 567890"), Some("07777 123456"), "email@email.com")
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
            s4lConnector.saveFormData(KeystoreKeys.backLinkConfirmCorrespondence, routes.ContactDetailsController.show().url)
            Future.successful(Redirect(routes.ContactDetailsController.show()))
          }
        }
      }
    )
  }
}