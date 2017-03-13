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

package views.eisseis

import auth.{MockConfig, MockAuthConnector}
import common.KeystoreKeys
import config.FrontendAppConfig
import controllers.eisseis.ContactDetailsController
import controllers.routes
import models.ContactDetailsModel
import models.submission.SchemeTypesModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._
import play.api.test.Helpers._
import views.helpers.ViewSpec

import scala.concurrent.Future

class ContactDetailsSpec extends ViewSpec {
  
  object TestController extends ContactDetailsController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(contactDetailsModel: Option[ContactDetailsModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.manualContactDetails))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(contactDetailsModel))

    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(eisSeisSchemeTypesModel))
  }

  "The Contact Details page" should {

    "Verify that the contact details page contains the correct elements when a valid ContactDetailsModel is passed" in new Setup {
      val document: Document = {
        setupMocks(Some(contactDetailsModel))
        val result = TestController.show.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.contactInformation.contactDetails.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.contactDetails.heading")
      document.getElementById("label-forename").text() shouldBe Messages("page.contactInformation.contactDetails.forename.label")
      document.getElementById("label-surname").text() shouldBe Messages("page.contactInformation.contactDetails.surname.label")
      document.getElementById("label-telephoneNumber").text() shouldBe Messages("page.contactInformation.contactDetails.phoneNumber.label")
      document.getElementById("label-mobileNumber").text() shouldBe Messages("page.contactInformation.contactDetails.mobileNumber.label")
      document.getElementById("label-email").text() shouldBe Messages("page.contactInformation.contactDetails.email.label")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.ConfirmContactDetailsController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.four")
    }

    "Verify that the proposed investment page contains the correct elements when an invalid ContactDetailsModel is passed" in new Setup {
      val document: Document = {
        setupMocks()
        val result = TestController.submit.apply(authorisedFakeRequest)
        Jsoup.parse(contentAsString(result))
      }
      document.title() shouldBe Messages("page.contactInformation.contactDetails.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.contactInformation.contactDetails.heading")
      document.getElementById("label-forename").text() contains Messages("page.contactInformation.contactDetails.forename.label")
      document.getElementById("label-surname").text() contains Messages("page.contactInformation.contactDetails.surname.label")
      document.getElementById("label-telephoneNumber").text() contains Messages("page.contactInformation.contactDetails.phoneNumber.label")
      document.getElementById("label-mobileNumber").text() shouldBe Messages("page.contactInformation.contactDetails.mobileNumber.label")
      document.getElementById("label-email").text() contains Messages("page.contactInformation.contactDetails.email.label")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual controllers.eisseis.routes.ConfirmContactDetailsController.show().url
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.four")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

  }

}
