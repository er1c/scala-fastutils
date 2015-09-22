lazy val settings = Seq(
  organization := "szymonm",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  homepage := Some(url("http://github.com/szymonm/scala-fastutils")),

  scalaVersion := "2.11.7",
  crossScalaVersions := Seq("2.10.5", "2.11.7"),

  resolvers += Resolver.sonatypeRepo("releases"),
  libraryDependencies ++= Seq(
    "it.unimi.dsi" % "fastutil" % "7.0.7" % "provided",
    "org.scalatest" %% "scalatest" % "2.2.1" % "test",
    "org.scalacheck" %% "scalacheck" % "1.12.1" % "test"
  ),

  scalacOptions ++= Seq(
    "-Xlog-free-terms",
    "-feature",
    "-Yinline-warnings",
    "-deprecation",
    "-optimize",
    "-unchecked"
  ))


lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

lazy val core = project
  .in(file("."))
  .settings(moduleName := "scala-fastutils")
  .settings(settings)

lazy val benchmark = project.dependsOn(core)
  .in(file("benchmark"))
  .settings(moduleName := "scala-fu-benchmark")
  .settings(settings)
  .settings(Seq(
    javaOptions in run += "-Xmx3G",
    fork in run := true),
    libraryDependencies += "it.unimi.dsi" % "fastutil" % "7.0.7")
  .settings(noPublishSettings)
