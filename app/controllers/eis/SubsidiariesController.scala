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
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.ControllerHelpers
import models.SubsidiariesModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import scala.concurrent.Future

object SubsidiariesController extends SubsidiariesController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait SubsidiariesController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS),Seq(VCT),Seq(EIS,VCT))

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def routeRequest(backUrl: Option[String]) = {
      if (backUrl.isDefined) {

//        s4lConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries) map {
//          case Some(data) => Ok(companyDetails.Subsidiaries(subsidiariesForm.fill(data), backUrl.get))
//          case None => Ok(Subsidiaries(subsidiariesForm, backUrl.get))
//        }
        // DEFAULT VALUE TO NO AND REDIRECT TO NEXT PAGE
        s4lConnector.saveFormData[SubsidiariesModel](KeystoreKeys.subsidiaries, SubsidiariesModel(Constants.StandardRadioButtonNoValue))
        Future.successful(Redirect(routes.HadPreviousRFIController.show()))
      }
      else {
        // no back link - user skipping - redirect to start of flow point
        Future.successful(Redirect(routes.DateOfIncorporationController.show()))
      }
    }

    for {
      link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkSubsidiaries, s4lConnector)
      route <- routeRequest(link)
    } yield route
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
//    subsidiariesForm.bindFromRequest.fold(
//      invalidForm => ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkSubsidiaries, s4lConnector)
//        .flatMap(url => Future.successful(BadRequest(companyDetails.Subsidiaries(invalidForm, url.
//          getOrElse(routes.DateOfIncorporationController.show().toString))))),
//      validForm => {
//        s4lConnector.saveFormData[SubsidiariesModel](KeystoreKeys.subsidiaries, validForm)
//        Future.successful(Redirect(routes.HadPreviousRFIController.show()))
//      }
//    )
    // DEFAULT VALUE TO NO AND REDIRECT TO NEXT PAGE
    s4lConnector.saveFormData[SubsidiariesModel](KeystoreKeys.subsidiaries, SubsidiariesModel(Constants.StandardRadioButtonNoValue))
    Future.successful(Redirect(routes.HadPreviousRFIController.show()))
  }
}
