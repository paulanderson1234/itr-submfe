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

import builders.SessionBuilder
import connectors.KeystoreConnector
import controllers.helpers.FakeRequestHelper
import controllers.{RegisteredAddressController, routes}
import models.RegisteredAddressModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class RegisteredAddressSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockKeystoreConnector = mock[KeystoreConnector]

  val registeredAddressModel = new RegisteredAddressModel("ST1 1QQ")

  class SetupPage {

    val controller = new RegisteredAddressController {
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  "The Registered Address page" should {

    "Verify that Registered Address page contains the correct elements " +
      "when a valid RegisteredAddressModel is passed as returned from keystore" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(registeredAddressModel)))
        val result = controller.show.apply(SessionBuilder.buildRequestWithSession(userId))
        Jsoup.parse(contentAsString(result))
      }

        document.title shouldBe Messages("page.companyDetails.RegisteredAddress.title")
        document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.RegisteredAddress.heading")
        document.body.getElementById("back-link").attr("href") shouldEqual routes.TaxpayerReferenceController.show().toString()
        document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
        document.body.getElementById("label-postcode").text() should include (Messages("common.address.postcode"))
        document.body.getElementById("uk-address").text() shouldBe Messages("common.address.findUKAddress")
        document.body.getElementById("description-one").text() shouldEqual Messages("page.companyDetails.RegisteredAddress.description.one")
        document.body.getElementById("next").text() shouldEqual Messages("common.button.continue")
        document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")

    }

    "Verify that Registered Address page contains the correct elements " +
      "when no data is returned from keystore" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        val result = controller.show.apply(SessionBuilder.buildRequestWithSession(userId))
        Jsoup.parse(contentAsString(result))
      }

        document.title shouldBe Messages("page.companyDetails.RegisteredAddress.title")
        document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.RegisteredAddress.heading")
        document.body.getElementById("back-link").attr("href") shouldEqual routes.TaxpayerReferenceController.show().toString()
        document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
        document.body.getElementById("label-postcode").text() should include (Messages("common.address.postcode"))
        document.body.getElementById("uk-address").text() shouldBe Messages("common.address.findUKAddress")
        document.body.getElementById("description-one").text() shouldEqual Messages("page.companyDetails.RegisteredAddress.description.one")
        document.body.getElementById("next").text() shouldEqual Messages("common.button.continue")
        document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")

    }

    "Verify that the contact address contains the correct elements and error when an invalid model is posted" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(registeredAddressModel)))
        val result = controller.submit.apply(fakeRequestWithSession.withFormUrlEncodedBody(
          "postcode" -> ""
        ))
        Jsoup.parse(contentAsString(result))
      }
      document.title shouldBe Messages("page.companyDetails.RegisteredAddress.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.RegisteredAddress.heading")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.TaxpayerReferenceController.show().toString()
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.one")
      document.body.getElementById("label-postcode").text() should include (Messages("common.address.postcode"))
      document.body.getElementById("uk-address").text() shouldBe Messages("common.address.findUKAddress")
      document.body.getElementById("description-one").text() shouldEqual Messages("page.companyDetails.RegisteredAddress.description.one")
      document.body.getElementById("next").text() shouldEqual Messages("common.button.continue")
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.getElementById("error-summary-heading").text() shouldBe Messages("common.error.summary.heading")
    }

  }
}
