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

package controllers.eisseis

import auth.{MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class PreviousSchemeControllerSpec extends BaseSpec {

  object TestController extends PreviousSchemeController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(previousSchemeVectorList)))

  def setupMocks(backLink: Option[String] = None, previousScheme: Option[PreviousSchemeModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[PreviousSchemeModel](Matchers.eq(KeystoreKeys.previousSchemes))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(previousScheme))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkPreviousScheme))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(backLink))
  }

  def setupVectorMocks(backLink: Option[String] = None, previousSchemeVectorList: Option[Vector[PreviousSchemeModel]] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(previousSchemeVectorList))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkPreviousScheme))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(backLink))
  }

  "PreviousSchemeController" should {
    "use the correct keystore connector" in {
      PreviousSchemeController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      PreviousSchemeController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      PreviousSchemeController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to PreviousSchemeController when authenticated and enrolled" should {
    "return a 200 when back link is fetched from keystore" in {
      setupMocks(Some(routes.ReviewPreviousSchemesController.show().url))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show(None))(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when None is fetched using keystore when authenticated and enrolled" in {
      setupMocks(Some(routes.ReviewPreviousSchemesController.show().url))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show(Some(1)))(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when an empty Vector List is fetched using keystore when authenticated and enrolled" in {
      setupVectorMocks(backLink = Some(routes.ReviewPreviousSchemesController.show().url))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show(Some(1)))(
        result => status(result) shouldBe OK
      )
    }

    "provide an populated model and return a 200 when model with matching Id is fetched using keystore when authenticated and enrolled" in {
      setupVectorMocks(Some(routes.ReviewPreviousSchemesController.show().url), Some(previousSchemeVectorList))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show(Some(3)))(

        result => status(result) shouldBe OK
      )
    }

    "navigate to start of flow if no back link provided even if a valid matching model returned when authenticated and enrolled" in {
      setupVectorMocks(previousSchemeVectorList = Some(previousSchemeVectorList))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show(Some(3)))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.HadPreviousRFIController.show().url)
        }
      )
    }

    "navigate to start of flow if no back link provided if a new add scheme when authenticated and enrolled" in {
      setupVectorMocks(previousSchemeVectorList = Some(previousSchemeVectorList))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show(None))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.HadPreviousRFIController.show().url)
        }
      )
    }
  }

  "Sending a valid new form submit to the PreviousSchemeController when authenticated and enrolled" should {

    "create a new item and redirect to the review previous investments page" in {
      setupVectorMocks(Some(routes.ReviewPreviousSchemesController.show().url), Some(previousSchemeVectorList))
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(cacheMap)
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      val formInput = Seq(
        "schemeTypeDesc" -> Constants.PageInvestmentSchemeAnotherValue,
        "investmentAmount" -> "12345",
        "investmentSpent" -> "",
        "otherSchemeName" -> "money making scheme",
        "investmentDay" -> "3",
        "investmentMonth" -> "8",
        "investmentYear" -> "1988",
        "processingId" -> ""
      )
      submitWithSessionAndAuth(TestController.submit, formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ReviewPreviousSchemesController.show().url)
        }
      )
    }

    "redirect to the invalid previous scheme error page if the scheme type is VCT and the user is currently eligible for SEIS" in {
      setupVectorMocks(Some(routes.ReviewPreviousSchemesController.show().url), Some(previousSchemeVectorList))
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(cacheMap)
      when(mockS4lConnector.fetchAndGetFormData[EisSeisProcessingModel](Matchers.eq(KeystoreKeys.eisSeisProcessingModel))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(eisSeisProcessingModelEligible)))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      val formInput = Seq(
        "schemeTypeDesc" -> Constants.schemeTypeVct,
        "investmentAmount" -> "12345",
        "investmentSpent" -> "",
        "otherSchemeName" -> "money making scheme",
        "investmentDay" -> "3",
        "investmentMonth" -> "8",
        "investmentYear" -> "1988",
        "processingId" -> ""
      )
      submitWithSessionAndAuth(TestController.submit, formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvalidPreviousSchemeController.show().url)
        }
      )
    }

    "redirect to the review invalid scheme page if the scheme type is VCT and the user is currently ineligible for SEIS" in {
      setupVectorMocks(Some(routes.ReviewPreviousSchemesController.show().url), Some(previousSchemeVectorList))
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(cacheMap)
      when(mockS4lConnector.fetchAndGetFormData[EisSeisProcessingModel](Matchers.eq(KeystoreKeys.eisSeisProcessingModel))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(eisSeisProcessingModelIneligibleStartDate)))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      val formInput = Seq(
        "schemeTypeDesc" -> Constants.schemeTypeVct,
        "investmentAmount" -> "12345",
        "investmentSpent" -> "",
        "otherSchemeName" -> "money making scheme",
        "investmentDay" -> "3",
        "investmentMonth" -> "8",
        "investmentYear" -> "1988",
        "processingId" -> ""
      )
      submitWithSessionAndAuth(TestController.submit, formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvalidPreviousSchemeController.show().url)
        }
      )
    }

    "redirect to the invalid previous scheme error page if the scheme type is EIS and the user is currently eligible for SEIS" in {
      setupVectorMocks(Some(routes.ReviewPreviousSchemesController.show().url), Some(previousSchemeVectorList))
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(cacheMap)
      when(mockS4lConnector.fetchAndGetFormData[EisSeisProcessingModel](Matchers.eq(KeystoreKeys.eisSeisProcessingModel))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(eisSeisProcessingModelEligible)))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      val formInput = Seq(
        "schemeTypeDesc" -> Constants.schemeTypeEis,
        "investmentAmount" -> "12345",
        "investmentSpent" -> "",
        "otherSchemeName" -> "money making scheme",
        "investmentDay" -> "3",
        "investmentMonth" -> "8",
        "investmentYear" -> "1988",
        "processingId" -> ""
      )
      submitWithSessionAndAuth(TestController.submit, formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvalidPreviousSchemeController.show().url)
        }
      )
    }

    "redirect to the invalid scheme error page if the scheme type is EIS and the user is currently ineligible for SEIS" in {
      setupVectorMocks(Some(routes.ReviewPreviousSchemesController.show().url), Some(previousSchemeVectorList))
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(cacheMap)
      when(mockS4lConnector.fetchAndGetFormData[EisSeisProcessingModel](Matchers.eq(KeystoreKeys.eisSeisProcessingModel))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(eisSeisProcessingModelIneligibleStartDate)))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      val formInput = Seq(
        "schemeTypeDesc" -> Constants.schemeTypeEis,
        "investmentAmount" -> "12345",
        "investmentSpent" -> "",
        "otherSchemeName" -> "money making scheme",
        "investmentDay" -> "3",
        "investmentMonth" -> "8",
        "investmentYear" -> "1988",
        "processingId" -> ""
      )
      submitWithSessionAndAuth(TestController.submit, formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.InvalidPreviousSchemeController.show().url)
        }
      )
    }

    "update the item and redirect to the review previous investments page" in {
      setupVectorMocks(Some(routes.ReviewPreviousSchemesController.show().url), Some(previousSchemeVectorList))
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
        .thenReturn(cacheMap)
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      val formInput = Seq(
        "schemeTypeDesc" -> Constants.PageInvestmentSchemeSeisValue,
        "investmentAmount" -> "666",
        "investmentSpent" -> "777",
        "otherSchemeName" -> "",
        "investmentDay" -> "7",
        "investmentMonth" -> "3",
        "investmentYear" -> "2015",
        "processingId" -> "5"

      )
      submitWithSessionAndAuth(TestController.submit, formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ReviewPreviousSchemesController.show().url)
        }
      )
    }
  }

  "Sending a new (processingId ==0) invalid (no amount) form submit  to the PreviousSchemeController when authenticated and enrolled" should {
    "not create the item and redirect to itself with errors as a bad request" in {
      setupVectorMocks(Some(routes.ReviewPreviousSchemesController.show().url), Some(previousSchemeVectorList))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      val formInput = Seq(
        "schemeTypeDesc" -> Constants.PageInvestmentSchemeAnotherValue,
        "investmentAmount" -> "",
        "investmentSpent" -> "",
        "otherSchemeName" -> "money making scheme",
        "investmentDay" -> "3",
        "investmentMonth" -> "8",
        "investmentYear" -> "1988",
        "processingId" -> ""
      )
      submitWithSessionAndAuth(TestController.submit, formInput:_*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

  "Sending a invalid (no amount) updated form submit to the PreviousSchemeController when authenticated and enrolled" should {
    "not update the item and redirect to itself with errors as a bad request" in {
      setupVectorMocks(Some(routes.ReviewPreviousSchemesController.show().url), Some(previousSchemeVectorList))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      val formInput = Seq(
        "schemeTypeDesc" -> Constants.PageInvestmentSchemeVctValue,
        "investmentAmount" -> "",
        "investmentSpent" -> "",
        "otherSchemeName" -> "",
        "investmentDay" -> "7",
        "investmentMonth" -> "3",
        "investmentYear" -> "2015",
        "processingId" -> "5"

      )
      submitWithSessionAndAuth(TestController.submit, formInput:_*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
