ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val endsoul    = "com.endsoul"
lazy val scala3Version = "3.7.0"

lazy val catsEffectVersion          = "3.6.1"

lazy val pureConfigVersion          = "0.17.9"

lazy val log4catsVersion            = "2.7.0"
lazy val slf4jVersion               = "2.0.17"

lazy val circeVersion               = "0.14.1"
lazy val http4sVersion              = "0.23.30"

lazy val scalaTestVersion           = "3.2.19"
lazy val scalaTestCatsEffectVersion = "1.6.0"

lazy val server = (project in file("."))
  .settings(
    name := "multitenant-ecommerce",
    scalaVersion := scala3Version,
    organization := endsoul,
    libraryDependencies ++= Seq(
      "org.typelevel"         %% "cats-effect"         % catsEffectVersion,
      "com.github.pureconfig" %% "pureconfig-core"     % pureConfigVersion,
      "org.typelevel"         %% "log4cats-slf4j"      % log4catsVersion,
      "io.circe"              %% "circe-generic"       % circeVersion,
      "io.circe"              %% "circe-fs2"           % circeVersion,
      "org.http4s"            %% "http4s-dsl"          % http4sVersion,
      "org.http4s"            %% "http4s-ember-server" % http4sVersion,
      "org.http4s"            %% "http4s-circe"        % http4sVersion,
    )
  )
