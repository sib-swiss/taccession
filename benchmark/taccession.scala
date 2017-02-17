//Starts the time
val start = System.currentTimeMillis();

val patterns = scala.io.Source.fromFile("patterns.txt").getLines().map(l => {val v = l.split("="); (v(0), new scala.util.matching.Regex(v(1)))}).toMap

//Defines the directory where the publications are stored
val PUBLI_DIR = "/scratch/local/monthly/dteixeir/publis/";

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

  println("\nShow top journals for " + k + " :")
  df.filter($"entity" === k).groupBy($"journal", $"entity").agg(count("*") as "numOccurances").orderBy($"numOccurances" desc).take(10).foreach(println)
  
  println("\nShow top words for " + k + " :")
  df.filter($"entity" === k).groupBy($"word", $"entity").agg(count("*") as "numOccurances").orderBy($"numOccurances" desc).take(10).foreach(println)

  println("\nShow sample for " + k + " :")
  df.filter($"entity" === k ).take(10).foreach(_ => println(_))

}

println("Finished in " + (System.currentTimeMillis() - start) / (60 * 1000.0)  + " min")