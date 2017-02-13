#!/bin/bash

#BSUB -L /bin/bash
#BSUB -J spark-worker[1-15]
#BSUB -n 4
#BSUB -R "rusage[mem=5120] span[ptile=4]"
#BSUB -M 52444160
#BSUB -o ./logs/spark-workers-%J-output.log
#BSUB -e ./logs/spark-workers-%J-error.log


master_hostname=$(cat /scratch/cluster/monthly/$USER/master_hostname)
spark_release=spark-2.1.0-bin-hadoop2.7

module add Development/java_jdk/1.8.0_112
mkdir -p /scratch/local/daily/$USER/$spark_release
rm -r /scratch/local/daily/$USER/$spark_release/*
cp -r /scratch/cluster/monthly/$USER/$spark_release /scratch/local/daily/$USER/
cd /scratch/local/daily/$USER/$spark_release
./bin/spark-class org.apache.spark.deploy.worker.Worker -c 4 -m 4G spark://$master_hostname:7077
module rm Development/java_jdk/1.8.0_112
