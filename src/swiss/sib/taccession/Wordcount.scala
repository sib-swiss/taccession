package swiss.sib.taccession

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import java.io.File


// http://data-flair.training/blogs/create-run-first-spark-project-scala-eclipse-without-maven/
// See this doc http://data-flair.training/blogs/install-configure-run-apache-spark-2-x-single-node/
object Wordcount {
  def maina(args: Array[String]) {

    //Create conf object
    val conf = new SparkConf().setAppName("WordCount").setMaster("local[2]").set("spark.executor.memory","1g");

    //create spark context object
    val sc = new SparkContext(conf)

    //Read file and create RDD
    val rawData = sc.textFile("/Users/dteixeira/tools/spark/README.md");

    //convert the lines into words using flatMap operation
    val words = rawData.flatMap(line => line.split(" "))

    //count the individual words using map and reduceByKey operation
    val wordCount = words.map(word => (word, 1)).reduceByKey(_ + _)

    //Save the result
    wordCount.saveAsTextFile("/tmp/results.txt")

    //stop the spark context
    sc.stop
  }
}