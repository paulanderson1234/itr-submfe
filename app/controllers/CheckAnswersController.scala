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
import models._
import play.api.mvc.Action
import uk.gov.hmrc.play.frontend.controller.FrontendController
import views.html.checkAndSubmit.CheckAnswers

import scala.concurrent.Future

object CheckAnswersController extends CheckAnswersController{

}

trait CheckAnswersController extends FrontendController with ValidActiveSession {


  val checkAnswersModel = new CheckAnswersModel(YourCompanyNeedModel(""), TaxpayerReferenceModel(""), RegisteredAddressModel(""),
    DateOfIncorporationModel(Some(1), Some(1), Some(1990)), NatureOfBusinessModel(""), CommercialSaleModel("No", None, None, None),
    Some(IsKnowledgeIntensiveModel("")), Some(OperatingCostsModel("", "", "", "", "", "")), Some(PercentageStaffWithMastersModel("")),
    Some(TenYearPlanModel("", None)), Some(SubsidiariesModel("")), HadPreviousRFIModel(""), ProposedInvestmentModel(0), WhatWillUseForModel(""),
    Some(UsedInvestmentReasonBeforeModel("")), Some(PreviousBeforeDOFCSModel("")), Some(NewGeographicalMarketModel("")),
    Some(SubsidiariesSpendingInvestmentModel("")), Some(SubsidiariesNinetyOwnedModel("")), InvestmentGrowModel(""))

  val show = ValidateSession.async { implicit request =>
    Future.successful(Ok(CheckAnswers(checkAnswersModel)))
  }

  val submit = Action.async { implicit request =>
    Future.successful(Redirect(routes.CheckAnswersController.show()))
  }


}
