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

package controllers.examples

import connectors.KeystoreConnector
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import models.DateOfFirstSaleModel._
import common._
import views.html._
import forms.DateOfFirstSaleForm._

import scala.concurrent.Future
import controllers.predicates.ValidActiveSession
import models.DateOfFirstSaleModel

object DateOfFirstSaleController extends DateOfFirstSaleController
{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait DateOfFirstSaleController extends FrontendController with ValidActiveSession{

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[DateOfFirstSaleModel](KeystoreKeys.dateOfFirstSaleExample).map {
      case Some(data) => Ok(examples.DateOfFirstSale(dateOfFirstSaleForm.fill(data)))
      case None => Ok(examples.DateOfFirstSale(dateOfFirstSaleForm))
    }
  }

  val submit = Action.async { implicit request =>
    val response = dateOfFirstSaleForm.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(examples.DateOfFirstSale(formWithErrors))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.dateOfFirstSaleExample, validFormData)
        Redirect(routes.DoSubmissionController.show)
      }
    )
    Future.successful(response)
  }
}
