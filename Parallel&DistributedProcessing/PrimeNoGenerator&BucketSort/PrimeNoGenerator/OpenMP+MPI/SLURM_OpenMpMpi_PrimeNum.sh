#!/bin/bash

#SBATCH --partition=general-compute
#SBATCH --nodes=2
#SBATCH --ntasks-per-node=4
#SBATCH -n 2
#SBATCH --job-name=PrimeNumOpenMpMpi
#SBATCH --mail-user=srao2@buffalo.edu
#SBATCH --output=Result_PrimeNumOpenMpMpi.out
#SBATCH --error=Result_PrimeNumOpenMpMpi.out

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

export OMP_NUM_THREADS=$SLURM_NTASKS_PER_NODE

#
export I_MPI_PMI_LIBRARY=/usr/lib64/libpmi.so
srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 10000000
srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 20000000
srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 30000000
srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 40000000
srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 50000000
srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 60000000
srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 70000000
srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 80000000
srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 90000000
srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 100000000
srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 110000000
srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 120000000
srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 130000000
srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 140000000
srun ./PrimeNumOpenMpMpi $SLURM_NNODES $SLURM_NTASKS_PER_NODE 150000000
#
echo "All Done!"
