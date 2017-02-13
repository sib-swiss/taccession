#Taccession (Under active development)

Mining SIB accessions in the literature. 

#Run

To run in LSF, log in LSF and lauch master and worker tasks
master_hostname=$(cat /scratch/cluster/monthly/$USER/master_hostname)
./bin/spark-shell --master spark://$master_hostname:7077


#DOING

Optimising Spark algorithm

#TODO
