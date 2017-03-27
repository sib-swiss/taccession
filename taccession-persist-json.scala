import java.io._

//Starts the time
val start = System.currentTimeMillis();

def getListOfFiles(dir: String): List[File] = { val d = new File(dir); if (d.exists && d.isDirectory) { d.listFiles.filter(_.isFile).toList } else { List[File]() }}

val config = scala.io.Source.fromFile("config.properties").getLines().filter(l => (!l.startsWith("#") && !l.trim().isEmpty())).map(l => {val v = l.split("="); (v(0), v(1))}).toMap

val patternFile = config.get("PATTERN_FILE").getOrElse("pattern.properties")
val unsortedPatterns = scala.io.Source.fromFile(patternFile).getLines().filter(l => (!l.startsWith("#") && !l.trim().isEmpty())).map(l => {val v = l.split("="); (v(0), new scala.util.matching.Regex(v(1)))}).toMap
val patterns = scala.collection.immutable.TreeMap(unsortedPatterns.toSeq:_*)

//Defines the directory where the publications are stored
val PUBLI_DIR = config.get("PUBLI_DIR").getOrElse(".")

val pathFile = "file_names_to_process.txt"
val fileNames = getListOfFiles(PUBLI_DIR).map(f => f.getAbsolutePath()).toList
new PrintWriter(pathFile) { write(fileNames.mkString("\n")); close }


//Creates a domain class for manipulation and exporting the result.
//A word found in a publication for a certain entity (defined in patterns) at a line in a offset and with a length.
case class TokenMatch(tokenMatched: String, 
                      tokenMatchedLength: Integer,
                      fullWord: String, //A given word on a text file (word is separated by \\s+
                      tokenStartInWord:Integer,  //The start or index of of the token inside the fullWord
                      lineNumberWord: Integer, //The line in the text where the word appears
                      wordsOffset: Integer, //Number of words from the beginning of the line 
                      publication: String, 
                      journal: String,
                      patternName: String,
                      pattern: String)

//Function that opens a file, search for the patterns and returns a list of TokenMatch with the results
def searchTokens(fileName: String): List[TokenMatch] = {

    val journalPattern = "(.+\\/)*(.+)\\/\\S{1,}\\.txt".r
    val journal = journalPattern.findAllMatchIn(fileName).next().group(2)
    val file = scala.io.Source.fromFile(fileName)//.replaceAll("\\.\\/", "\\/"));

    val result  = 
      file.getLines().zipWithIndex.flatMap { //Reads all lines and keep the index to get the line number
      case (lineContent, lineNumber) => { 
        lineContent.split("\\s+").zipWithIndex.flatMap { //For each line, split it into many words
          case (fullWord, offset) => {
            //Check for all patterns
            patterns.map {
              case (patternName, pattern) => {
                pattern.findAllMatchIn(fullWord.toString).map(m => {
		   val (tokenMatched, tokenStart) = (m.toString(), m.start)
		   TokenMatch(tokenMatched, 
                             new Integer(tokenMatched.length), 
                             fullWord, 
                             new Integer(tokenStart), 
                             new Integer(lineNumber + 1), 
                             new Integer(offset),
                             fileName, 
                             journal,
                             patternName, 
                             pattern.toString)
		})}}}}}}.toList.flatten;

   file.close()
   return result
}

//This parameter can be tune (a benchmark as showed 200 is a good fit)
val minPartitions = config.get("MIN_PARTITIONS").getOrElse("200").toInt

//Reads paths 
val filesRDD = sc.textFile(pathFile, minPartitions)

//Reads all files (this is distributed among all workers)
val df = filesRDD.flatMap(searchTokens(_)).toDF()

//Save results in json format
df.select("tokenMatched", "tokenMatchedLength", "fullWord", "tokenStartInWord", "lineNumberWord", "wordsOffset", "publication", "patternName", "pattern").write.json("result-json")

println("Finished in " + (System.currentTimeMillis() - start) / (60 * 1000.0)  + " min")
