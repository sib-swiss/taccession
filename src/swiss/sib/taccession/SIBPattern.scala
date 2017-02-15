package swiss.sib.taccession

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import java.io.File
import org.apache.spark.sql.functions._
import java.util.logging.Level
import java.util.logging.Logger

// http://data-flair.training/blogs/create-run-first-spark-project-scala-eclipse-without-maven/
// See this doc http://data-flair.training/blogs/install-configure-run-apache-spark-2-x-single-node/
object SIBPattern {

  val PUBLI_DIR = "/scratch/cluster/monthly/dteixeir/publis/";

  //Test pattern
  val testPattern = SIBPatterns.uniprotPattern;

  case class TokenMatch(word: String, publication: String, line: Integer, offset: Integer, length: Integer)

  /**
   * Search for a list of tokens an retrieve a result with
   * word, publication, line number, offset and word length
   */
  def searchTokens(fileName: String): List[TokenMatch] = {

    val tokens = scala.io.Source.fromFile(PUBLI_DIR + fileName).getLines().zipWithIndex.flatMap {
      case (line, lineNumber) => {
        line.split(" ").zipWithIndex.map { case (word, offset) => TokenMatch(word, fileName, new Integer(lineNumber + 1), new Integer(offset), new Integer(word.length)) }
      }
    };

    tokens.filter(t => testPattern.findFirstIn((t.word)).isDefined).toList

  }

  def main(args: Array[String]) {

    //Create conf object
    val conf = new SparkConf().setAppName("SIBPattern")
      .setMaster("local[2]").set("spark.executor.memory", "1g");

    //create spark context object
    val sc = new SparkContext(conf)

    val sqlContext = new org.apache.spark.sql.SQLContext(sc)
    import sqlContext.implicits._

    //Read file and create RDD
    //Tip 1: don't use wholeTextFiles. Looks like the driver reads all the files before passing the job to the workers.
    val allFiles = sc.textFile(PUBLI_DIR + "file_names.txt")

    /*
    val fileNames = scala.io.Source.fromFile(PUBLI_DIR + "file_names.txt").getLines().toSeq;
    val allFiles = sc.parallelize(fileNames, 60)
     * 
     * 
     */
    val df = allFiles.flatMap(searchTokens(_)).toDF();

    val result = df.groupBy($"word").agg(count("*") as "numOccurances").orderBy($"numOccurances" desc)

    //result.saveAsTextFile("/tmp/results.txt")

    result.take(10)
    
    sc.stop
  }
}