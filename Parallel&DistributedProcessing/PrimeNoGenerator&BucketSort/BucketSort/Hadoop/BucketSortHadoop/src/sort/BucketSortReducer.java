package sort;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class BucketSortReducer extends
		Reducer<Text, IntWritable, Text, Text> {

	private static final Text value = new Text("");

	public void reduce(Text key, Iterable<IntWritable> values,
			Context context) throws IOException, InterruptedException {
		for (IntWritable val : values) {
			for (int i = 0, j = val.get(); i < j; i++) {
				context.write(key, value);
			}
		}
	}

}
