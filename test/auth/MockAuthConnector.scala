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

import config.WSHttp
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.auth.connectors.domain.ConfidenceLevel
import uk.gov.hmrc.play.frontend.auth.connectors.domain.ConfidenceLevel.{L50, L500}
import uk.gov.hmrc.play.frontend.auth.connectors.domain.{Accounts, Authority, CredentialStrength, PayeAccount}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}

import scala.concurrent.Future


object MockAuthConnector extends AuthConnector {
  override val http : HttpGet = WSHttp
  override val serviceUrl: String = ""
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
      None
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
      None
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
      None
    ))
}
