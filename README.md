# revised-demo

A second attempt at estimating the number paths between two nodes in a graph. 
Part of the Special Topics at Amherst College, Dependence and Testability.

## Overview
Computing the number of paths in an arbitrary graph between two nodes is a
problem in the complexity class #P. Despite this, the application of
determining the number of paths in a graph between a node, S, and another
node, T, in an arbitrary graph has useful application. For instance, in
compilation and code analysis, the execution time of a program is dependent on
the number of conditionals that it will encounter. Take for example, in the
following control flow graph (Arrows directed to a different letter indicate
different state, different portion of control):

                              A
                             / \
                            ˇ   ˇ
                            B    C
                            |  ^ |
                            ˇ /  ˇ
                            B <- C
                           / \   |
                          ˇ   ˇ  ˇ
                          D   E  C
                          |   \ /
                          ˇ    ˇ
                          D    F
                           \  /
                            ˇˇ
                             G
                             |
                             ˇ
                             H

In the control flow graphs like the one shown above, the enumeration of the
number of paths and more specifically, the calculating of the average length
of those paths would take O(n!).

### Directed Acyclic Graphs and Path Enumeration
In special cases, we are able to reduce the time complexity of calculating the
number of paths to polynomial time. One case that is taken advantage of in
this project is the properties of Directed Acyclic Graphs (DaG). When
presented with a DaG, we can perform a topological sort and use dynamic
programing to determine the path count and average path length of those
enumerated paths. For programming languages such as Java and Python, the
control flow graphs of a program will be Directed Acyclic Graphs such as 
represented below:


                              A
                             / \
                            ˇ   ˇ
                            B    C
                            |    |
                            ˇ    ˇ
                            B    C
                           / \   |
                          ˇ   ˇ  ˇ
                          D   E  C
                          |    \/
                          ˇ    ˇ
                          D    F
                           \  /
                            ˇˇ
                             G
                             |
                             ˇ
                             H

### Estimation on General Graphs
In 2007, Dr. Dirk P. Kroese and Dr. Ben Roberts published a paper describing
how to estimate the number of paths between two nodes in a graph. The
algorithm described has two stages (1) naive path generation as a means of
determining the likelihood of traversing a path by random walk, and (2) length
distribution which utilizes the probabilities computed in the first step to
determine the average length and number of steps that are likely to exist
between the two nodes.

We use the algorithm presented in [this](https://people.smp.uq.edu.au/DirkKroese/ps/robkro_rev.pdf) paper to perform any estimation on the
portions of the graphs that we consider to be strongly connected. We did find,
however, that for larger graphs, this algorithm is less likely to be close to
the correct answer.

### Our work
In this project we combine the use of path estimation with the properties of
Strongly Connected Components as they related to Directed Acyclic Graphs in
order to increase the accuracy of the estimation. The algorithm that we use
can be explained in the following steps:

    1. Break the graph down into its strongly connected components using
    Tarjan's Algorithm for computing the strongly connected components in a
    graph.
    2. Contract all the Strongly Connected Components in the graph into single
    nodes. Given the property of a Strongly Connected Component, we can say
    that after contraction, the resulting graph is a DaG.
    3. In each SCC, use the path estimation technique to determine an
    estimation of the number of paths between the set of nodes that have
    in-edges from outside of the strongly connected component and out-edges to
    other nodes in the original graph. This can be done iteratively for each
    node, but in our project we use the MapReduce Framework to compute the
    estimated values in a distributed manner.
    4. Perform a path traversal of the contracted graph (in order of DFS) to
    determine the number of paths that exist between the strongly connected
    components of the graph. As we traverse the path from the contracted graph
    we aggregate the number of paths between the number in-nodes and out-nodes
    in a graph while adding the estimated average path length between the sets.

Command to make running A LOT easier:
  - Clone Repo into directory labeled 'uniquepaths'
  - From hadoop-hdfs directory:
      cd uniquepaths/ && rm -rf up.jar input/file1 output/ com/uniquepaths/*.class com/uniquepaths/*/*.class && javac com/uniquepaths/UniquePaths.java com/uniquepaths/*/*.java -Xlint:unchecked && jar cf up.jar com/uniquepaths/UniquePaths.class com/uniquepaths/*/*.class && cd .. && hadoop jar uniquepaths/up.jar com.uniquepaths.UniquePaths uniquepaths/data/graph5.txt 1 21 uniquepaths/input/ uniquepaths/output/
