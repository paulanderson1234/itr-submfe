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

import connectors.KeystoreConnector
import models.RegisteredAddressModel
import org.jsoup._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class RegisteredAddressControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[RegisteredAddressModel], postData: Option[RegisteredAddressModel]): RegisteredAddressController = {

    val mockKeystoreConnector = mock[KeystoreConnector]


    when(mockKeystoreConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    lazy val data = CacheMap("form-id", Map("data" -> Json.toJson(postData.getOrElse(RegisteredAddressModel("")))))
    when(mockKeystoreConnector.saveFormData[RegisteredAddressModel](Matchers.anyString(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))

    new RegisteredAddressController {
      override val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  "RegisteredAddressController" should {
    "use the correct keystore connector" in {
      RegisteredAddressController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  // GET Tests
  "Calling the RegisteredAddress.show" when {

      lazy val fakeRequest = FakeRequest("GET", "/investment-tax-relief/registered-address").withSession(SessionKeys.sessionId -> "12345")

    "not supplied with a pre-existing stored model" should {

      val target = setupTarget(None, None)
      lazy val result = target.show(fakeRequest)
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 200" in {
        status(result) shouldBe 200
      }

      "return some HTML that" should {
        "contain some text and use the character set utf-8" in {
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

      }
    }

    "supplied with a pre-existing stored model" should {

      "return a 200" in {
        val target = setupTarget(Some(RegisteredAddressModel("ST1 1QQ")), None)
        lazy val result = target.show(fakeRequest)
        status(result) shouldBe 200
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          val target = setupTarget(Some(RegisteredAddressModel("ST1 1QQ")), None)
          lazy val result = target.show(fakeRequest)
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }
      }
    }
  }

  // POST Tests
  "In RegisteredAddressController calling the .submit action" when {

    def buildRequest(body: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest("POST",
      "/investment-tax-relief/registered-address")
      .withSession(SessionKeys.sessionId -> "12345")
      .withFormUrlEncodedBody(body: _*)

    def executeTargetWithMockData(data: String): Future[Result] = {
      lazy val fakeRequest = buildRequest(("postcode", data))
      val mockData = new RegisteredAddressModel(data)
      val target = setupTarget(None, Some(mockData))
      target.submit(fakeRequest)
    }

    "submitting a valid form with 'a valid postcode'" should {

      lazy val result = executeTargetWithMockData("ST1 1QQ")

      "return a 303" in {
        status(result) shouldBe 303
      }
    }

    "submitting a valid form with 'a invalid postcode'" should {

      lazy val result = executeTargetWithMockData("ST111 1QQ")

      "return a 400" in {
        status(result) shouldBe 400
      }
    }

    "submitting a valid form with 'a invalid postcode (!)'" should {

      lazy val result = executeTargetWithMockData("ST! 1QQ")

      "return a 400" in {
        status(result) shouldBe 400
      }
    }

    "submitting a valid form with 'a invalid postcode (One letter)'" should {

      lazy val result = executeTargetWithMockData("S")

      "return a 400" in {
        status(result) shouldBe 400
      }
    }

    "submitting an invalid form with no content" should {

      lazy val result = executeTargetWithMockData("")
      lazy val document = Jsoup.parse(bodyOf(result))

      "return a 400" in {
        status(result) shouldBe 400
      }

      "display a visible Error Summary field" in {
        document.getElementById("error-summary-display").hasClass("error-summary--show")
      }
    }
  }
}
