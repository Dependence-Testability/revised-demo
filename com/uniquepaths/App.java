package com.uniquepaths;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.List;

import com.uniquepaths.util.*;

public class App {
    
  public static void main(String[] args) {
    sccTest();
  }

  private static void sccTest() {
    File file;
    Scanner scan = null;
    String[] line1;
    int graphId;
    SCC<Integer> scc;
    List<SCC<Integer>> sccList;
    sccList = new ArrayList<>();
    Map<Integer, List<String[]>> sccMap = new HashMap<>();
    try {
      file = new File("data/scc_file1.txt");
      scan = new Scanner(file);
      while (scan.hasNextLine()) {
        line1 = scan.nextLine().split(" ");
        System.out.println(line1[0]);
        System.out.println(line1[1]);
        graphId = Integer.parseInt(line1[0]);
        if (!sccMap.containsKey(graphId)) {
          sccMap.put(graphId, new ArrayList<String[]>());
        }
        sccMap.get(graphId).add(line1);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (scan != null) {
        scan.close();
      }
    }

    for (int i = 0; i < sccMap.size(); i++) {
      scc = new SCC<Integer>();
      for (String[] line : sccMap.get(i)) {
        if (line.length == 4) {
          System.out.println("adding an edge");
          scc.addEdge(Integer.parseInt(line[1]), Integer.parseInt(line[2]), Integer.parseInt(line[3]));
        } else if (line.length == 3) {
          if (line[1].equals("in")) {
            System.out.println("adding an in node");
            scc.addInNode(Integer.parseInt(line[2]));
          } else if (line[1].equals("out")) {
            System.out.println("adding an out node");
            scc.addOutNode(Integer.parseInt(line[2]));
          }
        }
      }
      sccList.add(scc);
    }
    processList(sccList);
  }

  private static void processList(List<SCC<Integer>> sccList) {
    int i = 0;
    for (SCC<Integer> scc : sccList) {
      scc.computeInternalDistances();
      System.out.println(i + " size: " + scc.size());
      System.out.println(i + " inNodes: " + scc.getInNodes());
      System.out.println(i + " outNodes: " + scc.getOutNodes());
      System.out.println(i + " number paths: " + scc.getTotalNumberPaths());
      System.out.println(i + " distance: " + scc.getTotalAvgPathLength());
      ++i;
    }
  }

  private static void mrTest(String fileName) {
    Graph<Integer> graph;
    Graph<Integer> contracted;
    List<SCC<Integer>> sccs;
    double[] result;
    int s = 1;
    int e = 41;

    System.out.println("Stage 1: Preparation; Pre mapreduce stage");
    graph = readGraphFromFile(fileName);
    sccs = StronglyConnectedComponents.getStronglyConnectedComponents(graph);

    System.out.println("Stage 2: Mapper Stage");
    for (SCC<Integer> scc : sccs) {
      scc.computeInternalDistances();
      System.out.println(scc);
    }

    System.out.println("Stage 3: Reduce");
    contracted = StronglyConnectedComponents.contractSCCs(sccs);
    Node<Integer> start = graph.getNode(s);
    Node<Integer> end = graph.getNode(e);

    result = PathFinder.dagTraversal(graph, contracted,
        sccs.get(start.getSccId()).getExpandedNodes().get(0).getValue(),
        sccs.get(end.getSccId()).getExpandedNodes().get(0).getValue(), s, e);

    System.out.println("MapReduce Completed");
    System.out.println("MapReduce Results: ");
    System.out.println("\tNumber of Paths: " + result[0]);
    System.out.println("\tAverage length of paths: " + result[1]);
    result = PathApproximation.lengthDistribution(graph, s, e);
    System.out.println("Estimation Results: ");
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

  private static void nonMRCode() {
    Graph<Integer> graph;
    List<SCC<Integer>> sccs;
    graph = generateGraphWithSCC();
    System.out.println("Original Graph");
    System.out.println(graph);

    sccs = StronglyConnectedComponents.getStronglyConnectedComponents(graph);
    System.out.println("Number of SCCs: " + sccs.size());
    System.out.println();
    System.out.println("SCCs:");

    for (SCC<Integer> scc : sccs) {
      System.out.println(scc);
      System.out.println();
    }

    Graph<Integer> contracted
        = StronglyConnectedComponents.contractSCCs(sccs);
    System.out.println("Contracted Graph:");
    System.out.println(contracted);

    Map<Node<Integer>, Double> likelihood = new HashMap<>();
    Map<Node<Integer>, Integer> counter = new HashMap<>(); 
    System.out.println(PathApproximation.naivePathGeneration(graph, likelihood, counter, 1, 5));
    System.out.println("Counter:");
    System.out.println(counter);
    System.out.println("Likelihood:");
    System.out.println(likelihood);

    double[] result = PathApproximation.lengthDistribution(graph, 1, 5);
    System.out.println(result[0]);
    System.out.println(result[1]);

    result = PathFinder.dagTraversal(graph, contracted, 2, 5, 1, 5);
    System.out.println("Cool");
    System.out.println(result[0]);
    System.out.println(result[1]);
  }

  private static Graph<Integer> generateGraphWithSCC() {
    Graph<Integer> graph = new Graph<>();
    graph.addEdge(1, 3);
    graph.addEdge(3, 2);
    graph.addEdge(2, 1);
    graph.addEdge(1, 4);
    graph.addEdge(4, 5);
    return graph;
  }

  private static Graph<Integer> generateGraph(int size, double density) {
    int maxEdges = (int) ((0.5 * density * (double) (size * (size - 1)))
        + Math.random());
    List<Point> preList = new ArrayList<>();
    for (int i = 1; preList.size() <= maxEdges; ++i) {
      int j;
      for (j = 1; j <= i; j++) {
        if (Math.random() < 0.50) {
          preList.add(new Point(i, j));
        }
      }
      for (; j <= size; j++) {
        if (Math.random() < density) {
          preList.add(new Point(i, j));
        }
      }
    }

    Graph<Integer> graph = new Graph<>();
    for (Point p : preList) {
      graph.addEdge(p.x, p.y);
    }
    return graph;
  }
}
