#!/bin/bash
JStorm_home=/home/jstorm-0.9.6.3
jar_home=./out/artifacts/BigDataBench_JStorm_Search_jar
properties_home=./resource


$JStorm_home/bin/jstorm jar $jar_home/BigDataBench-JStorm-Search.jar framework.Search Search $properties_home/Search.properties
