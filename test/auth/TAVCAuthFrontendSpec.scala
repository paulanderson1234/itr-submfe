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

package auth

import auth.authModels.UserIDs
import controllers.helpers.BaseSpec
import play.api.test.Helpers._
import org.mockito.Matchers
import org.mockito.Mockito._
import uk.gov.hmrc.play.http.{Upstream4xxResponse, Upstream5xxResponse}

import scala.concurrent.Future


class TAVCAuthFrontendSpec extends BaseSpec {

  private val mockAuthority = ggUser.sufficientAuthority
  private val mockUserIds = UserIDs("Int-312e5e92-762e-423b-ac3d-8686af27fdb5", "Ext-312e5e92-762e-423b-ac3d-8686af27fdb5")

  val failedResponseAuth = Upstream4xxResponse("Error", UNAUTHORIZED, UNAUTHORIZED)
  val failedResponseConversion = Upstream5xxResponse("Error", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)

  "Calling authenticated async action with frontend authentication" should {
    "respond with OK" in {
      lazy val result = AuthFrontendTestController.authorisedAsyncAction(authorisedFakeFrontendRequest)
      when(AuthFrontendTestController.authConnector.currentAuthority(Matchers.any())).thenReturn(Future.successful(Some(mockAuthority)))
      when(AuthFrontendTestController.authConnector.getIds[UserIDs](Matchers.any())(Matchers.any(),
        Matchers.any())).thenReturn(Future.successful(mockUserIds))
      status(result) shouldBe OK
    }
  }

  "Calling authenticated async action with browser authentication" should {
    "respond with InternalServerError" in {
      lazy val result = AuthFrontendTestController.authorisedAsyncAction(authorisedFakeRequest)
      when(AuthFrontendTestController.authConnector.currentAuthority(Matchers.any())).thenReturn(Future.failed(failedResponseAuth))
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "Calling authenticated async action with frontend authentication" should {
    "respond with Unauthorised if an auth record cannot be found" in {
      lazy val result = AuthFrontendTestController.authorisedAsyncAction(authorisedFakeFrontendRequest)
      when(AuthFrontendTestController.authConnector.currentAuthority(Matchers.any())).thenReturn(Future.successful(None))
      status(result) shouldBe UNAUTHORIZED
    }
  }

  "Calling authenticated async action with frontend authentication" should {
    "respond with InternalServerError no UserIds can be found" in {
      lazy val result = AuthFrontendTestController.authorisedAsyncAction(authorisedFakeFrontendRequest)
      when(AuthFrontendTestController.authConnector.currentAuthority(Matchers.any())).thenReturn(Future.successful(Some(mockAuthority)))
      when(AuthFrontendTestController.authConnector.getIds[UserIDs](Matchers.any())(Matchers.any(),
        Matchers.any())).thenReturn(Future.failed(failedResponseConversion))
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "Calling authenticated action with frontend authentication" should {
    "respond with OK" in {
      lazy val result = AuthFrontendTestController.authorisedAction(authorisedFakeFrontendRequest)
      when(AuthFrontendTestController.authConnector.currentAuthority(Matchers.any())).thenReturn(Future.successful(Some(mockAuthority)))
      when(AuthFrontendTestController.authConnector.getIds[UserIDs](Matchers.any())(Matchers.any(),
        Matchers.any())).thenReturn(Future.successful(mockUserIds))
      status(result) shouldBe OK
    }
  }

  "Calling authenticated action with browser authentication" should {
    "respond with InternalServerError" in {
      lazy val result = AuthFrontendTestController.authorisedAction(authorisedFakeRequest)
      when(AuthFrontendTestController.authConnector.currentAuthority(Matchers.any())).thenReturn(Future.failed(failedResponseAuth))
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "Calling authenticated action with frontend authentication" should {
    "respond with Unauthorised if an auth record cannot be found" in {
      lazy val result = AuthFrontendTestController.authorisedAction(authorisedFakeFrontendRequest)
      when(AuthFrontendTestController.authConnector.currentAuthority(Matchers.any())).thenReturn(Future.successful(None))
      status(result) shouldBe UNAUTHORIZED
    }
  }

  "Calling authenticated action with frontend authentication" should {
    "respond with InternalServerError no UserIds can be found" in {
      lazy val result = AuthFrontendTestController.authorisedAction(authorisedFakeFrontendRequest)
      when(AuthFrontendTestController.authConnector.currentAuthority(Matchers.any())).thenReturn(Future.successful(Some(mockAuthority)))
      when(AuthFrontendTestController.authConnector.getIds[UserIDs](Matchers.any())(Matchers.any(),
        Matchers.any())).thenReturn(Future.failed(failedResponseConversion))
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
