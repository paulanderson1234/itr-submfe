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
import controllers.{ReviewPreviousSchemesController, routes}
import models.{PreviousSchemeModel}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class ReviewPreviousSchemesSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper {

  val mockKeystoreConnector = mock[KeystoreConnector]

  val previousSchemesList = Vector(PreviousSchemeModel("Enterprise Investment Scheme",23000,None,None,Some(23),Some(11),Some(1993),Some(1)),
    PreviousSchemeModel("Enterprise Investment Scheme",1101,None,None,Some(9),Some(11),Some(2015),Some(2)))
  val emptyPreviousSchemesList  = Vector[PreviousSchemeModel]()


  class SetupPage {

    val controller = new ReviewPreviousSchemesController {
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  "The Review Previous Schemes Spec page" should {

    "Verify that Review Previous Schemes page contains the correct table rows and data " +
      "when a valid vector of PreviousSchemeModels are passed as returned from keystore" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"
        when(mockKeystoreConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.any())(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Option(previousSchemesList)))
        val result = controller.show.apply(SessionBuilder.buildRequestWithSession(userId))
        Jsoup.parse(contentAsString(result))
      }

      document.title shouldBe Messages("page.previousInvestment.reviewPreviousSchemes.title")
      document.getElementById("main-heading").text() shouldBe Messages("page.previousInvestment.reviewPreviousSchemes.heading")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.PreviousSchemeController.show().toString()
      document.body.getElementById("progress-section").text shouldBe Messages("summaryQuestion.previousRFISection")

      lazy val reviewSchemesTableHead = document.getElementById("previous-schemes-table").select("thead")
      lazy val reviewSchemesTableBody = document.getElementById("previous-schemes-table").select("tbody")
      //head
      reviewSchemesTableHead.select("tr").get(0).getElementById("scheme-table-heading").text() shouldBe
        Messages("page.previousInvestment.reviewPreviousSchemes.scheme")
      reviewSchemesTableHead.select("tr").get(0).getElementById("date-table-heading").text() shouldBe
        Messages("page.previousInvestment.reviewPreviousSchemes.dateOfShareIssue")
      reviewSchemesTableHead.select("tr").get(0).getElementById("amount-table-heading").text() shouldBe
        Messages("page.previousInvestment.reviewPreviousSchemes.investmentAmount")
      //body
      for((previousScheme, index) <- previousSchemesList.zipWithIndex) {
        reviewSchemesTableBody.select("tr").get(index).getElementById(s"scheme-type-$index").text() shouldBe
          previousScheme.schemeTypeDesc
        reviewSchemesTableBody.select("tr").get(index).getElementById(s"scheme-date-$index").text() shouldBe
          PreviousSchemeModel.toDateString(previousScheme.day.get, previousScheme.month.get, previousScheme.year.get)
        reviewSchemesTableBody.select("tr").get(index).getElementById(s"scheme-amount-$index").text() shouldBe
          PreviousSchemeModel.getAmountAsFormattedString(previousScheme.investmentAmount)
        reviewSchemesTableBody.select("tr").get(index).getElementById(s"change-$index").text() shouldBe
          Messages("common.base.change")
        reviewSchemesTableBody.select("tr").get(index).getElementById(s"change-$index").getElementById(s"change-ref-$index").attr("href")shouldBe
          routes.PreviousSchemeController.show(previousScheme.processingId).toString
        reviewSchemesTableBody.select("tr").get(index).getElementById(s"remove-$index").text() shouldBe
          Messages("common.base.remove")
      }

      document.body.getElementById("next").text() shouldEqual Messages("common.button.continueThirdSection")
      document.body.getElementById("get-help-action").text shouldBe Messages("common.error.help.text")

    }
  }
}