package com.uniquepaths.util;

public class GraphTuple<T> {

  public final T from;
  public final T to;
  public final Integer weight;

  public GraphTuple(T from, T to, Integer weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }

  public String toString() {
    StringBuilder strBldr = new StringBuilder();
    strBldr.append("{from : ");
    strBldr.append(from);
    strBldr.append(", to : ");
    strBldr.append(to);
    strBldr.append(", weight : ");
    strBldr.append(weight);
    strBldr.append("}");
    return strBldr.toString();
  }
}
