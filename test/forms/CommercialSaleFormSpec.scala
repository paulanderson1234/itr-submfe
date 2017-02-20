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

import models.CommercialSaleModel
import forms.CommercialSaleForm._
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import java.time.ZoneId
import java.util.Date
import common.Constants

class CommercialSaleFormSpec extends UnitSpec with OneAppPerSuite {

  // set up border line conditions of today and future date (tomorrow)
  val date = new Date();
  val localDate = date.toInstant.atZone(ZoneId.systemDefault()).toLocalDate;
  val tomorrow = localDate.plusDays(1)
  val tomorrowDay: String = tomorrow.getDayOfMonth.toString
  val tomorrowMonth: String = tomorrow.getMonthValue.toString
  val tomorrowYear: String = tomorrow.getYear.toString

  val todayDay:String = localDate.getDayOfMonth.toString
  val todayMonth: String = localDate.getMonthValue.toString
  val todayYear: String = localDate.getYear.toString

  "Creating the form for the date of incorporation date" should {
    "return a populated yes form using .fill" in {
      val model = CommercialSaleModel(Constants.StandardRadioButtonYesValue, Some(10), Some(2), Some(2016))
      val form = commercialSaleForm.fill(model)
      form.value.get shouldBe CommercialSaleModel(Constants.StandardRadioButtonYesValue, Some(10), Some(2), Some(2016))
    }

    "return a populated no form using .fill" in {
      val model = CommercialSaleModel(Constants.StandardRadioButtonNoValue, None, None, None)
      val form = commercialSaleForm.fill(model)
      form.value.get shouldBe CommercialSaleModel(Constants.StandardRadioButtonNoValue, None, None, None)
    }

    "return a Some if a model with valid inputs is supplied using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonYesValue),("commercialSaleDay", "10"), ("commercialSaleMonth", "3"), ("commercialSaleYear", "2016"))
      val form = commercialSaleForm.bind(map)
      form.value shouldBe Some(CommercialSaleModel(Constants.StandardRadioButtonYesValue, Some(10), Some(3), Some(2016)))
    }

    "return a None if a model with non-numeric inputs is supplied using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonYesValue),("commercialSaleDay", "a"), ("commercialSaleMonth", "b"), ("commercialSaleYear", "c"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and date present using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonNoValue),("commercialSaleDay", "2"), ("commercialSaleMonth", "4"), ("commercialSaleYear", "2006"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with day present using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonNoValue),("commercialSaleDay", "2"), ("commercialSaleMonth", ""), ("commercialSaleYear", ""))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with month present using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonNoValue),("commercialSaleDay", ""), ("commercialSaleMonth", "2"), ("commercialSaleYear", ""))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with year present using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonNoValue),("commercialSaleDay", ""), ("commercialSaleMonth", ""), ("commercialSaleYear", "2006"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with invalid day present using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonNoValue),("commercialSaleDay", "f"), ("commercialSaleMonth", ""), ("commercialSaleYear", ""))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with invalid month present using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonNoValue),("commercialSaleDay", ""), ("commercialSaleMonth", "z"), ("commercialSaleYear", ""))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with invalid year present using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonNoValue),("commercialSaleDay", ""), ("commercialSaleMonth", ""), ("commercialSaleYear", "q"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with year in future using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonNoValue),("commercialSaleDay", ""), ("commercialSaleMonth", ""), ("commercialSaleYear", "9999"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a single digit year is supplied using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonYesValue),("commercialSaleDay", "2"), ("commercialSaleMonth", "2"), ("commercialSaleYear", "2"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a double digit year is supplied using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonYesValue),("commercialSaleDay", "2"), ("commercialSaleMonth", "2"), ("commercialSaleYear", "22"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a triple digit year is supplied using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonYesValue),("commercialSaleDay", "2"), ("commercialSaleMonth", "2"), ("commercialSaleYear", "222"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a day of 32 is supplied using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonYesValue),("commercialSaleDay", "32"), ("commercialSaleMonth", "3"), ("commercialSaleYear", "1980"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 29th Feb in a non leap year is supplied using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonYesValue),("commercialSaleDay", "29"), ("commercialSaleMonth", "2"), ("commercialSaleYear", "1981"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a Some if a model with a 29th Feb in a leap year is supplied using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonYesValue),("commercialSaleDay", "29"), ("commercialSaleMonth", "2"), ("commercialSaleYear", "2004"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe false
    }

    "return a None if a model with a 31st june is supplied using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonYesValue),("commercialSaleDay", "31"), ("commercialSaleMonth", "6"), ("commercialSaleYear", "1981"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st september is supplied using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonYesValue),("commercialSaleDay", "31"), ("commercialSaleMonth", "9"), ("commercialSaleYear", "1981"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st November is supplied using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonYesValue),("commercialSaleDay", "31"), ("commercialSaleMonth", "11"), ("commercialSaleYear", "1981"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st April is supplied using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonYesValue),("commercialSaleDay", "31"), ("commercialSaleMonth", "4"), ("commercialSaleYear", "1981"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a non-valid date input is supplied using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonYesValue),("commercialSaleDay", "32"), ("commercialSaleMonth", "4"), ("commercialSaleYear", "2016"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty day input is supplied using .bind" in {

      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonYesValue),("commercialSaleDay", ""), ("commercialSaleMonth", "4"), ("commercialSaleYear", "2016"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty month input is supplied using .bind" in {

      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonYesValue),("commercialSaleDay", "5"), ("commercialSaleMonth", ""), ("commercialSaleYear", "2016"))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty year input is supplied using .bind" in {

      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonYesValue),("commercialSaleDay", "5"), ("commercialSaleMonth", "10"), ("commercialSaleYear", ""))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a date in the future is supplied using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonYesValue),("commercialSaleDay", tomorrowDay), ("commercialSaleMonth", tomorrowMonth), ("commercialSaleYear", tomorrowYear))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a Some date if a model with a non future date (today) is supplied using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonYesValue),("commercialSaleDay", todayDay), ("commercialSaleMonth", todayMonth), ("commercialSaleYear", todayYear))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe false
    }

    "return a Some date if a model with no date an 'No' option is supplied using .bind" in {
      val map = Map(("hasCommercialSale",Constants.StandardRadioButtonNoValue),("commercialSaleDay", ""), ("commercialSaleMonth", ""), ("commercialSaleYear", ""))
      val form = commercialSaleForm.bind(map)
      form.hasErrors shouldBe false
    }

  }
}
