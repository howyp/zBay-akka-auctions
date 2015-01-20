name := "zBay-akka-auctions"

version := "1.0"

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"   % "2.3.9",
  "joda-time"         %  "joda-time"    % "2.2",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.9"  % "test",
  "org.specs2"        %% "specs2-core"  % "2.4.15" % "test"
)

scalacOptions in Test ++= Seq("-Yrangepos")

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)