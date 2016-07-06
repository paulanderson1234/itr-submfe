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

class DateOfIncorporationControllerSpec extends UnitSpec with MockitoSugar with BeforeAndAfterEach with OneServerPerSuite {

  val mockKeyStoreConnector = mock[KeystoreConnector]

  object DateOfIncorporationControllerTest extends DateOfIncorporationController {
    val keyStoreConnector: KeystoreConnector = mockKeyStoreConnector
  }

  val dateOfIncorporationAsJson = """{"day": 23,"month": 11, "year": 1993}"""

  val model = DateOfIncorporationModel(Some(23),Some(11),Some(1993))
  val emptyModel = DateOfIncorporationModel(None, None, None)
  val cacheMap: CacheMap = CacheMap("", Map("" -> Json.toJson(model)))
  val keyStoreSavedDateOfIncorporation = DateOfIncorporationModel(Some(23),Some(11),Some(1993))


  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = DateOfIncorporationControllerTest.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  def submitWithSession(request: FakeRequest[AnyContentAsFormUrlEncoded])(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = DateOfIncorporationControllerTest.submit.apply(SessionBuilder.updateRequestFormWithSession(request, sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()

  override def beforeEach() {
    reset(mockKeyStoreConnector)
  }

  "DateOfIncorporationController" should {
    "use the correct keystore connector" in {
      DateOfIncorporationController.keyStoreConnector shouldBe KeystoreConnector
    }
  }

  "Sending a GET request to DateOfIncorporationController" should {
    "return a 200 when something is fetched from keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(keyStoreSavedDateOfIncorporation)))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

    "provide an empty model and return a 200 when nothing is fetched using keystore" in {
      when(mockKeyStoreConnector.saveFormData(Matchers.any(), Matchers.any())(Matchers.any(), Matchers.any())).thenReturn(cacheMap)
      when(mockKeyStoreConnector.fetchAndGetFormData[DateOfIncorporationModel](Matchers.any())(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(None))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }

  }

  "Sending a valid form submit to the DateOfIncorporationController" should {
    "redirect to the itself" in {

      val request = FakeRequest().withFormUrlEncodedBody(
        "incorporationDay" -> "23",
        "incorporationMonth" -> "11",
        "incorporationYear" -> "1993")

      submitWithSession(request)(
        result => {
          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some("/investment-tax-relief/nature-of-business")
        }
      )
    }
  }

  "Sending an invalid form submission with validation errors to the DateOfIncorporationController" should {
    "redirect to itself" in {

      val request = FakeRequest().withFormUrlEncodedBody(
        "incorporationDay" -> "",
        "incorporationMonth" -> "",
        "incorporationYear" -> "")

      submitWithSession(request)(
        result => {
          status(result) shouldBe BAD_REQUEST
          //redirectLocation(result) shouldBe Some(routes.CompanyDetailsController.show.toString())
        }
      )
    }
  }

}
