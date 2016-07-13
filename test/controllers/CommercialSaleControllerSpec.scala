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
import connectors.KeystoreConnector
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

class CommercialSaleControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  // set up border line conditions of today and future date (tomorrow)
  val date = new Date();
  val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
  val date3YearsAgo = localDate.minusYears(3)
  val date3YearsAgoDay: Int = date3YearsAgo.getDayOfMonth
  val date3YearsAgoMonth: Int = date3YearsAgo.getMonthValue
  val date3YearsAgoYear: Int = date3YearsAgo.getYear

  val date3YearsOneDay = localDate.minusYears(3).minusDays(1)
  val date3YearsOneDayDay: Int = date3YearsOneDay.getDayOfMonth
  val date3YearsOneDayMonth: Int = date3YearsOneDay.getMonthValue
  val date3YearsOneDayYear: Int = date3YearsOneDay.getYear

  val date3YearsLessOneDay = localDate.minusYears(3).plusDays(1)
  val date3YearsLessOneDayDay: Int = date3YearsLessOneDay.getDayOfMonth
  val date3YearsLessOneDayMonth: Int = date3YearsLessOneDay.getMonthValue
  val date3YearsLessOneDayYear: Int = date3YearsLessOneDay.getYear

  val todayDay:String = localDate.getDayOfMonth.toString
  val todayMonth: String = localDate.getMonthValue.toString
  val todayYear: String = localDate.getYear.toString


  object CommercialSaleControllerTest extends CommercialSaleController {
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  val model = CommercialSaleModel("Yes", Some(23),Some(11),Some(1993))
  val emptyModel = CommercialSaleModel("", None, None, None)
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(model)))
  val keyStoreSavedCommercialSale = CommercialSaleModel("Yes", Some(15),Some(3),Some(1996))
  val keyStoreSavedDateOfIncorporation3Years = DateOfIncorporationModel(Some(date3YearsAgoDay),Some(date3YearsAgoMonth),Some(date3YearsAgoYear))
  val keyStoreSavedDateOfIncorporation3YearsLessOneDay = DateOfIncorporationModel(Some(date3YearsLessOneDayDay),Some(date3YearsLessOneDayMonth),Some(date3YearsLessOneDayYear))
  val keyStoreSavedDateOfIncorporation3YearsAndOneDay = DateOfIncorporationModel(Some(date3YearsOneDayDay),Some(date3YearsOneDayMonth),Some(date3YearsOneDayYear))

//  val commercialSaleModelValidNo = new CommercialSaleModel("No", None, None, None)
//  val commercialSaleModelValidYes = new CommercialSaleModel("Yes", Some(10), Some(25), Some(2015))
//  val commercialSaleModelInvalidYes = new CommercialSaleModel("Yes", None, Some(25), Some(2015))
//  val emptyCommercialSaleModel = new CommercialSaleModel("", None, None, None)


  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = CommercialSaleControllerTest.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = CommercialSaleControllerTest.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "CommercialSaleController" should {
    "use the correct keystore connector" in {
      CommercialSaleController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to CommercialSaleController" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCommercialSale)))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

  }

  "Sending a valid Yes form submission to the CommercialSaleController" should {
    "redirect to the subsidiaries page if date of incorporation is exactly 3 years from today" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "hasCommercialSale" -> "Yes",
        "commercialSaleDay" -> "23",
        "commercialSaleMonth" -> "11",
        "commercialSaleYear" -> "1993")
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation3Years)))
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/is-knowledge-intensive")
        }
      )
    }
  }

  "Sending a valid Yes form submission to the CommercialSaleController" should {
    "redirect to the subsidiaries page if date of incorporation is more than 3 years from today" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "hasCommercialSale" -> "Yes",
        "commercialSaleDay" -> "23",
        "commercialSaleMonth" -> "11",
        "commercialSaleYear" -> "1993")
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation3YearsAndOneDay)))
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/is-knowledge-intensive")
        }
      )
    }
  }

  "Sending a valid Yes form submission to the CommercialSaleController" should {
    "redirect to the knowledge intensive page if date of incorporation is less than 3 years from today" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "hasCommercialSale" -> "Yes",
        "commercialSaleDay" -> "23",
        "commercialSaleMonth" -> "11",
        "commercialSaleYear" -> "1993")
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation3YearsLessOneDay)))
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries")
        }
      )
    }
  }


  "Sending a valid No form submission to the CommercialSaleController" should {
    "redirect to the subsidiaries page if date of incorporation is exactly 3 years from today" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "hasCommercialSale" -> "No",
        "commercialSaleDay" -> "",
        "commercialSaleMonth" -> "",
        "commercialSaleYear" -> "")
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation3Years)))
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/is-knowledge-intensive")
        }
      )
    }
  }

  "Sending a valid No form submission to the CommercialSaleController" should {
    "redirect to the subsidiaries page if date of incorporation is more than 3 years from today" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "hasCommercialSale" -> "No",
        "commercialSaleDay" -> "",
        "commercialSaleMonth" -> "",
        "commercialSaleYear" -> "")
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation3YearsAndOneDay)))
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/is-knowledge-intensive")
        }
      )
    }
  }

  "Sending a valid No form submission to the CommercialSaleController" should {
    "redirect to the knowledge intensive page if date of incorporation is less than 3 years from today" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "hasCommercialSale" -> "No",
        "commercialSaleDay" -> "",
        "commercialSaleMonth" -> "",
        "commercialSaleYear" -> "")
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation3YearsLessOneDay)))
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries")
        }
      )
    }
  }

  "Sending a valid No form submission to the CommercialSaleController" should {
    "redirect to the date of incorporation page if no date of incorporation is retrieved from keystore" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "hasCommercialSale" -> "No",
        "commercialSaleDay" -> "",
        "commercialSaleMonth" -> "",
        "commercialSaleYear" -> "")
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/date-of-incorporation")
        }
      )
    }
  }

  "Sending a valid Yes form submission to the CommercialSaleController" should {
    "redirect to the date of incorporation page if no date of incorporation is retrieved from keystore" in {
      val request = FakeRequest().withFormUrlEncodedBody(
        "hasCommercialSale" -> "Yes",
        "commercialSaleDay" -> "12",
        "commercialSaleMonth" -> "1",
        "commercialSaleYear" -> "2016")
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/date-of-incorporation")
        }
      )
    }
  }

  "Sending an empty invalid form submission with validation errors to the CommercialSaleController" should {
    "redirect to itself" in {

      val request = FakeRequest().withFormUrlEncodedBody(
        "hasCommercialSale" -> "",
        "commercialSaleDay" -> "",
        "commercialSaleMonth" -> "",
        "commercialSaleYear" -> "")

      submitWithSession(request)(
        result => {
          status(result) shouldBe BAD_REQUEST
          //redirectLocation(result) shouldBe Some(routes.CompanyDetailsController.show.toString())
        }
      )
    }
  }

  "Sending an an invalid form submission with both No and a date to the CommercialSaleController" should {
    "redirect to itself with validation errors" in {

      val request = FakeRequest().withFormUrlEncodedBody(
        "hasCommercialSale" -> "",
        "commercialSaleDay" -> "",
        "commercialSaleMonth" -> "",
        "commercialSaleYear" -> "")

      submitWithSession(request)(
        result => {
          status(result) shouldBe BAD_REQUEST
          //redirectLocation(result) shouldBe Some(routes.CompanyDetailsController.show.toString())
        }
      )
    }
  }

  "Sending an invalid form with missing data submission with validation errors to the CommercialSaleController" should {
    "redirect to itself" in {

      val request = FakeRequest().withFormUrlEncodedBody(
        "hasCommercialSale" -> "Yes",
        "commercialSaleDay" -> "12",
        "commercialSaleMonth" -> "11",
        "commercialSaleYear" -> "")

      submitWithSession(request)(
        result => {
          status(result) shouldBe BAD_REQUEST
          //redirectLocation(result) shouldBe Some(routes.CompanyDetailsController.show.toString())
        }
      )
    }
  }

}
