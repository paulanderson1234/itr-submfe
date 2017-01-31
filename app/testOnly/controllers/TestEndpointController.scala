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

package testOnly.controllers

import auth.{AuthorisedAndEnrolledForTAVC, TAVCUser}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.Helpers.PreviousSchemesHelper
import models._
import forms._
import play.api.data.Form
import play.api.libs.json.Format
import play.api.mvc.{Action, AnyContent, Request}
import testOnly.models._
import testOnly.forms._
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.HeaderCarrier
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

import scala.concurrent.Future

trait TestEndpointController extends FrontendController with AuthorisedAndEnrolledForTAVC {

  val s4lConnector: S4LConnector
  val defaultPreviousSchemesSize = 5

  val kiProcessingModelYes = KiProcessingModel(Some(true), Some(true), Some(true), Some(true), Some(true), Some(true))
  val kiProcessingModelNo = KiProcessingModel(Some(false), Some(false), Some(false), Some(false), Some(false), Some(false))

  def showPageOne(schemes: Option[Int]): Action[AnyContent] = AuthorisedAndEnrolled.async {
    implicit user => implicit request =>
    for {
      natureOfBusinessForm <- fillForm[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness, NatureOfBusinessForm.natureOfBusinessForm)
      dateOfIncorporationForm <- fillForm[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation, DateOfIncorporationForm.dateOfIncorporationForm)
      commercialSaleForm <- fillForm[CommercialSaleModel](KeystoreKeys.commercialSale, CommercialSaleForm.commercialSaleForm)
      isKnowledgeIntensiveForm <- fillForm[IsKnowledgeIntensiveModel](KeystoreKeys.isKnowledgeIntensive, IsKnowledgeIntensiveForm.isKnowledgeIntensiveForm)
      operatingCostsForm <- fillForm[OperatingCostsModel](KeystoreKeys.operatingCosts, TestOperatingCostsForm.testOperatingCostsForm)
      percentageStaffWithMastersForm <- fillForm[PercentageStaffWithMastersModel](KeystoreKeys.percentageStaffWithMasters,
        PercentageStaffWithMastersForm.percentageStaffWithMastersForm)
      tenYearPlanForm <- fillForm[TenYearPlanModel](KeystoreKeys.tenYearPlan, TenYearPlanForm.tenYearPlanForm)
      hadPreviousRFIForm <- fillForm[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI, HadPreviousRFIForm.hadPreviousRFIForm)
      previousSchemesForm <- fillPreviousSchemesForm
    } yield Ok(
      testOnly.views.html.testEndpointPageOne(
        natureOfBusinessForm,
        dateOfIncorporationForm,
        commercialSaleForm,
        isKnowledgeIntensiveForm,
        operatingCostsForm,
        percentageStaffWithMastersForm,
        tenYearPlanForm,
        hadPreviousRFIForm,
        previousSchemesForm,
        schemes.getOrElse(defaultPreviousSchemesSize)
      )
    )
  }

  def showPageTwo: Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    for {
      proposedInvestmentForm <- fillForm[ProposedInvestmentModel](KeystoreKeys.proposedInvestment, ProposedInvestmentForm.proposedInvestmentForm)
      usedInvestmentReasonBeforeForm <- fillForm[UsedInvestmentReasonBeforeModel](KeystoreKeys.usedInvestmentReasonBefore,
        UsedInvestmentReasonBeforeForm.usedInvestmentReasonBeforeForm)
      previousBeforeDoFCSForm <- fillForm[PreviousBeforeDOFCSModel](KeystoreKeys.previousBeforeDOFCS, PreviousBeforeDOFCSForm.previousBeforeDOFCSForm)
      newGeographicalMarketForm <- fillForm[NewGeographicalMarketModel](KeystoreKeys.newGeographicalMarket, NewGeographicalMarketForm.newGeographicalMarketForm)
      newProductForm <- fillForm[NewProductModel](KeystoreKeys.newProduct, NewProductForm.newProductForm)
      turnoverCostsForm <- fillForm[AnnualTurnoverCostsModel](KeystoreKeys.turnoverCosts, TestTurnoverCostsForm.testTurnoverCostsForm)
      investmentGrowForm <- fillForm[InvestmentGrowModel](KeystoreKeys.investmentGrow, InvestmentGrowForm.investmentGrowForm)
      confirmContactDetailsForm <- fillForm[ConfirmContactDetailsModel](KeystoreKeys.confirmContactDetails, ConfirmContactDetailsForm.confirmContactDetailsForm)
      contactDetailsForm <- fillForm[ContactDetailsModel](KeystoreKeys.manualContactDetails, ContactDetailsForm.contactDetailsForm)
      confirmCorrespondAddressForm <- fillForm[ConfirmCorrespondAddressModel](KeystoreKeys.confirmContactAddress,
        ConfirmCorrespondAddressForm.confirmCorrespondAddressForm)
      contactAddressForm <- fillForm[AddressModel](KeystoreKeys.manualContactAddress, ContactAddressForm.contactAddressForm)
    } yield Ok(
      testOnly.views.html.testEndpointPageTwo(
        proposedInvestmentForm,
        usedInvestmentReasonBeforeForm,
        previousBeforeDoFCSForm,
        newGeographicalMarketForm,
        newProductForm,
        turnoverCostsForm,
        investmentGrowForm,
        confirmContactDetailsForm,
        contactDetailsForm,
        confirmCorrespondAddressForm,
        contactAddressForm
      )
    )
  }

  def submitPageOne: Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    val natureOfBusiness = bindForm[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness, NatureOfBusinessForm.natureOfBusinessForm)
    val dateOfIncorporation = bindForm[DateOfIncorporationModel](KeystoreKeys.dateOfIncorporation, DateOfIncorporationForm.dateOfIncorporationForm)
    val commercialSale = bindForm[CommercialSaleModel](KeystoreKeys.commercialSale, CommercialSaleForm.commercialSaleForm)
    val isKnowledgeIntensive = bindKIForm()
    val testOperatingCosts = bindForm[OperatingCostsModel](KeystoreKeys.operatingCosts, TestOperatingCostsForm.testOperatingCostsForm)
    val percentageStaffWithMasters = bindForm[PercentageStaffWithMastersModel](KeystoreKeys.percentageStaffWithMasters,
      PercentageStaffWithMastersForm.percentageStaffWithMastersForm)
    val tenYearPlan = bindForm[TenYearPlanModel](KeystoreKeys.tenYearPlan, TenYearPlanForm.tenYearPlanForm)
    val hadPreviousRFI = bindForm[HadPreviousRFIModel](KeystoreKeys.hadPreviousRFI, HadPreviousRFIForm.hadPreviousRFIForm)
    val testPreviousSchemes = bindPreviousSchemesForm()
    saveBackLinks()
    Future.successful(Ok(
      testOnly.views.html.testEndpointPageOne(
        natureOfBusiness,
        dateOfIncorporation,
        commercialSale,
        isKnowledgeIntensive,
        testOperatingCosts,
        percentageStaffWithMasters,
        tenYearPlan,
        hadPreviousRFI,
        testPreviousSchemes,
        defaultPreviousSchemesSize
      )
    ))
  }

  def submitPageTwo: Action[AnyContent] = AuthorisedAndEnrolled.async { implicit user => implicit request =>
    val proposedInvestment = bindForm[ProposedInvestmentModel](KeystoreKeys.proposedInvestment, ProposedInvestmentForm.proposedInvestmentForm)
    val usedInvestmentReasonBefore = bindForm[UsedInvestmentReasonBeforeModel](KeystoreKeys.usedInvestmentReasonBefore,
      UsedInvestmentReasonBeforeForm.usedInvestmentReasonBeforeForm)
    val previousBeforeDoFCS = bindForm[PreviousBeforeDOFCSModel](KeystoreKeys.previousBeforeDOFCS, PreviousBeforeDOFCSForm.previousBeforeDOFCSForm)
    val newGeographicalMarket = bindForm[NewGeographicalMarketModel](KeystoreKeys.newGeographicalMarket, NewGeographicalMarketForm.newGeographicalMarketForm)
    val newProduct = bindForm[NewProductModel](KeystoreKeys.newProduct, NewProductForm.newProductForm)
    val testTurnoverCosts = bindForm[AnnualTurnoverCostsModel](KeystoreKeys.turnoverCosts, TestTurnoverCostsForm.testTurnoverCostsForm)
    val investmentGrow = bindForm[InvestmentGrowModel](KeystoreKeys.investmentGrow, InvestmentGrowForm.investmentGrowForm)
    val confirmContactDetails = bindForm[ConfirmContactDetailsModel](KeystoreKeys.confirmContactDetails, ConfirmContactDetailsForm.confirmContactDetailsForm)
    val contactDetails = bindForm[ContactDetailsModel](KeystoreKeys.manualContactDetails, ContactDetailsForm.contactDetailsForm)
    val confirmCorrespondAddress = bindForm[ConfirmCorrespondAddressModel](KeystoreKeys.confirmContactAddress,
      ConfirmCorrespondAddressForm.confirmCorrespondAddressForm)
    val contactAddress = bindForm[AddressModel](KeystoreKeys.manualContactAddress, ContactAddressForm.contactAddressForm)
    saveBackLinks()
    Future.successful(Ok(
      testOnly.views.html.testEndpointPageTwo(
      proposedInvestment,
        usedInvestmentReasonBefore,
        previousBeforeDoFCS,
        newGeographicalMarket,
        newProduct,
        testTurnoverCosts,
        investmentGrow,
        confirmContactDetails,
        contactDetails,
        confirmCorrespondAddress,
        contactAddress
      )
    ))
  }

  private def saveBackLinks()(implicit hc: HeaderCarrier, user: TAVCUser) = {
    s4lConnector.saveFormData[Boolean](KeystoreKeys.applicationInProgress, true)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkConfirmCorrespondence, routes.TestEndpointController.showPageOne(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkIneligibleForKI, routes.TestEndpointController.showPageOne(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkInvestmentGrow, routes.TestEndpointController.showPageOne(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkNewGeoMarket, routes.TestEndpointController.showPageOne(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkPreviousScheme, routes.TestEndpointController.showPageOne(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkProposedInvestment, routes.TestEndpointController.showPageOne(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkReviewPreviousSchemes, routes.TestEndpointController.showPageOne(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkSubsidiaries, routes.TestEndpointController.showPageOne(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkSubSpendingInvestment, routes.TestEndpointController.showPageOne(None).url)
    s4lConnector.saveFormData[String](KeystoreKeys.backLinkSupportingDocs, routes.TestEndpointController.showPageOne(None).url)
  }

  def fillForm[A](s4lKey: String, form: Form[A])(implicit hc: HeaderCarrier, user: TAVCUser, format: Format[A]): Future[Form[A]] = {
    s4lConnector.fetchAndGetFormData[A](s4lKey).map {
      case Some(data) =>
        form.fill(data)
      case None => form
    }
  }

  def fillPreviousSchemesForm(implicit hc: HeaderCarrier, user: TAVCUser): Future[Form[TestPreviousSchemesModel]] = {
    PreviousSchemesHelper.getAllInvestmentFromKeystore(s4lConnector).map {
      data =>
        if(data.nonEmpty) {
          TestPreviousSchemesForm.testPreviousSchemesForm.fill(TestPreviousSchemesModel(Some(data)))
        } else {
          TestPreviousSchemesForm.testPreviousSchemesForm.fill(TestPreviousSchemesModel(None))
        }
    }
  }

  def bindForm[A](s4lKey: String, form: Form[A])(implicit request: Request[AnyContent], user: TAVCUser, format: Format[A]): Form[A] = {
    form.bindFromRequest().fold(
      formWithErrors => {
        formWithErrors
      },
      validFormData => {
        s4lConnector.saveFormData(s4lKey, validFormData)(hc,format,user)
        form.fill(validFormData)
      }
    )
  }

  def bindKIForm()(implicit request: Request[AnyContent], user: TAVCUser): Form[IsKnowledgeIntensiveModel] = {
    IsKnowledgeIntensiveForm.isKnowledgeIntensiveForm.bindFromRequest().fold(
      formWithErrors => {
        formWithErrors
      },
      validFormData => {
        if(validFormData.isKnowledgeIntensive == Constants.StandardRadioButtonYesValue)  {
          s4lConnector.saveFormData(KeystoreKeys.kiProcessingModel, kiProcessingModelYes)
          s4lConnector.saveFormData(KeystoreKeys.isKnowledgeIntensive, validFormData)
        } else {
          s4lConnector.saveFormData(KeystoreKeys.kiProcessingModel, kiProcessingModelNo)
          s4lConnector.saveFormData(KeystoreKeys.isKnowledgeIntensive, validFormData)
        }
        IsKnowledgeIntensiveForm.isKnowledgeIntensiveForm.fill(validFormData)
      }
    )
  }


  def bindPreviousSchemesForm()(implicit request: Request[AnyContent], user: TAVCUser): Form[TestPreviousSchemesModel] = {
    TestPreviousSchemesForm.testPreviousSchemesForm.bindFromRequest().fold(
      formWithErrors => {
        formWithErrors
      },
      validFormData => {
        validFormData.previousSchemes.fold(TestPreviousSchemesForm.testPreviousSchemesForm.fill(validFormData)){
          previousSchemes =>
            s4lConnector.saveFormData(KeystoreKeys.previousSchemes, previousSchemes.toVector)
            TestPreviousSchemesForm.testPreviousSchemesForm.fill(validFormData)
        }
      }
    )
  }

}

object TestEndpointController extends TestEndpointController
{
  override lazy val applicationConfig = FrontendAppConfig
  override lazy val authConnector = FrontendAuthConnector
  override lazy val enrolmentConnector = EnrolmentConnector
  override lazy val s4lConnector = S4LConnector
}