package com.uniquepaths;

import com.uniquepaths.mr.GraphMapper;
import com.uniquepaths.mr.GraphReducer;
import com.uniquepaths.util.Edge;
import com.uniquepaths.util.Graph;
import com.uniquepaths.util.Node;
import com.uniquepaths.util.PathApproximation;
import com.uniquepaths.util.PathFinder;
import com.uniquepaths.util.SCC;
import com.uniquepaths.util.StronglyConnectedComponents;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.StringUtils;

public class UniquePaths {
    
  public static void main(String[] args) {
    System.out.println(arg);
    double[] result;
    Node<Integer> start;
    Node<Integer> end;
    int superS;
    int superE;
    String[] phase1Args = Arrays.copyOfRange(args, 3, 5);
    String[] phase2Args = Arrays.copyOfRange(args, 5, 7);
    Graph<Integer> graph = readGraphFromFile(args[0]);
    int s = Integer.parseInt(args[1]);
    int e = Integer.parseInt(args[2]);
    List<SCC<Integer>> sccs
        = StronglyConnectedComponents.getStronglyConnectedComponents(graph);
    List<SCC<Integer>> permutations
        = StronglyConnectedComponents.getPermutedSCCs(sccs);
    writeToInputFile(permutations);
    mapReducePathFinder(phase1Args);
    mapReduceAggregator(phase2Args);
    readSCCInfo(sccs);
    Graph<Integer> contracted = StronglyConnectedComponents.contractSCCs(sccs);
    start = graph.getNode(s);
    end = graph.getNode(e);
    superS = sccs.get(start.getSccId()).getExpandedNodes().get(0).getValue();
    superE = sccs.get(end.getSccId()).getExpandedNodes().get(0).getValue();
    result = PathFinder.dagTraversal(graph, contracted, superS, superE, s, e);
    System.out.println("MapReduce Results: ");
    System.out.println("\tNumber of Paths: " + result[0]);
    System.out.println("\tAverage length of paths: " + result[1]);
    result = PathFinder.uniquePaths(graph, s, e);
    System.out.println("Actual Results: ");
    System.out.println("\tNumber of Paths: " + result[0]);
    System.out.println("\tAverage length of paths: " + result[1]);
  }

  private static Graph<Integer> readGraphFromFile(String fileName) {
    File file = null;
    Scanner scan = null;
    Graph<Integer> graph = null;
    try {
      file = new File(fileName);
      scan = new Scanner(file);
      graph = new Graph<>();
      while (scan.hasNextLine()) {
        String line = scan.nextLine();
        String[] coords = line.split(" ");
        int x = Integer.parseInt(coords[0]);
        int y = Integer.parseInt(coords[1]);
        graph.addEdge(x, y);
      } 
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (scan != null) {
        scan.close();
      }
    }
    return graph;
  }

  private static void writeToInputFile(List<SCC<Integer>> sccs) {
    PrintWriter pw = null;
    StringBuilder strBldr;
    SCC<Integer> scc;
    List<String> lines = new ArrayList<>(); 
    try {
      pw = new PrintWriter("uniquepaths/inputPhase1/input");
      for (int i = 0; i < sccs.size(); ++i) {
        scc = sccs.get(i);
        for (Edge<Integer> edge : scc.getGraphAsEdgeList()) {
          strBldr = new StringBuilder();
          strBldr.append(i);
          strBldr.append(": ");
          strBldr.append(scc.getSccId());
          strBldr.append(' ');
          strBldr.append(edge.from);
          strBldr.append(' ');
          strBldr.append(edge.to);
          strBldr.append(' ');
          strBldr.append(edge.weight);
          pw.println(strBldr);
        }
        for (Node<Integer> in : scc.getInNodes()) {
          strBldr = new StringBuilder();
          strBldr.append(i);
          strBldr.append(": ");
          strBldr.append(scc.getSccId());
          strBldr.append(" in ");
          strBldr.append(in.getValue());
          pw.println(strBldr);
        }
        for (Node<Integer> out : scc.getOutNodes()) {
          strBldr = new StringBuilder();
          strBldr.append(i);
          strBldr.append(": ");
          strBldr.append(scc.getSccId());
          strBldr.append(" out ");
          strBldr.append(out.getValue());
          pw.println(strBldr);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (pw != null) {
        pw.close();
      }
    }
  }

  private static void mapReducePathFinder(String[] args) {
    Configuration conf;
    GenericOptionsParser optionParser;
    String[] remainingArgs;

    try {
      conf = new Configuration();
      optionParser = new GenericOptionsParser(conf, args);
      remainingArgs = optionParser.getRemainingArgs();
      if (!(remainingArgs.length != 2 || remainingArgs.length != 4)) {
        System.err.println("Usage: uniquepaths <in> <out> [-skip skipPatternFile]");
        System.exit(2);
      }
      Job job = Job.getInstance(conf, "uniquepaths");
      job.setMapperClass(GraphMapper.class);
      job.setReducerClass(GraphReducer.class);
      job.setMapOutputKeyClass(IntWritable.class);
      job.setMapOutputValueClass(Text.class);
      job.setOutputKeyClass(Text.class);
      job.setOutputValueClass(DoubleWritable.class);
      job.setJarByClass(UniquePaths.class);

      List<String> otherArgs = new ArrayList<String>();
      for (int i=0; i < remainingArgs.length; ++i) {
        if ("-skip".equals(remainingArgs[i])) {
          job.addCacheFile(new Path(remainingArgs[++i]).toUri());
          job.getConfiguration().setBoolean("uniquepaths.skip.patterns", true);
        } else {
          otherArgs.add(remainingArgs[i]);
        }
      }

      FileInputFormat.addInputPath(job, new Path(otherArgs.get(0)));
      FileOutputFormat.setOutputPath(job, new Path(otherArgs.get(1)));
      job.waitForCompletion(true);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }  catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void mapReduceAggregator(String[] args) {
    Configuration conf;
    GenericOptionsParser optionParser;
    String[] remainingArgs;

    try {
      conf = new Configuration();
      optionParser = new GenericOptionsParser(conf, args);
      remainingArgs = optionParser.getRemainingArgs();
      if (!(remainingArgs.length != 2 || remainingArgs.length != 4)) {
        System.err.println("Usage: uniquepaths <in> <out> [-skip skipPatternFile]");
        System.exit(2);
      }
      Job job = Job.getInstance(conf, "uniquepaths");
      job.setMapperClass(AggregatorMapper.class);
      job.setReducerClass(AggregatorReducer.class);
      job.setMapOutputKeyClass(Text.class);
      job.setMapOutputValueClass(DoubleWritable.class);
      job.setOutputKeyClass(Text.class);
      job.setOutputValueClass(DoubleWritable.class);
      job.setJarByClass(UniquePaths.class);

      List<String> otherArgs = new ArrayList<String>();
      for (int i=0; i < remainingArgs.length; ++i) {
        if ("-skip".equals(remainingArgs[i])) {
          job.addCacheFile(new Path(remainingArgs[++i]).toUri());
          job.getConfiguration().setBoolean("uniquepaths.skip.patterns", true);
        } else {
          otherArgs.add(remainingArgs[i]);
        }
      }

      FileInputFormat.addInputPath(job, new Path(otherArgs.get(0)));
      FileOutputFormat.setOutputPath(job, new Path(otherArgs.get(1)));
      job.waitForCompletion(true);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }  catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void readSCCInfo(List<SCC<Integer>> sccList) {
    File file;
    Scanner scan = null;
    String[] line;
    SCC<Integer> scc;
    int pos;
    int totalNumPaths;
    double totalAvgPathLen;
    try {
      file = new File("uniquepaths/outputPhase2/part-r-00000");
      scan = new Scanner(file);
      while (scan.hasNextLine()) {
        line = scan.nextLine().split(" : ");
        pos = Integer.parseInt(line[0]);
        scc = sccList.get(pos);
        totalNumPaths = (int) Double.parseDouble(line[1]);
        line = scan.nextLine().split(" : ");
        totalAvgPathLen = Double.parseDouble(line[1]);
        scc.setTotalNumberPaths(totalNumPaths);
        scc.setTotalAvgPathLength(totalAvgPathLen);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (scan != null) {
        scan.close();
      }
    }
  }
}
