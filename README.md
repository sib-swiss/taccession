# Taccession 

**Note:** This project is under **active development**.

Text Mining of SIB accessions in the literature. 

## Internal procedure

```shell
 ssh goldorak
 cd /data/user/taccession
 
 sbt package #SBT bin should be in the path. Add in your bash_profile the following: PATH=$PATH:/data/user/tools/sbt/bin
 
 # For saving stats
 $SPARK_HOME/bin/spark-shell --executor-memory 100g --driver-memory 100g --jars target/scala-2.11/taccession_2.11-1.0.jar -i script/taccession-save-stats.scala
 
 # For saving results
 $SPARK_HOME/bin/spark-shell --jars target/scala-2.11/taccession_2.11-1.0.jar -i script/taccession-save-json.scala

```

## Installation

* Java 8
* [Spark](http://spark.apache.org/downloads.html). Results.tsv were created with this bundle: [spark-2.1.0-bin-hadoop2.7](http://d3kbcqa49mib13.cloudfront.net/spark-2.1.0-bin-hadoop2.7.tgz)
* [sbt](http://www.scala-sbt.org/)


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
```shell
$SPARK_HOME/bin/spark-shell --master local[32] -i taccession.scala
spark-shell --executor-memory 100g --driver-memory 100g --master spark://goldorak:7077 -i taccession-persist-json.scala
$SPARK_HOME/bin/spark-shell --master spark://$master_hostname:7070 -i taccession.scala
```
