package it.gov.daf.datasetmanager

import java.io.File
import java.net.URLEncoder

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import it.gov.daf.storagemanager.ActionAnyContent
import dataset_manager.yaml.StorageContent
// import it.gov.daf.storagemanager.client.Storage_managerClient
import caller.{ Storage_managerClient, Catalog_managerClient }
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.Future
import scala.util.{Failure, Success}

import it.gov.daf.storagemanager.json._

import play.api.libs.ws._

import play.api.libs.json._

import javax.inject._

import play.api.libs.concurrent.Execution.Implicits._

object StorageCaller {

  val uriStorageManager = ConfigFactory.load().getString("WebServices.storageUrl")
  val uriCatalogManager = ConfigFactory.load().getString("WebServices.catalogUrl")

  def getDataset(format: String, chunk_size: Option[Int], uri: String, Authorization: String, limit: Option[Int]) // : Future[Successfull]
  = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    val client: AhcWSClient = AhcWSClient()
    val storageManager = new Storage_managerClient(client)(uriStorageManager)
    val catalogManager = new Catalog_managerClient(client)(uriCatalogManager)

    //val service = s"$uriCatalogManager/dataset-catalogs/$uri"
    //valL response = ingestionManager.connect(client)(service)

    val encodedUri = URLEncoder.encode(uri, "UTF-8")

    val response = {
      val controller: WSResponse => StorageContent = format match {
        case "json" => resp => StorageContent(Option(resp.body)) //resp.json
        case _ => resp => StorageContent(Option(resp.body))
      }


      catalogManager.uribyid(Authorization, encodedUri).flatMap( uri =>
        storageManager.getDataset(format, chunk_size, uri, Authorization, limit, controller))

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
