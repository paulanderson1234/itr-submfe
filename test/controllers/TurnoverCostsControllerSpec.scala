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

import java.net.URLEncoder

import auth.{Enrolment, Identifier, MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.{FrontendAppConfig, FrontendAuthConnector}
import connectors.{EnrolmentConnector, S4LConnector, SubmissionConnector}
import helpers.FakeRequestHelper
import models._
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.OneServerPerSuite
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future

class TurnoverCostsControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite with FakeRequestHelper {

  val mockS4lConnector = mock[S4LConnector]
  val mockSubmissionConnector = mock[SubmissionConnector]

  object TurnoverCostsControllerTest extends TurnoverCostsController {
    override lazy val applicationConfig = FrontendAppConfig
    override lazy val authConnector = MockAuthConnector
    val s4lConnector: S4LConnector = mockS4lConnector
    val submissionConnector: SubmissionConnector = mockSubmissionConnector
    override lazy val enrolmentConnector = mock[EnrolmentConnector]
  }

  private def mockEnrolledRequest = when(TurnoverCostsControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(Option(Enrolment("HMRC-TAVC-ORG", Seq(Identifier("TavcReference", "1234")), "Activated"))))

  private def mockNotEnrolledRequest = when(TurnoverCostsControllerTest.enrolmentConnector.getTAVCEnrolment(Matchers.any())(Matchers.any()))
    .thenReturn(Future.successful(None))


  val keyStoreSubsidiariesYes = SubsidiariesModel("Yes")
  val keyStoreSubsidiariesNo = SubsidiariesModel("No")
  val keyStoreSavedTurnoverCosts = AnnualTurnoverCostsModel("23", "34", "44", "66", "98")
  val keyStorePostedTurnoverCosts = AnnualTurnoverCostsModel("132", "134", "144", "166", "198")
  //val emptyModel = AnnualTurnoverCostsModel("")
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(keyStorePostedTurnoverCosts)))
  val keyStoreSavedProposedInvestment = ProposedInvestmentModel(50)


  val keyStoreSavedSubsidiariesYes = SubsidiariesModel(Constants.StandardRadioButtonYesValue)
  val keyStoreSavedSubsidiariesNo = SubsidiariesModel(Constants.StandardRadioButtonNoValue)
  val kiModel = KiProcessingModel(Some(true), Some(true), Some(true), Some(true), Some(true), Some(true))
  val nonKiModel = KiProcessingModel(Some(false), Some(false), Some(false), Some(false), Some(false), Some(false))
  val emptyKiModel = KiProcessingModel(None, None, None, None, None, None)
  val commercialSaleModel = CommercialSaleModel("true", Some(29), Some(2), Some(2004))

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockS4lConnector)
  }

  def setup(turnoverCostsModel: Option[AnnualTurnoverCostsModel], checkAveragedAnnualTurnover: Boolean): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.eq(KeystoreKeys.turnoverCosts))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(turnoverCostsModel))

    // Change to checkAveragedAnnualTurnover method below when ready and perform additional tests
    when(mockSubmissionConnector.checkLifetimeAllowanceExceeded(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())(Matchers.any()))
      .thenReturn(Future.successful(Option(checkAveragedAnnualTurnover)))
  }

  "TurnoverCostsController" should {
    "use the correct keystore connector" in {
      TurnoverCostsController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      TurnoverCostsController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      TurnoverCostsController.enrolmentConnector shouldBe EnrolmentConnector
    }
    "use the correct submission connector" in {
      TurnoverCostsController.submissionConnector shouldBe SubmissionConnector
    }
  }

  "Sending a GET formInput to TurnoverCostsController when Authenticated and enrolled" when {

    "The AnnualTurnoverCostsModel can be obtained from keystore" should {
      "return an OK" in {
        setup(Some(keyStoreSavedTurnoverCosts), true)
        mockEnrolledRequest
        showWithSessionAndAuth(TurnoverCostsControllerTest.show())(
          result => status(result) shouldBe OK
        )
      }
    }

    "The AnnualTurnoverCostsModel can't be obtained from keystore" should {
      "return an OK" in {
        setup(Some(keyStoreSavedTurnoverCosts), true)
        mockEnrolledRequest
        showWithSessionAndAuth(TurnoverCostsControllerTest.show())(
          result => status(result) shouldBe OK
        )
      }
    }

    "Sending a GET formInput to TurnoverCostsController when Authenticated and NOT enrolled" should {
      "redirect to the Subscription Service" in {
        when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMap)
        when(mockS4lConnector.fetchAndGetFormData[AnnualTurnoverCostsModel](Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedTurnoverCosts)))
        mockNotEnrolledRequest
        showWithSessionAndAuth(TurnoverCostsControllerTest.show())(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
          }
        )
      }
    }

    "Sending an Unauthenticated formInput with a session to TurnoverCostsController when Authenticated and enrolled" should {
      "return a 302 and redirect to GG login" in {
        mockEnrolledRequest
        showWithSessionWithoutAuth(TurnoverCostsControllerTest.show())(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
              URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
            }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
          }
        )
      }
    }

    "Sending a formInput with no session to TurnoverCostsController when Authenticated and enrolled" should {
      "return a 302 and redirect to GG login" in {
        mockEnrolledRequest
        showWithoutSession(TurnoverCostsControllerTest.show())(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
              URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
            }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
          }
        )
      }
    }

    "Sending a timed-out formInput to TurnoverCostsController when Authenticated and enrolled" should {
      "return a 302 and redirect to the timeout page" in {
        mockEnrolledRequest
        showWithTimeout(TurnoverCostsControllerTest.show())(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
          }
        )
      }
    }

    "Sending a valid form submission to the TurnoverCostsController when Authenticated and enrolled" should {
      "redirect to subsidiariess spending investment form when annual turnover check returns true and owns subsidiaries is true" in {
        mockEnrolledRequest

        when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMap)
        when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))
          (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedProposedInvestment)))
        when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))
          (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesYes)))
        when(mockSubmissionConnector.checkAveragedAnnualTurnover(Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(Option(true)))
        val formInput = Seq(
          "amount1" -> "100",
          "amount2" -> "100",
          "amount3" -> "100",
          "amount4" -> "100",
          "amount5" -> "100"
        )
        submitWithSessionAndAuth(TurnoverCostsControllerTest.submit, formInput: _*)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries-spending-investment")
          }
        )
      }

        "redirect to investment grow form when annual turnover check returns true and owns subsidiaries is false" in {
          mockEnrolledRequest

          when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))
            (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedProposedInvestment)))
          when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(cacheMap)
          when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))
            (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedProposedInvestment)))
          when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))
            (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
          when(mockSubmissionConnector.checkAveragedAnnualTurnover(Matchers.any(), Matchers.any())(Matchers.any()))
            .thenReturn(Future.successful(Option(true)))
          val formInput = Seq(
            "amount1" -> "100",
            "amount2" -> "100",
            "amount3" -> "100",
            "amount4" -> "100",
            "amount5" -> "100"
          )
          submitWithSessionAndAuth(TurnoverCostsControllerTest.submit, formInput: _*)(
            result => {
              status(result) shouldBe SEE_OTHER
              redirectLocation(result) shouldBe Some("/investment-tax-relief/how-plan-to-use-investment")
            }
          )
        }

      "redirect to annual turnover error page when annual turnover check returns false" in {
        mockEnrolledRequest

        when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMap)
        when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))
          (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedProposedInvestment)))
        when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))
          (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
        when(mockSubmissionConnector.checkAveragedAnnualTurnover(Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(Option(false)))
        val formInput = Seq(
          "amount1" -> "100",
          "amount2" -> "100",
          "amount3" -> "100",
          "amount4" -> "100",
          "amount5" -> "100"
        )
        submitWithSessionAndAuth(TurnoverCostsControllerTest.submit, formInput: _*)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some("/investment-tax-relief/annual-turnover-error")
          }
        )
      }

      "redirect to proposed investment page when no proposed investment is returned from keystore" in {
        mockEnrolledRequest

        when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMap)
        when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))
          (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(None))
        when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))
          (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
        val formInput = Seq(
          "amount1" -> "100",
          "amount2" -> "100",
          "amount3" -> "100",
          "amount4" -> "100",
          "amount5" -> "100"
        )
        submitWithSessionAndAuth(TurnoverCostsControllerTest.submit, formInput: _*)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some("/investment-tax-relief/proposed-investment")
          }
        )
      }

      "redirect to subsidiaries page when no subsidiaries model is returned from keystore" in {
        mockEnrolledRequest

        when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(cacheMap)
        when(mockS4lConnector.fetchAndGetFormData[ProposedInvestmentModel](Matchers.eq(KeystoreKeys.proposedInvestment))
          (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Option(keyStoreSavedProposedInvestment)))
        when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))
          (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        when(mockSubmissionConnector.checkAveragedAnnualTurnover(Matchers.any(), Matchers.any())(Matchers.any()))
          .thenReturn(Future.successful(Option(true)))
        val formInput = Seq(
          "amount1" -> "100",
          "amount2" -> "100",
          "amount3" -> "100",
          "amount4" -> "100",
          "amount5" -> "100"
        )
        submitWithSessionAndAuth(TurnoverCostsControllerTest.submit, formInput: _*)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some("/investment-tax-relief/subsidiaries")
          }
        )
      }


    }

    "Sending an invalid form submit to the TurnoverCostsController when Authenticated and enrolled" should {
      "return a bad request" in {
        when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(cacheMap)
        when(mockS4lConnector.fetchAndGetFormData[SubsidiariesModel](Matchers.eq(KeystoreKeys.subsidiaries))(Matchers.any(), Matchers.any(),Matchers.any()))
          .thenReturn(Future.successful(Option(keyStoreSavedSubsidiariesNo)))
        mockEnrolledRequest
        val formInput = Seq(
          "amount1" -> "",
          "amount2" -> "",
          "amount3" -> "",
          "amount4" -> "",
          "amount5" -> ""
        )
        submitWithSessionAndAuth(TurnoverCostsControllerTest.submit, formInput: _*)(
          result => {
            status(result) shouldBe BAD_REQUEST
          }
        )
      }
    }

    "Sending a submission to the TurnoverCostsController when not authenticated" should {

      "redirect to the GG login page when having a session but not authenticated" in {
        submitWithSessionWithoutAuth(TurnoverCostsControllerTest.submit)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
              URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
            }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
          }
        )
      }

      "redirect to the GG login page with no session" in {
        submitWithoutSession(TurnoverCostsControllerTest.submit)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some(s"${FrontendAppConfig.ggSignInUrl}?continue=${
              URLEncoder.encode(MockConfig.introductionUrl, "UTF-8")
            }&origin=investment-tax-relief-submission-frontend&accountType=organisation")
          }
        )
      }
    }
  }

  "Sending a submission to the TurnoverCostsController when a timeout has occurred" should {
    "redirect to the Timeout page when session has timed out" in {
      submitWithTimeout(TurnoverCostsControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.TimeoutController.timeout().url)
        }
      )
    }
  }

  "Sending a submission to the TurnoverCostsController when NOT enrolled" should {
    "redirect to the Subscription Service" in {
      mockNotEnrolledRequest
      submitWithSessionAndAuth(TurnoverCostsControllerTest.submit)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(FrontendAppConfig.subscriptionUrl)
        }
      )
    }
  }
}