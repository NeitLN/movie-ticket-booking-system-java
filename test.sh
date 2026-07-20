#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

SOURCE_LIST="$(mktemp)"
TEST_SOURCE_LIST="$(mktemp)"
trap 'rm -f "$SOURCE_LIST" "$TEST_SOURCE_LIST"' EXIT

echo "[1/4] Cleaning old test build..."
rm -rf out test-out
mkdir -p out test-out

echo "[2/4] Compiling application and test sources..."
find src -name '*.java' -print > "$SOURCE_LIST"
find tests -name '*.java' -print > "$TEST_SOURCE_LIST"
javac -encoding UTF-8 -d out @"$SOURCE_LIST"
javac -encoding UTF-8 -cp out -d test-out @"$TEST_SOURCE_LIST"

echo "[3/4] Running business-rule regression tests..."
java -Dfile.encoding=UTF-8 -cp out:test-out movieticketbooking.tests.BookingSystemRegressionTest

echo "[4/4] Running Swing integration smoke test..."
if command -v xvfb-run >/dev/null 2>&1; then
    xvfb-run -a java -Dfile.encoding=UTF-8 -cp out:test-out movieticketbooking.tests.GuiSmokeTest
else
    java -Dfile.encoding=UTF-8 -cp out:test-out movieticketbooking.tests.GuiSmokeTest
fi

echo "All tests passed."
