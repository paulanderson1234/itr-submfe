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
import play.api.http.Status
import play.api.http.Status._
import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object TokenService  extends TokenService{
  val tokenConnector = TokenConnector
  val keystoreConnector = KeystoreConnector
}

trait TokenService {
  val tokenConnector: TokenConnector
  val keystoreConnector: KeystoreConnector

  def generateTemporaryToken(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    tokenConnector.generateTemporaryToken.map{
      response => response.json.validate[TokenModel] match {
        case data: JsSuccess[TokenModel] =>
          keystoreConnector.saveFormData[TokenModel](KeystoreKeys.throttlingToken,data.value)
          HttpResponse(OK)
        case e: JsError =>
          val errorMessage = s"[TokenService][generateTemporaryToken] - Failed to parse JSON response. Errors=${e.errors}"
          Logger.warn(errorMessage)
          HttpResponse(INTERNAL_SERVER_ERROR, Some(Json.toJson(errorMessage)))
      }
    }.recover {
      case _ => {
        val errorMessage = s"[TokenService][generateTemporaryToken] - No temporary token response retrieved"
        Logger.warn(errorMessage)
        HttpResponse(INTERNAL_SERVER_ERROR, Some(Json.toJson(errorMessage))
        )
      }
    }
  }

  def validateTemporaryToken(implicit hc: HeaderCarrier) : Future[Boolean] = {

    def hasValidToken(token: Option[TokenModel], hasExistingThrottleCheck: Option[Boolean]): Future[Boolean] = {
      if (hasExistingThrottleCheck.getOrElse(false)) Future(true)
      else tokenConnector.validateTemporaryToken(token).map {
        result => result.getOrElse(false)
      }
    }

    (for{
      hasExistingThrottleCheck <- keystoreConnector.fetchAndGetFormData[Boolean](KeystoreKeys.throttleCheckPassed)
      tokenModel <-  keystoreConnector.fetchAndGetFormData[TokenModel](KeystoreKeys.throttlingToken)
      validated <- hasValidToken(tokenModel,hasExistingThrottleCheck)
    } yield validated). recover {
      case _ => Logger.warn(s"[TokenService][validateTemporaryToken] - Call to validate token failed")
        false
    }
  }
}
