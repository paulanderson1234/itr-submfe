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

package controllers.Helpers

import auth.TAVCUser
import models.submission.SchemeTypesModel
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Request}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ControllerHelpers extends ControllerHelpers {

}

trait ControllerHelpers {

  def getSavedBackLink(keystoreKey: String, s4lConnector: connectors.S4LConnector)
                      (implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[String]] = {
    s4lConnector.fetchAndGetFormData[String](keystoreKey).flatMap {
      case Some(data) => Future.successful(Some(data))
      case None => Future.successful(None)
    }
  }

  def routeToScheme(schemeTypesModel: SchemeTypesModel)(implicit request: Request[AnyContent]): String = {
    schemeTypesModel match {
      //EIS Flow
      case SchemeTypesModel(true,false,false,false) => controllers.eis.routes.NatureOfBusinessController.show().url
      //SEIS Flow
      case SchemeTypesModel(false,true,false,false) => controllers.seis.routes.NatureOfBusinessController.show().url
      //VCT Flow
      case SchemeTypesModel(false,false,false,true) => controllers.eis.routes.NatureOfBusinessController.show().url
      //EIS SEIS Flow
      case SchemeTypesModel(true,true,false,false) => controllers.eisseis.routes.NatureOfBusinessController.show().url
      //EIS VCT Flow
      case SchemeTypesModel(true,false,false,true) => controllers.eis.routes.NatureOfBusinessController.show().url
      //SEIS VCT Flow
      case SchemeTypesModel(false,true,false,true) => controllers.eisseis.routes.NatureOfBusinessController.show().url
      //EIS SEIS VCT Flow
      case SchemeTypesModel(true,true,false,true) => controllers.eisseis.routes.NatureOfBusinessController.show().url
      //Assume EIS
      case _ => controllers.eis.routes.NatureOfBusinessController.show().url
    }
  }

  def schemeDescriptionFromTypes(schemeTypesModel: Option[SchemeTypesModel])(implicit request: Request[AnyContent], messages: Messages): String = {
    schemeTypesModel match {
      //EIS Flow
      case Some(SchemeTypesModel(true,false,false,false)) => Messages("page.introduction.hub.existing.advanced.assurance.type")
      //SEIS Flow
      case Some(SchemeTypesModel(false,true,false,false)) => Messages("page.introduction.hub.existing.seis.type")
      //VCT Flow
      case Some(SchemeTypesModel(false,false,false,true)) => Messages("page.introduction.hub.existing.vct.type")
      //EIS SEIS Flow
      case Some(SchemeTypesModel(true,true,false,false)) => Messages("page.introduction.hub.existing.eis-seis.type")
      //EIS VCT Flow
      case Some(SchemeTypesModel(true,false,false,true)) => Messages("page.introduction.hub.existing.eis-vct.type")
      //SEIS VCT Flow
      case Some(SchemeTypesModel(false,true,false,true)) => Messages("page.introduction.hub.existing.seis-vct.type")
      //EIS SEIS VCT Flow
      case Some(SchemeTypesModel(true,true,false,true)) => Messages("page.introduction.hub.existing.eis-seis-vct.type")
      //Assume EIS
      case Some(_) => Messages("page.introduction.hub.existing.advanced.assurance.type")
      //Assume EIS
      case None =>  Messages("page.introduction.hub.existing.advanced.assurance.type")
    }
  }

}
