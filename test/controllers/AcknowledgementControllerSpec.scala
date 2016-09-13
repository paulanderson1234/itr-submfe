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
import common.KeystoreKeys
import connectors.KeystoreConnector
import models.{YourCompanyNeedModel, ContactDetailsModel, CommercialSaleModel}
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.OneServerPerSuite
import org.specs2.mock.Mockito
import play.api.mvc.{AnyContentAsFormUrlEncoded, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

import scala.concurrent.Future

class AcknowledgementControllerSpec extends UnitSpec  with Mockito with WithFakeApplication{

  val mockKeyStoreConnector = mock[KeystoreConnector]

  val contactValid = ContactDetailsModel("Frank","The Tank","01384 555678","email@gmail.com")
  val contactInvalid = ContactDetailsModel("Frank","The Tank","01384 555678","email@badrequest.com")
  val yourCompanyNeed = YourCompanyNeedModel("AA")

  def showWithSession(test: Future[Result] => Any) {
    val sessionId = s"user-${UUID.randomUUID}"
    val result = AcknowledgementController.show().apply(SessionBuilder.buildRequestWithSession(sessionId))
    test(result)
  }

  implicit val hc = HeaderCarrier()


  "Sending a GET request to AcknowledgementController with a valid email address" should {
    "return a 200" in {
     when(mockKeyStoreConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(), Matchers.any()))
      .thenReturn(Future.successful(Option(contactValid)))
      when(mockKeyStoreConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.eq(KeystoreKeys.yourCompanyNeed))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(yourCompanyNeed)))
      showWithSession(
        result => status(result) shouldBe OK
      )
    }
  }

  "Sending a GET request to AcknowledgementController with a invalid email address" should {
    "return a 5xx" in {
      when(mockKeyStoreConnector.fetchAndGetFormData[ContactDetailsModel](Matchers.eq(KeystoreKeys.contactDetails))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(contactValid)))
      when(mockKeyStoreConnector.fetchAndGetFormData[YourCompanyNeedModel](Matchers.eq(KeystoreKeys.yourCompanyNeed))(Matchers.any(), Matchers.any()))
        .thenReturn(Future.successful(Option(yourCompanyNeed)))
      showWithSession(
        result => status(result) shouldBe INTERNAL_SERVER_ERROR
      )
    }
  }
}
