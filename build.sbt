name := """blocking_vs_non-blocking_ IO"""

version := "1.0"

scalaVersion := "2.11.7"

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

// Uncomment to use Akka
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.11"

// Akka Testkit
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.3.6" % "test"

//mainClass in (Compile, run) := Some("benchmark.blocking.Initializer")
mainClass in (Compile, run) := Some("benchmark.nonblocking.Initializer")
