package com.uniquepaths.mr;

import com.uniquepaths.util.SCC;

import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class AggregatorReducer
      extends Reducer<IntWritable, Text, Text, DoubleWritable> {

  private static final String NUM_PATH = "number";
  private static final String AVG_PATH_LEN = "length";

  @Override
  public void reduce(IntWritable key, Iterable<Text> values,
      Context context) throws IOException, InterruptedException {
    double number = 0.0;
    double length = 0.0;
    int keyValue = key.get();
    for (Text value : values) {
      String[] line = value.toString().split("\t");
      if (line[0].contains(NUM_PATH)) {
        number += Double.parseDouble(line[1]);
      } else if (line[0].contains(AVG_PATH_LEN)) {
        length += Double.parseDouble(line[1]);
      }
    }
    length = number == 0.0 ? 0.0 : length/number;
    context.write(new Text(key + " number of paths: "),
        new DoubleWritable(number));
    context.write(new Text(key + " average path length: "),
        new DoubleWritable(length));
  }
}
