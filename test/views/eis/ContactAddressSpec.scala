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

import forms.ContactAddressForm._
import models.AddressModel
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.eis.contactInformation.ContactAddress

class ContactAddressSpec extends ViewSpec {

  val emptyAddressModel = new AddressModel("", "", countryCode = "")

  lazy val form = contactAddressForm.bind(Map("addressline1" -> "ABC XYZ",
    "addressline2" -> "1 ABCDE Street",
    "addressline3" -> "",
    "addressline4" -> "",
    "postcode" -> "",
    "countryCode" -> "JP"))

  lazy val emptyForm = contactAddressForm.bind(Map("addressline1" -> "",
    "addressline2" -> "",
    "addressline3" -> "",
    "addressline4" -> "",
    "postcode" -> "",
    "countryCode" -> ""))

  lazy val errorForm = contactAddressForm.bind(Map("addressline1" -> "ABC XYZ",
    "addressline2" -> "1 ABCDE Street",
    "addressline3" -> "",
    "addressline4" -> "",
    "postcode" -> "",
    "countryCode" -> ""))

  val countriesList: List[(String, String)] = List(("JP", "Japan"), ("GB", "United Kingdom"))
  lazy val page = ContactAddress(form, countriesList)(authorisedFakeRequest,applicationMessages)
  lazy val emptyPage = ContactAddress(emptyForm, countriesList)(authorisedFakeRequest, applicationMessages)
  lazy val errorPage = ContactAddress(errorForm, countriesList)(authorisedFakeRequest, applicationMessages)

  "The Provide Correspondence Address page" should {

    "Verify that the Provide Correspondence Address page contains the correct elements when a valid AddressModel is passed" in {

      lazy val document = {
        Jsoup.parse(contentAsString(page))
      }

      document.title() shouldBe Messages("page.contactInformation.ProvideContactAddress.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.ProvideContactAddress.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.ConfirmCorrespondAddressController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.four")
      document.body.getElementById("addressline1").`val`() shouldBe contactAddressModel.addressline1
      document.body.getElementById("addressline2").`val`() shouldBe contactAddressModel.addressline2
      document.body.getElementById("addressline3").`val`() shouldBe ""
      document.body.getElementById("addressline4").`val`() shouldBe ""
      document.body.getElementById("postcode").`val`() shouldBe ""
      document.body.select("select[name=countryCode] option[selected]").`val`() shouldBe contactAddressModel.countryCode
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
    }

    "Verify that the Provide Correspondence Address page contains the correct elements " +
      "when an empty AddressModel is passed" in {

      lazy val document = {
        Jsoup.parse(contentAsString(emptyPage))
      }

      document.title() shouldBe Messages("page.contactInformation.ProvideContactAddress.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.ProvideContactAddress.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.ConfirmCorrespondAddressController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.four")
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.getElementById("countryCode-error-summary").text should include(Messages("validation.error.countryCode"))
      document.getElementById("addressline1-error-summary").text should include(Messages("validation.error.mandatoryaddresssline"))
      document.getElementById("addressline2-error-summary").text should include(Messages("validation.error.mandatoryaddresssline"))
    }

    "Verify that the Provide Correspondence Address page contains the correct elements " +
      "when an invalid AddressModel is passed" in {

      lazy val document = {
        Jsoup.parse(contentAsString(errorPage))
      }
      document.title() shouldBe Messages("page.contactInformation.ProvideContactAddress.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.ProvideContactAddress.heading")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.ConfirmCorrespondAddressController.show().url
      document.body.getElementById("progress-section").text shouldBe Messages("common.section.progress.company.details.four")
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
      document.getElementById("countryCode-error-summary").text should include(Messages("validation.error.countryCode"))
    }
  }
}
