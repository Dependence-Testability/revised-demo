package com.uniquepaths.util;

public class NodeTuple<T> {

  public final Node<T> orig;
  public final Node<T> dest;
  
  public NodeTuple(Node<T> orig, Node<T> dest) {
    this.orig = orig;
    this.dest = dest;
  }
}
