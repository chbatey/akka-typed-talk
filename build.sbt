import Dependencies._

lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(
      organization := "info.batey.akka",
      scalaVersion := "2.12.4",
      version := "0.1.0-SNAPSHOT"
    )))
  .settings(
    name := "akka-typed-talk"
  )
  .aggregate(examples, presentation)

lazy val examples = (project in file("examples"))
  .settings(
    libraryDependencies ++= exampleDeps
  )

lazy val presentation = (project in file("presentation"))
  .dependsOn(examples)
  .settings(
    tutSourceDirectory := baseDirectory.value / "tut",
    tutTargetDirectory := baseDirectory.value / "../docs",
    watchSources ++= (tutSourceDirectory.value ** "*.html").get
  )
  .enablePlugins(TutPlugin)


