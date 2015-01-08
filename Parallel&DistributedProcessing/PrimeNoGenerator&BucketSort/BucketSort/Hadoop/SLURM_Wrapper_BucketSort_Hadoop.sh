#!/bin/bash
if [ $# -ne 2 ]; then
	echo "Enter two numbers indicating range of number of nodes.\nE.g. ./SLURM_Wrapper_BucketSort_Hadoop.sh 5 10\n This will in turn run the actual SLURM script with -ntasks-per-node values from 5 to 10\n"
else
	a=$1
	b=$2
	while [ $a -le $b ]
	do
		echo '#!/bin/bash' >> SLURM_BucketSortHadoop.sh
		echo '#SBATCH --partition=general-compute' >> SLURM_BucketSortHadoop.sh
		echo "#SBATCH --nodes=$a" >> SLURM_BucketSortHadoop.sh
		echo '#SBATCH --ntasks-per-node=1' >> SLURM_BucketSortHadoop.sh
		echo '#SBATCH --exclusive' >> SLURM_BucketSortHadoop.sh
		echo '#SBATCH --output=Output_BucketSortHadoop.out' >> SLURM_BucketSortHadoop.sh

		echo 'module load myhadoop/0.30b/hadoop-2.5.1' >> SLURM_BucketSortHadoop.sh
		echo 'export MH_SCRATCH_DIR=$SLURMTMPDIR' >> SLURM_BucketSortHadoop.sh
		echo 'export HADOOP_CONF_DIR=$SLURM_SUBMIT_DIR/config-$SLURM_JOBID' >> SLURM_BucketSortHadoop.sh

		echo 'probSizeArr=(100000 1000000 2000000 3000000 4000000 5000000 6000000 7000000 8000000 9000000 10000000 11000000 12000000 13000000 14000000 100000000 200000000)' >> SLURM_BucketSortHadoop.sh
		echo 'for i in "${probSizeArr[@]}"' >> SLURM_BucketSortHadoop.sh
		echo 'do' >> SLURM_BucketSortHadoop.sh
		echo '$MH_HOME/bin/myhadoop-configure.sh' >> SLURM_BucketSortHadoop.sh
		echo '$HADOOP_HOME/sbin/start-all.sh' >> SLURM_BucketSortHadoop.sh
		echo '$HADOOP_HOME/bin/hadoop dfsadmin -report' >> SLURM_BucketSortHadoop.sh
		echo '$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR jar BucketSort.jar sort.Driver -t $i $SLURM_NNODES' >> SLURM_BucketSortHadoop.sh
		echo '$HADOOP_HOME/sbin/stop-all.sh' >> SLURM_BucketSortHadoop.sh
		echo '$MH_HOME/bin/myhadoop-cleanup.sh' >> SLURM_BucketSortHadoop.sh
		echo 'done' >> SLURM_BucketSortHadoop.sh
		echo 'rm -rf config-$SLURM_JOBID*' >> SLURM_BucketSortHadoop.sh

		chmod 777 SLURM_BucketSortHadoop.sh
		sbatch SLURM_BucketSortHadoop.sh
		rm -rf SLURM_BucketSortHadoop.sh
		a=`expr $a + 1`
	done
fi
