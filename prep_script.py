#!/usr/bin/env python

import glob
import os
import shutil

"""
Script to automate the development of the mapreduce job
"""

def main():
  moveCodeFiles()
  moveDataFiles()

def moveCodeFiles():
  path = os.path.abspath('.')
  src_path = path + '/com'
  if os.path.exists(src_path):
    shutil.rmtree(src_path)
  dest_path = path + '/dev/src/main/java/com'
  shutil.copytree(dest_path, src_path)
  os.remove(src_path + '/uniquepaths/App.java')

def moveDataFiles():
  path = os.path.abspath('.')
  dest_path = path + '/data'
  if os.path.exists(dest_path):
    shutil.rmtree(dest_path)
  src_path = path + '/dev/data'
  os.makedirs(dest_path)
  files = glob.iglob(os.path.join(src_path, "graph*.txt"))
  for file in files:
      if os.path.isfile(file):
          shutil.copy2(file, dest_path)

if __name__ == '__main__':
  main()
