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

import auth.TAVCUser
import config.TAVCShortLivedCache
import play.api.libs.json.Format
import uk.gov.hmrc.http.cache.client.{CacheMap, ShortLivedCache}
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.Future

object S4LConnector extends S4LConnector {
  override val shortLivedCache = TAVCShortLivedCache
}

trait S4LConnector {

  val shortLivedCache : ShortLivedCache

  def saveFormData[T](key: String, data : T)(implicit hc: HeaderCarrier, format: Format[T], user: TAVCUser): Future[CacheMap] = {
    shortLivedCache.cache[T](user.internalId, key, data)
  }

  def fetchAndGetFormData[T](key : String)(implicit hc: HeaderCarrier, format: Format[T], user: TAVCUser): Future[Option[T]] = {
    shortLivedCache.fetchAndGetEntry(user.internalId, key)
  }

  def clearCache()(implicit hc : HeaderCarrier, user: TAVCUser) : Future[HttpResponse] = {
    shortLivedCache.remove(user.internalId)
  }

  def fetch()(implicit hc : HeaderCarrier, user: TAVCUser) : Future[Option[CacheMap]] = {
    shortLivedCache.fetch(user.internalId)
  }

}
