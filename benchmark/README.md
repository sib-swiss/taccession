#Spark investigation / benchmark

This benchmark was run against a sample of 71517 publications (approx 20GB) taken from [PubMed](ftp://ftp.ncbi.nlm.nih.gov/pub/pmc). The publication names can be found in file_names.txt file.

See spreadsheet for detailed results: [Spreadsheet](https://docs.google.com/spreadsheets/d/1Z_7flxM5si8DfLZTMH8ruwFiU5ONX5He062SGxgsla8/edit?usp=sharing)

## Results
The best result we achieved with 200 cores was 61 seconds (1.02381 minutes) to process this task.
This corresponds to process 1164 files / second.

This result is 11 times faster than running in local mode with 4 cores, where we reach 11 minutes for the same task. 
Still, when processing in local mode, we see that each file is processed in 38 ms. 
In cluster mode each file takes in average 188ms, therefore we surely add a non-negligeable network overhead.

## Distributed files vs local files
All the tests were run with files on DFS .
We have tested the overhead of having files in the network in T12, which reduced from 6.8min to 5.6min the processing time.

## DAG

## Glossary

```shell 
[Stage 0:=======>                                             (297 + 32) / 2001]

Means we are at Stage 0. There are a total of 2001 tasks to be done. We have done 297 so far. And we are currently doing 32 (this last number should correspond to the number of cores we have).
```
## Technical details

The job was lanched as follows:

```shell
SPARK_HOME=
$SPARK_HOME/bin/spark-shell --master local[4] -i taccession.scala
$SPARK_HOME/bin/spark-shell --master local[16] -i taccession.scala
$SPARK_HOME/bin/spark-shell --master local[32] -i taccession.scala

$SPARK_HOME/bin/spark-shell --master spark:$cluster_master_200:7070 -i taccession.scala
```
