package it.gov.daf.storagemanager

import java.lang.reflect.UndeclaredThrowableException
import java.net.URI
import java.security.PrivilegedExceptionAction

import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import com.databricks.spark.avro.SchemaConverters
import com.google.inject.Inject
import io.swagger.annotations.{Api, ApiOperation, ApiParam, Authorization}
import it.gov.daf.common.authentication.Authentication
import storage_manager.yaml.RawDataset
import org.apache.avro.SchemaBuilder
import org.apache.hadoop.fs._
import org.apache.hadoop.security.{AccessControlException, UserGroupInformation}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{AnalysisException, SparkSession}
import org.pac4j.play.store.PlaySessionStore
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{ AnyContent, Request }
// import play.api.mvc._
import play.mvc.Http
import play.api.Logger

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}
import scala.concurrent.Future


object Dataset {

  private def HadoopDoAsAction[T](action: Request[AnyContent] => T)(
    implicit proxyUser: UserGroupInformation,
    request: Request[AnyContent]  ) = (request: Request[AnyContent]) => {
    val profiles = Authentication.getProfiles(request)
    val user = profiles.headOption.map(_.getId).getOrElse("anonymous")
    val ugi = UserGroupInformation.createProxyUser(user, proxyUser)
    ugi.doAs(new PrivilegedExceptionAction[T]() {
      override def run: T = action(request)
    })
  }

  def getPhysicalDatasetJson(uri: String, format: String, limit: Int, chunkSize: Int)
    (implicit proxyUser: UserGroupInformation,
      sparkSession: SparkSession,
      request: Request[AnyContent]
    ) : List[RawDataset]
  = {

      HadoopDoAsAction {
        _ =>
        Logger("getDataset").info(s"GetDataset with limit: ${chunkSize}")
        val datasetURI = new URI(uri)
        val locationURI = new URI(datasetURI.getSchemeSpecificPart)
        val locationScheme = locationURI.getScheme
        val actualFormat = format match {
          case "avro" => "com.databricks.spark.avro"
          case "csv" => "com.databricks.spark.csv"
          case format: String => format
        }
        locationScheme match {
          case "hdfs" if actualFormat == "csv" =>
            val location = locationURI.getSchemeSpecificPart
            val df = sparkSession.read
              .option("header", "true")
              .csv(location)
            // val rdd = sparkSession.sparkContext.textFile(location)
            // val doc = rdd.take(limit.getOrElse(defaultLimit))
            // val doc = s"[${
            //   df.take(limit).map(row => {
            //     Utility.rowToJson(df.schema)(row)
            //   })//.mkString(",")
            // }]"
            df.take(limit)
          // case "hdfs" if actualFormat == "text" =>
          //   val location = locationURI.getSchemeSpecificPart
          //   val rdd = sparkSession.sparkContext.textFile(location)
          //   val doc = rdd.take(limit.getOrElse(defaultLimit)).mkString("\n")
          //   Ok(doc).as("text/plain")
          // case "hdfs" if actualFormat == "raw" =>
          //   val location = locationURI.getSchemeSpecificPart
          //   val path = new Path(location)
          //   if (fileSystem.isDirectory(path))
          //     throw new InvalidPathException("The specified location is not a file")
          //   val data: FSDataInputStream = fileSystem.open(path)
          //   val dataContent: Source[ByteString, _] = StreamConverters.fromInputStream(() => data, chunkSize)
          //   Ok.chunked(dataContent.take(limit.getOrElse(defaultLimit).asInstanceOf[Long])).as("application/octet-stream")
          // case "hdfs" =>
          //   val location = locationURI.getSchemeSpecificPart
          //   val df = sparkSession.read.format(actualFormat).load(location)
          //   val doc = s"[${
          //     df.take(limit.getOrElse(defaultLimit)).map(row => {
          //       Utility.rowToJson(df.schema)(row)
          //     }).mkString(",")
          //   }]"
          //   Ok(doc).as(JSON)
          case scheme =>
            throw new NotImplementedError(s"storage scheme: $scheme not supported")
        }
      }
    // }
    // ???
  }
}
