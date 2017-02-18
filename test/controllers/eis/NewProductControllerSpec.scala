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

package controllers.eis

import auth.{MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import models.{NewGeographicalMarketModel, NewProductModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.test.Helpers._

import scala.concurrent.Future

class NewProductControllerSpec extends BaseSpec {

  object TestController extends NewProductController {
    override lazy val applicationConfig = MockConfig
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
    override lazy val submissionConnector = mockSubmissionConnector
  }

  val newGeographicMarketYes = NewGeographicalMarketModel("Yes")
  val newGeographicMarketNo = NewGeographicalMarketModel("No")

  def setupSubmitMocksTrue(): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(newGeographicMarketYes)))
    when(mockSubmissionConnector.checkMarketCriteria(Matchers.any(), Matchers.any())(Matchers.any())).
      thenReturn(Future.successful(Some(true)))
  }

  def setupSubmitMocksFalse(): Unit = {
    when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
      (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(newGeographicMarketNo)))
    when(mockSubmissionConnector.checkMarketCriteria(Matchers.any(), Matchers.any())(Matchers.any())).
      thenReturn(Future.successful(Some(false)))
  }

  "NewProductController" should {
    "use the correct keystore connector" in {
      NewProductController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      NewProductController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      NewProductController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  def setupMocks(newProductModel: Option[NewProductModel] = None): Unit =
    when(mockS4lConnector.fetchAndGetFormData[NewProductModel](Matchers.eq(KeystoreKeys.newProduct))
      (Matchers.any(), Matchers.any(),Matchers.any())).thenReturn(Future.successful(newProductModel))

  "Sending a GET request to NewProductController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      setupMocks(Some(newProductMarketModelYes))
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore when authenticated and enrolled" in {
      setupMocks()
      mockEnrolledRequest(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show)(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid 'Yes' form submit to the NewProductController" when {
    "NewGeograhic is 'No' or 'Yes' and the request is authenticated and enrolled" should {

      "redirect to the annual turnover page" in {
        mockEnrolledRequest(eisSchemeTypesModel)
        setupSubmitMocksTrue()
        val formInput = "isNewProduct" -> Constants.StandardRadioButtonYesValue
        submitWithSessionAndAuth(TestController.submit, formInput)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/annual-turnover")
          }
        )
      }
    }
  }

  //TODO:
  // the No sections below will be much simplified later as they will just go to the required error page
  // (or in page javascript to make it red in which case not part of navigation at all and no controller test required)
  // The subsidiaries logic test is not required in the 3 tests below can be replaced by a single test  top the error page
  "Sending a valid 'No' form submit to the NewProductController" when {
    "NewGeograhic is 'No' and the request is authenticated and enrolled" should {

      "redirect to the annual turnover page" in {
        mockEnrolledRequest(eisSchemeTypesModel)
        setupSubmitMocksFalse()
        val formInput = "isNewProduct" -> Constants.StandardRadioButtonNoValue
        submitWithSessionAndAuth(TestController.submit, formInput)(
          result => {
            status(result) shouldBe SEE_OTHER
            redirectLocation(result) shouldBe Some("/investment-tax-relief/eis/trading-for-too-long")
          }
        )
      }
    }
  }

  "Sending a valid form submit to the NewProductController" when {
    "NewGeograhic is empty and the request is authenticated and enrolled" should {

      "output an INTERNAL_SERVER_ERROR" in {
        mockEnrolledRequest(eisSchemeTypesModel)
        when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
          (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(None))
        val formInput = "isNewProduct" -> Constants.StandardRadioButtonNoValue
        submitWithSessionAndAuth(TestController.submit, formInput)(
          result => {
            status(result) shouldBe INTERNAL_SERVER_ERROR
          }
        )
      }
    }
  }

  "Sending a valid form submit to the NewProductController" when {
    "the API response is empty and the request is authenticated and enrolled" should {

      "output an INTERNAL_SERVER_ERROR" in {
        mockEnrolledRequest(eisSchemeTypesModel)
        when(mockS4lConnector.fetchAndGetFormData[NewGeographicalMarketModel](Matchers.eq(KeystoreKeys.newGeographicalMarket))
          (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(Future.successful(Some(newGeographicMarketNo)))
        when(mockSubmissionConnector.checkMarketCriteria(Matchers.any(), Matchers.any())(Matchers.any())).
          thenReturn(Future.successful(None))
        val formInput = "isNewProduct" -> Constants.StandardRadioButtonNoValue
        submitWithSessionAndAuth(TestController.submit, formInput)(
          result => {
            status(result) shouldBe INTERNAL_SERVER_ERROR
          }
        )
      }
    }
  }

  "Sending an invalid form submission with validation errors to the NewProductController when authenticated and enrolled" should {
    "redirect to itself" in {
      mockEnrolledRequest(eisSchemeTypesModel)
      val formInput = "isNewProduct" -> ""
      submitWithSessionAndAuth(TestController.submit, formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
