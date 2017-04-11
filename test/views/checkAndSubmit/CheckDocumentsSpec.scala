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

import auth.{MockAuthConnector, MockConfigEISFlow}
import controllers.CheckDocumentsController
import models.fileUpload.{EnvelopeFile, Metadata}
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import views.helpers.ViewSpec
import views.html.checkAndSubmit.CheckDocuments

class CheckDocumentsSpec extends ViewSpec {

  val files = Seq(EnvelopeFile("1","PROCESSING","test.pdf","application/pdf","2016-03-31T12:33:45Z",Metadata(None),"test.url"))

  "The CheckDocuments page" should {

    "Verify that the check documents page contains the correct documents when a valid envelopeId is passed" in {
      lazy val page = CheckDocuments(files, envelopeId.get)(fakeRequest, applicationMessages)
      lazy val document = Jsoup.parse(page.body)
      document.title() shouldBe Messages("page.checkAndSubmit.checkDocuments.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkDocuments.title")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkDocuments.outsideFlow.desc")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkDocuments.outsideFlow.desc")
      //file table
      lazy val filesTable = document.getElementById("files-table").select("tbody")
      filesTable.select("tr").get(1).getElementById("file-0").text() shouldBe "test.pdf"
      filesTable.select("tr").get(1).getElementById("supporting-docs-link").text() shouldBe Messages("common.base.change")

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkDocuments.button.confirm")
    }

    "Verify that the check documents page contains no documents when the envelopeId passed has no documents enclosed" in {
      lazy val page = CheckDocuments(Seq(), envelopeId.get)(fakeRequest, applicationMessages)
      lazy val document = Jsoup.parse(page.body)
      document.title() shouldBe Messages("page.checkAndSubmit.checkDocuments.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkDocuments.title")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkDocuments.outsideFlow.desc")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkDocuments.outsideFlow.desc")
      //file table
      document.getElementById("files-table") shouldBe null

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkDocuments.button.confirm")
    }
  }
}
