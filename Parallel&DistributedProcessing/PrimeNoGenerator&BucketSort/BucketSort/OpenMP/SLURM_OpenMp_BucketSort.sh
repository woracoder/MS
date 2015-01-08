#!/bin/bash

#SBATCH --partition=debug
#SBATCH --nodes=1
#SBATCH --ntasks-per-node=2
#SBATCH --job-name=BucketSortOpenMp
#SBATCH --mail-user=srao2@buffalo.edu
#SBATCH --output=Result_BucketSortOpenMp.out
#SBATCH --error=Result_BucketSortOpenMp.out

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

NPROCS=`srun --nodes=${SLURM_NNODES} bash -c 'hostname' | wc -l`
echo "NPROCS="$NPROCS

export OMP_NUM_THREADS=$NPROCS
export | grep OMP
#
./BucketSortOpenMp -t 100000 $NPROCS
./BucketSortOpenMp -t 1000000 $NPROCS
./BucketSortOpenMp -t 2000000 $NPROCS
./BucketSortOpenMp -t 3000000 $NPROCS
./BucketSortOpenMp -t 4000000 $NPROCS
./BucketSortOpenMp -t 5000000 $NPROCS
./BucketSortOpenMp -t 6000000 $NPROCS
./BucketSortOpenMp -t 7000000 $NPROCS
./BucketSortOpenMp -t 8000000 $NPROCS
./BucketSortOpenMp -t 9000000 $NPROCS
./BucketSortOpenMp -t 10000000 $NPROCS
./BucketSortOpenMp -t 11000000 $NPROCS
./BucketSortOpenMp -t 12000000 $NPROCS
./BucketSortOpenMp -t 13000000 $NPROCS
./BucketSortOpenMp -t 14000000 $NPROCS
#
echo "All Done!"