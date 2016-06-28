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

import models.DateOfFirstSaleModel
import uk.gov.hmrc.play.test.UnitSpec
import forms.DateOfFirstSaleForm._
import java.time.ZoneId
import java.util.Date


class DateOfFirstSaleFormSpec extends UnitSpec {

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

  "Creating the form for the date of first sale date" should {
    "return a populated form using .fill" in {
      val model = DateOfFirstSaleModel(10, 2, 2016)
      val form = dateOfFirstSaleForm.fill(model)
      form.value.get shouldBe DateOfFirstSaleModel(10, 2, 2016)
    }

    "return a Some if a model with valid inputs is supplied using .bind" in {
      val map = Map(("dateOfFirstSaleDay", "10"), ("dateOfFirstSaleMonth", "3"), ("dateOfFirstSaleYear", "2016"))
      val form = dateOfFirstSaleForm.bind(map)
      form.value shouldBe Some(DateOfFirstSaleModel(10, 3, 2016))
    }

    "return a None if a model with non-numeric inputs is supplied using .bind" in {
      val map = Map(("dateOfFirstSaleDay", "a"), ("dateOfFirstSaleMonth", "b"), ("dateOfFirstSaleYear", "c"))
      val form = dateOfFirstSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a non-valid date input is supplied using .bind" in {
      val map = Map(("dateOfFirstSaleDay", "32"), ("dateOfFirstSaleMonth", "4"), ("dateOfFirstSaleYear", "2016"))
      val form = dateOfFirstSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with an empty input is supplied using .bind" in {

     val map = Map(("dateOfFirstSaleDay", ""), ("dateOfFirstSaleMonth", "4"), ("dateOfFirstSaleYear", "2016"))
      val form = dateOfFirstSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a None if a model with a date in the future is supplied using .bind" in {
      val map = Map(("dateOfFirstSaleDay", tomorrowDay), ("dateOfFirstSaleMonth", tomorrowMonth), ("dateOfFirstSaleYear", tomorrowYear))
      val form = dateOfFirstSaleForm.bind(map)
      form.hasErrors shouldBe true
    }

    "return a Some date if a model with a non future date (today) is supplied using .bind" in {
      val map = Map(("dateOfFirstSaleDay", todayDay), ("dateOfFirstSaleMonth", todayMonth), ("dateOfFirstSaleYear", todayYear))
      val form = dateOfFirstSaleForm.bind(map)
      form.hasErrors shouldBe false
    }

  }
}
