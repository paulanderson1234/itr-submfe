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

package views.helpers

import controllers.helpers.BaseSpec
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._


trait ViewSpec extends BaseSpec {

  class Setup {
    mockEnrolledRequest(eisSchemeTypesModel)
  }

  class SEISSetup {
    mockEnrolledRequest(seisSchemeTypesModel)
  }

  class SEISEISSetup {
    mockEnrolledRequest(eisSeisSchemeTypesModel)
  }

  def getExternalLinkText(linkText: String): String = s"""$linkText ${Messages("common.externalLink")}"""

  def getExternalEmailText(emailTextPre: String): String = s"$emailTextPre enterprise.centre@hmrc.gsi.gov.uk."
}
