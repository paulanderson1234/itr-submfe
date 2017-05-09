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

package views.throttlingGuidance


import common.Constants
import controllers.throttlingGuidance.routes
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.throttlingGuidance.IsGroupError


class IsGroupErrorSpec  extends ViewSpec {

  "The user limit reached page" should {

    "contain the correct elements when loaded" in {

      lazy val page = IsGroupError()(fakeRequest, applicationMessages)
      lazy val document = Jsoup.parse(contentAsString(page))

      document.title shouldEqual Messages("page.throttlingGuidance.GuidanceError.common.title")
      document.body.getElementById("main-heading").text() shouldEqual Messages("page.throttlingGuidance.GuidanceError.common.heading")
      document.body.getElementById("throttle-reason").text() shouldEqual Messages("page.throttlingGuidance.isGroupError.reason")
      document.body.getElementById("apply-info").text() shouldEqual Messages("page.throttlingGuidance.GuidanceError.common.apply") +
        " " +  Messages("page.throttlingGuidance.GuidanceError.common.findOut") + "."

      document.body.getElementById("guidance-link").attr("href") shouldEqual Constants.guidanceRedirectUrl

      document.body.getElementById("change-info").text() shouldEqual Messages("page.throttlingGuidance.isGroupError.notGroup") +
        " " +  Messages("page.throttlingGuidance.GuidanceError.common.changeAnswers") + "."

      document.body.getElementById("change-link").attr("href") shouldEqual routes.IsAgentController.show().url

    }

  }
}
