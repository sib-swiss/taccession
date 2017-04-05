# Taccession 

**Note:** This project is under **active development**.

Text Mining of SIB accessions in the literature. 

## Execution

```shell
ssh goldorak
cd /data/user/taccession

export SPARK_HOME=/data/user/tools/spark
export TACCESSION_CMD="$SPARK_HOME/bin/spark-shell --executor-memory 100g --driver-memory 100g --jars target/scala-2.11/taccession_2.11-1.0.jar"
export VARIANT_CONFIG="--conf spark.driver.extraJavaOptions=\"-Dconfig.file=config-variants.properties\""
export ACCESSION_CONFIG="--conf spark.driver.extraJavaOptions=\"-Dconfig.file=config-accession.properties\""

#Generating stats
# For accession patterns
$TACCESSION_CMD -i script/taccession-save-stats.scala $ACCESSION_CONFIG
# For variant patterns
$TACCESSION_CMD -i script/taccession-save-stats.scala $VARIANT_CONFIG
 
# For saving results for accessions
$TACCESSION_CMD -i script/taccession-save-json.scala $ACCESSION_CONFIG

$TACCESSION_CMD -i script/taccession-save-json.scala $VARIANT_CONFIG
```

## Installation

* Java 8
* [Spark](http://spark.apache.org/downloads.html). Results.tsv were created with this bundle: [spark-2.1.0-bin-hadoop2.7](http://d3kbcqa49mib13.cloudfront.net/spark-2.1.0-bin-hadoop2.7.tgz)
* [sbt](http://www.scala-sbt.org/)

In order to build the project, you need sbt. (http://www.scala-sbt.org/)
The command to execute:

```shell
 sbt package #SBT bin should be in the path. Add in your bash_profile the following: PATH=$PATH:/data/user/tools/sbt/bin
 ```

## Configuration file
The config files contain several attributes: 

* PUBLI_DIR - The directory where one can find the publications
* MIN_PARTITIONS - The minimal number of partitions to distribute the workers
* PATTERN_FILE - The name of the file


## Troubleshooting

```shell
rm derby.log
rm -r metastore_db/ 
```

## Run on a cluster

Simply add the master option followed by its url at the end of the TACCESSION_CMD command: 
```
--master spark://goldorak:7077 
``
