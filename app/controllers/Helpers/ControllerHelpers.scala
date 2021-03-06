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
import common.Constants
import config.FrontendAppConfig
import models.submission.SchemeTypesModel
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Request}

import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

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

  def removeDescriptionFromTypes(schemeTypesModel: Option[SchemeTypesModel])(implicit request: Request[AnyContent], messages: Messages): String = {
    schemeTypesModel match {
      //EIS Flow
      case Some(SchemeTypesModel(true,false,false,false)) => Messages("page.deleteApplication.hub.advanced.assurance.type")
      //SEIS Flow
      case Some(SchemeTypesModel(false,true,false,false)) => Messages("page.deleteApplication.hub.seis.type")
      //VCT Flow
      case Some(SchemeTypesModel(false,false,false,true)) => Messages("page.deleteApplication.hub.vct.type")
      //EIS SEIS Flow
      case Some(SchemeTypesModel(true,true,false,false)) => Messages("page.deleteApplication.hub.eis-seis.type")
      //EIS VCT Flow
      case Some(SchemeTypesModel(true,false,false,true)) => Messages("page.deleteApplication.hub.eis-vct.type")
      //SEIS VCT Flow
      case Some(SchemeTypesModel(false,true,false,true)) => Messages("page.deleteApplication.hub.seis-vct.type")
      //EIS SEIS VCT Flow
      case Some(SchemeTypesModel(true,true,false,true)) => Messages("page.deleteApplication.hub.eis-seis-vct.type")
      //Assume EIS
      case Some(_) => Messages("page.deleteApplication.hub.advanced.assurance.type")
      //Assume EIS
      case None =>  Messages("page.deleteApplication.hub.advanced.assurance.type")
    }
  }

  def routeToCSScheme(schemeType: String)(implicit request: Request[AnyContent]): String = {
    schemeType match {
      //EIS Flow
      case Constants.schemeTypeEis => FrontendAppConfig.submissionCSFrontendServiceEISBaseUrl
      //SEIS Flow
      case Constants.schemeTypeSeis => FrontendAppConfig.submissionCSFrontendServiceSEISBaseUrl
      //Assume EIS
      case _ => FrontendAppConfig.submissionCSFrontendServiceEISBaseUrl
    }
  }


  def schemeDescriptionFromCSTypes(schemeType: String)(implicit request: Request[AnyContent], messages: Messages): String = {
    schemeType match {
      //EIS Flow
      case Constants.schemeTypeEis => Messages("page.introduction.hub.existing.compliance.statement.type")
      //SEIS Flow
      case Constants.schemeTypeSeis => Messages("page.introduction.hub.existing.compliance.statement.seis.type")
      //Assume EIS
      case _ => Messages("page.introduction.hub.existing.compliance.statement.type")
    }
  }
}
