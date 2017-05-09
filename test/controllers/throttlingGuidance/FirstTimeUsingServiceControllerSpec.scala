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

import auth.MockConfig
import common.Constants
import connectors.{KeystoreConnector, ThrottleConnector}
import controllers.helpers.BaseSpec
import org.mockito.Mockito._
import play.api.http.Status.OK
import play.api.test.Helpers._
import services.{ThrottleService, TokenService}
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FirstTimeUsingServiceControllerSpec extends BaseSpec {

  object TestController extends FirstTimeUsingServiceController {
    override val keystoreConnector = mock[KeystoreConnector]
    override val tokenService =  mock[TokenService]
    override val throttleService = MyThrottleService
    override val applicationConfig = MockConfig
  }

  object MyThrottleService extends ThrottleService {
    override val throttleConnector = mock[ThrottleConnector]
    override def checkUserAccess(implicit hc: HeaderCarrier) : Future[Boolean] = {
      true
    }
  }

  implicit override val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId.toString)))

  def setupMocks(bool: Boolean): Unit = {
    when(TestController.throttleService.throttleConnector.checkUserAccess()).thenReturn(Some(bool))
    when(mockThrottleService.checkUserAccess).thenReturn(Future(bool))
    //when(TestController.tokenService.generateTemporaryToken).thenReturn(Future.successful(HttpResponse(OK)))
  }

  "Sending a GET request to FirstTimeUsingServiceController" should {
    "return a 200 OK" in {
      showWithoutSession(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "POST to the FirstTimeUsingServiceController" should {

    "redirect to Hub page" in {
      val formInput = "isFirstTimeUsingService" -> Constants.StandardRadioButtonNoValue
      submitWithoutSession(TestController.submit, formInput){
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
          }
      }
    }

    "redirect to page" in {
      setupMocks(false)
      val formInput = "isFirstTimeUsingService" -> Constants.StandardRadioButtonYesValue
      submitWithoutSession(TestController.submit, formInput){
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.routes.ApplicationHubController.show().url)
        }
      }
    }
  }
}