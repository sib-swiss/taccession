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
      
    //Test pattern
    val testPattern = SIBPatterns.uniprotPattern;

  def isMatch(s: String): Boolean = {
    val boolean = testPattern.findFirstIn(s).isDefined
    return boolean
  }
    
  case class TokenMatch(word: String, 
                        publication: String, 
                        line : Integer, 
                        offset: Integer, 
                        length: Integer)
  
  /**
   * Search for a list of tokens an retrieve a result with 
   * word, publication, line number, offset and word length                      
   */
  def searchTokens(publication: String, content: String) : List[(String, String, Integer, Integer, Integer)]  = {
  
    val regexToExtractFilname = "^file:/(.+/)*(.+)$".r;    
    val publiName = regexToExtractFilname.findAllIn(publication).matchData.next().group(2);
    
    val tokens = content.split("\n").zipWithIndex.flatMap{case (line, lineNumber) => {
      line.split(" ").zipWithIndex.map{ case(word, offset) => (word, publiName, new Integer(lineNumber+1), new Integer(offset), new Integer(word.length)) }
    }};
    
    tokens.filter(t => isMatch(t._1)).toList

  }
  
  def main(args: Array[String]) {

    //Create conf object
    val conf = new SparkConf().setAppName("SIBPattern")
    //.setMaster("local[2]").set("spark.executor.memory","1g");

    //create spark context object
    val sc = new SparkContext(conf)

    val sqlContext= new org.apache.spark.sql.SQLContext(sc)
    import sqlContext.implicits._

    //Read file and create RDD
    val files = sc.wholeTextFiles("/scratch/cluster/monthly/dteixeir/publis/sample70k/**", 100);
    
    val df = files.flatMap{case (filename, content) =>  searchTokens(filename, content)}.toDF();
    
    df.cache();
    
    df.take(10).foreach(println)
    
    val result = df.groupBy($"_1").agg(count("*") as "numOccurances").orderBy($"numOccurances" desc)  

    result.take(10).foreach(println)
    sc.stop
  }
}