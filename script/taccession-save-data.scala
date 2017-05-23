import swiss.sib.taccession._

//Starts the time
val start = System.currentTimeMillis();

//Init configs
val config = TaccessionUtils.readConfigFile(System.getProperty("config.file"))

val filePaths = TaccessionUtils.getFilePaths(config)

//Reads paths 
val filesRDD = sc.textFile(filePaths, config.sparkPartitions)

//Reads all files (this is distributed among all workers)
val df = filesRDD.flatMap(f => Taccession.searchTokens(config.patterns, f)).toDF()

//Save results in json format
df.select("matchedPattern", "matchedPatternLength", "lineNumber", "columnNumber", "publicationName", "patternName").write.json(config.dataOutputFolder.getPath + "/json-output")

println("Finished in " + (System.currentTimeMillis() - start) / (60 * 1000.0)  + " min")