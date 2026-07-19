#!/bin/bash

set -u

SOURCE_LIST="sources.txt"
trap 'rm -f "$SOURCE_LIST"' EXIT

echo "[1/3] Cleaning old build..."
rm -rf out
mkdir out

echo "[2/3] Compiling Java Swing UI..."
find src -name "*.java" > "$SOURCE_LIST"
javac -encoding UTF-8 -d out @"$SOURCE_LIST"
if [ $? -ne 0 ]; then
    echo ""
    echo "Compilation failed. Review the javac errors above."
    exit 1
fi

echo "[3/3] Opening Bittersweet Cinemas UI..."
java -Dfile.encoding=UTF-8 -cp out movieticketbooking.Main
