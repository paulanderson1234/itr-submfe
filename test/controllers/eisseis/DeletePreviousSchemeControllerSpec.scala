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
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HttpResponse

import scala.concurrent.Future

class DeletePreviousSchemeControllerSpec extends BaseSpec {

  object TestController extends DeletePreviousSchemeController {
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val applicationConfig = MockConfig

    override protected def authConnector = MockAuthConnector

    override lazy val s4lConnector = mockS4lConnector
  }

  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(previousSchemeVectorList)))
  val cacheMapEisSeisProcessingModel: CacheMap = CacheMap("", Map("" -> Json.toJson(eisSeisProcessingModelEligible)))


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


  def setupMocks(previousSchemeModel: Option[Vector[PreviousSchemeModel]] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.eq(KeystoreKeys.previousSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(previousSchemeModel))
    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.previousSchemes), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(cacheMap)
    when(mockS4lConnector.fetchAndGetFormData[EisSeisProcessingModel](Matchers.eq(KeystoreKeys.eisSeisProcessingModel))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(eisSeisProcessingModelEligible)))
    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.eisSeisProcessingModel),Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(cacheMapEisSeisProcessingModel)
  }


  "Issuing a GET request to the PreviousSchemeControler when authenticated and enrolled" should {
    "return a 500 if no list of previous schemes is found" in {
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      setupMocks()
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
      setupMocks(Some(previousSchemeVectorList))
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
      setupMocks(Some(previousSchemeVectorList))
      showWithSessionAndAuth(TestController.show(2))(
        result => {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      )
    }
  }


  "Issuing a POST request to the DeletePreviousSchemeController when authenticated and enrolled" should {
    "redirect to the ReviewPreviousScheme page and delete the selected previous scheme when the scheme exists" in {
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      setupMocks(Some(previousSchemeVectorList))
      val formInput = "previousSchemeId" -> "1"
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eisseis.routes.ReviewPreviousSchemesController.show().url)
        }
      )
    }
  }

  "Issuing a POST request to the DeletePreviousSchemeController when authenticated and enrolled" should {
    "redirect to the ReviewPreviousScheme page when the scheme does not exists" in {
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      setupMocks(Some(previousSchemeVectorList))
      val formInput = "previousSchemeId" -> "2"
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eisseis.routes.ReviewPreviousSchemesController.show().url)
        }
      )
    }
  }


  "Issuing a POST request to the DeletePreviousSchemeController when authenticated and enrolled" should {
    "error out" when {
      "a minus scheme id is given" in {
        mockEnrolledRequest(eisSeisSchemeTypesModel)
        setupMocks(Some(previousSchemeVectorList))
        val formInput = "" -> "-1"
        submitWithSessionAndAuth(TestController.submit,formInput)(
          result => {
            status(result) shouldBe INTERNAL_SERVER_ERROR
          }
        )
      }
    }
  }

}