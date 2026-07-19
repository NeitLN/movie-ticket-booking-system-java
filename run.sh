#!/bin/bash

# Enforce UTF-8 output formatting
export LANG=en_US.UTF-8

echo "[1/3] Cleaning old build..."
rm -rf out
mkdir out

echo "[2/3] Compiling Java Swing UI..."
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d out @sources.txt
if [ $? -ne 0 ]; then
    echo ""
    echo "Compile failed. Please check your JDK installation."
    exit 1
fi

echo "[3/3] Opening Bittersweet Cinemas UI..."
java -Dfile.encoding=UTF-8 -cp out movieticketbooking.Main
