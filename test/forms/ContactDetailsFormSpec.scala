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

package forms

import forms.ContactDetailsForm._
import models.ContactDetailsModel
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec
import play.api.i18n.Messages.Implicits._

class ContactDetailsFormSpec extends UnitSpec with OneAppPerSuite{

  val chars132 = "thisxx@" + ("12345678911" * 11) + ".com"

  "Creating a form using an empty model" should {
    lazy val form = contactDetailsForm
    "return an empty string for forename, surname, telephone number and email" in {
      form.data.isEmpty shouldBe true
    }
  }

  "Creating a form using a valid model" should {
    "return a form with the data specified in the model" in {
      val model = ContactDetailsModel("firstname", "lastname", Some("07000 111222"), None, "test@test.com")
      val form = contactDetailsForm.fill(model)
      form.data("forename") shouldBe "firstname"
      form.data("surname") shouldBe "lastname"
      form.data("telephoneNumber") shouldBe "07000 111222"
      form.data("email") shouldBe "test@test.com"
      form.errors.length shouldBe 0
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for forename" should {
      lazy val form = contactDetailsForm.bind(Map(
        "forename" -> "",
        "surname" -> "lastname",
        "telephoneNumber" -> "07000 111222",
        "email" -> "test@test.com")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "forename"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("forename").get.message) shouldBe Messages("error.required")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for surname" should {
      lazy val form = contactDetailsForm.bind(Map(
        "forename" -> "firstname",
        "surname" -> "",
        "telephoneNumber" -> "07000 111222",
        "email" -> "test@test.com")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "surname"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("surname").get.message) shouldBe Messages("error.required")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for email" should {
      lazy val form = contactDetailsForm.bind(Map(
        "forename" -> "firstname",
        "surname" -> "lastname",
        "telephoneNumber" -> "07000 111222",
        "email" -> "")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "email"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("email").get.message) shouldBe Messages("validation.error.email")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for forename and surname" should {
      lazy val form = contactDetailsForm.bind(Map(
        "forename" -> "",
        "surname" -> "",
        "telephoneNumber" -> "07000 111222",
        "email" -> "test@test.com")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 2 form errors" in {
        form.errors.length shouldBe 2
        form.errors.head.key shouldBe "forename"
        form.errors(1).key shouldBe "surname"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("forename").get.message) shouldBe Messages("error.required")
        Messages(form.error("surname").get.message) shouldBe Messages("error.required")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for telephone number and email" should {
      lazy val form = contactDetailsForm.bind(Map(
        "forename" -> "firstname",
        "surname" -> "lastname",
        "telephoneNumber" -> "",
        "email" -> "")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form errors" in {
        form.errors.length shouldBe 1
        form.errors(0).key shouldBe "email"
      }
      "associate the correct error message to the error" in {
        form.error("email").get.message shouldBe Messages("validation.error.email")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for forename, surname or telephone number" should {
      lazy val form = contactDetailsForm.bind(Map(
        "forename" -> "",
        "surname" -> "",
        "telephoneNumber" -> "",
        "email" -> "test@test.com")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 2 form errors" in {
        form.errors.length shouldBe 2
        form.errors.head.key shouldBe "forename"
        form.errors(1).key shouldBe "surname"
      }
      "associate the correct error message to the error" in {
        Messages(form.error("forename").get.message) shouldBe Messages("error.required")
        Messages(form.error("surname").get.message) shouldBe Messages("error.required")
      }
    }
  }

  "supplied with empty space for forename" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "    ",
      "surname" -> "lastname",
      "telephoneNumber" -> "07000 111222",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "forename"
    }
    "associate the correct error message to the error " in {
      Messages(form.error("forename").get.message) shouldBe Messages("error.required")
    }
  }

  "supplied with empty space for surname" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "   ",
      "telephoneNumber" -> "07000 111222",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "surname"
    }
    "associate the correct error message to the error " in {
      Messages(form.error("surname").get.message) shouldBe Messages("error.required")
    }
  }

  "supplied with empty space for telephoneNumber" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "     ",
      "email" -> "test@test.com")
    )
    "raise no form errors" in {
      form.hasErrors shouldBe false
    }
  }

  "supplied with empty space for email" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "07000 111222",
      "email" -> "    ")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "email"
    }
    "associate the correct error message to the error " in {
      form.error("email").get.message shouldBe Messages("validation.error.email")
    }
  }

  "supplied with numeric input for forename" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstn4me",
      "surname" -> "lastname",
      "telephoneNumber" -> "07000 111222",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
     }
  }

  "supplied with numeric input for surname" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastnam3",
      "telephoneNumber" -> "07000 111222",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "supplied with alphanumeric input for telephone number" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "0000 O0000I",
      "email" -> "test@test.com")
    )
    "raise no form errors" in {
      form.hasErrors shouldBe false
    }
  }

  "supplied with alphanumeric input for email" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "07000 111222",
      "email" -> "t3st@t3st.c0m")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

//  BVA

  "forename value supplied with the minimum allowed" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "F",
      "surname" -> "lastname",
      "telephoneNumber" -> "07000 111222",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "surname value supplied with the minimum allowed" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "L",
      "telephoneNumber" -> "07000 111222",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "telephoneNumber value supplied with the minimum allowed" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "0",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "email value supplied with the minimum allowed" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "07000 111222",
      "email" -> "T@t.c")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "forename value supplied with the maximum allowed (on the boundary)" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "Thisnameisthirtyfivecharacterslongg",
      "surname" -> "lastname",
      "telephoneNumber" -> "07000 111222",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "surname value supplied with the maximum allowed (on the boundary)" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "Thisnameisthirtyfivecharacterslongg",
      "telephoneNumber" -> "07000 111222",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "telephoneNumber value supplied with the maximum allowed (on the boundary)" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "000000000000000000000005",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "telephoneNumber value supplied over the maximum allowed (over the boundary)" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "0000000000000000000000006",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "telephoneNumber"
    }
    "associate the correct error message to the error" in {
      form.error("telephoneNumber").get.message shouldBe Messages("validation.error.telephoneNumber")
    }
  }

  "telephoneNumber value supplied over the maximum allowed (over the boundary) incluses whitespace in the count" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "0000000000000 0000000 00003",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "telephoneNumber"
    }
    "associate the correct error message to the error" in {
      form.error("telephoneNumber").get.message shouldBe Messages("validation.error.telephoneNumber")
    }
  }

//Telephone Number Regex

  "telephoneNumber value supplied with multiple white space" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "0 00 0 0 0 0 0 0 0 7",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "telephoneNumber value supplied with brackets" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "(00000) 000006",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "telephoneNumber value supplied with +44" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "+440000000005",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
    }
  }

  "telephoneNumber value supplied with /" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "0/13/84/55/33/82",
      "email" -> "test@test.com")
    )
    "raise no form errors" in {
      form.hasErrors shouldBe false
    }
  }

  "telephoneNumber value supplied with #" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "#00000000007",
      "email" -> "test@test.com")
    )
    "raise no form errors" in {
      form.hasErrors shouldBe false
    }
  }

  "telephoneNumber value supplied with *" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "#00000000008",
      "email" -> "test@test.com")
    )
    "raise no form errors" in {
      form.hasErrors shouldBe false
    }
  }

  "telephoneNumber value supplied with :" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "00000:000007",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "telephoneNumber"
    }
    "associate the correct error message to the error" in {
      form.error("telephoneNumber").get.message shouldBe Messages("validation.error.telephoneNumber")
    }
  }

  "telephoneNumber value supplied with - (American)" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "+1 000-000-0007",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "telephoneNumber"
    }
    "associate the correct error message to the error" in {
      form.error("telephoneNumber").get.message shouldBe Messages("validation.error.telephoneNumber")
    }
  }

  "telephoneNumber value supplied with - (France)" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "+00(0)000000008",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "telephoneNumber"
    }
    "associate the correct error message to the error" in {
      form.error("telephoneNumber").get.message shouldBe Messages("validation.error.telephoneNumber")
    }
  }

  "telephoneNumber value supplied with ext (extensions)" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "+44 0000000000 ext 123",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "telephoneNumber"
    }
    "associate the correct error message to the error" in {
      form.error("telephoneNumber").get.message shouldBe Messages("validation.error.telephoneNumber")
    }
  }

  "telephoneNumber value supplied with . " should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "00000.00.00.00.00",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "telephoneNumber"
    }
    "associate the correct error message to the error" in {
      form.error("telephoneNumber").get.message shouldBe Messages("validation.error.telephoneNumber")
    }
  }

  "telephoneNumber value supplied with a leading space " should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> " 07000 111222",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "telephoneNumber value supplied with a trailing space " should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "07000 111222 ",
      "email" -> "test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
  }

//Email Regex

  "email supplied with multiple white spaces" should {
      lazy val form = contactDetailsForm.bind(Map(
        "forename" -> "firstname",
        "surname" -> "lastname",
        "telephoneNumber" -> "07000 111222",
        "email" -> "Te st@tes t.com")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "email"
      }
      "associate the correct error message to the error" in {
        form.error("email").get.message shouldBe Messages("validation.error.email")
      }
    }

  "email supplied with multiple @" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "07000 111222",
      "email" -> "test@test@test.co.uk")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "email"
    }
    "associate the correct error message to the error" in {
      form.error("email").get.message shouldBe Messages("validation.error.email")
    }
  }

  "email supplied without @" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "07000 111222",
      "email" -> "test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "email"
    }
    "associate the correct error message to the error" in {
      form.error("email").get.message shouldBe Messages("validation.error.email")
    }
  }

  "email supplied with sub domain" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "07000 111222",
      "email" -> "test@subdomain.ntlworld.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form error" in {
      form.errors.length shouldBe 0
    }
  }

  "email supplied with firstname.lastname@" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "07000 111222",
      "email" -> "firstname.lastname@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form error" in {
      form.errors.length shouldBe 0
    }
  }

  "email supplied with forename surname <email@example.com>" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "07000 111222",
      "email" -> "firstname lastname <firstname.lastname@test.com>")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "email"
    }
    "associate the correct error message to the error" in {
      form.error("email").get.message shouldBe Messages("validation.error.email")
    }
  }

  "email supplied with firstname_lastname@" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "07000 111222",
      "email" -> "Test_test@test.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form error" in {
      form.errors.length shouldBe 0
    }
  }

  "Part 1 - minimum allowed supplied for email (on boundary) " should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "00000 000001",
      "email" -> "F@t.c")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form error" in {
      form.errors.length shouldBe 0
    }
  }

  "Part 1 - nothing supplied for first part of the email (under the boundary) " should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "00000 000002",
      "email" -> "@t.c")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "email"
    }
    "associate the correct error message to the error" in {
      form.error("email").get.message shouldBe Messages("validation.error.email")
    }
  }

  "Part 1 - maximum allowed supplied for email (on boundary) " should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "07000 111222",
      "email" -> chars132)
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form error" in {
      form.errors.length shouldBe 0
    }
  }

  "Part 1 - too many characters supplied for the first part of the email (over the boundary) " should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "07000 111222",
      "email" -> s"1$chars132")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "email"
    }
    "associate the correct error message to the error" in {
      form.error("email").get.message shouldBe Messages("validation.error.email")
    }
  }

  "Part 2 - minimum allowed supplied for email (on boundary)" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "07000 111222",
      "email" -> "f@P.c")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form error" in {
      form.errors.length shouldBe 0
    }
  }

  "Part 2 - nothing supplied for second part of the email (under the boundary) " should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "firstname",
      "surname" -> "lastname",
      "telephoneNumber" -> "07000 111222",
      "email" -> "f.f@")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "email"
    }
    "associate the correct error message to the error" in {
      form.error("email").get.message shouldBe Messages("validation.error.email")
    }
  }

}
