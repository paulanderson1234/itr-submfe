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

package services

import auth.TAVCUser
import common.{Constants, KeystoreKeys}
import connectors.{FileUploadConnector, S4LConnector, SubmissionConnector}
import models.fileUpload.{Envelope, EnvelopeFile, MetadataModel}
import play.api.Logger

import play.mvc.Http.Status._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}


object FileUploadService extends FileUploadService {
  override lazy val fileUploadConnector = FileUploadConnector
  override lazy val s4lConnector = S4LConnector
  override lazy val submissionConnector = SubmissionConnector
}

trait FileUploadService {

  final val PDF = "application/pdf"
  final val XML = "application/xml"

  val fileUploadConnector: FileUploadConnector
  val s4lConnector: S4LConnector
  val submissionConnector: SubmissionConnector

  def validateFile(envelopeID: String, fileName: String, fileSize: Int)
                  (implicit hc: HeaderCarrier, ex: ExecutionContext): Future[Seq[Boolean]] = {

    val lessThanFiveMegabytes: Int => Boolean = length => length <= Constants.fileSizeLimit
    val isPDF: String => Boolean = fileName => fileName.matches("""([\w]\S*?\.[pP][dD][fF])""")

    def fileNameUnique(envelopeID: String, fileName: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[Boolean] = {

      def compareFilenames(files: Seq[EnvelopeFile], index: Int = 0): Boolean = {
        if(files(index).name.equalsIgnoreCase(fileName)) false
        else if(index < files.length - 1) compareFilenames(files, index + 1)
        else true
      }

      getEnvelopeFiles(envelopeID).map {
        case files if files.nonEmpty => {
          compareFilenames(files)
        }
        case _ => true
      }
    }

    fileNameUnique(envelopeID, fileName).map {
      nameUnique => Seq(nameUnique, lessThanFiveMegabytes(fileSize), isPDF(fileName))
    }

  }

  def belowFileNumberLimit(envelopeID: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[Boolean] = {
    getEnvelopeFiles(envelopeID).map {
      files => files.size < Constants.numberOfFilesLimit
    }
  }


  def getEnvelopeID(createNewID: Boolean = true)(implicit hc: HeaderCarrier, ex: ExecutionContext, user: TAVCUser): Future[String] = {
    s4lConnector.fetchAndGetFormData[String](KeystoreKeys.envelopeID).flatMap {
      case Some(envelopeID) if envelopeID.nonEmpty => Future.successful(envelopeID)
      case _ => if (createNewID) {
        submissionConnector.createEnvelope().map {
          result =>
            val envelopeID = result.json.\("envelopeID").as[String]
            s4lConnector.saveFormData(KeystoreKeys.envelopeID, envelopeID)
            envelopeID
        }
      } else {
        Future.successful("")
      }
    }
  }

  def checkEnvelopeStatus(envelopeID: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[Option[Envelope]] = {
      submissionConnector.getEnvelopeStatus(envelopeID).map {
      result => result.status match {
        case OK =>
          result.json.asOpt[Envelope]
        case _ => Logger.warn(s"[FileUploadConnector][checkEnvelopeStatus] Error ${result.status} received.")
          None
      }
    }
  }

  def uploadFile(file: Array[Byte], fileName: String, envelopeID: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[HttpResponse] = {
    for {
      fileID <- generateFileID(envelopeID)
      result <- fileUploadConnector.addFileContent(envelopeID, fileID, fileName, file, PDF)
    } yield result.status match {
      case OK =>
        HttpResponse(result.status)
      case _ =>
        Logger.warn(s"[FileUploadConnector][uploadFile] Error ${result.status} received.")
        HttpResponse(result.status)
    }
  }

  def getEnvelopeFiles(envelopeID: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[Seq[EnvelopeFile]] = {
    checkEnvelopeStatus(envelopeID).map {
      case Some(envelope)=> envelope.files.getOrElse(Seq())
      case _ => Seq()
    }
  }

  def closeEnvelope(tavcRef: String)(implicit hc: HeaderCarrier, ex: ExecutionContext, user: TAVCUser): Future[HttpResponse] = {
    getEnvelopeID(createNewID = false).flatMap {
      envelopeID => if(envelopeID.nonEmpty) {
        addMetadataFile(envelopeID, tavcRef).flatMap[HttpResponse] {
          case true => submissionConnector.closeEnvelope(envelopeID).map {
            result => result.status match {
              case OK =>
                s4lConnector.saveFormData(KeystoreKeys.envelopeID, "")
                result
              case _ => Logger.warn(s"[FileUploadConnector][closeEnvelope] Error ${result.status} received.")
                s4lConnector.saveFormData(KeystoreKeys.envelopeID, "")
                result
            }
          }
          case false => Logger.warn(s"[FileUploadConnector][closeEnvelope] Error false false received.")
            s4lConnector.saveFormData(KeystoreKeys.envelopeID, "")
            Future.successful(HttpResponse(INTERNAL_SERVER_ERROR))
        }
      } else {
        Future.successful(HttpResponse(OK))
      }

    }.recover {
      case e: Exception => Logger.warn(s"[FileUploadConnector][closeEnvelope] Error ${e.getMessage} received.")
        s4lConnector.saveFormData(KeystoreKeys.envelopeID, "")
        HttpResponse(INTERNAL_SERVER_ERROR)
    }
  }

  private def generateFileID(envelopeID: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[Int] = {
    submissionConnector.getEnvelopeStatus(envelopeID).map {
      result =>
        val envelope = result.json.as[Envelope]
        if(envelope.files.isDefined) {
          envelope.files.get.last.id.toInt + 1
        } else {
          1
        }
    }
  }

  private def addMetadataFile(envelopeID: String, tavcRef: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[Boolean] = {
    generateFileID(envelopeID).flatMap {
      fileID =>
        fileUploadConnector.addFileContent(envelopeID, fileID, s"$envelopeID.xml", MetadataModel(envelopeID, tavcRef).getControlFile, XML).map {
        result => result.status match {
          case OK => true
          case _ => Logger.warn(s"[FileUploadConnector][closeEnvelope] Error ${result.status} received.")
            false
        }
      }
    }
  }

}
