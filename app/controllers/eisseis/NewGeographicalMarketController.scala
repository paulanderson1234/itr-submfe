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
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.ControllerHelpers
import controllers.predicates.FeatureSwitch
import forms.NewGeographicalMarketForm._
import models.NewGeographicalMarketModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future
import views.html.eisseis.investment.NewGeographicalMarket

object NewGeographicalMarketController extends NewGeographicalMarketController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait NewGeographicalMarketController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(EIS,SEIS,VCT),Seq(SEIS,VCT), Seq(EIS,SEIS))

  val show = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      def routeRequest(backUrl: Option[String]) = {
        if (backUrl.isDefined) {
          s4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](KeystoreKeys.newGeographicalMarket) map {
            case Some(data) => Ok(NewGeographicalMarket(newGeographicalMarketForm.fill(data), backUrl.get))
            case None => Ok(NewGeographicalMarket(newGeographicalMarketForm, backUrl.get))
          }
        }
        else Future.successful(Redirect(routes.ProposedInvestmentController.show()))
      }

      for {
        link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkNewGeoMarket, s4lConnector)
        route <- routeRequest(link)
      } yield route
    }
  }

  val submit = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      newGeographicalMarketForm.bindFromRequest.fold(
        invalidForm =>
          ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkNewGeoMarket, s4lConnector).flatMap {
            case Some(data) => Future.successful(BadRequest(NewGeographicalMarket(invalidForm, data)))
            case None => Future.successful(Redirect(routes.ProposedInvestmentController.show()))

          },
        validForm => {
          s4lConnector.saveFormData[NewGeographicalMarketModel](KeystoreKeys.newGeographicalMarket, validForm)
          Future.successful(Redirect(routes.NewProductController.show()))
        }
      )
    }
  }

}
