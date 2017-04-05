package swiss.sib.taccession

import java.io.File
import java.io.PrintWriter;

object Taccession {

  //Function that opens a file, search for the patterns and returns a list of TokenMatch with the results
  def searchTokens(patterns: Map[String, scala.util.matching.Regex], filePath: String): List[TokenMatch] = {

    val f = new File(filePath);
    val file = scala.io.Source.fromFile(f)

    val result =
      file.getLines().zipWithIndex.flatMap { //Reads all lines and keep the index to get the line number
        case (lineContent, lineNumber) => {
          //Check for all patterns
          patterns.map {
            case (patternName, pattern) => {
              pattern.findAllMatchIn(lineContent.toString).map(m => {
                val (matchedPattern, columnNumber) = (m.toString(), m.start)
                
                //Defining context
                val (contextStart, startText) = if((m.start - 30) > 0) ((m.start - 30), "...") else (0, "");
                val (contextEnd, endText)  = if((m.start + 30) <= (lineContent.length() - 1)) ((m.start + 30), "...") else (lineContent.length() - 1, "");
                
                val context = lineContent.substring(contextStart, contextEnd)
                TokenMatch(matchedPattern,
                  startText + context + endText,  
                  new Integer(matchedPattern.length),
                  new Integer(lineNumber + 1),
                  new Integer(columnNumber),
                  f.getName,
                  patternName)
              })
            }
          }
        }
      }.toList.flatten;

    file.close()
    return result
  }

}