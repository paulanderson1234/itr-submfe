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

package controllers

import common.KeystoreKeys
import connectors.KeystoreConnector

import controllers.predicates.ValidActiveSession
import forms.SubsidiariesForm._
import forms.SubsidiariesSpendingInvestmentForm._
import models.{SubsidiariesModel, PreviousBeforeDOFCSModel, NewProductModel, SubsidiariesSpendingInvestmentModel}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc.Action
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html._

import scala.concurrent.Future

object SubsidiariesSpendingInvestmentController extends SubsidiariesSpendingInvestmentController{
  val keyStoreConnector: KeystoreConnector = KeystoreConnector
}

trait SubsidiariesSpendingInvestmentController extends FrontendController with ValidActiveSession{

  val keyStoreConnector: KeystoreConnector

  val show = ValidateSession.async { implicit request =>

    def routeRequest(backUrl: String) = {
      keyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](KeystoreKeys.subsidiariesSpendingInvestment).map {
        case Some(data) => Ok(investment.SubsidiariesSpendingInvestment(subsidiariesSpendingInvestmentForm.fill(data),backUrl))
        case None => Ok(investment.SubsidiariesSpendingInvestment(subsidiariesSpendingInvestmentForm,backUrl))
      }
    }

    for {
      link <- getBackLink
      route <- routeRequest(link)
    } yield route
  }

  val submit = Action.async { implicit request =>
    subsidiariesSpendingInvestmentForm.bindFromRequest.fold(
      invalidForm => getBackLink.flatMap(url => Future.successful(BadRequest(investment.SubsidiariesSpendingInvestment(invalidForm, url)))),
      validForm => {
        keyStoreConnector.saveFormData(KeystoreKeys.subsidiariesSpendingInvestment, validForm)
        validForm.subSpendingInvestment match {
          case "Yes"  => Future.successful(Redirect(routes.SubsidiariesSpendingInvestmentController.show))
          case "No"   => Future.successful(Redirect(routes.SubsidiariesSpendingInvestmentController.show))
        }
      }
    )
  }

  def getBackLink(implicit hc: HeaderCarrier): Future[String] = {
    def routeRequest(newProduct: Option[NewProductModel], previousBeforeDOFCS : Option[PreviousBeforeDOFCSModel],
                     subsidiaries: Option[SubsidiariesModel]): String = {
      (newProduct,previousBeforeDOFCS,subsidiaries) match {
        case (Some(newProduct),_,_) => routes.NewProductController.show.toString()
        case (None,Some(previousBeforeDOFCS),_) => routes.PreviousBeforeDOFCSController.show.toString()
        case (None,None,Some(subsidiaries)) => routes.SubsidiariesController.show.toString()
        case _ => routes.SubsidiariesController.show.toString()
      }
    }

    for {
      newProduct <- keyStoreConnector.fetchAndGetFormData[NewProductModel](KeystoreKeys.newProduct)
      previousBeforeDOFCS <- keyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](KeystoreKeys.previousBeforeDOFCS)
      subsidiaries <- keyStoreConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries)
      route <- Future.successful(routeRequest(newProduct,previousBeforeDOFCS,subsidiaries))
    } yield route

  }

}
