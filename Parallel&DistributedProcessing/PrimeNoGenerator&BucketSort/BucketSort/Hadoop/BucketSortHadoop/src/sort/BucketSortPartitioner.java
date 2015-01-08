package sort;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

public class BucketSortPartitioner extends
		Partitioner<Text, IntWritable> implements Configurable {

	private Configuration conf;
	private double mean = 0;
	private double stdDev = 0;

	@Override
	public Configuration getConf() {
		return this.conf;
	}

	@Override
	public void setConf(Configuration arg0) {
		this.conf = arg0;
		mean = this.conf.getDouble("Mean", 0);
		stdDev = this.conf.getDouble("StdDev", 0);
	}

	@Override
	public int getPartition(Text key, IntWritable val,
			int numPartitions) {
		return (int) (cdf((Double.parseDouble(key.toString()) - mean) / stdDev) * numPartitions);
	}

	// http://www.johndcook.com/cpp_phi.html
	private double cdf(double x) {

		double a1 = 0.254829592;
		double a2 = -0.284496736;
		double a3 = 1.421413741;
		double a4 = -1.453152027;
		double a5 = 1.061405429;
		double p = 0.3275911;

		int sign = 1;
		if (x < 0) {
			sign = -1;
		}
		x = Math.abs(x) / Math.sqrt(2.0);

		double t = 1.0 / (1.0 + p * x);
		double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t
				* Math.exp(-x * x);

		return 0.5 * (1.0 + sign * y);
	}

}
