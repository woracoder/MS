#!/bin/bash
#SBATCH --partition=debug
#SBATCH --nodes=4
#SBATCH --ntasks-per-node=1
#SBATCH --exclusive
#SBATCH --output=Output_AirlineHadoop.out

module load myhadoop/0.30b/hadoop-2.5.1
export MH_SCRATCH_DIR=$SLURMTMPDIR
export HADOOP_CONF_DIR=$SLURM_SUBMIT_DIR/config-$SLURM_JOBID

$MH_HOME/bin/myhadoop-configure.sh
echo "Config done"
$HADOOP_HOME/sbin/start-all.sh
echo "Start done"
$HADOOP_HOME/bin/hadoop dfsadmin -report
echo "Report done"
$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -mkdir /input/
echo "Input dir created"
$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -put /panasas/scratch/srao2/airline/allyears.csv /input/
echo "Copied allyears.csv to /input"
$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -mkdir /data/
echo "Data dir created"
$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -put /panasas/scratch/srao2/airline/carriers.csv /data/
$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -put /panasas/scratch/srao2/airline/airports.csv /data/
$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -put /panasas/scratch/srao2/airline/plane-data.csv /data/
echo "Copied files to /data"
$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -ls /
$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -ls /input/
$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -ls /data/
echo "Verifying the structure. Will start execution now"
$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR jar airline.jar com.driver.Driver $SLURM_NNODES
echo "Execution done"
$HADOOP_HOME/bin/hadoop --config $HADOOP_CONF_DIR dfs -get /output/* /panasas/scratch/srao2/airline/output/
echo "Copy from hdfs to local done"
$HADOOP_HOME/sbin/stop-all.sh
echo "Stop done"
$MH_HOME/bin/myhadoop-cleanup.sh
echo "cleanup done"
rm -rf config-*
echo "Config removed"

