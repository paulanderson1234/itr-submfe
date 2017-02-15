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

import auth.{AuthorisedAndEnrolledForTAVC, EIS, TAVCUser, VCT}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import forms.PreviousBeforeDOFCSForm._
import models.{CommercialSaleModel, KiProcessingModel, PreviousBeforeDOFCSModel, SubsidiariesModel}
import org.joda.time.DateTime
import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.play.frontend.controller.FrontendController
import play.api.mvc._
import utils.DateFormatter
import views.html.eis.investment.PreviousBeforeDOFCS
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

object  PreviousBeforeDOFCSController extends PreviousBeforeDOFCSController {
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait PreviousBeforeDOFCSController extends FrontendController with AuthorisedAndEnrolledForTAVC with DateFormatter {

  override val acceptedFlows = Seq(Seq(EIS),Seq(VCT),Seq(EIS,VCT))

  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    createResponse(None)
  }

  val submit = AuthorisedAndEnrolled.async { implicit user => implicit request =>

    def routeRequest(date: Option[SubsidiariesModel]): Future[Result] = {
      date match {
        case Some(data) if data.ownSubsidiaries == Constants.StandardRadioButtonYesValue =>
          s4lConnector.saveFormData(KeystoreKeys.backLinkSubSpendingInvestment, routes.PreviousBeforeDOFCSController.show().url)
          Future.successful(Redirect(routes.SubsidiariesSpendingInvestmentController.show()))
        case Some(_) =>
          s4lConnector.saveFormData(KeystoreKeys.backLinkInvestmentGrow, routes.PreviousBeforeDOFCSController.show().url)
          Future.successful(Redirect(routes.InvestmentGrowController.show()))
        case None => Future.successful(Redirect(routes.SubsidiariesController.show()))
      }
    }

    previousBeforeDOFCSForm.bindFromRequest().fold(
      formWithErrors => {
        createResponse(Some(formWithErrors))
      },
      validFormData => {
        s4lConnector.saveFormData(KeystoreKeys.previousBeforeDOFCS, validFormData)
        validFormData.previousBeforeDOFCS match {
          case Constants.StandardRadioButtonNoValue => {
            s4lConnector.saveFormData(KeystoreKeys.backLinkNewGeoMarket, routes.PreviousBeforeDOFCSController.show().url)
            Future.successful(Redirect(routes.NewGeographicalMarketController.show()))
          }
          case Constants.StandardRadioButtonYesValue => for {
            subsidiaries <- s4lConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries)
            route <- routeRequest(subsidiaries)
          } yield route
        }
      }
    )
  }

  private def generatePage(day: Int, month: Int, year: Int, difference: Int, formWithErrors: Option[Form[PreviousBeforeDOFCSModel]])
                          (implicit request: Request[Any], user: TAVCUser): Future[Result] = {
    val newDate = new DateTime(year,month,day,0,0).plusYears(difference)
    val convertedNewDate = toDateString(newDate.getDayOfMonth,newDate.getMonthOfYear,newDate.getYear)
    val commercialDate = toDateString(day,month,year)
    val question = Messages("page.previousInvestment.previousBeforeDOFCS.heading",commercialDate,convertedNewDate)
    val description = Messages("page.previousInvestment.previousBeforeDOFCS.description",difference)
    if(formWithErrors.isDefined) {
      Future.successful(BadRequest(PreviousBeforeDOFCS(formWithErrors.get,question,description)))
    } else {
      s4lConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](KeystoreKeys.previousBeforeDOFCS).map {
        case Some(data) => Ok(PreviousBeforeDOFCS(previousBeforeDOFCSForm.fill(data), question, description))
        case None => Ok(PreviousBeforeDOFCS(previousBeforeDOFCSForm, question, description))
      }
    }
  }

  private def handleResponse(kiProcessingModel: Option[KiProcessingModel], commercialSaleModel: Option[CommercialSaleModel],
                             formWithErrors: Option[Form[PreviousBeforeDOFCSModel]])(implicit request: Request[Any], user: TAVCUser): Future[Result] = {

    def isMissingKiData: Boolean =
      kiProcessingModel.fold(true)(kiModel => kiModel.dateConditionMet.isEmpty || kiModel.companyAssertsIsKi.isEmpty)

    def isMissingCommercialSale: Boolean =
    commercialSaleModel.fold(true)(commercialModel => commercialModel.commercialSaleDay.isEmpty
      || commercialModel.commercialSaleMonth.isEmpty
      || commercialModel.commercialSaleYear.isEmpty)

    (isMissingCommercialSale, isMissingKiData) match {
      case (false, false) => {
        (kiProcessingModel.get.isKi,formWithErrors.isDefined) match {
          case (true,false) => {
            generatePage(commercialSaleModel.get.commercialSaleDay.get,
              commercialSaleModel.get.commercialSaleMonth.get,
              commercialSaleModel.get.commercialSaleYear.get,
              Constants.IsKnowledgeIntensiveYears, None)
          }
          case (true,true) => {
            generatePage(commercialSaleModel.get.commercialSaleDay.get,
              commercialSaleModel.get.commercialSaleMonth.get,
              commercialSaleModel.get.commercialSaleYear.get,
              Constants.IsKnowledgeIntensiveYears,
              formWithErrors)
          }
          case (false,false) => {
            generatePage(commercialSaleModel.get.commercialSaleDay.get,
              commercialSaleModel.get.commercialSaleMonth.get,
              commercialSaleModel.get.commercialSaleYear.get,
              Constants.IsNotKnowledgeIntensiveYears, None)
          }
          case (false,true) => {
            generatePage(commercialSaleModel.get.commercialSaleDay.get,
              commercialSaleModel.get.commercialSaleMonth.get,
              commercialSaleModel.get.commercialSaleYear.get,
              Constants.IsNotKnowledgeIntensiveYears,
              formWithErrors)
          }
        }
      }
      case (true, _) => Future.successful(Redirect(routes.CommercialSaleController.show()))
      case (_, true) => Future.successful(Redirect(routes.DateOfIncorporationController.show()))
    }
  }

  private def createResponse(formWithErrors: Option[Form[PreviousBeforeDOFCSModel]])(implicit request: Request[Any], user: TAVCUser): Future[Result] = {
    for {
      kiModel <- s4lConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)
      commercialSale <- s4lConnector.fetchAndGetFormData[CommercialSaleModel](KeystoreKeys.commercialSale)
      result <- handleResponse(kiModel,commercialSale,formWithErrors)
    } yield result
  }
}
