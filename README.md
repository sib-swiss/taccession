# Taccession 

**Note:** The TAccession project is partially funded by the Business, Management and Services field of the HES-SO. 

Text Mining of SIB accessions in the literature. 
## Build
```
sbt package #This creates the following jar file: target/scala-2.11/taccession_2.11-1.0.jar
```

## Execution

```shell
ssh goldorak
cd /data/user/taccession

export SPARK_HOME=/data/user/tools/spark
export TACCESSION_CMD="$SPARK_HOME/bin/spark-shell --executor-memory 100g --driver-memory 100g --jars lib/moultingyaml_2.11-0.4.0.jar,lib/snakeyaml-1.18.jar,target/scala-2.11/taccession_2.11-1.0.jar"
export ACCESSION_CONFIG="--conf spark.driver.extraJavaOptions=\"-Dconfig.file=accession-config.yaml\""
export VARIANT_CONFIG="--conf spark.driver.extraJavaOptions=\"-Dconfig.file=variants-config.yaml\""

./clear-stats-accessions.sh #This will remove and git remove the previous stats directory.
./clear-stats-variants.sh #This will remove and git remove the previous stats directory.

#Generating stats
# For accession patterns
$TACCESSION_CMD -i script/taccession-save-stats.scala $ACCESSION_CONFIG
# For variant patterns
$TACCESSION_CMD -i script/taccession-save-stats.scala $VARIANT_CONFIG
 
# For saving results for accessions
$TACCESSION_CMD -i script/taccession-save-data.scala $ACCESSION_CONFIG

$TACCESSION_CMD -i script/taccession-save-data.scala $VARIANT_CONFIG
```


## Tests
```
sbt test
```

## Troubleshooting
If you find this error: Failed to start database 'metastore_db'
It might be because there is another instance runnning. Kill the process and remove the folder metastore_db and the file derby.log


## Installation

* Java 8
* [Spark](http://spark.apache.org/downloads.html). (Tested with: [spark-2.1.0-bin-hadoop2.7](http://d3kbcqa49mib13.cloudfront.net/spark-2.1.0-bin-hadoop2.7.tgz)
* [sbt](http://www.scala-sbt.org/) Add to your PATH sbt/bin


## Run on a cluster

Simply add the master option followed by its url at the end of the TACCESSION_CMD command: 
```
--master spark://goldorak:7077 
```
