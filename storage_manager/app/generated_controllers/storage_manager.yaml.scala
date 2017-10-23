
import play.api.mvc.{Action,Controller}

import play.api.data.validation.Constraint

import play.api.i18n.MessagesApi

import play.api.inject.{ApplicationLifecycle,ConfigurationProvider}

import de.zalando.play.controllers._

import PlayBodyParsing._

import PlayValidations._

import scala.util._

import javax.inject._


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

        // ----- End of unmanaged code area for constructor Storage_managerYaml
        val getDataset = getDatasetAction { input: (String, String, Physical_datasetsGetChunk_size, Physical_datasetsGetChunk_size) =>
            val (uri, format, limit, chunk_size) = input
            // ----- Start of unmanaged code area for action  Storage_managerYaml.getDataset
            NotImplementedYet
            // ----- End of unmanaged code area for action  Storage_managerYaml.getDataset
        }
        val getDatasetSchema = getDatasetSchemaAction { input: (String, String) =>
            val (uri, format) = input
            // ----- Start of unmanaged code area for action  Storage_managerYaml.getDatasetSchema
            NotImplementedYet
            // ----- End of unmanaged code area for action  Storage_managerYaml.getDatasetSchema
        }
    
    }
}
