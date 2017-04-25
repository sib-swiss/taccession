package swiss.sib.taccession

import java.io.File
import java.io.PrintWriter;
import scala.collection.immutable.HashMap

object Taccession {

  //Function that opens a file, search for the patterns and returns a list of TokenMatch with the results
  def searchTokens(patterns: Map[String, scala.util.matching.Regex], keywords: Map[String, List[String]], filePath: String): List[TokenMatch] = {

    val f = new File(filePath);
    val file = scala.io.Source.fromFile(f)
    
    val patternKeywordsFound = new java.util.HashMap[String, Boolean]();
    keywords.keySet.foreach(patternName => {patternKeywordsFound.put(patternName, false)})
        
    val result =
      file.getLines().zipWithIndex.flatMap { //Reads all lines and keep the index to get the line number
        case (lineContent, lineNumber) => {
          //Check for all patterns
          patterns.map {
            case (patternName, pattern) => {

              //If it's a pattern keyword and no keyword was found yet
              if(patternKeywordsFound.containsKey(patternName) && !patternKeywordsFound.get(patternName)){
                val foundKw = keywords.getOrElse(patternName, List()).find { kw => lineContent.toLowerCase().contains(kw.toLowerCase()) }.isDefined
                if(foundKw){
                  patternKeywordsFound.put(patternName, true);
                }
              }


              pattern.findAllMatchIn(lineContent.toString).map(m => {
                val (matchedPattern, columnNumber) = (m.toString(), m.start)

                //Defining context
                val (contextStart, startText) = if ((m.start - 30) > 0) ((m.start - 30), "...") else (0, "");
                val (contextEnd, endText) = if ((m.start + 30) <= (lineContent.length() - 1)) ((m.start + 30), "...") else (lineContent.length() - 1, "");

                val context = lineContent.substring(contextStart, contextEnd)
                TokenMatch(matchedPattern,
                  startText + context + endText,
                  new Integer(matchedPattern.length),
                  new Integer(lineNumber + 1),
                  new Integer(columnNumber + 1),
                  f.getName,
                  patternName)
              })
            }
          }
        }
      }.toList.flatten;

      
    file.close();
    return result.filter(r => {
      if(patternKeywordsFound.containsKey(r.patternName)){
        patternKeywordsFound.get(r.patternName); //Filter out the element if it was not found (false)
      }else true;
    })
      
  }

}