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

package controllers.eis

import auth.{AuthorisedAndEnrolledForTAVC, EIS, VCT}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import forms.ContactAddressForm._
import models.AddressModel
import play.api.i18n.Messages
import uk.gov.hmrc.play.frontend.controller.FrontendController
import utils.CountriesHelper
import views.html.eis.contactInformation.ContactAddress
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object ContactAddressController extends ContactAddressController
{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait ContactAddressController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS),Seq(VCT),Seq(EIS,VCT))

  lazy val countriesList = CountriesHelper.getIsoCodeTupleList

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.fetchAndGetFormData[AddressModel](KeystoreKeys.manualContactAddress).map {
      case Some(data) => Ok(ContactAddress(contactAddressForm.fill(data), countriesList))
      case None => Ok(ContactAddress(contactAddressForm.fill(AddressModel("","")), countriesList))
    }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    contactAddressForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(ContactAddress(if(formWithErrors.hasGlobalErrors)
          formWithErrors.discardingErrors.withError("postcode", Messages("validation.error.countrypostcode")) else formWithErrors, countriesList)))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.manualContactAddress, validFormData)
        s4lConnector.saveFormData(KeystoreKeys.contactAddress, validFormData)
        s4lConnector.saveFormData(KeystoreKeys.backLinkSupportingDocs, routes.ContactAddressController.show().url)
        Future.successful(Redirect(routes.SupportingDocumentsController.show()))
      }
    )
  }
}
