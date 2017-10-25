package it.gov.daf.storagemanager

object Dataset {

    // def getDataset(@ApiParam(value = "the dataset's physical URI", required = true) uri: String,
    //              @ApiParam(value = "the dataset's format", required = true) format: String,
    //              @ApiParam(value = "max number of rows/chunks to return", required = false) limit: Option[Int],
    //              @ApiParam(value = "chunk size", required = false) chunk_size: Option[Int]): Action[AnyContent] =
    // Action {
    //   Logger("getDataset").info(s"GetDataset in action: ${chunk_size}")
    //   CheckedAction(exceptionManager orElse hadoopExceptionManager) {
    //     HadoopDoAsAction {
    //       _ =>
    //       Logger("getDataset").info(s"GetDataset with limit: ${chunk_size}")
    //         val datasetURI = new URI(uri)
    //         val locationURI = new URI(datasetURI.getSchemeSpecificPart)
    //         val locationScheme = locationURI.getScheme
    //         val actualFormat = format match {
    //           case "avro" => "com.databricks.spark.avro"
    //           case "csv" => "com.databricks.spark.csv"
    //           case format: String => format
    //         }
    //         locationScheme match {
    //           case "hdfs" if actualFormat == "csv" =>
    //             val location = locationURI.getSchemeSpecificPart
    //             val df = sparkSession.read
    //               .option("header", "true")
    //               .csv(location)
    //             // val rdd = sparkSession.sparkContext.textFile(location)
    //             // val doc = rdd.take(limit.getOrElse(defaultLimit))
    //             val doc = s"[${
    //               df.take(limit.getOrElse(defaultLimit)).map(row => {
    //                 Utility.rowToJson(df.schema)(row)
    //               })//.mkString(",")
    //             }]"
    //             Ok(doc).as(JSON)
    //           case "hdfs" if actualFormat == "text" =>
    //             val location = locationURI.getSchemeSpecificPart
    //             val rdd = sparkSession.sparkContext.textFile(location)
    //             val doc = rdd.take(limit.getOrElse(defaultLimit)).mkString("\n")
    //             Ok(doc).as("text/plain")
    //           case "hdfs" if actualFormat == "raw" =>
    //             val location = locationURI.getSchemeSpecificPart
    //             val path = new Path(location)
    //             if (fileSystem.isDirectory(path))
    //               throw new InvalidPathException("The specified location is not a file")
    //             val data: FSDataInputStream = fileSystem.open(path)
    //             val dataContent: Source[ByteString, _] = StreamConverters.fromInputStream(() => data, chunk_size.getOrElse(defaultChunkSize))
    //             Ok.chunked(dataContent.take(limit.getOrElse(defaultLimit).asInstanceOf[Long])).as("application/octet-stream")
    //           case "hdfs" =>
    //             val location = locationURI.getSchemeSpecificPart
    //             val df = sparkSession.read.format(actualFormat).load(location)
    //             val doc = s"[${
    //               df.take(limit.getOrElse(defaultLimit)).map(row => {
    //                 Utility.rowToJson(df.schema)(row)
    //               }).mkString(",")
    //             }]"
    //             Ok(doc).as(JSON)
    //           case scheme =>
    //             throw new NotImplementedError(s"storage scheme: $scheme not supported")
    //         }
    //     }
    //   }
    // }

}
