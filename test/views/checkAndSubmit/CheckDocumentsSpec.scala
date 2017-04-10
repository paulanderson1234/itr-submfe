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

package views.checkAndSubmit

import controllers.helpers.BaseSpec
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import services.FileUploadServiceSpec
import views.helpers.ViewSpec
import views.html.checkAndSubmit.CheckDocuments

class CheckDocumentsSpec extends ViewSpec{

  val files = (new FileUploadServiceSpec).files

    "The Check Documents page" should {

      lazy val page = CheckDocuments(files, envelopeId.get)(fakeRequest, applicationMessages)
      lazy val document = Jsoup.parse(contentAsString(page))

      "have the title 'Check your documents'" in {
        document.title() shouldBe Messages("page.checkAndSubmit.checkDocuments.heading")
      }

      "display the heading 'Check your documents'" in {
        document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkDocuments.heading")
      }

      "display the correct description" in {
        document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkDocuments.outsideFlow.desc")
      }

      "display the correct submit button" in {
        document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkDocuments.button.confirm")
      }

      "display the correct back button" in {
        document.getElementById("back-link").text() shouldBe Messages("common.button.back")
      }

      "display the help button" in {
        document.body.getElementById("get-help-action").text() shouldBe Messages("common.error.help.text")
      }
    }
}
