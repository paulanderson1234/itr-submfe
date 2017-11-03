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

package controllers

import auth._
import common.KeystoreKeys
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.submission.SchemeTypesModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HttpResponse

import scala.concurrent.Future

class ConfirmDeleteApplicationControllerSpec extends BaseSpec {


  trait TestController extends ConfirmDeleteApplicationController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  object TestController extends TestController {
    override lazy val applicationConfig = MockConfig
  }

  val cacheMapSchemeTypes: CacheMap = CacheMap("", Map("" -> Json.toJson(SchemeTypesModel(eis = true))))


  def setupMocks(applicationIsInProgress: Option[Boolean]): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[Boolean](Matchers.eq(KeystoreKeys.applicationInProgress))(Matchers.any(),
      Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(applicationIsInProgress))
  }

  def setupMocksNotAvailable(): Unit = {
    when(mockRegistrationDetailsService.getRegistrationDetails(Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).
      thenReturn(Future.successful(None))
    when(mockSubscriptionService.getSubscriptionContactDetails(Matchers.any())(Matchers.any(), Matchers.any())).
      thenReturn(Future.successful(None))
  }

  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(true)))


  "ConfirmDeleteApplicationController" should {
    "use the correct auth connector" in {
      ConfirmDeleteApplicationController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      ConfirmDeleteApplicationController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      ConfirmDeleteApplicationController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct registration service" in {
      ConfirmDeleteApplicationController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct service service" in {
      ConfirmDeleteApplicationController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to ConfirmDeleteApplicationController" should {
    "return a 200 when application in progress for a seis scheme fetched from  keystore" in {
      setupMocks(applicationIsInProgress = Some(true))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe OK
        }
      )
    }
  }

  "Sending a GET request to ConfirmDeleteApplicationController" should {
    "return a 200 when application in progress for a eis scheme fetched from  keystore" in {
      setupMocks(applicationIsInProgress = Some(true))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe OK
        }
      )
    }
  }

  "Sending a GET request to ConfirmDeleteApplicationController" should {
    "return a 200 when application in progress for a eisSeis scheme fetched from  keystore" in {
      setupMocks(applicationIsInProgress = Some(true))
      mockEnrolledRequest(eisSeisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe OK
        }
      )
    }
  }

  "Sending a GET request to ConfirmDeleteApplicationController" should {
    "return a 200 when application in progress for a vct scheme fetched from  keystore" in {
      setupMocks(applicationIsInProgress = Some(true))
      mockEnrolledRequest(vctSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe OK
        }
      )
    }
  }

  "Sending a GET request to ConfirmDeleteApplicationController" should {
    "return a 200 when application in progress for a eisSeisVct scheme fetched from  keystore" in {
      setupMocks(applicationIsInProgress = Some(true))
      mockEnrolledRequest(eisSeisVctSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe OK
        }
      )
    }
  }

  "Sending a GET request to ConfirmDeleteApplicationController" should {
    "return a 200 when application in progress for a eisVct scheme fetched from  keystore" in {
      setupMocks(applicationIsInProgress = Some(true))
      mockEnrolledRequest(eisVctSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe OK
        }
      )
    }
  }


  "Sending a GET request to ConfirmDeleteApplicationController" should {
    "return a 200 when application in progress for a seisVct scheme fetched from  keystore" in {
      setupMocks(applicationIsInProgress = Some(true))
      mockEnrolledRequest(seisVctSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe OK
        }
      )
    }
  }


  "Sending a GET request to ConfirmDeleteApplicationController" should {
    "return a 303  redirect when application started is not fetched from keystore" in {
      setupMocks(Some(false))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
        }
      )
    }
  }

  "Sending a GET request to ConfirmDeleteApplicationController" should {
    "return a 303  redirect when the application is started but no scheme types model is fetched from keystore" in {
      setupMocks(Some(true))
      mockEnrolledRequest(None)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      )
    }
  }

  "Sending a GET request to ConfirmDeleteApplicationController" should {
    "return a 303  redirect when no application is started andt no scheme types model is fetched from keystore" in {
      setupMocks(Some(false))
      mockEnrolledRequest(None)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      )
    }
  }

  "Posting the confirm delete button on the ConfirmDeleteApplicationController" should {
    "redirect to hub page" in {
      when(mockS4lConnector.clearCache()(Matchers.any(), Matchers.any())).thenReturn(HttpResponse(NO_CONTENT))
      mockEnrolledRequest(seisSchemeTypesModel)
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.delete)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
        }
      )
    }
  }

}
