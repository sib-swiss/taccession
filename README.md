# Taccession 

**Note:** This project is under **active development**.

Text Mining of SIB accessions in the literature. 

## Internal procedure

```shell
 ssh goldorak
 cd /data/user/
 cd taccession
 sbt package
 # For saving stats
 $SPARK_HOME/bin/spark-shell --jars target/scala-2.11/taccession_2.11-1.0.jar -i script/taccession-save-stats.scala

 # For saving results
 $SPARK_HOME/bin/spark-shell --jars target/scala-2.11/taccession_2.11-1.0.jar -i script/taccession-save-json.scala

```

## Troubleshooting

```
rm derby.log
rm -r metastore_db/ 
```

## Installation

* Java 8
* [Spark](http://spark.apache.org/downloads.html). Results.tsv were created with this bundle: [spark-2.1.0-bin-hadoop2.7](http://d3kbcqa49mib13.cloudfront.net/spark-2.1.0-bin-hadoop2.7.tgz)
* [sbt](http://www.scala-sbt.org/)

## Configuration file
Publications

* PUBLI_DIR Make sure you have the 70K publications defined in file_names.txt under PUBLI_DIR
* MIN_PAT ...

## Run on a cluster
```shell
$SPARK_HOME/bin/spark-shell --master local[32] -i taccession.scala
spark-shell --executor-memory 100g --driver-memory 100g --master spark://goldorak:7077 -i taccession-persist-json.scala
$SPARK_HOME/bin/spark-shell --master spark://$master_hostname:7070 -i taccession.scala
```