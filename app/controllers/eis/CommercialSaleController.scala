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
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import models.{CommercialSaleModel, KiProcessingModel}
import forms.CommercialSaleForm._
import views.html.eis.companyDetails.CommercialSale
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object CommercialSaleController extends CommercialSaleController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait CommercialSaleController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS),Seq(VCT),Seq(EIS,VCT))

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    s4lConnector.fetchAndGetFormData[CommercialSaleModel](KeystoreKeys.commercialSale).map {
      case Some(data) => Ok(CommercialSale(commercialSaleForm.fill(data)))
      case None => Ok(CommercialSale(commercialSaleForm))
    }
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def routeRequest(kiModel: Option[KiProcessingModel]): Future[Result] = {
      kiModel match {
        case Some(data) if data.dateConditionMet.isEmpty =>
          Future.successful(Redirect(routes.DateOfIncorporationController.show()))
        case Some(dataWithDateCondition) =>
          if (dataWithDateCondition.dateConditionMet.get) {
            Future.successful(Redirect(routes.IsKnowledgeIntensiveController.show()))
          }
          else {
            s4lConnector.saveFormData(KeystoreKeys.backLinkSubsidiaries, routes.CommercialSaleController.show().url)
            Future.successful(Redirect(routes.SubsidiariesController.show()))
          }
        case None => Future.successful(Redirect(routes.DateOfIncorporationController.show()))
      }
    }

    commercialSaleForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(CommercialSale(formWithErrors)))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.commercialSale, validFormData)
        for {
          model <- s4lConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)
          route <- routeRequest(model)
        } yield route
      }
    )
  }
}
