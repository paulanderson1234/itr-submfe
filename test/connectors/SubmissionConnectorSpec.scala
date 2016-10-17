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

import models.{AnnualTurnoverCostsModel, ProposedInvestmentModel}
import models.submission.{AnnualCostModel, TurnoverCostModel}
import play.api.test.Helpers._
import fixtures.SubmissionFixture

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.OneServerPerSuite
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.cache.client.SessionCache
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class SubmissionConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite with SubmissionFixture {

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


  "Calling checkAveragedAnnualTurnover" should {

    when(mockHttp.GET[Option[Boolean]](Matchers.anyString())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(validResponse)))

    "return a valid response" in {

      val proposedInvestment = ProposedInvestmentModel(50)
      val annualTurnoverCosts = AnnualTurnoverCostsModel("100","100","100","100","100")

      val result = TargetSubmissionConnector.checkAveragedAnnualTurnover(proposedInvestment,annualTurnoverCosts)
      await(result) shouldBe Some(validResponse)
    }
  }

  "Calling submitAdvancedAssurance with a email with a valid model" should {

    "return a OK" in {

      val validRequest = fullSubmissionSourceData
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK)))
      val result = TargetSubmissionConnector.submitAdvancedAssurance(validRequest)
      await(result).status shouldBe OK
    }
  }

  "Calling submitAdvancedAssurance with a email containing 'badrequest'" should {

    "return a BAD_REQUEST error" in {

      val request = fullSubmissionSourceData
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(BAD_REQUEST)))
      val result = TargetSubmissionConnector.submitAdvancedAssurance(request)
      await(result).status shouldBe BAD_REQUEST
    }
  }


  "Calling submitAdvancedAssurance with a email containing 'forbidden'" should {

    "return a FORBIDDEN Error" in {

      val request = fullSubmissionSourceData
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(FORBIDDEN)))
      val result = TargetSubmissionConnector.submitAdvancedAssurance(request)
      await(result).status shouldBe FORBIDDEN
    }
  }


  "Calling submitAdvancedAssurance with a email containing 'serviceunavailable'" should {

    "return a SERVICE UNAVAILABLE ERROR" in {

      val request = fullSubmissionSourceData
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(SERVICE_UNAVAILABLE)))
      val result = TargetSubmissionConnector.submitAdvancedAssurance(request)
      await(result).status shouldBe SERVICE_UNAVAILABLE
    }
  }

  "Calling submitAdvancedAssurance with a email containing 'internalservererror'" should {

    "return a INTERNAL SERVER ERROR" in {

      val request = fullSubmissionSourceData
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))
      val result = TargetSubmissionConnector.submitAdvancedAssurance(request)
      await(result).status shouldBe INTERNAL_SERVER_ERROR
    }
  }

}
