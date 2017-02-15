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

import java.time.ZoneId
import java.util.Date

import auth.{MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class ProposedInvestmentControllerSpec extends BaseSpec {

  object TestController extends ProposedInvestmentController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val submissionConnector = mockSubmissionConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  val date = new Date()
  val localDate = date.toInstant.atZone(ZoneId.systemDefault()).toLocalDate

  val todayDay: String = localDate.getDayOfMonth.toString
  val todayMonth: String = localDate.getMonthValue.toString
  val todayYear: String = localDate.getYear.toString

  // 3 year boundary dates (at, below, above) from today
  val date3YearsAgo = localDate.minusYears(3)
  val date3YearsAgoDay: Int = date3YearsAgo.getDayOfMonth
  val date3YearsAgoMonth: Int = date3YearsAgo.getMonthValue
  val date3YearsAgoYear: Int = date3YearsAgo.getYear

  val date3YearsOneDay = localDate.minusYears(3).minusDays(1)
  val date3YearsOneDayDay: Int = date3YearsOneDay.getDayOfMonth
  val date3YearsOneDayMonth: Int = date3YearsOneDay.getMonthValue
  val date3YearsOneDayYear: Int = date3YearsOneDay.getYear

  val date3YearsLessOneDay = localDate.minusYears(3).plusDays(1)
  val date3yearsLessOneDayDay: Int = date3YearsLessOneDay.getDayOfMonth
  val date3YearsLessOneDayMonth: Int = date3YearsLessOneDay.getMonthValue
  val date3YearsLessOneDayYear: Int = date3YearsLessOneDay.getYear

  // 1 year
  val date1YearAgo = localDate.minusYears(1)
  val date1YearAgoDay: Int = date1YearAgo.getDayOfMonth
  val date1YearAgoMonth: Int = date1YearAgo.getMonthValue
  val date1YearAgoYear: Int = date1YearAgo.getYear

  // 7 year boundary dates (at, below, above) from today
  val date7YearsAgo = localDate.minusYears(7)
  val date7YearsAgoDay: Int = date7YearsAgo.getDayOfMonth
  val date7YearsAgoMonth: Int = date7YearsAgo.getMonthValue
  val date7YearsAgoYear: Int = date7YearsAgo.getYear

  val date7YearsOneDay = localDate.minusYears(7).minusDays(1)
  val date7YearsOneDayDay: Int = date7YearsOneDay.getDayOfMonth
  val date7YearsOneDayMonth: Int = date7YearsOneDay.getMonthValue
  val date7YearsOneDayYear: Int = date7YearsOneDay.getYear

  val date7YearsLessOneDay = localDate.minusYears(7).plusDays(1)
  val date7YearsLessOneDayDay: Int = date7YearsLessOneDay.getDayOfMonth
  val date7YearsLessOneDayMonth: Int = date7YearsLessOneDay.getMonthValue
  val date7YearsLessOneDayYear: Int = date7YearsLessOneDay.getYear

  // 10 year boundary dates (at, below, above) from today
  val date10YearsAgo = localDate.minusYears(10)
  val date10YearsAgoDay: Int = date10YearsAgo.getDayOfMonth
  val date10YearsAgoMonth: Int = date10YearsAgo.getMonthValue
  val date10YearsAgoYear: Int = date10YearsAgo.getYear

  val date10YearsOneDay = localDate.minusYears(10).minusDays(1)
  val date10YearsOneDayDay: Int = date10YearsOneDay.getDayOfMonth
  val date10YearsOneDayMonth: Int = date10YearsOneDay.getMonthValue
  val date10YearsOneDayYear: Int = date10YearsOneDay.getYear

  val date10YearsLessOneDay = localDate.minusYears(10).plusDays(1)
  val date10YearsLessOneDayDay: Int = date10YearsLessOneDay.getDayOfMonth
  val date10YearsLessOneDayMonth: Int = date10YearsLessOneDay.getMonthValue
  val date10YearsLessOneDayYear: Int = date10YearsLessOneDay.getYear

  val keyStoreSavedHadPreviousRFIYes = HadPreviousRFIModel(Constants.StandardRadioButtonYesValue)
  val keyStoreSavedHadPreviousRFINo = HadPreviousRFIModel(Constants.StandardRadioButtonNoValue)
  val keyStoreSavedHadPreviousRFIEmpty = HadPreviousRFIModel("")
  val keyStoreSavedSubsidiariesYes = SubsidiariesModel(Constants.StandardRadioButtonYesValue)
  val keyStoreSavedSubsidiariesNo = SubsidiariesModel(Constants.StandardRadioButtonNoValue)
  val keyStoreSavedSubsidiariesEmpty = SubsidiariesModel("")

  val keyStoreSavedCommercialSaleNo = CommercialSaleModel(Constants.StandardRadioButtonNoValue, None, None, None)
  val keyStoreSavedCommercialSaleEmpty = CommercialSaleModel("", None, None, None)

  val keyStoreSavedDOIEmpty = DateOfIncorporationModel(None, None, None)

  // 10 year from today boundary models Commercial Sale
  val keyStoreSavedCommercialSale10Years = CommercialSaleModel(Constants.StandardRadioButtonYesValue,
    Some(date10YearsAgoDay), Some(date10YearsAgoMonth), Some(date10YearsAgoYear))
  val keyStoreSavedCommercialSale10YearsOneDay = CommercialSaleModel(Constants.StandardRadioButtonYesValue,
    Some(date10YearsOneDayDay), Some(date10YearsOneDayMonth), Some(date10YearsOneDayYear))
  val keyStoreSavedCommercialSale10YearsLessOneDay = CommercialSaleModel(Constants.StandardRadioButtonYesValue,
    Some(date10YearsLessOneDayDay), Some(date10YearsLessOneDayMonth), Some(date10YearsLessOneDayYear))

  // 7 year from today boundary models Commercial Sale
  val keyStoreSavedCommercialSale7Years = CommercialSaleModel(Constants.StandardRadioButtonYesValue,
    Some(date7YearsAgoDay), Some(date7YearsAgoMonth), Some(date7YearsAgoYear))
  val keyStoreSavedCommercialSale7YearsOneDay = CommercialSaleModel(Constants.StandardRadioButtonYesValue,
    Some(date7YearsOneDayDay), Some(date7YearsOneDayMonth), Some(date7YearsOneDayYear))
  val keyStoreSavedCommercialSale7YearsLessOneDay = CommercialSaleModel(Constants.StandardRadioButtonYesValue,
    Some(date7YearsLessOneDayDay), Some(date7YearsLessOneDayMonth), Some(date7YearsLessOneDayYear))

  val keyStoreSavedCommercialSale1Year = CommercialSaleModel(Constants.StandardRadioButtonYesValue,
    Some(date1YearAgoDay), Some(date1YearAgoMonth), Some(date1YearAgoYear))

  //3 year from today bounder models Date of Incorporation
  val keyStoreSavedDOI3Years = DateOfIncorporationModel(Some(date3YearsAgoDay), Some(date3YearsAgoMonth), Some(date3YearsAgoYear))
  val keyStoreSavedDOI3YearsOneDay = DateOfIncorporationModel(Some(date3YearsOneDayDay), Some(date3YearsOneDayMonth), Some(date3YearsOneDayYear))
  val keyStoreSavedDOI3YearsLessOneDay = DateOfIncorporationModel(Some(date10YearsLessOneDayDay),
    Some(date10YearsLessOneDayMonth), Some(date10YearsLessOneDayYear))

  val keyStoreSavedDOI10Years = DateOfIncorporationModel(Some(date10YearsAgoDay), Some(date10YearsAgoMonth), Some(date10YearsAgoYear))

  val modelProposedInvestment = ProposedInvestmentModel(12345)
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(modelProposedInvestment)))
  val keyStoreSavedProposedInvestment = ProposedInvestmentModel(12345)

  val optionKIModel = KiProcessingModel(Some(false), Some(true), Some(false), None, None, None)
  val emptyKIModel = KiProcessingModel(None, None, None, None, None, None)
  val missingCompanyAssertsIsKiKiModel = KiProcessingModel(None, Some(true), Some(true), Some(true), None, Some(true))
  val missingCostsConditionKiModel = KiProcessingModel(Some(true),Some(true),None, None, None, None)
  val missingSecondaryConditionsKiModel = KiProcessingModel(Some(true),Some(true),Some(false),Some(true),Some(true),None)
  val missingDateConditionMetKiModel = KiProcessingModel(Some(false),None,Some(true), None, None, None)

  val model1 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeEisValue, 2356, None, None, Some(4), Some(12), Some(2009), Some(1))
  val model2 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeSeisValue, 2356, Some(666), None, Some(4), Some(12), Some(2010), Some(3))
  val model3 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeAnotherValue, 2356, None, Some("My scheme"), Some(9), Some(8), Some(2010), Some(5))
  val model4 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeAnotherValue, 19999999, None, Some("My scheme"), Some(9), Some(8), Some(2010), Some(5))
  val model5 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeAnotherValue, 1, None, Some("My scheme"), Some(9), Some(8), Some(2010), Some(5))
  val model6 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeAnotherValue, 11999999, None, Some("My scheme"), Some(9), Some(8), Some(2010), Some(5))
  val model7 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeAnotherValue, 15000000, None, Some("My scheme"), Some(9), Some(8), Some(2010), Some(5))

  val previousSchemeTrueKIVectorList = Vector(model1, model2, model3)
  val previousSchemeOverTrueKIVectorList = Vector(model4, model5, model5)
  val previousSchemeFalseKIVectorList = Vector(model1, model2, model3)
  val previousSchemeOverFalseKIVectorList = Vector(model4, model5, model6)
  val previousSchemeUnderTotalAmount = Vector(model3, model5, model7)

  "ProposedInvestmentController" should {
    "use the correct keystore connector" in {
      ProposedInvestmentController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      ProposedInvestmentController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      ProposedInvestmentController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct submission connector" in {
      ProposedInvestmentController.submissionConnector shouldBe SubmissionConnector
    }
  }

  "Sending a GET request to ProposedInvestmentController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedProposedInvestment)))
      when(mockS4lConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().url)))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().url)))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request to ProposedInvestmentController without a valid backlink from keystore when authenticated and enrolled" should {
    "redirect to the beginning of the flow" in {
      when(mockS4lConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.HadPreviousRFIController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ProposedInvestmentController for first investment when more than 7 years from " +
    "Commercial sale date when not deemed knowledge intensive and lifetime limit has not been exceeded" should {
    "redirect to new geographical market page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.NewGeographicalMarketController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ProposedInvestment Controlller for first investment when more than 10 years from " +
    "Commercial sale date and not exceeded lifetime limit" should {
    "redirect to new geographical market page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale10YearsOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.NewGeographicalMarketController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ProposedInvestmentController for first investment when NOT more than 7 years from " +
    "Commercial sale date when not deemed knowledge intensive with subsidiaries and not exceeded lifetime limit" should {
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesSpendingInvestmentController.show().url)
        }
      )
    }
  }


  "Sending a valid form submit to the ProposedInvestmentController for first investment when EXACTLY 7 years from " +
    "Commercial sale date when not deemed knowledge intensive with subsidiaries and not exceeded lifetime limit" should {
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7Years)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesSpendingInvestmentController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ProposedInvestmentController for first investment when NOT more than 10 years from " +
    "Commercial sale date when it is deemed knowledge intensive with subsidiaries and not exceeded lifetime limit" should {
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesSpendingInvestmentController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ProposedInvestmentController for first investment when EXACTLY 10 years from " +
    "Commercial sale date when it is deemed knowledge intensive with subsidiaries and not exceeded lifetime limit" should {
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7Years)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesSpendingInvestmentController.show().url)
        }
      )
    }
  }


  "Sending a valid form submit to the ProposedInvestmentController for first investment is NOT more than 7 years from " +
    "Commercial sale date when it is NOT deemed knowledge intensive and without any subsidiaries and not exceeded lifetime limit" should {
    "redirect to new how-plan-to-use-investment page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvestmentGrowController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ProposedInvestmentController for first investment is EXACTLY 7 years from " +
    "Commercial sale date when it is NOT deemed knowledge intensive and without any subsidiaries and not exceeded lifetime limit" should {
    "redirect to new how-plan-to-use-investment page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7Years)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvestmentGrowController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ProposedInvestmentController for the first investment when NOT more than 10 years from " +
    "Commercial sale date and when it IS deemed knowledge intensive and without any subsidiaries and not exceeded lifetime limit" should {
    "redirect to new how-plan-to-use-investment page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale10YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvestmentGrowController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ProposedInvestmentController for the first investment when EXACTLY 10 years from " +
    "Commercial sale date and when it IS deemed knowledge intensive and without any subsidiaries and not exceeded lifetime limit" should {
    "redirect to new how-plan-to-use-investment page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale10Years)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvestmentGrowController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ProposedInvestmentController for the first investment when no commercial sale has been made " +
    "and when it IS deemed knowledge intensive and has subsidiaries and not exceeded lifetime limit" should {
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleNo)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesSpendingInvestmentController.show().url)
        }
      )
    }
  }

  // 5e
  "Sending a valid form submit to the ProposedInvestmentController for the first investment when no commercial sale has been made " +
    "and when it IS deemed knowledge intensive and does not have subsidiaries and not exceeded lifetime limit" should {
    "redirect to investment grow page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleNo)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvestmentGrowController.show().url)
        }
      )
    }
  }

  // 5f
  "Sending a valid form submit to the ProposedInvestmentController for the first investment when no commercial sale has been made " +
    "and when it IS NOT deemed knowledge intensive and does not have subsidiaries and not exceeded lifetime limit" should {
    "redirect to investment grow page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleNo)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvestmentGrowController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ProposedInvestmentController for the first investment when no commercial sale has been made " +
    "and when it IS NOT deemed knowledge intensive and has subsidiaries and not exceeded lifetime limit" should {
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleNo)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesSpendingInvestmentController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ProposedInvestmentController when investment has been used previously and a commercial sale exists" +
    "and has a date that isn't within the range and not exceeded lifetime limit" should {
    "redirect to subsidiaries page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale1Year)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ProposedInvestmentController when investment has been used previously and a commercial sale exists " +
    "and not exceeded lifetime limit" should {
    "redirect to reason used before page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.UsedInvestmentReasonBeforeController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ProposedInvestmentController when investment has been used previously and a commercial sale exists" +
    "but using different models and not exceeded lifetime limit" should {
    "redirect to reason used before page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale10YearsOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.UsedInvestmentReasonBeforeController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ProposedInvestmentController for the first investment with an empty PrevRFI and not exceeded lifetime limit" should {
    "redirect to used-investment-scheme-before page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7Years)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.HadPreviousRFIController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ProposedInvestmentController for the first investment with an empty Commercial sale " +
    "and not exceeded lifetime limit" should {
    "redirect to commercial-sale page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.CommercialSaleController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the ProposedInvestmentController for the first investment with an empty KIProcessingModel " +
    "and not exceeded lifetime limit" should {
    "redirect to date-of-incorporation page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(false)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7Years)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeTrueKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "123456")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.DateOfIncorporationController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit with exceeded lifetime allowance (true KI) to the ProposedInvestmentController when authenticated and enrolled" should {
    "redirect to the exceeded lifetime limit page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(true)))
      when(mockS4lConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeUnderTotalAmount)))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "investmentAmount" -> "5000000"
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.LifetimeAllowanceExceededController.show().url)
        }
      )
    }
  }


  "Sending a valid form submit with exceeded lifetime allowance (false KI) to the ProposedInvestmentController when authenticated and enrolled" should {
    "redirect to the exceeded lifetime limit page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(Option(true)))
      when(mockS4lConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeOverFalseKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "investmentAmount" -> "1234567"
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.LifetimeAllowanceExceededController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit with No Response from checkLifetimeAllowance API" +
    "to the ProposedInvestmentController when authenticated and enrolled" should {
    "redirect to the HadPreviousRFI page" in {
      when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeOverFalseKIVectorList)))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "investmentAmount" -> "1234567"
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.HadPreviousRFIController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the ProposedInvestmentController" should {
    "redirect to itself" in {
      when(mockS4lConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkProposedInvestment))(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().toString())))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,
        "investmentAmount" -> "")(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
