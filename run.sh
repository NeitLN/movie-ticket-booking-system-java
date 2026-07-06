#!/usr/bin/env bash
set -e
rm -rf out
mkdir -p out
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d out @sources.txt
java -Dfile.encoding=UTF-8 -cp out com.bittersweetcinemas.ui.Main
