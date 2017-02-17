//Starts the time
val start = System.currentTimeMillis();

val patterns = scala.io.Source.fromFile("patterns.txt").getLines().filter(l => !l.startsWith("#")).map(l => {val v = l.split("="); (v(0), new scala.util.matching.Regex(v(1)))}).toMap

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
        lineContent.split("\\s+").zipWithIndex.flatMap {
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

//This parameter can be tune (a benchmark as showed 200 is a good fit)
val minPartitions = 200;

//Reads paths 
val allFiles = sc.textFile(PUBLI_DIR + "file_names.txt", minPartitions)

//Reads all files (this is distributed among all workers)
val df = allFiles.flatMap(searchTokens(_)).toDF()

//Loads result in cache
df.cache()

//Function to define a way to print dataframe results
def printSample(_df: org.apache.spark.sql.DataFrame) = {_df.take(10).foreach(l => println("\t\t" + l.mkString("\t")))}

//Print stats per keywords
patterns.keys.foreach{ k =>

  val entity = df.filter($"entity" === k)
  val cnt = entity.count()
  
  println("Found " + cnt + " matches for " + k + " :")
   
  if(cnt > 0) {

      println("\n\tShow top journals for " + k + " :")
      printSample(entity.groupBy($"journal", $"entity").agg(count("*") as "numOccurances").orderBy($"numOccurances" desc))
      
      println("\n\tCount distinct words per journal " + k + " :")
      printSample(entity.groupBy($"journal", $"entity").agg(countDistinct("word") as "numOccurances").orderBy($"numOccurances" desc))
      
      println("\n\tShow top words for " + k + " :")
      printSample(entity.groupBy($"word", $"entity").agg(count("*") as "numOccurances").orderBy($"numOccurances" desc))
    
      println("\n\tShow sample for " + k + " :")
      printSample(entity)

  }
  
  println("\n * * * * * * * * * * * * * * \n\n")


}

println("Finished in " + (System.currentTimeMillis() - start) / (60 * 1000.0)  + " min")

exit