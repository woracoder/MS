#!/bin/bash

#SBATCH --partition=debug
#SBATCH --nodes=2
#SBATCH --ntasks-per-node=1
#SBATCH --job-name=PrimeNumMpi
#SBATCH --mail-user=srao2@buffalo.edu
#SBATCH --output=Result_PrimeNumMpi.out
#SBATCH --error=Result_PrimeNumMpi.out

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
srun ./PrimeNumMpi $SLURM_NNODES 100000
srun ./PrimeNumMpi $SLURM_NNODES 1000000
srun ./PrimeNumMpi $SLURM_NNODES 2000000
srun ./PrimeNumMpi $SLURM_NNODES 3000000
srun ./PrimeNumMpi $SLURM_NNODES 4000000
srun ./PrimeNumMpi $SLURM_NNODES 5000000
srun ./PrimeNumMpi $SLURM_NNODES 6000000
srun ./PrimeNumMpi $SLURM_NNODES 7000000
srun ./PrimeNumMpi $SLURM_NNODES 8000000
srun ./PrimeNumMpi $SLURM_NNODES 9000000
srun ./PrimeNumMpi $SLURM_NNODES 10000000
srun ./PrimeNumMpi $SLURM_NNODES 11000000
srun ./PrimeNumMpi $SLURM_NNODES 12000000
srun ./PrimeNumMpi $SLURM_NNODES 13000000
srun ./PrimeNumMpi $SLURM_NNODES 14000000
#
echo "All Done!"