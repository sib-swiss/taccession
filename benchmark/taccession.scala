//Starts the time
val start = System.currentTimeMillis();

//Defines the directory where the publications are stored
val PUBLI_DIR = "/scratch/local/monthly/dteixeir/publis/";

//Creates a map with several regular expressions
val patterns = Map(
    "uniprot" -> "[OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2}".r,
    "string" -> "^([A-N,R-Z][0-9][A-Z][A-Z, 0-9][A-Z, 0-9][0-9])|([O,P,Q][0-9][A-Z, 0-9][A-Z, 0-9][A-Z, 0-9][0-9])|([0-9][A-Za-z0-9]{3})$".r,
    "swisslipid" -> "(?i)SLM:\\d+".r,
    "bgee" -> "^(ENS|FBgn)w+$".r,
    "bgee-organ" -> "^(XAO|ZFA|EHDAA|EMAPA|EV|MA):d+$".r,
    "bgee-stage" -> "^(FBvd|XtroDO|HsapDO|MmusDO):d+$".r,
    "bgee-family" -> "^(ENSFM|ENSGTV:)d+$".r,
    "nextprot" -> "NX_\\w+".r,
    "mail" -> "[A-Za-z0-9_.+-]*@(?i)(isb-sib\\.ch|sib\\.swiss)".r,
    "cellosaurus" -> "(?i)CVCL_\\w{4,6}+".r,
    "hgvs-frameshift" -> "^p\\.([A-Z])([a-z]{2})?(\\d+)([A-Z])([a-z]{2})?fs(?:\\*|Ter)(\\d+)$".r,
    "hgvs-single-modification" -> "^(\\w+)-([A-Z])([a-z]{2})?(\\d+)$".r,
    "hgvs-duplication" -> "^p\\.([A-Z])([a-z]{2})?(\\d+)(?:_([A-Z])([a-z]{2})?(\\d+))?dup$".r,
    "hgvs-deletion" -> "^p\\.([A-Z])([a-z]{2})?(\\d+)(?:_([A-Z])([a-z]{2})?(\\d+))?del$".r,
    "hgvs-deletion-insertion" -> "^p\\.([A-Z])([a-z]{2})?(\\d+)(?:_([A-Z])([a-z]{2})?(\\d+))?delins((?:[A-Z\\*]([a-z]{2})?)+)$".r,
    "hgvs-insertion" -> "^p\\.([A-Z])([a-z]{2})?(\\d+)_([A-Z])([a-z]{2})?(\\d+)ins((?:[A-Z\\*]([a-z]{2})?)+)$".r
)

//Creates a domain class for manipulation and exporting the result.
//A word found in a publication for a certain entity (defined in patterns) at a line in a offset and with a length.
case class TokenMatch(word: String, journal: String, publication: String, entity: String, line: Integer, offset: Integer, length: Integer)

//Function that opens a file, search for the patterns and returns a list of TokenMatch with the results
def searchTokens(fileName: String): List[TokenMatch] = {

    val journalPattern = "(.+\\/)*(.+)\\/\\S{1,}\\.txt".r
    val journal = journalPattern.findAllMatchIn(fileName).next().group(2)
    
    val file = scala.io.Source.fromFile(PUBLI_DIR + fileName);

    val result = file.getLines().zipWithIndex.flatMap {
      case (lineContent, lineNumber) => {
        lineContent.split(" ").zipWithIndex.flatMap {
          case (word, offset) => {
            //Check for all patterns
            patterns.map {
              case (key, pattern) => {
                if (pattern.findFirstIn(word).isDefined) {
                  TokenMatch(word, journal, fileName, key, new Integer(lineNumber + 1), new Integer(offset), new Integer(word.length))
                } else null
              }
            }
          }
        }
      }
    }.filter(_ != null).toList;
    
   file.close()

   return result
}

//Change this value to have different results
val minPartitions = 200;

//Reads a file containing alls files to be parsed
val allFiles = sc.textFile(PUBLI_DIR + "file_names.txt", minPartitions)

//Reads all files
val df = allFiles.flatMap(searchTokens(_)).toDF()

//Loads result in cache
df.persist()

val result = df.filter($"entity" !== "string").filter($"entity" !== "uniprot").groupBy($"word", $"entity").agg(count("*") as "numOccurances").orderBy($"numOccurances" desc)


// Print result by journals
df.groupBy($"entity", $"entity").agg(count("*") as "numOccurances").orderBy($"numOccurances" desc).take(10).foreach(println)

// Print result by journals (distinct words)
df.groupBy($"entity", $"entity").agg(countDistinct("word") as "numOccurances").orderBy($"numOccurances" desc).take(10).foreach(println)

//Print for each scenario
patterns.keys.foreach{ k =>
  println("Show top journals for " + k)
  df.filter($"entity" === k).groupBy($"journal", $"entity").agg(count("*") as "numOccurances").orderBy($"numOccurances" desc).take(10).foreach(println)
  
  println("Show top words for " + k)
  df.filter($"entity" === k).groupBy($"word", $"entity").agg(count("*") as "numOccurances").orderBy($"numOccurances" desc).take(10).foreach(println)

  println("Show sample for " + k)
  df.filter($"entity" === k ).take(10).foreach(println)

}

println("Finished in " + (System.currentTimeMillis() - start) / (60 * 1000.0)  + " min")