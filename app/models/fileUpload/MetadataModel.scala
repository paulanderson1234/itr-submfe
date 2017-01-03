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

package models.fileUpload

import org.joda.time.format.DateTimeFormatterBuilder
import uk.gov.hmrc.time.DateTimeUtils

case class MetadataModel(envelopeID: String, tavcRef: String) {

  def getControlFile: Array[Byte] = controlFormat.getBytes

  private val caseID = tavcRef.replaceFirst("[A-Z]{2}TAVC", "TAVC-")

  private def getTimestamp: String = {
    val formatter = new DateTimeFormatterBuilder()
      .appendDayOfMonth(2)
      .appendLiteral("/")
      .appendMonthOfYear(2)
      .appendLiteral("/")
      .appendYear(1, 4)
      .appendLiteral(" ")
      .appendHourOfDay(2)
      .appendLiteral(":")
      .appendMinuteOfHour(2)
      .appendLiteral(":")
      .appendSecondOfMinute(2)
      .toFormatter
    DateTimeUtils.now.toString(formatter)
  }

  private val controlFormat = s"""<?xml version="1.0" encoding="UTF-8" standalone="no"?>
    |<documents xmlns="http://govtalk.gov.uk/hmrc/gis/content/1">
    |  <document>
    |    <header>
    |      <format>pdf</format>
    |      <source>tavc</source>
    |      <target>CFS</target>
    |      <reconciliation_id>$envelopeID</reconciliation_id>
    |    </header>
    |    <metadata>
    |      <attribute>
    |        <attribute_name>hmrc_time_of_receipt</attribute_name>
    |        <attribute_type>time</attribute_type>
    |        <attribute_values>
    |          <attribute_value>$getTimestamp</attribute_value>
    |        </attribute_values>
    |      </attribute>
    |      <attribute>
    |        <attribute_name>customer_id</attribute_name>
    |        <attribute_type>string</attribute_type>
    |        <attribute_values>
    |          <attribute_value>$tavcRef</attribute_value>
    |        </attribute_values>
    |      </attribute>
    |      <attribute>
    |        <attribute_name>case_id</attribute_name>
    |        <attribute_type>string</attribute_type>
    |        <attribute_values>
    |          <attribute_value>$caseID</attribute_value>
    |        </attribute_values>
    |      </attribute>
    |    </metadata>
    |  </document>
    |</documents>""".stripMargin

}
