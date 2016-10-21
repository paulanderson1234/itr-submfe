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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import models.TaxpayerReferenceModel
import common._
import forms.TaxPayerReferenceForm._

import scala.concurrent.Future
import views.html.companyDetails.TaxpayerReference

object TaxpayerReferenceController extends TaxpayerReferenceController
{
  val s4lConnector: S4LConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait TaxpayerReferenceController extends FrontendController with AuthorisedAndEnrolledForTAVC{

  val s4lConnector: S4LConnector

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.fetchAndGetFormData[TaxpayerReferenceModel](KeystoreKeys.taxpayerReference).map {
      case Some(data) => Ok(TaxpayerReference(taxPayerReferenceForm.fill(data)))
      case None => Ok(TaxpayerReference(taxPayerReferenceForm))
    }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    taxPayerReferenceForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(TaxpayerReference(formWithErrors)))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.taxpayerReference, validFormData)
        Future.successful(Redirect(routes.RegisteredAddressController.show()))
      }
    )
  }
}
