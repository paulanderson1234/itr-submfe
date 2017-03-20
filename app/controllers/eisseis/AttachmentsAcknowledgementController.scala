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

package controllers.eisseis

import auth.{AuthorisedAndEnrolledForTAVC, EIS, SEIS, TAVCUser, VCT}
import config.{FrontendAppConfig, FrontendAuthConnector}
import common.{Constants, KeystoreKeys}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.Helpers.PreviousSchemesHelper
import models.registration.RegistrationDetailsModel
import models.submission._
import models._
import play.Logger
import play.api.mvc.{Action, AnyContent, Request, Result}
import services.{FileUploadService, RegistrationDetailsService}
import uk.gov.hmrc.play.frontend.controller.FrontendController
import utils.{Converters, Validation}
import play.api.i18n.Messages.Implicits._
import play.api.Play.current
import config.FrontendGlobal.internalServerErrorTemplate

import scala.concurrent.Future

object AttachmentsAcknowledgementController extends AttachmentsAcknowledgementController{
  override lazy val s4lConnector = S4LConnector
  override lazy val submissionConnector = SubmissionConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  val registrationDetailsService: RegistrationDetailsService = RegistrationDetailsService
  override lazy val fileUploadService = FileUploadService
}

trait AttachmentsAcknowledgementController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  override val acceptedFlows = Seq(Seq(EIS,SEIS,VCT),Seq(SEIS,VCT), Seq(EIS,SEIS))

  val submissionConnector: SubmissionConnector
  val registrationDetailsService: RegistrationDetailsService
  val fileUploadService: FileUploadService


  //noinspection ScalaStyle
  val show = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    (for {
    // minimum required fields to continue
      kiProcModel <- s4lConnector.fetchAndGetFormData[KiProcessingModel](KeystoreKeys.kiProcessingModel)
      natureOfBusiness <- s4lConnector.fetchAndGetFormData[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness)
      contactDetails <- s4lConnector.fetchAndGetFormData[ContactDetailsModel](KeystoreKeys.contactDetails)
      proposedInvestment <- s4lConnector.fetchAndGetFormData[ProposedInvestmentModel](KeystoreKeys.proposedInvestment)
      investmentGrow <- s4lConnector.fetchAndGetFormData[InvestmentGrowModel](KeystoreKeys.investmentGrow)
      dateOfIncorporation <- s4lConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation)
      contactAddress <- s4lConnector.fetchAndGetFormData[AddressModel](KeystoreKeys.contactAddress)
      tavcRef <- getTavCReferenceNumber()
      schemeType <- s4lConnector.fetchAndGetFormData[SchemeTypesModel](KeystoreKeys.selectedSchemes)
      registrationDetailsModel <- registrationDetailsService.getRegistrationDetails(tavcRef)

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

      result <- createSubmissionDetailsModel(kiProcModel, natureOfBusiness, contactDetails, proposedInvestment,
        investmentGrow, dateOfIncorporation, contactAddress, schemeType, tavcRef, subsidiariesSpendInvest, subsidiariesNinetyOwned,
        previousSchemes.toList, commercialSale, newGeographicalMarket, newProduct, tenYearPlan, operatingCosts, turnoverCosts, registrationDetailsModel)
    } yield result) recover {
      case e: Exception => {
        Logger.warn(s"[AttachmentsAcknowledgementController][submit] - SEIS - Exception: ${e.getMessage}")
        InternalServerError(internalServerErrorTemplate)
      }
    }
  }

  def submit: Action[AnyContent] = AuthorisedAndEnrolled.apply { implicit user => implicit request =>
    Redirect(controllers.feedback.routes.FeedbackController.show().url)
  }

  //noinspection ScalaStyle
  //TODO:
  // 1) subsidiary performing trade name/adress is required if subsidiary and needs retrieving (post MVP)
  private def createSubmissionDetailsModel(
                                            //required
                                            kiProcModel: Option[KiProcessingModel],
                                            natOfBusiness: Option[NatureOfBusinessModel],
                                            contactDetails: Option[ContactDetailsModel],
                                            proposedInvestment: Option[ProposedInvestmentModel],
                                            investmentGrowModel: Option[InvestmentGrowModel],
                                            dateOfIncorporation: Option[DateOfIncorporationModel],
                                            contactAddress: Option[AddressModel],
                                            schemeType: Option[SchemeTypesModel],
                                            tavcReferenceNumber: String,

                                            // potentially optional or potentially required
                                            subsidiariesSpendInvest: Option[SubsidiariesSpendingInvestmentModel],
                                            subsidiariesNinetyOwned: Option[SubsidiariesNinetyOwnedModel],
                                            previousSchemes: List[PreviousSchemeModel],
                                            commercialSale: Option[CommercialSaleModel],
                                            newGeographicalMarket: Option[NewGeographicalMarketModel],
                                            newProduct: Option[NewProductModel],
                                            tenYearPlan: Option[TenYearPlanModel],
                                            operatingCosts: Option[OperatingCostsModel],
                                            turnoverCosts: Option[AnnualTurnoverCostsModel],
                                            registrationDetailsModel: Option[RegistrationDetailsModel])
                                          (implicit request: Request[AnyContent], user: TAVCUser): Future[Result] = {

    val tempAddress = None
    val tempSubsidiaryTradeName = "Subsidiary Company Name Ltd"

    (kiProcModel, natOfBusiness, contactDetails, proposedInvestment, investmentGrowModel, dateOfIncorporation,
      contactAddress, registrationDetailsModel, schemeType) match {
      case (Some(ki), Some(natureBusiness), Some(cntDetail), Some(propInv), Some(howInvGrow), Some(dateIncorp),
      Some(cntAddress), Some(regDetail), Some(schType)) => {

        // maybe enhance validation here later (validate Ki and description, validate subsid = yes and ninety etc.)
        val submission = Submission(AdvancedAssuranceSubmissionType(
          agentReferenceNumber = None, acknowledgementReference = None,
          natureOfBusinessModel = natureBusiness, contactDetailsModel = cntDetail, proposedInvestmentModel = propInv,
          investmentGrowModel = howInvGrow, correspondenceAddress = cntAddress,
          schemeTypes = schType,
          marketInfo = buildMarketInformation(ki, newGeographicalMarket, newProduct),
          dateTradeCommenced = Constants.standardIgnoreYearValue,
          annualCosts = if (operatingCosts.nonEmpty)
            Some(Converters.operatingCostsToList(operatingCosts.get))
          else None,
          annualTurnover = if (turnoverCosts.nonEmpty)
            Some(Converters.turnoverCostsToList(turnoverCosts.get))
          else None,
          knowledgeIntensive = buildKnowledgeIntensive(ki, tenYearPlan),
          subsidiaryPerformingTrade = buildSubsidiaryPerformingTrade(subsidiariesSpendInvest,
            subsidiariesNinetyOwned, tempSubsidiaryTradeName, tempAddress),
          organisationDetails = buildOrganisationDetails(commercialSale, dateOfIncorporation.get, regDetail.organisationName
            , regDetail.addressModel, previousSchemes)
        ))

        val submissionResponseModel = submissionConnector.submitAdvancedAssurance(submission, tavcReferenceNumber)
        def ProcessResult: Future[Result] = {
          submissionResponseModel.map { submissionResponse =>
            submissionResponse.status match {
              case OK =>
                s4lConnector.clearCache()
                Ok(views.html.eis.checkAndSubmit.AttachmentsAcknowledgement(submissionResponse.json.as[SubmissionResponse]))
              case _ => {
                Logger.warn(s"[AttachmentsAcknowledgementController][createSubmissionDetailsModel] [ProcessResul]- HTTP Submission failed. Response Code: ${submissionResponse.status}")
                InternalServerError
              }
            }
          }
        }.recover{
          case e: Exception => {
            Logger.warn(s"[AttachmentsAcknowledgementController][submit] - Exception submitting application: ${e.getMessage}")
            InternalServerError(internalServerErrorTemplate)
          }
        }

        def ProcessResultUpload: Future[Result] = {
          submissionResponseModel.flatMap { submissionResponse =>
            submissionResponse.status match {
              case OK =>
                s4lConnector.fetchAndGetFormData[String](KeystoreKeys.envelopeId).flatMap {
                  envelopeId => fileUploadService.closeEnvelope(tavcReferenceNumber, envelopeId.fold("")(_.toString)).map {
                    result => result.status match {
                      case OK =>
                        s4lConnector.clearCache()
                        Ok(views.html.eis.checkAndSubmit.AttachmentsAcknowledgement(submissionResponse.json.as[SubmissionResponse]))
                      case _ => s4lConnector.clearCache()
                        InternalServerError
                    }
                  }
                }
              case _ => {
                Logger.warn(s"[AttachmentsAcknowledgementController][createSubmissionDetailsModel][ProcessResultUpload] - HTTP Submission failed. Response Code: ${submissionResponse.status}")
                Future.successful(InternalServerError)
              }
            }
          }

        }.recover{
          case e: Exception => {
            Logger.warn(s"[AttachmentsAcknowledgementController][submit] - Exception submitting application: ${e.getMessage}")
            InternalServerError(internalServerErrorTemplate)
          }
        }

        if (fileUploadService.getUploadFeatureEnabled) ProcessResultUpload else ProcessResult
      }

      // inconsistent state send to start
      case (_, _, _, _, _, _, _, _, _) => {
        Logger.warn(s"[AttachmentsAcknowledgementController][createSubmissionDetailsModel] - Submission failed mandatory models check. TAVC Reference Number is: $tavcReferenceNumber")
        Future.successful(Redirect(controllers.routes.ApplicationHubController.show()))
      }
    }
  }

  private def buildKnowledgeIntensive(ki: KiProcessingModel, tenYearPlan: Option[TenYearPlanModel]): Option[KiModel] = {
    if (ki.companyAssertsIsKi.getOrElse(false))
      Some(KiModel(skilledEmployeesConditionMet = ki.hasPercentageWithMasters.getOrElse(false),
        innovationConditionMet = if (tenYearPlan.nonEmpty) tenYearPlan.get.tenYearPlanDesc else None,
        kiConditionMet = ki.isKi))
    else None
  }

  private def buildMarketInformation(ki: KiProcessingModel, newGeographicalMarket: Option[NewGeographicalMarketModel],
                                     newProduct: Option[NewProductModel]): Option[SubmitMarketInfoModel] = {

    if (newGeographicalMarket.nonEmpty || newProduct.nonEmpty) Some(SubmitMarketInfoModel(
      newGeographicalMarketModel = newGeographicalMarket.get, newProductModel = newProduct.get))
    else None
  }

  private def buildSubsidiaryPerformingTrade(subsidiariesSpendInvest: Option[SubsidiariesSpendingInvestmentModel],
                                             subsidiariesNinetyOwned: Option[SubsidiariesNinetyOwnedModel],
                                             tradeName: String,
                                             tradeAddress: Option[AddressModel]): Option[SubsidiaryPerformingTradeModel] = {

    if (subsidiariesSpendInvest.fold("")(_.subSpendingInvestment) == Constants.StandardRadioButtonYesValue)
      Some(SubsidiaryPerformingTradeModel(ninetyOwnedModel = subsidiariesNinetyOwned.get,
        organisationName = tradeName, companyAddress = tradeAddress,
        ctUtr = None, crn = None))
    else None
  }

  private def buildOrganisationDetails(commercialSale: Option[CommercialSaleModel],
                                       dateOfIncorporation: DateOfIncorporationModel,
                                       companyName: String,
                                       companyAddress: AddressModel,
                                       previousSchemes: List[PreviousSchemeModel]): OrganisationDetailsModel = {

    OrganisationDetailsModel(utr = None, organisationName = companyName, chrn = None, startDate = dateOfIncorporation,
      firstDateOfCommercialSale = if (commercialSale.fold("")(_.hasCommercialSale) == Constants.StandardRadioButtonYesValue) {
        val date = commercialSale.get
        Some(Validation.dateToDesFormat(date.commercialSaleDay.get, date.commercialSaleMonth.get, date.commercialSaleYear.get))
      } else None,
      ctUtr = None, crn = None, companyAddress = Some(companyAddress),
      previousRFIs = if (previousSchemes.nonEmpty) Some(previousSchemes) else None)
  }

}
