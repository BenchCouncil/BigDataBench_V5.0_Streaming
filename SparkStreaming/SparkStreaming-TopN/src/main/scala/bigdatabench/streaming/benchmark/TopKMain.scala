package bigdatabench.streaming.benchmark

//Created by rain on 2015/12/23.

import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.SparkContext._


object TopKMain {
  def main(args: Array[String]) {

    if (args.length < 2) {
      System.err.println("Usage: TopKMain <directory> <top>")
      System.exit(1)
    }

    val input = args(0) //输入文件的路径（目录）
    val top = args(1)   //

    val conf = new SparkConf().setAppName("TopNWords")
    //val sc = new SparkContext(conf)   //这行需要注释掉，默认情况下Spark Shell只允许一个SparkContext实例，spark shell默认已经创建了一个sc了，直接创建ssc就行
    val ssc = new StreamingContext(conf, Seconds(10)) //Seconds(10):指定Spark Streaming运行时的batch窗口大小
    //ssc.checkpoint("checkpoint")

    val lines = ssc.textFileStream(input)
    val words = lines.flatMap(_.split(" "))

    val topCounts = words.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(30))
      .map { case (topic, count) => (count, topic) }
      .transform(_.sortByKey(false))


    topCounts.foreachRDD(rdd => {
      //take top 15 hashtags as per frequency
      val topList = rdd.take(top.toInt)
      println("\nTop hashtags in last 30 seconds (%s total):".format(rdd.count()))
      topList.foreach { case (count, tag) => println("%s (%s words)".format(tag, count))
        /*
      import scala.collection.immutable.Map
      // create a key-value map

     var m1: Map[String, Int] = Map()

      topList.foreach { case (count, tag) => {
        // add value to the Map
        m1 += (tag -> count)}
    */
      }
    })
    ssc.start()
    ssc.awaitTermination()
  }
}





