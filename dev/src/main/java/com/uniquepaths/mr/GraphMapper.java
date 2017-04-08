package com.uniquepaths.mr;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class GraphMapper extends Mapper<Object, Text, IntWritable, Text> {

  @Override
  public void map(Object key, Text value, Context context)
      throws IOException, InterruptedException {
    String[] line = value.toString().split(": ");
    IntWritable newKey = new IntWritable(Integer.parseInt(line[0]));
    Text newValue = new Text(line[1]);
    context.write(newKey, newValue);
  }
}
