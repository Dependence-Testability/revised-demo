package com.uniquepaths.mr;

import com.uniquepaths.util.SCC;

import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class AggregatorReducer
      extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {

  private static final String AVG_PATH_LEN = "average path length";

  @Override
  public void reduce(Text key, Iterable<DoubleWritable> values,
      Context context) throws IOException, InterruptedException {
    double aggregate = 0.0;
    double size = 0.0;
    String keyText = key.toString();
    for (DoubleWritable value : values) {
      aggregate += value.get();
      ++size;
    }

    if (keyText.contains(AVG_PATH_LEN)) {
      aggregate /= size;
    }
    context.write(key, new DoubleWritable(aggregate));
  }
}
