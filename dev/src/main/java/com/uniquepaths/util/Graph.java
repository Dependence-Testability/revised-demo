package com.uniquepaths.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class Graph<T> {

  protected Map<T, Node<T>> nodeMap;

  public Graph() {
    nodeMap = new HashMap<>();
  }

  public void addEdge(T s, T e) {
    addEdge(s, e, 1);
  }

  public void addEdge(T s, T e, int weight) {
    Node<T> start = getOrAddNode(s);
    Node<T> end = getOrAddNode(e);
    if (!start.hasEdge(end)) {
      start.addEdge(end, 1);
    }
  }

  public boolean edgeExists(T s, T e) {
    boolean contains = false;
    Node<T> start = nodeMap.get(s);
    Node<T> end = nodeMap.get(e);
    if (start != null && end != null) {
      contains = start.hasEdge(end);
    }
    return contains;
  }

  public int size() {
    return nodeMap.size();
  }

  public Node<T> getNode(T v) {
    return nodeMap.get(v);
  }

  public Set<Map.Entry<T, Node<T>>> getNodes() {
    return nodeMap.entrySet();
  }

  protected Map<T, Node<T>> getNodeMap() {
    return nodeMap;
  }

  protected Node<T> getOrAddNode(T v) {
    Node<T> node;
    if (nodeMap.containsKey(v)) {
      node = nodeMap.get(v);
    } else {
      node = new Node<>(v);
      nodeMap.put(v, node);
    }
    return node;
  }

  public boolean containsNode(T value) {
    return nodeMap.containsKey(value);
  }

  protected boolean addSuperNode(int sccNum, SCC<T> scc) {
    SuperNode<T> node = new SuperNode<T>(sccNum, scc);
    boolean added = false;
    T value = (T) node.getValue();
    if (!nodeMap.containsKey(value)) {
      nodeMap.put(value, node);
      added = true;
    }
    return added;
  }

  public List<Edge<T>> getGraphAsEdgeList() {
    List<Edge<T>> list = new ArrayList<>();
    Node<T> node;
    for (Map.Entry<T, Node<T>> nodeInfo : nodeMap.entrySet()) {
      node = nodeInfo.getValue();
      for (Map.Entry<Node<T>, Integer> edge : node.getEdges()) {
        list.add(new Edge<T>(node.getValue(), edge.getKey().getValue(),
            edge.getValue()));
      }
    }
    return list;
  }

  public String toString() {
    StringBuilder strBldr = new StringBuilder();
    Node<T> node;
    Node<T> adj;
    for (Map.Entry<T, Node<T>> nodeInfo : nodeMap.entrySet()) {
      node = nodeInfo.getValue();
      strBldr.append(nodeInfo.getKey());
      strBldr.append(" :");
      for (Map.Entry<Node<T>, Integer> edge : node.getEdges()) {
        adj = edge.getKey();
        strBldr.append(" ");
        strBldr.append(adj.getValue());
      }
      strBldr.append("\n");
    }
    return strBldr.toString();
  }
}
