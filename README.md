#Taccession (Under active development)

Mining SIB accessions in the literature. 

#Run

To run in LSF, log in LSF and launch master and worker tasks in the LSF folder


Launch the spark shell
```shell
master_hostname=$(cat /scratch/cluster/monthly/$USER/master_hostname)
./bin/spark-shell --master spark://$master_hostname:7077
```


#DOING

Optimising Spark algorithm

Currently the algorithm takes 6min for 70'000 files in a cluster with 60 cores.

The algorithm is divided in 2 stages. Only 2 executors are used, don't know why...



#TODO
