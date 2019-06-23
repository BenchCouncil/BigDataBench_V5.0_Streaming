#!/bin/bash

benchmark_home=/home/renrui/SparkStreaming-KMeans
destinationDir=$benchmark_home/data/kmeans_train
#destinationDir2=/vagrant/sparkstreaming/data/kmeans_train
jar_home=$benchmark_home/data

rm -rf $destinationDir/*
#rm -rf $destinationDir2/*
txtNum=100
#numDimensions
columnNum=3

echo "columnNum:$columnNum"

for i in $(seq 1 $txtNum); do 
       echo $i
       rand1=`expr $RANDOM % 51` 
       echo "rand1:$rand1"
       rowNum=$(($rand1+1))
       echo "rowNum:$rowNum"
       #if [ $(($i%2)) == 0 ]; then
       java -jar $jar_home/KMeansTrainDataGenerator.jar $rowNum $columnNum > $destinationDir/$i.txt
       echo "Output $destinationDir/$i.txt"
       #else java -jar $jar_home/KMeansDataGenerator.jar $rowNum $columnNum > $destinationDir2/$i.txt
       #echo "Output $destinationDir2/$i.txt"
       #fi
       sleep 5
done 


