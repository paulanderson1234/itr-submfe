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

package controllers

import java.time.ZoneId
import java.util.{Date, UUID}

import builders.SessionBuilder
import connectors.KeystoreConnector
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future

class CheckAnswersControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object CheckAnswersControllerTest extends CheckAnswersController {
  }

  val checkAnswersModel = new CheckAnswersModel(YourCompanyNeedModel(""), TaxpayerReferenceModel(""), RegisteredAddressModel(""),
    DateOfIncorporationModel(Some(1), Some(1), Some(1990)), NatureOfBusinessModel(""), CommercialSaleModel("No", None, None, None),
    Some(IsKnowledgeIntensiveModel("")), Some(OperatingCostsModel("", "", "", "", "", "")), Some(PercentageStaffWithMastersModel("")),
    Some(TenYearPlanModel("", None)), Some(SubsidiariesModel("")), HadPreviousRFIModel(""), ProposedInvestmentModel(0), WhatWillUseForModel(""),
    Some(UsedInvestmentReasonBeforeModel("")), Some(PreviousBeforeDOFCSModel("")), Some(NewGeographicalMarketModel("")),
    Some(SubsidiariesSpendingInvestmentModel("")), Some(SubsidiariesNinetyOwnedModel("")), InvestmentGrowModel(""))

  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = CheckAnswersControllerTest.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = CheckAnswersControllerTest.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "Sending a GET request to CommercialSaleController" should {
    "return a 200 when the page is loaded" in {
      showWithSession(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid Yes form submission to the CommercialSaleController" should {
    "redirect to the subsidiaries page if date of incorporation is exactly 3 years from today" in {
      val request = FakeRequest().withFormUrlEncodedBody()
      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/check-your-answers")
        }
      )
    }
  }
}
