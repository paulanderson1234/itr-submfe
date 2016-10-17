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

/**
  * Copyright 2016 HM Revenue & Customs
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package views

import java.util.UUID

import auth.{Enrolment, Identifier, MockAuthConnector}
import common.{Constants, KeystoreKeys}
import config.FrontendAppConfig
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.{CheckAnswersController, routes}
import controllers.helpers.FakeRequestHelper
import models._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class CheckAnswersPreviousSchemeSpec extends UnitSpec with WithFakeApplication with MockitoSugar with FakeRequestHelper with BeforeAndAfterEach {

  val mockS4lConnector = mock[S4LConnector]

   class SetupPage {

    val controller = new CheckAnswersController {
      override lazy val applicationConfig = FrontendAppConfig
      override lazy val authConnector = MockAuthConnector
      val s4lConnector: S4LConnector = mockS4lConnector
      override lazy val enrolmentConnector = mock[EnrolmentConnector]
    }

     when(controller.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
       .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))
  }

  override def beforeEach() {
    reset(mockS4lConnector)
  }

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 2: Previous Schemes" +
      " when an empty set of company detail models are passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockS4lConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.eq(KeystoreKeys.yourCompanyNeed))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[TaxpayerReferenceModel](Matchers.eq(KeystoreKeys.taxpayerReference))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.eq(KeystoreKeys.registeredAddress))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](Matchers.eq(KeystoreKeys.percentageStaffWithMasters))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))

        val result = controller.show.apply(authorisedFakeRequest.withFormUrlEncodedBody())
        Jsoup.parse(contentAsString(result))
      }


      document.title() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description")
      document.getElementById("print-this-page").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.print.text")

      lazy val previousRfiTableTbody = document.getElementById("previous-rfi-table").select("tbody")
      lazy val notAvailableMessage = Messages("common.notAvailable")

      //Section 1 table heading
      document.getElementById("previousRFISection-table-heading").text() shouldBe Messages("summaryQuestion.previousRFISection")
      //Previous RFI None
      previousRfiTableTbody.select("tr").get(0).getElementById("emptyPreviousRFISection-subHeading").text() shouldBe
        notAvailableMessage
      previousRfiTableTbody.select("tr").get(0).getElementById("emptyPreviousRFISection-link")
        .attr("href") shouldEqual routes.HadPreviousRFIController.show().toString()

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.SupportingDocumentsController.show().toString()
    }
  }
}
