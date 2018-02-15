import sbt.Keys._

lazy val root = project.in(file("."))
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "0.9.0",
      "org.scalameta" %% "scalameta" % "2.1.2"),
    name := "sbt-source-docs",
    organization := "com.threatstack",
    scalaVersion := "2.12.4",
    sbtPlugin := true,
    scriptedLaunchOpts := scriptedLaunchOpts.value ++ Seq("-Dplugin.version=" + version.value))

lazy val docs = project.in(file("docs"))
  .enablePlugins(MicrositesPlugin)
  .settings(
    micrositeName := "sbt-source-docs",
    micrositeDescription := "A plugin to pull source code into docs.",
    micrositeAuthor := "Threat Stack, Inc",
    micrositeHighlightTheme := "atom-one-light",
    micrositeHomepage := "https://sbt-source-docs.github.io/threatstack/sbt-source-docs/",
    micrositeGithubOwner := "threatstack",
    micrositeGithubRepo := "sbt-source-docs",
    micrositeBaseUrl := "docs",
    ghpagesNoJekyll := false,
    micrositeGitterChannel :=  false,
    scalacOptions ~= { _.filterNot(Set("-Yno-predef", "-Xlint")) },
    git.remoteRepo := "git@github.com:threatstack/sbt-source-docs.git",
    includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.svg" | "*.js" | "*.swf" | "*.yml" | "*.md")
