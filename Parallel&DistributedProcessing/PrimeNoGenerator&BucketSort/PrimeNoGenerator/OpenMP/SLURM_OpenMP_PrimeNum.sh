#!/bin/bash

#SBATCH --partition=debug
#SBATCH --nodes=1
#SBATCH --ntasks-per-node=2
#SBATCH --job-name=PrimeNumOpenMP
#SBATCH --mail-user=srao2@buffalo.edu
#SBATCH --output=Result_PrimeNumOpenMP.out
#SBATCH --error=Result_PrimeNumOpenMP.out

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
./PrimeNumOpenMP $NPROCS 100000
./PrimeNumOpenMP $NPROCS 1000000
./PrimeNumOpenMP $NPROCS 2000000
./PrimeNumOpenMP $NPROCS 3000000
./PrimeNumOpenMP $NPROCS 4000000
./PrimeNumOpenMP $NPROCS 5000000
./PrimeNumOpenMP $NPROCS 6000000
./PrimeNumOpenMP $NPROCS 7000000
./PrimeNumOpenMP $NPROCS 8000000
./PrimeNumOpenMP $NPROCS 9000000
./PrimeNumOpenMP $NPROCS 10000000
./PrimeNumOpenMP $NPROCS 11000000
./PrimeNumOpenMP $NPROCS 12000000
./PrimeNumOpenMP $NPROCS 13000000
./PrimeNumOpenMP $NPROCS 14000000

#
echo "All Done!"