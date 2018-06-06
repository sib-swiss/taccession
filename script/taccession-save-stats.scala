import swiss.sib.taccession._
import org.apache.spark.sql.DataFrame

//Starts the time
val start = System.currentTimeMillis();

val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm");
val date = java.time.LocalDateTime.now();
val fileSuffix = "" //date.format(formatter)

//Init configs
val config = TaccessionUtils.readConfigFile(System.getProperty("config.file"))

val filePaths: String = TaccessionUtils.getFilePaths(config)

//Reads paths 
val filesRDD = sc.textFile(filePaths, config.sparkPartitions)


//Reads all files (this is distributed among all workers)
val df = filesRDD.flatMap(f => Taccession.searchTokens(config.patterns, f)).toDF().as("dfAll")
df.cache()


def writeToCsv(df: org.apache.spark.sql.DataFrame, fileName: String): Boolean = {
  if (!df.rdd.isEmpty) {
    println("Writing for " + config.statsOutputFolder.getPath + "/" + fileName)

    df.coalesce(1).write.option("header", "true").csv(config.statsOutputFolder.getPath + "/" + fileName)

    return true
  } else {
    println("Nothing found for " + fileName)
    return false
  }
}

def getRandomValuesFromDataFrame(dataframe: DataFrame, limitForRandom: Double): DataFrame = {
  val dfPatternCount = dataframe.count
  if (dfPatternCount > limitForRandom) {
    //If the size of dataframe is bigger than the limit, we take a sample
    dataframe.sample(true, (limitForRandom / dfPatternCount))
  } else {
    //In other case we take the original dataframe
    dataframe
  }
}


def findTopForPattern(patternName: String) = {

  var foundTop = true;
  List(("top", 20, $"count" desc), ("bottom", 20, $"count" asc)).foreach(op => {

    if (foundTop) { //No need to check bottom if top is not found
      val (top, limit, order) = (op._1, op._2, op._3)
      val topPatterns = df.filter($"patternName" === patternName).select($"matchedPattern", $"patternName").groupBy($"patternName", $"matchedPattern").count().orderBy(order).limit(10000).as("dfTop")
      topPatterns.cache();

      val result = writeToCsv(topPatterns.select("matchedPattern", "count"), "stats-csv-" + fileSuffix + "/" + patternName + "/" + top)

      if (result) {
        val matchedPatternString = topPatterns.select("matchedPattern").rdd.map(r => r(0)).take(limit)
        matchedPatternString.foreach(ms => {
          val msString = ms.toString
          val msStringEscapedForFileName = msString.replaceAll("[^a-zA-Z0-9.-]", "_")

          val dfFiltered = df.filter($"matchedPattern" === msString);
          writeToCsv(dfFiltered.select("matchedPattern", "context", "lineNumber", "columnNumber", "publicationName").limit(20), "stats-csv-" + fileSuffix + "/" + patternName + "/" + top + "/" + msStringEscapedForFileName)
        })
      } else {
        //No need to check bottom if top is not found
        foundTop = false
      }
    }
  })
}

//Random values for each patterns
config.patterns.foreach(p => {

  val dfFilterRandoms = getRandomValuesFromDataFrame(df.filter($"patternName" === p.patternName), 2500.0)
  val dfFilterRandomsWithCorrectHeader = dfFilterRandoms.select("matchedPattern", "context", "lineNumber", "columnNumber", "publicationName")
  if (!dfFilterRandomsWithCorrectHeader.rdd.isEmpty) {
    val result = writeToCsv(dfFilterRandomsWithCorrectHeader, "stats-csv-" + fileSuffix + "/" + p.patternName + "/random")
    if (result) {
      findTopForPattern(p.patternName)
    }
  } else {
    println("Nothing found for " + p.patternName)
  }
  
})

println("Finished in " + (System.currentTimeMillis() - start) / (60 * 1000.0) + " min")
