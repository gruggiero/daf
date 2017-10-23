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

import CommonBuild._
import Versions._
import com.typesafe.sbt.packager.docker.Cmd
import sbt.Keys.resolvers

organization in ThisBuild := "it.gov.daf"
name := "daf-storage-manager"

version := "1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.8"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
  "-Xfuture"
)

wartremoverErrors ++= Warts.allBut(Wart.Equals)
wartremoverExcluded ++= getRecursiveListOfFiles(baseDirectory.value / "target" / "scala-2.11" / "routes").toSeq
wartremoverExcluded ++= getRecursiveListOfFiles(baseDirectory.value / "app" / "generated_controllers").toSeq
wartremoverExcluded ++= routes.in(Compile).value

// lazy val client = (project in file("client")).
//   settings(Seq(
//     name := "daf-storage-manager-client",
//     swaggerGenerateClient := true,
//     swaggerClientCodeGenClass := new it.gov.daf.swaggergenerators.DafClientGenerator,
//     swaggerCodeGenPackage := "it.gov.daf.storagemanager",
//     swaggerModelFilesSplitting := "oneFilePerModel",
//     swaggerSourcesDir := file(s"${baseDirectory.value}/../conf"),
//     libraryDependencies ++= Seq(
//       "com.typesafe.play" %% "play-json" % playVersion,
//       "com.typesafe.play" %% "play-ws" %  playVersion
//     )
//   )).
//   enablePlugins(SwaggerCodegenPlugin)

lazy val root = (project in file(".")).
  enablePlugins(PlayScala, ApiFirstCore, ApiFirstPlayScalaCodeGenerator, ApiFirstSwaggerParser, /*AutomateHeaderPlugin,*/ DockerPlugin)
  // enablePlugins(PlayScala, AutomateHeaderPlugin, DockerPlugin)
  // .dependsOn(client).aggregate(client)

val hadoopExcludes =
  (moduleId: ModuleID) => moduleId.
    exclude("org.slf4j", "slf4j-log4j12").
    exclude("org.slf4j", "slf4j-api")

val sparkExcludes =
  (moduleId: ModuleID) => moduleId.
    exclude("org.slf4j", "slf4j-log4j12").
    exclude("org.slf4j", "slf4j-api").
    exclude("org.slf4j", "jcl-over-sl4j").
    exclude("org.slf4j", "jul-to-sl4j")

val sparkLibraries = Seq(
  sparkExcludes("org.apache.spark" %% "spark-core" % sparkVersion % Compile),
  sparkExcludes("org.apache.spark" %% "spark-sql" % sparkVersion % Compile),
  "com.databricks" %% "spark-avro" % "3.2.0" % Compile
)

val hadoopLibraries = Seq(
  hadoopExcludes("org.apache.hadoop" % "hadoop-client" % hadoopVersion % Compile),
  hadoopExcludes("org.apache.hadoop" % "hadoop-client" % hadoopVersion % Test classifier "tests"),
  hadoopExcludes("org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion % Test classifier "tests"),
  hadoopExcludes("org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion % Test classifier "tests" extra "type" -> "test-jar"),
  hadoopExcludes("org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion % Test extra "type" -> "test-jar"),
  hadoopExcludes("org.apache.hadoop" % "hadoop-client" % hadoopVersion % Test classifier "tests"),
  hadoopExcludes("org.apache.hadoop" % "hadoop-minicluster" % hadoopVersion % Test),
  hadoopExcludes("org.apache.hadoop" % "hadoop-common" % hadoopVersion % Test classifier "tests" extra "type" -> "test-jar"),
  hadoopExcludes("org.apache.hadoop" % "hadoop-mapreduce-client-jobclient" % hadoopVersion % Test classifier "tests"),
  "com.github.pathikrit" %% "better-files" % betterFilesVersion % Test
)

libraryDependencies ++= Seq(
  cache,
  ws,
  "org.webjars" % "swagger-ui" % swaggerUiVersion,
  specs2 % Test,
  "io.swagger" %% "swagger-play2" % "1.5.3",
  "com.typesafe.play" %% "play-json" % playVersion,
  "it.gov.daf" %% "common" % version.value
) ++ hadoopLibraries ++ sparkLibraries

resolvers ++= Seq(
  "zalando-bintray" at "https://dl.bintray.com/zalando/maven",
  Resolver.url("sbt-plugins", url("http://dl.bintray.com/gruggiero/sbt-plugins"))(Resolver.ivyStylePatterns),
  Resolver.sonatypeRepo("releases"),
  "cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos/",
  "daf repo" at "http://nexus.default.svc.cluster.local:8081/repository/maven-public/"
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

apiFirstParsers := Seq(ApiFirstSwaggerParser.swaggerSpec2Ast.value).flatten

playScalaAutogenerateTests := false

playScalaCustomTemplateLocation := Some(baseDirectory.value / "templates")

licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))
headerLicense := Some(HeaderLicense.ALv2("2017", "TEAM PER LA TRASFORMAZIONE DIGITALE"))
headerMappings := headerMappings.value + (HeaderFileType.conf -> HeaderCommentStyle.HashLineComment)

dockerBaseImage := "anapsix/alpine-java:8_jdk_unlimited"
dockerCommands := dockerCommands.value.flatMap {
  case cmd@Cmd("FROM", _) => List(cmd,
    Cmd("RUN", "apk update && apk add bash krb5-libs krb5"),
    Cmd("RUN", "ln -sf /etc/krb5.conf /opt/jdk/jre/lib/security/krb5.conf")
  )
  case other => List(other)
}
dockerEntrypoint := Seq(s"bin/${name.value}", "-Dconfig.file=conf/production.conf")
dockerCmd := Seq("-jvm-debug", "5005")
dockerExposedPorts := Seq(9000)
dockerRepository := Option("10.98.74.120:5000")

// PlayKeys.devSettings := Seq("config.file" -> "/Users/gruggie/git/gruggiero/daf/storage_manager/conf/production.conf")

publishTo in ThisBuild := {
  val nexus = "http://nexus.default.svc.cluster.local:8081/repository/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "maven-snapshots/")
  else
    Some("releases"  at nexus + "maven-releases/")
}

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")
