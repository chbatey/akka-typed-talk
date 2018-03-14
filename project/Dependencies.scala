import sbt._

object Dependencies {

  val akkaVersion = "2.5.11"
  val akkaHttpVersion = "10.1.0"
  val log4jVersion = "2.9.1"

  val akkaStreams = "com.typesafe.akka" %% "akka-stream" % akkaVersion
  val akkaTyped = "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
  val akkaClusterTyped = "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion
  val akkaPersistenceTyped = "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion

  val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
  val akkaHttpSpray = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
  val akkaStreamsContrib =  "com.typesafe.akka" %% "akka-stream-contrib" % "0.8"
  val log4j2Api = "org.apache.logging.log4j" % "log4j-api" % log4jVersion
  val log4j2Core = "org.apache.logging.log4j" % "log4j-core" % log4jVersion % Runtime
  val log4jSlf4J = "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jVersion
  val typesafeLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"

  val akkaStreamsTestKit = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion
  val akkaHttpTestKit = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test

  val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % Test

  val exampleDeps: Seq[ModuleID] = Seq(
    akkaTyped, akkaClusterTyped, akkaStreams, akkaStreamsTestKit, akkaStreamsContrib, scalaTest
  )
}

