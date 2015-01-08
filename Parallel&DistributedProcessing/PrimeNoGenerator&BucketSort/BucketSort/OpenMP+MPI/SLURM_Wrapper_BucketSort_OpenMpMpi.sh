#!/bin/bash
if [ $# -ne 5 ]; then
	echo "Enter five numbers indicating number of nodes and cores per node.\nE.g. ./SLURM_Wrapper_BucketSort_OpenMpMpi.sh 1 2 2 10 2\n This will in turn run the actual SLURM script with -nodes values from 1 to 2 and vary -cpus-per-task values from 2 to 10 for each node with step size of 2 i.e. 2, 4, 6, 8, 10 respectively\n"
else
	a=$1
	b=$2
	c=$3
	d=$4
	tmp=$3
	stepsize=$5
	while [ $a -le $b ]
	do
		while [ $c -le $d ]
		do
			echo '#!/bin/bash' > SLURM_OpenMpMpi_BucketSort.sh
			echo >> SLURM_OpenMpMpi_BucketSort.sh
			echo '#SBATCH --partition=general-compute' >> SLURM_OpenMpMpi_BucketSort.sh
			echo "#SBATCH --nodes=$a" >> SLURM_OpenMpMpi_BucketSort.sh
			echo '#SBATCH --ntasks-per-node=1' >> SLURM_OpenMpMpi_BucketSort.sh
			echo "#SBATCH --cpus-per-task=$c" >> SLURM_OpenMpMpi_BucketSort.sh
			echo '#SBATCH --job-name=BucketSortOpenMpMpi' >> SLURM_OpenMpMpi_BucketSort.sh
			echo '#SBATCH --mail-user=srao2@buffalo.edu' >> SLURM_OpenMpMpi_BucketSort.sh
			echo '#SBATCH --output=Result_BucketSortOpenMpMpi.out' >> SLURM_OpenMpMpi_BucketSort.sh
			echo '#SBATCH --error=Result_BucketSortOpenMpMpi.out' >> SLURM_OpenMpMpi_BucketSort.sh
			echo '' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'echo "SLURM Environment Variables:"' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'echo "Job ID = "$SLURM_JOB_ID' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'echo "Job Name = "$SLURM_JOB_NAME' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'echo "Job Node List = "$SLURM_JOB_NODELIST' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'echo "Number of Nodes = "$SLURM_NNODES' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'echo "Tasks per Nodes = "$SLURM_NTASKS_PER_NODE' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'echo "CPUs per task = "$SLURM_CPUS_PER_TASK' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'echo "/scratch/jobid = "$SLURMTMPDIR' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'echo "submit Host = "$SLURM_SUBMIT_HOST' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'echo "Submit Directory = "$SLURM_SUBMIT_DIR' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'echo ' >> SLURM_OpenMpMpi_BucketSort.sh
			echo '' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'ulimit -s unlimited' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'module load intel' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'module load intel-mpi' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'module list' >> SLURM_OpenMpMpi_BucketSort.sh
			echo '' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'export OMP_NUM_THREADS=$SLURM_CPUS_PER_TASK' >> SLURM_OpenMpMpi_BucketSort.sh
			echo '#' >> SLURM_OpenMpMpi_BucketSort.sh
			echo '' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'export I_MPI_PMI_LIBRARY=/usr/lib64/libpmi.so' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'srun ./BucketSortOpenMpMpi -t 100000 $SLURM_NNODES $SLURM_CPUS_PER_TASK' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'srun ./BucketSortOpenMpMpi -t 1000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'srun ./BucketSortOpenMpMpi -t 10000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'srun ./BucketSortOpenMpMpi -t 30000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'srun ./BucketSortOpenMpMpi -t 50000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'srun ./BucketSortOpenMpMpi -t 80000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'srun ./BucketSortOpenMpMpi -t 100000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'srun ./BucketSortOpenMpMpi -t 130000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'srun ./BucketSortOpenMpMpi -t 150000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'srun ./BucketSortOpenMpMpi -t 180000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'srun ./BucketSortOpenMpMpi -t 200000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'srun ./BucketSortOpenMpMpi -t 220000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'srun ./BucketSortOpenMpMpi -t 250000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'srun ./BucketSortOpenMpMpi -t 300000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'srun ./BucketSortOpenMpMpi -t 350000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK' >> SLURM_OpenMpMpi_BucketSort.sh
			echo '#' >> SLURM_OpenMpMpi_BucketSort.sh
			echo 'echo "All Done!"' >> SLURM_OpenMpMpi_BucketSort.sh
			chmod 777 SLURM_OpenMpMpi_BucketSort.sh
			sbatch SLURM_OpenMpMpi_BucketSort.sh
			rm -rf SLURM_OpenMpMpi_BucketSort.sh
			c=`expr $c + $stepsize`
		done
		a=`expr $a + 1`
		c=$tmp
	done
fi
