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

///*
// * Copyright 2016 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
package controllers

import java.net.URLEncoder
import java.time.ZoneId
import java.util.{Date, UUID}

import auth.{Enrolment, Identifier, MockAuthConnector, MockConfig}
import builders.SessionBuilder
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import helpers.FakeRequestHelper
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class WhatWillUseForControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite with FakeRequestHelper {

  val mockS4lConnector = mock[S4LConnector]

  object WhatWillUseForControllerTest extends WhatWillUseForController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    val s4lConnector: S4LConnector = mockS4lConnector
    override lazy val enrolmentConnector = mock[EnrolmentConnector]
  }

  private def mockEnrolledRequest = when(WhatWillUseForControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TavcReference","1234")),"Activated"))))

  private def mockNotEnrolledRequest = when(WhatWillUseForControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(None))

//  //Gary TEST SCENARIOS
//  /*
//
//    3) Investment previously(some previous RFI = Yes) and Commercial sale (Some) => Same Reason AS Before
//
//    4) This is first Investment Scheme(some previous RFI = No) and it is more than 7 years from first commercial sale(SOME > 7 years i.e. (7 years plus one day))
//       and NOT deemed knowledge intensive (None) => New Geo Market
//
//   4a) This is first Investment Scheme(some previous RFI = No) and it is more than 10 years from first commercial sale(SOME > 10 years (i.e. 10 years plus one day))
//       and it is deemed knowledge intensive(Some) => New Geo Market
//
//    5) This is first Investment Scheme (some previous RFI = No) and it is NOT more than 7 years from first commercial sale(SOME < 7 years (i.e. 7 years less one day))
//       and NOT deemed knowledge intensive(No) and has subsidiaries(Some) => subsidiaries spending investment
//
//      5 boundary) ** BOUNDARY TESTS for the above: repeat above but with exactly 7 years from commercial sale (still not more than 7 years)
//            => subsidiaries spending investment
//
//    5a) This is first Investment Scheme (some previous RFI = No) and it is NOT more than 10 years from first commercial sale(SOME < 10 years (i.e. 10 years less one day))
//        and IS deemed knowledge intensive(some) and has subsidiaries(Some) => subsidiaries spending investment
//
//       5a boundary ** BOUNDARY TESTS for the above: repeat above but with exactly 10 years from commercial sale (still not more than ten years)
//            => subsidiaries spending investment
//
//    5b) This is first Investment Scheme (some previous RFI = No) and it is NOT more than 7 years from first commercial sale(SOME < 7 years (i.e. 7 years less one day))
//        and NOT deemed knowledge intensive(None) and DOES NOT HAVE subsidiaries(Nome) => How plan use investment (Investment grow)
//
//        5b boundary ** BOUNDARY TESTS for the above: repeat above but with exactly 7 years from commercial sale (still not more than 7 years)
//            => How plan use investment (Investment grow)
//
//
//    5c) This is first Investment Scheme (some previous RFI = No) and it is NOT more than 10 years from first commercial sale(SOME < 10 years (i.e. 10 years less one day))
//        and IS deemed knowledge intensive(some) and DOES NOT HAVE subsidiaries(Nome) => How plan use investment (Investment grow)
//
//       5c boundary  ** BOUNDARY TESTS for the above: repeat above but with exactly 10 years from commercial sale (still not more than 10 years)
//            => How plan use investment (Investment grow)
//
//   5d) This is first Investment Scheme (some previous RFI = No) and No commercial sale has been made (i.e Commercial sale is None) and IS KI(Some yes) and HAS subsidiaries(Some)
//		=>  subsidiaries spending investment
//
//   5e) This is first Investment Scheme (some previous RFI = No) and No commercial sale has been made (i.e Commercial sale is None) and IS KI(Some yes) and HAS subsidiaries(NONE)
//		=>  How plan use investment (Investment grow)
//
//   5f) This is first Investment Scheme (some previous RFI = No) and No commercial sale has been made (i.e Commercial sale is None) and NOT KI(None) and Does NOT have subsidaries(NONE)
//		=> How plan use investment (Investment grow)
//
//
//   5g) This is first Investment Scheme (some previous RFI = No) and No commercial sale has been made (i.e Commercial sale is None) and NOT KI(None) and HAS subsidaries(Some)
//		=> subsidiaries spending investment
//
//   OTHER: ANY other route 3 cases with previous investments and commercial sale existing should => 3 (Same Reason as Before)
//
//   6) These are tests that contain empty models to make sure the routing back to previous pages are being tested. These will also include routes for empty KI models
//
//   */
//

  // set up border line conditions of today and future date (tomorrow)
  val date = new Date()
  val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

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

  val modelBusiness = WhatWillUseForModel("Doing Business")
  val modelPrepare = WhatWillUseForModel("Getting ready to do business")
  val modelRAndD = WhatWillUseForModel("Research and Development")
  val emptyModel = WhatWillUseForModel("")
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(modelBusiness)))
  val keyStoreSavedWhatWillUseForBusiness = WhatWillUseForModel("Doing Business")
  val keyStoreSavedWhatWillUseForPrepare = WhatWillUseForModel("Getting ready to do business")
  val keyStoreSavedWhatWillUseForRAndD = WhatWillUseForModel("Research and Development")

  val trueKIModel = KiProcessingModel(Some(true), Some(true), Some(true), Some(true), None, Some(true))
  val falseKIModel = KiProcessingModel(Some(false), Some(false), Some(false), Some(false), None, Some(false))
  val optionKIModel = KiProcessingModel(Some(false), Some(true), Some(false), None, None, None)
  val emptyKIModel = KiProcessingModel(None, None, None, None, None, None)
  val missingCompanyAssertsIsKiKiModel = KiProcessingModel(None, Some(true), Some(true), Some(true), None, Some(true))
  val missingCostsConditionKiModel = KiProcessingModel(Some(true),Some(true),None, None, None, None)
  val missingSecondaryConditionsKiModel = KiProcessingModel(Some(true),Some(true),Some(false),Some(true),Some(true),None)
  val missingDateConditionMetKiModel = KiProcessingModel(Some(false),None,Some(true), None, None, None)

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockS4lConnector)
  }

  "WhatWillUseForController" should {
    "use the correct keystore connector" in {
      WhatWillUseForController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      WhatWillUseForController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      WhatWillUseForController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to WhatWillUseForController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMap)
      when(mockS4lConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedWhatWillUseForBusiness)))
      mockEnrolledRequest
      showWithSessionAndAuth(WhatWillUseForControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMap)
      when(mockS4lConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(None))
      mockEnrolledRequest
      showWithSessionAndAuth(WhatWillUseForControllerTest.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  //Gary Tests

  //4
  "Sending a valid form submit to the WhatWillUseForController for first investment when more than 7 years from " +
    "Commercial sale date when not deemed knowledge intensive" should {
    "redirect to new geographical market page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "Research and Development")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/new-geographical-market")
        }
      )
    }
  }

  // 4a
  "Sending a valid form submit to the WhatWillUseForControlller for first investment when more than 10 years from " +
    "Commercial sale date" should {
    "redirect to new geographical market page" in {

      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale10YearsOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "Research and Development")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/new-geographical-market")
        }
      )
    }
  }

  // 5
  "Sending a valid form submit to the WhatWillUseForController for first investment when NOT more than 7 years from " +
    "Commercial sale date when not deemed knowledge intensive with subsidiaries" should {
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "Research and Development")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries-spending-investment")
        }
      )
    }
  }

  //5 boundary
  "Sending a valid form submit to the WhatWillUseForController for first investment when EXACTLY 7 years from " +
    "Commercial sale date when not deemed knowledge intensive with subsidiaries" should {
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7Years)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "Research and Development")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries-spending-investment")
        }
      )
    }
  }

  // 5a
  "Sending a valid form submit to the WhatWillUseForController for first investment when NOT more than 10 years from " +
    "Commercial sale date when it is deemed knowledge intensive with subsidiaries" should {
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "Research and Development")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries-spending-investment")
        }
      )
    }
  }

  //5a boundary
  "Sending a valid form submit to the WhatWillUseForController for first investment when EXACTLY 10 years from " +
    "Commercial sale date when it is deemed knowledge intensive with subsidiaries" should {
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7Years)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "Research and Development")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries-spending-investment")
        }
      )
    }
  }

  // 5b
  "Sending a valid form submit to the WhatWillUseForController for first investment is NOT more than 7 years from " +
    "Commercial sale date when it is NOT deemed knowledge intensive and without any subsidiaries" should {
    "redirect to new how-plan-to-use-investment page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "Research and Development")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/how-plan-to-use-investment")
        }
      )
    }
  }

  // 5b boundary
  "Sending a valid form submit to the WhatWillUseForController for first investment is EXACTLY 7 years from " +
    "Commercial sale date when it is NOT deemed knowledge intensive and without any subsidiaries" should {
    "redirect to new how-plan-to-use-investment page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7Years)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsOneDay)))
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "Research and Development")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/how-plan-to-use-investment")
        }
      )
    }
  }

  // 5c
  "Sending a valid form submit to the WhatWillUseForController for the first investment when NOT more than 10 years from " +
    "Commercial sale date and when it IS deemed knowledge intensive and without any subsidiaries" should {
    "redirect to new how-plan-to-use-investment page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale10YearsLessOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "Research and Development")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/how-plan-to-use-investment")
        }
      )
    }
  }

  // 5c boundary
  "Sending a valid form submit to the WhatWillUseForController for the first investment when EXACTLY 10 years from " +
    "Commercial sale date and when it IS deemed knowledge intensive and without any subsidiaries" should {
    "redirect to new how-plan-to-use-investment page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale10Years)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "Research and Development")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/how-plan-to-use-investment")
        }
      )
    }
  }

  // 5d
  "Sending a valid form submit to the WhatWillUseForController for the first investment when no commercial sale has been made " +
    "and when it IS deemed knowledge intensive and has subsidiaries" should {
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleNo)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "Research and Development")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries-spending-investment")
        }
      )
    }
  }

  // 5e
  "Sending a valid form submit to the WhatWillUseForController for the first investment when no commercial sale has been made " +
    "and when it IS deemed knowledge intensive and does not have subsidiaries" should {
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleNo)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "Research and Development")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/how-plan-to-use-investment")
        }
      )
    }
  }

  // 5f
  "Sending a valid form submit to the WhatWillUseForController for the first investment when no commercial sale has been made " +
    "and when it IS NOT deemed knowledge intensive and does not have subsidiaries" should {
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleNo)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "Research and Development")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/how-plan-to-use-investment")
        }
      )
    }
  }

  // 5g
  "Sending a valid form submit to the WhatWillUseForController for the first investment when no commercial sale has been made " +
    "and when it IS NOT deemed knowledge intensive and has subsidiaries" should {
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleNo)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "Research and Development")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries-spending-investment")
        }
      )
    }
  }

  "Sending a valid form submit to the WhatWillUseForController when investment has been used previously and a commercial sale exists" +
    "and has a date that isn't within the range" should {
    "redirect to subsidiaries page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale1Year)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "Research and Development")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries")
        }
      )
    }
  }

  //3
  "Sending a valid form submit to the WhatWillUseForController when investment has been used previously and a commercial sale exists" should {
    "redirect to reason used before page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3YearsLessOneDay)))
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "Research and Development")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/used-investment-reason-before")
        }
      )
    }
  }

  //3 variation - prove adding other models values has no affect on result
  "Sending a valid form submit to the WhatWillUseForController when investment has been used previously and a commercial sale exists" +
    "but using different models" should {
    "redirect to reason used before page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(falseKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale10YearsOneDay)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "Research and Development")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/used-investment-reason-before")
        }
      )
    }
  }

  // 6
  "Sending a valid form submit to the WhatWillUseForController for the first investment with an empty PrevRFI" should {
    "redirect to used-investment-scheme-before page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7Years)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(None))
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "Research and Development")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/used-investment-scheme-before")
        }
      )
    }
  }

  // 6
  "Sending a valid form submit to the WhatWillUseForController for the first investment with an empty Commercial sale" should {
    "redirect to commercial-sale page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(trueKIModel)))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(None))
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "Research and Development")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/commercial-sale")
        }
      )
    }
  }

  "Sending a valid form submit to the WhatWillUseForController for the first investment with an empty KIProcessingModel" should {
    "redirect to date-of-incorporation page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7Years)))
      when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDOI3Years)))
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "Research and Development")(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/date-of-incorporation")
        }
      )
    }
  }


  "Sending an invalid form submission with validation errors to the WhatWillUseForController" should {
    "redirect to itself" in {
      mockEnrolledRequest
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit,
        "whatWillUseFor" -> "")(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }


  "Sending a request with no session to WhatWillUseForController" should {
    "return a 303" in {
      status(WhatWillUseForControllerTest.show(fakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in {
      redirectLocation(WhatWillUseForControllerTest.show(fakeRequest)) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending an Unauthenticated request with a session to WhatWillUseForController" should {
    "return a 303" in {
      status(WhatWeAskYouController.show(fakeRequestWithSession)) shouldBe SEE_OTHER
    }

    s"should redirect to GG login" in {
      redirectLocation(WhatWillUseForControllerTest.show(fakeRequestWithSession)) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
        URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")}&origin=investment-tax-relief-submission-frontend&accountType=organisation")
    }
  }

  "Sending a timed-out request to WhatWillUseForController" should {

    "return a 303 in" in {
      status(WhatWillUseForControllerTest.show(timedOutFakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to timeout page" in {
      redirectLocation(WhatWillUseForControllerTest.show(timedOutFakeRequest)) shouldBe Some(routes.TimeoutController.timeout().url)
    }
  }

  "Sending a request to WhatWillUseForController when NOT enrolled" should {

    "return a 303 in" in {
      mockNotEnrolledRequest
      status(WhatWillUseForControllerTest.show(authorisedFakeRequest)) shouldBe SEE_OTHER
    }

    s"should redirect to the Subscription Service" in {
      mockNotEnrolledRequest
      redirectLocation(WhatWillUseForControllerTest.show(authorisedFakeRequest)) shouldBe Some(FrontendAppConfig.subscriptionUrl)
    }
  }

  "Sending a submission to the WhatWillUseForController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(WhatWillUseForControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the WhatWillUseForController with no session" should {

    "redirect to the GG login page with no session" in {
      submitWithoutSession(WhatWillUseForControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl,"UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the WhatWillUseForController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(WhatWillUseForControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the WhatWillUseForController when NOT enrolled" should {
    "redirect to the Subscription Service" in {
      submitWithSessionAndAuth(WhatWillUseForControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }
}
