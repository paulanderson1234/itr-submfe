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

import auth.{MockConfigSingleFlow, MockConfigEISFlow, MockAuthConnector, MockConfig}
import common.KeystoreKeys
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.submission.SchemeTypesModel
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HttpResponse
import views.html.hubPartials.{ApplicationHubExisting, ApplicationHubNew}

import scala.concurrent.Future

class ApplicationHubControllerSpec extends BaseSpec{


  trait TestController extends ApplicationHubController {
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val subscriptionService = mockSubscriptionService
    override lazy val registrationDetailsService = mockRegistrationDetailsService
  }

  object TestControllerCombined extends TestController {
    override lazy val applicationConfig = MockConfig
  }

  object TestControllerSingle extends TestController {
    override lazy val applicationConfig = MockConfigSingleFlow
  }

  object TestControllerEIS extends TestController {
    override lazy val applicationConfig = MockConfigEISFlow
  }

  val cacheMapSchemeTypes: CacheMap = CacheMap("", Map("" -> Json.toJson(SchemeTypesModel(eis = true))))


  def setupMocks(bool: Option[Boolean]): Unit = {
    when(mockRegistrationDetailsService.getRegistrationDetails(Matchers.any())(Matchers.any(),Matchers.any(), Matchers.any())).
      thenReturn(Future.successful(Some(registrationDetailsModel)))
    when(mockSubscriptionService.getSubscriptionContactDetails(Matchers.any())(Matchers.any(),Matchers.any())).
      thenReturn(Future.successful(Some(contactDetailsModel)))
    when(mockS4lConnector.fetchAndGetFormData[Boolean](Matchers.eq(KeystoreKeys.applicationInProgress))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(bool))
    when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(seisSchemeTypesModel))
    when(mockS4lConnector.saveFormData(Matchers.eq(KeystoreKeys.selectedSchemes), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(cacheMapSchemeTypes)
  }

  def setupMocksNotAvailable(): Unit = {
    when(mockRegistrationDetailsService.getRegistrationDetails(Matchers.any())(Matchers.any(),Matchers.any(), Matchers.any())).
      thenReturn(Future.successful(None))
    when(mockSubscriptionService.getSubscriptionContactDetails(Matchers.any())(Matchers.any(),Matchers.any())).
      thenReturn(Future.successful(None))
  }



  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(true)))


  "ApplicationHubController" should {
    "use the correct auth connector" in {
      ApplicationHubController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct keystore connector" in {
      ApplicationHubController.s4lConnector shouldBe S4LConnector
    }
    "use the correct enrolment connector" in {
      ApplicationHubController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct registration service" in {
      ApplicationHubController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct service service" in {
      ApplicationHubController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to ApplicationHubController when authenticated and enrolled" should {
    "return a 200 when true is fetched from keystore" in {
      setupMocks(Some(true))
      mockEnrolledRequest()
      showWithSessionAndAuth(TestControllerCombined.show())(
        result => {
          status(result) shouldBe OK
          contentAsString(result) contains ApplicationHubExisting
        }
      )
    }

    "return a 200 when false is fetched from keystore" in {
      setupMocks(Some(false))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestControllerCombined.show())(
        result => {
          status(result) shouldBe OK
          val document = Jsoup.parse(contentAsString(result))
          contentAsString(result) contains ApplicationHubNew
        }
      )
    }

    "return a 200 when nothing is fetched from keystore" in {
      setupMocks(None)
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestControllerCombined.show())(
        result => {
          status(result) shouldBe OK
          val document = Jsoup.parse(contentAsString(result))
          contentAsString(result) contains ApplicationHubNew
        }
      )
    }

    "return a 500 when an ApplicationHubModel cannot be composed" in {
      setupMocksNotAvailable()
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestControllerCombined.show())(
        result => {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      )
    }
  }

  "Posting to the 'create new application' button on the ApplicationHubController when authenticated and enrolled" should {
    "redirect to 'scheme selections' page if eisSeisFlowEnabled is enabled" in {
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(CacheMap("", Map())))
      mockEnrolledRequest(None)
      submitWithSessionAndAuth(TestControllerCombined.newApplication)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.schemeSelection.routes.SchemeSelectionController.show().url)
        }
      )
    }
  }

  "Posting to the 'create new application' button on the ApplicationHubController when authenticated and enrolled" should {
    "redirect to 'scheme selection' page if eisSeisFlowEnabled is disabled but seisFlowEnabled is enabled" in {
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(CacheMap("", Map())))
      mockEnrolledRequest(None)
      submitWithSessionAndAuth(TestControllerSingle.newApplication)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.schemeSelection.routes.SingleSchemeSelectionController.show().url)
        }
      )
    }
  }

  "Posting to the 'create new application' button on the ApplicationHubController when authenticated and enrolled" should {
    "redirect to 'nature of business' page in EIS flow if neither seisFlowEnabled or eisSeisFlowEnabled are true" in {
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(CacheMap("", Map())))
      mockEnrolledRequest(None)
      submitWithSessionAndAuth(TestControllerEIS.newApplication)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eis.routes.NatureOfBusinessController.show().url)
        }
      )
    }
  }

  "Sending a POST request to ApplicationHubController delete method when authenticated and enrolled" should {
    "redirect to itself and delete the application currently in progress" in {
      when(mockS4lConnector.clearCache()(Matchers.any(),Matchers.any())).thenReturn(HttpResponse(NO_CONTENT))
      mockEnrolledRequest(eisSchemeTypesModel)
      submitWithSessionAndAuth(TestControllerCombined.delete)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.ApplicationHubController.show().url)
        }
      )
    }
  }
  
}
