scalaVersion := "2.12.8"

scalacOptions += "-Ypartial-unification"

libraryDependencies += "org.typelevel" %% "cats-core" % "1.6.1"
libraryDependencies += "org.typelevel" %% "cats-effect" % "1.4.0"

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")