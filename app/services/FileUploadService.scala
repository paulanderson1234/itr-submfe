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

import java.io.File

import auth.TAVCUser
import common.KeystoreKeys
import config.FrontendAppConfig
import connectors.{FileUploadConnector, S4LConnector}
import models.upload.{Envelope, EnvelopeFile}
import play.api.Logger
import play.api.libs.ws.WSResponse

import scala.util.control.Breaks._
import play.mvc.Http.Status._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}


object FileUploadService extends FileUploadService {
  override lazy val fileUploadConnector = FileUploadConnector
  override lazy val serviceURL = FrontendAppConfig.fileUploadUrl
  override lazy val s4lConnector = S4LConnector
}

trait FileUploadService {

  final val PDF = "application/pdf"
  final val XML = "application/xml"

  val fileUploadConnector: FileUploadConnector
  val s4lConnector: S4LConnector
  val serviceURL: String

  def getEnvelopeID(createNewID: Boolean = true)(implicit hc: HeaderCarrier, ex: ExecutionContext, user: TAVCUser): Future[String] = {
    s4lConnector.fetchAndGetFormData[String](KeystoreKeys.envelopeID).flatMap {
      case Some(envelopeID) if envelopeID.nonEmpty => Future.successful(envelopeID)
      case _ => if (createNewID) {
        fileUploadConnector.createEnvelope().map {
          result =>
            val envelopeID = result.header("Location").get.replaceAll("""[:.\-a-z].+\/file-upload\/envelopes\/""", "")
            s4lConnector.saveFormData(KeystoreKeys.envelopeID, envelopeID)
            envelopeID
        }
      } else {
        Future.successful("")
      }
    }
  }

  def checkEnvelopeStatus(implicit hc: HeaderCarrier, ex: ExecutionContext, user: TAVCUser): Future[Option[Envelope]] = {
    for {
      envelopeID <- getEnvelopeID()
      result <- fileUploadConnector.getEnvelopeStatus(envelopeID)
    } yield result.status match {
      case OK =>
        result.json.asOpt[Envelope]
      case _ => Logger.warn(s"[FileUploadConnector][checkEnvelopeStatus] Error ${result.status} received.")
        None
    }
  }

  def generateFileID(envelopeID: String)(implicit hc: HeaderCarrier, ex: ExecutionContext, user: TAVCUser): Future[Int] = {
    fileUploadConnector.getEnvelopeStatus(envelopeID).map {
      result =>
        val envelope = result.json.as[Envelope]
        if(envelope.files.isDefined) {
          envelope.files.get.last.id.toInt + 1
        } else {
          1
        }
    }
  }

  def uploadFile(file: File)(implicit hc: HeaderCarrier, ex: ExecutionContext, user: TAVCUser): Future[WSResponse] = {
    for {
      envelopeID <- getEnvelopeID()
      fileID <- generateFileID(envelopeID)
      result <- fileUploadConnector.addFileContent(envelopeID, fileID, file, PDF)
    } yield result.status match {
      case OK =>
        result
      case _ =>
        Logger.warn(s"[FileUploadConnector][uploadFile] Error ${result.status} received.")
        result
    }
  }

  def getEnvelopeFiles(implicit hc: HeaderCarrier, ex: ExecutionContext, user: TAVCUser): Future[Seq[EnvelopeFile]] = {
    checkEnvelopeStatus.map {
      case Some(envelope)=> envelope.files.getOrElse(Seq())
      case _ => Seq()
    }
  }

  def closeEnvelope(implicit hc: HeaderCarrier, ex: ExecutionContext, user: TAVCUser): Future[HttpResponse] = {
    getEnvelopeID(createNewID = false).flatMap {
      envelopeID => if(envelopeID.nonEmpty) {
        generateAdditionalFiles(envelopeID).flatMap[HttpResponse] {
          case true => fileUploadConnector.closeEnvelope(envelopeID).map {
            result => result.status match {
              case CREATED =>
                //delete envelope from session
                s4lConnector.saveFormData(KeystoreKeys.envelopeID, "")
                result
              case _ => Logger.warn(s"[FileUploadConnector][closeEnvelope] Error ${result.status} received.")
                // delete envelope from session
                s4lConnector.saveFormData(KeystoreKeys.envelopeID, "")
                result
            }
          }
          case false => Logger.warn(s"[FileUploadConnector][closeEnvelope] Error false false received.")
            //delete envelope from session
            s4lConnector.saveFormData(KeystoreKeys.envelopeID, "")
            Future.successful(HttpResponse(INTERNAL_SERVER_ERROR))
        }
      } else {
        Future.successful(HttpResponse(INTERNAL_SERVER_ERROR))
      }

    }.recover {
      case e: Exception => Logger.warn(s"[FileUploadConnector][closeEnvelope] Error ${e.getMessage} received.")
        //delete envelope from session
        s4lConnector.saveFormData(KeystoreKeys.envelopeID, "")
        HttpResponse(INTERNAL_SERVER_ERROR)
    }
  }

  private def addControlFiles(envelopeID: String, files: Seq[EnvelopeFile])
                             (implicit hc: HeaderCarrier, ex: ExecutionContext, user: TAVCUser): Future[Boolean] = {

    def generateControlFile: Future[File] = Future.successful(File.createTempFile("hello", ".xml"))

    tryBreakable {
      for (file <- files) {
        for {
          fileID <- generateFileID(envelopeID)
          controlFile <- generateControlFile
          result <- fileUploadConnector.addFileContent(envelopeID, fileID, controlFile, XML)
        } yield if(result.status != OK) break
      }
      Future.successful(true)
    } catchBreak {
      Future.successful(false)
    }
  }

  private def addManifestFile(envelopeID: String)(implicit hc: HeaderCarrier, ex: ExecutionContext, user: TAVCUser): Future[Boolean] = {

    val manifestFile = File.createTempFile("hello", ".xml")

    fileUploadConnector.addFileContent(envelopeID, OK, manifestFile, XML).map {
      result => result.status match {
        case OK => true
        case _ => Logger.warn(s"[FileUploadConnector][closeEnvelope] Error ${result.status} received.")
          false
      }
    }
  }

  private def generateAdditionalFiles(envelopeID: String)(implicit hc: HeaderCarrier, ex: ExecutionContext, user: TAVCUser): Future[Boolean] = {
    for {
      envelopeFiles <- getEnvelopeFiles
      controlFilesUploaded <- addControlFiles(envelopeID,envelopeFiles)
      manifestFileUploaded <- addManifestFile(envelopeID)
    } yield (controlFilesUploaded, manifestFileUploaded) match {
      case (true, true) => true
      case (_,_) => false
    }
  }

}
