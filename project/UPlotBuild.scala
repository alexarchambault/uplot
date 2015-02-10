import sbt._, Keys._

class UPlotBuild(base: File) extends Build {

  lazy val publishingSettings = xerial.sbt.Sonatype.sonatypeSettings ++ Seq[Setting[_]](
    publishMavenStyle := true,
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    pomExtra := {
      <url>https://github.com/alexarchambault/uplot</url>
        <licenses>
          <license>
            <name>LGPL-3.0</name>
            <url>http://opensource.org/licenses/LGPL-3.0</url>
          </license>
        </licenses>
        <scm>
          <connection>scm:git:github.com/alexarchambault/uplot.git</connection>
          <developerConnection>scm:git:git@github.com:alexarchambault/uplot.git</developerConnection>
          <url>github.com/alexarchambault/uplot.git</url>
        </scm>
        <developers>
          <developer>
            <id>alexarchambault</id>
            <name>Alexandre Archambault</name>
            <url>https://github.com/alexarchambault</url>
          </developer>
        </developers>
    }
  )

  lazy val root = Project(id = "uplot", base = base)
    .settings(
      organization := "com.github.alexarchambault",
      name := "uplot",
      version := "0.1.0-SNAPSHOT",
      scalaVersion := "2.11.5",
      crossScalaVersions := Seq("2.10.4", "2.11.5"),
      libraryDependencies ++= Seq(
        "com.github.nscala-time" %% "nscala-time" % "1.8.0"
      )
    )
    .settings(publishingSettings: _*)

  lazy val highcharts = Project(id = "highcharts", base = base / "highcharts")
    .settings(
      organization := "com.github.alexarchambault",
      name := "uplot-highcharts",
      version := "0.1.0-SNAPSHOT",
      scalaVersion := "2.11.5",
      crossScalaVersions := Seq("2.10.4", "2.11.5"),
      libraryDependencies ++= Seq(
        "io.argonaut" %% "argonaut" % "6.1-M5"
      ),
      libraryDependencies ++= {
        if (scalaVersion.value startsWith "2.10.")
          Seq()
        else
          Seq(
            "org.scala-lang.modules" %% "scala-xml" % "1.0.3"
          )
      }
    )
    .settings(publishingSettings: _*)
    .dependsOn(root)

}
