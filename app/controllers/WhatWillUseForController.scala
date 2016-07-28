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
import forms.WhatWillUseForForm._
import models._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc.{Action, Result}
import utils.Validation
import views.html.investment.WhatWillUseFor

import scala.concurrent.Future

object WhatWillUseForController extends WhatWillUseForController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait WhatWillUseForController extends FrontendController with ValidActiveSession {

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>
    keyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](KeystoreKeys.whatWillUseFor).map {
      case Some(data) => Ok(WhatWillUseFor(whatWillUseForForm.fill(data)))
      case None => Ok(WhatWillUseFor(whatWillUseForForm))
    }
  }

  val submit = Action.async { implicit request =>

    def calcRoute(prevRFI: Option[HadPreviousRFIModel], comSale: Option[CommercialSaleModel],
                  HasSub: Option[SubsidiariesModel], kIFlag: Option[IsKnowledgeIntensiveModel]): Future[Result] = {

      def getAgeLimit(KIFlag: IsKnowledgeIntensiveModel): String = {
        KIFlag match {
          case IsKnowledgeIntensiveModel("Yes") => "10"
          case _ => "7"
        }
      }

      def subsidiariesCheck(hasSub: Option[SubsidiariesModel]): Future[Result] = {
        hasSub match {
          case Some(hasSub) => if (hasSub.ownSubsidiaries.equals("Yes")) {
            //goes to subsidiary spending investment
            Future.successful(Redirect(routes.WhatWillUseForController.show()))
          } else {
            //goes to how to plan to use investment
            Future.successful(Redirect(routes.WhatWillUseForController.show()))
          }
          case None => Future.successful(Redirect(routes.WhatWillUseForController.show()))
        }
      }

      comSale match {
        case Some(comSale) => if (comSale.hasCommercialSale.equals("Yes")) {
          prevRFI match {
            case Some(prevRFI) => if (prevRFI.hadPreviousRFI.equals("Yes")) {
              Future.successful(Redirect(routes.UsedInvestmentReasonBeforeController.show()))
            }
            else { kIFlag match {
              case Some(kIFlag) =>
                if (Validation.checkAgeRule(comSale.commercialSaleDay.get,comSale.commercialSaleMonth.get,comSale.commercialSaleYear.get,getAgeLimit(kIFlag))) {
                  //Goes to new geographic market
                  Future.successful(Redirect(routes.NewGeographicalMarketController.show()))
                }
                else {subsidiariesCheck(HasSub)}

              case None => Future.successful(Redirect(routes.WhatWillUseForController.show()))
              }

            }
            case None => Future.successful(Redirect(routes.WhatWillUseForController.show()))
          }
        }
        else {subsidiariesCheck(HasSub)}

        case None => Future.successful(Redirect(routes.WhatWillUseForController.show()))
      }

    }

    whatWillUseForForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(WhatWillUseFor(formWithErrors)))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.whatWillUseFor, validFormData)

        for {
          prevRFI <- keyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI)
          comSale <- keyStoreConnector.fetchAndGetFormData[CommercialSaleModel](KeystoreKeys.commercialSale)
          hasSub <- keyStoreConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries)
          kIFlag <- keyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](KeystoreKeys.isKnowledgeIntensive)
          route <- calcRoute(prevRFI, comSale, hasSub, kIFlag)
        } yield route
      }
    )
  }
}
