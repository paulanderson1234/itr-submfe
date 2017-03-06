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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.predicates.FeatureSwitch
import uk.gov.hmrc.play.frontend.controller.FrontendController
import models.NatureOfBusinessModel
import common._
import forms.NatureOfBusinessForm._
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future
import views.html.eisseis.companyDetails.NatureOfBusiness

object NatureOfBusinessController extends NatureOfBusinessController
{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait NatureOfBusinessController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(EIS,SEIS,VCT),Seq(SEIS,VCT), Seq(EIS,SEIS))

  val show = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.fetchAndGetFormData[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness).map {
        case Some(data) => Ok(NatureOfBusiness(natureOfBusinessForm.fill(data)))
        case None => Ok(NatureOfBusiness(natureOfBusinessForm))
      }
    }
  }

  val submit = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      natureOfBusinessForm.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(NatureOfBusiness(formWithErrors)))
        },
        validFormData => {
          s4lConnector.saveFormData(KeystoreKeys.natureOfBusiness, validFormData)
          Future.successful(Redirect(routes.DateOfIncorporationController.show()))
        }
      )
    }
  }

}
