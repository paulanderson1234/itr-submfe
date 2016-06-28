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
import controllers.examples.DoSubmissionController
import models.DoSubmissionModel
import org.jsoup._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class IntroductionControllerSpec extends UnitSpec with WithFakeApplication with MockitoSugar {

  implicit val hc = new HeaderCarrier()

  def setupTarget(getData: Option[DoSubmissionModel], postData: Option[DoSubmissionModel]): DoSubmissionController = {

    val mockKeystoreConnector = mock[KeystoreConnector]


    when(mockKeystoreConnector.fetchAndGetFormData[DoSubmissionModel](Matchers.anyString())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(getData))

    lazy val data = CacheMap("form-id", Map("data" -> Json.toJson(postData.getOrElse(DoSubmissionModel("")))))
    when(mockKeystoreConnector.saveFormData[DoSubmissionModel](Matchers.anyString(), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(data))

    new DoSubmissionController {
      override val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  // GET Tests
  "Calling the DoSubmissio.disabledTrustee" when {

    lazy val fakeRequest = FakeRequest("GET", "/investment-tax-relief/start").withSession(SessionKeys.sessionId -> "12345")

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

        "have the title 'Welcome'" in {
          document.title shouldEqual Messages("page.start.welcome.title")
        }

        "have the correct heading" in {
          document.body.getElementsByTag("h1").text shouldEqual Messages("page.start.welcome.heading")
        }

        //        "have a 'Back' link to ${routes.DoSubmissionController.show()}" in {
        //          document.body.getElementById("back-link").text shouldEqual Messages("common.button.back")
        //          document.body.getElementById("back-link").attr("href") shouldEqual routes.DateOfFirstSaleController.show.toString()
        //        }

        "have the correct text in description-one" in {
          document.body.getElementById("description-one").parent.text shouldEqual Messages("page.start.welcome.description.one")
        }

        "have the correct text in description-two" in {
          document.body.getElementById("description-two").parent.text shouldEqual Messages("page.start.welcome.description.two")
        }

        "have the correct text for bullet point 1-learn" in {
          document.body.getElementById("learn").parent.text shouldEqual Messages("page.start.welcome.learn")
        }

        "have the correct text for bullet point 2-eligible" in {
          document.body.getElementById("eligible").parent.text shouldEqual Messages("page.start.welcome.bullet.eligible")
        }

        "have the correct text for bullet point 3-certify" in {
          document.body.getElementById("certify").parent.text shouldEqual Messages("page.start.welcome.bullet.certify")
        }

        "have the correct text for bullet point 4-claim" in {
          document.body.getElementById("claim").parent.text shouldEqual Messages("page.start.welcome.bullet.claim")
        }
        //        "display a radio button with the option 'Yes'" in {
        //          document.body.getElementById("doSubmission-yes").parent.text shouldEqual Messages("common.base.yes")
        //        }
        //        "display a radio button with the option 'No'" in {
        //          document.body.getElementById("doSubmission-no").parent.text shouldEqual Messages("common.base.no")
        //        }

        "display a 'Start now' button " in {
          document.body.getElementById("next").text shouldEqual Messages("common.button.start")
        }

        "have the correct sub heading" in {
          document.body.getElementsByTag("h2").text shouldEqual Messages("common.readMore")
        }

        "have the correct sub heading" in {
          document.body.getElementsByTag("h2").text shouldEqual Messages("common.readMore")
        }

        "have the correct sub heading" in {
          document.body.attr("href") shouldEqual Messages("How to apply")
        }
      }

      // POST Tests
      "In DoSubmissionContoller calling the .submit action" when {

        def buildRequest(body: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] = FakeRequest("POST",
          "/investment-tax-relief/examples/examples-do-submission")
          .withSession(SessionKeys.sessionId -> "12345")
          .withFormUrlEncodedBody(body: _*)

        def executeTargetWithMockData(data: String): Future[Result] = {
          lazy val fakeRequest = buildRequest(("doSubmission", data))
          val mockData = new DoSubmissionModel(data)
          val target = setupTarget(None, Some(mockData))
          target.submit(fakeRequest)
        }

        "submitting a valid form with 'Yes'" should {

          lazy val result = executeTargetWithMockData("Yes")

          "return a 303" in {
            status(result) shouldBe 303
          }
        }

        "submitting a valid form with 'No'" should {

          lazy val result = executeTargetWithMockData("No")

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
  }
}