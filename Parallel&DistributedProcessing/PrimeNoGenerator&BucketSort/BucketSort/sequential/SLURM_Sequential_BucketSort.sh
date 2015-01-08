#!/bin/bash

#SBATCH --partition=debug
#SBATCH --nodes=1
#SBATCH --ntasks-per-node=1
#SBATCH --job-name=BucketSortSequential
#SBATCH --mail-user=srao2@buffalo.edu
#SBATCH --output=Result_BucketSortSequential.out
#SBATCH --error=Result_BucketSortSequential.out

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
./BucketSortSequential -t 100000
./BucketSortSequential -t 1000000
./BucketSortSequential -t 2000000
./BucketSortSequential -t 3000000
./BucketSortSequential -t 4000000
./BucketSortSequential -t 5000000
./BucketSortSequential -t 6000000
./BucketSortSequential -t 7000000
./BucketSortSequential -t 8000000
./BucketSortSequential -t 9000000
./BucketSortSequential -t 10000000
./BucketSortSequential -t 11000000
./BucketSortSequential -t 12000000
./BucketSortSequential -t 13000000
./BucketSortSequential -t 14000000
#
echo "All Done!"