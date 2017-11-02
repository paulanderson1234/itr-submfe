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

package services.internal

import auth.authModels.UserIDs
import common.KeystoreKeys
import config.FrontendAuthConnector
import connectors.{ComplianceStatementConnector, S4LConnector}
import models.internal.CSApplicationModel
import uk.gov.hmrc.play.frontend.auth.AuthContext
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector
import uk.gov.hmrc.play.frontend.auth.connectors.domain.Authority
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait InternalService {

  val authConnector: AuthConnector
  val s4LConnector: S4LConnector
  val csConnector: ComplianceStatementConnector

  def getApplicationInProgress(authority: Authority)(implicit hc: HeaderCarrier): Future[Option[Boolean]] = {
    authConnector.getIds[UserIDs](AuthContext(authority)).flatMap{
      userIDs => {
        s4LConnector.fetchAndGetFormData[Boolean](userIDs.internalId, KeystoreKeys.applicationInProgress)
      }
    }
  }

  def getCSApplicationInProgress()(implicit hc: HeaderCarrier, user: T): Future[CSApplicationModel] = {
    csConnector.getComplianceStatementApplication()
  }
}

object InternalService extends InternalService {
  val authConnector = FrontendAuthConnector
  val s4LConnector = S4LConnector
  val csConnector = ComplianceStatementConnector

}

