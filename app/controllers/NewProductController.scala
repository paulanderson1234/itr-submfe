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
import forms.NewProductForm._
import models.{NewProductModel, SubsidiariesModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._

import scala.concurrent.Future
import views.html.investment.NewProduct

object NewProductController extends NewProductController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait NewProductController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  val keyStoreConnector: KeystoreConnector

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    keyStoreConnector.fetchAndGetFormData[NewProductModel](KeystoreKeys.newProduct).map {
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
        keyStoreConnector.saveFormData(KeystoreKeys.newProduct, validFormData)
        Future.successful(Redirect(routes.TurnoverCostsController.show()))
      }
    )
  }
}