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

/**
  * Copyright 2016 HM Revenue & Customs
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package connectors

import java.util.UUID
import play.api.test.Helpers._
import common.Constants
import models.{YourCompanyNeedModel, ContactDetailsModel, SubmissionRequest, SubmissionResponse}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.OneServerPerSuite
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.{Json, JsValue}
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class SubmissionConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

 // val mockHttp = mock[HttpGet with HttpPost with HttpPut]
 val mockHttp : WSHttp = mock[WSHttp]
  val mockSessionCache = mock[SessionCache]
  val sessionId = UUID.randomUUID.toString


  object TargetSubmissionConnector extends SubmissionConnector with FrontendController {
    override val sessionCache = mockSessionCache
    override val serviceUrl = "dummy"
    override val http = mockHttp

  }

  val validResponse = true
  val trueResponse = true
  val falseResponse = false
  val dummySubmissionRequestModelValid = SubmissionRequest(ContactDetailsModel("James", "Harris", "0872990915","harris@gmail.com"),YourCompanyNeedModel("AA"))
  val dummySubmissionRequestModelBad = SubmissionRequest(ContactDetailsModel("James", "Harris", "0872990915","harris@badrequest.com"),YourCompanyNeedModel("AA"))
  val dummySubmissionRequestModelInternalServerError = SubmissionRequest(ContactDetailsModel("James", "Harris", "0872990915","harris@internalservererrorrequestgmail.com"),YourCompanyNeedModel("AA"))
  val dummySubmissionRequestModelForbidden = SubmissionRequest(ContactDetailsModel("James", "Harris", "0872990915","harris@forbiddengmail.com"),YourCompanyNeedModel("AA"))
  val dummySubmissionRequestModelServiceUnavailable = SubmissionRequest(ContactDetailsModel("James", "Harris", "0872990915","harris@serviceunavailablerequestgmail.com"),YourCompanyNeedModel("AA"))
  val dummySubmissionResponseModel = SubmissionResponse(true,"FBUND93821077","Submission Request Successful")

  implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(sessionId.toString)))

  "Calling validateKiCostConditions" should {

    when(mockHttp.GET[Option[Boolean]](Matchers.anyString())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(validResponse)))

    "return a valid response" in {

      val operatingCostData: Int = 1000
      val rAndDCostData: Int = 100

      val result = TargetSubmissionConnector.validateKiCostConditions(operatingCostData,operatingCostData,operatingCostData,rAndDCostData,rAndDCostData,rAndDCostData)
      await(result) shouldBe Some(trueResponse)
    }
  }

  "Calling validateSecondaryKiConditions" should {

    when(mockHttp.GET[Option[Boolean]](Matchers.anyString())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(validResponse)))

    "return a valid response" in {

      val hasPercentageWithMasters: Boolean = true
      val hasTenYearPlan: Boolean = true

      val result = TargetSubmissionConnector.validateSecondaryKiConditions(hasPercentageWithMasters,hasTenYearPlan)
      await(result) shouldBe Some(trueResponse)
    }
  }

  "Calling checkLifetimeAllowanceExceeded" should {

    when(mockHttp.GET[Option[Boolean]](Matchers.anyString())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(validResponse)))

    "return a valid response" in {

      val hadPrevRFI = true
      val isKi = true
      val previousInvestmentSchemesTotal= 1000
      val proposedAmount = 1000

      val result = TargetSubmissionConnector.checkLifetimeAllowanceExceeded(hadPrevRFI, isKi, previousInvestmentSchemesTotal, proposedAmount)
      await(result) shouldBe Some(validResponse)
    }
  }

  "Calling submitAdvancedAssurance with a email with a valid model" should {

    "return a OK" in {

      val validRequest = dummySubmissionRequestModelValid
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK)))
      val result = TargetSubmissionConnector.submitAdvancedAssurance(validRequest)
      await(result).status shouldBe OK
    }
  }

  "Calling submitAdvancedAssurance with a email containing 'badrequest'" should {

    "return a BAD_REQUEST error" in {

      val badRequest = dummySubmissionRequestModelBad
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(BAD_REQUEST)))
      val result = TargetSubmissionConnector.submitAdvancedAssurance(badRequest)
      await(result).status shouldBe BAD_REQUEST
    }
  }


  "Calling submitAdvancedAssurance with a email containing 'forbidden'" should {

    "return a FORBIDDEN Error" in {

      val forbiddenRequest = dummySubmissionRequestModelForbidden
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(FORBIDDEN)))
      val result = TargetSubmissionConnector.submitAdvancedAssurance(forbiddenRequest)
      await(result).status shouldBe FORBIDDEN
    }
  }


  "Calling submitAdvancedAssurance with a email containing 'serviceunavailable'" should {

    "return a SERVICE UNAVAILABLE ERROR" in {

      val unavailableRequest = dummySubmissionRequestModelServiceUnavailable
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(SERVICE_UNAVAILABLE)))
      val result = TargetSubmissionConnector.submitAdvancedAssurance(unavailableRequest)
      await(result).status shouldBe SERVICE_UNAVAILABLE
    }
  }

  "Calling submitAdvancedAssurance with a email containing 'internalservererror'" should {

    "return a INTERNAL SERVER ERROR" in {

      val internalErrorRequest = dummySubmissionRequestModelInternalServerError
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))
      val result = TargetSubmissionConnector.submitAdvancedAssurance(internalErrorRequest)
      await(result).status shouldBe INTERNAL_SERVER_ERROR
    }
  }


}
