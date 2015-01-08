#!/bin/bash
if [ $# -ne 2 ]; then
	echo "Enter two numbers indicating range of nodes.\nE.g. ./SLURM_Wrapper_Mpi.sh 5 16\n This will in turn run the actual SLURM script with -nodes values from 5 to 16\n"
else
	a=$1
	b=$2
	while [ $a -le $b ]
	do
		echo '#!/bin/bash' > SLURM_Mpi_PrimeNum.sh
		echo >> SLURM_Mpi_PrimeNum.sh
		echo '#SBATCH --partition=debug' >> SLURM_Mpi_PrimeNum.sh
		echo "#SBATCH --nodes=$a" >> SLURM_Mpi_PrimeNum.sh
		echo "#SBATCH --ntasks-per-node=1" >> SLURM_Mpi_PrimeNum.sh
		echo '#SBATCH --job-name=PrimeNumMpi' >> SLURM_Mpi_PrimeNum.sh
		echo '#SBATCH --mail-user=srao2@buffalo.edu' >> SLURM_Mpi_PrimeNum.sh
		echo '#SBATCH --output=Result_PrimeNumMpi.out' >> SLURM_Mpi_PrimeNum.sh
		echo '#SBATCH --error=Result_PrimeNumMpi.out' >> SLURM_Mpi_PrimeNum.sh
		echo '' >> SLURM_Mpi_PrimeNum.sh
		echo 'echo "SLURM Environment Variables:"' >> SLURM_Mpi_PrimeNum.sh
		echo 'echo "Job ID = "$SLURM_JOB_ID' >> SLURM_Mpi_PrimeNum.sh
		echo 'echo "Job Name = "$SLURM_JOB_NAME' >> SLURM_Mpi_PrimeNum.sh
		echo 'echo "Job Node List = "$SLURM_JOB_NODELIST' >> SLURM_Mpi_PrimeNum.sh
		echo 'echo "Number of Nodes = "$SLURM_NNODES' >> SLURM_Mpi_PrimeNum.sh
		echo 'echo "Tasks per Nodes = "$SLURM_NTASKS_PER_NODE' >> SLURM_Mpi_PrimeNum.sh
		echo 'echo "CPUs per task = "$SLURM_CPUS_PER_TASK' >> SLURM_Mpi_PrimeNum.sh
		echo 'echo "/scratch/jobid = "$SLURMTMPDIR' >> SLURM_Mpi_PrimeNum.sh
		echo 'echo "submit Host = "$SLURM_SUBMIT_HOST' >> SLURM_Mpi_PrimeNum.sh
		echo 'echo "Submit Directory = "$SLURM_SUBMIT_DIR' >> SLURM_Mpi_PrimeNum.sh
		echo 'echo ' >> SLURM_Mpi_PrimeNum.sh
		echo '' >> SLURM_Mpi_PrimeNum.sh
		echo 'module load intel' >> SLURM_Mpi_PrimeNum.sh
		echo 'module load intel-mpi' >> SLURM_Mpi_PrimeNum.sh
		echo 'module list' >> SLURM_Mpi_PrimeNum.sh
		echo 'ulimit -s unlimited' >> SLURM_Mpi_PrimeNum.sh
		echo '' >> SLURM_Mpi_PrimeNum.sh
		echo '#' >> SLURM_Mpi_PrimeNum.sh
		echo 'export I_MPI_PMI_LIBRARY=/usr/lib64/libpmi.so'  >> SLURM_Mpi_PrimeNum.sh
		echo 'srun ./PrimeNumMpi $SLURM_NNODES 100000' >> SLURM_Mpi_PrimeNum.sh
		echo 'srun ./PrimeNumMpi $SLURM_NNODES 1000000' >> SLURM_Mpi_PrimeNum.sh
		echo 'srun ./PrimeNumMpi $SLURM_NNODES 2000000' >> SLURM_Mpi_PrimeNum.sh
		echo 'srun ./PrimeNumMpi $SLURM_NNODES 3000000' >> SLURM_Mpi_PrimeNum.sh
		echo 'srun ./PrimeNumMpi $SLURM_NNODES 4000000' >> SLURM_Mpi_PrimeNum.sh
		echo 'srun ./PrimeNumMpi $SLURM_NNODES 5000000' >> SLURM_Mpi_PrimeNum.sh
		echo 'srun ./PrimeNumMpi $SLURM_NNODES 6000000' >> SLURM_Mpi_PrimeNum.sh
		echo 'srun ./PrimeNumMpi $SLURM_NNODES 7000000' >> SLURM_Mpi_PrimeNum.sh
		echo 'srun ./PrimeNumMpi $SLURM_NNODES 8000000' >> SLURM_Mpi_PrimeNum.sh
		echo 'srun ./PrimeNumMpi $SLURM_NNODES 9000000' >> SLURM_Mpi_PrimeNum.sh
		echo 'srun ./PrimeNumMpi $SLURM_NNODES 10000000' >> SLURM_Mpi_PrimeNum.sh
		echo 'srun ./PrimeNumMpi $SLURM_NNODES 11000000' >> SLURM_Mpi_PrimeNum.sh
		echo 'srun ./PrimeNumMpi $SLURM_NNODES 12000000' >> SLURM_Mpi_PrimeNum.sh
		echo 'srun ./PrimeNumMpi $SLURM_NNODES 13000000' >> SLURM_Mpi_PrimeNum.sh
		echo 'srun ./PrimeNumMpi $SLURM_NNODES 14000000' >> SLURM_Mpi_PrimeNum.sh
		echo '#' >> SLURM_Mpi_PrimeNum.sh
		echo 'echo "All Done!"' >> SLURM_Mpi_PrimeNum.sh
		chmod 777 SLURM_Mpi_PrimeNum.sh
		sbatch SLURM_Mpi_PrimeNum.sh
		rm -rf SLURM_Mpi_PrimeNum.sh
		a=`expr $a + 1`
	done
fi
