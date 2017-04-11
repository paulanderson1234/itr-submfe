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

package views

import auth.MockConfigUploadFeature
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import utils.CountriesHelper
import views.helpers.ViewSpec
import views.html.hubPartials.{ApplicationHubExisting, ApplicationHubNew}
import views.html.introduction.ApplicationHub

class ApplicationHubSpec extends ViewSpec {

  val continueUrl = "/seis/natureOfbusiness"
  val schemeType = "SEED Enterprise Investment Scheme - Advanced Assurance"

  "The Application Hub page" should {

    "Verify that hub page contains the correct elements when a 'hub new' partial is passed to it and " +
      "the model used contains the max fields" in {
      lazy val view = ApplicationHub(applicationHubModelMax, ApplicationHubNew()(fakeRequest, applicationMessages))(fakeRequest, applicationMessages)
      val document = Jsoup.parse(view.body)
      document.title shouldEqual Messages("page.introduction.hub.title")
      document.body.getElementsByTag("h1").text() shouldEqual Messages("page.introduction.hub.heading")
      //organisation name
      document.body.getElementById("organisation-name").text() shouldEqual applicationHubModelMax.organisationName
      //registered address
      document.body.getElementById("address-line0").text() shouldBe applicationHubModelMax.registeredAddress.addressline1
      document.body.getElementById("address-line1").text() shouldBe applicationHubModelMax.registeredAddress.addressline2
      document.body.getElementById("address-line2").text() shouldBe applicationHubModelMax.registeredAddress.addressline3.get
      document.body.getElementById("address-line3").text() shouldBe applicationHubModelMax.registeredAddress.addressline4.get
      document.body.getElementById("address-line4").text() shouldBe applicationHubModelMax.registeredAddress.postcode.get
      document.body.getElementById("address-line5").text() shouldBe CountriesHelper.getSelectedCountry(applicationHubModelMax.registeredAddress.countryCode)
      //contact details
      document.body.getElementById("contactDetails-line0").text() shouldBe applicationHubModelMax.contactDetails.fullName
      document.body.getElementById("contactDetails-line1").text() shouldBe applicationHubModelMax.contactDetails.telephoneNumber.get
      document.body.getElementById("contactDetails-line2").text() shouldBe applicationHubModelMax.contactDetails.mobileNumber.get
      document.body.getElementById("contactDetails-line3").text() shouldBe applicationHubModelMax.contactDetails.email
      //attachments outside
      if (MockConfigUploadFeature.uploadFeatureEnabled){
        document.body.getElementById("attachments-outside-heading").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.heading")
        document.body.getElementById("attachments-outside-desc").text() shouldBe
          Messages("page.introduction.hub.upload.link") + " " + Messages("page.introduction.hub.upload.desc")
        document.body.getElementById("attachments-outside-link").text() shouldBe
          Messages("page.introduction.hub.upload.link")
      }
    }

    "Verify that hub page contains the correct elements when a 'hub existing' partial is passed to it and" +
      "the model used contains the max amount of fields" in {
      lazy val view = ApplicationHub(applicationHubModelMax, ApplicationHubExisting(continueUrl, schemeType)(fakeRequest, applicationMessages))(fakeRequest, applicationMessages)
      val document = Jsoup.parse(view.body)
      document.title shouldEqual Messages("page.introduction.hub.title")
      document.body.getElementsByTag("h1").text() shouldEqual Messages("page.introduction.hub.heading")
      //organisation name
      document.body.getElementById("organisation-name").text() shouldEqual applicationHubModelMax.organisationName
      //registered address
      document.body.getElementById("address-line0").text() shouldBe applicationHubModelMax.registeredAddress.addressline1
      document.body.getElementById("address-line1").text() shouldBe applicationHubModelMax.registeredAddress.addressline2
      document.body.getElementById("address-line2").text() shouldBe applicationHubModelMax.registeredAddress.addressline3.get
      document.body.getElementById("address-line3").text() shouldBe applicationHubModelMax.registeredAddress.addressline4.get
      document.body.getElementById("address-line4").text() shouldBe applicationHubModelMax.registeredAddress.postcode.get
      document.body.getElementById("address-line5").text() shouldBe CountriesHelper.getSelectedCountry(applicationHubModelMax.registeredAddress.countryCode)
      //contact details
      document.body.getElementById("contactDetails-line0").text() shouldBe applicationHubModelMax.contactDetails.fullName
      document.body.getElementById("contactDetails-line1").text() shouldBe applicationHubModelMax.contactDetails.telephoneNumber.get
      document.body.getElementById("contactDetails-line2").text() shouldBe applicationHubModelMax.contactDetails.mobileNumber.get
      document.body.getElementById("contactDetails-line3").text() shouldBe applicationHubModelMax.contactDetails.email
      //attachments outside
      if (MockConfigUploadFeature.uploadFeatureEnabled) {
        document.body.getElementById("attachments-outside-heading").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.heading")
        document.body.getElementById("attachments-outside-desc").text() shouldBe
          Messages("page.introduction.hub.upload.link") + " " + Messages("page.introduction.hub.upload.desc")
        document.body.getElementById("attachments-outside-link").text() shouldBe
          Messages("page.introduction.hub.upload.link")
      }
    }

    "Verify that hub page contains the correct elements when a 'hub new' partial is passed to it and" +
      "the model used contains the min amount of fields" in {
      lazy val view = ApplicationHub(applicationHubModelMin, ApplicationHubNew()(fakeRequest, applicationMessages))(fakeRequest, applicationMessages)
      val document = Jsoup.parse(view.body)
      document.title shouldEqual Messages("page.introduction.hub.title")
      document.body.getElementsByTag("h1").text() shouldEqual Messages("page.introduction.hub.heading")
      //organisation name
      document.body.getElementById("organisation-name").text() shouldEqual applicationHubModelMin.organisationName
      //registered address
      document.body.getElementById("address-line0").text() shouldBe applicationHubModelMin.registeredAddress.addressline1
      document.body.getElementById("address-line1").text() shouldBe applicationHubModelMin.registeredAddress.addressline2
      document.body.getElementById("address-line2").text() shouldBe CountriesHelper.getSelectedCountry(applicationHubModelMin.registeredAddress.countryCode)
      //contact details
      document.body.getElementById("contactDetails-line0").text() shouldBe applicationHubModelMin.contactDetails.fullName
      document.body.getElementById("contactDetails-line1").text() shouldBe applicationHubModelMin.contactDetails.email
      //attachments outside
      if (MockConfigUploadFeature.uploadFeatureEnabled) {
        document.body.getElementById("attachments-outside-heading").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.heading")
        document.body.getElementById("attachments-outside-desc").text() shouldBe
          Messages("page.introduction.hub.upload.link") + " " + Messages("page.introduction.hub.upload.desc")
        document.body.getElementById("attachments-outside-link").text() shouldBe
          Messages("page.introduction.hub.upload.link")
      }
    }

    "Verify that hub page contains the correct elements when a 'hub existing' partial is passed to it and" +
      "the model used contains the min amount of fields" in {
      lazy val view = ApplicationHub(applicationHubModelMin, ApplicationHubExisting(continueUrl, schemeType)(fakeRequest, applicationMessages))(fakeRequest, applicationMessages)
      val document = Jsoup.parse(view.body)
      document.title shouldEqual Messages("page.introduction.hub.title")
      document.body.getElementsByTag("h1").text() shouldEqual Messages("page.introduction.hub.heading")
      //organisation name
      document.body.getElementById("organisation-name").text() shouldEqual applicationHubModelMin.organisationName
      //registered address
      document.body.getElementById("address-line0").text() shouldBe applicationHubModelMin.registeredAddress.addressline1
      document.body.getElementById("address-line1").text() shouldBe applicationHubModelMin.registeredAddress.addressline2
      document.body.getElementById("address-line2").text() shouldBe CountriesHelper.getSelectedCountry(applicationHubModelMin.registeredAddress.countryCode)
      //contact details
      document.body.getElementById("contactDetails-line0").text() shouldBe applicationHubModelMin.contactDetails.fullName
      document.body.getElementById("contactDetails-line1").text() shouldBe applicationHubModelMin.contactDetails.email
      //attachments outside
      if (MockConfigUploadFeature.uploadFeatureEnabled) {
        document.body.getElementById("attachments-outside-heading").text() shouldBe Messages("page.supportingDocuments.SupportingDocuments.heading")
        document.body.getElementById("attachments-outside-desc").text() shouldBe
          Messages("page.introduction.hub.upload.link") + " " + Messages("page.introduction.hub.upload.desc")
        document.body.getElementById("attachments-outside-link").text() shouldBe
          Messages("page.introduction.hub.upload.link")
      }
    }
  }
}
