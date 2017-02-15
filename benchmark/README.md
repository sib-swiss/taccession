#This benchmark was run against a sample of 71K publications taken from PubMed (ftp://ftp.ncbi.nlm.nih.gov/pub/pmc)

The job was lanched as follows:

```shell
SPARK_HOME=
$SPARK_HOME/bin/spark-shell --master local[4] -i taccession.scala
$SPARK_HOME/bin/spark-shell --master local[16] -i taccession.scala
$SPARK_HOME/bin/spark-shell --master local[32] -i taccession.scala

$SPARK_HOME/bin/spark-shell --master spark:$cluster_master_200:7070 -i taccession.scala
```

See spreadsheet for results: [Spreadsheet](https://docs.google.com/spreadsheets/d/1Z_7flxM5si8DfLZTMH8ruwFiU5ONX5He062SGxgsla8/edit?usp=sharing)

