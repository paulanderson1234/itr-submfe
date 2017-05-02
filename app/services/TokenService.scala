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

import auth.TAVCUser
import common.KeystoreKeys
import connectors.{KeystoreConnector, S4LConnector, TokenConnector}
import models.throttling.TokenModel
import play.api.Logger
import play.api.http.Status
import play.api.libs.json.{Json, JsError, JsSuccess}
import uk.gov.hmrc.play.http.{HttpResponse, HeaderCarrier}
import scala.concurrent.Future
import Status._
import scala.concurrent.ExecutionContext.Implicits.global


object TokenService  extends TokenService{
  val tokenConnector = TokenConnector
  val keystoreConnector = KeystoreConnector
}

trait TokenService {
  val tokenConnector: TokenConnector
  val keystoreConnector: KeystoreConnector

  def generateTemporaryToken(implicit hc: HeaderCarrier, user: TAVCUser): Future[HttpResponse] = {
    tokenConnector.generateTemporaryToken.map{
      response => response.json.validate[TokenModel] match {
        case data: JsSuccess[TokenModel] =>
          keystoreConnector.saveFormData[TokenModel](KeystoreKeys.selectedSchemes,data.value)
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

  def validateTemporaryToken(implicit hc: HeaderCarrier, user: TAVCUser) : Future[Boolean] = {
    (for{
      tokenModel <-  keystoreConnector.fetchAndGetFormData[TokenModel](KeystoreKeys.throttlingToken)
      validated <- tokenConnector.validateTemporaryToken(tokenModel)
    } yield validated.getOrElse(false)). recover {
      case e: NoSuchElementException => Logger.warn(s"[TokenService][validateTemporaryToken] - No token found in Save4Later. Errors=${e.getMessage}")
        false
      case _ => Logger.warn(s"[TokenService][validateTemporaryToken] - Call to validate token failed")
        false
    }
  }
}
