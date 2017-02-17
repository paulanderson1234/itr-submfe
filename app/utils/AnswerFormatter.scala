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

/**
  * Copyright 2016 HM Revenue & Customs
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIED OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package utils

import play.api.i18n.Messages
import common.Constants
import play.api.i18n.Messages.Implicits._
import play.api.Play.current

trait AnswerFormatter {

  def purposeTransformation(input: String): String = input match{
    case(Constants.businessActivityTrade) => Messages("page.investment.whatWillUseFor.business")
    case(Constants.businessActivityPreparation) => Messages("page.investment.whatWillUseFor.preparing")
    case(Constants.businessActivityRAndD) => Messages("page.investment.whatWillUseFor.rAndD")
    case _ => Messages("common.notAvailable")
  }
}
