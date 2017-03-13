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
import common.{Constants, KeystoreKeys}
import config.FrontendAppConfig
import controllers.eis.ConfirmCorrespondAddressController
import data.SubscriptionTestData._
import controllers.helpers.BaseSpec
import models.{AddressModel, ConfirmCorrespondAddressModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import services.SubscriptionService
import views.helpers.ViewSpec

import scala.concurrent.Future

class ConfirmCorrespondAddressSpec extends ViewSpec with BaseSpec {

  object TestController extends ConfirmCorrespondAddressController {
    override lazy val subscriptionService = mock[SubscriptionService]
    override lazy val applicationConfig = MockConfigEISFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupSaveForLaterMocks
  (
    confirmCorrespondAddressModel: Option[ConfirmCorrespondAddressModel] = None,
    backLink: Option[String] = None
  ): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](Matchers.eq(KeystoreKeys.confirmContactAddress))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(confirmCorrespondAddressModel))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkConfirmCorrespondence))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(backLink))
  }

  def mockSubscriptionServiceResponse(address: Option[AddressModel] = None): Unit =
    when(TestController.subscriptionService.getSubscriptionContactAddress(Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(address))


  "The Confirm Correspondence Address page" should {

    "Verify that the Confirm Correspondence Address page contains the correct elements when a valid ConfirmCorrespondAddressModel is passed" +
      "and there is no data already stored so it uses the ETMP address" in new Setup {
      val document: Document = {
        setupSaveForLaterMocks(None,Some("backLink"))
        mockSubscriptionServiceResponse(Some(expectedContactAddressFull))
        val result = TestController.show.apply(authorisedFakeRequest.withFormUrlEncodedBody(
          "contactAddressUse" -> Constants.StandardRadioButtonYesValue
        ))
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.contactInformation.ConfirmCorrespondAddress.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.ConfirmCorrespondAddress.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual "backLink"
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.four")
      document.body.getElementById("contactAddressUse-yesLabel").text shouldBe  Messages("common.radioYesLabel")
      document.body.getElementById("contactAddressUse-noLabel").text shouldBe  Messages("common.radioNoLabel")
      document.body.select("#contactAddressUse-yes").size() shouldBe 1
      document.body.select("#contactAddressUse-no").size() shouldBe 1
      document.body.getElementById("storedAddressDiv")
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.body.getElementById("line1-display").text shouldBe expectedContactAddressFull.addressline1
      document.body.getElementById("line2-display").text shouldBe expectedContactAddressFull.addressline2
      document.body.getElementById("line3-display").text shouldBe expectedContactAddressFull.addressline3.getOrElse("")
      document.body.getElementById("line4-display").text shouldBe expectedContactAddressFull.addressline4.getOrElse("")
      document.body.getElementById("postcode-display").text shouldBe expectedContactAddressFull.postcode.getOrElse("")
      document.body.getElementById("country-display").text shouldBe utils.CountriesHelper.getSelectedCountry(expectedContactAddressFull.countryCode)
      }

    "Verify that the Confirm Correspondence Address page contains the correct elements when a valid" +
      "and there is data stored for the confirmCorrespondenceAddress" in new Setup {
      val document: Document = {
        setupSaveForLaterMocks(Some(confirmCorrespondAddressModel),Some("backLink"))
        val result = TestController.show.apply(authorisedFakeRequest.withFormUrlEncodedBody(
          "contactAddressUse" -> Constants.StandardRadioButtonYesValue
        ))
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.contactInformation.ConfirmCorrespondAddress.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.ConfirmCorrespondAddress.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual "backLink"
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.four")
      document.body.getElementById("contactAddressUse-yesLabel").text shouldBe  Messages("common.radioYesLabel")
      document.body.getElementById("contactAddressUse-noLabel").text shouldBe  Messages("common.radioNoLabel")
      document.body.select("#contactAddressUse-yes").size() shouldBe 1
      document.body.select("#contactAddressUse-no").size() shouldBe 1
      document.body.getElementById("storedAddressDiv")
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.body.getElementById("line1-display").text shouldBe confirmCorrespondAddressModel.address.addressline1
      document.body.getElementById("line2-display").text shouldBe confirmCorrespondAddressModel.address.addressline2
      document.body.getElementById("line3-display").text shouldBe confirmCorrespondAddressModel.address.addressline3.getOrElse("")
      document.body.getElementById("line4-display").text shouldBe confirmCorrespondAddressModel.address.addressline4.getOrElse("")
      document.body.getElementById("postcode-display").text shouldBe confirmCorrespondAddressModel.address.postcode.getOrElse("")
      document.body.getElementById("country-display").text shouldBe utils.CountriesHelper.getSelectedCountry(confirmCorrespondAddressModel.address.countryCode)
    }

    "Verify that the Confirm Correspondence Address page contains the correct elements " +
      "when an invalid ConfirmCorrespondAddressModel is passed" in new Setup {
      val document: Document = {
        setupSaveForLaterMocks(Some(confirmCorrespondAddressModel),Some("backLink"))
        val result = TestController.submit.apply(authorisedFakeRequest.withFormUrlEncodedBody(
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
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual "backLink"
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
      document.body.getElementById("country-display").text shouldBe utils.CountriesHelper.getSelectedCountry(expectedContactAddressFull.countryCode)
    }
  }
}
