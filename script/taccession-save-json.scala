import swiss.sib.taccession._

//Starts the time
val start = System.currentTimeMillis();

//Init configs
val config = TaccessionConfig.init(System.getProperty("config.file"))

//File containing all path of the files to parse (a trick for spark going faster)
val filePaths = TaccessionConfig.getFilePaths(config);

//Reads paths 
val filesRDD = sc.textFile(filePaths, TaccessionConfig.getMinPartitions(config))

//Patterns
val patterns = TaccessionConfig.getPatterns(config);

//Reads all files (this is distributed among all workers)
val df = filesRDD.flatMap(f => Taccession.searchTokens(patterns, f)).toDF()

val outputFolder = TaccessionConfig.getOutputFolder(config);

//Save results in json format
df.write.json(outputFolder + "/json-output")

println("Finished in " + (System.currentTimeMillis() - start) / (60 * 1000.0)  + " min")
