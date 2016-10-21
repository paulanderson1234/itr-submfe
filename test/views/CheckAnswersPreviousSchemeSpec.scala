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

package views

import auth.MockAuthConnector
import config.FrontendAppConfig
import controllers.{CheckAnswersController, routes}
import controllers.helpers.FakeRequestHelper
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.helpers.CheckAnswersSpec
import play.api.test.Helpers._

class CheckAnswersPreviousSchemeSpec extends CheckAnswersSpec {

  object TestController extends CheckAnswersController {
     override lazy val applicationConfig = FrontendAppConfig
     override lazy val authConnector = MockAuthConnector
     override lazy val s4lConnector = mockS4lConnector
     override lazy val enrolmentConnector = mockEnrolmentConnector
   }

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 2: Previous Schemes" +
      " when an empty set of company detail models are passed" in new Setup {
      val document: Document = {
        previousRFISetup()
        investmentSetup()
        contactDetailsSetup()
        companyDetailsSetup()
        val result = TestController.show.apply(authorisedFakeRequest.withFormUrlEncodedBody())
        Jsoup.parse(contentAsString(result))
      }


      document.title() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description")
      document.getElementById("print-this-page").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.print.text")

      lazy val previousRfiTableTbody = document.getElementById("previous-rfi-table").select("tbody")
      lazy val notAvailableMessage = Messages("common.notAvailable")

      //Section 1 table heading
      document.getElementById("previousRFISection-table-heading").text() shouldBe Messages("summaryQuestion.previousRFISection")
      //Previous RFI None
      previousRfiTableTbody.select("tr").get(0).getElementById("emptyPreviousRFISection-subHeading").text() shouldBe
        notAvailableMessage
      previousRfiTableTbody.select("tr").get(0).getElementById("emptyPreviousRFISection-link")
        .attr("href") shouldEqual routes.HadPreviousRFIController.show().url

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.SupportingDocumentsController.show().url
    }
  }
}
