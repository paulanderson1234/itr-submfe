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
import builders.SessionBuilder
import common.Constants
import config.FrontendAppConfig
import connectors.{EnrolmentConnector, KeystoreConnector}
import controllers.helpers.FakeRequestHelper
import controllers.{HadPreviousRFIController, routes}
import models.HadPreviousRFIModel
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class HadPreviousRFISpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper{

  val mockKeystoreConnector = mock[KeystoreConnector]

  val hadPreviousRFIModel = new HadPreviousRFIModel(Constants.StandardRadioButtonYesValue)
  val emptyHadPreviousRFIModel = new HadPreviousRFIModel("")

  class SetupPage {

    val controller = new HadPreviousRFIController{
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

    when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))
  }

  "Verify that the hadPreviousRFI page contains the correct elements " +
    "when a valid HadPreviousRFIModel is passed as returned from keystore" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeystoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(hadPreviousRFIModel)))
      val result = controller.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.SubsidiariesController.show.toString()
    document.title() shouldBe Messages("page.previousInvestment.hadPreviousRFI.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.previousInvestment.hadPreviousRFI.heading")
    document.select("#hadPreviousRFI-yes").size() shouldBe 1
    document.select("#hadPreviousRFI-yes").size() shouldBe 1
    document.getElementById("hadPreviousRFI-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("hadPreviousRFI-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("hadPreviousRFILegend").select(".visuallyhidden").text() shouldBe Messages("page.previousInvestment.hadPreviousRFI.heading")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.two")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")




  }

  "Verify that hadPreviousRFI page contains the correct elements when an empty model " +
    "is passed because nothing was returned from keystore" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      when(mockKeystoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(emptyHadPreviousRFIModel)))
      val result = controller.show.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }

    document.body.getElementById("back-link").attr("href") shouldEqual routes.SubsidiariesController.show.toString()
    document.title() shouldBe Messages("page.previousInvestment.hadPreviousRFI.title")
    document.getElementById("main-heading").text() shouldBe Messages("page.previousInvestment.hadPreviousRFI.heading")
    document.select("#hadPreviousRFI-yes").size() shouldBe 1
    document.select("#hadPreviousRFI-no").size() shouldBe 1
    document.getElementById("hadPreviousRFI-yesLabel").text() shouldBe Messages("common.radioYesLabel")
    document.getElementById("hadPreviousRFI-noLabel").text() shouldBe Messages("common.radioNoLabel")
    document.getElementById("hadPreviousRFILegend").select(".visuallyhidden").text() shouldBe Messages("page.previousInvestment.hadPreviousRFI.heading")
    document.body.getElementById("progress-section").text shouldBe  Messages("common.section.progress.company.details.two")
    document.getElementById("next").text() shouldBe Messages("common.button.continue")
  }

  "Verify that HadPreviousRFI page contains show the error summary when an invalid model (no radio button selection) is submitted" in new SetupPage {
    val document : Document = {
      val userId = s"user-${UUID.randomUUID}"
      // submit the model with no radio slected as a post action
      val result = controller.submit.apply(authorisedFakeRequest)
      Jsoup.parse(contentAsString(result))
    }

    // Make sure we have the expected error summary displayed
    document.getElementById("error-summary-display").hasClass("error-summary--show")
    document.title() shouldBe Messages("page.previousInvestment.hadPreviousRFI.title")

  }
}

