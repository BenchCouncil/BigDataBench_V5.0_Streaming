#i/bin/bash

benchmark_home=/home/renrui/SparkStreaming-KMeans
spark_home=/root/spark/spark-1.6.0-bin-hadoop2.6
jar_home=$benchmark_home/out/artifacts/bigdatabench_sparkstreaming_kmeans_jar
trainingDir=file:///$benchmark_home/data/kmeans_train
testDir=file:///$benchmark_home/data/kmeans_test

#############################################################################################
#StreamingKMeans <trainingDir> <testDir> <batchDuration> <numClusters> <numDimensions>
#<trainingDir>: The directory used to save the training file
#<testDir>: The directory used to save the test file
#<batchDuration>: Batch's time interval
#<numClusters>: The number of clusters
#<numDimensions>: The dimension of vector (which is number of columns), it needs to be same as " columnNum" in 
#                 KMeansTestDataGenerator.sh and KMeansTrainDataGenerator.sh
#############################################################################################
$spark_home/bin/spark-submit --class  org.apache.sparkstreaming.KMeans --master spark://172.18.11.4:7077  $jar_home/bigdatabench-sparkstreaming-kmeans.jar $trainingDir $testDir 10 3 3                      
