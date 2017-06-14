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

package auth

import config.{AppConfig, FrontendAppConfig}

trait MockConfig extends AppConfig {
  override val analyticsToken: String = ""
  override val analyticsHost: String = ""
  override val reportAProblemPartialUrl: String = ""
  override val reportAProblemNonJSUrl: String = ""
  override val notAuthorisedRedirectUrl: String = "/investment-tax-relief/not-authorised"
  override val ggSignInUrl: String = "/gg/sign-in"
  override val introductionUrl: String = "http://localhost:9635/investment-tax-relief/hub"
  override val subscriptionUrl: String = "/investment-tax-relief-subscription/"
  override val ggSignOutUrl: String = "/gg/sign-out"
  override val signOutPageUrl: String = "/investment-tax-relief/signed-out"
  override val submissionUrl: String = "localhost"
  override val contactFormServiceIdentifier: String = "/contact"
  override val contactFrontendPartialBaseUrl: String = "/contact/partial"
  override val attachmentsFrontEndServiceBaseUrl = "http://localhost:9643/investment-tax-relief-attachments-frontend"
  override val internalAttachmentsUrl = "localhost"
  override val submissionFrontendServiceBaseUrl = "http://localhost:9635/investment-tax-relief-submission"
  override val uploadFeatureEnabled: Boolean = false
  override val seisFlowEnabled: Boolean = true
  override val eisseisFlowEnabled: Boolean = true


  override lazy val attachmentFileUploadUrl: (String) => String = schemeType =>
    s"http://localhost:9643/investment-tax-relief-attachments-frontend/file-upload?continueUrl=http://localhost:9635/" +
      s"investment-tax-relief/$schemeType/check-your-answers"

  override lazy val attachmentsServiceUrl: String = "http://localhost:9644"

  override lazy val attachmentFileUploadOutsideUrl =
     s"http://localhost:9643/investment-tax-relief-attachments-frontend/file-upload?continueUrl=http://localhost:9635/" +
      s"investment-tax-relief/check-your-documents"

  override lazy val emailVerificationEisReturnUrl = "http://localhost:9635/investment-tax-relief/eis/email-verification/1"
  override lazy val emailVerificationSeisReturnUrl = "http://localhost:9635/investment-tax-relief/seis/email-verification/1"
  override lazy val emailVerificationCombinedReturnUrl = "http://localhost:9635/investment-tax-relief/eisseis/email-verification/1"
  override lazy val sendVerificationEmailURL = "http://localhost:9640/email-verification/verification-requests"
  override lazy val checkVerifiedEmailURL = "http://localhost:9640/email-verification/verified-email-addresses"
  override lazy val emailVerificationTemplate = "verifyEmailAddress"
}

object MockConfig extends MockConfig

object MockConfigSingleFlow extends MockConfig{
  override val eisseisFlowEnabled: Boolean = false
}

object MockConfigEISFlow extends MockConfig{
  override val seisFlowEnabled: Boolean = false
  override val eisseisFlowEnabled: Boolean = false
}

object MockConfigAllFlows extends MockConfig{
  override val seisFlowEnabled: Boolean = true
  override val eisseisFlowEnabled: Boolean = true
}

object MockConfigUploadFeature extends MockConfig{
  override val uploadFeatureEnabled: Boolean = FrontendAppConfig.uploadFeatureEnabled
}