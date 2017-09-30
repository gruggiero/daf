package it.gov.daf.datasetmanager

import java.io.File
import java.net.URLEncoder

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import it.gov.daf.storagemanager.ActionAnyContent
import dataset_manager.yaml.StorageContent
import it.gov.daf.storagemanager.client.Storage_managerClient
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.Future
import scala.util.{Failure, Success}

import play.api.libs.ws._

import play.api.libs.json._

import javax.inject._

import play.api.libs.concurrent.Execution.Implicits._

object StorageCaller {

  import scala.concurrent.ExecutionContext.Implicits.global

  val uriCatalogManager = ConfigFactory.load().getString("WebServices.storageUrl")

  def getDataset(format: String, chunk_size: Option[Int], uri: String, Authorization: String, limit: Option[Int]) // : Future[Successfull]
  = {
    println(format)
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    val client: AhcWSClient = AhcWSClient()
    val storageManager = new Storage_managerClient(client)(uriCatalogManager)

    //val service = s"$uriCatalogManager/dataset-catalogs/$uri"
    //val response = ingestionManager.connect(client)(service)

    val logical_uri = URLEncoder.encode(uri, "UTF-8")

    client.url(s"$uriCatalogManager/storage-manager/v1/physical-datasets?uri=dataset%3Ahdfs%3A%2Fdaf%2Fordinary%2Fcomune_milano%2Fenergia%2Fconsumi%2Fa2a_curve.landing.csv&format=csv&limit=10").withHeaders((this._render_header_params("Authorization" -> Some(Authorization)): _*)).get().foreach({ resp =>
      println(resp.body)
      // if ((resp.status >= 200) && (resp.status <= 299)) Json.parse(resp.body).as[ActionAnyContent]
      // else throw new java.lang.RuntimeException("unexpected response status: " + resp.status + " " + resp.body.toString)
    })

    val response = storageManager.getDataset(format: String, chunk_size: Option[Int], uri: String, Authorization: String, limit: Option[Int])
    val res: Future[StorageContent] = response
      .map{
        case ActionAnyContent(value) =>
          println(s"value: $value")
          StorageContent(value)
        case ex => StorageContent(Some(s"ERROR $ex"))
      }

  // def getDataset(format: String, chunk_size: Option[Int], uri: String, Authorization: String, limit: Option[Int]) = {
  //   client.url(s"$uriCatalogManager/storage-manager/v1/physical-datasets?uri=dataset%3Ahdfs%3A%2Fdaf%2Fordinary%2Fcomune_milano%2Fenergia%2Fconsumi%2Fa2a_curve.landing.csv&format=csv&limit=10").withHeaders((this._render_header_params("Authorization" -> Some(Authorization)): _*)).get().map({ resp =>
  //     println(resp.body)
  //     if ((resp.status >= 200) && (resp.status <= 299)) Json.parse(resp.body).as[ActionAnyContent]
  //     else throw new java.lang.RuntimeException("unexpected response status: " + resp.status + " " + resp.body.toString)
  //   })
  // }

    res
  }

  // private def _render_url_params(pairs: (String, Option[Any])*) = {
  //   val parts = pairs.collect({
  //     case (k, Some(v)) => k + "=" + v
  //   })
  //   if (parts.nonEmpty) parts.mkString("?", "&", "")
  //   else ""
  // }

  private def _render_header_params(pairs: (String, Option[String])*) = {
    pairs.collect({
      case (k, Some(v)) => k -> v
    })
  }

}
