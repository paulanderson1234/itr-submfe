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

package utils

import models.{CommercialSaleModel, DateOfIncorporationModel}
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

class DateFormatterSpec extends UnitSpec with MockitoSugar{

  // Encapulate test fixtures in private variables.
  private val validDateMarch = (1,3,1993)
  private val validDateSeptember= (23,9,1944)
  private val validDateNovember= (13,11,2015)
  private val inValidDateNoneLeapYear= (29,2,2015)

  private val validDateMarchFormatted = "01 March 1993"
  private val validDateSeptemberFormatted= "23 September 1944"
  private val validDateNovemberFormatted= "13 November 2015"
  private val inValidDateNonLeapYearFormatted= ""

  "The Date Formatter" should {

    "Return the date correctly formatted from the given day, month and year interger values" in {
      CommercialSaleModel.toDateString(validDateMarch._1,validDateMarch._2,validDateMarch._3) shouldBe (validDateMarchFormatted)
      CommercialSaleModel.toDateString(validDateSeptember._1,validDateSeptember._2,validDateSeptember._3) shouldBe (validDateSeptemberFormatted)
      CommercialSaleModel.toDateString(validDateNovember._1,validDateNovember._2,validDateNovember._3) shouldBe (validDateNovemberFormatted)
      CommercialSaleModel.toDateString(inValidDateNoneLeapYear._1,inValidDateNoneLeapYear._2,inValidDateNoneLeapYear._3) shouldBe
        (inValidDateNonLeapYearFormatted)
    }
  }
}
