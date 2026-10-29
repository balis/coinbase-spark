ThisBuild / organization := "pl.edu.agh"
ThisBuild / scalaVersion := "2.13.18"
ThisBuild / version := "0.1.0-SNAPSHOT"

val sparkVersion = "4.2.1"
val circeVersion = "0.14.17"

lazy val root = (project in file("."))
  .settings(
    name := "coinbase-spark",
    scalacOptions += "-Ymacro-annotations",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "org.scalatest" %% "scalatest" % "3.2.20" % "test",
      "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
      "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",
      "com.squareup.okhttp3" % "okhttp-jvm" % "5.5.1",
    )
  )

assembly / assemblyMergeStrategy := {
  case "META-INF/MANIFEST.MF" => MergeStrategy.discard
  case x                      => MergeStrategy.first
}
