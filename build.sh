#!/bin/bash
mkdir -p bin
javac -d bin src/treevisualizer/*.java
jar -cmf Manifest.mf  TreeVisualizer.jar bin/treevisualizer
