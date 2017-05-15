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

package testOnly.controllers

import controllers.helpers.BaseSpec
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._
import testOnly.connectors.ResetTokenConnector
import uk.gov.hmrc.play.http.HttpResponse
import scala.concurrent.Future

class ResetTokenControllerSpec extends BaseSpec {

  val mockResetTokenConnector = mock[ResetTokenConnector]

  object TestController extends ResetTokenController {
    override lazy val resetTokenConnector = mockResetTokenConnector
  }

  "resetTokenController" should {
    "Use the correct s4l connector" in {
      TestController.resetTokenConnector shouldBe mockResetTokenConnector
    }
  }

  def setupMocks(result: Int) : Unit = {
    when(TestController.resetTokenConnector.resetTokens()
    (Matchers.any())).thenReturn(Future.successful(HttpResponse(result)))
  }

  "resetTokenController.resetToken" when {
    "returns an OK " should {
      "Return OK" in {
        setupMocks(OK)
        showWithSessionAndAuth(TestController.resetTokens())(
          result => status(result) shouldBe OK
        )
      }

      "Output a string response" in {
        setupMocks(OK)
        showWithSessionAndAuth(TestController.resetTokens())(
          result => contentAsString(result) shouldBe "Successfully reset tokens"
        )
      }

    }

    "returns a BAD_REQUEST" should {
      "Return BadRequest" in {
        setupMocks(BAD_REQUEST)
        showWithSessionAndAuth(TestController.resetTokens())(
          result => status(result) shouldBe BAD_REQUEST
        )
      }

      "Output a string response" in {
        setupMocks(BAD_REQUEST)
        showWithSessionAndAuth(TestController.resetTokens())(
          result => contentAsString(result) shouldBe "Failed to reset tokens"
        )
      }

    }

  }

}
