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

package fixtures

import config.AppConfig

object MockSeisConfig extends AppConfig {
  override val analyticsToken: String = ""
  override val analyticsHost: String = ""
  override val reportAProblemPartialUrl: String = ""
  override val reportAProblemNonJSUrl: String = ""
  override val notAuthorisedRedirectUrl: String = "/investment-tax-relief/not-authorised"
  override val contactFrontendService: String = "/contact"
  override val contactFormServiceIdentifier: String = ""
  override val ggSignInUrl: String = "/gg/sign-in"
  override val introductionUrl: String = "http://localhost:9635/investment-tax-relief/hub"
  override val subscriptionUrl: String = "/investment-tax-relief-subscription/"
  override val ggSignOutUrl: String = "/gg/sign-out"
  override val signOutPageUrl: String = "/investment-tax-relief/signed-out"
  override val submissionUrl: String = "localhost"

  override val attachmentsFrontEndServiceBaseUrl = "http://localhost:9643/investment-tax-relief-attachments-frontend"
  override val internalAttachmentsUrl = "localhost"
  override val submissionFrontendServiceBaseUrl = "http://localhost:9635/investment-tax-relief-submission"
  override val uploadFeatureEnabled: Boolean = false
  override val seisFlowEnabled: Boolean = true

  override lazy val tempAttachmentFileUploadEISUrl: String =
    s"http://localhost:9643/investment-tax-relief-attachments-frontend/file-upload?continueUrl=http://localhost:9635/investment-tax-relief/check-your-answers"


  override lazy val attachmentFileUploadUrl: (String) => String = schemeType =>
    s"http://localhost:9643/investment-tax-relief-attachments-frontend/file-upload?continueUrl=http://localhost:9635/" +
      s"investment-tax-relief/$schemeType/check-your-answers"

}
