package com.uniquepaths.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Random;

public class PathApproximation {

  private static final int N_BASE = 50000;
  private static final int N_PRIME = 2000;
  private static Random generator = new Random();

  public static <T> double[] lengthDistribution(Graph<T> graph,
      T start, T end) {
    Map<Node<T>, Double> likelihoodMasterMap = new HashMap<>();
    Map<Node<T>, Integer> counterMasterMap = new HashMap<>();
    Map<Node<T>, Double> likelihoodMap;
    Map<Node<T>, Integer> counterMap;
    Double likelihood;
    // Step 1
    List<Double> pilotRun = pilotRun(graph, start, end);
    resetGraph(graph);

    double paths = 0;
    double length = 0;
    int count = 0;
    for (int i = 0; i < N_BASE; i++) {
      likelihoodMap = new HashMap<>();
      counterMap = new HashMap<>();
      Node<T> sNode = graph.getNode(start);
      Node<T> eNode = graph.getNode(end);
      int currLen = traversal(sNode, eNode, likelihoodMap, counterMap,
          pilotRun, 1.0, 1);
      if (eNode.visited()) {
        likelihood = likelihoodMap.get(eNode);
        paths += likelihoodMap.containsKey(eNode) ? 1.0/likelihood : 0.0;
        length += (double) currLen;
        ++count;
      }
    }
    length = count == 0 ? 0.0 : length/(double) count;
    return new double[]{Math.ceil(paths/(double) N_BASE), length};
  }

  private static <T> List<Double> pilotRun(Graph<T> graph, T start, T end) {
    Map<Node<T>, Double> likelihoodMap;
    Map<Node<T>, Integer> counterMap;

    List<Double> pilotList = new ArrayList<>(
        Collections.nCopies(graph.size(), 0.0));
    List<Double> numer = new ArrayList<>(
        Collections.nCopies(graph.size(), 0.0));
    List<Double> denom = new ArrayList<>(
        Collections.nCopies(graph.size(), 0.0));

    for (int i = 0; i < N_PRIME; i++) {
      likelihoodMap = new HashMap<>();
      counterMap = new HashMap<>();
      List<Node<T>> path = naivePathGeneration(graph, likelihoodMap,
          counterMap, start, end);
      Node<T> eNode = graph.getNode(end);
      int index = counterMap.containsKey(eNode) ? counterMap.get(eNode) : 0;
      index = index - 1;
      double newValue;

      if (path.get(path.size() - 1) == eNode) {
        newValue = numer.get(index) + (1.0/likelihoodMap.get(eNode));
        numer.set(index, newValue);

        for (int j = 0; j < path.size(); j++) {
          Node<T> node = path.get(j);
          newValue = denom.get(j) + (1.0/likelihoodMap.get(node));
          denom.set(j, newValue);
          node.setVisited(false);
        }
      }
    }

    for (int i = 0; i < pilotList.size(); i++) {
      double averaged = denom.get(i) == 0.0 ? 0.0 : numer.get(i)/denom.get(i);
      pilotList.set(i, averaged);
    }
    return pilotList;
  }

  /** Implementation of algorithm 1 (Naive Path Generation). */
  public static <T> List<Node<T>> naivePathGeneration(Graph<T> graph,
      Map<Node<T>, Double> likelihoodMap, Map<Node<T>, Integer> counterMap,
      T start, T end) {
    Node<T> sNode = graph.getNode(start);
    Node<T> eNode = graph.getNode(end);
    return naivePathGen(sNode, eNode, likelihoodMap, counterMap, 1.0, 1);
  }

  private static <T> List<Node<T>> naivePathGen(Node<T> start, Node<T> end,
      Map<Node<T>, Double> likelihoodMap, Map<Node<T>, Integer> counterMap,
      double likelihood, int counter) {
    List<Node<T>> path = new ArrayList<>();
    Node<T> curr = start;

    // Step 1
    likelihoodMap.put(curr, likelihood);
    counterMap.put(curr, counter);

    // Step 2
    curr.setVisited(true);

    boolean complete = false;
    while (!complete) {
      path.add(curr);

      // Step 3
      List<Node<T>> list = getUnvisited(curr);
      if (list.isEmpty()) {
        complete = true;
        continue;
      }

      // Step 4
      int index = (int) ((double) list.size() * Math.random());
      Node<T> next = list.get(index);

      // Step 5
      likelihood = likelihood/(double) list.size();
      ++counter;
      likelihoodMap.put(next, likelihood);
      counterMap.put(next, counter);
      next.setVisited(true);
      curr = next;

      // Step 6
      if (curr == end) {
        path.add(curr);
        complete = true;
      }
    }
    return path;
  }

  private static <T> int traversal(Node<T> start, Node<T> end,
      Map<Node<T>, Double> likelihoodMap, Map<Node<T>, Integer> counterMap,
      List<Double> pilotRun, double likelihood, int counter) {
    // Step 2
    Node<T> curr = start;
    Node<T> next;
    List<Node<T>> unvisited;
    likelihoodMap.put(curr, likelihood);
    counterMap.put(curr, counter);
    curr.setVisited(true);

    boolean completed = false;
    while (!completed) {
      // Step 3
      if (curr.hasEdge(end)) {
        if (curr.getEdges().size() == 1) {
          end.setVisited(true);
          likelihoodMap.put(end, likelihood);
          completed = true;
          continue;
        } else {
          double nProbability = pilotRun.get(counter - 1);
          double guessProbability = Math.random();
          if (guessProbability < nProbability) {
            likelihood = likelihood * nProbability;
            likelihoodMap.put(end, likelihood);
            end.setVisited(true);
            completed = true;
                  continue;
          } else {
            likelihood = likelihood * (1.0 - nProbability);
          }
        }
      }

      // Step 5
      unvisited = getUnvisited(curr, end);
      if (unvisited.isEmpty()) {
        completed = true;
        continue;
      }

      // Step 6
      int index = (int) ((double) unvisited.size() * Math.random());
      next = unvisited.get(index);

      // Step 7
      ++counter;
      likelihood = likelihood / (double) unvisited.size();
      likelihoodMap.put(next, likelihood);
      counterMap.put(next, counter);
      next.setVisited(true);
      curr = next;
    }
    return counter;
  }

  private static <T> List<Node<T>> getUnvisited(Node<T> curr) {
    // Processing of unvisited nodes.
    List<Node<T>> validAdj = new ArrayList<>();
    Node<T> edgeNode;
    for (Map.Entry<Node<T>, Integer> edge : curr.getEdges()) {
      edgeNode = edge.getKey();
      if (!edgeNode.visited()) {
        validAdj.add(edgeNode);
      }
    }
    return validAdj;
  }

  private static <T> List<Node<T>> getUnvisited(Node<T> curr, Node<T> end) {
    List<Node<T>> validAdj = new ArrayList<>();
    Node<T> edgeNode;
    for (Map.Entry<Node<T>, Integer> edge : curr.getEdges()) {
      edgeNode = edge.getKey();
      if (!edgeNode.visited()) {
        validAdj.add(edgeNode);
      }
    }
    return validAdj;
  }

  public static <T> void resetGraph(Graph<T> graph) {
    Node<T> node;
    for (Map.Entry<T, Node<T>> nodeInfo : graph.getNodes()) {
      node = nodeInfo.getValue();
      node.setVisited(false);
    }
  }
}
