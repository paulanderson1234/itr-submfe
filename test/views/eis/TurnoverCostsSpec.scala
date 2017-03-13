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
import common.KeystoreKeys
import config.FrontendAppConfig
import controllers.eis.TurnoverCostsController
import models.AnnualTurnoverCostsModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.helpers.ViewSpec
import play.api.i18n.Messages.Implicits._

import scala.concurrent.Future

class TurnoverCostsSpec extends ViewSpec {

  object TestController extends TurnoverCostsController {
    override lazy val applicationConfig = MockConfigEISFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val submissionConnector = mockSubmissionConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(annualTurnoverCostsModel: Option[AnnualTurnoverCostsModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.eq(KeystoreKeys.turnoverCosts))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(annualTurnoverCostsModel))

  "Verify that the TurnoverCosts page contains the correct elements " +
    "when a valid TurnoverCostsModel is passed as returned from keystore" in new Setup {
    val document : Document = {
      setupMocks(Some(annualTurnoverCostsModel))
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.NewProductController.show().url
    document.title() shouldBe Messages("page.companyDetails.TurnoverCosts.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.TurnoverCosts.heading")
    document.getElementById("turnover-costs-hint-one").text() shouldBe Messages("page.companyDetails.TurnoverCosts.hint.one")
    document.getElementById("col-heading-one").hasClass("visuallyhidden")
    document.getElementById("col-heading-one").text() shouldBe Messages("page.companyDetails.TurnoverCosts.col.heading.one")
    document.getElementById("col-heading-two").hasClass("visuallyhidden")
    document.getElementById("col-heading-two").text() shouldBe Messages("page.companyDetails.TurnoverCosts.col.heading.two")

    document.getElementById("help").text() shouldBe Messages("page.companyDetails.TurnoverCosts.help.link")
    document.getElementById("help-text").text() shouldBe Messages("page.companyDetails.TurnoverCosts.help.text")

    document.getElementById("row-heading-one").text() shouldBe Messages("page.companyDetails.TurnoverCosts.row.heading.one")
    document.getElementById("row-heading-two").text() shouldBe Messages("page.companyDetails.TurnoverCosts.row.heading.two")
    document.getElementById("row-heading-three").text() shouldBe Messages("page.companyDetails.TurnoverCosts.row.heading.three")
    document.getElementById("row-heading-four").text() shouldBe Messages("page.companyDetails.TurnoverCosts.row.heading.four")
    document.getElementById("row-heading-five").text() shouldBe Messages("page.companyDetails.TurnoverCosts.row.heading.five")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")

  }

  "Verify that TurnoverCosts page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" in new Setup {
    val document : Document = {
      setupMocks()
      val result = TestController.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    document.body.getElementById("back-link").attr("href") shouldEqual controllers.eis.routes.NewProductController.show().url
    document.title() shouldBe Messages("page.companyDetails.TurnoverCosts.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.companyDetails.TurnoverCosts.heading")
    document.getElementById("turnover-costs-hint-one").text() shouldBe Messages("page.companyDetails.TurnoverCosts.hint.one")
    document.getElementById("col-heading-one").hasClass("visuallyhidden")
    document.getElementById("col-heading-one").text() shouldBe Messages("page.companyDetails.TurnoverCosts.col.heading.one")
    document.getElementById("col-heading-two").hasClass("visuallyhidden")
    document.getElementById("col-heading-two").text() shouldBe Messages("page.companyDetails.TurnoverCosts.col.heading.two")

    document.getElementById("help").text() shouldBe Messages("page.companyDetails.TurnoverCosts.help.link")
    document.getElementById("help-text").text() shouldBe Messages("page.companyDetails.TurnoverCosts.help.text")

    document.getElementById("row-heading-one").text() shouldBe Messages("page.companyDetails.TurnoverCosts.row.heading.one")
    document.getElementById("row-heading-two").text() shouldBe Messages("page.companyDetails.TurnoverCosts.row.heading.two")
    document.getElementById("row-heading-three").text() shouldBe Messages("page.companyDetails.TurnoverCosts.row.heading.three")
    document.getElementById("row-heading-four").text() shouldBe Messages("page.companyDetails.TurnoverCosts.row.heading.four")
    document.getElementById("row-heading-five").text() shouldBe Messages("page.companyDetails.TurnoverCosts.row.heading.five")
    document.getElementById("next").text() shouldBe Messages("common.button.snc")
  }

  "Verify that TurnoverCosts page contains show the error summary when an invalid model (no data) is submitted" in new Setup {
    val document : Document = {
      // submit the model with no radio selected as a post action
      val result = TestController.submit.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }
    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.companyDetails.TurnoverCosts.title")
  }
}
