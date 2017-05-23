package swiss.sib.taccession

import java.io.File
import java.io.PrintWriter;
import scala.collection.immutable.HashMap
import scala.util.matching.Regex

object Taccession {

  def searchTokenForPattern(patternName: String, pattern: Regex, mustFindRegexInFile: Option[Regex], excludeTokens: Option[Regex], lineContent: String, lineNumber: Int, fileName: String): List[TokenMatch] = {

    val foundKeywordValue: Boolean = if (mustFindRegexInFile.isDefined) {
      mustFindRegexInFile.get.findAllIn(lineContent).size > 0
    } else true;

    val result = pattern.findAllMatchIn(lineContent).map(m => {
      val (matchedPattern, columnNumber) = (m.toString(), m.start)

      //Defining context
      val (contextStart, startText) = if ((m.start - 50) > 0) ((m.start - 50), "...") else (0, "");
      val (contextEnd, endText) = if ((m.start + 50) <= (lineContent.length() - 1)) ((m.start + 50), "...") else (lineContent.length() - 1, "");

      val context = lineContent.substring(contextStart, contextEnd).replaceAll("\"", "");
      TokenMatch(matchedPattern,
        startText + context + endText,
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

    val f = new File(filePath);
    val file = scala.io.Source.fromFile(f)

    val rawResult =
      file.getLines().zipWithIndex.flatMap { //Reads all lines and keep the index to get the line number
        case (lineContent, lineNumber) => {
          //Check for all patterns
          patterns.flatMap(p => {
            searchTokenForPattern(p.patternName, p.pattern, p.mustFindRegexInFile, p.excludeTokens, lineContent, lineNumber, f.getName)
          })
        }
      }.toList;

    file.close();
    
    //Removes tokens where the keyword was specified but not found in the document
    //It looks a bit weird to filter at the end, but it was done like this because it performs better to read each line only once
    val keywordFoundResults : List[TokenMatch] = patterns.flatMap(p => {
      val tokensFoundForPattern = rawResult.filter(t => t.patternName.equals(p.patternName));
      if(p.mustFindRegexInFile.isDefined){
        val foundKeyword = tokensFoundForPattern.find(t => t.containsMustFindKeywordInCurrentLine);
        if(foundKeyword.isDefined)
          Some(tokensFoundForPattern)
        else None
      }else Some(tokensFoundForPattern)
    }).flatten

    //Filters out the excluded tokens
    val excludedTokensResults = keywordFoundResults.filter(t => !t.containsExcludedToken).toList;

    return excludedTokensResults;
  }

}
