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

package views

import java.util.UUID

import auth.{Enrolment, Identifier, MockAuthConnector}
import common.{Constants, KeystoreKeys}
import config.FrontendAppConfig
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.{PreviousSchemeController, routes}
import controllers.helpers.FakeRequestHelper
import models.PreviousSchemeModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import views.helpers.ViewTestHelper

import scala.concurrent.Future

class PreviousSchemeSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper with ViewTestHelper {

  val model = PreviousSchemeModel(
    Constants.PageInvestmentSchemeEisValue, 2356, None, None, Some(4), Some(12), Some(2009), Some(1))
  val model2 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeSeisValue, 2356, Some(666), None, Some(4), Some(12), Some(2010), Some(3))
  val model3 = PreviousSchemeModel(
    Constants.PageInvestmentSchemeAnotherValue, 2356, None, Some("My scheme"), Some(9), Some(8), Some(2010), Some(5))

  val emptyVectorList = Vector[PreviousSchemeModel]()
  val previousSchemeVectorList = Vector(model, model2, model3)
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(previousSchemeVectorList)))


  class SetupPage {
    val controller = new PreviousSchemeController {
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val s4lConnector: S4LConnector = mockS4lConnector
      override lazy val enrolmentConnector = mockEnrolmentConnector
    }

    when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))
  }

  "The Previous Scheme page" should {

    "Verify that the page contains the correct elements for a new scheme model and back link" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMap)
        when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]]
          (Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(),Matchers.any()))
          .thenReturn(Future.successful(Option(previousSchemeVectorList)))
        when(mockS4lConnector.fetchAndGetFormData[String]
          (Matchers.eq(KeystoreKeys.backLinkPreviousScheme))(Matchers.any(), Matchers.any(),Matchers.any()))
          .thenReturn(Future.successful(Option(routes.ReviewPreviousSchemesController.show().url)))
        val result = controller.show(None).apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.investment.PreviousScheme.title")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ReviewPreviousSchemesController.show.toString()
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.two")

      document.getElementById("main-heading").text() shouldBe Messages("page.investment.PreviousScheme.heading")

      document.getElementById("scheme-type-legend").text() shouldBe Messages("page.investment.PreviousScheme.schemeType")
      document.getElementById("schemeTypeDesc-eisLabel").text() shouldBe Messages("page.previousInvestment.schemeType.eis")
      document.getElementById("schemeTypeDesc-seisLabel").text() shouldBe Messages("page.previousInvestment.schemeType.seis")
      document.getElementById("schemeTypeDesc-sitrLabel").text() shouldBe Messages("page.previousInvestment.schemeType.sitr")
      document.getElementById("schemeTypeDesc-vctLabel").text() shouldBe Messages("page.previousInvestment.schemeType.vct")
      document.getElementById("schemeTypeDesc-otherLabel").text() shouldBe Messages("page.previousInvestment.schemeType.other")
      document.getElementById("label-amount").text() shouldBe Messages("page.investment.amount.heading")

      document.getElementById("label-amount-spent").text() shouldBe Messages("page.investment.amountSpent.label")
      document.getElementById("label-other-scheme").text() shouldBe Messages("page.investment.PreviousScheme.otherSchemeName.label")

      document.getElementById("question-text-id").text() shouldBe Messages("page.investment.dateOfShareIssue.label")
      document.body.getElementById("investmentDay").parent.text shouldBe Messages("common.date.fields.day")
      document.body.getElementById("investmentMonth").parent.text shouldBe Messages("common.date.fields.month")
      document.body.getElementById("investmentYear").parent.text shouldBe Messages("common.date.fields.year")

      document.getElementById("help").text() shouldBe Messages("page.investment.PreviousScheme.howToFind")
      document.getElementById("date-of-share-issue-where-to-find").text() should include(Messages("page.investment.PreviousScheme.location"))
      document.getElementById("company-house-db").attr("href") shouldBe "https://www.gov.uk/get-information-about-a-company"
      document.body.getElementById("company-house-db").text() shouldEqual getExternalLinkText(Messages("page.investment.PreviousScheme.companiesHouse"))

      // BUTTON SHOULD BE ADD
      document.getElementById("next").text() shouldBe Messages("page.investment.PreviousScheme.button.add")
    }

    "Verify the page contains the correct elements for an exiting scheme model and changed backlink" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMap)
        when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]]
          (Matchers.eq(KeystoreKeys.previousSchemes))(Matchers.any(), Matchers.any(),Matchers.any()))
          .thenReturn(Future.successful(Option(previousSchemeVectorList)))
        when(mockS4lConnector.fetchAndGetFormData[String]
          (Matchers.eq(KeystoreKeys.backLinkPreviousScheme))(Matchers.any(), Matchers.any(),Matchers.any()))
          .thenReturn(Future.successful(Option(routes.HadPreviousRFIController.show().url)))
        val result = controller.show(Some(model3.processingId.get)).apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.investment.PreviousScheme.title")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.HadPreviousRFIController.show.toString()
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.two")

      document.getElementById("main-heading").text() shouldBe Messages("page.investment.PreviousScheme.heading")

      document.getElementById("scheme-type-legend").text() shouldBe Messages("page.investment.PreviousScheme.schemeType")
      document.getElementById("schemeTypeDesc-eisLabel").text() shouldBe Messages("page.previousInvestment.schemeType.eis")
      document.getElementById("schemeTypeDesc-seisLabel").text() shouldBe Messages("page.previousInvestment.schemeType.seis")
      document.getElementById("schemeTypeDesc-sitrLabel").text() shouldBe Messages("page.previousInvestment.schemeType.sitr")
      document.getElementById("schemeTypeDesc-vctLabel").text() shouldBe Messages("page.previousInvestment.schemeType.vct")
      document.getElementById("schemeTypeDesc-otherLabel").text() shouldBe Messages("page.previousInvestment.schemeType.other")
      document.getElementById("label-amount").text() shouldBe Messages("page.investment.amount.heading")

      document.getElementById("label-amount-spent").text() shouldBe Messages("page.investment.amountSpent.label")
      document.getElementById("label-other-scheme").text() shouldBe Messages("page.investment.PreviousScheme.otherSchemeName.label")

      document.getElementById("question-text-id").text() shouldBe Messages("page.investment.dateOfShareIssue.label")
      document.body.getElementById("investmentDay").parent.text shouldBe Messages("common.date.fields.day")
      document.body.getElementById("investmentMonth").parent.text shouldBe Messages("common.date.fields.month")
      document.body.getElementById("investmentYear").parent.text shouldBe Messages("common.date.fields.year")

      document.getElementById("help").text() shouldBe Messages("page.investment.PreviousScheme.howToFind")
      document.getElementById("date-of-share-issue-where-to-find").text() should include(Messages("page.investment.PreviousScheme.location"))
      document.getElementById("company-house-db").attr("href") shouldBe "https://www.gov.uk/get-information-about-a-company"
      document.body.getElementById("company-house-db").text() shouldEqual getExternalLinkText(Messages("page.investment.PreviousScheme.companiesHouse"))

      // BUTTON SHOULD BE UPDATE
      document.getElementById("next").text() shouldBe Messages("page.investment.PreviousScheme.button.update")
    }

    "Verify the previous scheeme page contains the error summary, button text and back link for invalid new submisison" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
          .thenReturn(Future.successful(Option(previousSchemeVectorList)))
        when(mockS4lConnector.fetchAndGetFormData[String]
          (Matchers.eq(KeystoreKeys.backLinkPreviousScheme))(Matchers.any(), Matchers.any(),Matchers.any()))
          .thenReturn(Future.successful(Option(routes.RegisteredAddressController.show().url)))
        val result = controller.submit().apply(authorisedFakeRequestToPOST(
          "schemeTypeDesc" -> Constants.PageInvestmentSchemeSeisValue,
          "investmentAmount" -> "",
          "investmentSpent" -> "777",
          "otherSchemeName" -> "",
          "investmentDay" -> "7",
          "investmentMonth" -> "3",
          "investmentYear" -> "2015",
          "processingId" -> ""
        ))
        Jsoup.parse(contentAsString(result))
      }

      // BUTTON SHOULD BE ADD
      document.getElementById("next").text() shouldBe Messages("page.investment.PreviousScheme.button.add")
      // SHOULD BE ERROR SECTION AS NO Amount posted
      document.getElementById("error-summary-display").hasClass("error-summary--show")

      document.body.getElementById("back-link").attr("href") shouldEqual routes.RegisteredAddressController.show.toString()
    }
  }

  "Verify that the proposed investment page contains the error summary and button text when an invalid new submisison os posted" in new SetupPage {
    val document: Document = {
      val userId = s"user-${UUID.randomUUID}"

      when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(previousSchemeVectorList)))
      when(mockS4lConnector.fetchAndGetFormData[String]
        (Matchers.eq(KeystoreKeys.backLinkPreviousScheme))(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(Future.successful(Option(routes.HadPreviousRFIController.show().url)))
      val result = controller.submit().apply(authorisedFakeRequestToPOST(
        "schemeTypeDesc" -> Constants.PageInvestmentSchemeSeisValue,
        "investmentAmount" -> "",
        "investmentSpent" -> "777",
        "otherSchemeName" -> "",
        "investmentDay" -> "7",
        "investmentMonth" -> "3",
        "investmentYear" -> "2015",
        "processingId" -> "1"
      ))
      Jsoup.parse(contentAsString(result))
    }

    // BUTTON SHOULD BE UPDATE
    document.getElementById("next").text() shouldBe Messages("page.investment.PreviousScheme.button.update")
    // SHOULD BE ERROR SECTION AS NO Amount posted
    document.getElementById("error-summary-display").hasClass("error-summary--show")

    document.body.getElementById("back-link").attr("href") shouldEqual routes.HadPreviousRFIController.show.toString()

  }

}
