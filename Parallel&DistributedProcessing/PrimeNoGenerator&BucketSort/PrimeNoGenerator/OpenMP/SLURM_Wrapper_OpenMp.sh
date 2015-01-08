#!/bin/bash
if [ $# -ne 2 ]; then
	echo "Enter two numbers indicating number of cores per node.\nE.g. ./SLURM_Wrapper_OpenMp.sh 5 16\n This will in turn run the actual SLURM script with -ntasks-per-node values from 5 to 16\n"
else
	a=$1
	b=$2
	while [ $a -le $b ]
	do
		echo '#!/bin/bash' > SLURM_OpenMp_PrimeNum.sh
		echo >> SLURM_OpenMp_PrimeNum.sh
		echo '#SBATCH --partition=debug' >> SLURM_OpenMp_PrimeNum.sh
		echo '#SBATCH --nodes=1' >> SLURM_OpenMp_PrimeNum.sh
		echo "#SBATCH --ntasks-per-node=$a" >> SLURM_OpenMp_PrimeNum.sh
		echo '#SBATCH --job-name=PrimeNumOpenMp' >> SLURM_OpenMp_PrimeNum.sh
		echo '#SBATCH --mail-user=srao2@buffalo.edu' >> SLURM_OpenMp_PrimeNum.sh
		echo '#SBATCH --output=Result_PrimeNumOpenMp.out' >> SLURM_OpenMp_PrimeNum.sh
		echo '#SBATCH --error=Result_PrimeNumOpenMp.out' >> SLURM_OpenMp_PrimeNum.sh
		echo '' >> SLURM_OpenMp_PrimeNum.sh
		echo 'echo "SLURM Environment Variables:"' >> SLURM_OpenMp_PrimeNum.sh
		echo 'echo "Job ID = "$SLURM_JOB_ID' >> SLURM_OpenMp_PrimeNum.sh
		echo 'echo "Job Name = "$SLURM_JOB_NAME' >> SLURM_OpenMp_PrimeNum.sh
		echo 'echo "Job Node List = "$SLURM_JOB_NODELIST' >> SLURM_OpenMp_PrimeNum.sh
		echo 'echo "Number of Nodes = "$SLURM_NNODES' >> SLURM_OpenMp_PrimeNum.sh
		echo 'echo "Tasks per Nodes = "$SLURM_NTASKS_PER_NODE' >> SLURM_OpenMp_PrimeNum.sh
		echo 'echo "CPUs per task = "$SLURM_CPUS_PER_TASK' >> SLURM_OpenMp_PrimeNum.sh
		echo 'echo "/scratch/jobid = "$SLURMTMPDIR' >> SLURM_OpenMp_PrimeNum.sh
		echo 'echo "submit Host = "$SLURM_SUBMIT_HOST' >> SLURM_OpenMp_PrimeNum.sh
		echo 'echo "Submit Directory = "$SLURM_SUBMIT_DIR' >> SLURM_OpenMp_PrimeNum.sh
		echo 'echo ' >> SLURM_OpenMp_PrimeNum.sh
		echo '' >> SLURM_OpenMp_PrimeNum.sh
		echo 'ulimit -s unlimited' >> SLURM_OpenMp_PrimeNum.sh
		echo 'module load intel' >> SLURM_OpenMp_PrimeNum.sh
		echo 'module list' >> SLURM_OpenMp_PrimeNum.sh
		echo '' >> SLURM_OpenMp_PrimeNum.sh
		echo 'NPROCS=`srun --nodes=${SLURM_NNODES} bash -c '\'hostname\'' | wc -l`' >> SLURM_OpenMp_PrimeNum.sh
		echo 'echo "NPROCS="$NPROCS' >> SLURM_OpenMp_PrimeNum.sh
		echo '' >> SLURM_OpenMp_PrimeNum.sh
		echo 'export OMP_NUM_THREADS=$NPROCS' >> SLURM_OpenMp_PrimeNum.sh
		echo 'export | grep OMP' >> SLURM_OpenMp_PrimeNum.sh
		echo '#' >> SLURM_OpenMp_PrimeNum.sh
		echo './PrimeNumOpenMp $NPROCS 100000' >> SLURM_OpenMp_PrimeNum.sh
		echo './PrimeNumOpenMp $NPROCS 1000000' >> SLURM_OpenMp_PrimeNum.sh
		echo './PrimeNumOpenMp $NPROCS 2000000' >> SLURM_OpenMp_PrimeNum.sh
		echo './PrimeNumOpenMp $NPROCS 3000000' >> SLURM_OpenMp_PrimeNum.sh
		echo './PrimeNumOpenMp $NPROCS 4000000' >> SLURM_OpenMp_PrimeNum.sh
		echo './PrimeNumOpenMp $NPROCS 5000000' >> SLURM_OpenMp_PrimeNum.sh
		echo './PrimeNumOpenMp $NPROCS 6000000' >> SLURM_OpenMp_PrimeNum.sh
		echo './PrimeNumOpenMp $NPROCS 7000000' >> SLURM_OpenMp_PrimeNum.sh
		echo './PrimeNumOpenMp $NPROCS 8000000' >> SLURM_OpenMp_PrimeNum.sh
		echo './PrimeNumOpenMp $NPROCS 9000000' >> SLURM_OpenMp_PrimeNum.sh
		echo './PrimeNumOpenMp $NPROCS 10000000' >> SLURM_OpenMp_PrimeNum.sh
		echo './PrimeNumOpenMp $NPROCS 11000000' >> SLURM_OpenMp_PrimeNum.sh
		echo './PrimeNumOpenMp $NPROCS 12000000' >> SLURM_OpenMp_PrimeNum.sh
		echo './PrimeNumOpenMp $NPROCS 13000000' >> SLURM_OpenMp_PrimeNum.sh
		echo './PrimeNumOpenMp $NPROCS 14000000' >> SLURM_OpenMp_PrimeNum.sh
		echo '#' >> SLURM_OpenMp_PrimeNum.sh
		echo 'echo "All Done!"' >> SLURM_OpenMp_PrimeNum.sh
		chmod 777 SLURM_OpenMp_PrimeNum.sh
		sbatch SLURM_OpenMp_PrimeNum.sh
		rm -rf SLURM_OpenMp_PrimeNum.sh
		a=`expr $a + 1`
	done
fi
