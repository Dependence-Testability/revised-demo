package com.uniquepaths.mr;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class AggregatorMapper extends Mapper<Object, Text, Text,
    Text> {

  @Override
  public void map(Object key, Text value, Context context)
      throws IOException, InterruptedException {
    String[] line = value.toString().split(":");
    Text newKey = new Text(line[0]);
    DoubleWritable newValue = new DoubleWritable(Double.parseDouble(line[1]));
    context.write(newKey, newValue);
  }
}
