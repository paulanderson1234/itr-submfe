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

import connectors.{KeystoreConnector, SubmissionConnector}
import controllers.Helpers.{ControllerHelpers, PreviousSchemesHelper}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import models.{KiProcessingModel, ProposedInvestmentModel}
import common._
import forms.ProposedInvestmentForm._

import scala.concurrent.Future
import controllers.predicates.ValidActiveSession
import views.html.investment.ProposedInvestment

object ProposedInvestmentController extends ProposedInvestmentController
{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait ProposedInvestmentController extends FrontendController with ValidActiveSession {

  val keyStoreConnector: KeystoreConnector

  val show: Action[AnyContent] = ValidateSession.async { implicit request =>
    def routeRequest(backUrl: Option[String]) = {
      if (backUrl.isDefined) {
        keyStoreConnector.fetchAndGetFormData[ProposedInvestmentModel](KeystoreKeys.proposedInvestment).map {
          case Some(data) => Ok(ProposedInvestment(proposedInvestmentForm.fill(data), backUrl.get))
          case None => Ok(ProposedInvestment(proposedInvestmentForm, backUrl.get))
        }

      } else {
        // no back link - send to beginning of flow
        Future.successful(Redirect(routes.HadPreviousRFIController.show()))
      }
    }
    for {
      link <- ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkProposedInvestment, keyStoreConnector)(hc)
      route <- routeRequest(link)
    } yield route
  }

  val submit = Action.async { implicit request =>

    def routeRequest(kiModel: Option[KiProcessingModel], isLifeTimeAllowanceExceeded: Option[Boolean]): Future[Result] = {
      kiModel match {
        // check previous answers present
        case Some(data) if isMissingKiData(data) => {
          Future.successful(Redirect(routes.DateOfIncorporationController.show()))
        }
        case Some(dataWithPreviousValid) => {
          // all good - TODO:Save the lifetime exceeded flag? - decide how to handle. For now I put it in keystore..
          keyStoreConnector.saveFormData(KeystoreKeys.lifeTimeAllowanceExceeded, isLifeTimeAllowanceExceeded)

          // if it's exceeded go to the error page
          if (isLifeTimeAllowanceExceeded.getOrElse(false)) {
            Future.successful(Redirect(routes.LifetimeAllowanceExceededController.show()))
          }
          else {
            // not exceeded - continue
            Future.successful(Redirect(routes.WhatWillUseForController.show()))
          }
        }
        case None => Future.successful(Redirect(routes.DateOfIncorporationController.show()))
      }
    }

    proposedInvestmentForm.bindFromRequest().fold(
      formWithErrors => {
        ControllerHelpers.getSavedBackLink(KeystoreKeys.backLinkProposedInvestment, keyStoreConnector).flatMap(url =>
          Future.successful(BadRequest(ProposedInvestment(formWithErrors,
            url.getOrElse(routes.HadPreviousRFIController.show().toString)))))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.proposedInvestment, validFormData)
        for {
          kiModel <- keyStoreConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)
          previousInvestments <- PreviousSchemesHelper.getPreviousInvestmentTotalFromKeystore(keyStoreConnector)
          // Call API

          isLifeTimeAllowanceExceeded <- SubmissionConnector.checkLifetimeAllowanceExceeded(
            if (kiModel.isDefined) kiModel.get.isKi else false, previousInvestments,
            validFormData.investmentAmount) //TO DO - PROPER API CALL

          route <- routeRequest(kiModel, isLifeTimeAllowanceExceeded)
        } yield route
      }
    )
  }

  def isMissingKiData(data: KiProcessingModel): Boolean = {

    false
//    if (data.dateConditionMet.isEmpty) {
//      println("===================1")
//      true
//    }
//    else if (data.dateConditionMet.get) {
//      println("===================2")
//      data.companyAssertsIsKi.isEmpty
//    }
//    else if (data.companyAssertsIsKi.get && !data.costsConditionMet.getOrElse(false)) {
//      println("===================3")
//      data.companyAssertsIsKi.isEmpty || data.costsConditionMet.isEmpty
//    }
//    else if (data.companyAssertsIsKi.get && data.costsConditionMet.getOrElse(false)) {
//      println("===================4")
//      data.companyAssertsIsKi.isEmpty || data.costsConditionMet.isEmpty || data.secondaryCondtionsMet.isEmpty
//    } else {
//      println("===================5")
//      false
//    }
  }
}

