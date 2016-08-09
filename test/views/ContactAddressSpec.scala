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
import controllers.{routes, ContactAddressController}
import controllers.helpers.FakeRequestHelper
import models.ContactAddressModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class ContactAddressSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockKeystoreConnector = mock[KeystoreConnector]

  val contactAddressModel = new ContactAddressModel("ST1 1QQ")
  val emptyContactAddressModel = new ContactAddressModel("")

  class SetupPage {

    val controller = new ContactAddressController {
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  "The Contact Address page" should {

    "Verify that Contact Address page contains the correct elements " +
      "when a valid ContactAddressModel is passed as returned from keystore" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[ContactAddressModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(contactAddressModel)))
        val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody(
          "postcode" -> "ST1 1QQ"
        ))
        Jsoup.parse(contentAsString(result))
      }

      document.title shouldBe Messages("page.contactInformation.ContactAddress.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.ContactAddress.heading")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ConfirmCorrespondAddressController.show().toString()
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.four")
      document.body.getElementById("label-postcode").text() should include(Messages("common.address.postcode"))
      document.body.getElementById("uk-address").text() shouldBe Messages("common.address.findUKAddress")
      document.body.getElementById("description-one").text() shouldBe Messages("page.contactInformation.ContactAddress.description.one")
      document.body.getElementById("next").text() shouldEqual Messages("common.button.continueNextSection")
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")

    }

    "Verify that the Contact Address page contains the correct elements " +
      "when an invalid ContactAddressModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[ContactAddressModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(contactAddressModel)))
        val result = controller.submit.apply(fakeRequestWithSession.withFormUrlEncodedBody(
          "postcode" -> ""
        ))
        Jsoup.parse(contentAsString(result))
      }

      document.title shouldBe Messages("page.contactInformation.ContactAddress.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.ContactAddress.heading")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ConfirmCorrespondAddressController.show().toString()
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.four")
      document.body.getElementById("label-postcode").text() should include(Messages("common.address.postcode"))
      document.body.getElementById("uk-address").text() shouldBe Messages("common.address.findUKAddress")
      document.body.getElementById("description-one").text() shouldBe Messages("page.contactInformation.ContactAddress.description.one")
      document.body.getElementById("next").text() shouldEqual Messages("common.button.continueNextSection")
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.getElementById("error-summary-heading").text() shouldBe Messages("common.error.summary.heading")

    }
  }
}
