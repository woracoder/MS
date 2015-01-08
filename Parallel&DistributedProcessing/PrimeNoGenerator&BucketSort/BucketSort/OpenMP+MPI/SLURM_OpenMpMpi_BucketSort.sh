#!/bin/bash

#SBATCH --partition=general-compute
#SBATCH --nodes=1
#SBATCH --ntasks-per-node=1
#SBATCH --cpus-per-task=2
#SBATCH --job-name=BucketSortOpenMpMpi
#SBATCH --mail-user=srao2@buffalo.edu
#SBATCH --output=Result_BucketSortOpenMpMpi.out
#SBATCH --error=Result_BucketSortOpenMpMpi.out

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
module load intel-mpi
module list

export OMP_NUM_THREADS=$SLURM_CPUS_PER_TASK

#
export I_MPI_PMI_LIBRARY=/usr/lib64/libpmi.so
srun ./BucketSortOpenMpMpi -t 100000 $SLURM_NNODES $SLURM_CPUS_PER_TASK
srun ./BucketSortOpenMpMpi -t 1000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK
srun ./BucketSortOpenMpMpi -t 10000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK
srun ./BucketSortOpenMpMpi -t 30000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK
srun ./BucketSortOpenMpMpi -t 50000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK
srun ./BucketSortOpenMpMpi -t 80000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK
srun ./BucketSortOpenMpMpi -t 100000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK
srun ./BucketSortOpenMpMpi -t 130000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK
srun ./BucketSortOpenMpMpi -t 150000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK
srun ./BucketSortOpenMpMpi -t 180000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK
srun ./BucketSortOpenMpMpi -t 200000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK
srun ./BucketSortOpenMpMpi -t 220000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK
srun ./BucketSortOpenMpMpi -t 250000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK
srun ./BucketSortOpenMpMpi -t 300000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK
srun ./BucketSortOpenMpMpi -t 350000000 $SLURM_NNODES $SLURM_CPUS_PER_TASK
#
echo "All Done!"
