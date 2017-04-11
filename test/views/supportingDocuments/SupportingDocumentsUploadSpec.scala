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
import controllers.SupportingDocumentsUploadController
import models.fileUpload.{EnvelopeFile, Metadata}
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits.applicationMessages
import views.helpers.ViewSpec
import views.html.supportingDocuments.SupportingDocumentsUpload

class SupportingDocumentsUploadSpec extends ViewSpec {

  val files = Seq(EnvelopeFile("1", "PROCESSING", "test.pdf", "application/pdf", "2016-03-31T12:33:45Z", Metadata(None), "test.url"))

  object TestController extends SupportingDocumentsUploadController {
    override lazy val applicationConfig = MockConfigEISFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override val fileUploadService = mockFileUploadService
  }

  "The SupportingDocumentsUpload page" should {

    "verify that the supporting documents upload page contains the correct information" in {
      lazy val page = SupportingDocumentsUpload()(fakeRequest, applicationMessages)
      lazy val document = Jsoup.parse(page.body)
      document.title() shouldBe Messages("page.supportingDocuments.SupportingDocuments.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.heading")
      document.getElementById("intro").text() shouldBe Messages("page.supportingDocumentsUpload.outFlow.heading")

      document.getElementById("bullet-one").text() shouldBe Messages("page.supportingDocumentsUpload.bullet.one")
      document.getElementById("bullet-two").text() shouldBe Messages("page.supportingDocumentsUpload.bullet.two")
      document.getElementById("bullet-three").text() shouldBe Messages("page.supportingDocumentsUpload.bullet.three")
      document.getElementById("bullet-four").text() shouldBe Messages("page.supportingDocumentsUpload.bullet.four")
      document.getElementById("bullet-five").text() shouldBe Messages("page.supportingDocumentsUpload.bullet.five")
      document.getElementById("docs-current").text() shouldBe Messages("page.supportingDocumentsUpload.docs.current")
      document.getElementById("noticeMessage").text() shouldBe Messages("page.supportingDocumentsUpload.outFlow.Note")

      document.getElementById("submit").text() shouldBe Messages("common.button.upload")
      document.getElementById("cancel-link").text() shouldBe Messages("common.button.cancel")
    }
  }
}
