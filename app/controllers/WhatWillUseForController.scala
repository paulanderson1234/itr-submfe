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
import forms.CommercialSaleForm._
import forms.WhatWillUseForForm._
import models._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc.{Action, Result}
import utils.Validation
import views.html.companyDetails.CommercialSale

import scala.concurrent.Future
import views.html.investment.WhatWillUseFor

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

//    def routeRequest(PrevRFI: Option[HadPreviousRFIModel], ComSale: Option[CommercialSaleModel], HasSub: Option[SubsidiariesModel]): Future[Result] = {
//      PrevRFI match {
//        case Some(HadPreviousRFIModel("Yes")) => ComSale match {
//          case Some(CommercialSaleModel("Yes", Some(day), Some(month), Some(year))) =>
//            //Goes to Same Reason As Before
//            Future.successful(Redirect(routes.WhatWillUseForController.show()))
//
//        }
//        case Some(HadPreviousRFIModel("No")) => ComSale match {
//          case Some(CommercialSaleModel("No", Some(day), Some(month), Some(year))) => HasSub match {
//            case Some(SubsidiariesModel("Yes")) =>
//              //Goes to Subsidiaries spending investment
//              Future.successful(Redirect(routes.WhatWillUseForController.show()))
//
//            case Some(SubsidiariesModel("No")) =>
//              //Goes to How plan to use Investment
//              Future.successful(Redirect(routes.WhatWillUseForController.show()))
//          }
//        }
//        case Some(_) =>  Future.successful(Redirect(routes.WhatWillUseForController.show()))
//        case None => Future.successful(Redirect(routes.WhatWillUseForController.show()))
//      }
//    }

    def routeRequest(PrevRFI: HadPreviousRFIModel, ComSale: CommercialSaleModel, HasSub: SubsidiariesModel): Future[Result] = {
      PrevRFI match {
        case HadPreviousRFIModel("Yes") => ComSale match {
          case CommercialSaleModel("Yes", Some(day), Some(month), Some(year)) =>
              //Goes to Same Reason As Before
              Future.successful(Redirect(routes.WhatWillUseForController.show()))

          }
        case HadPreviousRFIModel("No") => ComSale match {
          case CommercialSaleModel("No", Some(day), Some(month), Some(year)) => HasSub match {
            case SubsidiariesModel("Yes") =>
              //Goes to Subsidiaries spending investment
              Future.successful(Redirect(routes.WhatWillUseForController.show()))

            case SubsidiariesModel("No") =>
              //Goes to How plan to use Investment
              Future.successful(Redirect(routes.WhatWillUseForController.show()))
            }
          }
      }
    }

    def routeRequestTwo(KIFlag: IsKnowledgeIntensiveModel, ComSale: CommercialSaleModel): Future[Result] = {

      def getAgeLimit(KIFlag: IsKnowledgeIntensiveModel): String = {
        KIFlag match {
          case IsKnowledgeIntensiveModel("Yes") => "10"
          case _ => "7"
        }
      }

      ComSale match {
        case ComSale => {
          if (Validation.checkAgeRule(ComSale.commercialSaleDay.get, ComSale.commercialSaleMonth.get, ComSale.commercialSaleYear.get, getAgeLimit(KIFlag).toInt)){
            //Goes to new geographic market
           Future.successful(Redirect(routes.WhatWillUseForController.show()))
          }
          else {
            //ERROR SCREEN
            Future.successful(Redirect(routes.WhatWillUseForController.show()))
          }
        }

      }
    }

    val response = whatWillUseForForm.bindFromRequest().fold(
      formWithErrors => {
        BadRequest(WhatWillUseFor(formWithErrors))
      },
      validFormData => {
        keyStoreConnector.saveFormData(KeystoreKeys.whatWillUseFor, validFormData)
        validFormData.whatWillUseFor match {
          case "Doing business"  => Redirect(routes.WhatWillUseForController.show())
          case "Getting ready to do business"   => Redirect(routes.WhatWillUseForController.show())
          case "Research and Development"   => Redirect(routes.WhatWillUseForController.show())
        }
      }
    )
    Future.successful(response)
  }
}
