#!/bin/bash

#BSUB -L /bin/bash
#BSUB -u dteixeir@sib.swiss
#BSUB -N
#BSUB -J sparkmaster
#BSUB -o ./logs/spark-master-%J-output.log
#BSUB -e ./logs/spark-master-%J-error.log

hostname > /scratch/cluster/monthly/$USER/master_hostname

spark_release=spark-2.2.0-bin-hadoop2.7

module add Development/java_jdk/1.8.0_121
mkdir -p /scratch/local/daily/$USER/$spark_release
rm -r /scratch/local/daily/$USER/$spark_release/*

#Must download SPARK beforehand and unzipped it into /scratch/cluster/monthly/spark
cp -r /scratch/cluster/monthly/spark/$spark_release /scratch/local/daily/$USER/
cd /scratch/local/daily/$USER/$spark_release

rm -r /scratch/local/daily/$USER/spark-notebook
rsync -ra /scratch/cluster/monthly/$USER/spark-notebook /scratch/local/daily/$USER/
./sbin/start-master.sh
cd ../spark-notebook/
./bin/spark-notebook
#./bin/spark-class org.apache.spark.deploy.master.Master
module rm Development/java_jdk/1.8.0_112
