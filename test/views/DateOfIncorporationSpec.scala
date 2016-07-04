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
import controllers.{DateOfIncorporationController, routes}
import controllers.helpers.FakeRequestHelper
import models.DateOfIncorporationModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class DateOfIncorporationSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  val mockKeystoreConnector = mock[KeystoreConnector]

  val contactDetailsModel = new DateOfIncorporationModel(23,11,1993)
  val emptyDateOfIncorporationModel = new DateOfIncorporationModel(0,0,0)

  class SetupPage {

    val controller = new DateOfIncorporationController{
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  "The Date Of Incorporation page" should {

    "Verify that date of incorporation page contains the correct elements when a valid DateOfIncorporationModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(contactDetailsModel)))
        val result = controller.show.apply((fakeRequestWithSession))
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.companyDetails.DateOfIncorporation.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.DateOfIncorporation.heading")
      document.body.getElementsByClass("form-hint").text should include(Messages("date.hint.dateOfIncorporation"))
      document.body.getElementById("incorporationDay").parent.text shouldBe Messages("common.date.fields.day")
      document.body.getElementById("incorporationMonth").parent.text shouldBe Messages("common.date.fields.month")
      document.body.getElementById("incorporationYear").parent.text shouldBe Messages("common.date.fields.year")
      document.body.getElementById("date-of-incorporation-where-to-find").parent.text should include
      (Messages("page.companyDetails.DateOfIncorporation.location"))
      document.body.getElementById("company-house-db").text() shouldEqual Messages("page.companyDetails.DateOfIncorporation.companiesHouse")
      document.body.getElementById("company-house-db").attr("href") shouldEqual Messages("page.companyDetails.DateOfIncorporation.companiesHouse.link")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.DateOfIncorporationController.show.toString()
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.one")
    }

    "Verify that the date of incorporation page contains the correct elements when an invalid DateOfIncorporationModel is passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(emptyDateOfIncorporationModel)))
        val result = controller.submit.apply((fakeRequestWithSession))
        Jsoup.parse(contentAsString(result))
      }

      document.title() shouldBe Messages("page.companyDetails.DateOfIncorporation.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.DateOfIncorporation.heading")
      document.body.getElementsByClass("form-hint").text should include(Messages("date.hint.dateOfIncorporation"))
      document.body.getElementById("incorporationDay").parent.text shouldBe Messages("common.date.fields.day")
      document.body.getElementById("incorporationMonth").parent.text shouldBe Messages("common.date.fields.month")
      document.body.getElementById("incorporationYear").parent.text shouldBe Messages("common.date.fields.year")
      document.body.getElementById("date-of-incorporation-where-to-find").parent.text should include
      (Messages("page.companyDetails.DateOfIncorporation.location"))
      document.body.getElementById("company-house-db").text() shouldEqual Messages("page.companyDetails.DateOfIncorporation.companiesHouse")
      document.body.getElementById("company-house-db").attr("href") shouldEqual Messages("page.companyDetails.DateOfIncorporation.companiesHouse.link")
      document.getElementById("next").text() shouldBe Messages("common.button.continue")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.DateOfIncorporationController.show.toString()
      document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.one")
      document.getElementById("error-summary-display").hasClass("error-summary--show")
    }

  }

}