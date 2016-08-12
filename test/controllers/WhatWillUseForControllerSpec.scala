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

import java.time.ZoneId
import java.util.{Date, UUID}

import builders.SessionBuilder
import common.{Constants, KeystoreKeys}
import connectors.KeystoreConnector
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

class WhatWillUseForControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object WhatWillUseForControllerTest extends WhatWillUseForController {
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  //Gary TEST SCENARIOS
  /*

    3) Investment previously(some previous RFI = Yes) and Commercial sale (Some) => Same Reason AS Before

    4) This is first Investment Scheme(some previous RFI = No) and it is more than 7 years from first commercial sale(SOME > 7 years i.e. (7 years plus one day))
       and NOT deemed knowledge intensive (None) => New Geo Market

   4a) This is first Investment Scheme(some previous RFI = No) and it is more than 10 years from first commercial sale(SOME > 10 years (i.e. 10 years plus one day))
       and it is deemed knowledge intensive(Some) => New Geo Market

    5) This is first Investment Scheme (some previous RFI = No) and it is NOT more than 7 years from first commercial sale(SOME < 7 years (i.e. 7 years less one day))
       and NOT deemed knowledge intensive(No) and has subsidiaries(Some) => subsidiaries spending investment

      5 boundary) ** BOUNDARY TESTS for the above: repeat above but with exactly 7 years from commercial sale (still not more than 7 years)
            => subsidiaries spending investment

    5a) This is first Investment Scheme (some previous RFI = No) and it is NOT more than 10 years from first commercial sale(SOME < 10 years (i.e. 10 years less one day))
        and IS deemed knowledge intensive(some) and has subsidiaries(Some) => subsidiaries spending investment

       5a boundary ** BOUNDARY TESTS for the above: repeat above but with exactly 10 years from commercial sale (still not more than ten years)
            => subsidiaries spending investment

    5b) This is first Investment Scheme (some previous RFI = No) and it is NOT more than 7 years from first commercial sale(SOME < 7 years (i.e. 7 years less one day))
        and NOT deemed knowledge intensive(None) and DOES NOT HAVE subsidiaries(Nome) => How plan use investment (Investment grow)

        5b boundary ** BOUNDARY TESTS for the above: repeat above but with exactly 7 years from commercial sale (still not more than 7 years)
            => How plan use investment (Investment grow)


    5c) This is first Investment Scheme (some previous RFI = No) and it is NOT more than 10 years from first commercial sale(SOME < 10 years (i.e. 10 years less one day))
        and IS deemed knowledge intensive(some) and DOES NOT HAVE subsidiaries(Nome) => How plan use investment (Investment grow)

       5c boundary  ** BOUNDARY TESTS for the above: repeat above but with exactly 10 years from commercial sale (still not more than 10 years)
            => How plan use investment (Investment grow)

   5d) This is first Investment Scheme (some previous RFI = No) and No commercial sale has been made (i.e Commercial sale is None) and IS KI(Some yes) and HAS subsidiaries(Some)
		=>  subsidiaries spending investment

   5e) This is first Investment Scheme (some previous RFI = No) and No commercial sale has been made (i.e Commercial sale is None) and IS KI(Some yes) and HAS subsidiaries(NONE)
		=>  How plan use investment (Investment grow)

   5f) This is first Investment Scheme (some previous RFI = No) and No commercial sale has been made (i.e Commercial sale is None) and NOT KI(None) and Does NOT have subsidaries(NONE)
		=> How plan use investment (Investment grow)


   5g) This is first Investment Scheme (some previous RFI = No) and No commercial sale has been made (i.e Commercial sale is None) and NOT KI(None) and HAS subsidaries(Some)
		=> subsidiaries spending investment

   OTHER: ANY other route 3 cases with previous investments and commercial sale existing should => 3 (Same Reason as Before)

   */

  // set up border line conditions of today and future date (tomorrow)
  val date = new Date();
  val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

  val todayDay: String = localDate.getDayOfMonth.toString
  val todayMonth: String = localDate.getMonthValue.toString
  val todayYear: String = localDate.getYear.toString

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

  val keyStoreSavedIsKnowledgeIntensiveYes = IsKnowledgeIntensiveModel(Constants.StandardRadioButtonYesValue)
  val keyStoreSavedIsKnowledgeIntensiveNo = IsKnowledgeIntensiveModel(Constants.StandardRadioButtonNoValue)
  val keyStoreSavedIsKnowledgeIntensiveEmpty = IsKnowledgeIntensiveModel("")
  val keyStoreSavedHadPreviousRFIYes = HadPreviousRFIModel(Constants.StandardRadioButtonYesValue)
  val keyStoreSavedHadPreviousRFINo = HadPreviousRFIModel(Constants.StandardRadioButtonNoValue)
  val keyStoreSavedHadPreviousRFIEmpty = HadPreviousRFIModel("")
  val keyStoreSavedSubsidiariesYes = SubsidiariesModel(Constants.StandardRadioButtonYesValue)
  val keyStoreSavedSubsidiariesNo = SubsidiariesModel(Constants.StandardRadioButtonNoValue)
  val keyStoreSavedSubsidiariesEmpty = SubsidiariesModel("")

  val keyStoreSavedCommercialSaleNo = CommercialSaleModel(Constants.StandardRadioButtonNoValue, None, None, None)
  val keyStoreSavedCommercialSaleEmpty = CommercialSaleModel("", None, None, None)

  // 10 year from today boundary models
  val keyStoreSavedCommercialSale10Years = CommercialSaleModel(Constants.StandardRadioButtonYesValue,
    Some(date10YearsAgoDay), Some(date10YearsAgoMonth), Some(date10YearsAgoYear))
  val keyStoreSavedCommercialSale10YearsOneDay = CommercialSaleModel(Constants.StandardRadioButtonYesValue,
    Some(date10YearsOneDayDay), Some(date10YearsOneDayMonth), Some(date10YearsOneDayYear))
  val keyStoreSavedCommercialSale10YearsLessOneDay = CommercialSaleModel(Constants.StandardRadioButtonYesValue,
    Some(date10YearsLessOneDayDay), Some(date10YearsLessOneDayMonth), Some(date10YearsLessOneDayYear))

  // 7 year from today boundary models
  val keyStoreSavedCommercialSale7Years = CommercialSaleModel(Constants.StandardRadioButtonYesValue,
    Some(date7YearsAgoDay), Some(date7YearsAgoMonth), Some(date7YearsAgoYear))
  val keyStoreSavedCommercialSale7YearsOneDay = CommercialSaleModel(Constants.StandardRadioButtonYesValue,
    Some(date7YearsOneDayDay), Some(date7YearsOneDayMonth), Some(date7YearsOneDayYear))
  val keyStoreSavedCommercialSale7YearsLessOneDay = CommercialSaleModel(Constants.StandardRadioButtonYesValue,
    Some(date7YearsLessOneDayDay), Some(date7YearsLessOneDayMonth), Some(date7YearsLessOneDayYear))


  val modelBusiness = WhatWillUseForModel("Doing Business")
  val modelPrepare = WhatWillUseForModel("Getting ready to do business")
  val modelRAndD = WhatWillUseForModel("Research and Development")
  val emptyModel = WhatWillUseForModel("")
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(modelBusiness)))
  val keyStoreSavedWhatWillUseForBusiness = WhatWillUseForModel("Doing Business")
  val keyStoreSavedWhatWillUseForPrepare = WhatWillUseForModel("Getting ready to do business")
  val keyStoreSavedWhatWillUseForRAndD = WhatWillUseForModel("Research and Development")

  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = WhatWillUseForControllerTest.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = WhatWillUseForControllerTest.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "WhatWillUseForController" should {
    "use the correct keystore connector" in {
      WhatWillUseForController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to WhatWillUseForController" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedWhatWillUseForBusiness)))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }
  }

  //Gary Tests

  //4
  "Sending a valid form submit to the WhatWillUseForControlller for first investment when more than 7 years from " +
    "Commercial sale date when not deemed knowledge intensive" should {
    "redirect to new geographical market page" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsOneDay)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveNo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/new-geographical-market")
        }
      )
    }
  }

  // 4a
  "Sending a valid form submit to the WhatWillUseForControlller for first investment when more than 10 years from " +
    "Commercial sale date when it is deemed knowledge intensive" should {
    "redirect to new geographical market page" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale10YearsOneDay)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
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
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsLessOneDay)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveNo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
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
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7Years)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveNo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
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
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsLessOneDay)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
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
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7Years)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
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
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7YearsLessOneDay)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveNo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
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
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7Years)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveNo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
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
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale10YearsLessOneDay)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
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
    "redirect to new subsidiaries-spending-investment page" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale10Years)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
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
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleNo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
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
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleNo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
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
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleNo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveNo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
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
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFINo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSaleNo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveNo)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries-spending-investment")
        }
      )
    }
  }

  // Route 3 tests - previous investment and commercial sale exists

  //3
  "Sending a valid form submit to the WhatWillUseForControlller when investment has been used previosuly and a commercial sale exists" should {
    "redirect to reason used before page" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale7Years)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/used-investment-reason-before")
        }
      )
    }
  }

  //3 variation - prove adding other models values has no affect on result
  "fffSending a valid form submit to the WhatWillUseForControlller when investment has been used previosuly and a commercial sale exists" should {
    "redirect to reason used before page" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedHadPreviousRFIYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale10YearsOneDay)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveYes)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "Research and Development")
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/used-investment-reason-before")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the WhatWillUseForController" should {
    "redirect to itself" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "whatWillUseFor" -> "")
      submitWithSession(request)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
