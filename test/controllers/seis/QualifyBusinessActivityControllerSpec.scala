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

package controllers.seis

import auth.{MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class QualifyBusinessActivityControllerSpec extends BaseSpec {

  object TestController extends QualifyBusinessActivityController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  "QualifyBusinessActivityController" should {
    "use the correct keystore connector" in {
      QualifyBusinessActivityController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      QualifyBusinessActivityController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      QualifyBusinessActivityController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(qualifyBusinessActivityModel: Option[QualifyBusinessActivityModel] = None): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[QualifyBusinessActivityModel](Matchers.eq(KeystoreKeys.isQualifyBusinessActivity))
      (Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(qualifyBusinessActivityModel))

  }

  "Sending a GET request to QualifyBusinessActivityController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(qualifyPrepareToTrade))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupMocks(None)
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid Yes form submission to the QualifyBusinessActivityController when authenticated and enrolled" should {
    "redirect to the HadPreviousRFI page" in {
      val formInput = "isQualifyBusinessActivity" -> Constants.qualifyPrepareToTrade
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.HadPreviousRFIController.show().url)
        }
      )
    }
  }

  "Sending a valid Yes form submission to the QualifyBusinessActivityController when authenticated and enrolled" should {
    "redirect to the NotFirstTrade page" in {
      val formInput = "isQualifyBusinessActivity" -> Constants.qualifyResearchAndDevelopment
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
     // To navigate to NotFirstTradeError page
          redirectLocation(result) shouldBe Some(controllers.seis.routes.NotFirstTradeController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the QualifyBusinessActivityController when authenticated and enrolled" should {
    "redirect to itself" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = "isFirstTrade" -> ""
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
