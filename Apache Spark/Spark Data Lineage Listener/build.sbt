ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := "2.12.14"

lazy val root = (project in file("."))
  .settings(
    name := "spark-data-lineage-listener"
  )

assembly / assemblyMergeStrategy := {
  case PathList("reference.conf") => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

assemblyShadeRules ++= Seq(
  ShadeRule.rename("com.typesafe.config.**" -> "my_conf.@0").inAll
)

assemblyJarName := "spark-data-lineage-listener.jar"

lazy val akkaVersion = "2.8.0"
lazy val akkaHttpVersion = "10.5.0"
lazy val sparkVersion = "3.2.1"

libraryDependencies ++= Seq(
  // Apache Spark
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",

  // Delta Io
  "io.delta" %% "delta-core" % "2.2.0",

  // Akka Actor
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,

  // Akka Streams
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,

  // Akka http
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,

  // Spray Json
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,

  // Dbutils
  "com.databricks" %% "dbutils-api" % "0.0.6" % "provided"
)
