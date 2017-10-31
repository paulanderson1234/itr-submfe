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
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.throttlingGuidance.WhoCanUseNewService


class WhoCanUseNewServiceSpec extends ViewSpec {


  "The Who can use our service page" should {

    "contain the correct elements when loaded" in {

      lazy val page = WhoCanUseNewService()(fakeRequest, applicationMessages)
      lazy val document = Jsoup.parse(contentAsString(page))

      //title
      document.title() shouldBe Messages("page.throttlingGuidance.whoCanUseNewService.title")

      //main heading
      document.getElementById("main-heading").text() shouldBe Messages("page.throttlingGuidance.whoCanUseNewService.heading")

      //description
      document.body.getElementById("description").text() shouldBe Messages("page.throttlingGuidance.whoCanUseNewService.apply.if")
      document.body.getElementById("reason-one").text() shouldBe Messages("page.throttlingGuidance.whoCanUseNewService.apply.if.one")
      document.body.getElementById("reason-two").text() shouldBe Messages("page.throttlingGuidance.whoCanUseNewService.apply.if.two")
      document.body.getElementById("reason-three").text() shouldBe Messages("page.throttlingGuidance.whoCanUseNewService.apply.if.three")
      document.body.getElementById("still-apply").text() shouldBe Messages("page.throttlingGuidance.whoCanUseNewService.still.apply") + " " + Messages("page.throttlingGuidance.whoCanUseNewService.existing")

      //link
      document.body.getElementById("change-answers").text() shouldBe Messages("page.throttlingGuidance.whoCanUseNewService.existing")
      document.body.getElementById("change-answers").attr("href") shouldBe Constants.guidanceRedirectUrl

      //continue button
      document.body.getElementById("next").text() shouldBe Messages("common.button.continue")
    }

  }
}
