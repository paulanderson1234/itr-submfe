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

package controllers.eisseis

import auth.{MockConfigSingleFlow, MockAuthConnector, MockConfig}
import common.{Constants, KeystoreKeys}
import config.FrontendAuthConnector
import connectors.{EnrolmentConnector, S4LConnector}
import controllers.helpers.BaseSpec
import controllers.schemeSelection.SingleSchemeSelectionController
import models._
import models.submission.SchemeTypesModel
import org.mockito.Matchers
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class SingleSchemeSelectionControllerSpec extends BaseSpec {

  object TestController extends SingleSchemeSelectionController {
    override lazy val applicationConfig = MockConfigSingleFlow
    override lazy val authConnector = MockAuthConnector
    override lazy val s4lConnector = mockS4lConnector
    override lazy val enrolmentConnector = mockEnrolmentConnector
  }

  val cacheMapSchemeTypesEis: CacheMap = CacheMap("", Map("" -> Json.toJson(SchemeTypesModel(eis = true))))
  val cacheMapSchemeTypesSeis: CacheMap = CacheMap("", Map("" -> Json.toJson(SchemeTypesModel(seis = true))))
  val cacheMapSchemeTypesVct: CacheMap = CacheMap("", Map("" -> Json.toJson(SchemeTypesModel(vct = true))))

  "SingleSchemeSelectionController" should {
    "use the correct keystore connector" in {
      SingleSchemeSelectionController.s4lConnector shouldBe S4LConnector
    }
    "use the correct auth connector" in {
      SingleSchemeSelectionController.authConnector shouldBe FrontendAuthConnector
    }
    "use the correct enrolment connector" in {
      SingleSchemeSelectionController.enrolmentConnector shouldBe EnrolmentConnector
    }
  }

  "Sending a GET request to SingleSchemeSelectionController when authenticated and enrolled" should {
    "return a 200 when something is fetched from keystore" in {
      mockEnrolledRequest(None)
      when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(eisSchemeTypesModel)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      mockEnrolledRequest(None)
      when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(None)
      showWithSessionAndAuth(TestController.show())(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a valid 'EIS' form submit to the SingleSchemeSelectionController when authenticated and enrolled" should {
    "redirect to review schemes page" in {
      mockEnrolledRequest(None)
      when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(eisSchemeTypesModel)
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(cacheMapSchemeTypesEis)
      val formInput = "singleSchemeSelection" -> Constants.schemeTypeEis
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eis.routes.NatureOfBusinessController.show().url)
        }
      )
    }
  }

  "Sending a valid 'SEIS' form submit to the SingleSchemeSelectionController when authenticated and enrolled" should {
    "redirect to review schemes page" in {
      mockEnrolledRequest(None)
      when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(seisSchemeTypesModel)
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(cacheMapSchemeTypesSeis)
      val formInput = "singleSchemeSelection" -> Constants.schemeTypeSeis
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.seis.routes.NatureOfBusinessController.show().url)
        }
      )
    }
  }

  "Sending a valid 'VCT' form submit to the SingleSchemeSelectionController when authenticated and enrolled" should {
    "redirect to review schemes page" in {
      mockEnrolledRequest(None)
      when(mockS4lConnector.fetchAndGetFormData[SchemeTypesModel](Matchers.eq(KeystoreKeys.selectedSchemes))
        (Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(vctSchemeTypesModel)
      when(mockS4lConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(cacheMapSchemeTypesVct)
      val formInput = "singleSchemeSelection" -> Constants.schemeTypeVct
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(controllers.eis.routes.NatureOfBusinessController.show().url)
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the SingleSchemeSelectionController when authenticated and enrolled" should {
    "redirect to itself" in {
      mockEnrolledRequest(None)
      val formInput = "singleSchemeSelection" -> ""
      submitWithSessionAndAuth(TestController.submit,formInput)(
        result => {
          status(result) shouldBe BAD_REQUEST
        }
      )
    }
  }
}
