#!/bin/bash

#SBATCH --partition=general-compute
#SBATCH --nodes=1
#SBATCH --ntasks-per-node=1
#SBATCH --job-name=PrimeNumSseAvx
#SBATCH --mail-user=srao2@buffalo.edu
#SBATCH --output=Result_PrimeNumSseAvx.out
#SBATCH --error=Result_PrimeNumSseAvx.out

echo "SLURN Environment Variables:"
echo "Job ID = "$SLURM_JOB_ID
echo "Job Name = "$SLURM_JOB_NAME
echo "Job Node List = "$SLURM_JOB_NODELIST
echo "Number of Nodes = "$SLURM_NNODES
echo "Tasks per Nodes = "$SLURM_NTASKS_PER_NODE
echo "/scratch/jobid = "$SLURMTMPDIR
echo "submit Host = "$SLURM_SUBMIT_HOST
echo "Submit Directory = "$SLURM_SUBMIT_DIR
echo 

ulimit -s unlimited
module load intel
module list

#
./PrimeNumSseAvx $SLURM_NTASKS_PER_NODE 10000000
./PrimeNumSseAvx $SLURM_NTASKS_PER_NODE 20000000
./PrimeNumSseAvx $SLURM_NTASKS_PER_NODE 30000000
./PrimeNumSseAvx $SLURM_NTASKS_PER_NODE 40000000
./PrimeNumSseAvx $SLURM_NTASKS_PER_NODE 50000000
./PrimeNumSseAvx $SLURM_NTASKS_PER_NODE 60000000
./PrimeNumSseAvx $SLURM_NTASKS_PER_NODE 70000000
./PrimeNumSseAvx $SLURM_NTASKS_PER_NODE 80000000
./PrimeNumSseAvx $SLURM_NTASKS_PER_NODE 90000000
./PrimeNumSseAvx $SLURM_NTASKS_PER_NODE 100000000
./PrimeNumSseAvx $SLURM_NTASKS_PER_NODE 110000000
./PrimeNumSseAvx $SLURM_NTASKS_PER_NODE 120000000
./PrimeNumSseAvx $SLURM_NTASKS_PER_NODE 130000000
./PrimeNumSseAvx $SLURM_NTASKS_PER_NODE 140000000
./PrimeNumSseAvx $SLURM_NTASKS_PER_NODE 150000000
#
echo "All Done!"