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

package views.eis

import auth.{MockConfigEISFlow, MockAuthConnector}
import common.KeystoreKeys
import config.FrontendAppConfig
import controllers.eis.SubsidiariesController
import models.SubsidiariesModel
import org.mockito.Matchers
import org.mockito.Mockito._
import views.helpers.ViewSpec

import scala.concurrent.Future

class SubsidiariesSpec extends ViewSpec {

  object TestController extends SubsidiariesController {
    override lazy val applicationConfig = MockConfigEISFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(subsidiariesModel: Option[SubsidiariesModel] = None, backLink: Option[String] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(subsidiariesModel))
    when(mockS4lConnector.fetchAndGetFormData[String]
      (Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(backLink))
  }

  "The Subsidiaries page" should {

    // SINCE THE CONTROLLER REDIRECTS THE PAGE IS NEVER RETURNED
//    "Verify the Subsidiaries page contains the correct elements when a valid 'Yes' SubsidiariesModel is retrieved" +
//      "from keystore and IsKnowledgeIntensiveController back link also retrieved" in new Setup {
//      val document: Document = {
//        setupMocks(Some(subsidiariesModelYes),Some(routes.IsKnowledgeIntensiveController.show().url))
//        val result = TestController.show.apply(authorisedFakeRequest)
//        Jsoup.parse(contentAsString(result))
//      }
//      // Back link should change based saved backlink retrieved from keystore (IsKnowledgeIntensiveController in this test)
//      document.body.getElementById("back-link").attr("href") shouldEqual routes.IsKnowledgeIntensiveController.show().url
//      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
//      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
//      document.select("#subsidiaries-yes").size() shouldBe 1
//      document.select("#subsidiaries-yes").size() shouldBe 1
//      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
//      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
//      document.getElementById("next").text() shouldBe Messages("common.button.continue")
//      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
//    }
//
//    "Verify the Subsidiaries page contains the correct elements when a valid 'Yes' SubsidiariesModel is retrieved" +
//      "from keystore and PercentageStaffWithMastersController back link also retrieved" in new Setup {
//      val document: Document = {
//        setupMocks(Some(subsidiariesModelYes), Some(routes.PercentageStaffWithMastersController.show().url))
//        val result = TestController.show.apply(authorisedFakeRequest)
//        Jsoup.parse(contentAsString(result))
//      }
//      // Back link should change based saved backlink retrieved from keystore (IsKnowledgeIntensiveController in this test)
//      document.body.getElementById("back-link").attr("href") shouldEqual
//        routes.PercentageStaffWithMastersController.show().url
//      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
//      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
//      document.select("#subsidiaries-yes").size() shouldBe 1
//      document.select("#subsidiaries-yes").size() shouldBe 1
//      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
//      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
//      document.getElementById("next").text() shouldBe Messages("common.button.continue")
//      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
//    }
//
//    "Verify the Subsidiaries page contains the correct elements when a valid 'No' SubsidiariesModel is retrieved" +
//      "from keystore and TenYearPlanController back link also retrieved" in new Setup {
//      val document: Document = {
//        setupMocks(Some(subsidiariesModelYes), Some(routes.TenYearPlanController.show().url))
//        val result = TestController.show.apply(authorisedFakeRequest)
//        Jsoup.parse(contentAsString(result))
//      }
//      // Back link should change based saved backlink retrieved from keystore (TenYearPlanController in this test)
//      document.body.getElementById("back-link").attr("href") shouldEqual routes.TenYearPlanController.show().toString
//      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
//      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
//      document.select("#subsidiaries-yes").size() shouldBe 1
//      document.select("#subsidiaries-yes").size() shouldBe 1
//      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
//      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
//      document.getElementById("next").text() shouldBe Messages("common.button.continue")
//      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
//    }
//
//    "Verify the Subsidiaries page contains the correct elements when a valid 'No' SubsidiariesModel is retrieved" +
//      "from keystore and CommercialSaleController back link also retrieved" in new Setup {
//      val document: Document = {
//        setupMocks(Some(subsidiariesModelNo), Some(routes.CommercialSaleController.show().url))
//        val result = TestController.show.apply(authorisedFakeRequest)
//        Jsoup.parse(contentAsString(result))
//      }
//      // Back link should change based saved backlink retrieved from keystore (CommercialSaleController in this test)
//      document.body.getElementById("back-link").attr("href") shouldEqual routes.CommercialSaleController.show().toString
//      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
//      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
//      document.select("#subsidiaries-yes").size() shouldBe 1
//      document.select("#subsidiaries-yes").size() shouldBe 1
//      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
//      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
//      document.getElementById("next").text() shouldBe Messages("common.button.continue")
//      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
//    }
//
//    "Verify that Subsidiaries page contains the correct elements when an empty model " +
//      "is passed because nothing was returned from keystore" in new Setup {
//      val document: Document = {
//        setupMocks(backLink = Some(routes.CommercialSaleController.show().url))
//        val result = TestController.show.apply(authorisedFakeRequestToPOST(
//          "ownSubsidiaries" -> Constants.StandardRadioButtonNoValue
//        ))
//        Jsoup.parse(contentAsString(result))
//      }
//      // Back link should change based on the value of date of incorporation retrieved from keystore
//      document.body.getElementById("back-link").attr("href") shouldEqual routes.CommercialSaleController.show().url
//      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
//      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
//      document.select("#subsidiaries-yes").size() shouldBe 1
//      document.select("#subsidiaries-yes").size() shouldBe 1
//      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
//      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
//      document.getElementById("next").text() shouldBe Messages("common.button.continue")
//      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
//    }
//
//    "Verify that Subsidiaries page shows the error summary and has the correct back link when " +
//      " an invalid model (no radio button selection) is submitted" in new Setup {
//      val document: Document = {
//        setupMocks(backLink = Some(routes.IsKnowledgeIntensiveController.show().url))
//        //submit the model with no radio selected as a post action
//        val result = TestController.submit.apply(authorisedFakeRequestToPOST(
//          "subsidiaries" -> ""
//        ))
//        Jsoup.parse(contentAsString(result))
//      }
//      // Make sure we have the expected error summary displayed and correct backv link rendered on error
//      document.getElementById("error-summary-display").hasClass("error-summary--show")
//      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
//      document.body.getElementById("back-link").attr("href") shouldEqual routes.IsKnowledgeIntensiveController.show().url
//    }
//
//    // this should never happen but defensive code is present so test it..
//    "Verify that Subsidiaries page shows the error summary and has a default 'date of incorporation' back link when " +
//      "no back link is retrieved and an invalid model (no radio button selection) is submitted" in new Setup {
//      val document: Document = {
//        setupMocks()
//        //submit the model with no radio selected as a post action
//        val result = TestController.submit.apply(authorisedFakeRequestToPOST(
//          "subsidiaries" -> ""
//        ))
//        Jsoup.parse(contentAsString(result))
//      }
//      // Make sure we have the expected error summary displayed and correct back link rendered on error
//      document.getElementById("error-summary-display").hasClass("error-summary--show")
//      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
//      document.body.getElementById("back-link").attr("href") shouldEqual routes.DateOfIncorporationController.show().url
//    }

  }

}
