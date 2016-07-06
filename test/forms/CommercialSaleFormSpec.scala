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

import models.{CommercialSaleModel}
import forms.CommercialSaleForm._
import uk.gov.hmrc.play.test.UnitSpec
import java.time.ZoneId
import java.util.Date


class CommercialSaleFormSpec extends UnitSpec {

  // set up border line conditions of today and future date (tomorrow)
  val date = new Date();
  val localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
  val tomorrow = localDate.plusDays(1)
  val tomorrowDay: String = tomorrow.getDayOfMonth.toString
  val tomorrowMonth: String = tomorrow.getMonthValue.toString
  val tomorrowYear: String = tomorrow.getYear.toString

  val todayDay:String = localDate.getDayOfMonth.toString
  val todayMonth: String = localDate.getMonthValue.toString
  val todayYear: String = localDate.getYear.toString

  "Creating the form for the date of incorporation date" should {
    "return a populated yes form using .fill" in {
      val model = CommercialSaleModel("Yes", Some(10), Some(2), Some(2016))
      val form = commercialSaleForm.fill(model)
      form.value.get shouldBe CommercialSaleModel("Yes", Some(10), Some(2), Some(2016))
    }

    "return a populated no form using .fill" in {
      val model = CommercialSaleModel("No", None, None, None)
      val form = commercialSaleForm.fill(model)
      form.value.get shouldBe CommercialSaleModel("No", None, None, None)
    }

    "return a Some if a model with valid inputs is supplied using .bind" in {
      val map = Map(("hasCommercialSale","Yes"),("day", "10"), ("month", "3"), ("year", "2016"))
      val form = commercialSaleForm.bind(map)
      form.value shouldBe Some(CommercialSaleModel("Yes", Some(10), Some(3), Some(2016)))
    }

    "return a None if a model with non-numeric inputs is supplied using .bind" in {
      val map = Map(("hasCommercialSale","Yes"),("day", "a"), ("month", "b"), ("year", "c"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a single digit year is supplied using .bind" in {
      val map = Map(("hasCommercialSale","Yes"),("day", "2"), ("month", "2"), ("year", "2"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a double digit year is supplied using .bind" in {
      val map = Map(("hasCommercialSale","Yes"),("day", "2"), ("month", "2"), ("year", "22"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a triple digit year is supplied using .bind" in {
      val map = Map(("hasCommercialSale","Yes"),("day", "2"), ("month", "2"), ("year", "222"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a day of 32 is supplied using .bind" in {
      val map = Map(("hasCommercialSale","Yes"),("day", "32"), ("month", "3"), ("year", "1980"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 29th Feb in a non leap year is supplied using .bind" in {
      val map = Map(("hasCommercialSale","Yes"),("day", "29"), ("month", "2"), ("year", "1981"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a Some if a model with a 29th Feb in a leap year is supplied using .bind" in {
      val map = Map(("hasCommercialSale","Yes"),("day", "29"), ("month", "2"), ("year", "2004"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe false
    }

    "return a None if a model with a 31st june is supplied using .bind" in {
      val map = Map(("hasCommercialSale","Yes"),("day", "31"), ("month", "6"), ("year", "1981"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st september is supplied using .bind" in {
      val map = Map(("hasCommercialSale","Yes"),("day", "31"), ("month", "9"), ("year", "1981"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st November is supplied using .bind" in {
      val map = Map(("hasCommercialSale","Yes"),("day", "31"), ("month", "11"), ("year", "1981"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st April is supplied using .bind" in {
      val map = Map(("hasCommercialSale","Yes"),("day", "31"), ("month", "4"), ("year", "1981"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a non-valid date input is supplied using .bind" in {
      val map = Map(("hasCommercialSale","Yes"),("day", "32"), ("month", "4"), ("year", "2016"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty day input is supplied using .bind" in {

      val map = Map(("hasCommercialSale","Yes"),("day", ""), ("month", "4"), ("year", "2016"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty month input is supplied using .bind" in {

      val map = Map(("hasCommercialSale","Yes"),("day", "5"), ("month", ""), ("year", "2016"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty year input is supplied using .bind" in {

      val map = Map(("hasCommercialSale","Yes"),("day", "5"), ("month", "10"), ("year", ""))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a date in the future is supplied using .bind" in {
      val map = Map(("hasCommercialSale","Yes"),("day", tomorrowDay), ("month", tomorrowMonth), ("year", tomorrowYear))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a Some date if a model with a non future date (today) is supplied using .bind" in {
      val map = Map(("hasCommercialSale","Yes"),("day", todayDay), ("month", todayMonth), ("year", todayYear))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe false
    }

    "return a Some date if a model with no date an 'No' option is supplied using .bind" in {
      val map = Map(("hasCommercialSale","No"),("day", ""), ("month", ""), ("year", ""))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe false
    }

  }
}
