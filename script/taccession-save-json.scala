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

//Save results in json format
df.write.json("result-json")

println("Finished in " + (System.currentTimeMillis() - start) / (60 * 1000.0)  + " min")