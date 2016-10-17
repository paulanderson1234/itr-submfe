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

import java.net.URLEncoder

import auth.{Enrolment, Identifier, MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, KeystoreConnector}
import controllers.helpers.FakeRequestHelper
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class InvestmentGrowControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite with FakeRequestHelper{

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object InvestmentGrowControllerTest extends InvestmentGrowController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
    override lazy val enrolmentConnector = mock[EnrolmentConnector]
  }

  private def mockEnrolledRequest = when(InvestmentGrowControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG",Seq(Identifier("TavcReference","1234")),"Activated"))))

  private def mockNotEnrolledRequest = when(InvestmentGrowControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(None))

  val validInvestmentGrowModel = InvestmentGrowModel(Constants.StandardRadioButtonYesValue)
  val emptyModel = InvestmentGrowModel("")
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(validInvestmentGrowModel)))
  val newGeoModel = NewGeographicalMarketModel(Constants.StandardRadioButtonYesValue)
  val newProductModel = NewProductModel(Constants.StandardRadioButtonYesValue)
  val validBackLink = routes.SubsidiariesNinetyOwnedController.show().toString()

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  def setup(investmentGrowModel: Option[InvestmentGrowModel], newGeographicalMarketModel: Option[NewGeographicalMarketModel],
                newProductModel: Option[NewProductModel], backLink: Option[String]): Unit = {
    when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.eq(KeystoreKeys.investmentGrow))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(investmentGrowModel))
    when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkInvestmentGrow))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(backLink))
    when(mockKeyStoreConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(newGeographicalMarketModel))
    when(mockKeyStoreConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(newProductModel))
  }

  "InvestmentGrowController" should {
    "use the correct keystore connector" in {
      InvestmentGrowController.keyStoreConnector shouldBe KeystoreConnector
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
        setup(Some(validInvestmentGrowModel),Some(newGeoModel),Some(newProductModel),Some(validBackLink))
        mockEnrolledRequest
        showWithSessionAndAuth(InvestmentGrowControllerTest.show)(
          result => status(result) shouldBe OK
        )
      }
    }

    "no Investment Grow form is retrieved from keystore" should {
      "return an OK" in {
        setup(None,Some(newGeoModel),Some(newProductModel),Some(validBackLink))
        mockEnrolledRequest
        showWithSessionAndAuth(InvestmentGrowControllerTest.show)(
          result => status(result) shouldBe OK
        )
      }
    }

    "no new geographical market model and new product market model are retrieved from keystore" should {
      "return an OK" in {
        setup(None,None,None,Some(validBackLink))
        mockEnrolledRequest
        showWithSessionAndAuth(InvestmentGrowControllerTest.show)(
          result => status(result) shouldBe OK
        )
      }
    }

    "no back link is retrieved from keystore" should {

      "return a SEE_OTHER" in {
        setup(None,Some(newGeoModel),Some(newProductModel),None)
        mockEnrolledRequest
        showWithSessionAndAuth(InvestmentGrowControllerTest.show)(
          result => {
            status(result) shouldBe SEE_OTHER
          }
        )
      }

      "redirect to the investment purpose page" in {
        setup(None,Some(newGeoModel),Some(newProductModel),None)
        mockEnrolledRequest
        showWithSessionAndAuth(InvestmentGrowControllerTest.show)(
          result => {
            redirectLocation(result) shouldBe Some(routes.WhatWillUseForController.show().url)
          }
        )
      }
    }

    "no new geographic market model is retrieved from keystore" should {

      "return a SEE_OTHER" in {
        setup(None,None, Some(newProductModel),Some(validBackLink))
        mockEnrolledRequest
        showWithSessionAndAuth(InvestmentGrowControllerTest.show)(
          result => {
            status(result) shouldBe SEE_OTHER
          }
        )
      }

      "redirect to the new geographical market page" in {
        setup(None,None, Some(newProductModel),Some(validBackLink))
        mockEnrolledRequest
        showWithSessionAndAuth(InvestmentGrowControllerTest.show)(
          result => {
            redirectLocation(result) shouldBe Some(routes.NewGeographicalMarketController.show().url)
          }
        )
      }
    }

    "no new product market model is retrieved from keystore" should {

      "return a SEE_OTHER" in {
        setup(None,Some(newGeoModel),None,Some(validBackLink))
        mockEnrolledRequest
        showWithSessionAndAuth(InvestmentGrowControllerTest.show)(
          result => {
            status(result) shouldBe SEE_OTHER
          }
        )
      }

      "redirect to the new product market page" in {
        setup(None,Some(newGeoModel),None,Some(validBackLink))
        mockEnrolledRequest
        showWithSessionAndAuth(InvestmentGrowControllerTest.show)(
          result => {
            redirectLocation(result) shouldBe Some(routes.NewProductController.show().url)
          }
        )
      }
    }
  }

  "Sending a GET request to InvestmentGrowController when authenticated and NOT enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[InvestmentGrowModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(validInvestmentGrowModel)))
      when(mockKeyStoreConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkInvestmentGrow))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(routes.SubsidiariesNinetyOwnedController.show().toString())))
      mockNotEnrolledRequest
      showWithSessionAndAuth(InvestmentGrowControllerTest.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to InvestmentGrowController" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(InvestmentGrowControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to InvestmentGrowController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(InvestmentGrowControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to InvestmentGrowController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(InvestmentGrowControllerTest.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }
  

  "Sending a valid form submit to the InvestmentGrowController when authenticated and enrolled" should {
    "redirect to Contact Details Controller" in {
      setup(None,Some(newGeoModel),Some(newProductModel),Some(validBackLink))
      mockEnrolledRequest
      val formInput = "investmentGrowDesc" -> "some text so it's valid"
      submitWithSessionAndAuth(InvestmentGrowControllerTest.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/contact-details")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the InvestmentGrowController with no backlink when authenticated and enrolled" should {

    "return a SEE_OTHER" in {
      setup(None,Some(newGeoModel),Some(newProductModel),None)
      mockEnrolledRequest
      val formInput = "investmentGrowDesc" -> ""
      submitWithSessionAndAuth(InvestmentGrowControllerTest.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
        }
      )
    }

    "redirect to WhatWillUseFor page" in {
      setup(None,Some(newGeoModel),Some(newProductModel),None)
      mockEnrolledRequest
      val formInput = "investmentGrowDesc" -> ""
      submitWithSessionAndAuth(InvestmentGrowControllerTest.submit,formInput)(
        result => {
          redirectLocation(result) shouldBe Some("/investment-tax-relief/investment-purpose")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the InvestmentGrowController when authenticated and enrolled" should {
    "redirect to itself with errors" in {
      setup(None,Some(newGeoModel),Some(newProductModel),Some(validBackLink))
      mockEnrolledRequest
      val formInput = "investmentGrowDesc" -> ""
      submitWithSessionAndAuth(InvestmentGrowControllerTest.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending a submission to the InvestmentGrowController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(InvestmentGrowControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the GG login page with no session" in {
      submitWithoutSession(InvestmentGrowControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the InvestmentGrowController when a timeout has occured" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(InvestmentGrowControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the InvestmentGrowController when NOT enrolled" should {
    "redirect to the Subscription Service" in {
      mockNotEnrolledRequest
      submitWithSessionAndAuth(InvestmentGrowControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }
}
