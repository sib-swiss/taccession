//Starts the time
val start = System.currentTimeMillis();

//Defines the directory where the publications are stored
val PUBLI_DIR = "/scratch/cluster/monthly/dteixeir/publis/";

//Creates a map with several regular expressions
val patterns = Map(
    "uniprot" -> "[OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2}".r,
    "string" -> "^([A-N,R-Z][0-9][A-Z][A-Z, 0-9][A-Z, 0-9][0-9])|([O,P,Q][0-9][A-Z, 0-9][A-Z, 0-9][A-Z, 0-9][0-9])|([0-9][A-Za-z0-9]{3})$".r,
    "swisslipid" -> "^SLM:d+$".r,
    "bgee" -> "^(ENS|FBgn)w+$".r,
    "bgee-organ" -> "^(XAO|ZFA|EHDAA|EMAPA|EV|MA):d+$".r,
    "bgee-stage" -> "^(FBvd|XtroDO|HsapDO|MmusDO):d+$".r,
    "bgee-family" -> "^(ENSFM|ENSGTV:)d+$".r,
    "nextprot" -> "^NX_w+".r,
    "mail" -> "isb-sib.chw+$".r //val cellosaurusCVCL = "^wCVCL+$".r
    //Cellosorus / CALOHA ? SwissRegulon
)

//Creates a domain class for manipulation and exporting the result.
//A word found in a publication for a certain entity (defined in patterns) at a line in a offset and with a length.
case class TokenMatch(word: String, publication: String, entity: String, line: Integer, offset: Integer, length: Integer)

//Function that opens a file, search for the patterns and returns a list of TokenMatch with the results
def searchTokens(fileName: String): List[TokenMatch] = {

    val file = scala.io.Source.fromFile(PUBLI_DIR + fileName);

    val result = file.getLines().zipWithIndex.flatMap {
      case (lineContent, lineNumber) => {
        lineContent.split(" ").zipWithIndex.flatMap {
          case (word, offset) => {
            //Check for all patterns
            patterns.map {
              case (key, pattern) => {
                if (pattern.findFirstIn(word).isDefined) {
                  TokenMatch(word, fileName, key, new Integer(lineNumber + 1), new Integer(offset), new Integer(word.length))
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

val result = df.filter($"entity" !== "string").filter($"entity" !== "uniprot").groupBy($"word", $"entity").agg(count("*") as "numOccurances").orderBy($"numOccurances" desc)

val start2 = System.currentTimeMillis();

//Gets the first 10 results
result.take(10)

println("Finished in " + (System.currentTimeMillis() - start) / (60 * 1000.0)  + " min")
println("Finished in " + (System.currentTimeMillis() - start2) / (60 * 1000.0)  + " min")

