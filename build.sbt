scalaVersion in ThisBuild := "2.11.8"

resolvers += Resolver.typesafeRepo("releases")
resolvers += Resolver.bintrayRepo("mingchuno", "maven")

libraryDependencies += "org.typelevel"      %% "cats"                     % "0.7.0"

libraryDependencies += "io.circe"           %% "circe-core"               % "0.5.0-M3"
libraryDependencies += "io.circe"           %% "circe-parser"             % "0.5.0-M3"
libraryDependencies += "io.circe"           %% "circe-generic"             % "0.5.0-M3"

libraryDependencies += "com.typesafe.akka"  %% "akka-actor"               % "2.4.9"
libraryDependencies += "com.typesafe.akka"  %% "akka-stream"              % "2.4.9"
libraryDependencies += "com.typesafe.akka"  %% "akka-http-core"           % "2.4.9"
libraryDependencies += "com.typesafe.akka"  %% "akka-http-experimental"   % "2.4.9"

libraryDependencies += "com.amazonaws"      % "aws-java-sdk-sqs"          % "1.11.29"
libraryDependencies += "com.amazonaws"      % "aws-java-sdk-sns"          % "1.11.29"
libraryDependencies += "com.amazonaws"      % "aws-java-sdk-dynamodb"     % "1.11.29"

libraryDependencies += "com.github.dwhjames" %% "aws-wrap" % "0.9.0"
