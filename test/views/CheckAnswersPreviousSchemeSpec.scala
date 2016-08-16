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

import common.{Constants, KeystoreKeys}
import connectors.KeystoreConnector
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

  val mockKeystoreConnector = mock[KeystoreConnector]

  val yourCompanyNeedModel = YourCompanyNeedModel("AA")
  val taxpayerReferenceModel = TaxpayerReferenceModel("1234567891012")
  val registeredAddressModel = RegisteredAddressModel("SY26GA")
  val dateOfIncorporationModel = DateOfIncorporationModel(Some(20), Some(4), Some(1990))
  val natureOfBusinessModel = NatureOfBusinessModel("Creating new products")
  val commercialSaleModelYes = CommercialSaleModel(Constants.StandardRadioButtonYesValue, Some(4), Some(8), Some(1995))
  val commercialSaleModelNo = CommercialSaleModel(Constants.StandardRadioButtonNoValue, None, None, None)
  val isKnowledgeIntensiveModelYes = IsKnowledgeIntensiveModel(Constants.StandardRadioButtonYesValue)
  val isKnowledgeIntensiveModelNo = IsKnowledgeIntensiveModel(Constants.StandardRadioButtonNoValue)
  val operatingCostsModel = OperatingCostsModel("28976", "12348", "77725", "99883", "23321", "65436")
  val percentageStaffWithMastersModel = PercentageStaffWithMastersModel(Constants.StandardRadioButtonYesValue)
  val tenYearPlanModelYes = TenYearPlanModel(Constants.StandardRadioButtonYesValue, Some("At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium " +
    "voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique " +
    "sunt in culpa qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. " +
    "Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus id quod maxime placeat facere possimus"))
  val tenYearPlanModelNo = TenYearPlanModel(Constants.StandardRadioButtonNoValue, None)
  val subsidiariesModel = SubsidiariesModel(Constants.StandardRadioButtonYesValue)
  val hadPreviousRFIModel = HadPreviousRFIModel("")
  val proposedInvestmentModel = ProposedInvestmentModel(0)
  val whatWillUseForModel = WhatWillUseForModel("")
  val usedInvestmentReasonBeforeModel = UsedInvestmentReasonBeforeModel("")
  val previousBeforeDOFCSModel = PreviousBeforeDOFCSModel("")
  val newGeographicalMarketModel = NewGeographicalMarketModel("")
  val newProductModel = NewProductModel("")
  val subsidiariesSpendingInvestmentModel = SubsidiariesSpendingInvestmentModel("")
  val subsidiariesNinetyOwnedModel = SubsidiariesNinetyOwnedModel("")
  val investmentGrowModel = InvestmentGrowModel("")


  class SetupPage {

    val controller = new CheckAnswersController {
      val keyStoreConnector: KeystoreConnector = mockKeystoreConnector
    }
  }

  override def beforeEach() {
    reset(mockKeystoreConnector)
  }

  "The Check Answers page" should {

    "Verify that the Check Answers page contains the correct elements for Section 2: Previous Schemes" +
      " when an empty set of company detail models are passed" in new SetupPage {
      val document: Document = {
        val userId = s"user-${UUID.randomUUID}"

        when(mockKeystoreConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.eq(KeystoreKeys.yourCompanyNeed))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[TaxpayerReferenceModel](Matchers.eq(KeystoreKeys.taxpayerReference))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.eq(KeystoreKeys.registeredAddress))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](Matchers.eq(KeystoreKeys.percentageStaffWithMasters))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
          (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(),
          Matchers.any())).thenReturn(Future.successful(None))
        when(mockKeystoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))

        val result = controller.show.apply(fakeRequestWithSession.withFormUrlEncodedBody())
        Jsoup.parse(contentAsString(result))
      }


      document.title() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("main-heading").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.heading")
      document.getElementById("description").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.description")
      document.getElementById("print-this-page").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.print.text")

      lazy val previousRfiTableTbody = document.getElementById("previous-rfi-table").select("tbody")

      //Section 1 table heading
      document.getElementById("previousRFISection-table-heading").text() shouldBe Messages("summaryQuestion.previousRFISection")
      //Previous RFI None
      previousRfiTableTbody.select("tr").get(0).getElementById("emptyPreviousRFISection-subHeading").text() shouldBe
        Messages("common.notAvailable")
      previousRfiTableTbody.select("tr").get(0).getElementById("emptyPreviousRFISection-link")
        .attr("href") shouldEqual routes.HadPreviousRFIController.show().toString()

      document.getElementById("submit").text() shouldBe Messages("page.checkAndSubmit.checkAnswers.button.confirm")
      document.body.getElementById("back-link").attr("href") shouldEqual routes.SupportingDocumentsController.show().toString()
    }
  }
}
