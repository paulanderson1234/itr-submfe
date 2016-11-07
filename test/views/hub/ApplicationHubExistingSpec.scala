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

package views.hub

import controllers.routes
import org.jsoup.Jsoup
import play.api.i18n.Messages
import views.helpers.ViewSpec
import views.html.hubPartials.ApplicationHubExisting

class ApplicationHubExistingSpec extends ViewSpec{

  "The Application Hub 'Existing' partial" should {
    "load the correct elements for when there is an applications in progress" in {
      lazy val view = ApplicationHubExisting()(fakeRequest)
      val document = Jsoup.parse(view.body)
      document.getElementById("hub-application-heading").text() shouldBe Messages("page.introduction.hub.existing.heading")
      document.getElementById("hub-application-in-progress").text() shouldBe Messages("page.introduction.hub.existing.table.heading")
      document.getElementById("hub-application").text() shouldBe Messages("page.introduction.hub.existing.advanced.assurance.type")
      document.getElementById("continue").text() shouldBe Messages("common.button.continue")
      document.getElementById("continue-ref").attr("href") shouldEqual routes.YourCompanyNeedController.show().toString()
      document.getElementById("delete-link").text() shouldBe Messages("common.button.delete")

    }
  }

}