name := "attempt"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= {
  val akkaV = "2.5.11"
  val akkaHttpV = "10.0.12"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-http" % akkaHttpV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV
  )
}