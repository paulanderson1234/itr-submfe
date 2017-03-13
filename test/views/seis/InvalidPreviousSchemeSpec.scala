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

package views.seis

import auth.{MockConfigSingleFlow, MockAuthConnector}
import controllers.seis.{InvalidPreviousSchemeController, routes}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._

class InvalidPreviousSchemeSpec extends ViewSpec {

  object TestController extends InvalidPreviousSchemeController {
    override lazy val applicationConfig = MockConfigSingleFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val s4lConnector = mockS4lConnector
  }

  "The Invalid Previous Scheme error page" should {

    "Verify that start page contains the correct elements" in new SEISSetup {
      val document: Document = {
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }

      document.title shouldEqual Messages("page.previousInvestment.InvalidPreviousScheme.title")
      document.body.getElementById("main-heading").text() shouldEqual Messages("page.previousInvestment.InvalidPreviousScheme.heading")

      document.body.getElementById("invalid-scheme-reason").text() shouldEqual Messages("page.previousInvestment.InvalidPreviousScheme.reason")
      document.body.getElementById("change-answers").text() shouldEqual Messages("page.previousInvestment.InvalidPreviousScheme.change-text") +
        " " + Messages("page.previousInvestment.InvalidPreviousScheme.change-link") + "."
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ReviewPreviousSchemesController.show().url
      document.body.getElementById("change-answers-link").attr("href") shouldEqual routes.ReviewPreviousSchemesController.show().url

    }
  }
}
