package com.uniquepaths.util;

import java.util.HashSet;
import java.util.Set;

public class SuperNode<T> extends Node<T> {

  private Set<Node<T>> contractedNodes;
  public final SCC<T> scc;
  private int sccNumber;

  public SuperNode(int sccNumber, SCC<T> scc) {
    super(scc.getExpandedNodes().get(0).getValue());
    this.contractedNodes = new HashSet<>();
    this.scc = scc;
    this.sccNumber = sccNumber;
  }

  public int getSccNumber() {
    return sccNumber;
  }
}
