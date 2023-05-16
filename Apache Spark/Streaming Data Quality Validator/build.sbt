ThisBuild / version := "1.1.0"

ThisBuild / scalaVersion := "2.12.14"

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

assemblyJarName := "spark-streaming-data-quality-validator.jar"

lazy val root = (project in file("."))
  .settings(
      name := "spark-streaming-data-quality-validator"
)

val sparkVersion = "3.2.1"
val deltaVersion = "1.2.1"
val akkaVersion = "2.6.19"
val log4jVersion = "2.17.2"

libraryDependencies ++= Seq(
  // Spark Core
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",

  // Spark Streaming
  "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",

  // Delta Tables
  "io.delta" %% "delta-core" % deltaVersion % "provided",

  // Akka
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,

  // Logging
  "org.apache.logging.log4j" % "log4j-api" % log4jVersion % "provided",
  "org.apache.logging.log4j" % "log4j-core" % log4jVersion % "provided"
)