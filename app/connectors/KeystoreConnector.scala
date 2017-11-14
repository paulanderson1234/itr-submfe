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

package connectors

import config.TAVCSessionCache
import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache}
import scala.concurrent.Future
import uk.gov.hmrc.http.{ HeaderCarrier, HttpResponse  }
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

object KeystoreConnector extends KeystoreConnector{
   val sessionCache: SessionCache = TAVCSessionCache
}

trait KeystoreConnector{

  val sessionCache: SessionCache

  def saveFormData[T](key: String, data : T)(implicit writes: Writes[T], hc: HeaderCarrier): Future[CacheMap] = {
    sessionCache.cache[T](key, data)
  }

  def fetchAndGetFormData[T](key : String)(implicit hc: HeaderCarrier, reads: Reads[T]): Future[Option[T]] = {
    sessionCache.fetchAndGetEntry(key)
  }

  def clearCache()(implicit hc : HeaderCarrier) : Future[HttpResponse] = {
    sessionCache.remove()
  }

  def fetch()(implicit hc : HeaderCarrier) : Future[Option[CacheMap]] = {
    sessionCache.fetch()
  }
}
