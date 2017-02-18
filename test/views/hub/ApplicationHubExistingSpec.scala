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

package views.hub

import org.jsoup.Jsoup
import play.api.i18n.Messages
import views.helpers.ViewSpec
import views.html.hubPartials.ApplicationHubExisting
import play.api.i18n.Messages.Implicits._

class ApplicationHubExistingSpec extends ViewSpec{

  val continueUrl = "/seis/natureOfbusiness"
  val schemeType = "SEED Enterprise Investment Scheme - Advanced Assurance"

  "The Application Hub 'Existing' partial" should {
    "load the correct elements for when there is an applications in progress" in {
      lazy val view = ApplicationHubExisting(continueUrl, schemeType)(fakeRequest,applicationMessages)
      val document = Jsoup.parse(view.body)
      document.getElementById("hub-application-heading").text() shouldBe Messages("page.introduction.hub.existing.heading")
      document.getElementById("hub-application-in-progress").text() shouldBe Messages("page.introduction.hub.existing.table.heading")
      document.getElementById("hub-application").text() shouldBe schemeType
      document.getElementById("continue").text() shouldBe Messages("common.button.continue")
      document.getElementById("continue-ref").attr("href") shouldEqual continueUrl
      document.getElementById("delete-link").text() shouldBe Messages("common.button.delete")

    }
  }

}
