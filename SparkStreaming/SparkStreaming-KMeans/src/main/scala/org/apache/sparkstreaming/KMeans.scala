/**
  * Created by ACS on 2015/12/26.
  */
package org.apache.sparkstreaming

import org.apache.spark.SparkConf
import org.apache.spark.mllib.clustering.StreamingKMeans
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * forked from apache/spark
  * Estimate clusters on one stream of data and make predictions
  * on another stream, where the data stereams arrive as text files
  * into two different directories.
  *
  * The rows of the training text files must be vector data in the form
  * `[x1,x2,x3,...,xn]`
  * Where n is the number of dimensions.
  *
  * The rows of the test text files must be labeled data in the form
  * `(y,[x1,x2,x3,...,xn])`
  * Where y is some identifier. n must be the same for train and test.
  *
  * Usage:
  *   StreamingKMeansExample <trainingDir> <testDir> <batchDuration> <numClusters> <numDimensions>
  *
  * To run on your local machine using the two directories `trainingDir` and `testDir`,
  * with updates every 5 seconds, 2 dimensions per data point, and 3 clusters, call:
  *    $bin/run-example mllib.StreamingKMeansExample trainingDir testDir 5 3 2
  *
  * As you add text files to `trainingDir` the clusters will continuously update.
  * （添加文件到目录trainingDir）
  * Anytime you add text files to `testDir`, you'll see predicted labels using the current model.
  *  (添加文件到目录testDir)
  */
object KMeans {

  def main(args: Array[String]) {
    if (args.length != 5) {
      System.err.println(
        "Usage: StreamingKMeansExample " +
          "<trainingDir> <testDir> <batchDuration> <numClusters> <numDimensions>")   //参数：训练文件目录，测试文件目录，处理间隔，聚类数，维数（几维向量）
      System.exit(1)
    }

    val conf = new SparkConf().setMaster("local").setAppName("StreamingKMeans")
    val ssc = new StreamingContext(conf, Seconds(args(2).toLong))

    val trainingData = ssc.textFileStream(args(0)).map(Vectors.parse)   //训练文件
    val testData = ssc.textFileStream(args(1)).map(LabeledPoint.parse)   //测试文件

    val model = new StreamingKMeans()
      .setK(args(3).toInt)   //聚类数
      .setDecayFactor(1.0)
      .setRandomCenters(args(4).toInt, 0.0)   //随机设置中心

    model.trainOn(trainingData)
    model.predictOnValues(testData.map(lp => (lp.label, lp.features))).print()

    ssc.start()
    ssc.awaitTermination()
  }
}
