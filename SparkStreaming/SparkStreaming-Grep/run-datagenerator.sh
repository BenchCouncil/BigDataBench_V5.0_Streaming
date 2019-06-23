#!/bin/bash

#Please set the environment parameters

benchmark_home=/home/renrui/SparkStreaming-Grep
spark_home=/root/spark/spark-1.6.0-bin-hadoop2.6
jar_home=$benchmark_home/out/artifacts/bigdatabench_sparkstreaming_datagenerator_jar
data_home=$benchmark_home/data

################################################
#Usage: RawTextSender <port> <file> <bytesPerSec>
#for example: <port>  9999
#             <file>  100-bytes-lines.txt
#             <bytesPerSec>   10000000
#These parameters can be modifyed as required
################################################

$spark_home/bin/spark-submit --class  bigdatabench.streaming.input.DataGenerator  --master spark://172.18.11.4:7077 $jar_home/bigdatabench-sparkstreaming-datagenerator.jar 9999 $data_home/100-bytes-lines.txt 10000000
