#!/bin/bash
#SBATCH --partition=debug
#SBATCH --nodes=3
#SBATCH --ntasks-per-node=1
#SBATCH --exclusive
#SBATCH --output=Output_BucketSortHadoop.out

module load myhadoop/0.30b/hadoop-2.5.1
export MH_SCRATCH_DIR=$SLURMTMPDIR
export HADOOP_CONF_DIR=$SLURM_SUBMIT_DIR/config-$SLURM_JOBID

probSizeArr=(100000 1000000 2000000 3000000 4000000 5000000 6000000 7000000 8000000 9000000 10000000 11000000 12000000 13000000 14000000 100000000 200000000)
for i in "${probSizeArr[@]}"
do
	$MH_HOME/bin/myhadoop-configure.sh
	$HADOOP_HOME/sbin/start-all.sh
	$HADOOP_HOME/bin/hadoop dfsadmin -report
	$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR jar BucketSort.jar sort.Driver -t $i $SLURM_NNODES
	$HADOOP_HOME/sbin/stop-all.sh
	$MH_HOME/bin/myhadoop-cleanup.sh
done
rm -rf config-$SLURM_JOBID*
