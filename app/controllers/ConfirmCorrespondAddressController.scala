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
import config.FrontendGlobal._
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.ControllerHelpers
import forms.ConfirmCorrespondAddressForm._
import models.{AddressModel, ConfirmCorrespondAddressModel}
import play.api.mvc.Result
import services.SubscriptionService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.contactInformation.ConfirmCorrespondAddress

import scala.concurrent.Future

object ConfirmCorrespondAddressController extends ConfirmCorrespondAddressController{
  val subscriptionService = SubscriptionService
  val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ConfirmCorrespondAddressController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  val s4lConnector: S4LConnector
  val subscriptionService: SubscriptionService

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def getStoredConfirmContactAddress: Future[Option[ConfirmCorrespondAddressModel]] = {
      for {
        confirmContactAddress <- s4lConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](KeystoreKeys.confirmContactAddress)
      } yield confirmContactAddress
    }

    def getEtmpContactAddress: Option[ConfirmCorrespondAddressModel] => Future[Option[ConfirmCorrespondAddressModel]] = {
      case Some(storedConfirmContactAddress) => Future.successful(Some(storedConfirmContactAddress))
      case _ => for {
        tavcRef <- getTavCReferenceNumber()
        subscriptionDetails <- subscriptionService.getEtmpSubscriptionDetails(tavcRef)
      } yield subscriptionDetails match {
        case Some(subscriptionData) => Some(ConfirmCorrespondAddressModel("", subscriptionData.contactAddress))
        case _ => None
      }
    }

    def routeRequest: Option[String] => Future[Result] = {
      case Some(backLink) =>
        for {
          storedConfirmContactAddress <- getStoredConfirmContactAddress
          etmpContactAddress <- getEtmpContactAddress(storedConfirmContactAddress) //Only calls DES if no stored details passed to it
        } yield (storedConfirmContactAddress, etmpContactAddress) match {
          case (Some(storedData), _) => Ok(ConfirmCorrespondAddress(confirmCorrespondAddressForm.fill(storedData), backLink))
          case (_, Some(etmpData)) => Ok(ConfirmCorrespondAddress(confirmCorrespondAddressForm.fill(etmpData), backLink))
          case _ => InternalServerError(internalServerErrorTemplate)
        }
      case _ => Future.successful(Redirect(routes.ConfirmContactDetailsController.show()))
    }

    for {
      backLink <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkConfirmCorrespondence, s4lConnector)
      route <- routeRequest(backLink)
    } yield route
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
