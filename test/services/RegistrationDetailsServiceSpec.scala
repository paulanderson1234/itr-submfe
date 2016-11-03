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

package services

import auth.TAVCUser
import auth.ggUser.allowedAuthContext
import common.KeystoreKeys
import connectors.{S4LConnector, SubmissionConnector}
import models.{AddressModel, ContactDetailsModel}
import models.etmp.SubscriptionTypeModel
import models.registration.RegistrationDetailsModel
import org.mockito.Matchers
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class RegistrationDetailsServiceSpec extends UnitSpec with MockitoSugar with WithFakeApplication {

  val mockSubmissionConnector = mock[SubmissionConnector]
  val mockS4LConnector = mock[S4LConnector]
  val mockSubscriptionService = mock[SubscriptionService]
  val tavcRef = "XATAVC000123456"

  val addressModel = AddressModel("line1", "line2",countryCode = "NZ")
  val registrationDetailsModel = RegistrationDetailsModel("test name", addressModel)
  val subscriptionTypeModel = SubscriptionTypeModel("XA0001234567890",ContactDetailsModel("test","name",email = "test@test.com"), addressModel)

  implicit val hc = HeaderCarrier()
  implicit val user = TAVCUser(allowedAuthContext)

  object TestService extends RegistrationDetailsService {
    override lazy val submissionConnector = mockSubmissionConnector
    override lazy val s4LConnector = mockS4LConnector
    override lazy val subscriptionService = mockSubscriptionService
  }

  def setupMocks(registrationDetailsModel1: Option[RegistrationDetailsModel] = None, registrationDetailsModel2: Option[RegistrationDetailsModel] = None,
                 subscriptionTypeModel: Option[SubscriptionTypeModel] = None): Unit = {
    when(mockS4LConnector.fetchAndGetFormData[RegistrationDetailsModel](Matchers.eq(KeystoreKeys.registrationDetails))
      (Matchers.any(),Matchers.any(),Matchers.any())).thenReturn(Future.successful(registrationDetailsModel1))
    when(mockSubscriptionService.getEtmpSubscriptionDetails(Matchers.eq(tavcRef))
      (Matchers.any(), Matchers.any())).thenReturn(Future.successful(subscriptionTypeModel))
    when(mockSubmissionConnector.getRegistrationDetails(Matchers.any())
      (Matchers.any())).thenReturn(Future.successful(registrationDetailsModel2))
  }

  "RegistrationDetailsService" should {

    "Use the correct submission connector" in {
      RegistrationDetailsService.submissionConnector shouldBe SubmissionConnector
    }

    "Use the correct subscription service" in {
      RegistrationDetailsService.subscriptionService shouldBe SubscriptionService
    }

    "Use the correct save 4 later connector" in {
      RegistrationDetailsService.s4LConnector shouldBe S4LConnector
    }

  }

  "getRegistrationDetails" when {

    "The safe ID can be retrieved from the subscription service and registration details are retrieved from API call" should {

      lazy val result = TestService.getRegistrationDetails(tavcRef)

      "Return the registration details model" in {
        setupMocks(None,Some(registrationDetailsModel),Some(subscriptionTypeModel))
        await(result) shouldBe Some(registrationDetailsModel)
      }

    }

    "The safe ID can be retrieved from the subscription service and registration details are not retrieved from API call" should {

      lazy val result = TestService.getRegistrationDetails(tavcRef)

      "Return None" in {
        setupMocks(None,None,Some(subscriptionTypeModel))
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
