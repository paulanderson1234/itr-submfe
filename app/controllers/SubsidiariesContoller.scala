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

import common.KeystoreKeys
import connectors.KeystoreConnector
import controllers.predicates.ValidActiveSession
import models.{DateOfIncorporationModel, SubsidiariesModel}
import forms.SubsidiariesForm._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.Validation

import scala.concurrent.Future
import views.html._
import views.html.companyDetails.Subsidiaries

object SubsidiariesController extends SubsidiariesController {
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait SubsidiariesController extends FrontendController with ValidActiveSession {

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String) = {
      keyStoreConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries) map {
        case Some(data) => Ok(companyDetails.Subsidiaries(subsidiariesForm.fill(data), backUrl))
        case None => Ok(Subsidiaries(subsidiariesForm, backUrl))
      }
    }

    for {
      link <- getBackLink
      route <- routeRequest(link)
    } yield route
  }

  val thisIsTheEquivalentOf_show = ValidateSession.async { implicit request =>
    getBackLink flatMap { link =>
      keyStoreConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries) map {
        case Some(data) => Ok(companyDetails.Subsidiaries(subsidiariesForm.fill(data), link))
        case None => Ok(Subsidiaries(subsidiariesForm, link))
      }
    }
  }

  val submit = Action.async { implicit request =>
    subsidiariesForm.bindFromRequest.fold(
      invalidForm => getBackLink.flatMap(url => Future.successful(BadRequest(companyDetails.Subsidiaries(invalidForm, url)))),
      validForm => {
        keyStoreConnector.saveFormData[SubsidiariesModel](KeystoreKeys.subsidiaries, validForm)
        Future.successful(Redirect(routes.SubsidiariesController.show()))
      }
    )
  }

  /** Generates the back link based on the data captured on a previous form and stored in keystore.
   *
   * The date of incorporation iss retrieved from keystore and used to determine the backlink Url required form the Subsidiaries view.
   * This is the same intrinsic logic used in the CommercialSale view to deetermine how this page is reached and the back button location required.
   * If the date of incorporation is not found in keystore that becomes the backlink value.
   */
  def getBackLink(implicit hc: HeaderCarrier): Future[String] = {
    def routeRequest(date: Option[DateOfIncorporationModel]): String = {
      date match {
        case Some(data) if Validation.dateAfterIncorporationRule(data.day.get, data.month.get, data.year.get) =>
          routes.IsKnowledgeIntensiveController.show.toString
        case Some(_) => routes.CommercialSaleController.show.toString
        case None => routes.DateOfIncorporationController.show.toString
      }
    }

    for {
      date <- keyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation)
      route <- Future.successful(routeRequest(date))
    } yield route

  }
}
