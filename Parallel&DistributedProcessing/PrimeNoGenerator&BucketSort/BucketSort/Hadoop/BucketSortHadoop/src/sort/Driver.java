package sort;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.MRJobConfig;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class Driver {

	public static void main(String[] args) throws IOException,
			ClassNotFoundException, InterruptedException {

		if (args.length != 4) {
			System.out
					.println("Usage: (sort.Driver) (-t) (Input Size) (No. of nodes)");
			System.exit(1);
		}

		if (!args[0].equals("sort.Driver") || !args[1].equals("-t")
				|| !(Integer.valueOf(args[2]) instanceof Integer)
				|| !(Integer.valueOf(args[3]) instanceof Integer)) {
			System.out
					.println("Enter correct parameters. Usage: (sort.Driver) (-t) (Input Size) (No. of nodes)");
			System.exit(1);
		}

		int problemSize = Integer.parseInt(args[2]);
		int noOfNodes = Integer.parseInt(args[3]);

		Configuration myConfig = new Configuration();

		FileSystem fs = FileSystem.get(myConfig);

		File nof = new File(
				"/panasas/scratch/srao2/BucketSortHadoop/Numbers.txt" + noOfNodes);
		nof.createNewFile();
		BufferedWriter bwr = new BufferedWriter(new FileWriter(nof, false));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				fs.create(new Path("/input/Numbers.txt"), true)));

		float maxNum = (float) (problemSize * 10.0);
		float meanNum = (float) (maxNum / 2.0);
		float var = (float) Math.sqrt(maxNum);
		Random rnd = new Random();
		double sum = 0;
		float tmpFl = 0.0f;
		for (int i = 0; i < problemSize; i++) {
			tmpFl = (float) (meanNum + rnd.nextGaussian() * var);
			sum += tmpFl;
			bwr.append(String.format("%.6f", tmpFl) + "\n");
			bw.append(String.format("%.6f", tmpFl) + "\n");
		}
		bwr.close();
		bw.close();

		double mean = sum / problemSize;
		double stdDev = calculateStandardDeviation(mean, problemSize, noOfNodes);

		myConfig.setDouble("Mean", mean);
		myConfig.setDouble("StdDev", stdDev);

		long fileSize = fs.getFileStatus(new Path("/input/Numbers.txt"))
				.getLen();

		int mapsByH = (int) Math.ceil(fileSize / (64000000.0f));
		if (mapsByH < 10 * noOfNodes) {
			mapsByH = 10 * noOfNodes;
		}

		myConfig.set(MRJobConfig.NUM_MAPS, String.valueOf(mapsByH));

		Job myJob = Job.getInstance(myConfig, "Bucket Sort");
		myJob.setJarByClass(Driver.class);
		myJob.setNumReduceTasks(2 * noOfNodes);
		myJob.setMapperClass(BucketSortMapper.class);
		myJob.setCombinerClass(BucketSortCombiner.class);
		myJob.setPartitionerClass(BucketSortPartitioner.class);
		myJob.setReducerClass(BucketSortReducer.class);
		myJob.setMapOutputKeyClass(Text.class);
		myJob.setMapOutputValueClass(IntWritable.class);
		myJob.setOutputKeyClass(Text.class);
		myJob.setOutputValueClass(Text.class);

		TextInputFormat.setMaxInputSplitSize(myJob, fileSize / mapsByH);
		TextInputFormat.setMinInputSplitSize(myJob, fileSize / (mapsByH + 2));
		TextInputFormat.addInputPath(myJob, new Path("/input/"));
		TextOutputFormat.setOutputPath(myJob, new Path("/output/"));

		long startTime = System.currentTimeMillis();
		myJob.waitForCompletion(true);
		double timeReqd = (System.currentTimeMillis() - startTime) / 1000.0;

		BufferedWriter bwrt = new BufferedWriter(new FileWriter(new File(
				"Results.txt"), true));
		bwrt.append("Time taken to sort " + problemSize + " numbers = "
				+ timeReqd + " seconds on " + noOfNodes + " nodes.\n");
		bwrt.close();

		FileStatus[] fstat = fs.listStatus(new Path("/output/"));
		int cnt = 0;
		String[] split = null;
		String num = null, tmp = null;
		BufferedWriter bwrit = new BufferedWriter(new FileWriter(new File(
				"/panasas/scratch/srao2/BucketSortHadoop/SortedNos.txt")));
		while (cnt < (2 * noOfNodes)) {
			for (FileStatus f : fstat) {
				if (f.getPath().getName().contains("part")) {
					split = f.getPath().getName().split("-");
					num = split[split.length - 1];
					if (cnt == Integer.parseInt(num)) {
						BufferedReader brd = new BufferedReader(
								new InputStreamReader(fs.open(f.getPath())));
						while ((tmp = brd.readLine()) != null
								&& tmp.trim().length() > 0) {
							bwrit.append(tmp + "\n");
							System.out.println(tmp + "\n");
						}
						cnt++;
						brd.close();
						break;
					}
				}
			}
		}
		bwrit.close();

		System.exit(0);
	}

	public static double calculateStandardDeviation(double mean, int problemSize, int noOfNodes)
			throws NumberFormatException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(
				"/panasas/scratch/srao2/BucketSortHadoop/Numbers.txt" + noOfNodes)));
		double var = 0;
		String tmp = null;
		while ((tmp = br.readLine()) != null && tmp.trim().length() != 0) {
			var += Math.pow((Double.parseDouble(tmp) - mean), 2.0);
		}
		var /= problemSize;
		br.close();
		return (Math.sqrt(var));
	}

}
