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

package views.fileUpload

import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.fileUpload.{NoJavascriptUploadForm, JavascriptUploadForm}

class NoJavascriptUploadFormSpec extends ViewSpec{

  val envelopeID = "00000000-0000-0000-0000-000000000000"

  "The NoJavascriptUploadForm" should {

    "contain the correct elements when loaded with no files" in {

      lazy val page = NoJavascriptUploadForm(Seq(), envelopeID)(fakeRequest)
      lazy val document = Jsoup.parse(contentAsString(page))

      //Dynamic button
      document.body.getElementsByAttributeValue("style", "display: none;").size() shouldBe 0
      document.body.getElementById("upload-button").text() shouldBe Messages("page.fileUpload.upload")
    }

    "contain the correct elements when loaded with 1 or more files" in {

      lazy val page = NoJavascriptUploadForm(files, envelopeID)(fakeRequest)
      lazy val document = Jsoup.parse(contentAsString(page))

      //File upload should be invisible
      document.body.getElementsByAttributeValue("style", "display: none;").size() shouldBe 0
      document.body.getElementById("upload-button").text() shouldBe Messages("page.fileUpload.upload.another")
    }

  }


}
