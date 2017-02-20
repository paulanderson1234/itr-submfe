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

/**
  * Copyright 2016 HM Revenue & Customs
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package controllers.eis

import auth.{AuthorisedAndEnrolledForTAVC, EIS, VCT}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.ControllerHelpers
import forms.SubsidiariesSpendingInvestmentForm._
import models.SubsidiariesSpendingInvestmentModel
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.eis.investment.SubsidiariesSpendingInvestment
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import scala.concurrent.Future

object SubsidiariesSpendingInvestmentController extends SubsidiariesSpendingInvestmentController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait SubsidiariesSpendingInvestmentController extends FrontendController with AuthorisedAndEnrolledForTAVC{

  override val acceptedFlows = Seq(Seq(EIS),Seq(VCT),Seq(EIS,VCT))

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    def routeRequest(backUrl: Option[String]) = {
      if(backUrl.isDefined) {
        s4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](KeystoreKeys.subsidiariesSpendingInvestment).map {
          case Some(data) => Ok(SubsidiariesSpendingInvestment(subsidiariesSpendingInvestmentForm.fill(data), backUrl.get))
          case None => Ok(SubsidiariesSpendingInvestment(subsidiariesSpendingInvestmentForm, backUrl.get))
        }
      }
      else Future.successful(Redirect(routes.ProposedInvestmentController.show()))
    }
    for {
      link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkSubSpendingInvestment, s4lConnector)
      route <- routeRequest(link)
    } yield route
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    subsidiariesSpendingInvestmentForm.bindFromRequest.fold(
      invalidForm =>
        ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkSubSpendingInvestment, s4lConnector).flatMap {
          case Some(data) => Future.successful(
            BadRequest(views.html.eis.investment.SubsidiariesSpendingInvestment(invalidForm, data)))
          case None => Future.successful(Redirect(routes.ProposedInvestmentController.show()))
      },
      validForm => {
        s4lConnector.saveFormData(KeystoreKeys.subsidiariesSpendingInvestment, validForm)
        validForm.subSpendingInvestment match {
          case  Constants.StandardRadioButtonYesValue  =>
            Future.successful(Redirect(routes.SubsidiariesNinetyOwnedController.show()))
          case  Constants.StandardRadioButtonNoValue =>
            s4lConnector.saveFormData(KeystoreKeys.backLinkInvestmentGrow,
              routes.SubsidiariesSpendingInvestmentController.show().url)
            Future.successful(Redirect(routes.InvestmentGrowController.show()))
        }
      }
    )
  }
}
