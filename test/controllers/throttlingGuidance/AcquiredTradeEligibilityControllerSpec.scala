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

package controllers.throttlingGuidance

import common.{Constants, KeystoreKeys}
import connectors.KeystoreConnector
import controllers.helpers.BaseSpec
import models.throttlingGuidance.{AcquiredTradeEligibilityModel, GroupsAndSubsEligibilityModel, IsAgentModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import services.TokenService
import uk.gov.hmrc.play.http.HttpResponse

import scala.concurrent.Future

class AcquiredTradeEligibilityControllerSpec extends BaseSpec {

  object TestController extends AcquiredTradeEligibilityController {
    override val keystoreConnector: KeystoreConnector = mock[KeystoreConnector]
    override val tokenService: TokenService = mock[TokenService]
  }


  def setupMocks(throttleCheckPassed: Option[Boolean], acquiredTradeEligibilityModel: Option[AcquiredTradeEligibilityModel] = None): Unit = {
    when(TestController.keystoreConnector.fetchAndGetFormData[Boolean](Matchers.eq(KeystoreKeys.throttleCheckPassed))
      (Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(throttleCheckPassed))
    when(TestController.keystoreConnector.fetchAndGetFormData[AcquiredTradeEligibilityModel](Matchers.eq(KeystoreKeys.acquiredTradeEligibility))
      (Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(acquiredTradeEligibilityModel))

  }

  def setupSubmissionMocks(isAgent: Option[IsAgentModel], isGroup: Option[GroupsAndSubsEligibilityModel] = None, tok:
  String): Unit = {
    when(TestController.keystoreConnector.fetchAndGetFormData[IsAgentModel](Matchers.eq(KeystoreKeys.isAgentEligibility))
      (Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(isAgent))
    when(TestController.keystoreConnector.fetchAndGetFormData[GroupsAndSubsEligibilityModel](Matchers.eq(KeystoreKeys.groupsAndSubsEligibility))
      (Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(isGroup))
    when(TestController.tokenService.generateTemporaryToken(Matchers.any())).thenReturn(Future.successful(tok))


  }




  "THe AcquiredTradeEligibilityController" should {
    "use the correct keystore connector" in {
      AcquiredTradeEligibilityController.keystoreConnector shouldBe KeystoreConnector
    }
    "use the correct token service" in {
      AcquiredTradeEligibilityController.tokenService shouldBe TokenService
    }
  }

  "Sending a GET request to AcquiredTradeEligibilityController" should {
    "return a 200 when something is fetched from keystore and the throttle check is passed" in {
      setupMocks(Some(true), Some(acquiredTradeYes))
      showWithSessionWithoutAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore and the throttle check is passed" in {
      setupMocks(Some(true), None)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "redirect to the first guidance page when the throttlecheck is false" in {
      setupMocks(Some(false), None)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.throttlingGuidance.routes.OurServiceChangeController.show().url)
        }
      )
    }

    "redirect to the first guidance page when the throttlecheck is not found" in {
      setupMocks(None, None)
      showWithSessionAndAuth(TestController.show())(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.throttlingGuidance.routes.OurServiceChangeController.show().url)
        }
      )
    }
  }

  "Sending a valid Yes form submission to the AcquiredTradeEligibilityController" should {
    "redirect to an error page" in {
      val formInput = "acquiredTrade" -> Constants.StandardRadioButtonYesValue
      submitWithSessionWithoutAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.throttlingGuidance.routes.IsAcquiredTradeErrorController.show().url)
        }
      )
    }
  }

  "Sending a valid No form submission to the AcquiredTradeEligibilityController" should {
    val formInput = "acquiredTrade" -> Constants.StandardRadioButtonNoValue
    "redirect to the first eligibility page when there is no agent or groups eligibility answers found in keystore" in {
      setupSubmissionMocks(None, None, tokenId)
      submitWithSessionWithoutAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.throttlingGuidance.routes.IsAgentController.show().url)
        }
      )
    }
    "redirect to the first eligibility page when there is no agent eligibility answer found in keystore" in {
      setupSubmissionMocks(None, Some(groupOrSubNo), tokenId)
      submitWithSessionWithoutAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.throttlingGuidance.routes.IsAgentController.show().url)
        }
      )
    }
    "redirect to the first eligibility page when there is no groups eligibility answer found in keystore" in {
      setupSubmissionMocks(Some(isAgentModelNo), None, tokenId)
      submitWithSessionWithoutAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.throttlingGuidance.routes.IsAgentController.show().url)
        }
      )
    }
    "redirect to the first eligibility page when the answer to agent eligibility and groups and subs eligibility is 'Yes'" in {
      setupSubmissionMocks(Some(isAgentModelYes), Some(groupOrSubYes), tokenId)
      submitWithSessionWithoutAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.throttlingGuidance.routes.IsAgentController.show().url)
        }
      )
    }
    "redirect to the first eligibility page when the answer to agent eligibility is 'Yes'" in {
      setupSubmissionMocks(Some(isAgentModelYes), Some(groupOrSubNo), tokenId)
      submitWithSessionWithoutAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.throttlingGuidance.routes.IsAgentController.show().url)
        }
      )
    }
    "redirect to the first eligibility page when the answer to groups and subs eligibility is 'Yes'" in {
      setupSubmissionMocks(Some(isAgentModelNo), Some(groupOrSubYes), tokenId)
      submitWithSessionWithoutAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.throttlingGuidance.routes.IsAgentController.show().url)
        }
      )
    }
    "redirect to the  hub when all eligibility checks are passed and a token is successfully generated" in {
      setupSubmissionMocks(Some(isAgentModelNo), Some(groupOrSubNo), tokenId)
      submitWithSessionWithoutAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show(Some(tokenId)).url)
        }
      )
    }
    "Internal Server Error when the eligibility checks are passed but an empty token is generated" in {
      setupSubmissionMocks(Some(isAgentModelNo), Some(groupOrSubNo),"")
      submitWithSessionWithoutAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe INTERNAL_SERVER_ERROR
        }
      )
    }
    }

  "Sending an invalid form submission with validation errors to the AcquiredTradeEligibilityController when authenticated and enrolled" should {
    "redirect to itself" in {
      val formInput = "acquiredTrade" -> ""
      submitWithSessionWithoutAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
