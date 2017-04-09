package com.uniquepaths.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SuperNode<T> extends Node<T> {

  private Set<Node<T>> contractedNodes;
  public final SCC<T> scc;
  private int sccNumber;
  protected Map<T, Integer> outEdges;
  protected Map<T, Map<T, Integer>> weights;

  public SuperNode(int sccNumber, SCC<T> scc) {
    super(scc.getExpandedNodes().get(0).getValue());
    this.contractedNodes = new HashSet<>();
    this.outEdges = new HashMap<>();
    this.weights = new HashMap<>();
    this.scc = scc;
    this.sccNumber = sccNumber;
  }

  public int getSccNumber() {
    return sccNumber;
  }

  public void incrementEdgeCount(T superNodeVal) {
    if (!outEdges.containsKey(superNodeVal)) {
      outEdges.put(superNodeVal, 1);
    } else {
      outEdges.put(superNodeVal, outEdges.get(superNodeVal) + 1);
    }
  }

  public int getEdgeCount(T superNodeVal) {
    return outEdges.get(superNodeVal);
  }

  public void addEdgeWeight(T superNodeVal, T nodeVal, int w) {
    Map<T, Integer> weight;
    if (!weights.containsKey(superNodeVal)) {
      weight = new HashMap<>();
      weight.put(nodeVal, w);
      weights.put(superNodeVal, weight);
    } else {
      weight = weights.get(superNodeVal);
      weight.put(nodeVal, w);
    }
  }

  public Map<T, Integer> getEdgeWeights(T superNodeVal) {
    return weights.get(superNodeVal);
  }
}
