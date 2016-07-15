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

import connectors.KeystoreConnector
import controllers.{SubsidiariesController, routes}
import controllers.helpers.FakeRequestHelper
import models.{IsKnowledgeIntensiveModel, DateOfIncorporationModel, SubsidiariesModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import java.time.ZoneId
import java.util.{Date, UUID}

import common.KeystoreKeys
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.OneServerPerSuite
import uk.gov.hmrc.http.cache.client.CacheMap
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class SubsidiariesSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper with BeforeAndAfterEach with OneServerPerSuite{

  val mockKeystoreConnector = mock[KeystoreConnector]

  val subsidiariesModelYes = new SubsidiariesModel("Yes")
  val subsidiariesModelNo = new SubsidiariesModel("No")
  val emptySubsidiariesModel = new SubsidiariesModel("")
  val model = SubsidiariesModel("Yes")
  val emptyModel = SubsidiariesModel("")
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(model)))

  // set up border line conditions of today and future date (tomorrow)
  val date = new Date();
  val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
  val date3YearsAgo = localDate.minusYears(3)
  val date3YearsAgoDay: Int = date3YearsAgo.getDayOfMonth
  val date3YearsAgoMonth: Int = date3YearsAgo.getMonthValue
  val date3YearsAgoYear: Int = date3YearsAgo.getYear

  val date3YearsOneDay = localDate.minusYears(3).minusDays(1)
  val date3YearsOneDayDay: Int = date3YearsOneDay.getDayOfMonth
  val date3YearsOneDayMonth: Int = date3YearsOneDay.getMonthValue
  val date3YearsOneDayYear: Int = date3YearsOneDay.getYear

  val date3YearsLessOneDay = localDate.minusYears(3).plusDays(1)
  val date3YearsLessOneDayDay: Int = date3YearsLessOneDay.getDayOfMonth
  val date3YearsLessOneDayMonth: Int = date3YearsLessOneDay.getMonthValue
  val date3YearsLessOneDayYear: Int = date3YearsLessOneDay.getYear

  val todayDay:String = localDate.getDayOfMonth.toString
  val todayMonth: String = localDate.getMonthValue.toString
  val todayYear: String = localDate.getYear.toString

  val keyStoreSavedSubsidiaries = SubsidiariesModel("Yes")
  val keyStoreSavedIsKnowledgeIntensiveYes = IsKnowledgeIntensiveModel("Yes")
  val keyStoreSavedIsKnowledgeIntensiveNo = IsKnowledgeIntensiveModel("No")
  val keyStoreSavedDateOfIncorporation3Years = DateOfIncorporationModel(Some(date3YearsAgoDay),Some(date3YearsAgoMonth),Some(date3YearsAgoYear))
  val keyStoreSavedDateOfIncorporation3YearsLessOneDay = DateOfIncorporationModel(Some(date3YearsLessOneDayDay),Some(date3YearsLessOneDayMonth),Some(date3YearsLessOneDayYear))
  val keyStoreSavedDateOfIncorporation3YearsAndOneDay = DateOfIncorporationModel(Some(date3YearsOneDayDay),Some(date3YearsOneDayMonth),Some(date3YearsOneDayYear))

  class SetupPage {

    val controller = new SubsidiariesController {
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  override def beforeEach() {
    reset(mockKeystoreConnector)
  }

  "The Subsidiaries page" should {

    "Verify that the Subsidiaries page contains the correct elements " +
      "when a valid 'Yes' SubsidiariesModel is retrieved from keystore and date of incorporation is exactly 3 years from today from keystore" in new SetupPage {
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation3Years)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModelYes)))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
          "ownSubsidiaries" -> "Yes"
        )))
        Jsoup.parse(contentAsString(result))
      }

      // Back link should change based on the value of date of incorporation retrieved from keystore
      document.body.getElementById("back-link").attr("href") shouldEqual routes.IsKnowledgeIntensiveController.show.toString()
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
    }

    "Verify that the Subsidiaries page contains the correct elements " +
      "when a valid 'Yes' SubsidiariesModel is retrieved from keystore and date of incorporation is more than 3 years from today from keystore" in new SetupPage {
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation3YearsAndOneDay)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModelYes)))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
          "ownSubsidiaries" -> "Yes"
        )))
        Jsoup.parse(contentAsString(result))
      }

      // Back link should change based on the value of date of incorporation retrieved from keystore
      document.body.getElementById("back-link").attr("href") shouldEqual routes.IsKnowledgeIntensiveController.show.toString()
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
    }

    "Verify that the Subsidiaries page contains the correct elements " +
      "when a valid 'Yes' SubsidiariesModel is retrieved from keystore and date of incorporation is less than 3 years from today from keystore" in new SetupPage {
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation3YearsLessOneDay)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModelYes)))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
          "ownSubsidiaries" -> "Yes"
        )))
        Jsoup.parse(contentAsString(result))
      }

      // Back link should change based on the value of date of incorporation retrieved from keystore
      document.body.getElementById("back-link").attr("href") shouldEqual routes.CommercialSaleController.show.toString()
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
    }

    "Verify that the Subsidiaries page contains the correct elements " +
      "when a valid 'Yes' SubsidiariesModel is retrieved from keystore but the date of incorporation is empty from keystore" in new SetupPage {
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModelYes)))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
          "ownSubsidiaries" -> "Yes"
        )))
        Jsoup.parse(contentAsString(result))
      }

      // Back link should change based on the value of date of incorporation retrieved from keystore
      document.body.getElementById("back-link").attr("href") shouldEqual routes.DateOfIncorporationController.show.toString()
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
    }


    "Verify that the Subsidiaries page contains the correct elements " +
      "when a valid 'No' SubsidiariesModel is retrieved from keystore and date of incorporation is exactly 3 years from today from keystore" in new SetupPage {
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation3Years)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModelNo)))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
          "ownSubsidiaries" -> "No"
        )))
        Jsoup.parse(contentAsString(result))
      }

      // Back link should change based on the value of date of incorporation retrieved from keystore
      document.body.getElementById("back-link").attr("href") shouldEqual routes.IsKnowledgeIntensiveController.show.toString()
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
    }

    "Verify that the Subsidiaries page contains the correct elements " +
     "when a valid 'No' SubsidiariesModel is retrieved from keystore and date of incorporation is more than 3 years from today from keystore" in new SetupPage {
        val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation3YearsAndOneDay)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModelNo)))
          when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
            .thenReturn(Future.successful(None))
        val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
          "ownSubsidiaries" -> "No"
        )))
        Jsoup.parse(contentAsString(result))
      }

      // Back link should change based on the value of date of incorporation retrieved from keystore
      document.body.getElementById("back-link").attr("href") shouldEqual routes.IsKnowledgeIntensiveController.show.toString()
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
    }

    "Verify that the Subsidiaries page contains the correct elements " +
      "when a valid 'No' SubsidiariesModel is retrieved from keystore and date of incorporation is less than 3 years from today from keystore" in new SetupPage {
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation3YearsLessOneDay)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModelNo)))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
          "ownSubsidiaries" -> "No"
        )))
        Jsoup.parse(contentAsString(result))
      }

      // Back link should change based on the value of date of incorporation retrieved from keystore
      document.body.getElementById("back-link").attr("href") shouldEqual routes.CommercialSaleController.show.toString()
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
    }

    "Verify that the Subsidiaries page contains the correct elements " +
      "when a valid 'No' SubsidiariesModel is retrieved from keystore but the date of incorporation is empty from keystore" in new SetupPage {
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
          "ownSubsidiaries" -> "No"
        )))
        Jsoup.parse(contentAsString(result))
      }

      // Back link should change based on the value of date of incorporation retrieved from keystore
      document.body.getElementById("back-link").attr("href") shouldEqual routes.DateOfIncorporationController.show.toString()
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
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation3YearsLessOneDay)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(emptySubsidiariesModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
          "ownSubsidiaries" -> "No"
        )))
        Jsoup.parse(contentAsString(result))
      }

      // Back link should change based on the value of date of incorporation retrieved from keystore
      document.body.getElementById("back-link").attr("href") shouldEqual routes.CommercialSaleController.show.toString()
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
    }

    "Verify that Subsidiaries page contains show the error summary when an invalid model (no radio button selection) +" +
      "is submitted and backlink 3 years" in new SetupPage {
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation3Years)))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
       //submit the model with no radio slected as a post action
        val result = controller.submit.apply((fakeRequestWithSession))
        Jsoup.parse(contentAsString(result))
      }

      // Make sure we have the expected error summary displayed and correct backv link rendered on error
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.IsKnowledgeIntensiveController.show.toString()
    }

    "Verify that Subsidiaries page contains show the error summary when an invalid model (no radio button selection) +" +
      "is submitted and backlink more than years" in new SetupPage {
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation3YearsAndOneDay)))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        //submit the model with no radio slected as a post action
        val result = controller.submit.apply((fakeRequestWithSession))
        Jsoup.parse(contentAsString(result))
      }

      // Make sure we have the expected error summary displayed and correct backv link rendered on error
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.IsKnowledgeIntensiveController.show.toString()
    }

    "Verify that Subsidiaries page contains show the error summary when an invalid model (no radio button selection) +" +
      "is submitted and backlink less than 3 years" in new SetupPage {
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation3YearsLessOneDay)))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        //submit the model with no radio slected as a post action
        val result = controller.submit.apply((fakeRequestWithSession))
        Jsoup.parse(contentAsString(result))
      }

      // Make sure we have the expected error summary displayed and correct backv link rendered on error
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.CommercialSaleController.show.toString()
    }

    "Verify that Subsidiaries page contains show the error summary when an invalid model (no radio button selection) +" +
      "is submitted and backlink when no date of incorporation in keystore" in new SetupPage {
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        //submit the model with no radio slected as a post action
        val result = controller.submit.apply((fakeRequestWithSession))
        Jsoup.parse(contentAsString(result))
      }

      // Make sure we have the expected error summary displayed and correct backv link rendered on error
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.DateOfIncorporationController.show.toString()
    }


    "Verify that the Subsidiaries page contains the correct elements " +
      "when a valid Yes SubsidiariesModel is retrieved from keystore and date of incorporation is more than 3 years from today from keystore " +
      "and it is not a knowledge intensive company" in new SetupPage {
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation3YearsAndOneDay)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModelYes)))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveNo)))
        val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
          "ownSubsidiaries" -> "Yes"
        )))
        Jsoup.parse(contentAsString(result))
      }

      // Back link should change based on the value of date of incorporation retrieved from keystore
      document.body.getElementById("back-link").attr("href") shouldEqual routes.IsKnowledgeIntensiveController.show.toString()
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
    }

    "Verify that the Subsidiaries page contains the correct elements " +
      "when a valid Yes SubsidiariesModel is retrieved from keystore and date of incorporation is more than 3 years from today from keystore " +
      "and it is a knowledge intensive company" in new SetupPage {
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation3YearsAndOneDay)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModelYes)))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveYes)))
        val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
          "ownSubsidiaries" -> "Yes"
        )))
        Jsoup.parse(contentAsString(result))
      }

      // Back link should change based on the value of date of incorporation retrieved from keystore
      document.body.getElementById("back-link").attr("href") shouldEqual routes.TenYearPlanController.show.toString()
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
    }

    "Verify that the Subsidiaries page contains the correct elements " +
      "when a valid No SubsidiariesModel is retrieved from keystore and date of incorporation is more than 3 years from today from keystore " +
      "and it is not a knowledge intensive company" in new SetupPage {
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation3YearsAndOneDay)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModelNo)))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveNo)))
        val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
          "ownSubsidiaries" -> "Yes"
        )))
        Jsoup.parse(contentAsString(result))
      }

      // Back link should change based on the value of date of incorporation retrieved from keystore
      document.body.getElementById("back-link").attr("href") shouldEqual routes.IsKnowledgeIntensiveController.show.toString()
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
    }

    "Verify that the Subsidiaries page contains the correct elements " +
      "when a valid No SubsidiariesModel is retrieved from keystore and date of incorporation is more than 3 years from today from keystore " +
      "and it is a knowledge intensive company" in new SetupPage {
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation3YearsAndOneDay)))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModelNo)))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveYes)))
        val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
          "ownSubsidiaries" -> "Yes"
        )))
        Jsoup.parse(contentAsString(result))
      }

      // Back link should change based on the value of date of incorporation retrieved from keystore
      document.body.getElementById("back-link").attr("href") shouldEqual routes.TenYearPlanController.show.toString()
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
    }

    "Verify that the Subsidiaries page contains the correct elements " +
      "when a valid Yes SubsidiariesModel is retrieved from keystore and date of incorporation is empty " +
      "and it is a knowledge intensive company" in new SetupPage {
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModelYes)))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveYes)))
        val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
          "ownSubsidiaries" -> "Yes"
        )))
        Jsoup.parse(contentAsString(result))
      }

      // Back link should change based on the value of date of incorporation retrieved from keystore
      document.body.getElementById("back-link").attr("href") shouldEqual routes.DateOfIncorporationController.show.toString()
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
    }

    "Verify that the Subsidiaries page contains the correct elements " +
      "when a valid No SubsidiariesModel is retrieved from keystore and date of incorporation is empty " +
      "and it is not a knowledge intensive company" in new SetupPage {
      val document : Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(subsidiariesModelNo)))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedIsKnowledgeIntensiveNo)))
        val result = controller.show.apply((fakeRequestWithSession.withFormUrlEncodedBody(
          "ownSubsidiaries" -> "Yes"
        )))
        Jsoup.parse(contentAsString(result))
      }

      // Back link should change based on the value of date of incorporation retrieved from keystore
      document.body.getElementById("back-link").attr("href") shouldEqual routes.DateOfIncorporationController.show.toString()
      document.title() shouldBe Messages("page.companyDetails.Subsidiaries.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.Subsidiaries.heading")
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.select("#subsidiaries-yes").size() shouldBe 1
      document.getElementById("subsidiaries-yesLabel").text() shouldBe Messages("common.radioYesLabel")
      document.getElementById("subsidiaries-noLabel").text() shouldBe Messages("common.radioNoLabel")
      document.getElementById("next").text() shouldBe Messages("common.button.continueNextSection")
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
    }

  }
}
