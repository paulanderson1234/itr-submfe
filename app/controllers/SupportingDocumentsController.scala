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

import auth.AuthorisedForTAVC
import common.KeystoreKeys
import config.{FrontendAuthConnector, FrontendAppConfig}
import connectors.KeystoreConnector
import controllers.Helpers.ControllerHelpers
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.supportingDocuments.SupportingDocuments

import scala.concurrent.Future

object SupportingDocumentsController extends SupportingDocumentsController
{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
}

trait SupportingDocumentsController extends FrontendController with AuthorisedForTAVC {

  val keyStoreConnector: KeystoreConnector

  val show = Authorised.async { implicit user => implicit request =>

    ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkSupportingDocs, keyStoreConnector)(hc).flatMap {
      case Some(backlink) => Future.successful(Ok(SupportingDocuments(backlink)))
      case None => Future.successful(Redirect(routes.ConfirmCorrespondAddressController.show()))
    }
  }

  val submit = Authorised.async { implicit user => implicit request =>
    Future.successful(Redirect(routes.CheckAnswersController.show()))
  }
}
