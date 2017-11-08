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

package services

import auth.{TAVCUser, ggUser}
import common.KeystoreKeys
import connectors.{S4LConnector, SubscriptionConnector}
import controllers.helpers.FakeRequestHelper
import fixtures.SubmissionFixture
import org.mockito.Matchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.test.UnitSpec
import data.SubscriptionTestData._
import models.SubscriptionDetailsModel
import play.api.http.Status._

import scala.concurrent.Future
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse }
import uk.gov.hmrc.http.logging.SessionId

class SubscriptionServiceSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite with SubmissionFixture {

  object TargetSubscriptionService extends SubscriptionService with FrontendController with FakeRequestHelper with ServicesConfig {
    override val subscriptionConnector = mock[SubscriptionConnector]
    override val s4lConnector = mock[S4LConnector]
  }

  val internalId = "Int-312e5e92-762e-423b-ac3d-8686af27fdb5"
  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId("1234")))
  implicit val user: TAVCUser = TAVCUser(ggUser.allowedAuthContext, internalId)

  def setupMockedResponse(data: Option[HttpResponse] = None): OngoingStubbing[Future[Option[HttpResponse]]] = {
    when(TargetSubscriptionService.subscriptionConnector.getSubscriptionDetails(Matchers.eq(validTavcReference))(Matchers.any()))
      .thenReturn(Future.successful(data))
  }

  def setupMockedSaveForLaterResponse(subscriptionDetails: Option[SubscriptionDetailsModel] = None)
  :OngoingStubbing[Future[Option[SubscriptionDetailsModel]]] = {
    when(TargetSubscriptionService.s4lConnector.fetchAndGetFormData[SubscriptionDetailsModel]
      (Matchers.eq(KeystoreKeys.subscriptionDetails))(Matchers.any(), Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(subscriptionDetails))
  }

  "Calling getEtmpSubscriptionDetails" when {

    "there is data stored in S4l for the Subscription Details" should {
      lazy val result = TargetSubscriptionService.getEtmpSubscriptionDetails(validTavcReference)
      lazy val response = await(result)

      "have the expected stored contact details" in {
        setupMockedSaveForLaterResponse(Some(subscriptionDetailsFull.as[SubscriptionDetailsModel]))
        setupMockedResponse(None)
        response.get.contactDetails shouldBe expectedContactDetailsFull
      }

      "have the expected stored contact address" in {
        response.get.contactAddress shouldBe expectedContactAddressFull
      }

      "have the expected stored safeId" in {
        response.get.safeId shouldBe expectedSafeID
      }
    }

    "there is no stored data and a successful response with all data is received from DES/ETMP" should {
      lazy val result = TargetSubscriptionService.getEtmpSubscriptionDetails(validTavcReference)
      lazy val response = await(result)

      "have the expected contact details" in {
        setupMockedResponse(Some(HttpResponse(OK, Some(subscriptionTypeFull))))
        setupMockedSaveForLaterResponse()
        response.get.contactDetails shouldBe expectedContactDetailsFull
      }

      "have the expected contact address" in {
        response.get.contactAddress shouldBe expectedContactAddressFull
      }

      "have the expected safeId" in {
        response.get.safeId shouldBe expectedSafeID
      }
    }

    "there is no stored data and a successful response with the minimum data set (excludes options)" should {
      lazy val result = TargetSubscriptionService.getEtmpSubscriptionDetails(validTavcReference)
      lazy val response = await(result)

      "have the expected contact details" in {
        setupMockedResponse(Some(HttpResponse(OK, Some(subscriptionTypeMin))))
        setupMockedSaveForLaterResponse()
        response.get.contactDetails shouldBe expectedContactDetailsMin
      }

      "have the expected contact address" in {
        response.get.contactAddress shouldBe expectedContactAddressMin
      }

      "have the expected safeId" in {
        response.get.safeId shouldBe expectedSafeID
      }
    }

    "When no details are returned from the connector" should {
      lazy val result = TargetSubscriptionService.getEtmpSubscriptionDetails(validTavcReference)
      lazy val response = await(result)

      "have no subscription details returned" in {
        setupMockedResponse()
        setupMockedSaveForLaterResponse()
        response.isEmpty shouldBe true
      }
    }

    "When invalid JSON is returned from the connector" should {
      lazy val result = TargetSubscriptionService.getEtmpSubscriptionDetails(validTavcReference)
      lazy val response = await(result)

      "have no subscription details returned" in {
        setupMockedResponse(Some(HttpResponse(OK, Some(invalidSubscriptionJson))))
        setupMockedSaveForLaterResponse()
        response.isEmpty shouldBe true
      }
    }
  }
}
