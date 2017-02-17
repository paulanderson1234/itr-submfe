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
import common.KeystoreKeys
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class DateOfIncorporationControllerSpec extends BaseSpec {

  object DateOfIncorporationControllerTest extends DateOfIncorporationController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  def setupMocks(dateOfIncorporationModel: Option[DateOfIncorporationModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
      .thenReturn(Future.successful(dateOfIncorporationModel))

  "DateOfIncorporationController" should {
    "use the correct keystore connector" in {
      DateOfIncorporationController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      DateOfIncorporationController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      DateOfIncorporationController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to DateOfIncorporationController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(dateOfIncorporationModel))
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(DateOfIncorporationControllerTest.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      setupMocks()
      mockEnrolledRequest(seisSchemeTypesModel)
      showWithSessionAndAuth(DateOfIncorporationControllerTest.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid form submit to the DateOfIncorporationController when authenticated and enrolled" should {
    "redirect to first trade start date page" in {
      when(mockS4lConnector.fetchAndGetFormData[KiProcessingModel](Matchers.eq(KeystoreKeys.kiProcessingModel))
        (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(Option(kiProcessingModelMet)))
      setupMocks(Some(dateOfIncorporationModel))
      mockEnrolledRequest(seisSchemeTypesModel)

      val formInput = Seq(
        "incorporationDay" -> "23",
        "incorporationMonth" -> "11",
        "incorporationYear" -> "1993")

      submitWithSessionAndAuth(DateOfIncorporationControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TradeStartDateController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the DateOfIncorporationController when authenticated and enrolled" should {
    "return a bad request" in {
      mockEnrolledRequest(seisSchemeTypesModel)
      val formInput = Seq(
        "incorporationDay" -> "",
        "incorporationMonth" -> "",
        "incorporationYear" -> "")

      submitWithSessionAndAuth(DateOfIncorporationControllerTest.submit,formInput:_*)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }

}
