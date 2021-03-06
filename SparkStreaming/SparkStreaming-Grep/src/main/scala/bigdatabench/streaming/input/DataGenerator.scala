package bigdatabench.streaming.input

import java.io.{ByteArrayOutputStream, IOException}
import java.net.ServerSocket
import java.nio.ByteBuffer

import scala.io.Source

import org.apache.spark.{SparkContext,SparkConf, Logging}
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.util.IntParam



/**
  *forked from tdas/spark-streaming-benchmark/.
 * A helper program that sends blocks of Kryo-serialized text strings out on a socket at a
 * specified rate. Used to feed data into RawInputDStream.
 *
 * Files of the required line length can be generated by this two lines in the Spark-shell 
 * 
 *    val text = (1 to 1000).map(x => { (1 to 15).map(_ => scala.util.Random.alphanumeric.take(6).mkString("")).mkString(" ") }).mkString("\n")
 *    org.apache.commons.io.FileUtils.writeStringToFile(new java.io.File("test.txt"), text)
 *
 */
object DataGenerator {
  def main(args: Array[String]) {
    if (args.length != 3) {
      System.err.println("Usage: RawTextSender <port> <file> <bytesPerSec>")
      System.exit(1)
    }
    // Parse the arguments using a pattern match
    val (port, file, bytesPerSec) = (args(0).toInt, args(1), args(2).toInt)
    val blockSize = bytesPerSec / 10
    // Repeat the input data multiple times to fill in a buffer
    val lines = Source.fromFile(file).getLines().toArray
    val bufferStream = new ByteArrayOutputStream(blockSize + 1000)
    val ser = new KryoSerializer(new SparkConf()).newInstance()
    val serStream = ser.serializeStream(bufferStream)
    var i = 0
    while (bufferStream.size < blockSize) {
      serStream.writeObject(lines(i))
      i = (i + 1) % lines.length
    }
    val array = bufferStream.toByteArray

    val countBuf = ByteBuffer.wrap(new Array[Byte](4))
    countBuf.putInt(array.length)
    countBuf.flip()

    val serverSocket = new ServerSocket(port)
    println("Listening on port " + port)

    while (true) {
      val socket = serverSocket.accept()
      println("Got a new connection")
      val out = new RateLimitedOutputStream(socket.getOutputStream, bytesPerSec)
      try {
        while (true) {
          out.write(countBuf.array)
          out.write(array)
        }
      } catch {
        case e: IOException =>
          println("Client disconnected")
          socket.close()
      }
    }
  }
}

