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

package forms

import forms.ContactDetailsForm._
import models.ContactDetailsModel
import play.api.i18n.Messages
import uk.gov.hmrc.play.test.UnitSpec

class ContactDetailsFormSpec extends UnitSpec {

  "Creating a form using an empty model" should {
    lazy val form = contactDetailsForm
    "return an empty string for forename, surname, telephone number and email" in {
      form.data.isEmpty shouldBe true
    }
  }

  "Creating a form using a valid model" should {
    "return a form with the data specified in the model" in {
      val model = ContactDetailsModel("Percy", "Montague", "06472 778833", "1234@email.com")
      val form = contactDetailsForm.fill(model)
      form.data("forename") shouldBe "Percy"
      form.data("surname") shouldBe "Montague"
      form.data("telephoneNumber") shouldBe "06472 778833"
      form.data("email") shouldBe "1234@email.com"
      form.errors.length shouldBe 0
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for forename" should {
      lazy val form = contactDetailsForm.bind(Map(
        "forename" -> "",
        "surname" -> "Jones",
        "telephoneNumber" -> "02738 774893",
        "email" -> "Test@email.com")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "forename"
      }
      "associate the correct error message to the error" in {
        form.error("forename").get.message shouldBe Messages("error.required")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for surname" should {
      lazy val form = contactDetailsForm.bind(Map(
        "forename" -> "Tim",
        "surname" -> "",
        "telephoneNumber" -> "02738 774893",
        "email" -> "Test@email.com")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 1 form error" in {
        form.errors.length shouldBe 1
        form.errors.head.key shouldBe "surname"
      }
      "associate the correct error message to the error" in {
        form.error("surname").get.message shouldBe Messages("error.required")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for telephoneNumber" should {
      lazy val form = contactDetailsForm.bind(Map(
        "forename" -> "Tim",
        "surname" -> "Roth",
        "telephoneNumber" -> "",
        "email" -> "Test@email.com")
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
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for email" should {
      lazy val form = contactDetailsForm.bind(Map(
        "forename" -> "Tim",
        "surname" -> "Roth",
        "telephoneNumber" -> "08746 716283",
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
        form.error("email").get.message shouldBe Messages("validation.error.email")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for forename and surname" should {
      lazy val form = contactDetailsForm.bind(Map(
        "forename" -> "",
        "surname" -> "",
        "telephoneNumber" -> "01387 563748",
        "email" -> "james.helix@hmrcaspire.com")
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
        form.error("forename").get.message shouldBe Messages("error.required")
        form.error("surname").get.message shouldBe Messages("error.required")
      }
    }
  }

  "Creating a form using an invalid post" when {
    "supplied with no data for telephone number and email" should {
      lazy val form = contactDetailsForm.bind(Map(
        "forename" -> "James",
        "surname" -> "Helix",
        "telephoneNumber" -> "",
        "email" -> "")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 2 form errors" in {
        form.errors.length shouldBe 2
        form.errors.head.key shouldBe "telephoneNumber"
        form.errors(1).key shouldBe "email"
      }
      "associate the correct error message to the error" in {
        form.error("telephoneNumber").get.message shouldBe Messages("validation.error.telephoneNumber")
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
        "email" -> "james.helix@hmrcaspire.com")
      )
      "raise form error" in {
        form.hasErrors shouldBe true
      }
      "raise 2 form errors" in {
        form.errors.length shouldBe 3
        form.errors.head.key shouldBe "forename"
        form.errors(1).key shouldBe "surname"
        form.errors(2).key shouldBe "telephoneNumber"
      }
      "associate the correct error message to the error" in {
        form.error("forename").get.message shouldBe Messages("error.required")
        form.error("surname").get.message shouldBe Messages("error.required")
        form.error("telephoneNumber").get.message shouldBe Messages("validation.error.telephoneNumber")
      }
    }
  }

  "supplied with empty space for forename" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "    ",
      "surname" -> "Pivot",
      "telephoneNumber" -> "02635 789374",
      "email" -> "Matt.Pivot@hmrcaspire.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "forename"
    }
    "associate the correct error message to the error " in {
      form.error("forename").get.message shouldBe Messages("error.required")
    }
  }

  "supplied with empty space for surname" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "Matt",
      "surname" -> "   ",
      "telephoneNumber" -> "02635 789374",
      "email" -> "Matt.Pivot@hmrcaspire.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 1 form error" in {
      form.errors.length shouldBe 1
      form.errors.head.key shouldBe "surname"
    }
    "associate the correct error message to the error " in {
      form.error("surname").get.message shouldBe Messages("error.required")
    }
  }

  "supplied with empty space for telephoneNumber" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "Matt",
      "surname" -> "Pivot",
      "telephoneNumber" -> "     ",
      "email" -> "Matt.Divet@hmrcaspire.com")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }

  }

  "supplied with empty space for email" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "Matt",
      "surname" -> "Pivot",
      "telephoneNumber" -> "02635 789374",
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
      "forename" -> "D0ug",
      "surname" -> "Perry",
      "telephoneNumber" -> "03782 098372",
      "email" -> "Doug.Perry@digital.hmrc.gov.uk")
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
      "forename" -> "Doug",
      "surname" -> "P3rry",
      "telephoneNumber" -> "03782 098372",
      "email" -> "Doug.Perry@digital.hmrc.gov.uk")
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
      "forename" -> "Doug",
      "surname" -> "Perry",
      "telephoneNumber" -> "OI782 O98372",
      "email" -> "Doug.Perry@digital.hmrc.gov.uk")
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

  "supplied with alphanumeric input for email" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "Doug",
      "surname" -> "Perry",
      "telephoneNumber" -> "01782 098372",
      "email" -> "D0ug.P3rry@d1g1tal.hmrc.g0v.uk")
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
      "forename" -> "D",
      "surname" -> "Perry",
      "telephoneNumber" -> "01375 869472",
      "email" -> "Doug.Perry@digital.hmrc.gov.uk.")
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
      "forename" -> "Doug",
      "surname" -> "P",
      "telephoneNumber" -> "01375 869472",
      "email" -> "Doug.Perry@digital.hmrc.gov.uk.")
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
      "forename" -> "Doug",
      "surname" -> "Perry",
      "telephoneNumber" -> "0",
      "email" -> "Doug.Perry@digital.hmrc.gov.uk.")
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
      "forename" -> "Doug",
      "surname" -> "Perry",
      "telephoneNumber" -> "01375 869472",
      "email" -> "D@d.")
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
      "surname" -> "Perry",
      "telephoneNumber" -> "01375 869472",
      "email" -> "Doug.Perry@digital.hmrc.gov.uk.")
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
      "forename" -> "Doug",
      "surname" -> "Thisnameisthirtyfivecharacterslongg",
      "telephoneNumber" -> "01375 869472",
      "email" -> "Doug.Perry@digital.hmrc.gov.uk.")
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
      "forename" -> "Doug",
      "surname" -> "Perry",
      "telephoneNumber" -> "467846328764987832176776",
      "email" -> "Doug.Perry@digital.hmrc.gov.uk.")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

//  "email value supplied with the maximum allowed (on the boundary)" should {
//    lazy val form = contactDetailsForm.bind(Map(
//      "forename" -> "Doug",
//      "surname" -> "Perry",
//      "telephoneNumber" -> "01375 869472",
//      "email" -> "thisemailis255characterslongthisemailis255characterslongthisemailis255characterslongthisemailis255characterslongthisemailis255characterslongthisemailis255characterslongthisemailis255characterslongthisemailis255characterslongthisemailis255charac@hmrc.co.uk.")
//    )
//    "raise form error" in {
//      form.hasErrors shouldBe false
//    }
//    "raise 0 form errors" in {
//      form.errors.length shouldBe 0
//    }
//  }

//Telephone Number Regex

  "telephoneNumber value supplied with multiple white space" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "Jules",
      "surname" -> "McShane",
      "telephoneNumber" -> "0 13 8 4 5 5 5 8 6 9",
      "email" -> "jules.mcshane@digital.hmrc.gov.uk.")
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
      "forename" -> "Jules",
      "surname" -> "Mcshane",
      "telephoneNumber" -> "(01548) 665599",
      "email" -> "Jules.Mcshane@digital.hmrc.gov.uk.")
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
      "forename" -> "Jules",
      "surname" -> "Mcshane",
      "telephoneNumber" -> "+447567728337",
      "email" -> "Jules.Mcshane@digital.hmrc.gov.uk.")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "telephoneNumber value supplied with /" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "Jules",
      "surname" -> "Mcshane",
      "telephoneNumber" -> "0/13/84/55/33/82",
      "email" -> "Jules.Mcshane@digital.hmrc.gov.uk.")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "telephoneNumber value supplied with #" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "Jules",
      "surname" -> "Mcshane",
      "telephoneNumber" -> "#06534879542",
      "email" -> "Jules.Mcshane@digital.hmrc.gov.uk.")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "telephoneNumber value supplied with *" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "Jules",
      "surname" -> "Mcshane",
      "telephoneNumber" -> "*06534879542",
      "email" -> "Jules.Mcshane@digital.hmrc.gov.uk.")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "telephoneNumber value supplied with :" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "Jules",
      "surname" -> "Mcshane",
      "telephoneNumber" -> "06534:879542",
      "email" -> "Jules.Mcshane@digital.hmrc.gov.uk.")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 1
    }
    "associate the correct error message to the error" in {
      form.error("telephoneNumber").get.message shouldBe Messages("validation.error.telephoneNumber")
    }
  }

  "telephoneNumber value supplied with - (American)" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "Jules",
      "surname" -> "Mcshane",
      "telephoneNumber" -> "+1 855-953-3597",
      "email" -> "Jules.Mcshane@digital.hmrc.gov.uk.")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 1
    }
    "associate the correct error message to the error" in {
      form.error("telephoneNumber").get.message shouldBe Messages("validation.error.telephoneNumber")
    }
  }

  "telephoneNumber value supplied with - (France)" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "Jules",
      "surname" -> "Mcshane",
      "telephoneNumber" -> "+33(0)644444444",
      "email" -> "Jules.Mcshane@digital.hmrc.gov.uk.")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 0
    }
  }

  "telephoneNumber value supplied with ext (extensions)" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "Jules",
      "surname" -> "Mcshane",
      "telephoneNumber" -> "+44 1611234567 ext 123",
      "email" -> "Jules.Mcshane@digital.hmrc.gov.uk.")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 1
    }
    "associate the correct error message to the error" in {
      form.error("telephoneNumber").get.message shouldBe Messages("validation.error.telephoneNumber")
    }
  }

  "telephoneNumber value supplied with . " should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "Jules",
      "surname" -> "Mcshane",
      "telephoneNumber" -> "00336.44.44.44.44",
      "email" -> "Jules.Mcshane@digital.hmrc.gov.uk.")
    )
    "raise form error" in {
      form.hasErrors shouldBe true
    }
    "raise 0 form errors" in {
      form.errors.length shouldBe 1
    }
    "associate the correct error message to the error" in {
      form.error("telephoneNumber").get.message shouldBe Messages("validation.error.telephoneNumber")
    }
  }

//Email Regex

  "email supplied with multiple white spaces" should {
      lazy val form = contactDetailsForm.bind(Map(
        "forename" -> "Pat",
        "surname" -> "Butcher",
        "telephoneNumber" -> "08475 849375",
        "email" -> "P at@Butche r.com")
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
      "forename" -> "Pat",
      "surname" -> "Butcher",
      "telephoneNumber" -> "08475 849375",
      "email" -> "Pat@Butcher@HMRC.gov.uk")
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
      "forename" -> "Pat",
      "surname" -> "Butcher",
      "telephoneNumber" -> "08475 849375",
      "email" -> "PatButcher.com")
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
      "forename" -> "Pat",
      "surname" -> "Butcher",
      "telephoneNumber" -> "08475 849375",
      "email" -> "PatButcher@subdomain.ntlworld.com")
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
      "forename" -> "Pat",
      "surname" -> "Butcher",
      "telephoneNumber" -> "08475 849375",
      "email" -> "Pat.Butcher@HMRC.gov.uk")
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
      "forename" -> "Pat",
      "surname" -> "Butcher",
      "telephoneNumber" -> "08475 849375",
      "email" -> "Pat Butcher <Pat.Butcher@HMRC.gov.uk>")
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

  "email supplied with firstname+lastname@" should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "Pat",
      "surname" -> "Butcher",
      "telephoneNumber" -> "08475 849375",
      "email" -> "Pat+Butcher@HMRC.gov.uk")
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
      "forename" -> "Pat",
      "surname" -> "Butcher",
      "telephoneNumber" -> "08475 849375",
      "email" -> "Pat_Butcher@HMRC.gov.uk")
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
      "forename" -> "Pat",
      "surname" -> "Butcher",
      "telephoneNumber" -> "08475 849375",
      "email" -> "P@HMRC.gov.uk")
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
      "forename" -> "Pat",
      "surname" -> "Butcher",
      "telephoneNumber" -> "08475 849375",
      "email" -> "@HMRC.gov.uk")
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
      "forename" -> "Pat",
      "surname" -> "Butcher",
      "telephoneNumber" -> "08475 849375",
      "email" -> "thisisalongemailthisisalongemailthisisalongemailthisisalongemail@HMRC.gov.uk")
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
      "forename" -> "Pat",
      "surname" -> "Butcher",
      "telephoneNumber" -> "08475 849375",
      "email" -> "thisisalongemailthisisalongemailthisisalongemailthisisalongemailx@HMRC.gov.uk")
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
      "forename" -> "Pat",
      "surname" -> "Butcher",
      "telephoneNumber" -> "08475 849375",
      "email" -> "Pat.Butcher@P")
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
      "forename" -> "Pat",
      "surname" -> "Butcher",
      "telephoneNumber" -> "08475 849375",
      "email" -> "Pat.Butcher@")
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

  "Part 2 - maximum allowed supplied for email (on boundary) " should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "Pat",
      "surname" -> "Butcher",
      "telephoneNumber" -> "08475 849375",
      "email" -> "Pat.Butcher@thisisalongemailthisisalongemailthisisalongemai.thisisalongemail")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form error" in {
      form.errors.length shouldBe 0
    }
  }

  "Part 2 - too many characters supplied for the second part of the email (over the boundary) " should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "Pat",
      "surname" -> "Butcher",
      "telephoneNumber" -> "08475 849375",
      "email" -> "Pat.Butcher@thisisalongemailthisisalongemailthisisalongemai.thisisalongemailx")
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

  "Part 3 - max characters supplied for the email (on both boundaries) " should {
    lazy val form = contactDetailsForm.bind(Map(
      "forename" -> "Pat",
      "surname" -> "Butcher",
      "telephoneNumber" -> "08475 849375",
      "email" -> "thisisalongemailthisisalongemailthisisalongemailthisisalongemail@thisisalongemailthisisalongemailthisisalongemai.thisisalongemail")
    )
    "raise form error" in {
      form.hasErrors shouldBe false
    }
    "raise 0 form error" in {
      form.errors.length shouldBe 0
    }
  }
}













































