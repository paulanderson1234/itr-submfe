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
import controllers.Helpers.KnowledgeIntensiveHelper
import forms.DateOfIncorporationForm._
import models.DateOfIncorporationModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.companyDetails.DateOfIncorporation
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future


object DateOfIncorporationController extends DateOfIncorporationController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait DateOfIncorporationController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS),Seq(VCT),Seq(EIS,VCT))



  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation).map {
      case Some(data) => Ok(DateOfIncorporation(dateOfIncorporationForm.fill(data)))
      case None => Ok(DateOfIncorporation(dateOfIncorporationForm))
    }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    dateOfIncorporationForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(DateOfIncorporation(formWithErrors)))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.dateOfIncorporation, validFormData)
        KnowledgeIntensiveHelper.setKiDateCondition(s4lConnector, validFormData.day.get, validFormData.month.get, validFormData.year.get)
        Future.successful(Redirect(routes.CommercialSaleController.show()))
      }
    )
  }
}
