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
import connectors.{EnrolmentConnector, KeystoreConnector}
import forms.PreviousBeforeDOFCSForm._
import models.{PreviousBeforeDOFCSModel, SubsidiariesModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import views.html.investment.PreviousBeforeDOFCS

import scala.concurrent.Future
import views.html._

object  PreviousBeforeDOFCSController extends PreviousBeforeDOFCSController {
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait PreviousBeforeDOFCSController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  val keyStoreConnector: KeystoreConnector

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    keyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](KeystoreKeys.previousBeforeDOFCS).map {
      case Some(data) => Ok(investment.PreviousBeforeDOFCS(previousBeforeDOFCSForm.fill(data)))
      case None => Ok(investment.PreviousBeforeDOFCS(previousBeforeDOFCSForm))
    }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def routeRequest(date: Option[SubsidiariesModel]): Future[Result] = {
      date match {
        case Some(data) if data.ownSubsidiaries == Constants.StandardRadioButtonYesValue =>
          keyStoreConnector.saveFormData(KeystoreKeys.backLinkSubSpendingInvestment, routes.PreviousBeforeDOFCSController.show().toString())
          Future.successful(Redirect(routes.SubsidiariesSpendingInvestmentController.show()))
        case Some(_) =>
          keyStoreConnector.saveFormData(KeystoreKeys.backLinkInvestmentGrow, routes.PreviousBeforeDOFCSController.show().toString())
          Future.successful(Redirect(routes.InvestmentGrowController.show()))
        case None => Future.successful(Redirect(routes.SubsidiariesController.show()))
      }
    }

    previousBeforeDOFCSForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(PreviousBeforeDOFCS(formWithErrors)))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.previousBeforeDOFCS, validFormData)
        validFormData.previousBeforeDOFCS match {
          case Constants.StandardRadioButtonNoValue => {
            keyStoreConnector.saveFormData(KeystoreKeys.backLinkNewGeoMarket, routes.PreviousBeforeDOFCSController.show().toString())
            Future.successful(Redirect(routes.NewGeographicalMarketController.show()))
          }
          case Constants.StandardRadioButtonYesValue => for {
            subsidiaries <- keyStoreConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries)
            route <- routeRequest(subsidiaries)
          } yield route
        }
      }
    )
  }
}
