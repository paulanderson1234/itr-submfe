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
import connectors.{EnrolmentConnector, KeystoreConnector}
import controllers.helpers.FakeRequestHelper
import controllers.{ConfirmCorrespondAddressController, routes}
import models.{AddressModel, ConfirmCorrespondAddressModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import controllers.ConfirmCorrespondAddressController
import utils.CountriesHelper

import scala.concurrent.Future

class ConfirmCorrespondAddressSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  val mockKeystoreConnector = mock[KeystoreConnector]

  val address = AddressModel("Line 1", "Line 2", Some("Line 3"), Some("Line 4"), Some("TF1 5NY"), "GB")
  val confirmCorrespondAddressModel = new ConfirmCorrespondAddressModel(Constants.StandardRadioButtonYesValue, address)
  val emptyConfirmCorrespondAddressModel = new ConfirmCorrespondAddressModel("", address)

  //TODO: Mock this return when it is obtained from ETMP
  val addressFromEtmp =  AddressModel("Company Name Ltd.", "2 Telford Plaza", Some("Lawn Central"), Some("Telford"), Some("TF3 4NT"))

  class SetupPage {

    val controller = new ConfirmCorrespondAddressController{
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

    when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))
  }

  "The Confirm Correspondence Address page" should {

    "Verify that the Confirm Correspondence Address page contains the correct elements when a valid ConfirmCorrespondAddressModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel]
          (Matchers.eq(KeystoreKeys.confirmContactAddress))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(confirmCorrespondAddressModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[AddressModel]
          (Matchers.eq(KeystoreKeys.contactAddress))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(address)))

        val result = controller.show.apply(authorisedFakeRequest.withFormUrlEncodedBody(
          "contactAddressUse" -> Constants.StandardRadioButtonYesValue
        ))
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.contactInformation.ConfirmCorrespondAddress.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.ConfirmCorrespondAddress.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ContactDetailsController.show().toString()
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.four")
      document.body.getElementById("contactAddressUse-yesLabel").text shouldBe  Messages("common.radioYesLabel")
      document.body.getElementById("contactAddressUse-noLabel").text shouldBe  Messages("common.radioNoLabel")
      document.body.select("#contactAddressUse-yes").size() shouldBe 1
      document.body.select("#contactAddressUse-no").size() shouldBe 1
      document.body.getElementById("storedAddressDiv")
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.body.getElementById("line1-display").text shouldBe address.addressline1
      document.body.getElementById("line2-display").text shouldBe address.addressline2
      document.body.getElementById("line3-display").text shouldBe address.addressline3.getOrElse("")
      document.body.getElementById("line4-display").text shouldBe address.addressline4.getOrElse("")
      document.body.getElementById("postcode-display").text shouldBe address.postcode.getOrElse("")
      document.body.getElementById("country-display").text shouldBe utils.CountriesHelper.getSelectedCountry(address.countryCode)
      }

    "Verify that the Confirm Correspondence Address page contains the correct elements when a valid" +
      "ConfirmCorrespondAddressModel is passed but no address model is returned so the ETMP address is used" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel]
          (Matchers.eq(KeystoreKeys.confirmContactAddress))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(confirmCorrespondAddressModel)))
        when(mockKeystoreConnector.fetchAndGetFormData[AddressModel]
          (Matchers.eq(KeystoreKeys.contactAddress))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))

        val result = controller.show.apply(authorisedFakeRequest.withFormUrlEncodedBody(
          "contactAddressUse" -> Constants.StandardRadioButtonYesValue
        ))
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.contactInformation.ConfirmCorrespondAddress.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.ConfirmCorrespondAddress.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ContactDetailsController.show().toString()
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.four")
      document.body.getElementById("contactAddressUse-yesLabel").text shouldBe  Messages("common.radioYesLabel")
      document.body.getElementById("contactAddressUse-noLabel").text shouldBe  Messages("common.radioNoLabel")
      document.body.select("#contactAddressUse-yes").size() shouldBe 1
      document.body.select("#contactAddressUse-no").size() shouldBe 1
      document.body.getElementById("storedAddressDiv")
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.body.getElementById("line1-display").text shouldBe addressFromEtmp.addressline1
      document.body.getElementById("line2-display").text shouldBe addressFromEtmp.addressline2
      document.body.getElementById("line3-display").text shouldBe addressFromEtmp.addressline3.getOrElse("")
      document.body.getElementById("line4-display").text shouldBe addressFromEtmp.addressline4.getOrElse("")
      document.body.getElementById("postcode-display").text shouldBe addressFromEtmp.postcode.getOrElse("")
      document.body.getElementById("country-display").text shouldBe utils.CountriesHelper.getSelectedCountry(addressFromEtmp.countryCode)
    }

    "Verify that the Confirm Correspondence Address page contains the correct elements " +
      "when an invalid ConfirmCorrespondAddressModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(confirmCorrespondAddressModel)))
        val result = controller.submit.apply(authorisedFakeRequest.withFormUrlEncodedBody(
          "contactAddressUse" -> "",
          "address.addressline1" -> "LineX 1 Posted",
          "address.addressline2" -> "LineX 2 Posted",
          "address.addressline3" -> "LineX 3 Posted",
          "address.addressline4" -> "LineX 4 Posted",
          "address.postcode" -> "TXX 3XX",
          "address.countryCode" -> "GB"
        ))
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.contactInformation.ConfirmCorrespondAddress.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.ConfirmCorrespondAddress.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.ContactDetailsController.show().toString()
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.four")
      document.body.getElementById("contactAddressUse-yesLabel").text shouldBe  Messages("common.radioYesLabel")
      document.body.getElementById("contactAddressUse-noLabel").text shouldBe  Messages("common.radioNoLabel")
      document.body.select("#contactAddressUse-yes").size() shouldBe 1
      document.body.select("#contactAddressUse-no").size() shouldBe 1
      document.body.getElementById("storedAddressDiv")
      document.body.getElementById("get-help-action").text shouldBe  Messages("common.error.help.text")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.body.getElementById("line1-display").text shouldBe "LineX 1 Posted"
      document.body.getElementById("line2-display").text shouldBe "LineX 2 Posted"

      document.body.getElementById("line3-display").text shouldBe "LineX 3 Posted"
      document.body.getElementById("line4-display").text shouldBe "LineX 4 Posted"
      document.body.getElementById("postcode-display").text shouldBe "TXX 3XX"
      document.body.getElementById("country-display").text shouldBe utils.CountriesHelper.getSelectedCountry(addressFromEtmp.countryCode)
    }
  }
}
