#i/bin/bash

benchmark_home=/home/renrui/SparkStreaming-TopN
spark_home=/root/spark/spark-1.6.0-bin-hadoop2.6
jar_home=$benchmark_home/out/artifacts/bigdatabench_sparkstreaming_topn_jar
data_home=file:///$benchmark_home/data/input_topN

#########################################
#Usage: TopKMain <directory> <top>
#<directory>: The monitored directory
#<top>: The top number
#########################################
$spark_home/bin/spark-submit --class  bigdatabench.streaming.benchmark.TopKMain --master spark://172.18.11.4:7077  $jar_home/bigdatabench-sparkstreaming-topn.jar $data_home 10
