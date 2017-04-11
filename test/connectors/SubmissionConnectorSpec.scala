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

import auth.MockConfig
import models.{AddressModel, AnnualTurnoverCostsModel, ProposedInvestmentModel}
import play.api.test.Helpers._
import fixtures.SubmissionFixture
import models.registration.RegistrationDetailsModel
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.OneServerPerSuite
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.frontend.controller.FrontendController
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.SessionId
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

class SubmissionConnectorSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite with SubmissionFixture {

  val mockHttp : WSHttp = mock[WSHttp]
  val sessionId = UUID.randomUUID.toString

  val addressModel = AddressModel("line1", "line2",countryCode = "NZ")
  val safeID = "XA000123456789"
  val newGeographicalYes = true
  val newGeographicalNo = false
  val newProductYes = true
  val newProductNo= false
  val tradeStartDayYes = true
  val tradeStartDayNo = false
  val tradeStartMonthYes = true
  val tradeStartMonthNo = false
  val tradeStartYearYes = true
  val tradeStartYearNo = false

  object TargetSubmissionConnector extends SubmissionConnector with FrontendController {
    override val serviceUrl = MockConfig.submissionUrl
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

      val result = TargetSubmissionConnector.validateKiCostConditions(operatingCostData,operatingCostData,
        operatingCostData,rAndDCostData,rAndDCostData,rAndDCostData)
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
      val annualTurnoverCosts = AnnualTurnoverCostsModel("100","100","100","100","100","2005","2006","2007","2008","2009")

      val result = TargetSubmissionConnector.checkAveragedAnnualTurnover(proposedInvestment,annualTurnoverCosts)
      await(result) shouldBe Some(validResponse)
    }
  }

  "Calling submitAdvancedAssurance with a valid model" should {

    "return a OK" in {

      val validRequest = fullSubmissionSourceData
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK)))
      val result = TargetSubmissionConnector.submitAdvancedAssurance(validRequest, tavcReferenceId)
      await(result).status shouldBe OK
    }
  }

  "Calling submitAdvancedAssurance with a valid model but empty tavcRef" should {

    "return throw an illegal argument exception" in {

      val validRequest = fullSubmissionSourceData
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK)))

      val exception = the[IllegalArgumentException] thrownBy TargetSubmissionConnector.submitAdvancedAssurance(fullSubmissionSourceData, "")
      exception.getMessage shouldBe "requirement failed: [SubmissionConnector][submitAdvancedAssurance] An empty tavcReferenceNumber was passed"
    }
  }

  "Calling submitAdvancedAssurance with a email containing 'badrequest'" should {

    "return a BAD_REQUEST error" in {

      val request = fullSubmissionSourceData
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(BAD_REQUEST)))
      val result = TargetSubmissionConnector.submitAdvancedAssurance(request, tavcReferenceId)
      await(result).status shouldBe BAD_REQUEST
    }
  }


  "Calling submitAdvancedAssurance with a email containing 'forbidden'" should {

    "return a FORBIDDEN Error" in {

      val request = fullSubmissionSourceData
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(FORBIDDEN)))
      val result = TargetSubmissionConnector.submitAdvancedAssurance(request, tavcReferenceId)
      await(result).status shouldBe FORBIDDEN
    }
  }


  "Calling submitAdvancedAssurance with a email containing 'serviceunavailable'" should {

    "return a SERVICE UNAVAILABLE ERROR" in {

      val request = fullSubmissionSourceData
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(SERVICE_UNAVAILABLE)))
      val result = TargetSubmissionConnector.submitAdvancedAssurance(request, tavcReferenceId)
      await(result).status shouldBe SERVICE_UNAVAILABLE
    }
  }

  "Calling submitAdvancedAssurance with a email containing 'internalservererror'" should {

    "return a INTERNAL SERVER ERROR" in {

      val request = fullSubmissionSourceData
      when(mockHttp.POST[JsValue, HttpResponse](Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR)))
      val result = TargetSubmissionConnector.submitAdvancedAssurance(request, tavcReferenceId)
      await(result).status shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "Calling getRegistrationDetails with a safeID" should {

    lazy val result = TargetSubmissionConnector.getRegistrationDetails(safeID)

    "return a RegistrationDetailsModel" in {
      when(mockHttp.GET[Option[RegistrationDetailsModel]](Matchers.eq(
        s"${TargetSubmissionConnector.serviceUrl}/investment-tax-relief/registration/registration-details/safeid/$safeID"))
        (Matchers.any(),Matchers.any())).thenReturn(Some(registrationDetailsModel))
      await(result) shouldBe Some(registrationDetailsModel)
    }

  }

  "Calling checkMarketCriteria with true and true" should {

    lazy val result = TargetSubmissionConnector.checkMarketCriteria(newGeographicalYes,newProductYes)

    "return true" in {
      when(mockHttp.GET[Option[Boolean]](Matchers.eq(
        s"${TargetSubmissionConnector.serviceUrl}/investment-tax-relief/market-criteria/new-geographical/$newGeographicalYes/new-product/$newProductYes"))
        (Matchers.any(),Matchers.any())).thenReturn(Some(true))
      await(result) shouldBe Some(true)
    }

  }

  "Calling checkMarketCriteria with false and true" should {

    lazy val result = TargetSubmissionConnector.checkMarketCriteria(newGeographicalNo,newProductYes)

    "return true" in {
      when(mockHttp.GET[Option[Boolean]](Matchers.eq(
        s"${TargetSubmissionConnector.serviceUrl}/investment-tax-relief/market-criteria/new-geographical/$newGeographicalNo/new-product/$newProductYes"))
        (Matchers.any(),Matchers.any())).thenReturn(Some(true))
      await(result) shouldBe Some(true)
    }

  }

  "Calling checkMarketCriteria with true and false" should {

    lazy val result = TargetSubmissionConnector.checkMarketCriteria(newGeographicalYes,newProductNo)

    "return true" in {
      when(mockHttp.GET[Option[Boolean]](Matchers.eq(
        s"${TargetSubmissionConnector.serviceUrl}/investment-tax-relief/market-criteria/new-geographical/$newGeographicalYes/new-product/$newProductNo"))
        (Matchers.any(),Matchers.any())).thenReturn(Some(true))
      await(result) shouldBe Some(true)
    }

  }

  "Calling checkMarketCriteria with false and false" should {

    lazy val result = TargetSubmissionConnector.checkMarketCriteria(newGeographicalNo,newProductNo)

    "return false" in {
      when(mockHttp.GET[Option[Boolean]](Matchers.eq(
        s"${TargetSubmissionConnector.serviceUrl}/investment-tax-relief/market-criteria/new-geographical/$newGeographicalNo/new-product/$newProductNo"))
        (Matchers.any(),Matchers.any())).thenReturn(Some(false))
      await(result) shouldBe Some(false)
    }

  }

  "Calling validateTradeStartDateCondition" should {

    when(mockHttp.GET[Option[Boolean]](Matchers.anyString())(Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(validResponse)))

    "return a valid response" in {

      val tradeStartDay = 1
      val tradeStartMonth = 1
      val tradeStartYear = 2000

      val result = TargetSubmissionConnector.validateTradeStartDateCondition(tradeStartDay, tradeStartMonth, tradeStartYear)
      await(result) shouldBe Some(validResponse)
    }
  }



  "Calling getAASubmissionDetails" should {
    "throw an error if the TAVCRef is empty" in {
      intercept[IllegalArgumentException]{
        TargetSubmissionConnector.getAASubmissionDetails("")
      }
    }
    "return a response if a valid TAVCRef is given" in {
      when(mockHttp.GET[HttpResponse](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(HttpResponse(OK)))
      val result = TargetSubmissionConnector.getAASubmissionDetails(tavcReferenceId)
      await(result).status shouldBe OK
    }
  }

}
