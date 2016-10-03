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
import builders.SessionBuilder
import common.{Constants, KeystoreKeys}
import config.FrontendAppConfig
import connectors.{EnrolmentConnector, KeystoreConnector}
import controllers.helpers.FakeRequestHelper
import controllers.{NewProductController, routes}
import models.{NewProductModel, SubsidiariesModel}
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

import scala.concurrent.Future

class NewProductSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  val mockKeystoreConnector = mock[KeystoreConnector]

  val isNewProductModel = new NewProductModel(Constants.StandardRadioButtonYesValue)
  val emptyNewProductModel = new NewProductModel("")
  val modelSubsidiariesNo = SubsidiariesModel(Constants.StandardRadioButtonNoValue)
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(isNewProductModel)))

  class SetupPage {

    val controller = new NewProductController{
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

    when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))
  }

  "Verify that the NewProduct page contains the correct elements " +
    "when a valid NewProductModel is passed as returned from keystore" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeystoreConnector.fetchAndGetFormData[NewProductModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(isNewProductModel)))
      val result = controller.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.NewGeographicalMarketController.show().toString()
    document.title() shouldBe Messages("page.investment.NewProduct.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.NewProduct.heading")
    document.select("#isNewProduct-yes").size() shouldBe 1
    document.select("#isNewProduct-no").size() shouldBe 1
    document.getElementById("isNewProduct-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("isNewProduct-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("bullet-heading-hint").text() shouldBe Messages("page.investment.NewProduct.hint")
    document.getElementById("bullet-one").text() shouldBe Messages("page.investment.NewProduct.bullet.one")
    document.getElementById("bullet-two").text() shouldBe Messages("page.investment.NewProduct.bullet.two")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "Verify that NewProduct page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeystoreConnector.fetchAndGetFormData[NewProductModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(emptyNewProductModel)))
      val result = controller.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.NewGeographicalMarketController.show().toString()
    document.title() shouldBe Messages("page.investment.NewProduct.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.investment.NewProduct.heading")
    document.select("#isNewProduct-yes").size() shouldBe 1
    document.select("#isNewProduct-no").size() shouldBe 1
    document.getElementById("isNewProduct-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("isNewProduct-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("bullet-heading-hint").text() shouldBe Messages("page.investment.NewProduct.hint")
    document.getElementById("bullet-one").text() shouldBe Messages("page.investment.NewProduct.bullet.one")
    document.getElementById("bullet-two").text() shouldBe Messages("page.investment.NewProduct.bullet.two")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.three")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "Verify that NewProduct page contains error summary when invalid model is submitted" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      // submit the model with no radio slected as a post action
      val result = controller.submit.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }

    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.investment.NewProduct.title")
  }

  "Verify that NewProduct page contains no errors when valid model is submitted" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      // submit the model with no radio selected as a post action
      when(mockKeystoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(modelSubsidiariesNo)))
      val result = controller.submit.apply(authorisedFakeRequestToPOST(
        "isNewProduct" -> Constants.StandardRadioButtonYesValue
      ))
      Jsoup.parse(contentAsString(result))
    }

    // Make sure we have the expected error summary displayed
    //document.getElementById("error-summary-display").hasClass("error-summary--show")
    //document.title() shouldBe Messages("page.investment.NewProduct.title")
  }
}
