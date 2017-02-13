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

import java.net.URLEncoder

import auth.{MockAuthConnector, MockConfig}
import common.KeystoreKeys
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.ControllerSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class ReviewPreviousSchemesControllerSpec extends ControllerSpec {

  object TestController extends ReviewPreviousSchemesController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  val previousSchemeVectorListDeleted = Vector(previousSchemeModel2, previousSchemeModel3)
  val backLink = "/investment-tax-relief/previous-investment"

  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(previousSchemeVectorList)))
  val cacheMapEmpty: CacheMap = CacheMap("", Map("" -> Json.toJson(emptyVectorList)))
  val cacheMapDeleted: CacheMap = CacheMap("", Map("" -> Json.toJson(previousSchemeVectorListDeleted)))
  val cacheMapBackLink: CacheMap = CacheMap("", Map("" -> Json.toJson(backLink)))

  val testId = 1

  "ReviewPreviousSchemesController" should {
    "use the correct keystore connector" in {
      ReviewPreviousSchemesController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      ReviewPreviousSchemesController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      ReviewPreviousSchemesController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(previousSchemes: Option[Vector[PreviousSchemeModel]] = None, backLink: Option[String] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[Vector[PreviousSchemeModel]](Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(previousSchemes))
    when(mockS4lConnector.fetchAndGetFormData[String](Matchers.eq(KeystoreKeys.backLinkReviewPreviousSchemes))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(backLink))
  }

  "Sending a GET request to ReviewPreviousSchemesController when authenticated and enrolled" should {
    "return a 200 OK when a populated vector is returned from keystore and a back link is retrieved" in {
      setupMocks(Some(previousSchemeVectorList), Some(routes.HadPreviousRFIController.show().url))
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "redirect to HadPreviousRFI when nothing is returned from keystore when authenticated and enrolled" in {
      setupMocks()
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.HadPreviousRFIController.show().url)
        }
      )
    }

    "redirect to HadPreviousRFI when no previous schemes are returned from keystore when authenticated and enrolled" in {
      setupMocks(backLink = Some(routes.HadPreviousRFIController.show().url))
      mockEnrolledRequest()
      showWithSessionAndAuth(TestController.show)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.HadPreviousRFIController.show().url)
        }
      )
    }
  }

  "Sending a GET request to ReviewPreviousSchemesController when authenticated and NOT enrolled" should {
    "redirect to the Subscription Service" in {
      setupMocks(Some(previousSchemeVectorList))
      mockNotEnrolledRequest()
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }

  "Sending an Unauthenticated request with a session to ReviewPreviousSchemesController" should {
    "return a 302 and redirect to GG login" in {
      showWithSessionWithoutAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a request with no session to ReviewPreviousSchemesController" should {
    "return a 302 and redirect to GG login" in {
      showWithoutSession(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a timed-out request to ReviewPreviousSchemesController" should {
    "return a 302 and redirect to the timeout page" in {
      showWithTimeout(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Posting to the continue button on the ReviewPreviousSchemesController when authenticated and enrolled" should {
    "redirect to 'Proposed Investment' page if table is not empty" in {
      setupMocks(Some(previousSchemeVectorList))
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/proposed-investment")
        }
      )
    }

    "redirect to itself if table is empty" in {
      setupMocks()
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/review-previous-schemes")
        }
      )
    }
  }

  "Sending a POST request to PreviousSchemeController delete method when authenticated and enrolled" should {
    "redirect to 'Review previous scheme' and delete element from vector when an element with the given processing id is found" in {
      setupMocks(Some(previousSchemeVectorList))
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMapDeleted)
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.remove(1))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/review-previous-schemes")
        }
      )
    }


    "redirect to 'Review previous scheme' and return not delete from vector when an element with the given processing id is not found" +
      "when authenticated and enrolled" in {
      setupMocks(Some(previousSchemeVectorList))
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMap)
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.remove(10))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/review-previous-schemes")
        }
      )
    }

    "redirect to 'Review previous scheme' when the vector is empty when authenticated and enrolled" in {
      setupMocks()
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMapEmpty)
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.remove(1))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/review-previous-schemes")
        }
      )
    }
  }

  "Sending a GET request to ReviewPreviousSchemeController add method when authenticated and enrolled" should {
    "redirect to the previous investment scheme page" in {
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMapBackLink)
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.add)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/previous-investment")
        }
      )
    }
  }

  "Sending a GET request to ReviewPreviousSchemeController change method when authenticated and enrolled" should {
    "redirect to the previous investment scheme page" in {
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMapBackLink)
      mockEnrolledRequest()
      submitWithSessionAndAuth(TestController.change(testId))(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/previous-investment?id=" + testId)
        }
      )
    }
  }

  "Sending a submission to the NewGeographicalMarketController when not authenticated" should {

    "redirect to the GG login page when having a session but not authenticated" in {
      submitWithSessionWithoutAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }

    "redirect to the GG login page with no session" in {
      submitWithoutSession(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
            URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
          }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
        }
      )
    }
  }

  "Sending a submission to the NewGeographicalMarketController when a timeout has occurred" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the NewGeographicalMarketController when NOT enrolled" should {
    "redirect to the Susbcription Service" in {
      mockNotEnrolledRequest()
      submitWithSessionAndAuth(TestController.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }
}
