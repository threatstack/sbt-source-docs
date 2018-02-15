import com.threatstack.docs.SourceDocsPlugin

enablePlugins(SourceDocsPlugin)
lazy val root = project.in(file("."))
  .settings(version := "0.1", scalaVersion := "2.12.4")

lazy val check = TaskKey[Unit]("check")

check := {
  val expected = IO.readLines(file("expect.md"))
  val actual   = IO.readLines(crossTarget.value / "docs"/ "test.md")
  if (expected != actual) sys.error("Output doesn't match expected: \n" + actual.mkString("\n"))
}

sourceDocsSourceDirectory := (baseDirectory in Compile).value / "docs"
sourceDocsTargetDirectory := crossTarget.value / "docs"
