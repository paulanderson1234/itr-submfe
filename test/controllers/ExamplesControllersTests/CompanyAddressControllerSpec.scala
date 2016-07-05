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

import java.util.UUID

import builders.SessionBuilder
import connectors.KeystoreConnector
import controllers.examples.CompanyAddressController
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
import uk.gov.hmrc.play.http.{HeaderCarrier}
import uk.gov.hmrc.play.test.UnitSpec
import org.scalatest.mock.MockitoSugar

import scala.concurrent.Future

class CompanyAddressControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object CompanyAddressControllerTest extends CompanyAddressController {
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  val addressAsJson = """{"lines":["Flat 1","Some Street 1","Some Place 1"],"town":"Some Town 1","postcode":"ZE99 1XZ","country":{"code":"GB","name":"UK"}}"""

  val model = CompanyAddressModel("line 1", "line 2", "line 3", "line 4", "TF1 3NY", "")
  val emptyModel = CompanyAddressModel("", "", "", "", "", "")
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(model)))
  val keyStoreSavedCompanyAddress = CompanyAddressModel("ks line 1", "ks line 2", "ks line 3", "ks line 4", "ks TF13NY", "ks UK")


  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = CompanyAddressControllerTest.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = CompanyAddressControllerTest.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "CompanyAddressController" should {
    "use the correct keystore connector" in {
      CompanyAddressController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to CompanyAddressController" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[CompanyAddressModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedCompanyAddress)))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[CompanyAddressModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

  }

  "Sending a valid form submit to the CompanyAddressController" should {
    "redirect to the agent view page" in {

      val request = FakeRequest().withFormUrlEncodedBody(
        "addressline1" -> "line 1",
        "addressline2" -> "line 2",
        "addressline3" -> "line 3",
        "addressline4" -> "line 4",
        "postcode" -> "TF1 3NY",
        "country" -> "")

      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/examples-contact")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the CompanyAddressController" should {
    "redirect to the agent view page" in {

      val request = FakeRequest().withFormUrlEncodedBody(
        "addressline1" -> "",
        "addressline2" -> "line 2",
        "addressline3" -> "line 3",
        "addressline4" -> "line 4",
        "postcode" -> "TF1 3NY",
        "country" -> "")

      submitWithSession(request)(
        result => {
          status(result) shouldBe BAD_REQUEST
          //redirectLocation(result) shouldBe Some(routes.CompanyDetailsController.show.toString())
        }
      )
    }
  }

}
