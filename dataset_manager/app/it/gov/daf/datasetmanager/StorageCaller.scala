package it.gov.daf.datasetmanager

import java.io.File
import java.net.URLEncoder

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import it.gov.daf.storagemanager.ActionAnyContent
import dataset_manager.yaml.StorageContent
// import it.gov.daf.storagemanager.client.Storage_managerClient
import caller.Storage_managerClient
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.Future
import scala.util.{Failure, Success}

import it.gov.daf.storagemanager.json._

import play.api.libs.ws._

import play.api.libs.json._

import javax.inject._

import play.api.libs.concurrent.Execution.Implicits._

object StorageCaller {

  import scala.concurrent.ExecutionContext.Implicits.global

  val uriStorageManager = ConfigFactory.load().getString("WebServices.storageUrl")

  def getDataset(format: String, chunk_size: Option[Int], uri: String, Authorization: String, limit: Option[Int]) // : Future[Successfull]
  = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    val client: AhcWSClient = AhcWSClient()
    val storageManager = new Storage_managerClient(client)(uriStorageManager)
    // val catalogManager = new _managerClient(client)(uriStorageManager)

    //val service = s"$uriCatalogManager/dataset-catalogs/$uri"
    //valL response = ingestionManager.connect(client)(service)

    val logical_uri = URLEncoder.encode(uri, "UTF-8")

    val response = {
      val controller: WSResponse => StorageContent = format match {
        case "json" => resp => StorageContent(Option(resp.body)) //resp.json
        case _ => resp => StorageContent(Option(resp.body))
      }


      storageManager.getDataset(format: String, chunk_size: Option[Int], uri: String,
        Authorization: String, limit: Option[Int], controller)

      // val res: Future[StorageContent] = response
      //   .map{
      //     case ActionAnyContent(value) =>
      //       println(s"value: $value")
      //       StorageContent(value)
      //     case ex => StorageContent(Some(s"ERROR $ex"))
      //   }

    }
    response
  }

}
