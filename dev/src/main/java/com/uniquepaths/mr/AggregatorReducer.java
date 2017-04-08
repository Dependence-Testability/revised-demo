package com.uniquepaths.mr;

import com.uniquepaths.util.SCC;

import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class AggregatorReducer
      extends Reducer<Text, Text, Text, DoubleWritable> {

  private static final String NUM_PATH = "number";
  private static final String AVG_PATH_LEN = "length";

  @Override
  public void reduce(Text key, Iterable<Text> values,
      Context context) throws IOException, InterruptedException {
    double number = 0.0;
    double length = 0.0
    String keyText = key.toString();
    for (DoubleWritable value : values) {
      String[] line = value.toString().split(" ");
      if (keyText.contains(NUM_PATH)) {
        number = Double.parseDouble(line[1]);
      } else if (keyText.contains(AVG_PATH_LEN)) {
        length = Double.parseDouble(line[1]);
      }
    }
    length = number == 0.0 ? 0.0 : length/number;
    context.write(new Text(key + " number of paths: "),
        new DoubleWritable(number));
    context.write(new Text(key + " average path length: "),
        new DoubleWritable(length));
  }
}
