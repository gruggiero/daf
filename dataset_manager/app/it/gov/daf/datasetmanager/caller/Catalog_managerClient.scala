package it.gov.daf.datasetmanager.caller

import scala.concurrent.Future

import it.gov.daf.catalogmanager._

import it.gov.daf.catalogmanager.json._

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
class Catalog_managerClient @Inject() (WS: WSClient) (baseUrl: String) {

  def uribyid(Authorization: String, catalog_id: String):  Future[String] = Future{ catalog_id }

  def datasetcatalogbyid(Authorization: String, catalog_id: String):  Future[MetaCatalog] = ???

  // def datasetcatalogbyid(Authorization: String, catalog_id: String):  Future[MetaCatalog] = {
  // WS.url(s"$baseUrl/catalog-manager/v1/catalog-ds/get/$catalog_id").withHeaders((this._render_header_params("Authorization" -> Some(Authorization)): _*)).get().map({ resp =>
    //   if ((resp.status >= 200) && (resp.status <= 299)) Json.parse(resp.body).as[MetaCatalog]
    //   else throw new java.lang.RuntimeException("unexpected response status: " + resp.status + " " + resp.body.toString)
    // })
    // }

  // private def _render_url_params(pairs: (String, Option[Any])*) = {
  //   val parts = pairs.collect({
  //     case (k, Some(v)) => k + "=" + v
  //   })
  //   if (parts.nonEmpty) parts.mkString("?", "&", "")
  //   else ""
  // }
  // private def _render_header_params(pairs: (String, Option[Any])*) = {
  //   pairs.collect({
  //     case (k, Some(v)) => k -> v.toString
  //   })
  // }
}
