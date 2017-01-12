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

package models.registration

import models.AddressModel
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class RegistrationDetailsModel(organisationName: String,
                                    addressModel: AddressModel)

object RegistrationDetailsModel {
  implicit val format = Json.format[RegistrationDetailsModel]
  implicit val writes = Json.writes[RegistrationDetailsModel]
}

object ETMPRegistrationDetailsModel {

  implicit val readsA: Reads[AddressModel] = (
    (__ \ "addressLine1").read[String] and
      (__ \ "addressLine2").read[String] and
      (__ \ "addressLine3").readNullable[String] and
      (__ \ "addressLine4").readNullable[String] and
      (__ \ "postalCode").readNullable[String] and
      (__ \ "countryCode").read[String]
    )(AddressModel.apply _)

  implicit val writesA = Json.writes[AddressModel]

  implicit val readsRDM: Reads[RegistrationDetailsModel] = (
    (__ \ "organisation" \ "organisationName").read[String] and
      (__ \ "addressDetails").read[AddressModel]
    )(RegistrationDetailsModel.apply _)

  implicit val writesRDM = Json.writes[RegistrationDetailsModel]

}
