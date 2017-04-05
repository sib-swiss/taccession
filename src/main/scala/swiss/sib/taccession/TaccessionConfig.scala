package swiss.sib.taccession

import java.io.File
import java.io.PrintWriter
import scala.util.matching.Regex

object TaccessionConfig {

  //Returns a list of files for a given directory
  def recursiveListFiles(f: File, r: Regex): Array[File] = {
    val these = f.listFiles
    val good = these.filter(f => r.findFirstIn(f.getName).isDefined)
    good ++ these.filter(_.isDirectory).flatMap(recursiveListFiles(_, r))
  }
  
  def init(configFile: String): Map[String, String] = {
    return scala.io.Source.fromFile(configFile).getLines().filter(l => (!l.startsWith("#") && !l.trim().isEmpty())).map(l => { val v = l.split("="); (v(0), v(1)) }).toMap
  }

  def getOutputFolder(config: Map[String, String]): String = {
    return config.get("OUTPUT_FOLDER").getOrElse(".")
  }

  def getFilePaths(config: Map[String, String]): String = {

    //Defines the directory where the publications are stored
    val PUBLI_DIR = config.get("PUBLI_DIR").getOrElse(".")

    //Read the absolute path of the publication in the given directory
    val pathFile = "file_names_to_process.txt"
    val fileNames = recursiveListFiles(new File(PUBLI_DIR), """\.txt$""".r).map(f => f.getAbsolutePath()).toList

    new PrintWriter(pathFile) { write(fileNames.mkString("\n")); close }

    return pathFile;

  }

  def getMinPartitions(config: Map[String, String]): Integer = {
    //This parameter can be tune (a benchmark as showed 200 is a good fit)
    return config.get("MIN_PARTITIONS").getOrElse("200").toInt
  }

  def getPatterns(config: Map[String, String]): Map[String, Regex] = {

    //Reads the patterns, parses them and save them in an ordered map
    val patternFile = config.get("PATTERN_FILE").getOrElse("pattern.properties")
    val unsortedPatterns = scala.io.Source.fromFile(patternFile).getLines().filter(l => (!l.startsWith("#") && !l.trim().isEmpty())).map(l => { val v = l.split("="); (v(0), new scala.util.matching.Regex(v(1))) }).toMap
    val patterns = scala.collection.immutable.TreeMap(unsortedPatterns.toSeq: _*)

    return patterns;

  }

}