package com.uniquepaths.mr;

import com.uniquepaths.util.SCC;

import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class GraphReducer
      extends Reducer<IntWritable, Text, Text, DoubleWritable> {

  @Override
  public void reduce(IntWritable key, Iterable<Text> values,
      Context context) throws IOException, InterruptedException {
    StringBuilder strBldr;
    Text outputKey;
    double totalNumPaths = 1.0;
    double avgPathLen = 1.0;
    SCC<Integer> scc = constructSCC(values);
    
    if (scc.size() > 1) {
      scc.computeInternalDistances();
      totalNumPaths = scc.getTotalNumberPaths();
      avgPathLen = scc.getTotalAvgPathLength();
    }
    strBldr = new StringBuilder();
    strBldr.append(scc.getSccId());
    strBldr.append(" total number of paths: ");
    outputKey = new Text(strBldr.toString());
    context.write(outputKey, new DoubleWritable(totalNumPaths));
    strBldr = new StringBuilder();
    strBldr.append(scc.getSccId());
    strBldr.append(" average path length: ");
    outputKey = new Text(strBldr.toString());
    context.write(outputKey, new DoubleWritable(avgPathLen));
  }

  private static SCC<Integer> constructSCC(Iterable<Text> values) {
    SCC<Integer> scc = new SCC<>();
    int sccId = -1;
    for (Text value : values) {
      String[] line = value.toString().split(" ");
      if (sccId == -1) {
        scc.setSccId(Integer.parseInt(line[0]));
      }
      if (line.length == 4) {
        // Handling edges
        scc.addEdge(Integer.parseInt(line[1]), Integer.parseInt(line[2]),
            Integer.parseInt(line[3]));
      } else if (line.length == 3) {
        // Handling either in or out nodes
        if (line[1].equals("in")) {
          scc.addInNode(Integer.parseInt(line[2]));
        } else if (line[1].equals("out")) {
          scc.addOutNode(Integer.parseInt(line[2]));
        }
      }
    }
    return scc;
  }
}
