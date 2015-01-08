#!/bin/bash

#SBATCH --partition=gpu
#SBATCH --nodes=1
#SBATCH --ntasks-per-node=1
#SBATCH --job-name=PrimeNumSequential
#SBATCH --mail-user=srao2@buffalo.edu
#SBATCH --output=Result_PrimeNumSequential.out
#SBATCH --error=Result_PrimeNumSequential.out

echo "SLURM Environment Variables:"
echo "Job ID = "$SLURM_JOB_ID
echo "Job Name = "$SLURM_JOB_NAME
echo "Job Node List = "$SLURM_JOB_NODELIST
echo "Number of Nodes = "$SLURM_NNODES
echo "Tasks per Nodes = "$SLURM_NTASKS_PER_NODE
echo "CPUs per task = "$SLURM_CPUS_PER_TASK
echo "/scratch/jobid = "$SLURMTMPDIR
echo "submit Host = "$SLURM_SUBMIT_HOST
echo "Submit Directory = "$SLURM_SUBMIT_DIR
echo

ulimit -s unlimited
module load intel
module list

#
./PrimeNumSequential 100000
./PrimeNumSequential 1000000
./PrimeNumSequential 2000000
./PrimeNumSequential 3000000
./PrimeNumSequential 4000000
./PrimeNumSequential 5000000
./PrimeNumSequential 6000000
./PrimeNumSequential 7000000
./PrimeNumSequential 8000000
./PrimeNumSequential 9000000
./PrimeNumSequential 10000000
./PrimeNumSequential 11000000
./PrimeNumSequential 12000000
./PrimeNumSequential 13000000
./PrimeNumSequential 14000000

#
echo "All Done!"