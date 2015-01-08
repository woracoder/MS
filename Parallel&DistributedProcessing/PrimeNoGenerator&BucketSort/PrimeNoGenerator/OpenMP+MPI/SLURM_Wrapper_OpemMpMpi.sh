#!/bin/bash
if [ $# -ne 4 ]; then
	echo "Enter four numbers indicating number of nodes and cores per node.\nE.g. ./SLURM_Wrapper_OpenMpMpi.sh 2 4 5 8\n This will in turn run the actual SLURM script with -nodes values from 2 to 4 and vary -ntasks-per-node values from 5 to 8 for each node\n"
else
	a=$1
	b=$2
	c=$3
	d=$4
	tmp=$3
	while [ $a -le $b ]
	do
		while [ $c -le $d ]
		do
			echo '#!/bin/bash' > SLURM_OpenMpMpi_PrimeNum.sh
			echo >> SLURM_OpenMpMpi_PrimeNum.sh
			echo '#SBATCH --partition=general-compute' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo "#SBATCH --nodes=$a" >> SLURM_OpenMpMpi_PrimeNum.sh
			echo "#SBATCH --ntasks-per-node=$c" >> SLURM_OpenMpMpi_PrimeNum.sh
			echo "#SBATCH -n $a" >> SLURM_OpenMpMpi_PrimeNum.sh
			echo '#SBATCH --job-name=PrimeNumOpenMpMpi' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo '#SBATCH --mail-user=srao2@buffalo.edu' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo '#SBATCH --output=Result_PrimeNumOpenMpMpi.out' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo '#SBATCH --error=Result_PrimeNumOpenMpMpi.out' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo '' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'echo "SLURM Environment Variables:"' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'echo "Job ID = "$SLURM_JOB_ID' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'echo "Job Name = "$SLURM_JOB_NAME' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'echo "Job Node List = "$SLURM_JOB_NODELIST' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'echo "Number of Nodes = "$SLURM_NNODES' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'echo "Tasks per Nodes = "$SLURM_NTASKS_PER_NODE' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'echo "CPUs per task = "$SLURM_CPUS_PER_TASK' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'echo "/scratch/jobid = "$SLURMTMPDIR' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'echo "submit Host = "$SLURM_SUBMIT_HOST' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'echo "Submit Directory = "$SLURM_SUBMIT_DIR' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'echo ' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo '' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'ulimit -s unlimited' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'module load intel' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'module load intel-mpi' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'module list' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo '' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'export OMP_NUM_THREADS=$SLURM_NTASKS_PER_NODE' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo '#' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo '' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'export I_MPI_PMI_LIBRARY=/usr/lib64/libpmi.so' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 10000000' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 20000000' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 30000000' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 40000000' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 50000000' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 60000000' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 70000000' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 80000000' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 90000000' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 100000000' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 110000000' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 120000000' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 130000000' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 140000000' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 150000000' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo '#' >> SLURM_OpenMpMpi_PrimeNum.sh
			echo 'echo "All Done!"' >> SLURM_OpenMpMpi_PrimeNum.sh
			chmod 777 SLURM_OpenMpMpi_PrimeNum.sh
			sbatch SLURM_OpenMpMpi_PrimeNum.sh
			rm -rf SLURM_OpenMpMpi_PrimeNum.sh
			c=`expr $c + 1`
		done
		a=`expr $a + 1`
		c=$tmp
	done
fi
