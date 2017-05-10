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
import connectors.{KeystoreConnector, ThrottleConnector}
import play.api.Logger
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global


object ThrottleService extends ThrottleService {
  val throttleConnector = ThrottleConnector
  val keystoreConnector = KeystoreConnector
}

trait ThrottleService {

  val throttleConnector: ThrottleConnector
  val keystoreConnector: KeystoreConnector

  def checkUserAccess(implicit hc: HeaderCarrier): Future[Boolean] = {

    def checkAccess(implicit hc: HeaderCarrier):Future[Boolean] = {
      throttleSessionCheck(hc).flatMap {
        case exists if exists => {
          println ("=====================================exists")
          Future(true)
        }
        case notExists => {
          println ("=====================================not exists")
          throttleConnector.checkUserAccess() map {
            case Some(accessGranted) => {
              println ("=====================================granted")
              //keystoreConnector.saveFormData(KeystoreKeys.throttleCheckPassed, accessGranted)
              accessGranted
            }
            case None => {
              //keystoreConnector.saveFormData(KeystoreKeys.throttleCheckPassed, false)
              false
            }
          } recover {
            case e: Exception => {
              println("error=======================")
              Logger.warn(s"[ThrottleService][checkUserAccess] - Error occurred while checking user access. Errors=${e.getMessage}")
              false
            }
          }
        }
      }

    }

    checkAccess.map{ isValid =>
      if (isValid) {
        keystoreConnector.saveFormData(KeystoreKeys.throttleCheckPassed, true)
        true
      }
      else {
        keystoreConnector.saveFormData(KeystoreKeys.throttleCheckPassed, false)
        false
      }
    }
  }

  private def throttleSessionCheck(implicit hc: HeaderCarrier): Future[Boolean] = {
    keystoreConnector.fetchAndGetFormData[Boolean](KeystoreKeys.throttleCheckPassed).map {
      case data => {
        data.fold(false)(_.self)
      }
    }
  }
}
