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
import config.FrontendGlobal._
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import forms.NewProductForm._
import models.{NewGeographicalMarketModel, NewProductModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import utils.Transformers._

import scala.concurrent.Future
import views.html.eis.investment.NewProduct
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

object NewProductController extends NewProductController{
  override lazy val s4lConnector = S4LConnector
  val submissionConnector: SubmissionConnector = SubmissionConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait NewProductController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS),Seq(VCT),Seq(EIS,VCT))


  val submissionConnector: SubmissionConnector

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.fetchAndGetFormData[NewProductModel](KeystoreKeys.newProduct).map {
      case Some(data) => Ok(NewProduct(newProductForm.fill(data)))
      case None => Ok(NewProduct(newProductForm))
    }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    newProductForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(NewProduct(formWithErrors)))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.newProduct, validFormData)

        def routeRequest(continue: Option[Boolean]) = {
          continue match {
            case Some(bool) if bool => Future.successful(Redirect(routes.TurnoverCostsController.show()))
            case Some(bool)  => Future.successful(Redirect(routes.TradingForTooLongController.show()))
            case _ => Future.successful(InternalServerError(internalServerErrorTemplate))
          }
        }

        (for{
          newGeographicalMarket <- s4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](KeystoreKeys.newGeographicalMarket)
          continue <- submissionConnector.checkMarketCriteria(stringToBoolean(newGeographicalMarket.get.isNewGeographicalMarket),
            stringToBoolean(validFormData.isNewProduct))
          route <- routeRequest(continue)
        } yield route).recover {
          case _ => InternalServerError(internalServerErrorTemplate)
        }
      }
    )
  }
}
