#!/bin/bash
if [ $# -ne 2 ]; then
	echo "Enter two numbers indicating number of cores per node.\nE.g. ./SLURM_Wrapper_BucketSort_OpenMp.sh 2 11\n This will in turn run the actual SLURM script with -ntasks-per-node values from 2 to 11\n"
else
	a=$1
	b=$2
	while [ $a -le $b ]
	do
		echo '#!/bin/bash' > SLURM_OpenMp_BucketSort.sh
		echo >> SLURM_OpenMp_BucketSort.sh
		echo '#SBATCH --partition=debug' >> SLURM_OpenMp_BucketSort.sh
		echo '#SBATCH --nodes=1' >> SLURM_OpenMp_BucketSort.sh
		echo "#SBATCH --ntasks-per-node=$a" >> SLURM_OpenMp_BucketSort.sh
		echo '#SBATCH --job-name=BucketSortOpenMp' >> SLURM_OpenMp_BucketSort.sh
		echo '#SBATCH --mail-user=srao2@buffalo.edu' >> SLURM_OpenMp_BucketSort.sh
		echo '#SBATCH --output=Result_BucketSortOpenMp.out' >> SLURM_OpenMp_BucketSort.sh
		echo '#SBATCH --error=Result_BucketSortOpenMp.out' >> SLURM_OpenMp_BucketSort.sh
		echo '' >> SLURM_OpenMp_BucketSort.sh
		echo 'echo "SLURM Environment Variables:"' >> SLURM_OpenMp_BucketSort.sh
		echo 'echo "Job ID = "$SLURM_JOB_ID' >> SLURM_OpenMp_BucketSort.sh
		echo 'echo "Job Name = "$SLURM_JOB_NAME' >> SLURM_OpenMp_BucketSort.sh
		echo 'echo "Job Node List = "$SLURM_JOB_NODELIST' >> SLURM_OpenMp_BucketSort.sh
		echo 'echo "Number of Nodes = "$SLURM_NNODES' >> SLURM_OpenMp_BucketSort.sh
		echo 'echo "Tasks per Nodes = "$SLURM_NTASKS_PER_NODE' >> SLURM_OpenMp_BucketSort.sh
		echo 'echo "CPUs per task = "$SLURM_CPUS_PER_TASK' >> SLURM_OpenMp_BucketSort.sh
		echo 'echo "/scratch/jobid = "$SLURMTMPDIR' >> SLURM_OpenMp_BucketSort.sh
		echo 'echo "submit Host = "$SLURM_SUBMIT_HOST' >> SLURM_OpenMp_BucketSort.sh
		echo 'echo "Submit Directory = "$SLURM_SUBMIT_DIR' >> SLURM_OpenMp_BucketSort.sh
		echo 'echo ' >> SLURM_OpenMp_BucketSort.sh
		echo '' >> SLURM_OpenMp_BucketSort.sh
		echo 'ulimit -s unlimited' >> SLURM_OpenMp_BucketSort.sh
		echo 'module load intel' >> SLURM_OpenMp_BucketSort.sh
		echo 'module list' >> SLURM_OpenMp_BucketSort.sh
		echo '' >> SLURM_OpenMp_BucketSort.sh
		echo 'NPROCS=`srun --nodes=${SLURM_NNODES} bash -c '\'hostname\'' | wc -l`' >> SLURM_OpenMp_BucketSort.sh
		echo 'echo "NPROCS="$NPROCS' >> SLURM_OpenMp_BucketSort.sh
		echo '' >> SLURM_OpenMp_BucketSort.sh
		echo 'export OMP_NUM_THREADS=$NPROCS' >> SLURM_OpenMp_BucketSort.sh
		echo 'export | grep OMP' >> SLURM_OpenMp_BucketSort.sh
		echo '#' >> SLURM_OpenMp_BucketSort.sh
		echo './BucketSortOpenMp -t 100000 $NPROCS' >> SLURM_OpenMp_BucketSort.sh
		echo './BucketSortOpenMp -t 1000000 $NPROCS' >> SLURM_OpenMp_BucketSort.sh
		echo './BucketSortOpenMp -t 2000000 $NPROCS' >> SLURM_OpenMp_BucketSort.sh
		echo './BucketSortOpenMp -t 3000000 $NPROCS' >> SLURM_OpenMp_BucketSort.sh
		echo './BucketSortOpenMp -t 4000000 $NPROCS' >> SLURM_OpenMp_BucketSort.sh
		echo './BucketSortOpenMp -t 5000000 $NPROCS' >> SLURM_OpenMp_BucketSort.sh
		echo './BucketSortOpenMp -t 6000000 $NPROCS' >> SLURM_OpenMp_BucketSort.sh
		echo './BucketSortOpenMp -t 7000000 $NPROCS' >> SLURM_OpenMp_BucketSort.sh
		echo './BucketSortOpenMp -t 8000000 $NPROCS' >> SLURM_OpenMp_BucketSort.sh
		echo './BucketSortOpenMp -t 9000000 $NPROCS' >> SLURM_OpenMp_BucketSort.sh
		echo './BucketSortOpenMp -t 10000000 $NPROCS' >> SLURM_OpenMp_BucketSort.sh
		echo './BucketSortOpenMp -t 11000000 $NPROCS' >> SLURM_OpenMp_BucketSort.sh
		echo './BucketSortOpenMp -t 12000000 $NPROCS' >> SLURM_OpenMp_BucketSort.sh
		echo './BucketSortOpenMp -t 13000000 $NPROCS' >> SLURM_OpenMp_BucketSort.sh
		echo './BucketSortOpenMp -t 14000000 $NPROCS' >> SLURM_OpenMp_BucketSort.sh
		echo '#' >> SLURM_OpenMp_BucketSort.sh
		echo 'echo "All Done!"' >> SLURM_OpenMp_BucketSort.sh
		chmod 777 SLURM_OpenMp_BucketSort.sh
		sbatch SLURM_OpenMp_BucketSort.sh
		rm -rf SLURM_OpenMp_BucketSort.sh
		a=`expr $a + 1`
	done
fi
