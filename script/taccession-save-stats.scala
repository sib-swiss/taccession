import swiss.sib.taccession._

//Starts the time
val start = System.currentTimeMillis();

val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm");
val date = java.time.LocalDateTime.now();
val fileSuffix = "" //date.format(formatter)

//Init configs
val config = TaccessionUtils.readConfigFile(System.getProperty("config.file"))

//Reads paths 
val filesRDD = sc.textFile(config.filesDirectory.getPath, config.sparkPartitions)

//Reads all files (this is distributed among all workers)
val df = filesRDD.flatMap(f => Taccession.searchTokens(config.patterns, f)).toDF().as("dfAll")
df.cache()

def writeToCsv(df: org.apache.spark.sql.DataFrame, fileName: String): Boolean = {
  if (!df.rdd.isEmpty) {
    println("Writing for " + fileName)
    df.coalesce(1).write.option("header", "true").csv(outputFolder + "/" + fileName)
    return true
  } else {
    println("Nothing found for " + fileName)
    return false
  }
}

def findTopForPattern(patternName: String) = {

  var foundTop = true;
  List(("top", 20, $"count" desc), ("bottom", 20, $"count" asc)).foreach(op => {

    if (foundTop) { //No need to check bottom if top is not found
      val (top, limit, order) = (op._1, op._2, op._3)
      val topPatterns = df.filter($"patternName" === patternName).select($"matchedPattern", $"patternName").groupBy($"patternName", $"matchedPattern").count().orderBy(order).limit(5000).as("dfTop")
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
patterns.foreach(p => {
  val dfFiltered = df.filter($"patternName" === p._1).select("matchedPattern", "context", "lineNumber", "columnNumber", "publicationName").limit(1000)
  if (!dfFiltered.rdd.isEmpty) {
    val result = writeToCsv(dfFiltered, "stats-csv-" + fileSuffix + "/" + p._1 + "/random")
    if (result) {
        findTopForPattern(p._1)
    }
  } else {
    println("Nothing found for " + p._1)
  }
})


println("Finished in " + (System.currentTimeMillis() - start) / (60 * 1000.0) + " min")
