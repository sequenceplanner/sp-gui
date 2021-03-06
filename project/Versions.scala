import sbt._
import Keys._

/**
  * Created by alexa on 24/07/2018.
  * All Dependency-versions in one file.
  * If you need to update one dependency,
  * this is the place to be.
  *
  *
  * Declare global dependency versions here to avoid mismatches in multi part dependencies.
  *
  */
object Versions {
  /** VersionNumbers for Project-Dependencies */
  object ProjectVersion {
    lazy val scala = "2.12.3"
    lazy val log4js = "1.4.10"
  }

  /** VersionNumbers for Domain-Dependencies */
  object DomainVersion {
    lazy val playJson = "2.6.0"
    lazy val playJsonDerivedCodecs = "4.0.0"
    lazy val scalaJavaTime = "2.0.0-M12"
    lazy val scalaParser = "1.0.5"
  }

  /** VersionNumbers for Dependencies used in different */
  object MultiVersion {
    lazy val scalaTest = "3.0.1"
  }

  /** VersionNumbers for Comm-Dependencies */
  object CommVersion {
    lazy val akka = "2.5.3"
    lazy val slf4j = "1.7.7"
    lazy val akkaKryoSerialization = "0.5.1"
    lazy val avro4s = "1.8.0"
  }

  /** VersionNumbers for Gui-Dependencies */
  object GuiVersion {
    lazy val scalajsReact = "1.1.0"
    lazy val scalaCSS = "0.5.5"
    lazy val diode = "1.1.3"
    lazy val scalarx = "0.4.0"
    lazy val scalajsD3 = "0.3.4"
    lazy val uTest = "0.4.7"
    lazy val scalaDom = "0.9.3"
    lazy val monocleCore = "1.4.0"
    lazy val monocleMacro = "1.4.0"
    lazy val fs2Core = "0.10.5"
  }

  /** VersionNumbers for Npm-Dependencies added by scalajs-bundler*/
  object NpmVersion {
    lazy val bootstrap = "4.1.3"
    lazy val reactstrap = "6.3.0"
    lazy val react = "^16.2.0"
    lazy val reactDom = "^16.2.0"
    lazy val popperJS = "1.14.3"
    lazy val jQuery = "3.1.1"
    lazy val reactGridLayout = "0.16.6"

    // Dev
    lazy val webpackMerge = "4.1.0"
    lazy val styleLoader = "0.13.1"
    lazy val cssLoader = "0.26.1"
    lazy val jsonLoader = "0.5.4"
    lazy val fileLoader = "0.9.0"
  }

}
