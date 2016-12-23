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

package views.fileUpload

import controllers.routes
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.fileUpload.FileUpload

class FileUploadSpec extends ViewSpec {

  val envelopeID = "00000000-0000-0000-0000-000000000000"

  "The FileUpload page" should {

    "contain the correct elements when loaded with no files" in {

      lazy val page = FileUpload(Seq(), envelopeID)(fakeRequest)
      lazy val document = Jsoup.parse(contentAsString(page))

      //title and heading
      document.title() shouldBe Messages("page.fileUpload.title")

      //sidebar
      document.body.getElementById("supporting-docs-heading").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.heading")
      document.body.getElementById("supporting-docs-one").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.one")
      document.body.getElementById("supporting-docs-two").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.two")
      document.body.getElementById("supporting-docs-three").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.three")
      document.body.getElementById("supporting-docs-four").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.four")
      document.body.getElementById("supporting-docs-five").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.five")


      document.body.getElementById("main-heading").text() shouldBe Messages("page.fileUpload.heading")
      document.body.getElementById("file-upload-desc").text() shouldBe Messages("page.fileUpload.desc")
      document.body.getElementById("file-upload-restriction").text() shouldBe Messages("page.fileUpload.restriction")
      document.body.getElementById("what-to-upload").text() shouldBe Messages("page.fileUpload.whattoupload")
      document.body.getElementById("file-limit-amount").text() shouldBe Messages("page.fileUpload.limit.amount")
      document.body.getElementById("file-limit-type").text() shouldBe Messages("page.fileUpload.limit.type")
      document.body.getElementById("file-options-heading").text() shouldBe Messages("page.fileUpload.options.heading")
      document.body.getElementById("file-options-save").text() shouldBe Messages("page.fileUpload.options.save")
      document.body.getElementById("file-options-print").text() shouldBe Messages("page.fileUpload.options.print")
      document.body.getElementById("file-options-export").text() shouldBe Messages("page.fileUpload.options.export")
      document.body.getElementById("file-limit-size").text() shouldBe Messages("page.fileUpload.limit.size")

      //file table should not exist
      intercept[NullPointerException] {
        val filesTable = document.getElementById("files-table").select("tbody")
      }

      //Dynamic button
      document.body.getElementById("upload-button").text() shouldBe Messages("page.fileUpload.upload")

      document.body.getElementById("continue-link").text() shouldBe Messages("page.fileUpload.continue")

      document.body.getElementById("back-link").attr("href") shouldEqual routes.SupportingDocumentsController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.five")
    }

    "contain the correct elements when loaded with one or more files" in {

      lazy val page = FileUpload(files, envelopeID)(fakeRequest)
      lazy val document = Jsoup.parse(contentAsString(page))

      //title and heading
      document.title() shouldBe Messages("page.fileUpload.title")

      //sidebar
      document.body.getElementById("supporting-docs-heading").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.heading")
      document.body.getElementById("supporting-docs-one").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.one")
      document.body.getElementById("supporting-docs-two").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.two")
      document.body.getElementById("supporting-docs-three").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.three")
      document.body.getElementById("supporting-docs-four").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.four")
      document.body.getElementById("supporting-docs-five").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.bullet.five")


      document.body.getElementById("main-heading").text() shouldBe Messages("page.fileUpload.heading")
      document.body.getElementById("file-upload-desc").text() shouldBe Messages("page.fileUpload.desc")
      document.body.getElementById("file-upload-restriction").text() shouldBe Messages("page.fileUpload.restriction")
      document.body.getElementById("what-to-upload").text() shouldBe Messages("page.fileUpload.whattoupload")
      document.body.getElementById("file-limit-amount").text() shouldBe Messages("page.fileUpload.limit.amount")
      document.body.getElementById("file-limit-type").text() shouldBe Messages("page.fileUpload.limit.type")
      document.body.getElementById("file-options-heading").text() shouldBe Messages("page.fileUpload.options.heading")
      document.body.getElementById("file-options-save").text() shouldBe Messages("page.fileUpload.options.save")
      document.body.getElementById("file-options-print").text() shouldBe Messages("page.fileUpload.options.print")
      document.body.getElementById("file-options-export").text() shouldBe Messages("page.fileUpload.options.export")
      document.body.getElementById("file-limit-size").text() shouldBe Messages("page.fileUpload.limit.size")

      //file table
      lazy val filesTable = document.getElementById("files-table").select("tbody")
      filesTable.select("tr").get(0).getElementById("file-0").text() shouldBe "testOne.pdf"
      filesTable.select("tr").get(0).getElementById("remove-0").text() shouldBe Messages("page.fileUpload.remove")
      filesTable.select("tr").get(1).getElementById("file-1").text() shouldBe "testTwo.pdf"
      filesTable.select("tr").get(1).getElementById("remove-1").text() shouldBe Messages("page.fileUpload.remove")

      document.body.getElementById("continue-link").text() shouldBe Messages("page.fileUpload.continue")

      document.body.getElementById("back-link").attr("href") shouldEqual routes.SupportingDocumentsController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.five")
    }
  }

}
