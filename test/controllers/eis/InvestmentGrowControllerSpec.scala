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
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.{InvestmentGrowModel, NewGeographicalMarketModel, NewProductModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class InvestmentGrowControllerSpec extends BaseSpec {

  object TestController extends InvestmentGrowController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  val validBackLink = routes.SubsidiariesNinetyOwnedController.show().url

  def setup(investmentGrowModel: Option[InvestmentGrowModel], newGeographicalMarketModel: Option[NewGeographicalMarketModel],
                newProductModel: Option[NewProductModel], backLink: Option[String]): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(investmentGrowModel))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkInvestmentGrow))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(backLink))
    when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(newGeographicalMarketModel))
    when(mockS4lConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(newProductModel))
  }

  "InvestmentGrowController" should {
    "use the correct keystore connector" in {
      InvestmentGrowController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      InvestmentGrowController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      InvestmentGrowController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct app config" in {
      InvestmentGrowController.applicationConfig shouldBe FrontendAppConfig
    }
  }

  "Sending a GET request to InvestmentGrowController when authenticated and enrolled" when {

    "an Investment Grow form can be retrieved from keystore" should {
      "return an OK" in {
        setup(Some(investmentGrowModel),Some(newGeographicalMarketModelYes),Some(newProductMarketModelYes),Some(validBackLink))
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show)(
          result => status(result) shouldBe OK
        )
      }
    }

    "no Investment Grow form is retrieved from keystore" should {
      "return an OK" in {
        setup(None,Some(newGeographicalMarketModelYes),Some(newProductMarketModelYes),Some(validBackLink))
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show)(
          result => status(result) shouldBe OK
        )
      }
    }

    "no new geographical market model and new product market model are retrieved from keystore" should {
      "return an OK" in {
        setup(None,None,None,Some(validBackLink))
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show)(
          result => status(result) shouldBe OK
        )
      }
    }

    "no back link is retrieved from keystore" should {

      "return a SEE_OTHER" in {
        setup(None,Some(newGeographicalMarketModelYes),Some(newProductMarketModelYes),None)
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show)(
          result => {
            status(result) shouldBe SEE_OTHER
          }
        )
      }

      "redirect to the investment purpose page" in {
        setup(None,Some(newGeographicalMarketModelYes),Some(newProductMarketModelYes),None)
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show)(
          result => {
            redirectLocation(result) shouldBe Some(routes.ProposedInvestmentController.show().url)
          }
        )
      }
    }

    "no new geographic market model is retrieved from keystore" should {

      "return a SEE_OTHER" in {
        setup(None,None, Some(newProductMarketModelYes),Some(validBackLink))
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show)(
          result => {
            status(result) shouldBe SEE_OTHER
          }
        )
      }

      "redirect to the new geographical market page" in {
        setup(None,None, Some(newProductMarketModelYes),Some(validBackLink))
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show)(
          result => {
            redirectLocation(result) shouldBe Some(routes.NewGeographicalMarketController.show().url)
          }
        )
      }
    }

    "no new product market model is retrieved from keystore" should {

      "return a SEE_OTHER" in {
        setup(None,Some(newGeographicalMarketModelYes),None,Some(validBackLink))
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show)(
          result => {
            status(result) shouldBe SEE_OTHER
          }
        )
      }

      "redirect to the new product market page" in {
        setup(None,Some(newGeographicalMarketModelYes),None,Some(validBackLink))
        mockEnrolledRequest(eisSchemeTypesModel)
        showWithSessionAndAuth(TestController.show)(
          result => {
            redirectLocation(result) shouldBe Some(routes.NewProductController.show().url)
          }
        )
      }
    }
  }

  "Sending a valid form submit to the InvestmentGrowController when authenticated and enrolled" should {
    "redirect to Contact Details Controller" in {
      setup(None,Some(newGeographicalMarketModelYes),Some(newProductMarketModelYes),Some(validBackLink))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "investmentGrowDesc" -> "some text so it's valid"
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ConfirmContactDetailsController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the InvestmentGrowController with no backlink when authenticated and enrolled" should {

    "return a SEE_OTHER" in {
      setup(None,Some(newGeographicalMarketModelYes),Some(newProductMarketModelYes),None)
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "investmentGrowDesc" -> ""
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
        }
      )
    }

    "redirect to WhatWillUseFor page" in {
      setup(None,Some(newGeographicalMarketModelYes),Some(newProductMarketModelYes),None)
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "investmentGrowDesc" -> ""
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          redirectLocation(result) shouldBe Some(routes.ProposedInvestmentController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the InvestmentGrowController when authenticated and enrolled" should {
    "redirect to itself with errors" in {
      setup(None,Some(newGeographicalMarketModelYes),Some(newProductMarketModelYes),Some(validBackLink))
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "investmentGrowDesc" -> ""
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
