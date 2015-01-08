#!/bin/bash
if [ $# -ne 5 ]; then
	echo "Enter five numbers indicating range of nodes and cores per node.\nE.g. ./SLURM_Wrapper_BucketSort_Mpi.sh 1 2 2 10 2\n This will in turn run the actual SLURM script with -nodes values from 1 to 2 and ntasks-per-node 2 to 10 with step size of 2 cores per node i.e. 2, 4, 6, 8, 10 respectively\n"
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
			echo '#!/bin/bash' > SLURM_Mpi_BucketSort.sh
			echo >> SLURM_Mpi_BucketSort.sh
			echo '#SBATCH --partition=general-compute' >> SLURM_Mpi_BucketSort.sh
			echo "#SBATCH --nodes=$a" >> SLURM_Mpi_BucketSort.sh
			echo "#SBATCH --ntasks-per-node=$c" >> SLURM_Mpi_BucketSort.sh
			echo '#SBATCH --job-name=BucketSortMpi' >> SLURM_Mpi_BucketSort.sh
			echo '#SBATCH --mail-user=srao2@buffalo.edu' >> SLURM_Mpi_BucketSort.sh
			echo '#SBATCH --output=Result_BucketSortMpi.out' >> SLURM_Mpi_BucketSort.sh
			echo '#SBATCH --error=Result_BucketSortMpi.out' >> SLURM_Mpi_BucketSort.sh
			echo '' >> SLURM_Mpi_BucketSort.sh
			echo 'echo "SLURM Environment Variables:"' >> SLURM_Mpi_BucketSort.sh
			echo 'echo "Job ID = "$SLURM_JOB_ID' >> SLURM_Mpi_BucketSort.sh
			echo 'echo "Job Name = "$SLURM_JOB_NAME' >> SLURM_Mpi_BucketSort.sh
			echo 'echo "Job Node List = "$SLURM_JOB_NODELIST' >> SLURM_Mpi_BucketSort.sh
			echo 'echo "Number of Nodes = "$SLURM_NNODES' >> SLURM_Mpi_BucketSort.sh
			echo 'echo "Tasks per Nodes = "$SLURM_NTASKS_PER_NODE' >> SLURM_Mpi_BucketSort.sh
			echo 'echo "CPUs per task = "$SLURM_CPUS_PER_TASK' >> SLURM_Mpi_BucketSort.sh
			echo 'echo "/scratch/jobid = "$SLURMTMPDIR' >> SLURM_Mpi_BucketSort.sh
			echo 'echo "submit Host = "$SLURM_SUBMIT_HOST' >> SLURM_Mpi_BucketSort.sh
			echo 'echo "Submit Directory = "$SLURM_SUBMIT_DIR' >> SLURM_Mpi_BucketSort.sh
			echo 'echo ' >> SLURM_Mpi_BucketSort.sh
			echo '' >> SLURM_Mpi_BucketSort.sh
			echo 'module load intel' >> SLURM_Mpi_BucketSort.sh
			echo 'module load intel-mpi' >> SLURM_Mpi_BucketSort.sh
			echo 'module list' >> SLURM_Mpi_BucketSort.sh
			echo 'ulimit -s unlimited' >> SLURM_Mpi_BucketSort.sh
			echo '' >> SLURM_Mpi_BucketSort.sh
			echo '#' >> SLURM_Mpi_BucketSort.sh
			echo 'export I_MPI_PMI_LIBRARY=/usr/lib64/libpmi.so'  >> SLURM_Mpi_BucketSort.sh
			echo '' >> SLURM_Mpi_BucketSort.sh
			echo 'srun ./BucketSortMpi -t 100000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE' >> SLURM_Mpi_BucketSort.sh
			echo 'srun ./BucketSortMpi -t 1000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE' >> SLURM_Mpi_BucketSort.sh
			echo 'srun ./BucketSortMpi -t 2000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE' >> SLURM_Mpi_BucketSort.sh
			echo 'srun ./BucketSortMpi -t 3000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE' >> SLURM_Mpi_BucketSort.sh
			echo 'srun ./BucketSortMpi -t 4000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE' >> SLURM_Mpi_BucketSort.sh
			echo 'srun ./BucketSortMpi -t 5000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE' >> SLURM_Mpi_BucketSort.sh
			echo 'srun ./BucketSortMpi -t 6000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE' >> SLURM_Mpi_BucketSort.sh
			echo 'srun ./BucketSortMpi -t 7000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE' >> SLURM_Mpi_BucketSort.sh
			echo 'srun ./BucketSortMpi -t 8000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE' >> SLURM_Mpi_BucketSort.sh
			echo 'srun ./BucketSortMpi -t 9000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE' >> SLURM_Mpi_BucketSort.sh
			echo 'srun ./BucketSortMpi -t 10000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE' >> SLURM_Mpi_BucketSort.sh
			echo 'srun ./BucketSortMpi -t 11000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE' >> SLURM_Mpi_BucketSort.sh
			echo 'srun ./BucketSortMpi -t 12000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE' >> SLURM_Mpi_BucketSort.sh
			echo 'srun ./BucketSortMpi -t 13000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE' >> SLURM_Mpi_BucketSort.sh
			echo 'srun ./BucketSortMpi -t 14000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE#' >> SLURM_Mpi_BucketSort.sh
			echo 'echo "All Done!"' >> SLURM_Mpi_BucketSort.sh
			chmod 777 SLURM_Mpi_BucketSort.sh
			sbatch SLURM_Mpi_BucketSort.sh
			rm -rf SLURM_Mpi_BucketSort.sh
			c=`expr $c + $stepsize`
		done
		a=`expr $a + 1`
		c=$tmp
	done
fi
