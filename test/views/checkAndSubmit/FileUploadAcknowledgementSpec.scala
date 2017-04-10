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

import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.checkAndSubmit.Acknowledgement


class FileUploadAcknowledgementSpec extends ViewSpec {


  "The Acknowledgement page" should {

    "contain the correct elements when loaded" in {

      lazy val page = Acknowledgement()(fakeRequest,applicationMessages)
      lazy val document = Jsoup.parse(contentAsString(page))
      //title
      document.title() shouldBe Messages("page.checkAndsubmit.attachments.acknowledgement.title")
      //banner
      document.body.getElementById("submission-confirmation").text() shouldBe Messages("page.checkAndsubmit.attachments.acknowledgement.submission.confirmation")

      //what happens next
      document.body.getElementById("what-happens-next").text() shouldBe Messages("page.checkAndsubmit.attachments.acknowledgement.what.happens.heading")
      document.body.getElementById("happens-next").text() shouldBe Messages("page.checkAndsubmit.attachments.acknowledgement.what.happens.text")

      //get help
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")

      //finish button
      document.body.getElementById("submit").text() shouldBe Messages("page.checkAndsubmit.attachments.acknowledgement.button.confirm")
    }
  }

}
