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

package views

import common.Constants
import controllers.routes
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.helpers.ViewSpec
import forms.SupportingDocumentsUploadForm._
import views.html.supportingDocuments.SupportingDocumentsUpload
import play.api.i18n.Messages.Implicits._

class SupportingDocumentsUploadSpec extends ViewSpec {

  "The Supporting documents upload page" should {

    lazy val form = supportingDocumentsUploadForm.bind(Map("doUpload" -> Constants.StandardRadioButtonYesValue))
    lazy val emptyForm = supportingDocumentsUploadForm.bind(Map("doUpload" -> ""))
    lazy val page = SupportingDocumentsUpload(form, routes.ConfirmCorrespondAddressController.show().url)(fakeRequest, applicationMessages)
    lazy val emptyPage = SupportingDocumentsUpload(emptyForm, routes.ConfirmCorrespondAddressController.show().url)(fakeRequest, applicationMessages)
    lazy val document = Jsoup.parse(contentAsString(page))
    lazy val documentEmpty = Jsoup.parse(contentAsString(emptyPage))

    lazy val supportingDocsTable = document.getElementById("supporting-docs-table").select("tbody")

    "contain the correct elements when a valid form is submitted" in new Setup {
      document.title() shouldBe Messages("page.supportingDocuments.SupportingDocuments.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.heading")
      document.getElementById("intro").text() shouldBe Messages("page.supportingDocumentsUpload.heading")
      document.getElementById("bullet-one").text() shouldBe Messages("page.supportingDocumentsUpload.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.supportingDocumentsUpload.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.supportingDocumentsUpload.bullet.three")
      document.getElementById("bullet-four").text() shouldBe Messages("page.supportingDocumentsUpload.bullet.four")
      document.getElementById("bullet-five").text() shouldBe Messages("page.supportingDocumentsUpload.bullet.five")
      document.getElementById("docs-current").text() shouldBe Messages("page.supportingDocumentsUpload.docs.current")
      supportingDocsTable.select("tr").get(0).getElementsByClass("h2-heading").text() shouldBe Messages("page.supportingDocumentsUpload.Note")
      document.getElementById("send-instruction").text() shouldBe Messages("page.supportingDocumentsUpload.upload.instruction")
      document.getElementById("doUpload-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("doUpload-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")

    }


    "contain the correct elements when an empty form is submitted" in new Setup {
      documentEmpty.title() shouldBe Messages("page.supportingDocuments.SupportingDocuments.title")
      documentEmpty.getElementById("main-heading").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.heading")
      documentEmpty.getElementById("intro").text() shouldBe Messages("page.supportingDocumentsUpload.heading")
      documentEmpty.getElementById("bullet-one").text() shouldBe Messages("page.supportingDocumentsUpload.bullet.one")
      documentEmpty.getElementById("bullet-two").text() shouldBe Messages("page.supportingDocumentsUpload.bullet.two")
      documentEmpty.getElementById("bullet-three").text() shouldBe Messages("page.supportingDocumentsUpload.bullet.three")
      documentEmpty.getElementById("bullet-four").text() shouldBe Messages("page.supportingDocumentsUpload.bullet.four")
      documentEmpty.getElementById("bullet-five").text() shouldBe Messages("page.supportingDocumentsUpload.bullet.five")
      documentEmpty.getElementById("docs-current").text() shouldBe Messages("page.supportingDocumentsUpload.docs.current")
      documentEmpty.select("tr").get(0).getElementsByClass("h2-heading").text() shouldBe Messages("page.supportingDocumentsUpload.Note")
      documentEmpty.getElementById("send-instruction").text() shouldBe Messages("page.supportingDocumentsUpload.upload.instruction")
      documentEmpty.getElementById("doUpload-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      documentEmpty.getElementById("doUpload-noLabel").text() shouldBe Messages("common.radioNoLabel")
      documentEmpty.getElementById("next").text() shouldBe Messages("common.button.continue")
      documentEmpty.getElementById("error-summary-display").hasClass("error-summary--show")
    }
  }

}