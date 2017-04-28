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
import connectors.{S4LConnector, TokenConnector}
import models.throttling.TokenModel
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.play.http.HeaderCarrier
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


object TokenService  extends TokenService{
  val tokenConnector = TokenConnector
  val s4lConnector = S4LConnector
}

trait TokenService {
  val tokenConnector: TokenConnector
  val s4lConnector: S4LConnector

  def generateTemporaryToken(implicit hc: HeaderCarrier, user: TAVCUser): Future[Option[String]] = {
    tokenConnector.generateTemporaryToken.map{
      response => response.json.validate[TokenModel] match {
        case data: JsSuccess[TokenModel] =>
          s4lConnector.saveFormData[TokenModel](KeystoreKeys.selectedSchemes,data.value)
          Some(data.value.token)
        case e: JsError =>
          Logger.warn(s"[TokenService][generateTemporaryToken] - Failed to parse JSON response. Errors=${e.errors}")
          None
      }
    }.recover {
      case _ => {
        Logger.warn(s"[TokenService][generateTemporaryToken] - No temporary token response retrieved")
        None
      }
    }
  }

  def validateTemporaryToken(implicit hc: HeaderCarrier, user: TAVCUser) : Future[Boolean] = {
    (for{
      tokenModel <-  s4lConnector.fetchAndGetFormData[TokenModel](KeystoreKeys.throttlingToken)
      validated <- tokenConnector.validateTemporaryToken(tokenModel)
    } yield validated.getOrElse(false)). recover {
      case e: NoSuchElementException => Logger.warn(s"[TokenService][validateTemporaryToken] - No token found in Save4Later. Errors=${e.getMessage}")
        false
      case _ => Logger.warn(s"[TokenService][validateTemporaryToken] - Call to validate token failed")
        false
    }
  }
}
