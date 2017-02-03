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

package controllers.schemeSelection

import auth.AuthorisedAndEnrolledForTAVC
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.featureSwitch.SEISFeatureSwitch
import play.api.mvc.{Action, AnyContent, Request, Result}
import forms.schemeSelection.SchemeSelectionForm._
import models.submission.SchemeTypesModel
import views.html.schemeSelection.SchemeSelection
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import uk.gov.hmrc.play.frontend.controller.FrontendController

object SchemeSelectionController extends SchemeSelectionController {
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val s4lConnector = S4LConnector
}

trait SchemeSelectionController extends FrontendController with AuthorisedAndEnrolledForTAVC with SEISFeatureSwitch {

  val s4lConnector: S4LConnector

  def show(): Action[AnyContent] = seisFeatureSwitch { AuthorisedAndEnrolled.async { implicit user => implicit request =>
      s4lConnector.fetchAndGetFormData[SchemeTypesModel](KeystoreKeys.selectedSchemes).map {
        case Some(scheme) => Ok(SchemeSelection(schemeSelectionForm.fill(scheme)))
        case _ => Ok(SchemeSelection(schemeSelectionForm))
      }
    }
  }

  def submit(): Action[AnyContent] = seisFeatureSwitch { AuthorisedAndEnrolled.apply { implicit user => implicit request =>
      schemeSelectionForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(SchemeSelection(formWithErrors))
        },
        validFormData => {
          s4lConnector.saveFormData(KeystoreKeys.selectedSchemes, validFormData)
          s4lConnector.saveFormData(KeystoreKeys.applicationInProgress, true)
          routeToScheme(validFormData)
        }
      )
    }
  }

  private def routeToScheme(schemeTypesModel: SchemeTypesModel)(implicit request: Request[AnyContent]): Result = {
    schemeTypesModel match {
      //EIS Flow
      case SchemeTypesModel(true,false,false,false) => Redirect(controllers.routes.NatureOfBusinessController.show().url)
      //SEIS Flow
      case SchemeTypesModel(false,true,false,false) => Redirect(controllers.seis.routes.NatureOfBusinessController.show().url)
      //VCT Flow
      case SchemeTypesModel(false,false,false,true) => Redirect(controllers.routes.NatureOfBusinessController.show().url)
      //EIS SEIS Flow
      case SchemeTypesModel(true,true,false,false) => Redirect(controllers.routes.NatureOfBusinessController.show().url)
      //EIS VCT Flow
      case SchemeTypesModel(true,false,false,true) => Redirect(controllers.routes.NatureOfBusinessController.show().url)
      //SEIS VCT Flow
      case SchemeTypesModel(false,true,false,true) => Redirect(controllers.routes.NatureOfBusinessController.show().url)
      //EIS SEIS VCT Flow
      case SchemeTypesModel(true,true,false,true) => Redirect(controllers.routes.NatureOfBusinessController.show().url)
      //Invalid Flow
      case _ => BadRequest(SchemeSelection(schemeSelectionForm.fill(schemeTypesModel)))
    }
  }

}
