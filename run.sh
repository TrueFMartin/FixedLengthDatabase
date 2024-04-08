#!/bin/bash

echo "Franklin True Martin -- DB # 1.1"

FLAG_LOG="-Dlog4j.configurationFile=./log4j2.xml"
SRC="./src/main/java/com/github/truefmartin/**/*.java ./src/main/java/com/github/truefmartin/*.java"
JARS=""
CLASS_DIR="./out/"
RUN_CLASSPATH=":out$JARS"
MAIN="com.github.truefmartin.Main"

cd dependencies || exit
for file in *; do
    # Check if JARS is empty to avoid a leading colon
    if [ -z "$JARS" ]; then
        JARS="$file"
    else
        JARS="$JARS:$file"
    fi
done

cd ..

echo "Hello World"
echo "$JARS"

# Commands
BUILD="javac -d $CLASS_DIR -classpath "$JARS" "$SRC""
RUN="java $FLAG_LOG -classpath $RUN_CLASSPATH $MAIN"
CLEAN="rm -Rd ./out/*"


mkdir out 2>/dev/null

if [[ "$1" == "clean" ]]; then
  $CLEAN
  exit 0
fi

# If ran script with command 'build' exit now before running program
if [[ "$1" == "build" ]]; then
  # Compile the source files using log4j2 jars to the output dir, ./out/
  $BUILD
  exit 0
fi

if [[ "$1" == "run" ]]; then
  # Run with log4j2 configuration path, point to log and ORM jars and class files with Main class
  $RUN
  exit 0
fi

# If none of the above commands were passed in, run all
$BUILD
$RUN
