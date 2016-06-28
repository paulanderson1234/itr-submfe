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

package common

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}
import org.joda.time.DateTime

object Dates {

  val sf = new SimpleDateFormat("dd/MM/yyyy")
  val datePageFormat = new SimpleDateFormat("dd MMMM yyyy")
  val datePageFormatNoZero = new SimpleDateFormat("d MMMM yyyy")
  val nowDate = new Date();

  DateTime.now

  def constructDate (day: Int, month: Int, year: Int): Date = {
    sf.parse(s"$day/$month/$year")
  }

  def dateInFuture (date: Date): Boolean = {
    date.after(DateTime.now.toDate)
  }

  def dateNotInFuture (day: Int, month: Int, year: Int): Boolean = {
    !constructDate(day, month, year).after(DateTime.now.toDate)
  }

  def dateIsFuture (day: Int, month: Int, year: Int): Boolean = {
    constructDate(day, month, year).after(DateTime.now.toDate)
  }
}