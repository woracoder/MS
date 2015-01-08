#!/bin/bash

#SBATCH --partition=general-compute
#SBATCH --nodes=2
#SBATCH --ntasks-per-node=1
#SBATCH --job-name=BucketSortMpi
#SBATCH --mail-user=srao2@buffalo.edu
#SBATCH --output=Result_BucketSortMpi.out
#SBATCH --error=Result_BucketSortMpi.out

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

module load intel
module load intel-mpi
module list
ulimit -s unlimited

#
export I_MPI_PMI_LIBRARY=/usr/lib64/libpmi.so
srun ./BucketSortMpi -t 100000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE
srun ./BucketSortMpi -t 1000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE
srun ./BucketSortMpi -t 2000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE
srun ./BucketSortMpi -t 3000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE
srun ./BucketSortMpi -t 4000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE
srun ./BucketSortMpi -t 5000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE
srun ./BucketSortMpi -t 6000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE
srun ./BucketSortMpi -t 7000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE
srun ./BucketSortMpi -t 8000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE
srun ./BucketSortMpi -t 9000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE
srun ./BucketSortMpi -t 10000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE
srun ./BucketSortMpi -t 11000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE
srun ./BucketSortMpi -t 12000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE
srun ./BucketSortMpi -t 13000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE
srun ./BucketSortMpi -t 14000000 $SLURM_NNODES $SLURM_NTASKS_PER_NODE
#
echo "All Done!"