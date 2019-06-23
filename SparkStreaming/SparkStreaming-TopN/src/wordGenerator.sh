#!/bin/bash

sourcefile=/vagrant/sparkstreaming/data/word.txt
destinationDir=/vagrant/sparkstreaming/data/input_topN

rm -rf $destinationDir/*
txtnum=100

for i in $(seq 1 $txtnum); do 
       echo $i
       rand1=`expr $RANDOM % 101` 
       echo "rand1:$rand1"
       headline=$(($rand1+1))
       echo "headline num:$headline"
       rand2=`expr $RANDOM % 21`
       echo "rand2:$rand2" 
       lines=$(($rand2+1))
       echo "lins:$lines"
       lastline=$(($headline + $lines))
       echo "lastline num:$lastline"
       sed -n ''$headline','$lastline' p' $sourcefile > $destinationDir/$i.txt
       echo "Output $i.txt"
       sleep 5
done 


