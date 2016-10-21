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

import auth.AuthorisedAndEnrolledForTAVC
import config.{FrontendAppConfig, FrontendAuthConnector}
import common.{Constants, KeystoreKeys}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.Helpers.PreviousSchemesHelper
import models.submission._
import models._
import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import utils.{Converters, Validation}

import scala.concurrent.Future

object AcknowledgementController extends AcknowledgementController{
  override lazy val s4lConnector = S4LConnector
  override lazy val submissionConnector = SubmissionConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
}

trait AcknowledgementController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  val s4lConnector: S4LConnector
  val submissionConnector: SubmissionConnector

  //noinspection ScalaStyle
  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    for {
    // minimum required fields to continue
      kiProcModel <- s4lConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)
      natureOfBusiness <- s4lConnector.fetchAndGetFormData[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness)
      contactDetails <- s4lConnector.fetchAndGetFormData[ContactDetailsModel](KeystoreKeys.contactDetails)
      proposedInvestment <- s4lConnector.fetchAndGetFormData[ProposedInvestmentModel](KeystoreKeys.proposedInvestment)
      investmentGrow <- s4lConnector.fetchAndGetFormData[InvestmentGrowModel](KeystoreKeys.investmentGrow)
      dateOfIncorporation <- s4lConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation)
      contactAddress <- s4lConnector.fetchAndGetFormData[AddressModel](KeystoreKeys.contactAddress)
      // company name also a required field when it is implemented

      // potentially optional or required
      operatingCosts <- s4lConnector.fetchAndGetFormData[OperatingCostsModel](KeystoreKeys.operatingCosts)
      turnoverCosts <- s4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](KeystoreKeys.turnoverCosts)
      subsidiariesSpendInvest <- s4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](KeystoreKeys.subsidiariesSpendingInvestment)
      subsidiariesNinetyOwned <- s4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](KeystoreKeys.subsidiariesNinetyOwned)
      previousSchemes <- PreviousSchemesHelper.getAllInvestmentFromKeystore(s4lConnector)
      commercialSale <- s4lConnector.fetchAndGetFormData[CommercialSaleModel](KeystoreKeys.commercialSale)
      newGeographicalMarket <- s4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](KeystoreKeys.newGeographicalMarket)
      newProduct <- s4lConnector.fetchAndGetFormData[NewProductModel](KeystoreKeys.newProduct)
      tenYearPlan <- s4lConnector.fetchAndGetFormData[TenYearPlanModel](KeystoreKeys.tenYearPlan)

      result <- createSubmissionDetailsModel(kiProcModel, natureOfBusiness, contactDetails,
        proposedInvestment, investmentGrow, dateOfIncorporation, contactAddress, subsidiariesSpendInvest, subsidiariesNinetyOwned,
        previousSchemes.toList, commercialSale, newGeographicalMarket, newProduct, tenYearPlan, operatingCosts, turnoverCosts)
    } yield result
  }


  //noinspection ScalaStyle
  //TODO:
  // 1) MostRecent year used in this function will be passed in (used to calculate period ending for costs and turnover)
  // 2) Address Models (company and correspondence) will need passing in when properly implemented
  // from ETMP lookup) to replace tempAddress used below
  // 3) 5 years Annual turnover costs to be passed as list (or converted to list) when implemented - mapping in place already
  // 4) company name required
  // 5) subsidiary performing trade name is required if subsidiary and needs retrieving
  private def createSubmissionDetailsModel(
                                       //required
                                       kiProcModel: Option[KiProcessingModel],
                                       natOfBusiness: Option[NatureOfBusinessModel],
                                       contactDetails: Option[ContactDetailsModel],
                                       proposedInvestment: Option[ProposedInvestmentModel],
                                       investmentGrowModel: Option[InvestmentGrowModel],
                                       dateOfIncorporation: Option[DateOfIncorporationModel],
                                       contactAddress: Option[AddressModel],

                                       // potentially optional or potentially required
                                       subsidiariesSpendInvest: Option[SubsidiariesSpendingInvestmentModel],
                                       subsidiariesNinetyOwned: Option[SubsidiariesNinetyOwnedModel],
                                       previousSchemes: List[PreviousSchemeModel],
                                       commercialSale: Option[CommercialSaleModel],
                                       newGeographicalMarket: Option[NewGeographicalMarketModel],
                                       newProduct: Option[NewProductModel],
                                       tenYearPlan: Option[TenYearPlanModel],
                                       operatingCosts: Option[OperatingCostsModel],
                                       turnoverCosts: Option[AnnualTurnoverCostsModel])
                                             (implicit request: Request[AnyContent]): Future[Result] = {

    val tempCompanyAddress: AddressModel = AddressModel(addressline1 = "Company line 1 Ltd",
      addressline2 = "Company Line 2", addressline3 = Some("Company Line 3"), addressline4 = Some("Company Line 4"),
      postcode = Some("TF1 4NY"), countryCode = "GB")
    val tempCompanyName = "Company Name Ltd"
    val tempSubsidiaryTradeName = "Subsidiary Company Name Ltd"
    val tempMostRecentYear = 2015

    (kiProcModel, natOfBusiness, contactDetails, proposedInvestment, investmentGrowModel, dateOfIncorporation,
      contactAddress) match {
      case (Some(ki), Some(natureBusiness), Some(cntDetail), Some(propInv), Some(howInvGrow), Some(dateIncorp),
      Some(cntAddress)) => {

        // maybe enhance validation here later (validate Ki and description, validate subsid = yes and ninety etc.)
        val submission = Submission(AdvancedAssuranceSubmissionType(
          agentReferenceNumber = None, acknowledgementReference = None,
          natureOfBusinessModel = natureBusiness, contactDetailsModel = cntDetail, proposedInvestmentModel = propInv,
          investmentGrowModel = howInvGrow, correspondenceAddress = cntAddress,
          schemeTypes = SchemeTypesModel(eis = true),
          marketInfo = buildMarketInformation(ki, newGeographicalMarket, newProduct),
          annualCosts = if (operatingCosts.nonEmpty)
            Some(Converters.operatingCostsToList(operatingCosts.get, tempMostRecentYear)) else None,
          annualTurnover = if (turnoverCosts.nonEmpty)
            Some(Converters.turnoverCostsToList(turnoverCosts.get, tempMostRecentYear)) else None,
          knowledgeIntensive = buildKnowledgeIntensive(ki, tenYearPlan),
          subsidiaryPerformingTrade = buildSubsidiaryPerformingTrade(subsidiariesSpendInvest,
            subsidiariesNinetyOwned, tempSubsidiaryTradeName, tempCompanyAddress),
          organisationDetails = buildOrganisationDetails(commercialSale, dateOfIncorporation.get, tempCompanyName
            , tempCompanyAddress, previousSchemes)
        ))

        val submissionResponseModel = submissionConnector.submitAdvancedAssurance(submission)
        submissionResponseModel.map { submissionResponse =>
          submissionResponse.status match {
            case OK => Ok(views.html.checkAndSubmit.Acknowledgement(submissionResponse.json.as[SubmissionResponse]))
            case _ => InternalServerError
          }
        }
      }

      // inconsistent state send to start
      case (_, _, _, _, _, _,_) => Future.successful(Redirect(routes.IntroductionController.show()))
    }
  }

  private def buildKnowledgeIntensive(ki: KiProcessingModel, tenYearPlan: Option[TenYearPlanModel]):Option[KiModel] = {
    if (ki.companyAssertsIsKi.getOrElse(false))
      Some(KiModel(skilledEmployeesConditionMet = ki.hasPercentageWithMasters.getOrElse(false),
        innovationConditionMet = if (tenYearPlan.nonEmpty) tenYearPlan.get.tenYearPlanDesc else None,
        kiConditionMet = ki.isKi)) else None
  }

  private def buildMarketInformation(ki: KiProcessingModel, newGeographicalMarket: Option[NewGeographicalMarketModel],
  newProduct: Option[NewProductModel]):Option[SubmitMarketInfoModel] = {

    if (newGeographicalMarket.nonEmpty || newProduct.nonEmpty) Some(SubmitMarketInfoModel(
      newGeographicalMarketModel = newGeographicalMarket.get, newProductModel = newProduct.get)) else None
  }

  private def buildSubsidiaryPerformingTrade(subsidiariesSpendInvest: Option[SubsidiariesSpendingInvestmentModel],
                                                  subsidiariesNinetyOwned: Option[SubsidiariesNinetyOwnedModel],
                                                  tradeName: String,
                                                  tradeAddress: AddressModel):Option[SubsidiaryPerformingTradeModel] = {

    if (subsidiariesSpendInvest.fold("")(_.subSpendingInvestment) == Constants.StandardRadioButtonYesValue)
      Some(SubsidiaryPerformingTradeModel(ninetyOwnedModel = subsidiariesNinetyOwned.get,
        organisationName = tradeName, companyAddress = Some(tradeAddress),
        ctUtr = None, crn = None)) else None
  }

  private def buildOrganisationDetails(commercialSale: Option[CommercialSaleModel],
                                       dateOfIncorporation: DateOfIncorporationModel,
                                       companyName: String,
                                       companyAddress: AddressModel,
                                       previousSchemes: List[PreviousSchemeModel]):OrganisationDetailsModel = {

    OrganisationDetailsModel(utr = None, organisationName = companyName, chrn = None, startDate = dateOfIncorporation,
      firstDateOfCommercialSale = if (commercialSale.fold("")(_.hasCommercialSale) == Constants.StandardRadioButtonYesValue){
        val date = commercialSale.get
        Some(Validation.dateToDesFormat(date.commercialSaleDay.get, date.commercialSaleMonth.get, date.commercialSaleYear.get))
      } else None,
      ctUtr = None, crn = None, companyAddress = Some(companyAddress),
      previousRFIs = if(previousSchemes.nonEmpty) Some(previousSchemes) else None)
  }
}
