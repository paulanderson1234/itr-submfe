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

package controllers

import java.time.ZoneId
import java.util.{Date, UUID}

import builders.SessionBuilder
import common.{Constants, KeystoreKeys}
import connectors.KeystoreConnector
import models.{ContactDetailsModel, _}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future

class CheckAnswersControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object CheckAnswersControllerTest extends CheckAnswersController {
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  val yourCompanyNeedModel = YourCompanyNeedModel("")
  val taxpayerReferenceModel = TaxpayerReferenceModel("")
  val registeredAddressModel = RegisteredAddressModel("")
  val dateOfIncorporationModel = DateOfIncorporationModel(Some(1), Some(1), Some(1990))
  val natureOfBusinessModel = NatureOfBusinessModel("")
  val commercialSaleModel = CommercialSaleModel(Constants.StandardRadioButtonNoValue, None, None, None)
  val isKnowledgeIntensiveModel = IsKnowledgeIntensiveModel("")
  val operatingCostsModel = OperatingCostsModel("", "", "", "", "", "")
  val percentageStaffWithMastersModel = PercentageStaffWithMastersModel("")
  val tenYearPlanModel = TenYearPlanModel("", None)
  val subsidiariesModel = SubsidiariesModel("")
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
  val contactDetailsModel = ContactDetailsModel("", "", "", "")

  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = CheckAnswersControllerTest.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = CheckAnswersControllerTest.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "CheckAnswersController" should {
    "use the correct keystore connector" in {
      CheckAnswersController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to CheckAnswersController with a populated set of models" should {
    "return a 200 when the page is loaded" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.eq(KeystoreKeys.yourCompanyNeed))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(yourCompanyNeedModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[TaxpayerReferenceModel](Matchers.eq(KeystoreKeys.taxpayerReference))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(taxpayerReferenceModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.eq(KeystoreKeys.registeredAddress))(Matchers.any(),
        Matchers.any())).thenReturn(Future.successful(Option(registeredAddressModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(dateOfIncorporationModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(natureOfBusinessModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(),
        Matchers.any())).thenReturn(Future.successful(Option(commercialSaleModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(isKnowledgeIntensiveModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(operatingCostsModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](Matchers.eq(KeystoreKeys.percentageStaffWithMasters))(Matchers.any(),
        Matchers.any())).thenReturn(Future.successful(Option(percentageStaffWithMastersModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(tenYearPlanModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(subsidiariesModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(),
        Matchers.any())).thenReturn(Future.successful(Option(hadPreviousRFIModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(proposedInvestmentModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(whatWillUseForModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(usedInvestmentReasonBeforeModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(),
        Matchers.any())).thenReturn(Future.successful(Option(previousBeforeDOFCSModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(newGeographicalMarketModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(newProductModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(subsidiariesSpendingInvestmentModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(),
        Matchers.any())).thenReturn(Future.successful(Option(subsidiariesNinetyOwnedModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(),
        Matchers.any())).thenReturn(Future.successful(Option(contactDetailsModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(investmentGrowModel)))

      showWithSession(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request to CheckAnswersController with an empty set of models" should {
    "return a 200 when the page is loaded" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.eq(KeystoreKeys.yourCompanyNeed))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[TaxpayerReferenceModel](Matchers.eq(KeystoreKeys.taxpayerReference))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[RegisteredAddressModel](Matchers.eq(KeystoreKeys.registeredAddress))(Matchers.any(),
        Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))(Matchers.any(),
        Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](Matchers.eq(KeystoreKeys.percentageStaffWithMasters))(Matchers.any(),
        Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))(Matchers.any(),
        Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[WhatWillUseForModel](Matchers.eq(KeystoreKeys.whatWillUseFor))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))(Matchers.any(),
        Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesSpendingInvestmentModel](Matchers.eq(KeystoreKeys.subsidiariesSpendingInvestment))
        (Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[SubsidiariesNinetyOwnedModel](Matchers.eq(KeystoreKeys.subsidiariesNinetyOwned))(Matchers.any(),
        Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(),
        Matchers.any())).thenReturn(Future.successful(None))
      when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))

      showWithSession(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a submission to the CheckAnswersController" should {
    "redirect to the same page" in {
      val request = FakeRequest().withFormUrlEncodedBody()
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/acknowledgement")
        }
      )
    }
  }
}
