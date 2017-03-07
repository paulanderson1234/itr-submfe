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

import java.net.URLEncoder

import auth.{MockAuthConnector, MockConfig}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.{EisSeisProcessingModel, PreviousSchemeModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HttpResponse

import scala.concurrent.Future

class DeletePreviousSchemeControllerSpec extends BaseSpec {

  object TestController extends DeletePreviousSchemeController {
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig = MockConfig

    override protected def authConnector = MockAuthConnector

    override lazy val s4lConnector = mockS4lConnector
  }


  "DeletePreviousSchemeController" should {
    "use the correct keystore connector" in {
      DeletePreviousSchemeController.s4lConnector shouldBe S4LConnector
    }

    "use the correct enrollment connector" in {
      DeletePreviousSchemeController.enrolmentConnector shouldBe EnrolmentConnector
    }

    "use the correct config" in {
      DeletePreviousSchemeController.applicationConfig shouldBe FrontendAppConfig
    }

    "use the correct auth connector" in {
      DeletePreviousSchemeController.authConnector shouldBe FrontendAuthConnector
    }
  }


  def setupShowMocks(previousSchemeModel: Option[Vector[PreviousSchemeModel]] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(previousSchemeModel))

  def setupSubmitMocks(previousSchemeModel: Option[Vector[PreviousSchemeModel]] = None,processingModel: Option[EisSeisProcessingModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(previousSchemeModel))
    when(mockS4lConnector.fetchAndGetFormData[EisSeisProcessingModel](Matchers.eq(KeystoreKeys.eisSeisProcessingModel))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(processingModel))
    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.eisSeisProcessingModel), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(cacheMapEisSeisProcessingModelEligible)
  }

  "Issuing a GET request to the PreviousSchemeControler when authenticated and enrolled" should {
    "return a 500 if no list of previous schemes is found" in {
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      setupShowMocks()
      showWithSessionAndAuth(TestController.show(1))(
        result => {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      )
    }
  }


  "Issuing a GET request to the PreviousSchemeControler when authenticated and enrolled" should {
    "return a 200 Ok if previous scheme id is in list of previous schemes" in {
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      setupShowMocks(Some(previousSchemeVectorList))
      showWithSessionAndAuth(TestController.show(1))(
        result => {
          status(result) shouldBe OK
        }
      )
    }
  }


  "Issuing a GET request to the PreviousSchemeControler when authenticated and enrolled" should {
    "return a 500 if previous scheme id is not in list of previous schemes" in {
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      setupShowMocks(Some(previousSchemeVectorList))
      showWithSessionAndAuth(TestController.show(2))(
        result => {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      )
    }
  }

  "Issuing a POST request to the DeletePreviousSchemeController when authenticated and enrolled" should {
    "redirect to the Review Previous Schemes page when the file is successfully deleted and the user has 1 or more schemes remaining" in {
      mockEnrolledRequest()
      setupSubmitMocks(HttpResponse(OK))
      val formInput = "scheme-id" -> schemeId
      submitWithSessionAndAuth(DeletePreviousSchemeController.submit(),formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ReviewPreviousSchemesController.show().url)
        }
      )
    }
  }

  "Issuing a POST request to the DeletePreviousSchemeController when authenticated and enrolled" should {
    "redirect to the Previous Scheme page when the file is successfully deleted and the user has no other previous investment schemes" in {
      mockEnrolledRequest()
      setupSubmitMocks(HttpResponse(OK))
      val formInput = "scheme-id" -> schemeId
      submitWithSessionAndAuth(DeletePreviousSchemeController.submit(),formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.PreviousSchemeController.show().url)
        }
      )
    }
  }

  "Issuing a POST request to the PreviousInvestmentController when authenticated and enrolled" should {
    "return an INTERNAL_SERVER_ERROR when a form with errors is posted" in {
      mockEnrolledRequest()
      val formInput = "scheme-id" -> ""
      submitWithSessionAndAuth(DeletePreviousSchemeController.submit(),formInput)(
        result => {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      )
    }
  }





}