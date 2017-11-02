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

package config

import play.api.Play.{configuration, current}
import uk.gov.hmrc.play.config.ServicesConfig

trait AppConfig {
  val analyticsToken: String
  val analyticsHost: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
  val notAuthorisedRedirectUrl: String
  val ggSignInUrl: String
  val ggSignOutUrl: String
  val introductionUrl: String
  val subscriptionUrl: String
  val contactFormServiceIdentifier: String
  val contactFrontendPartialBaseUrl: String
  val signOutPageUrl: String
  val submissionUrl: String
  val attachmentFileUploadUrl: (String)=> String
  val internalAttachmentsUrl: String
  val attachmentsFrontEndServiceBaseUrl: String
  val submissionFrontendServiceBaseUrl: String
  val attachmentsServiceUrl: String
  val attachmentFileUploadOutsideUrl: String
  val emailDomain: String
  val emailUrl: String
  val emailConfirmationTemplate: String
  val noDocsEmailConfirmationTemplate: String
  val emailVerificationEisReturnUrlOne: String
  val emailVerificationSeisReturnUrlOne: String
  val emailVerificationCombinedReturnUrlOne: String
  val emailVerificationEisReturnUrlTwo: String
  val emailVerificationSeisReturnUrlTwo: String
  val emailVerificationCombinedReturnUrlTwo: String
  val sendVerificationEmailURL: String
  val checkVerifiedEmailURL: String
  val emailVerificationTemplate: String
  val internalCSSubmissionUrl: String
  val submissionCSFrontendServiceEISBaseUrl: String
  val submissionCSFrontendServiceSEISBaseUrl: String
  val submissionCSFrontendServiceBaseUrl: String
}

object FrontendAppConfig extends AppConfig with ServicesConfig {

  private def loadConfig(key: String) = configuration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))
  private def getFeature(key: String) = configuration.getBoolean(key).getOrElse(false)

  override lazy val analyticsToken = loadConfig(s"google-analytics.token")
  override lazy val analyticsHost = loadConfig(s"google-analytics.host")
  override lazy val notAuthorisedRedirectUrl = configuration.getString("not-authorised-callback.url").getOrElse("")
  override lazy val ggSignInUrl: String = configuration.getString(s"government-gateway-sign-in.host").getOrElse("")
  override lazy val ggSignOutUrl: String = configuration.getString(s"government-gateway-sign-out.host").getOrElse("")
  override lazy val introductionUrl: String = configuration.getString(s"introduction.url").getOrElse("")

  override lazy val attachmentsFrontEndServiceBaseUrl: String = loadConfig(s"investment-tax-relief-attachments-frontend.url")
  override lazy val submissionFrontendServiceBaseUrl: String = loadConfig(s"investment-tax-relief-submission-frontend.url")

  override lazy val subscriptionUrl: String = loadConfig("investment-tax-relief-subscription.url")
  override lazy val signOutPageUrl: String = configuration.getString(s"sign-out-page.url").getOrElse("")

  //Contact Frontend Config
  protected lazy val contactFrontendService = baseUrl("contact-frontend")
  protected lazy val contactHost = loadConfig("contact-frontend.host")
  override lazy val contactFormServiceIdentifier = "TAVC"
  override lazy val contactFrontendPartialBaseUrl = s"$contactFrontendService"
  override lazy val reportAProblemPartialUrl = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"

  override lazy val submissionUrl = baseUrl("investment-tax-relief-submission")
  override lazy val internalAttachmentsUrl = baseUrl("internal-attachments")
  override lazy val attachmentFileUploadUrl: (String)=> String = schemeType => {
    s"$attachmentsFrontEndServiceBaseUrl/file-upload?continueUrl=$submissionFrontendServiceBaseUrl" +
    s"/$schemeType/check-your-answers&backUrl=$submissionFrontendServiceBaseUrl/$schemeType/supporting-documents-upload"

  }
  override lazy val internalCSSubmissionUrl = baseUrl("internal-submission-cs")

  override lazy val attachmentsServiceUrl: String = baseUrl("investment-tax-relief-attachments")

  val attachmentFileUploadOutsideUrl =
    s"$attachmentsFrontEndServiceBaseUrl/file-upload?continueUrl=$submissionFrontendServiceBaseUrl" +
    s"/check-your-documents&backUrl=$submissionFrontendServiceBaseUrl/supporting-documents-upload"


  override lazy val emailDomain = loadConfig("email-confirmation.domain")
  override lazy val emailUrl = baseUrl("email")
  override lazy val emailConfirmationTemplate = loadConfig("email-confirmation.templateConfirmationId")
  override lazy val noDocsEmailConfirmationTemplate = loadConfig("email-confirmation.templateNoDocsConfirmationId")

  override lazy val emailVerificationEisReturnUrlOne = loadConfig(s"email.returnUrlEisOne")
  override lazy val emailVerificationSeisReturnUrlOne = loadConfig(s"email.returnUrlSeisOne")
  override lazy val emailVerificationCombinedReturnUrlOne = loadConfig(s"email.returnUrlCombinedOne")
  override lazy val emailVerificationEisReturnUrlTwo = loadConfig(s"email.returnUrlEisTwo")
  override lazy val emailVerificationSeisReturnUrlTwo = loadConfig(s"email.returnUrlSeisTwo")
  override lazy val emailVerificationCombinedReturnUrlTwo = loadConfig(s"email.returnUrlCombinedTwo")
  override lazy val sendVerificationEmailURL = baseUrl("email-verification") + loadConfig("email-vs.sendVerificationEmailURL")
  override lazy val checkVerifiedEmailURL = baseUrl("email-verification") + loadConfig("email-vs.checkVerifiedEmailURL")
  override lazy val emailVerificationTemplate = loadConfig("email.emailVerificationTemplate")

  override lazy val submissionCSFrontendServiceBaseUrl: String =
    internalCSSubmissionUrl + loadConfig(s"investment-tax-relief-cs-submission-flow.schemeSelection")
  override lazy val submissionCSFrontendServiceEISBaseUrl: String =
    internalCSSubmissionUrl + loadConfig(s"investment-tax-relief-cs-submission-flow.eisApplication")
  override lazy val submissionCSFrontendServiceSEISBaseUrl: String =
    internalCSSubmissionUrl + loadConfig(s"investment-tax-relief-cs-submission-flow.seisApplication")
}
