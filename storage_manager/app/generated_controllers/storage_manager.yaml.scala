
import play.api.mvc.{Action,Controller}

import play.api.data.validation.Constraint

import play.api.i18n.MessagesApi

import play.api.inject.{ApplicationLifecycle,ConfigurationProvider}

import de.zalando.play.controllers._

import PlayBodyParsing._

import PlayValidations._

import scala.util._

import javax.inject._

import org.apache.hadoop.security.{AccessControlException,UserGroupInformation}
import it.gov.daf.storagemanager.Dataset._
import org.apache.spark.SparkConf
import org.apache.spark.sql.{AnalysisException,SparkSession}

import play.api.mvc.{ AnyContent, Request }

/**
 * This controller is re-generated after each change in the specification.
 * Please only place your hand-written code between appropriate comments in the body of the controller.
 */

package storage_manager.yaml {
    // ----- Start of unmanaged code area for package Storage_managerYaml

    // ----- End of unmanaged code area for package Storage_managerYaml
    class Storage_managerYaml @Inject() (
        // ----- Start of unmanaged code area for injections Storage_managerYaml

        // ----- End of unmanaged code area for injections Storage_managerYaml
        val messagesApi: MessagesApi,
        lifecycle: ApplicationLifecycle,
        config: ConfigurationProvider
    ) extends Storage_managerYamlBase {
        // ----- Start of unmanaged code area for constructor Storage_managerYaml
      private val defaultLimit = config.get.getInt("max_number_of_rows").getOrElse(throw new Exception("it shouldn't happen"))

      private val defaultChunkSize = config.get.getInt("chunk_size").getOrElse(throw new Exception("it shouldn't happen"))

      UserGroupInformation.loginUserFromSubject(null)

      implicit val proxyUser = UserGroupInformation.getCurrentUser

      private val sparkConfig = new SparkConf()
      sparkConfig.set("spark.driver.memory", config.get.getString("spark_driver_memory").getOrElse("128M"))

      implicit val sparkSession = SparkSession.builder().master("local").config(sparkConfig).getOrCreate()
        // ----- End of unmanaged code area for constructor Storage_managerYaml
        val getDataset = getDatasetAction { input: (String, String, Physical_datasetsGetChunk_size, Physical_datasetsGetChunk_size) =>
            val (uri, format, limit, chunk_size) = input
            // ----- Start of unmanaged code area for action  Storage_managerYaml.getDataset
            // Logger("getDataset").info(s"GetDataset in action: ${chunk_size}")
          // val auth = currentRequest.headers.get("authorization")
          // getLogicalDataset(uri, format, limit, chunk_size)
          // GetDataset200(res)
            NotImplementedYet
            // ----- End of unmanaged code area for action  Storage_managerYaml.getDataset
        }
        val getDatasetJson = getDatasetJsonAction { input: (String, String, Physical_datasetsGetChunk_size, Physical_datasetsGetChunk_size) =>
            val (uri, format, limit, chunk_size) = input
            // ----- Start of unmanaged code area for action  Storage_managerYaml.getDatasetJson
          val auth = currentRequest.headers.get("authorization")
          implicit val req: Request[Any] = currentRequest
          GetDatasetJson200( getPhysicalDatasetJson(uri, format, limit.getOrElse(defaultLimit), chunk_size.getOrElse(defaultChunkSize)))
            // NotImplementedYet
            // ----- End of unmanaged code area for action  Storage_managerYaml.getDatasetJson
        }
        val getDatasetSchema = getDatasetSchemaAction { input: (String, String) =>
            val (uri, format) = input
            // ----- Start of unmanaged code area for action  Storage_managerYaml.getDatasetSchema
            NotImplementedYet
            // ----- End of unmanaged code area for action  Storage_managerYaml.getDatasetSchema
        }

    }
}
