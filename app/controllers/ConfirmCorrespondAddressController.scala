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
import controllers.Helpers.ControllerHelpers
import forms.ConfirmCorrespondAddressForm._
import models.{AddressModel, ConfirmCorrespondAddressModel}
import play.api.mvc.Result
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.contactInformation.ConfirmCorrespondAddress

import scala.concurrent.Future

object ConfirmCorrespondAddressController extends ConfirmCorrespondAddressController{
  val s4lConnector: S4LConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ConfirmCorrespondAddressController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  val s4lConnector: S4LConnector

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def routeRequest: Option[String] => Future[Result] = {
      case Some(backLink) => {
        for {
          confirmCorrespondAddress <- s4lConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](KeystoreKeys.confirmContactAddress)
        } yield confirmCorrespondAddress match {
          case Some(storedAddress) =>
            Ok(ConfirmCorrespondAddress(confirmCorrespondAddressForm.fill(storedAddress), backLink))
          case _ =>
            Ok(ConfirmCorrespondAddress(confirmCorrespondAddressForm.fill(ConfirmCorrespondAddressModel("", getSubscriptionAddress)), backLink))
        }
      }
      case _ => Future.successful(Redirect(routes.ConfirmContactDetailsController.show()))
    }

    for {
      backLink <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkConfirmCorrespondence, s4lConnector)
      route <- routeRequest(backLink)
    } yield route
  }

  //TODO: get the address below from ETMP when play this story
  def getSubscriptionAddress: AddressModel = {
    AddressModel("Company Name Ltd.", "2 Telford Plaza", Some("Lawn Central"), Some("Telford"), Some("TF3 4NT"))
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def routeRequest: Option[String] => Future[Result] = {
      case Some(backLink) => {
        confirmCorrespondAddressForm.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(BadRequest(ConfirmCorrespondAddress(formWithErrors, backLink)))
          },
          validFormData => {
            s4lConnector.saveFormData(KeystoreKeys.confirmContactAddress, validFormData)
            validFormData.contactAddressUse match {
              case Constants.StandardRadioButtonYesValue => {
                s4lConnector.saveFormData(KeystoreKeys.backLinkSupportingDocs,
                  routes.ConfirmCorrespondAddressController.show().toString())
                s4lConnector.saveFormData(KeystoreKeys.contactAddress, validFormData.address)
                Future.successful(Redirect(routes.SupportingDocumentsController.show()))
              }
              case Constants.StandardRadioButtonNoValue => {
                Future.successful(Redirect(routes.ContactAddressController.show()))
              }
            }
          }
        )
      }
      case _ => Future.successful(Redirect(routes.ConfirmContactDetailsController.show()))
    }

    for {
      backLink <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkConfirmCorrespondence, s4lConnector)
      route <- routeRequest(backLink)
    } yield route
  }
}
