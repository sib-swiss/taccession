#Taccession (Under active development)

Mining SIB accessions in the literature. 

#Installation
* Java 8
* [Spark](http://spark.apache.org/downloads.html) [spark-2.1.0-bin-hadoop2.7](http://d3kbcqa49mib13.cloudfront.net/spark-2.1.0-bin-hadoop2.7.tgz)

#Run
```shell
$SPARK_HOME/bin/spark-shell --master local[32] -i taccession.scala
```

#Run on a cluster (tried with 200 cores so far)
$SPARK_HOME/bin/spark-shell --master spark://$master_hostname:7070 -i taccession.scala

#Results
Report is generated in console and in results.tsv