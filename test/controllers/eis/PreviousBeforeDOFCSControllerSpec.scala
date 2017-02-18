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

package controllers.eis

import auth.{MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.{CommercialSaleModel, KiProcessingModel, PreviousBeforeDOFCSModel, SubsidiariesModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class PreviousBeforeDOFCSControllerSpec extends BaseSpec {

  object TestController extends PreviousBeforeDOFCSController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupShowMocks(kiProcessingModel: Option[KiProcessingModel] = None, commercialSaleModel: Option[CommercialSaleModel] = None,
                     previousBeforeDOFCSModel: Option[PreviousBeforeDOFCSModel] = None) : Unit = {
    when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(kiProcessingModel))
    when(mockS4lConnector.fetchAndGetFormData[CommercialSaleModel](Matchers.eq(KeystoreKeys.commercialSale))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(commercialSaleModel))
    when(mockS4lConnector.fetchAndGetFormData[PreviousBeforeDOFCSModel](Matchers.eq(KeystoreKeys.previousBeforeDOFCS))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(previousBeforeDOFCSModel))
  }

  def setupSubmitMocks(subsidiariesModel: Option[SubsidiariesModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(subsidiariesModel))

  "PreviousBeforeDOFCSController" should {
    "use the correct keystore connector" in {
      PreviousBeforeDOFCSController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      PreviousBeforeDOFCSController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      PreviousBeforeDOFCSController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET formInput to PreviousBeforeDOFCSController when Authenticated and enrolled" when {

    "The user is KI and has filled in all required models and a PreviousBeforeDOFCSModel can be obtained from keystore" should {
      "return an OK" in {
        setupShowMocks(Some(trueKIModel), Some(commercialSaleModelYes), Some(previousBeforeDOFCSModelYes))
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show())(
          result => status(result) shouldBe OK
        )
      }
    }

    "The user is non KI and has filled in all required models and a PreviousBeforeDOFCSModel can be obtained from keystore" should {
      "return an OK" in {
        setupShowMocks(Some(kiProcessingModelNotMet), Some(commercialSaleModelYes), Some(previousBeforeDOFCSModelYes))
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show())(
          result => status(result) shouldBe OK
        )
      }
    }

    "The user is KI and has filled in all required models and a PreviousBeforeDOFCSModel can't be obtained from keystore" should {
      "return an OK" in {
        setupShowMocks(Some(trueKIModel), Some(commercialSaleModelYes))
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show())(
          result => status(result) shouldBe OK
        )
      }
    }

    "The user is non KI and has filled in all required models and a PreviousBeforeDOFCSModel can't be obtained from keystore" should {
      "return an OK" in {
        setupShowMocks(Some(kiProcessingModelNotMet), Some(commercialSaleModelYes))
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show())(
          result => status(result) shouldBe OK
        )
      }
    }

    "The user is KI and has not filled in commercial sale model" should {
      "return a SEE_OTHER" in {
        setupShowMocks(Some(kiProcessingModelMet))
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show())(
          result => status(result) shouldBe SEE_OTHER
        )
      }
      "redirect to commercial sale page" in {
        setupShowMocks(Some(kiProcessingModelMet))
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show())(
          result => redirectLocation(result) shouldBe Some(routes.CommercialSaleController.show().url)
        )
      }
    }

    "The user has not filled in the KI model" should {
      "return a SEE_OTHER" in {
        setupShowMocks(commercialSaleModel = Some(commercialSaleModelYes))
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show())(
          result => status(result) shouldBe SEE_OTHER
        )
      }
      "redirect to date of incorporation page" in {
        setupShowMocks(commercialSaleModel = Some(commercialSaleModelYes))
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show())(
          result => redirectLocation(result) shouldBe Some(routes.DateOfIncorporationController.show().url)
        )
      }
    }

    "The user has not filled in the KI model or commercial sale model" should {
      "return a SEE_OTHER" in {
        setupShowMocks()
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show())(
          result => status(result) shouldBe SEE_OTHER
        )
      }
      "redirect to commercial sale page" in {
        setupShowMocks()
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show())(
          result => redirectLocation(result) shouldBe Some(routes.CommercialSaleController.show().url)
        )
      }
    }

    "There is no KI model or commercial sale model" should {
      "return a SEE_OTHER" in {
        setupShowMocks()
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show())(
          result => status(result) shouldBe SEE_OTHER
        )
      }
      "redirect to commercial sale page" in {
        setupShowMocks(None, None, None)
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show())(
          result => redirectLocation(result) shouldBe Some(routes.CommercialSaleController.show().url)
        )
      }
    }
  }

  "Sending a valid 'No' form submit to the PreviousBeforeDOFCSController when Authenticated and enrolled" should {
    "redirect to new geographical market" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "previousBeforeDOFCS" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.NewGeographicalMarketController.show().url)
        }
      )
    }
  }

  "Sending a valid 'Yes' form submit to the PreviousBeforeDOFCSController with 'No' to Subsidiaries Model when Authenticated and enrolled" should {
    "redirect to the how-plan-to-use-investment page" in {
      setupSubmitMocks(Some(subsidiariesModelNo))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "previousBeforeDOFCS" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvestmentGrowController.show().url)
        }
      )
    }
  }

  "Sending a valid 'Yes' form submit to the PreviousBeforeDOFCSController with 'Yes' to Subsidiaries Model when Authenticated and enrolled" should {
    "redirect to the subsidiaries-spending-investment page" in {
      setupSubmitMocks(Some(subsidiariesModelYes))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "previousBeforeDOFCS" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesSpendingInvestmentController.show().url)
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the PreviousBeforeDOFCSController with 'Yes' to Subsidiaries Model when Authenticated and enrolled" should {
    "redirect to new geographical market" in {
      setupSubmitMocks(Some(subsidiariesModelYes))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "previousBeforeDOFCS" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.NewGeographicalMarketController.show().url)
        }
      )
    }
  }

  "Sending a valid 'No' form submit to the PreviousBeforeDOFCSController with 'No' to Subsidiaries Model when Authenticated and enrolled" should {
    "redirect to new geographical market" in {
      setupSubmitMocks(Some(subsidiariesModelNo))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "previousBeforeDOFCS" -> Constants.StandardRadioButtonNoValue
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.NewGeographicalMarketController.show().url)
        }
      )
    }
  }

  "Sending a valid form submit to the PreviousBeforeDOFCSController without a Subsidiaries Model when Authenticated and enrolled" should {
    "redirect to Subsidiaries page" in {
      setupSubmitMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "previousBeforeDOFCS" -> Constants.StandardRadioButtonYesValue
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.SubsidiariesController.show().url)
        }
      )
    }
  }


  "Sending an invalid form submission with validation errors to the PreviousBeforeDOFCSController when Authenticated and enrolled" when {
    "the user submits and is KI" should {
      "redirect to itself" in {
        setupShowMocks(Some(trueKIModel), Some(commercialSaleModelYes), Some(previousBeforeDOFCSModelYes))
        mockEnrolledRequest(eisSchemeTypesModel)
        val formInput = "previousBeforeDOFCS" -> ""
        submitWithSessionAndAuth(TestController.submit, formInput)(
          result => {
            status(result) shouldBe BAD_REQUEST
          }
        )
      }
    }
    "the user submits and is not KI" should {
      "redirect to itself" in {
        setupShowMocks(Some(kiProcessingModelNotMet), Some(commercialSaleModelYes), Some(previousBeforeDOFCSModelYes))
        mockEnrolledRequest(eisSchemeTypesModel)
        val formInput = "previousBeforeDOFCS" -> ""
        submitWithSessionAndAuth(TestController.submit, formInput)(
          result => {
            status(result) shouldBe BAD_REQUEST
          }
        )
      }
    }
  }
}
