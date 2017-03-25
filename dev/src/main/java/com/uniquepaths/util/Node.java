package com.uniquepaths.util;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

public class Node<T> {

  private T value;
  private boolean onStack;
  private boolean visited;
  private int sccId;
  private HashMap<Node<T>, Integer> edges;
  private HashMap<Integer, Integer> distances;

  public Node(T v) {
    this.value = v;
    this.onStack = false;
    this.visited = false;
    this.edges = new HashMap<>();
    this.distances = new HashMap<>();
  }

  public void addEdge(Node<T> node, int weight) {
    edges.put(node, weight);
  }

  public boolean hasEdge(Node<T> node) {
    return edges.containsKey(node);
  }

  public T getValue() {
    return value;
  }

  public Set<Map.Entry<Node<T>, Integer>> getEdges() {
    return edges.entrySet();
  }

  public boolean onStack() {
    return onStack;
  }

  public void setOnStack(boolean onStack) {
    this.onStack = onStack;
  }

  public boolean visited() {
    return visited;
  }

  public void setSccId(int sccId) {
    this.sccId = sccId;
  }

  public int getSccId() {
    return this.sccId;
  }

  public void setVisited(boolean visited) {
    this.visited = visited;
  }

  /**
   * Adds a length to the value to the map of nodes determined by the
   * distance to a destination
   *
   */
  public void addDistance(int length) {
    if (distances.containsKey(length)) {
      distances.put(length, distances.get(length) + 1);
    } else {
      distances.put(length, 1);
    }
  }

  /**
   * Increments the values associated with the key (length)
   * for the mapping of path-length to occurrences.
   *
   */
  public void addDistance(int length, int times) {
    distances.put(length, times);
  }

  /** Retrives the path-length to occurrence mappings. */
  public Map<Integer, Integer> getDistances() {
    return distances;
  }

  /** Gets the count for a partcular distance */
  public Integer getDistanceCount(int count) {
    return distances.get(count);
  }

  public String toString() {
    StringBuilder strBldr = new StringBuilder();
    strBldr.append('<');
    strBldr.append(value);
    strBldr.append('>');
    return strBldr.toString();
  }
}
