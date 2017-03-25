#!/usr/bin/env python

import csv
import sys

"""
Clean up script for converting the graph commandline print into
useable text files.
"""
def main():
  for i in range(1, len(sys.argv)):
    graph_list = []
    readfilename = sys.argv[i]
    writefilename = readfilename[:-4] + ".txt"
    with open(readfilename) as file:
      for row in file:
        split = row.split(":")
        origin = split[0].strip()
        destinations = split[1].split()
        for dest in destinations:
          graph_list.append(origin + " " + dest)

    with open(writefilename, 'w') as file:
      for edge in graph_list:
        file.write(edge + "\n")

if __name__ == '__main__':
  main()
