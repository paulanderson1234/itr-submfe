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
import org.mockito.Matchers
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.auth.connectors.domain.ConfidenceLevel
import uk.gov.hmrc.play.frontend.auth.connectors.domain.ConfidenceLevel.{L50, L500}
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{Accounts, Authority, CredentialStrength}
import uk.gov.hmrc.play.http.ws.WSHttp
import uk.gov.hmrc.play.http.HeaderCarrier
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future


object MockAuthConnector extends AuthConnector with MockitoSugar {
  override val http = mock[WSHttp]
  override val serviceUrl: String = ""
  override def getIds[T](authContext : uk.gov.hmrc.play.frontend.auth.AuthContext)
                        (implicit hc : uk.gov.hmrc.play.http.HeaderCarrier,
                         reads : uk.gov.hmrc.play.http.HttpReads[T]) : scala.concurrent.Future[T] = {
    when(http.GET[UserIDs](Matchers.any())(Matchers.any(), Matchers.any()))
      .thenReturn(Future(UserIDs("Int-312e5e92-762e-423b-ac3d-8686af27fdb5", "Ext-312e5e92-762e-423b-ac3d-8686af27fdb5")))
    http.GET[T]("/")
  }
  override def currentAuthority(implicit hc: HeaderCarrier): Future[Option[Authority]] = {
    Future.successful(strongStrengthUser)
  }

  private def strongStrengthUser: Option[Authority] =
    Some(Authority("/auth/oid/mockuser",
      Accounts(),
      None,
      None,
      CredentialStrength.Strong,
      ConfidenceLevel.L50,
      None,
      None,
      None,
      ""
    ))

  private def weakStrengthUser: Option[Authority] =
    Some(Authority("/auth/oid/mockuser",
      Accounts(),
      None,
      None,
      CredentialStrength.Weak,
      ConfidenceLevel.L50,
      None,
      None,
      None,
      ""
    ))

  private def noStrengthUser: Option[Authority] =
    Some(Authority("/auth/oid/mockuser",
      Accounts(),
      None,
      None,
      CredentialStrength.None,
      ConfidenceLevel.L50,
      None,
      None,
      None,
      ""
    ))
}
