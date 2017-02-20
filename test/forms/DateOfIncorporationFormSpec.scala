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

import models.DateOfIncorporationModel
import forms.DateOfIncorporationForm._
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import java.time.ZoneId
import java.util.Date

class DateOfIncorporationFormSpec extends UnitSpec with OneAppPerSuite{

  // set up border line conditions of today and future date (tomorrow)
  val date = new Date()
  val localDate = date.toInstant.atZone(ZoneId.systemDefault()).toLocalDate;
  val tomorrow = localDate.plusDays(1)
  val tomorrowDay: String = tomorrow.getDayOfMonth.toString
  val tomorrowMonth: String = tomorrow.getMonthValue.toString
  val tomorrowYear: String = tomorrow.getYear.toString

  val todayDay:String = localDate.getDayOfMonth.toString
  val todayMonth: String = localDate.getMonthValue.toString
  val todayYear: String = localDate.getYear.toString

  "Creating the form for the date of incorporation date" should {
    "return a populated form using .fill" in {
      val model = DateOfIncorporationModel(Some(10), Some(2), Some(2016))
      val form = dateOfIncorporationForm.fill(model)
      form.value.get shouldBe DateOfIncorporationModel(Some(10), Some(2), Some(2016))
    }

    "return a Some if a model with valid inputs is supplied using .bind" in {
      val map = Map(("incorporationDay", "10"), ("incorporationMonth", "3"), ("incorporationYear", "2016"))
      val form = dateOfIncorporationForm.bind(map)
      form.value shouldBe Some(DateOfIncorporationModel(Some(10), Some(3), Some(2016)))
    }

    "return a None if a model with non-numeric inputs is supplied using .bind" in {
      val map = Map(("incorporationDay", "a"), ("incorporationMonth", "b"), ("incorporationYear", "c"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }


    "return a None if a model with a single digit year is supplied using .bind" in {
      val map = Map(("incorporationDay", "2"), ("incorporationMonth", "2"), ("incorporationYear", "2"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a double digit year is supplied using .bind" in {
      val map = Map(("incorporationDay", "2"), ("incorporationMonth", "2"), ("incorporationYear", "22"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a triple digit year is supplied using .bind" in {
      val map = Map(("incorporationDay", "2"), ("incorporationMonth", "2"), ("incorporationYear", "222"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a day of 32 is supplied using .bind" in {
      val map = Map(("incorporationDay", "32"), ("incorporationMonth", "3"), ("incorporationYear", "1980"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 29th Feb in a non leap year is supplied using .bind" in {
      val map = Map(("incorporationDay", "29"), ("incorporationMonth", "2"), ("incorporationYear", "1981"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a Some if a model with a 29th Feb in a leap year is supplied using .bind" in {
      val map = Map(("incorporationDay", "29"), ("incorporationMonth", "2"), ("incorporationYear", "2004"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe false
    }

    "return a None if a model with a 31st june is supplied using .bind" in {
      val map = Map(("incorporationDay", "31"), ("incorporationMonth", "6"), ("incorporationYear", "1981"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st september is supplied using .bind" in {
      val map = Map(("incorporationDay", "31"), ("incorporationMonth", "9"), ("incorporationYear", "1981"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st November is supplied using .bind" in {
      val map = Map(("incorporationDay", "31"), ("incorporationMonth", "11"), ("incorporationYear", "1981"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st April is supplied using .bind" in {
      val map = Map(("incorporationDay", "31"), ("incorporationMonth", "4"), ("incorporationYear", "1981"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a non-valid date input is supplied using .bind" in {
      val map = Map(("incorporationDay", "32"), ("incorporationMonth", "4"), ("incorporationYear", "2016"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty day input is supplied using .bind" in {
      val map = Map(("incorporationDay", ""), ("incorporationMonth", "4"), ("incorporationYear", "2016"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty month input is supplied using .bind" in {
      val map = Map(("incorporationDay", "4"), ("incorporationMonth", ""), ("incorporationYear", "2016"))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty yer input is supplied using .bind" in {
      val map = Map(("incorporationDay", "4"), ("incorporationMonth", "5"), ("incorporationYear", ""))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a date in the future is supplied using .bind" in {
      val map = Map(("incorporationDay", tomorrowDay), ("incorporationMonth", tomorrowMonth), ("incorporationYear", tomorrowYear))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a Some date if a model with a non future date (today) is supplied using .bind" in {
      val map = Map(("incorporationDay", todayDay), ("incorporationMonth", todayMonth), ("incorporationYear", todayYear))
      val form = dateOfIncorporationForm.bind(map)
      form.hasErrors shouldBe false
    }

  }
}
