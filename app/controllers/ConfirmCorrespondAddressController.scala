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
import connectors.{EnrolmentConnector, KeystoreConnector}
import forms.ConfirmCorrespondAddressForm._
import models.{AddressModel, ConfirmCorrespondAddressModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.contactInformation.ConfirmCorrespondAddress

import scala.concurrent.Future

object ConfirmCorrespondAddressController extends ConfirmCorrespondAddressController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ConfirmCorrespondAddressController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  val keyStoreConnector: KeystoreConnector

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def getAddressModels = {
      for {
        confirmCorrespondAddress <- keyStoreConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](KeystoreKeys.confirmContactAddress)
        contactAddress <- keyStoreConnector.fetchAndGetFormData[AddressModel](KeystoreKeys.contactAddress)
      } yield (confirmCorrespondAddress, contactAddress)
    }

    getAddressModels.map {
      case (_, Some(contactAddress)) if contactAddress.addressline1.length > 0 =>
        Ok(ConfirmCorrespondAddress(confirmCorrespondAddressForm.fill(
          ConfirmCorrespondAddressModel("", contactAddress))))
      case (_, _) =>
        Ok(ConfirmCorrespondAddress(confirmCorrespondAddressForm.fill(
          ConfirmCorrespondAddressModel("", getSubscriptionAddress))))
    }
  }

  //TODO: get the address below from ETMP when play this story
  def getSubscriptionAddress: AddressModel = {
    AddressModel("Company Name Ltd.", "2 Telford Plaza", Some("Lawn Central"), Some("Telford"), Some("TF3 4NT"))
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    confirmCorrespondAddressForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(ConfirmCorrespondAddress(formWithErrors)))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.confirmContactAddress, validFormData)
        validFormData.contactAddressUse match {
          case Constants.StandardRadioButtonYesValue => {
            keyStoreConnector.saveFormData(KeystoreKeys.backLinkSupportingDocs,
              routes.ConfirmCorrespondAddressController.show().toString())
            keyStoreConnector.saveFormData(KeystoreKeys.contactAddress, validFormData.address)
            Future.successful(Redirect(routes.SupportingDocumentsController.show()))
          }
          case Constants.StandardRadioButtonNoValue => {
            // Clear the saved address as user does not want to use it
            keyStoreConnector.saveFormData(KeystoreKeys.contactAddress, AddressModel("", ""))
            Future.successful(Redirect(routes.ContactAddressController.show()))
          }
        }
      }
    )
  }
}
