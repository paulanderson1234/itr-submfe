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

package services

import common.KeystoreKeys
import connectors.{KeystoreConnector, TokenConnector}
import models.throttling.TokenModel
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess}

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier

object TokenService  extends TokenService{
  val tokenConnector = TokenConnector
  val keystoreConnector = KeystoreConnector
}

trait TokenService {
  val tokenConnector: TokenConnector
  val keystoreConnector: KeystoreConnector

  def generateTemporaryToken(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] = {

    val noToken = ""

    tokenConnector.generateTemporaryToken.map {
      response => response.json.validate[TokenModel] match {
        case data: JsSuccess[TokenModel] =>
          data.value._id
        case e: JsError =>
          val errorMessage = s"[TokenService][generateTemporaryToken] - Failed to parse JSON response. Errors=${e.errors}"
          Logger.warn(errorMessage)
          noToken
      }
    }.recover {
      case _ => {
        val errorMessage = s"[TokenService][generateTemporaryToken] - No temporary token response retrieved"
        Logger.warn(errorMessage)
        noToken
      }
    }
  }

  def validateTemporaryToken(tokenId: Option[String])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    Logger.info(s"[TokenService][validateTemporaryToken] - START tokenId=${tokenId.getOrElse("")}")
    def hasValidToken(token: Option[String]): Future[Boolean] = {
      tokenConnector.validateTemporaryToken(tokenId).map {
        case Some(validationResult) if validationResult => {
          // if we have a valid token we can assume the throttle check was passed abd restore it in the new session created by auth
          // This will prevent user form wastijg tokens if subscription  ot completed
          keystoreConnector.saveFormData(KeystoreKeys.throttleCheckPassed, true)
          true
        }
        case _ =>
          // if the token is missing or invalid we won't assume there is no valid throttle check and reset it.
          // if there is a valid throttle check in session we should not emove it as it will expire at end of session in any case
          // we don't want the user to waste tokens if it can be avoided.
          false
      }.recover {
        case _ => // do not clear throttle check - see above
          false
      }
    }

    (for {
      validated <- hasValidToken(tokenId)
    } yield validated).recover {
      case _ =>
        // do not clear throttle check - see above
        false
    }

  }
}
