package swiss.sib.taccession

//import java.io.File
import java.io.{FileInputStream, PrintWriter}
import java.util.zip._

import scala.io.BufferedSource
import scala.util.matching.Regex

object Taccession {

  def searchTokenForPattern(patternName: String, pattern: Regex, mustFindRegexInFile: Option[Regex], excludeTokens: Option[Regex], lineContent: String, lineNumber: Int, fileName: String): List[TokenMatch] = {

    val foundKeywordValue: Boolean = if (mustFindRegexInFile.isDefined) {
      mustFindRegexInFile.get.findAllIn(lineContent).size > 0
    } else true

    val result = pattern.findAllMatchIn(lineContent).map(m => {
      val (matchedPattern, columnNumber) = (m.toString(), m.start)

      //Defining context
//      val (contextStart, startText) = if ((m.start - 50) > 0) ((m.start - 50), "...") else (0, "");
//      val (contextEnd, endText) = if ((m.start + 50) <= (lineContent.length() - 1)) ((m.start + 50), "...") else (lineContent.length() - 1, "");
    

      val context = lineContent; // .substring(contextStart, contextEnd).replaceAll("\"", "")
        println(context)
      TokenMatch(matchedPattern,
        context,
        new Integer(matchedPattern.length),
        new Integer(lineNumber + 1),
        new Integer(columnNumber + 1),
        fileName,
        patternName,
        foundKeywordValue,
        if (excludeTokens.isDefined) {excludeTokens.get.findAllIn(matchedPattern).size > 0} else {false})
    }).toList
    if(foundKeywordValue && result.isEmpty){
      return List(TokenMatch(
      "placeholder", "", 0, new Integer(lineNumber + 1), 0, fileName, patternName, foundKeywordValue, true)
    )
    }else {
      return result;
    }
  }

  //Function that opens a file, search for the patterns and returns a list of TokenMatch with the results
  def searchTokens(patterns: List[TConfigPattern], filePath: String): List[TokenMatch] = {
// to read gunzipped file in XML format for TREC data

    val rawResult : List[ TokenMatch ] = filePath match {
      case _ if filePath.endsWith("gz") => {
        // directly read from gz file
        println("--------------------------> reading file "+filePath)
        val fis: FileInputStream = new FileInputStream(filePath)
        val gzis: GZIPInputStream = new GZIPInputStream(fis)
        val xmlfile : BufferedSource = scala.io.Source.fromInputStream(gzis)
        val pubmeds : List[ PubMed ] = PubMed.fromSource( xmlfile ).filter { p => p.synopsis.nonEmpty }

        gzis.close()
        fis.close()

        pubmeds.flatMap {
          pm : PubMed => {
            pm.synopsis.split( """\r?\n""").toList.zipWithIndex.flatMap {
              case ( line : String, i : Int ) => {
                patterns.flatMap( p => {
                  val out: List[ TokenMatch ] = searchTokenForPattern( p.patternName, p.pattern, p.mustFindRegexInFile, p.excludeTokens, line, i, pm.pmid )
                  //out.foreach { t => println("====> "+t.publicationName+" "+t.patternName+" "+t.context+" "+t.matchedPattern+" "+t.columnNumber+" "+t.containsExcludedToken+" "+t.containsMustFindKeywordInCurrentLine+" "+t.lineNumber+" "+t.matchedPatternLength+" "+t.publicationName )}
                  out
                } )
              }
            }
          }
        }

      }
      case _ => {
  
        val f    = new java.io.File(filePath)
        val file = scala.io.Source.fromFile(filePath)
        val out : List[ TokenMatch ] = file.mkString.split("""\.(?=\s+|$)""").zipWithIndex.flatMap {
          //Reads all sentences and keep the index to get the sentence number
          case (sentenceContent, sentenceNumber) => {
            //Check for all patterns
            patterns.flatMap(p => {
              searchTokenForPattern(p.patternName, p.pattern, p.mustFindRegexInFile, p.excludeTokens, sentenceContent, sentenceNumber, f.getName)
            })
          }
        }
        file.close()
        out
      }
//      case _ => {
//
//        val f    = new java.io.File(filePath)
//        val file = scala.io.Source.fromFile(filePath)
//
//        val out : List[ TokenMatch ] = file.getLines().zipWithIndex.flatMap {
//          //Reads all lines and keep the index to get the line number
//          case (lineContent, lineNumber) => {
//            //Check for all patterns
//            patterns.flatMap(p => {
//              searchTokenForPattern(p.patternName, p.pattern, p.mustFindRegexInFile, p.excludeTokens, lineContent, lineNumber, f.getName)
//            })
//          }
//        }.toList
//        file.close()
//        out
//      }
    }
    //Removes tokens where the keyword was specified but not found in the document
    //It looks a bit weird to filter at the end, but it was done like this because it performs better to read each line only once
    val keywordFoundResults : List[TokenMatch] = patterns.flatMap(p => {
      val tokensFoundForPattern = rawResult.filter(t => t.patternName.equals(p.patternName))
      if(p.mustFindRegexInFile.isDefined){
        val foundKeyword = tokensFoundForPattern.find(t => t.containsMustFindKeywordInCurrentLine)
        if(foundKeyword.isDefined)
          Some(tokensFoundForPattern)
        else None
      }else Some(tokensFoundForPattern)
    }).flatten

    //Filters out the excluded tokens
    val excludedTokensResults = keywordFoundResults.filter(t => !t.containsExcludedToken).toList

    excludedTokensResults
  }

}
