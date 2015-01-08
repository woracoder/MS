#!/bin/bash
#SBATCH --partition=gpu
#SBATCH --gres=gpu:1
#SBATCH --nodes=1
#SBATCH --tasks-per-node=1
#SBATCH --job-name=PrimeNumCuda
#SBATCH --time=00:59:00
#SBATCH --mail-user=srao2@buffalo.edu
#SBATCH --output=Result_PrimeNumCuda.out
#SBATCH --error=Result_PrimeNumCuda.out

echo "SLURM Environment Variables:"
echo "Job ID = "$SLURM_JOB_ID
echo "Job Name = "$SLURM_JOB_NAME
echo "Job Node List = "$SLURM_JOB_NODELIST
echo "Number of Nodes = "$SLURM_NNODES
echo "Tasks per node = "$SLURM_NTASKS_PER_NODE
echo "CPUs per task = "$SLURM_CPUS_PER_TASK
echo "/scratch/jobid = "$SLURMTMPDIR
echo "Submit Host = "$SLURM_SUBMIT_HOST
echo "Submit Directory = "$SLURM_SUBMIT_DIR
echo 
echo

ulimit -s unlimited
#

module load cuda
which nvcc

./PrimeNumCuda 100000
./PrimeNumCuda 1000000
./PrimeNumCuda 2000000
./PrimeNumCuda 3000000
./PrimeNumCuda 4000000
./PrimeNumCuda 5000000
./PrimeNumCuda 6000000
./PrimeNumCuda 7000000
./PrimeNumCuda 8000000
./PrimeNumCuda 9000000
./PrimeNumCuda 10000000
./PrimeNumCuda 11000000
./PrimeNumCuda 12000000
./PrimeNumCuda 13000000
./PrimeNumCuda 14000000

#
echo "All Done!"
