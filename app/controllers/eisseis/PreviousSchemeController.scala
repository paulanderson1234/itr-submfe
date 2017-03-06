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

import auth._
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.predicates.FeatureSwitch
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import controllers.Helpers.{ControllerHelpers, EisSeisHelper, PreviousSchemesHelper}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import scala.concurrent.Future
import views.html.eisseis.previousInvestment.PreviousScheme
import forms.PreviousSchemeForm._
import models.PreviousSchemeModel

object PreviousSchemeController extends PreviousSchemeController
{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait PreviousSchemeController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch {

  override val acceptedFlows = Seq(Seq(EIS, SEIS, VCT), Seq(SEIS, VCT), Seq(EIS, SEIS))

  def show(id: Option[Int]): Action[AnyContent] = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      def routeRequest(backUrl: Option[String]) = {
        if (backUrl.isDefined) {
          id match {
            case Some(idVal) => {
              PreviousSchemesHelper.getExistingInvestmentFromKeystore(s4lConnector, idVal).map {
                case Some(data) => Ok(PreviousScheme(previousSchemeForm.fill(data), backUrl.get))
                case None => Ok(PreviousScheme(previousSchemeForm, backUrl.get))
              }
            }
            case None => {
              Future.successful(Ok(PreviousScheme(previousSchemeForm, backUrl.get)))
            }
          }
        } else {
          // no back link - send to beginning of flow
          Future.successful(Redirect(routes.HadPreviousRFIController.show()))
        }
      }
      for {
        link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkPreviousScheme, s4lConnector)
        route <- routeRequest(link)
      } yield route
    }
  }

  def submit: Action[AnyContent] = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      previousSchemeForm.bindFromRequest().fold(
        formWithErrors => {
          ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkPreviousScheme, s4lConnector).flatMap(url =>
            Future.successful(BadRequest(PreviousScheme(formWithErrors, url.get))))
        },
        validFormData => {
          s4lConnector.saveFormData(KeystoreKeys.backLinkReviewPreviousSchemes, routes.PreviousSchemeController.show().url)
          validFormData.processingId match {
            case Some(_) => PreviousSchemesHelper.updateKeystorePreviousInvestment(s4lConnector, validFormData).flatMap {
              _ => routeRequest(validFormData)
            }
            case None => PreviousSchemesHelper.addPreviousInvestmentToKeystore(s4lConnector, validFormData).flatMap {
              _ => routeRequest(validFormData)
            }
          }
        }
      )
    }
  }

  private def routeRequest(previousSchemeModel: PreviousSchemeModel)
                          (implicit request: Request[AnyContent], user: TAVCUser): Future[Result] = {
    if (previousSchemeModel.schemeTypeDesc == Constants.schemeTypeVct || previousSchemeModel.schemeTypeDesc == Constants.schemeTypeEis) {
      EisSeisHelper.setIneligiblePreviousSchemeTypeCondition(s4lConnector, previousSchemeTypeConditionIneligible = true)
      Future.successful(Redirect(routes.InvalidPreviousSchemeController.show()))

    } else {
      EisSeisHelper.updateIneligiblePreviousSchemeTypeCondition(s4lConnector)
      Future.successful(Redirect(routes.ReviewPreviousSchemesController.show()))
    }
  }

}

