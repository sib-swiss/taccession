#!/bin/bash

#BSUB -L /bin/bash
#BSUB -J spark-job
#BSUB -u dteixeir@sib.swiss
#BSUB -N
#BSUB -o ./logs/spark-job-%J-output.log
#BSUB -e ./logs/spark-job-%J-error.log


master_hostname=$(cat /scratch/cluster/monthly/$USER/master_hostname)
spark_release=spark-2.1.0-bin-hadoop2.7

module add Development/java_jdk/1.8.0_112
mkdir -p /scratch/local/daily/$USER/$spark_release
rm -r /scratch/local/daily/$USER/$spark_release/*
cp -r /scratch/cluster/monthly/$USER/$spark_release /scratch/local/daily/$USER/
cd /scratch/local/daily/$USER/$spark_release
./bin/spark-submit --verbose --class swiss.sib.taccession.SIBPattern --master spark://$master_hostname:7077 /scratch/cluster/monthly/$USER/tada.jar
module rm Development/java_jdk/1.8.0_112
