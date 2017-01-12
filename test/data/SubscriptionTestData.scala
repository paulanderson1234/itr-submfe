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

package data

import models.{AddressModel, ContactDetailsModel}
import play.api.libs.json.Json

object SubscriptionTestData {

  val validTavcReference = "XATAVC000123456"
  val expectedSafeID = "XA0000000012345"

  val subscriptionTypeFull = Json.parse(
    s"""
      |{
      |    "processingDate": "2001-12-17T09:30:47Z",
      |    "subscriptionType": {
      |        "safeId": "$expectedSafeID",
      |        "correspondenceDetails": {
      |            "contactName": {
      |                "name1": "first",
      |                "name2": "last"
      |            },
      |            "contactDetails": {
      |                "phoneNumber": "0000 00000",
      |                "mobileNumber": "0000 00000",
      |                "faxNumber": "0000 00000",
      |                "emailAddress": "test@test.com"
      |            },
      |            "contactAddress": {
      |                "addressLine1": "some line 1",
      |                "addressLine2": "some line 2",
      |                "addressLine3": "some line 3",
      |                "addressLine4": "some line 4",
      |                "countryCode": "GB",
      |                "postalCode": "AA1 1AA"
      |            }
      |        }
      |    }
      |}
    """.stripMargin
  )

  val subscriptionTypeMin = Json.parse(
    s"""
       |{
       |    "processingDate": "2001-12-17T09:30:47Z",
       |    "subscriptionType": {
       |        "safeId": "$expectedSafeID",
       |        "correspondenceDetails": {
       |            "contactName": {
       |                "name1": "first",
       |                "name2": "last"
       |            },
       |            "contactDetails": {
       |                "faxNumber": "0000 00000",
       |                "emailAddress": "test@test.com"
       |            },
       |            "contactAddress": {
       |                "addressLine1": "some line 1",
       |                "addressLine2": "some line 2",
       |                "countryCode": "GB"
       |            }
       |        }
       |    }
       |}
    """.stripMargin
  )

  val subscriptionDetailsFull = Json.parse(
    s"""
       |{
       |     "safeId": "$expectedSafeID",
       |     "contactDetails": {
       |          "forename": "first",
       |          "surname": "last",
       |          "telephoneNumber": "0000 00000",
       |          "mobileNumber": "0000 00000",
       |          "email": "test@test.com"
       |     },
       |     "contactAddress": {
       |          "addressline1": "some line 1",
       |          "addressline2": "some line 2",
       |          "addressline3": "some line 3",
       |          "addressline4": "some line 4",
       |          "postcode": "AA1 1AA",
       |          "countryCode": "GB"
       |     }
       |}
    """.stripMargin
  )

  val invalidSubscriptionJson = Json.parse(
    s"""
       |{
       |    "processingDate": "2001-12-17T09:30:47Z",
       |    "subscriptionType": {
       |        "safeId": "$expectedSafeID"
       |    }
       |}
    """.stripMargin
  )

  val expectedContactDetailsFull = ContactDetailsModel(
    forename = "first",
    surname = "last",
    telephoneNumber = Some("0000 00000"),
    mobileNumber = Some("0000 00000"),
    email = "test@test.com"
  )

  val expectedContactAddressFull = AddressModel(
    addressline1 = "some line 1",
    addressline2 = "some line 2",
    addressline3 = Some("some line 3"),
    addressline4 = Some("some line 4"),
    countryCode = "GB",
    postcode = Some("AA1 1AA")
  )

  val expectedContactDetailsMin = ContactDetailsModel(
    forename = "first",
    surname = "last",
    email = "test@test.com"
  )

  val expectedContactAddressMin = AddressModel(
    addressline1 = "some line 1",
    addressline2 = "some line 2",
    countryCode = "GB"
  )
}
