#!/bin/bash

#Please set the environment parameters
benchmark_home=/home/renrui/SparkStreaming-Grep
spark_home=/root/spark/spark-1.6.0-bin-hadoop2.6
jar_home=$benchmark_home/out/artifacts/bigdatabench_sparkstreaming_grep_jar
data_home=/vagrant/sparkstreaming/data

#################################################################################
#Usage: Grep <numStreams> <host> <port> <batchMillis>
# *   <numStream> is the number rawNetworkStreams, which should be same as number
# *               of work nodes in the cluster
# *   <host> is "localhost".
# *   <port> is the port on which RawTextSender is running in the worker nodes.
# *   <batchMillise> is the Spark Streaming batch duration in milliseconds.
#################################################################################

$spark_home/bin/spark-submit --class  bigdatabench.streaming.benchmark.Grep --master spark://172.18.11.4:7077  $jar_home/bigdatabench-sparkstreaming-grep.jar 1 localhost 9999 1000 2>&1 | grep -i "Total delay\|record"
