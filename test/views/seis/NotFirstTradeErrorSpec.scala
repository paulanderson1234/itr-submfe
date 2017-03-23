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

import auth.{MockConfigEISFlow, MockAuthConnector}
import config.FrontendAppConfig
import controllers.seis.NotFirstTradeController
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

class NotFirstTradeErrorSpec extends ViewSpec {

  object TestController extends NotFirstTradeController {
    override lazy val applicationConfig = MockConfigEISFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val s4lConnector = mockS4lConnector
  }

  "The Not first trade error page" should {

    "Verify that start page contains the correct elements" in new SEISSetup {
      val document: Document = {
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title shouldEqual Messages("page.seis.companyDetails.NotFirstTradeError.title")
      document.body.getElementById("main-heading").text() shouldEqual Messages("page.seis.companyDetails.NotFirstTradeError.heading")
      document.body.getElementById("error-reason").text() shouldEqual Messages("page.seis.companyDetails.NotFirstTradeError.reason")
      document.body.getElementById("incorrect-info").text() shouldEqual Messages("page.seis.companyDetails.NotFirstTradeError.incorrect.info") +
        " " + Messages("page.seis.companyDetails.NotFirstTradeError.change.link.changeAnswers") + "."
      document.body.getElementById("change-answers").attr("href") shouldEqual controllers.seis.routes.IsFirstTradeController.show().url
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.seis.routes.IsFirstTradeController.show().url

    }
  }
}
