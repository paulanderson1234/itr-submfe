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
import views.html.hubPartials.ApplicationHubNew
import play.api.i18n.Messages.Implicits._

class ApplicationHubNewSpec extends ViewSpec{

  "The Application Hub 'New' patial" should {
    "load the correct elements for when there are no applications in progress" in {
      lazy val view = ApplicationHubNew()(fakeRequest, applicationMessages)
      val document = Jsoup.parse(view.body)
      document.getElementById("hub-application-heading").text() shouldBe Messages("page.introduction.hub.new.heading")
      document.getElementById("create-new-application").text() shouldBe Messages("page.introduction.hub.button")
    }
  }

}
