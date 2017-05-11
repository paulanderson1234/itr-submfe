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

package views.hubGuidance

import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.hubGuidance.HubGuidanceFeedback
import views.html.throttlingGuidance.OurServiceChange


class HubGuidanceFeedbackSpec extends ViewSpec {


  "The Hub Guidance Feedback page" should {

    "contain the correct elements when loaded" in {

      lazy val page = HubGuidanceFeedback()(fakeRequest,applicationMessages)
      lazy val document = Jsoup.parse(contentAsString(page))

      //title
      document.title() shouldBe Messages("page.hubGuidance.hubGuidanceFeedback.title")

      //main heading
      document.getElementById("main-heading").text() shouldBe Messages("page.hubGuidance.hubGuidanceFeedback.heading")

      //description
      document.body.getElementById("description").text() shouldBe Messages("page.hubGuidance.hubGuidanceFeedback.description")

      //continue button
      document.body.getElementById("next").text() shouldBe Messages("common.button.continue")
    }
  }

}
