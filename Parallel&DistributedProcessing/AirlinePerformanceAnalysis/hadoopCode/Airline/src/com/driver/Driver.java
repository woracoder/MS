package com.driver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import com.map.AirlineAgeMapper;
import com.map.CascadingDelaysMapper;
import com.map.DelaysMapper;
import com.reduce.AirlineAgeReducer;
import com.reduce.CascadingDelaysReducer;
import com.reduce.DelaysReducer;

public class Driver {

	public static void main(String[] args) throws IOException, ParseException,
			InterruptedException, ClassNotFoundException {

		if (args.length != 3) {
			System.out
					.println("Usage: (com.driver.Driver) (No. of nodes) (Job Type)");
			System.exit(1);
		}

		if (!args[0].equals("com.driver.Driver")
				|| !(Integer.valueOf(args[1]) instanceof Integer)) {
			System.out
					.println("Enter correct parameters. Usage: (com.driver.Driver) (No. of nodes) (Job Type)");
			System.exit(1);
		}

		int noOfNodes = Integer.parseInt(args[1]);
		Configuration myConfig = new Configuration();

		Job myJob = Job.getInstance(myConfig, "Airline performance");
		myJob.setJarByClass(Driver.class);
		myJob.setNumReduceTasks(noOfNodes);

		if (args[2].equals("delay")) {
			myJob.setMapperClass(DelaysMapper.class);
			myJob.setReducerClass(DelaysReducer.class);
			TextInputFormat.addInputPath(myJob, new Path("/input/delay/"));
			TextOutputFormat.setOutputPath(myJob, new Path("/output/delay/"));
		} else if (args[2].equals("age")) {
			myJob.setMapperClass(AirlineAgeMapper.class);
			myJob.setReducerClass(AirlineAgeReducer.class);
			TextInputFormat.addInputPath(myJob, new Path("/input/age/"));
			TextOutputFormat.setOutputPath(myJob, new Path("/output/age/"));
		} else if (args[2].equals("cascade")) {
			myJob.setMapperClass(CascadingDelaysMapper.class);
			myJob.setReducerClass(CascadingDelaysReducer.class);
			TextInputFormat.addInputPath(myJob, new Path("/input/cascade/"));
			TextOutputFormat.setOutputPath(myJob, new Path("/output/cascade/"));
		}

		myJob.setMapOutputKeyClass(Text.class);
		myJob.setMapOutputValueClass(Text.class);
		myJob.setOutputKeyClass(Text.class);
		myJob.setOutputValueClass(Text.class);

		long startTime = System.currentTimeMillis();
		myJob.waitForCompletion(true);
		double timeReqd = (System.currentTimeMillis() - startTime) / 1000.0;

		BufferedWriter bwrt = new BufferedWriter(new FileWriter(new File(
				"Results.txt"), true));
		bwrt.append("Time taken to process all data for getting all metrics = "
				+ timeReqd + ".\n");
		bwrt.close();
	}

}
