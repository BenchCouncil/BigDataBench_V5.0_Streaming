#!/bin/bash
JStorm_home=/home/jstorm-0.9.6.3
jar_home=./out/artifacts/BigDataBench_JStorm_CfByUser_jar
properties_home=./resource


$JStorm_home/bin/jstorm jar $jar_home/BigDataBench-JStorm-CfByUser.jar framework.CfByUser CfByUser $properties_home/CfByUser.properties
