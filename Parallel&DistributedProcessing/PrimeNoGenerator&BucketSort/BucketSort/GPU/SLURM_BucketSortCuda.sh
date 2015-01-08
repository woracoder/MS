#!/bin/bash
#SBATCH --partition=gpu
#SBATCH --gres=gpu:1
#SBATCH --nodes=1
#SBATCH --tasks-per-node=1
#SBATCH --job-name=BucketSortCuda
#SBATCH --time=08:00:00
#SBATCH --mail-user=srao2@buffalo.edu
#SBATCH --output=Result_BucketSortCuda.out
#SBATCH --error=Result_BucketSortCuda.out

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

./BucketSortCuda -t 100000
./BucketSortCuda -t 1000000
./BucketSortCuda -t 2000000
./BucketSortCuda -t 3000000
./BucketSortCuda -t 4000000
./BucketSortCuda -t 5000000
./BucketSortCuda -t 6000000
./BucketSortCuda -t 7000000
./BucketSortCuda -t 8000000
./BucketSortCuda -t 9000000
./BucketSortCuda -t 10000000
./BucketSortCuda -t 11000000
./BucketSortCuda -t 12000000
./BucketSortCuda -t 13000000
./BucketSortCuda -t 14000000

#
echo "All Done!"
