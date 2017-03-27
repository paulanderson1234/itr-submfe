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

package testOnly.controllers

import auth.{MockAuthConnector, TAVCUser}
import auth._
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import forms.NatureOfBusinessForm
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import testOnly.controllers.eis.TestEndpointEISController
import testOnly.models.TestPreviousSchemesModel

import scala.concurrent.Future

class TestEndpointEISControllerSpec extends BaseSpec {

  object TestController extends TestEndpointEISController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  implicit val user = TAVCUser(ggUser.allowedAuthContext,internalId)

  def setupShowMocks(): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.eq(KeystoreKeys.dateOfIncorporation))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[OperatingCostsModel](Matchers.eq(KeystoreKeys.operatingCosts))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[IsKnowledgeIntensiveModel](Matchers.eq(KeystoreKeys.isKnowledgeIntensive))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[PercentageStaffWithMastersModel](Matchers.eq(KeystoreKeys.percentageStaffWithMasters))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[TenYearPlanModel](Matchers.eq(KeystoreKeys.tenYearPlan))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[HadPreviousRFIModel](Matchers.eq(KeystoreKeys.hadPreviousRFI))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[UsedInvestmentReasonBeforeModel](Matchers.eq(KeystoreKeys.usedInvestmentReasonBefore))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.eq(KeystoreKeys.turnoverCosts))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[ConfirmContactDetailsModel](Matchers.eq(KeystoreKeys.confirmContactDetails))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.manualContactDetails))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[ConfirmCorrespondAddressModel](Matchers.eq(KeystoreKeys.confirmContactAddress))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[AddressModel](Matchers.eq(KeystoreKeys.manualContactAddress))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[HadOtherInvestmentsModel](Matchers.eq(KeystoreKeys.hadOtherInvestments))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.any())
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
  }

  def setupFillFormMocks(natureOfBusinessModel: Option[NatureOfBusinessModel]): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[NatureOfBusinessModel](Matchers.eq(KeystoreKeys.natureOfBusiness))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(natureOfBusinessModel))
  }

  def setupFillPreviousSchemesFormMocks(previousSchemes: Option[Vector[PreviousSchemeModel]]): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(previousSchemes))
  }

  "TestEndpointEISController" should {
    "Use the correct s4l connector" in {
      TestEndpointEISController.s4lConnector shouldBe S4LConnector
    }
    "Use the correct auth connector" in {
      TestEndpointEISController.authConnector shouldBe FrontendAuthConnector
    }
    "Use the correct enrolment connector" in {
      TestEndpointEISController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "Use the correct app config" in {
      TestEndpointEISController.applicationConfig shouldBe FrontendAppConfig
    }
  }

  "TestEndpointEISController.showPageOne" when {

    "Called as an authorised and enrolled user" should {

      "Return OK" in {
        mockEnrolledRequest()
        setupShowMocks()
        showWithSessionAndAuth(TestController.showPageOne(None))(
          result => status(result) shouldBe OK
        )
      }

    }

  }

  "TestEndpointEISController.submitPageOne" when {

    "Called as an authorised and enrolled user" should {

      "Return OK" in {
        mockEnrolledRequest()
        submitWithSessionAndAuth(TestController.submitPageOne())(
          result => status(result) shouldBe OK
        )
      }

    }

  }

  "TestEndpointEISController.showPageTwo" when {

    "Called as an authorised and enrolled user" should {

      "Return OK" in {
        mockEnrolledRequest()
        setupShowMocks()
        showWithSessionAndAuth(TestController.showPageTwo())(
          result => status(result) shouldBe OK
        )
      }

    }

  }

  "TestEndpointEISController.submitPageTwo" when {

    "Called as an authorised and enrolled user" should {

      "Return OK" in {
        mockEnrolledRequest()
        submitWithSessionAndAuth(TestController.submitPageTwo())(
          result => status(result) shouldBe OK
        )
      }

    }

  }

  "TestEndpointEISController.fillForm" when {

    "s4lConnector returns data" should {

      "Return a form filled with data from s4lConnector" in {
        setupFillFormMocks(Some(natureOfBusinessModel))
        val result = TestController.fillForm[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness, NatureOfBusinessForm.natureOfBusinessForm)
        await(result).get shouldBe natureOfBusinessModel
      }

    }

    "s4lConnector returns nothing" should {

      "Return an empty form" in {
        setupFillFormMocks(None)
        val result = TestController.fillForm[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness, NatureOfBusinessForm.natureOfBusinessForm)
        await(result).value shouldBe None
      }

    }

  }

  "TestEndpointEISController.fillPreviousSchemesForm" when {

    "s4lConnector returns data" should {

      "Return a form filled with data from s4lConnector" in {
        setupFillPreviousSchemesFormMocks(Some(previousSchemeVectorList))
        val result = TestController.fillPreviousSchemesForm
        await(result).get shouldBe TestPreviousSchemesModel(Some(previousSchemeVectorList))
      }

    }

    "s4lConnector returns nothing" should {

      "Return an empty form" in {
        setupFillPreviousSchemesFormMocks(None)
        val result = TestController.fillPreviousSchemesForm
        await(result).get shouldBe TestPreviousSchemesModel(None)
      }

    }

  }

  "TestEndpointEISController.bindForm" when {

    "Sent a valid form" should {

      "Return the valid form" in {
        val result = TestController.bindForm[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness,
          NatureOfBusinessForm.natureOfBusinessForm)(fakeRequest.withFormUrlEncodedBody("natureofbusiness" -> "test"),user,NatureOfBusinessModel.format)
        await(result).get shouldBe NatureOfBusinessModel("test")
      }

    }

    "Sent an invalid form" should {

      "Return the invalid form with errors" in {

        val result = TestController.bindForm[NatureOfBusinessModel](KeystoreKeys.natureOfBusiness,
          NatureOfBusinessForm.natureOfBusinessForm)(fakeRequest,user,NatureOfBusinessModel.format)
        await(result).hasErrors shouldBe true
      }

    }

  }

  "TestEndpointEISController.bindKIForm" when {

    "Sent a valid form with Yes" should {

      "Return the valid form" in {
        val result = TestController.bindKIForm()(fakeRequest.withFormUrlEncodedBody("isKnowledgeIntensive" -> Constants.StandardRadioButtonYesValue),user)
        await(result).get shouldBe IsKnowledgeIntensiveModel(Constants.StandardRadioButtonYesValue)
      }

    }

    "Sent a valid form with No" should {

      "Return the valid form" in {
        val result = TestController.bindKIForm()(fakeRequest.withFormUrlEncodedBody("isKnowledgeIntensive" -> Constants.StandardRadioButtonNoValue),user)
        await(result).get shouldBe IsKnowledgeIntensiveModel(Constants.StandardRadioButtonNoValue)
      }

    }

    "Sent an invalid form" should {

      "Return the invalid form with errors" in {

        val result = TestController.bindKIForm()(fakeRequest,user)
        await(result).hasErrors shouldBe true
      }

    }

  }

  "TestEndpointEISController.bindPreviousSchemesForm" when {

    "Sent a valid form with a previous scheme" should {

      "Return the valid form" in {
        setupFillPreviousSchemesFormMocks(None)
        val result = TestController.bindPreviousSchemesForm()(fakeRequest.withFormUrlEncodedBody(
          "testPreviousSchemes[0].schemeTypeDesc" -> Constants.schemeTypeEis,
          "testPreviousSchemes[0].previousSchemeInvestmentAmount" -> "3",
          "testPreviousSchemes[0].previousSchemeInvestmentSpent" -> "",
          "testPreviousSchemes[0].previousSchemeOtherSchemeName" -> "",
          "testPreviousSchemes[0].previousSchemeInvestmentDay" -> "4",
          "testPreviousSchemes[0].previousSchemeInvestmentMonth" -> "5",
          "testPreviousSchemes[0].previousSchemeInvestmentYear" -> "2008",
          "testPreviousSchemes[0].previousSchemeProcessingId" -> "1"
        ), user)
        result.get shouldBe TestPreviousSchemesModel(Some(Seq(PreviousSchemeModel(Constants.schemeTypeEis,
          3, None, None, Some(4), Some(5), Some(2008), Some(1)))))
      }

    }

    "Sent an empty form" should {

      "Return the empty form" in {
        val result = TestController.bindPreviousSchemesForm()(fakeRequest, user)
        result.get shouldBe TestPreviousSchemesModel(None)
      }

    }

    "Sent an invalid form" should {

      "Return the invalid form with errors" in {
        setupFillPreviousSchemesFormMocks(None)
        val result = TestController.bindPreviousSchemesForm()(fakeRequest.withFormUrlEncodedBody(
          "testPreviousSchemes[0].schemeTypeDesc" -> Constants.schemeTypeEis
        ), user)
        result.hasErrors shouldBe true
      }

    }

  }

}
