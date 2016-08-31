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

import connectors.KeystoreConnector
import controllers.{SubsidiariesController, routes}
import controllers.helpers.FakeRequestHelper
import models.SubsidiariesModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import java.util.UUID

import common.{Constants, KeystoreKeys}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.OneServerPerSuite
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class SubsidiariesSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper with BeforeAndAfterEach with OneServerPerSuite {

  val mockKeystoreConnector = mock[KeystoreConnector]

  val subsidiariesModelYes = new SubsidiariesModel(Constants.StandardRadioButtonYesValue)
  val subsidiariesModelNo = new SubsidiariesModel(Constants.StandardRadioButtonNoValue)

  class SetupPage {

    val controller = new SubsidiariesController {
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  override def beforeEach() {
    reset(mockKeystoreConnector)
  }

  "The Subsidiaries page" should {

    "Verify the Subsidiaries page contains the correct elements when a valid 'Yes' SubsidiariesModel is retrieved" +
      "from keystore and IsKnowledgeIntensiveController backlink also retrieved" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[String]
          (Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(routes.IsKnowledgeIntensiveController.show().toString())))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModelYes)))
        val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody(
          "ownSubsidiaries" -> Constants.StandardRadioButtonYesValue
        ))
        Jsoup.parse(contentAsString(result))
      }

      // Back link should change based saved backlink retrieved from keystore (IsKnowledgeIntensiveController in this test)
      document.body.getElementById("back-link").attr("href") shouldEqual routes.IsKnowledgeIntensiveController.show().toString()
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
    }

    "Verify the Subsidiaries page contains the correct elements when a valid 'Yes' SubsidiariesModel is retrieved" +
      "from keystore and PercentageStaffWithMastersController backlink also retrieved" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[String]
          (Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(routes.PercentageStaffWithMastersController.show().toString())))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModelYes)))
        val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody(
          "ownSubsidiaries" -> Constants.StandardRadioButtonYesValue
        ))
        Jsoup.parse(contentAsString(result))
      }

      // Back link should change based saved backlink retrieved from keystore (IsKnowledgeIntensiveController in this test)
      document.body.getElementById("back-link").attr("href") shouldEqual
        routes.PercentageStaffWithMastersController.show().toString()
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
    }

    "Verify the Subsidiaries page contains the correct elements when a valid 'No' SubsidiariesModel is retrieved" +
      "from keystore and TenYearPlanController backlink also retrieved" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[String]
          (Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(routes.TenYearPlanController.show().toString())))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel]
          (Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModelNo)))
        val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody(
          "ownSubsidiaries" -> Constants.StandardRadioButtonYesValue
        ))
        Jsoup.parse(contentAsString(result))
      }

      // Back link should change based saved backlink retrieved from keystore (TenYearPlanController in this test)
      document.body.getElementById("back-link").attr("href") shouldEqual routes.TenYearPlanController.show().toString
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
    }

    "Verify the Subsidiaries page contains the correct elements when a valid 'No' SubsidiariesModel is retrieved" +
      "from keystore and CommercialSaleController backlink also retrieved" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[String]
          (Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(routes.CommercialSaleController.show().toString())))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel]
          (Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModelNo)))
        val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody(
          "ownSubsidiaries" -> Constants.StandardRadioButtonYesValue
        ))
        Jsoup.parse(contentAsString(result))
      }

      // Back link should change based saved backlink retrieved from keystore (CommercialSaleController in this test)
      document.body.getElementById("back-link").attr("href") shouldEqual routes.CommercialSaleController.show().toString
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
    }

    "Verify that Subsidiaries page contains the correct elements when an empty model " +
      "is passed because nothing was returned from keystore" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[String]
          (Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(routes.CommercialSaleController.show().toString())))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel]
          (Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody(
          "ownSubsidiaries" -> Constants.StandardRadioButtonNoValue
        ))
        Jsoup.parse(contentAsString(result))
      }

      // Back link should change based on the value of date of incorporation retrieved from keystore
      document.body.getElementById("back-link").attr("href") shouldEqual routes.CommercialSaleController.show().toString()
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
    }

    "Verify that Subsidiaries page shows the error summary and has the correct back link when " +
      " an invalid model (no radio button selection) is submitted" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[String]
          (Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(routes.IsKnowledgeIntensiveController.show().toString())))
        //submit the model with no radio selected as a post action
        val result = controller.submit.apply(fakeRequestWithSession.withFormUrlEncodedBody(
          "subsidiaries" -> ""
        ))
        Jsoup.parse(contentAsString(result))
      }

      // Make sure we have the expected error summary displayed and correct backv link rendered on error
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.IsKnowledgeIntensiveController.show().toString()
    }

    // this should never happen but defensive code is present so test it..
    "Verify that Subsidiaries page shows the error summary and has a default 'date of incorporation' back link when " +
      "no backlink is retrieved and an invalid model (no radio button selection) is submitted" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[String]
          (Matchers.eq(KeystoreKeys.backLinkSubsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        //submit the model with no radio selected as a post action
        val result = controller.submit.apply(fakeRequestWithSession.withFormUrlEncodedBody(
          "subsidiaries" -> ""
        ))
        Jsoup.parse(contentAsString(result))
      }

      // Make sure we have the expected error summary displayed and correct backv link rendered on error
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.DateOfIncorporationController.show().toString()
    }

  }

}
