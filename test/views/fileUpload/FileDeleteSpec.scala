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

package views.fileUpload

import controllers.routes
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.test.Helpers._
import views.helpers.ViewSpec
import views.html.fileUpload.FileDelete

class FileDeleteSpec  extends ViewSpec {

  val fileName = "test.pdf"
  val fileID = "1"

  "The FileUpload page" should {

    "contain the correct elements when loaded with a fileID and file name" in {

      lazy val page = FileDelete(fileID, fileName)(fakeRequest)
      lazy val document = Jsoup.parse(contentAsString(page))

      //title and heading
      document.title() shouldBe Messages("page.fileDelete.title")

      document.body.getElementById("main-heading").text() shouldBe Messages("page.fileDelete.heading")
      document.body.getElementById("file-delete-hint").text() shouldBe Messages("page.fileDelete.hint", fileName)
      document.body.getElementById("file-remove").text() shouldBe Messages("page.fileDelete.confirm")
      document.body.getElementById("file-cancel").text() shouldBe Messages("page.fileDelete.cancel")
    }
  }

}
