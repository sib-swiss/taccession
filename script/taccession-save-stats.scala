import swiss.sib.taccession._

//Starts the time
val start = System.currentTimeMillis();

//Init configs
val config = TaccessionConfig.init("config.properties")

//File containing all path of the files to parse (a trick for spark going faster)
val filePaths = TaccessionConfig.getFilePaths(config);

//Reads paths 
val filesRDD = sc.textFile(filePaths, TaccessionConfig.getMinPartitions(config))

//Patterns
val patterns = TaccessionConfig.getPatterns(config);

//Reads all files (this is distributed among all workers)
val df = filesRDD.flatMap(f => Taccession.searchTokens(patterns, f)).toDF()

val topMostFrequentPatterns = 
 df.withColumn("freq", lit("top"))
.select($"matchedPattern", $"patternName", $"freq")
.groupBy($"patternName", $"matchedPattern").count().orderBy($"count" desc).limit(5)

val topLessFrequentPattern =
 df.withColumn("freq", lit("bottom"))
.select($"matchedPattern", $"patternName", $"freq")
.groupBy($"patternName", $"matchedPattern").count().orderBy($"count" asc).limit(20)

topMostFrequentPatterns.write.partitionBy("patternName", "freq").mode("append").csv("stats-csv")
topLessFrequentPattern.write.partitionBy("patternName", "freq").mode("append").csv("stats-csv")

println("Finished in " + (System.currentTimeMillis() - start) / (60 * 1000.0)  + " min")

//Loads result in cache
//df.cache()

//Function to define a way to print dataframe results
//def printSample(_df: org.apache.spark.sql.DataFrame) = {_df.take(20).foreach(l => prt("\t\t" + l.mkString("\t")))}

//Print stats per keywords
/*patterns.foreach{ case (key: String, value: scala.util.matching.Regex) => {

    val entity = df.filter($"entity" === key)
    val cnt = entity.count()
    
    prt("Found " + cnt + " matches for " + key + " patterns :" + value)
     
    if(cnt > 0) {
  
        prt("\n\tShow top 20 journals for " + key + " : (journal, pattern, count)")
        printSample(entity.groupBy($"journal", $"entity").agg(count("*") as "numOccurances").orderBy($"numOccurances" desc))
        
        prt("\n\tShow top 20 journal where distinct words are counted " + key + " : (journal, pattern, distinct words count)")
        printSample(entity.groupBy($"journal", $"entity").agg(countDistinct("word") as "numOccurances").orderBy($"numOccurances" desc))
        
        prt("\n\tShow top 20 words for " + key + " : (word, pattern, count)")
        printSample(entity.groupBy($"word", $"entity").agg(count("*") as "numOccurances").orderBy($"numOccurances" desc))
      
        prt("\n\tShow sample for " + key + " : (word, journal, publication, pattern, count, line, offset, length)")
        printSample(entity)
  
    }
    
    prt("\n * * * * * * * * * * * * * * \n\n")
    
  }

}
prt("Finished in " + (System.currentTimeMillis() - start) / (60 * 1000.0)  + " min")
*/