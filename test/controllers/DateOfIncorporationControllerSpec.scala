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

import java.net.URLEncoder
import java.time.ZoneId
import java.util.{Date, UUID}

import auth.{MockConfig, MockAuthConnector}
import builders.SessionBuilder
import common.KeystoreKeys
import config.FrontendAppConfig
import connectors.KeystoreConnector
import controllers.helpers.FakeRequestHelper
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future

class DateOfIncorporationControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite with FakeRequestHelper{

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

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object DateOfIncorporationControllerTest extends DateOfIncorporationController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  val dateOfIncorporationAsJson = """{"day": 23,"month": 11, "year": 1993}"""

  val model = DateOfIncorporationModel(Some(23), Some(11), Some(1993))
  val emptyModel = DateOfIncorporationModel(None, None, None)
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(model)))

  val keyStoreSavedDateOfIncorporation = DateOfIncorporationModel(Some(23), Some(11), Some(1993))
  val savedKIData = KiProcessingModel(Some(false),Some(false), Some(false), Some(false), Some(false))
  val updatedKIData = KiProcessingModel(Some(true),Some(false), Some(false), Some(false), Some(false))
  val updatedKiCacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(updatedKIData)))



  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "DateOfIncorporationController" should {
    "use the correct keystore connector" in {
      DateOfIncorporationController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to DateOfIncorporationController when authenticated" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation)))
      showWithSessionAndAuth(DateOfIncorporationControllerTest.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      showWithSessionAndAuth(DateOfIncorporationControllerTest.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending an Unauthenticated request with a session to DateOfIncorporationController" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(DateOfIncorporationControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl)
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to DateOfIncorporationController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(DateOfIncorporationControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl)
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to DateOfIncorporationController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(DateOfIncorporationControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a valid form submit to the DateOfIncorporationController when authenticated" should {
    "redirect to nature of business page" in {

      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.saveFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel),
        Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(updatedKiCacheMap)
      when(mockKeyStoreConnector.saveFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation),
        Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(savedKIData)))
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(model)))

      val formInput = Seq(
        "incorporationDay" -> "23",
        "incorporationMonth" -> "11",
        "incorporationYear" -> "1993")

      submitWithSessionAndAuth(DateOfIncorporationControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/nature-of-business")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the DateOfIncorporationController when authenticated" should {
    "return a bad request" in {

      val formInput = Seq(
        "incorporationDay" -> "",
        "incorporationMonth" -> "",
        "incorporationYear" -> "")

      submitWithSessionAndAuth(DateOfIncorporationControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending a submission to the DateOfIncorporationController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(DateOfIncorporationControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl)
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the GG login page with no session" in {
      submitWithoutSession(DateOfIncorporationControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl)
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the DateOfIncorporationController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(DateOfIncorporationControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

}