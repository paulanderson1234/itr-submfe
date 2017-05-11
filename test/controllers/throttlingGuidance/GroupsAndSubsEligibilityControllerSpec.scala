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
import models.throttlingGuidance.GroupsAndSubsEligibilityModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class GroupsAndSubsEligibilityControllerSpec extends BaseSpec {

  object TestController extends GroupsAndSubsEligibilityController {
    override val keystoreConnector: KeystoreConnector = mock[KeystoreConnector]
  }


  def setupMocks(groupsAndSubsEligibilityModel: Option[GroupsAndSubsEligibilityModel] = None): Unit = {
    when(TestController.keystoreConnector.fetchAndGetFormData[GroupsAndSubsEligibilityModel](Matchers.eq(KeystoreKeys.groupsAndSubsEligibility))
      (Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(groupsAndSubsEligibilityModel))

  }

  "THe GroupsAndSubsEligibilityController" should {
    "use the correct keystore connector" in {
      GroupsAndSubsEligibilityController.keystoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to GroupsAndSubsEligibilityController" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(groupOrSubYes))
      showWithSessionWithoutAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupMocks(None)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid Yes form submission to the GroupsAndSubsEligibilityController" should {
    "redirect to an error page" in {
      val formInput = "isGroupOrSub" -> Constants.StandardRadioButtonYesValue
      submitWithSessionWithoutAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.throttlingGuidance.routes.IsGroupErrorController.show().url)
        }
      )
    }
  }

  "Sending a valid No form submission to the GroupsAndSubsEligibilityController" should {
    "redirect to the 'acquired trade' page" in {
      val formInput = "isGroupOrSub" -> Constants.StandardRadioButtonNoValue
      submitWithSessionWithoutAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.AcquiredTradeEligibilityController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the GroupsAndSubsEligibilityController when authenticated and enrolled" should {
    "redirect to itself" in {
      val formInput = "isGroupOrSub" -> ""
      submitWithSessionWithoutAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
