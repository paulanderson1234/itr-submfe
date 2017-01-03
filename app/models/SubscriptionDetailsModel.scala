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

package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class SubscriptionDetailsModel
(
  safeId: String,
  contactDetails: ContactDetailsModel,
  contactAddress: AddressModel
)

object SubscriptionDetailsModel {
  implicit val format = Json.format[SubscriptionDetailsModel]
  implicit val writes = Json.writes[SubscriptionDetailsModel]
}

object EtmpSubscriptionDetailsModel {

  implicit val careads: Reads[AddressModel] = (
      (__ \\ "addressLine1").read[String] and
      (__ \\ "addressLine2").read[String] and
      (__ \\ "addressLine3").readNullable[String] and
      (__ \\ "addressLine4").readNullable[String] and
      (__ \\ "postalCode").readNullable[String] and
      (__ \\ "countryCode").read[String]
    )(AddressModel.apply _)

  implicit val cawrites = Json.writes[AddressModel]

  implicit val cdreads: Reads[ContactDetailsModel] = (
      (__ \\ "contactName" \\ "name1").read[String] and
      (__ \\ "contactName" \\ "name2").read[String] and
      (__ \\ "contactDetails" \\ "phoneNumber").readNullable[String] and
      (__ \\ "contactDetails" \\ "mobileNumber").readNullable[String] and
      (__ \\ "contactDetails" \\ "emailAddress").read[String]
    )(ContactDetailsModel.apply _)

  implicit val cdwrites = Json.writes[ContactDetailsModel]

  implicit val streads: Reads[SubscriptionDetailsModel] = (
      (__ \\ "subscriptionType" \\ "safeId").read[String] and
      (__ \\ "subscriptionType").read[ContactDetailsModel] and
      (__ \\ "subscriptionType" \\ "correspondenceDetails" \\ "contactAddress").read[AddressModel]
    )(SubscriptionDetailsModel.apply _)

  implicit val stwrites = Json.writes[SubscriptionDetailsModel]
}
