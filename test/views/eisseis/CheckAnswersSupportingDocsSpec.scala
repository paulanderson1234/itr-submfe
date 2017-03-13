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

package views.eisseis

import auth.{MockConfig, MockAuthConnector}
import common.KeystoreKeys
import config.FrontendAppConfig
import controllers.eisseis.CheckAnswersController
import controllers.routes
import models.submission.SchemeTypesModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.CheckAnswersSpec
import scala.concurrent.Future

class CheckAnswersSupportingDocsSpec extends CheckAnswersSpec {

  object TestController extends CheckAnswersController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 5: Supporting Documents" +
      " when the page is loaded" in new Setup {

      when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(eisSeisSchemeTypesModel))

      val document: Document = {
        previousRFISetup()
        investmentSetup()
        contactDetailsSetup()
        companyDetailsSetup()
        tradeStartDateSetup()
        contactAddressSetup()
        isSeisInEligibleSetup(Some(eisSeisProcessingModelEligible))
        val result = TestController.show(None).apply(authorisedFakeRequest.withFormUrlEncodedBody())
        Jsoup.parse(contentAsString(result))
      }


      document.title() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description-one").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.one")
      document.getElementById("description-two").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description.two")

      lazy val supportingDocsTableBody = document.getElementById("supporting-docs-table").select("tbody")

      //Section 5 table heading
      document.getElementById("supportingDocsSection-table-heading").text() shouldBe Messages("page.summaryQuestion.supportingDocsSection")
      document.getElementById("supportingDocs-sub-text").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.suportingDocs.desc")
      supportingDocsTableBody .select("tr").get(0).getElementById("supportingDocs-business-plan").text() shouldBe
        Messages("page.supportingDocuments.SupportingDocuments.bullet.one")
      supportingDocsTableBody .select("tr").get(0).getElementById("supportingDocs-company-accounts").text() shouldBe
        Messages("page.supportingDocuments.SupportingDocuments.bullet.two")
      supportingDocsTableBody .select("tr").get(0).getElementById("shareholder-agree").text() shouldBe
        Messages("page.supportingDocuments.SupportingDocuments.bullet.three")
      supportingDocsTableBody .select("tr").get(0).getElementById("memorandum-docs").text() shouldBe
        Messages("page.supportingDocuments.SupportingDocuments.bullet.four")
      supportingDocsTableBody .select("tr").get(0).getElementById("supportingDocs-prospectus").text() shouldBe
        Messages("page.supportingDocuments.SupportingDocuments.bullet.five")


      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.SupportingDocumentsController.show().url
    }
  }
}
