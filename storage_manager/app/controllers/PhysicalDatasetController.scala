/*
 * Copyright 2017 TEAM PER LA TRASFORMAZIONE DIGITALE
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

package controllers

import java.lang.reflect.UndeclaredThrowableException
import java.net.URI
import java.security.PrivilegedExceptionAction

import akka.stream.scaladsl.{Source, StreamConverters}
import akka.util.ByteString
import com.databricks.spark.avro.SchemaConverters
import com.google.inject.Inject
import io.swagger.annotations.{Api, ApiOperation, ApiParam, Authorization}
import it.gov.daf.common.authentication.Authentication
import org.apache.avro.SchemaBuilder
import org.apache.hadoop.fs._
import org.apache.hadoop.security.{AccessControlException, UserGroupInformation}
import org.apache.spark.SparkConf
import org.apache.spark.sql.{AnalysisException, SparkSession}
import org.pac4j.play.store.PlaySessionStore
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc._
import play.mvc.Http
import play.api.Logger

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

@SuppressWarnings(
  Array(
    "org.wartremover.warts.Throw",
    "org.wartremover.warts.NonUnitStatements",
    "org.wartremover.warts.Nothing",
    "org.wartremover.warts.IsInstanceOf",
    "org.wartremover.warts.Null",
    "org.wartremover.warts.Var",
    "org.wartremover.warts.AsInstanceOf"
  )
)
@Api("physical-dataset")
class PhysicalDatasetController @Inject()(configuration: Configuration, val playSessionStore: PlaySessionStore) extends Controller {

  private val defaultLimit = configuration.getInt("max_number_of_rows").getOrElse(throw new Exception("it shouldn't happen"))

  private val defaultChunkSize = configuration.getInt("chunk_size").getOrElse(throw new Exception("it shouldn't happen"))

  private val sparkConfig = new SparkConf()
  sparkConfig.set("spark.driver.memory", configuration.getString("spark_driver_memory").getOrElse("128M"))

  private val sparkSession = SparkSession.builder().master("local").config(sparkConfig).getOrCreate()

  private val fileSystem: FileSystem = {
    val conf = new org.apache.hadoop.conf.Configuration()
    FileSystem.get(conf)
  }

  UserGroupInformation.loginUserFromSubject(null)

  private val proxyUser = UserGroupInformation.getCurrentUser

  Authentication(configuration, playSessionStore)

  private val exceptionManager: PartialFunction[Throwable, Result] = (exception: Throwable) => exception match {
    case ex: AnalysisException =>
      Ok(Json.toJson(ex.getMessage)).copy(header = ResponseHeader(Http.Status.NOT_FOUND, Map.empty))
    case ex: NotImplementedError =>
      Ok(Json.toJson(ex.getMessage)).copy(header = ResponseHeader(Http.Status.NOT_IMPLEMENTED, Map.empty))
    case ex: UndeclaredThrowableException if ex.getUndeclaredThrowable.isInstanceOf[AnalysisException] =>
      Ok(Json.toJson(ex.getMessage)).copy(header = ResponseHeader(Http.Status.NOT_FOUND, Map.empty))
    case ex: InvalidPathException =>
      Ok(Json.toJson(ex.getMessage)).copy(header = ResponseHeader(Http.Status.BAD_REQUEST, Map.empty))
    case ex: Throwable =>
      Ok(Json.toJson(ex.getMessage)).copy(header = ResponseHeader(Http.Status.INTERNAL_SERVER_ERROR, Map.empty))
  }

  private val hadoopExceptionManager: PartialFunction[Throwable, Result] = (exception: Throwable) => exception match {
    case ex: AccessControlException =>
      Ok(Json.toJson(ex.getMessage)).copy(header = ResponseHeader(Http.Status.UNAUTHORIZED, Map.empty))
  }

  private def CheckedAction(exceptionManager: Throwable => Result)(action: Request[AnyContent] => Result) = (request: Request[AnyContent]) => {
    Try(action(request)) match {
      case Success(response) => response
      case Failure(exception) => exceptionManager(exception)
    }
  }

  private def HadoopDoAsAction(action: Request[AnyContent] => Result) = (request: Request[AnyContent]) => {
    val profiles = Authentication.getProfiles(request)
    val user = profiles.headOption.map(_.getId).getOrElse("anonymous")
    val ugi = UserGroupInformation.createProxyUser(user, proxyUser)
    ugi.doAs(new PrivilegedExceptionAction[Result]() {
      override def run: Result = action(request)
    })
  }

  @ApiOperation(
    value = "given a physical dataset URI it returns a json document with the first 'limit' number of rows",
    produces = "application/json, text/plain, application/octet-stream",
    authorizations = Array(new Authorization(value = "basicAuth"))
  )
  def getDataset(@ApiParam(value = "the dataset's physical URI", required = true) uri: String,
                 @ApiParam(value = "the dataset's format", required = true) format: String,
                 @ApiParam(value = "max number of rows/chunks to return", required = false) limit: Option[Int],
                 @ApiParam(value = "chunk size", required = false) chunk_size: Option[Int]): Action[AnyContent] =
    Action {
      CheckedAction(exceptionManager orElse hadoopExceptionManager) {
        HadoopDoAsAction {
          _ =>
          Logger("getDataset").info(s"GetDataset with limit: ${chunk_size}")
            val datasetURI = new URI(uri)
            val locationURI = new URI(datasetURI.getSchemeSpecificPart)
            val locationScheme = locationURI.getScheme
            val actualFormat = format match {
              case "avro" => "com.databricks.spark.avro"
              case format: String => format
            }
            locationScheme match {
              case "hdfs" if actualFormat == "text" =>
                val location = locationURI.getSchemeSpecificPart
                val rdd = sparkSession.sparkContext.textFile(location)
                val doc = rdd.take(limit.getOrElse(defaultLimit)).mkString("\n")
                Ok(doc).as("text/plain")
              case "hdfs" if actualFormat == "raw" =>
                val location = locationURI.getSchemeSpecificPart
                val path = new Path(location)
                if (fileSystem.isDirectory(path))
                  throw new InvalidPathException("The specified location is not a file")
                val data: FSDataInputStream = fileSystem.open(path)
                val dataContent: Source[ByteString, _] = StreamConverters.fromInputStream(() => data, chunk_size.getOrElse(defaultChunkSize))
                Ok.chunked(dataContent.take(limit.getOrElse(defaultLimit).asInstanceOf[Long])).as("application/octet-stream")
              case "hdfs" =>
                val location = locationURI.getSchemeSpecificPart
                val df = sparkSession.read.format(actualFormat).load(location)
                val doc = s"[${
                  df.take(limit.getOrElse(defaultLimit)).map(row => {
                    Utility.rowToJson(df.schema)(row)
                  }).mkString(",")
                }]"
                Ok(doc).as(JSON)
              case scheme =>
                throw new NotImplementedError(s"storage scheme: $scheme not supported")
            }
        }
      }
    }

  @ApiOperation(
    value = "given a physical dataset URI it returns its AVRO schema in json format",
    produces = "application/json",
    authorizations = Array(new Authorization(value = "basicAuth"))
  )
  def getDatasetSchema(@ApiParam(value = "the dataset's physical URI", required = true) uri: String,
                       @ApiParam(value = "the dataset's format", required = true) format: String): Action[AnyContent] =
    Action {
      CheckedAction(exceptionManager) {
        HadoopDoAsAction {
          _ =>
            val datasetURI = new URI(uri)
            val locationURI = new URI(datasetURI.getSchemeSpecificPart)
            val locationScheme = locationURI.getScheme
            val actualFormat = format match {
              case "avro" => "com.databricks.spark.avro"
              case format: String => format
            }
            locationScheme match {
              case "hdfs" if actualFormat == "raw" =>
                Ok("No Scheme Available for raw format").as("text/plain")
              case "hdfs" if actualFormat == "text" =>
                Ok("No Scheme Available").as("text/plain")
              case "hdfs" =>
                val location = locationURI.getSchemeSpecificPart
                val df = sparkSession.read.format(actualFormat).load(location)
                val schema = SchemaConverters.convertStructToAvro(df.schema, SchemaBuilder.record("topLevelRecord"), "")
                Ok(schema.toString(true)).as(JSON)
              case scheme =>
                throw new NotImplementedError(s"storage scheme: $scheme not supported")
            }
        }
      }
    }

}
