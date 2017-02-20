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

import auth.TAVCUser
import auth.ggUser.allowedAuthContext
import common.KeystoreKeys
import connectors.{S4LConnector, SubmissionConnector}
import models.{AddressModel, ContactDetailsModel}
import models.SubscriptionDetailsModel
import models.registration.RegistrationDetailsModel
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.scalatestplus.play.OneAppPerSuite
import play.api.libs.json.Json
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse, Upstream5xxResponse}
import uk.gov.hmrc.play.test.UnitSpec
import play.api.test.Helpers._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationDetailsServiceSpec extends UnitSpec with MockitoSugar with OneAppPerSuite {

  val mockSubmissionConnector = mock[SubmissionConnector]
  val mockS4LConnector = mock[S4LConnector]
  val mockSubscriptionService = mock[SubscriptionService]
  val tavcRef = "XATAVC000123456"
  val internalId = "Int-312e5e92-762e-423b-ac3d-8686af27fdb5"

  val minimumRegResponse = Json.parse(
    """
      |{
      |   "sapNumber": "0123456789",
      |   "safeId": "XA0001234567890",
      |   "isEditable": true,
      |   "isAnAgent": false,
      |   "isAnIndividual": false,
      |   "organisation": {
      |    "organisationName": "test name"
      |   },
      |   "addressDetails": {
      |       "addressLine1": "line1",
      |       "addressLine2": "line2",
      |       "countryCode": "NZ"
      |   },
      |   "contactDetails": {
      |   }
      |}""".stripMargin
  )

  val addressModel = AddressModel("line1", "line2",countryCode = "NZ")
  val registrationDetailsModel = RegistrationDetailsModel("test name", addressModel)
  val subscriptionTypeModel = SubscriptionDetailsModel("XA0001234567890",ContactDetailsModel("test","name",email = "test@test.com"), addressModel)
  val httpResponse = HttpResponse(OK,Some(minimumRegResponse))
  val successResponse = Future.successful(httpResponse)
  val failedResponse = Future.failed(Upstream5xxResponse("Error",INTERNAL_SERVER_ERROR,INTERNAL_SERVER_ERROR))

  implicit val hc = HeaderCarrier()
  implicit val user = TAVCUser(allowedAuthContext, internalId)

  object TestService extends RegistrationDetailsService {
    override lazy val submissionConnector = mockSubmissionConnector
    override lazy val s4lConnector = mockS4LConnector
    override lazy val subscriptionService = mockSubscriptionService
  }

  def setupMocks(registrationDetailsModel1: Option[RegistrationDetailsModel] = None, response: Future[HttpResponse] = failedResponse,
                 subscriptionTypeModel: Option[SubscriptionDetailsModel] = None): Unit = {
    when(mockS4LConnector.fetchAndGetFormData[RegistrationDetailsModel](Matchers.eq(KeystoreKeys.registrationDetails))
      (Matchers.any(),Matchers.any(),Matchers.any())).thenReturn(Future.successful(registrationDetailsModel1))
    when(mockSubscriptionService.getEtmpSubscriptionDetails(Matchers.eq(tavcRef))
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(subscriptionTypeModel))
    when(mockSubmissionConnector.getRegistrationDetails(Matchers.any())
      (Matchers.any())).thenReturn(response)
  }

  "RegistrationDetailsService" should {

    "Use the correct submission connector" in {
      RegistrationDetailsService.submissionConnector shouldBe SubmissionConnector
    }

    "Use the correct subscription service" in {
      RegistrationDetailsService.subscriptionService shouldBe SubscriptionService
    }

    "Use the correct save 4 later connector" in {
      RegistrationDetailsService.s4lConnector shouldBe S4LConnector
    }

  }

  "getRegistrationDetails" when {

    "The safe ID can be retrieved from the subscription service and registration details are retrieved from API call" should {

      lazy val result = TestService.getRegistrationDetails(tavcRef)

      "Return the registration details model" in {
        setupMocks(None,successResponse,Some(subscriptionTypeModel))
        await(result) shouldBe Some(registrationDetailsModel)
      }

    }

    "The safe ID can be retrieved from the subscription service and registration details are not retrieved from API call" should {

      lazy val result = TestService.getRegistrationDetails(tavcRef)

      "Return None" in {
        setupMocks(None,failedResponse,Some(subscriptionTypeModel))
        await(result) shouldBe None
      }

    }

    "The safe ID can't be retrieved from the subscription service" should {

      lazy val result = TestService.getRegistrationDetails(tavcRef)

      "Return None" in {
        setupMocks()
        await(result) shouldBe None
      }

    }

    "The registration details are retrieved from save 4 later" should {

      lazy val result = TestService.getRegistrationDetails(tavcRef)

      "Return the registration details model" in {
        setupMocks(Some(registrationDetailsModel))
        await(result) shouldBe Some(registrationDetailsModel)
      }

    }

  }

}
