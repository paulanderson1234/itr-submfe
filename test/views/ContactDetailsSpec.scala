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
import controllers.examples.{ContactDetailsController, routes}
import controllers.helpers.FakeRequestHelper
import models.ContactDetailsModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class ContactDetailsSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  val mockKeystoreConnector = mock[KeystoreConnector]

  val contactDetailsModel = new ContactDetailsModel("Harry", "Gull", "hgull@nothotmail.com")
  val emptyContactDetailsModel = new ContactDetailsModel("", "", "")

  class SetupPage {

    val controller = new ContactDetailsController{
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  "The Contact Details page" should {

    "Verify that contact details page contains the correct elements when a valid ContactDetailsModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(contactDetailsModel)))
        val result = controller.show.apply((fakeRequestWithSession))
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.examples.ContactDetails.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.examples.ContactDetails.heading")
      document.getElementById("heading-description").text() shouldBe Messages("page.examples.ContactDetails.description")
      document.getElementById("label-forename").text() shouldBe Messages("page.examples.ContactDetails.forename.label")
      document.getElementById("label-surname").text() shouldBe Messages("page.examples.ContactDetails.surname.label")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.CompanyAddressController.show.toString()
    }

    "Verify that contact details page contains the correct elements when an invalid ContactDetailsModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(emptyContactDetailsModel)))
        val result = controller.show.apply((fakeRequestWithSession))
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.examples.ContactDetails.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.examples.ContactDetails.heading")
      document.getElementById("heading-description").text() shouldBe Messages("page.examples.ContactDetails.description")
      document.getElementById("label-forename").text() shouldBe Messages("page.examples.ContactDetails.forename.label")
      document.getElementById("label-surname").text() shouldBe Messages("page.examples.ContactDetails.surname.label")
      document.getElementById("next").text() shouldBe Messages("common.button.snc")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.CompanyAddressController.show.toString()
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

  }

  //  "Verify that AgentView page contains the correct elements when an empty ContactDetailsModel is passed" in new SetupPage {
//    val document : Document = {
//      val userId = s"user-${UUID.randomUUID}"
//      AuthBuilder.mockAuthorisedUser(userId, mockAuthConnector)
//      when(mockS4LConnector.fetchAndGet[ContactDetailsModel](Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any()))
//        .thenReturn(Future.successful(Option(emptyContactDetailsModel)))
//      val result = controller.show.apply(SessionBuilder.buildRequestWithSession(userId))
//      Jsoup.parse(contentAsString(result))
//    }
//
//    document.title() shouldBe Messages("page.reg.companyDetails.title")
//    document.getElementById("main-heading").text() shouldBe Messages("page.reg.companyDetails.heading")
//    document.getElementById("takeoverBusinessLabel").text() shouldBe Messages("page.reg.companyDetails.takeover.label")
//    document.select("#takeover-yes").size() shouldBe 1
//    document.select("#takeover-no").size() shouldBe 1
//    document.getElementById("companyMemberGroupLabel").text() shouldBe Messages("page.reg.companyDetails.group.label")
//    document.select("#group-yes").size() shouldBe 1
//    document.select("#group-no").size() shouldBe 1
//    document.getElementById("companyCharityLabel").text() shouldBe Messages("page.reg.companyDetails.charity.label")
//    document.select("#charity-yes").size() shouldBe 1
//    document.select("#charity-no").size() shouldBe 1
//    document.getElementById("next").text() shouldBe Messages("common.button.save")
//  }
}
