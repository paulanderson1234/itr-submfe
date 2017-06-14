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

import auth._
import common.{Constants, KeystoreKeys}
import config.FrontendGlobal.internalServerErrorTemplate
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.PreviousSchemesHelper
import controllers.predicates.FeatureSwitch
import models._
import models.submission.SchemeTypesModel
import play.api.Logger
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import services.EmailVerificationService
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import views.html.eisseis.checkAndSubmit.CheckAnswers

import scala.concurrent.Future

object CheckAnswersController extends CheckAnswersController{
  override lazy val s4lConnector = S4LConnector
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  val emailVerificationService = EmailVerificationService
}

trait CheckAnswersController extends FrontendController with AuthorisedAndEnrolledForTAVC with FeatureSwitch with PreviousSchemesHelper {

  override val acceptedFlows = Seq(Seq(EIS,SEIS,VCT),Seq(SEIS,VCT), Seq(EIS,SEIS))

  val emailVerificationService: EmailVerificationService

  def checkAnswersModel(implicit headerCarrier: HeaderCarrier, user: TAVCUser) : Future[CheckAnswersModel] = for {
    yourCompanyNeed <- s4lConnector.fetchAndGetFormData[YourCompanyNeedModel](KeystoreKeys.yourCompanyNeed)
    taxPayerReference <- s4lConnector.fetchAndGetFormData[TaxpayerReferenceModel](KeystoreKeys.taxpayerReference)
    registeredAddress <- s4lConnector.fetchAndGetFormData[RegisteredAddressModel](KeystoreKeys.registeredAddress)
    dateOfIncorporation <- s4lConnector.fetchAndGetFormData[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation)
    natureOfBusiness <- s4lConnector.fetchAndGetFormData[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness)
    commercialSale <- s4lConnector.fetchAndGetFormData[CommercialSaleModel](KeystoreKeys.commercialSale)
    isKnowledgeIntensive <- s4lConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](KeystoreKeys.isKnowledgeIntensive)
    operatingCosts <- s4lConnector.fetchAndGetFormData[OperatingCostsModel](KeystoreKeys.operatingCosts)
    percentageStaffWithMasters <- s4lConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](KeystoreKeys.percentageStaffWithMasters)
    tenYearPlan <- s4lConnector.fetchAndGetFormData[TenYearPlanModel](KeystoreKeys.tenYearPlan)
    subsidiaries <- s4lConnector.fetchAndGetFormData[SubsidiariesModel](KeystoreKeys.subsidiaries)
    hadPreviousRFI <- s4lConnector.fetchAndGetFormData[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI)
    previousSchemes <- getAllInvestmentFromKeystore(s4lConnector)
    proposedInvestment <- s4lConnector.fetchAndGetFormData[ProposedInvestmentModel](KeystoreKeys.proposedInvestment)
    usedInvestmentReasonBefore <- s4lConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](KeystoreKeys.usedInvestmentReasonBefore)
    previousBeforeDOFCS <- s4lConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](KeystoreKeys.previousBeforeDOFCS)
    newGeographicalMarket <- s4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](KeystoreKeys.newGeographicalMarket)
    newProduct <- s4lConnector.fetchAndGetFormData[NewProductModel](KeystoreKeys.newProduct)
    subsidiariesSpendingInvestment <- s4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](KeystoreKeys.subsidiariesSpendingInvestment)
    subsidiariesNinetyOwned <- s4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](KeystoreKeys.subsidiariesNinetyOwned)
    contactDetails <- s4lConnector.fetchAndGetFormData[ContactDetailsModel](KeystoreKeys.contactDetails)
    contactAddress <- s4lConnector.fetchAndGetFormData[AddressModel](KeystoreKeys.contactAddress)
    investmentGrowModel <- s4lConnector.fetchAndGetFormData[InvestmentGrowModel](KeystoreKeys.investmentGrow)
  }yield new CheckAnswersModel(yourCompanyNeed,taxPayerReference,registeredAddress,dateOfIncorporation
    ,natureOfBusiness,commercialSale,isKnowledgeIntensive,operatingCosts
    ,percentageStaffWithMasters,tenYearPlan,subsidiaries,hadPreviousRFI, previousSchemes, proposedInvestment
    ,usedInvestmentReasonBefore,previousBeforeDOFCS,newGeographicalMarket,newProduct,subsidiariesSpendingInvestment,
    subsidiariesNinetyOwned,contactDetails,contactAddress,investmentGrowModel, applicationConfig.uploadFeatureEnabled)


  def show (envelopeId: Option[String]) : Action[AnyContent]= featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      if (envelopeId.fold("")(_.toString).length > 0) {
        s4lConnector.saveFormData(KeystoreKeys.envelopeId, envelopeId.getOrElse(""))
      }

      checkAnswersModel.flatMap {
        checkAnswer =>
          (for{
            selectedSchemes <- s4lConnector.fetchAndGetFormData[SchemeTypesModel](KeystoreKeys.selectedSchemes)
            tradeStartDate <- s4lConnector.fetchAndGetFormData[TradeStartDateModel](KeystoreKeys.tradeStartDate)
            eisSeisProcessingModel <- s4lConnector.fetchAndGetFormData[EisSeisProcessingModel](KeystoreKeys.eisSeisProcessingModel)
          } yield {
            if(selectedSchemes.isDefined) Ok(CheckAnswers(checkAnswer,tradeStartDate,selectedSchemes.get, eisSeisProcessingModel.get))
            else Redirect(controllers.routes.ApplicationHubController.show())
          }).recover {
            case e: Exception => Logger.warn(s"[CheckAnswersController][show] Exception calling fetchAndGetFormData: ${e.getMessage}")
              InternalServerError(internalServerErrorTemplate)
          }
      }.recover {
        case e: Exception => Logger.warn(s"[CheckAnswersController][show] Exception calling checkAnswersModel: ${e.getMessage}")
          InternalServerError(internalServerErrorTemplate)
      }
    }
  }

  val submit = featureSwitch(applicationConfig.eisseisFlowEnabled) {
    AuthorisedAndEnrolled.async { implicit user => implicit request =>
      val verifyStatus = for {
        contactDetails <- s4lConnector.fetchAndGetFormData[ContactDetailsModel](KeystoreKeys.contactDetails)
        isVerified <- emailVerificationService.verifyEmailAddress(contactDetails.get.email)
      } yield isVerified.getOrElse(false)

      verifyStatus.flatMap {
        case true => {
          s4lConnector.fetchAndGetFormData[String](KeystoreKeys.envelopeId).flatMap {
            envelopeId => {
              if (envelopeId.isEmpty)
                Future.successful(Redirect(routes.AcknowledgementController.show()))
              else
                Future.successful(Redirect(routes.AttachmentsAcknowledgementController.show()))
            }
          }
        }
        case false => Future.successful(Redirect(routes.EmailVerificationController.verify(Constants.ContactDetailsReturnUrl)))
      }
    }
  }

}
