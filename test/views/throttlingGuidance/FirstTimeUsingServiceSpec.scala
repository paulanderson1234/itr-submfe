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

import connectors.KeystoreConnector
import controllers.throttlingGuidance.FirstTimeUsingServiceController
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers.{contentAsString, _}
import views.helpers.ViewSpec

class FirstTimeUsingServiceSpec extends ViewSpec {

  object TestController extends FirstTimeUsingServiceController {
    override val keystoreConnector =  mock[KeystoreConnector]
  }

  "The First time usage guidance page" should {

    "contain the correct elements when loaded" in {

      val result = TestController.show.apply(authorisedFakeRequest)
      val document = Jsoup.parse(contentAsString(result))

      //title
      document.title() shouldBe Messages("page.throttlingGuidance.firstTimeUsingService.title")

      //main heading
      document.getElementById("main-heading").text() shouldBe Messages("page.throttlingGuidance.firstTimeUsingService.heading")

      // radio button
      document.select("#isFirstTimeUsingService-yes").size() shouldBe 1
      document.select("#isFirstTimeUsingService-no").size() shouldBe 1
      document.getElementById("isFirstTimeUsingService-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("isFirstTimeUsingService-noLabel").text() shouldBe Messages("page.throttlingGuidance.firstTimeUsingService.radioNoLabel")
      document.getElementById("isFirstTimeUsingService-legend").select(".visuallyhidden").text() shouldBe Messages("page.throttlingGuidance.firstTimeUsingService.heading")

      //continue button
      document.body.getElementById("next").text() shouldBe Messages("common.button.continue")
    }
  }

}
