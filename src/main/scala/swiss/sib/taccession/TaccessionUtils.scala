package swiss.sib.taccession

import java.io.File
import java.io.PrintWriter

import scala.util.matching.Regex

import net.jcazevedo.moultingyaml.DefaultYamlProtocol
import net.jcazevedo.moultingyaml.PimpedString // if you don't supply your own protocol

case class YamlConfigPattern(patternName: String, pattern: String, mustFindRegexInFile: Option[String], excludeTokens: Option[String])

case class YamlConfig(name: String,
                      filesDirectory: String,
                      statsOutputFolder: String,
                      dataOutputFolder: String,
                      sparkPartitions: Int,
                      patterns: List[YamlConfigPattern])

object ConfigYamlProtocol extends DefaultYamlProtocol {
  implicit val configPatternFormat = yamlFormat4(YamlConfigPattern)
  implicit val configFormat = yamlFormat6(YamlConfig)
}

object TaccessionUtils {

  def convertYamlToTConfig(yc: YamlConfig): TConfig = {
    val patterns : List[TConfigPattern] = yc.patterns.map{
      p => TConfigPattern(p.patternName, 
                      new Regex(p.pattern), 
                      if(p.mustFindRegexInFile.isDefined) Some(new Regex(p.mustFindRegexInFile.get)) else None, 
                      if(p.excludeTokens.isDefined) Some(new Regex(p.excludeTokens.get)) else None
                    )}.toList

    val config = new TConfig(yc.name, new File(yc.filesDirectory), new File(yc.statsOutputFolder), new File(yc.dataOutputFolder), yc.sparkPartitions, patterns)
    println("Config: " + config)
    config;
                    
  }
  
  def readConfigFile(configFile: String): TConfig = {
    import ConfigYamlProtocol._
    convertYamlToTConfig(scala.io.Source.fromFile(configFile).mkString.parseYaml.convertTo[YamlConfig])
  }

  def getFilePaths(config: TConfig): String = {

    //Read the absolute path of the publication in the given directory
    val pathFile = "file_names_to_process.txt"
    val fileNames = recursiveListFiles(config.filesDirectory, """\.(?:gz|txt|xml)$""".r).map(f => f.getAbsolutePath()).toList

    new PrintWriter(pathFile) { write(fileNames.mkString("\n")); close }

    return pathFile;

  }

  //Returns a list of files for a given directory
  def recursiveListFiles(f: File, r: Regex): Array[File] = {
    val these = f.listFiles
    val good = these.filter(f => r.findFirstIn(f.getName).isDefined)
    good ++ these.filter(_.isDirectory).flatMap(recursiveListFiles(_, r))
  }

}