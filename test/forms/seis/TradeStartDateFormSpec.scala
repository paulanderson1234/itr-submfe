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

import models.TradeStartDateModel
import forms.TradeStartDateForm._
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import java.time.ZoneId
import java.util.Date
import common.Constants

class TradeStartDateFormSpec extends UnitSpec with OneAppPerSuite {

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

  "Creating the form for the trade start date" should {
    "return a populated yes form using .fill" in {
      val model = TradeStartDateModel(Constants.StandardRadioButtonYesValue, Some(10), Some(2), Some(2016))
      val form = tradeStartDateForm.fill(model)
      form.value.get shouldBe TradeStartDateModel(Constants.StandardRadioButtonYesValue, Some(10), Some(2), Some(2016))
    }

    "return a populated no form using .fill" in {
      val model = TradeStartDateModel(Constants.StandardRadioButtonNoValue, None, None, None)
      val form = tradeStartDateForm.fill(model)
      form.value.get shouldBe TradeStartDateModel(Constants.StandardRadioButtonNoValue, None, None, None)
    }

    "return a Some if a model with valid inputs is supplied using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonYesValue),("tradeStartDay", "10"), ("tradeStartMonth", "3"), ("tradeStartYear", "2016"))
      val form = tradeStartDateForm.bind(map)
      form.value shouldBe Some(TradeStartDateModel(Constants.StandardRadioButtonYesValue, Some(10), Some(3), Some(2016)))
    }

    "return a None if a model with non-numeric inputs is supplied using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonYesValue),("tradeStartDay", "a"), ("tradeStartMonth", "b"), ("tradeStartYear", "c"))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and date present using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonNoValue),("tradeStartDay", "2"), ("tradeStartMonth", "4"), ("tradeStartYear", "2006"))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with day present using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonNoValue),("tradeStartDay", "2"), ("tradeStartMonth", ""), ("tradeStartYear", ""))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with month present using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonNoValue),("tradeStartDay", ""), ("tradeStartMonth", "2"), ("tradeStartYear", ""))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with year present using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonNoValue),("tradeStartDay", ""), ("tradeStartMonth", ""), ("tradeStartYear", "2006"))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with invalid day present using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonNoValue),("tradeStartDay", "f"), ("tradeStartMonth", ""), ("tradeStartYear", ""))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with invalid month present using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonNoValue),("tradeStartDay", ""), ("tradeStartMonth", "z"), ("tradeStartYear", ""))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with invalid year present using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonNoValue),("tradeStartDay", ""), ("tradeStartMonth", ""), ("tradeStartYear", "q"))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with both a 'No' selection and partial date with year in future using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonNoValue),("tradeStartDay", ""), ("tradeStartMonth", ""), ("tradeStartYear", "9999"))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a single digit year is supplied using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonYesValue),("tradeStartDay", "2"), ("tradeStartMonth", "2"), ("tradeStartYear", "2"))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a double digit year is supplied using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonYesValue),("tradeStartDay", "2"), ("tradeStartMonth", "2"), ("tradeStartYear", "22"))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a triple digit year is supplied using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonYesValue),("tradeStartDay", "2"), ("tradeStartMonth", "2"), ("tradeStartYear", "222"))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a day of 32 is supplied using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonYesValue),("tradeStartDay", "32"), ("tradeStartMonth", "3"), ("tradeStartYear", "1980"))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 29th Feb in a non leap year is supplied using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonYesValue),("tradeStartDay", "29"), ("tradeStartMonth", "2"), ("tradeStartYear", "1981"))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a Some if a model with a 29th Feb in a leap year is supplied using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonYesValue),("tradeStartDay", "29"), ("tradeStartMonth", "2"), ("tradeStartYear", "2004"))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe false
    }

    "return a None if a model with a 31st june is supplied using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonYesValue),("tradeStartDay", "31"), ("tradeStartMonth", "6"), ("tradeStartYear", "1981"))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st september is supplied using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonYesValue),("tradeStartDay", "31"), ("tradeStartMonth", "9"), ("tradeStartYear", "1981"))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st November is supplied using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonYesValue),("tradeStartDay", "31"), ("tradeStartMonth", "11"), ("tradeStartYear", "1981"))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a 31st April is supplied using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonYesValue),("tradeStartDay", "31"), ("tradeStartMonth", "4"), ("tradeStartYear", "1981"))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a non-valid date input is supplied using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonYesValue),("tradeStartDay", "32"), ("tradeStartMonth", "4"), ("tradeStartYear", "2016"))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty day input is supplied using .bind" in {

      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonYesValue),("tradeStartDay", ""), ("tradeStartMonth", "4"), ("tradeStartYear", "2016"))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty month input is supplied using .bind" in {

      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonYesValue),("tradeStartDay", "5"), ("tradeStartMonth", ""), ("tradeStartYear", "2016"))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty year input is supplied using .bind" in {

      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonYesValue),("tradeStartDay", "5"), ("tradeStartMonth", "10"), ("tradeStartYear", ""))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a date in the future is supplied using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonYesValue),("tradeStartDay", tomorrowDay), ("tradeStartMonth", tomorrowMonth), ("tradeStartYear", tomorrowYear))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a Some date if a model with a non future date (today) is supplied using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonYesValue),("tradeStartDay", todayDay), ("tradeStartMonth", todayMonth), ("tradeStartYear", todayYear))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe false
    }

    "return a Some date if a model with no date an 'No' option is supplied using .bind" in {
      val map = Map(("hasTradeStartDate",Constants.StandardRadioButtonNoValue),("tradeStartDay", ""), ("tradeStartMonth", ""), ("tradeStartYear", ""))
      val form = tradeStartDateForm.bind(map)
      form.hasErrors shouldBe false
    }

  }
}
