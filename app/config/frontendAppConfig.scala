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
  val contactFrontendService: String
  val signOutPageUrl: String
  val submissionUrl: String
  val fileUploadUrl: String
}

object FrontendAppConfig extends AppConfig with ServicesConfig {

  private def loadConfig(key: String) = configuration.getString(key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  override lazy val analyticsToken = loadConfig(s"google-analytics.token")
  override lazy val analyticsHost = loadConfig(s"google-analytics.host")
  override lazy val notAuthorisedRedirectUrl = configuration.getString("not-authorised-callback.url").getOrElse("")
  override lazy val ggSignInUrl: String = configuration.getString(s"government-gateway-sign-in.host").getOrElse("")
  override lazy val ggSignOutUrl: String = configuration.getString(s"government-gateway-sign-out.host").getOrElse("")
  override lazy val introductionUrl: String = configuration.getString(s"introduction.url").getOrElse("")
  override lazy val subscriptionUrl: String = loadConfig("investment-tax-relief-subscription.url")
  override lazy val signOutPageUrl: String = configuration.getString(s"sign-out-page.url").getOrElse("")

  override lazy val contactFrontendService = loadConfig("contact-frontend.url")
  override val contactFormServiceIdentifier = "TAVC"
  override lazy val reportAProblemPartialUrl = s"$contactFrontendService/problem_reports_ajax?service=$contactFormServiceIdentifier"
  override lazy val reportAProblemNonJSUrl = s"$contactFrontendService/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  override lazy val submissionUrl = baseUrl("investment-tax-relief-submission")
  override lazy val fileUploadUrl: String = loadConfig("file-upload.url")
}
