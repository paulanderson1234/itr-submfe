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
import models.throttlingGuidance.IsAgentModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class IsAgentControllerSpec extends BaseSpec {

  object TestController extends IsAgentController {

    override lazy val keystoreConnector = mockKeystoreConnector

  }

  //override val keystoreConnector = KeystoreConnector
  "IsAgentController" should {
    "use the correct keystore connector" in {
      TestController.keystoreConnector shouldBe mockKeystoreConnector
    }
  }

  def setupMocks(isAgentModel: Option[IsAgentModel] = None): Unit = {
    when(TestController.keystoreConnector.fetchAndGetFormData[IsAgentModel](Matchers.eq(KeystoreKeys.isAgentEligibility))
      (Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(isAgentModel))

    when(mockKeystoreConnector.saveFormData(Matchers.eq(KeystoreKeys.isAgentEligibility), Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(CacheMap("", Map())))

  }

  "Sending a GET request to IsAgentController" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(isAgentModelYes))
      showWithSessionWithoutAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is found in keystore" in {
      setupMocks(None)
      showWithSessionWithoutAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid Yes form submission to the IsAgentController" should {
    "redirect to the error page" in {
      val formInput = "isAgent" -> Constants.StandardRadioButtonYesValue
      setupMocks()
      submitWithSessionWithoutAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.throttlingGuidance.routes.IsAgentErrorController.show().url)
        }
      )
    }
  }

  "Sending a valid No form submission to the IsAgentController" should {
    "redirect to the groups and subs page" in {
      val formInput = "isAgent" -> Constants.StandardRadioButtonNoValue
      setupMocks()
      submitWithSessionWithoutAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eligibility.routes.GroupsAndSubsEligibilityController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the IsAgentController" should {
    "redirect to itself" in {
      val formInput = "isAgent" -> ""
      submitWithSessionWithoutAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
