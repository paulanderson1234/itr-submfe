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
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.jsoup._
import org.scalatest.mock.MockitoSugar
import scala.concurrent.Future
import models.YourCompanyNeedModel
import play.api.mvc.Result

class YourCompanyNeedControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[YourCompanyNeedModel], postData: Option[YourCompanyNeedModel]): YourCompanyNeedController = {

    val mockKeystoreConnector = mock[KeystoreConnector]


    when(mockKeystoreConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    lazy val data = CacheMap("form-id", Map("data" -> Json.toJson(postData.getOrElse(YourCompanyNeedModel("")))))
    when(mockKeystoreConnector.saveFormData[YourCompanyNeedModel](Matchers.anyString(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))

    new YourCompanyNeedController {
      override val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  "YourCompanyNeedController" should {
    "use the correct keystore connector" in {
      YourCompanyNeedController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  // GET Tests
  "Calling the YourCompanyNeed.show" when {

      lazy val fakeRequest = FakeRequest("GET", "/investment-tax-relief/your-company-need").withSession(SessionKeys.sessionId -> "12345")

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
        val target = setupTarget(Some(YourCompanyNeedModel("Yes")), None)
        lazy val result = target.show(fakeRequest)
        status(result) shouldBe 200
      }

      "return some HTML that" should {

        "contain some text and use the character set utf-8" in {
          val target = setupTarget(Some(YourCompanyNeedModel("Yes")), None)
          lazy val result = target.show(fakeRequest)
          contentType(result) shouldBe Some("text/html")
          charset(result) shouldBe Some("utf-8")
        }

        "have the radio option `A letter to show to potential investors` selected if " +
          "`A letter to show to potential investors` is supplied in the model" in {
          val target = setupTarget(Some(YourCompanyNeedModel("AA")), None)
          lazy val result = target.show(fakeRequest)
          lazy val document = Jsoup.parse(bodyOf(result))
          document.body.getElementById("needAAorCS-aa").parent.classNames().contains("selected") shouldBe true
        }

        "have the radio option `A reference number so investors can claim relief` selected if " +
          "`A reference number so investors can claim relief` is supplied in the model" in {
          val target = setupTarget(Some(YourCompanyNeedModel("CS")), None)
          lazy val result = target.show(fakeRequest)
          lazy val document = Jsoup.parse(bodyOf(result))
          document.body.getElementById("needAAorCS-cs").parent.classNames().contains("selected") shouldBe true
        }
      }
    }
  }

  // POST Tests
  "In YourCompanyNeedContoller calling the .submit action" when {

    def buildRequest(body: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest("POST",
      "/investment-tax-relief/your-company-need")
      .withSession(SessionKeys.sessionId -> "12345")
      .withFormUrlEncodedBody(body: _*)

    def executeTargetWithMockData(data: String): Future[Result] = {
      lazy val fakeRequest = buildRequest(("needAAorCS", data))
      val mockData = new YourCompanyNeedModel(data)
      val target = setupTarget(None, Some(mockData))
      target.submit(fakeRequest)
    }

    "submitting a valid form with `A letter to show to potential investors`" should {

      lazy val result = executeTargetWithMockData("AA")

      "return a 303" in {
        status(result) shouldBe 303
      }
    }

    "submitting a valid form with `A reference number so investors can claim relief`" should {

      lazy val result = executeTargetWithMockData("CS")

      "return a 303" in {
        status(result) shouldBe 303
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
