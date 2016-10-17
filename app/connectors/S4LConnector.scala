/*
 * Copyright 2016 HM Revenue & Customs
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

import config.TAVCShortLivedCache
import play.api.libs.json.Format
import uk.gov.hmrc.http.cache.client.{CacheMap, SessionCache, ShortLivedCache}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

object S4LConnector extends S4LConnector {
  override val shortLivedCache = TAVCShortLivedCache
}

trait S4LConnector {

  val shortLivedCache : ShortLivedCache

  def saveFormData[T](key: String, data : T)(implicit hc: HeaderCarrier, format: Format[T]): Future[CacheMap] = {
    shortLivedCache.cache[T](getCacheId, key, data)
  }

  def fetchAndGetFormData[T](key : String)(implicit hc: HeaderCarrier, format: Format[T]): Future[Option[T]] = {
    shortLivedCache.fetchAndGetEntry(getCacheId, key)
  }

  def clearKeystore()(implicit hc : HeaderCarrier) : Future[HttpResponse] = {
    shortLivedCache.remove(getCacheId)
  }

  def fetch()(implicit hc : HeaderCarrier) : Future[Option[CacheMap]] = {
    shortLivedCache.fetch(getCacheId)
  }

  private def getCacheId (implicit hc: HeaderCarrier): String = {
    hc.sessionId.getOrElse(throw new RuntimeException("")).value
  }
}
