name := "BigDataBench-SparkStreaming-KMeans"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "org.apache.spark" %% "spark-core" % "1.5.0" % "provided"

libraryDependencies += "org.apache.spark" %% "spark-streaming" % "1.5.0" % "provided"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.5.0" % "provided"