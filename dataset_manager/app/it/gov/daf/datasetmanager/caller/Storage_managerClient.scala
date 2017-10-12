package it.gov.daf.datasetmanager.caller

import it.gov.daf.storagemanager._

import it.gov.daf.storagemanager.json._

import play.api.libs.ws._

import play.api.libs.json._

import javax.inject._

import play.api.libs.concurrent.Execution.Implicits._

@SuppressWarnings(
  Array(
    "org.wartremover.warts.Throw",
    "org.wartremover.warts.ToString",
    "org.wartremover.warts.Null"
  )
)
class Storage_managerClient @Inject() (WS: WSClient) (baseUrl: String) {


  def getDataset[T](format: String, chunk_size: Option[Int], uri: String, Authorization: String, limit: Option[Int],
  converter: WSResponse => T ) = {
    WS.url(s"$baseUrl/storage-manager/v1/physical-datasets" + this._render_url_params("uri" -> Some(uri), "format" -> Some(format), "limit" -> limit.map(_.toString), "chunk_size" -> chunk_size.map(_.toString))).withHeaders((this._render_header_params("Authorization" -> Some(Authorization)): _*)).get().map({ resp =>
      if ((resp.status >= 200) && (resp.status <= 299)) converter(resp)
      else throw new java.lang.RuntimeException(s"unexpected response status: ${resp.status.toString}  ${resp.body.toString}")
    })
  }
  def getDatasetSchema(Authorization: String, uri: String, format: String) = {
    WS.url(s"$baseUrl/storage-manager/v1/physical-datasets/schema" + this._render_url_params("uri" -> Some(uri), "format" -> Some(format))).withHeaders((this._render_header_params("Authorization" -> Some(Authorization)): _*)).get().map({ resp =>
      if ((resp.status >= 200) && (resp.status <= 299)) Json.parse(resp.body).as[ActionAnyContent]
      else throw new java.lang.RuntimeException(s"unexpected response status: ${resp.status.toString}  ${resp.body.toString}")
    })
  }
  private def _render_url_params(pairs: (String, Option[String])*) = {
    val parts = pairs.collect{
      case (k, Some(v)) => s"$k=${v.toString}"
    }
    if (parts.nonEmpty) parts.mkString("?", "&", "")
    else ""
  }
  private def _render_header_params(pairs: (String, Option[String])*) = {
    pairs.collect({
      case (k, Some(v)) => k -> v.toString
    })
  }
}
