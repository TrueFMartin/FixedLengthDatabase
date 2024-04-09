#!/bin/bash

echo "Franklin True Martin -- DB # 4.0"
echo "Turing does not have Maven installed on the PATH. The packaged jar contains all needed dependencies. It can be ran on its own."
echo "To build the jar with the maven command run: "
echo "  mvn clean package"
echo "or: "
echo "  ./run.sh build"

JAR="./hw4-packed-spring-boot.jar"


# Commands
BUILD="mvn clean package"
RUN="java -jar $JAR"
CLEAN="rm $JAR"

is_mvn_installed() {
      if command -v mvn >/dev/null 2>&1; then
          return 1
      else
          return 0
      fi
}

if [[ "$1" == "clean" ]]; then
  is_mvn_installed
  if [ $? -eq 1 ]; then
      echo "Maven is installed and on the PATH. Run this program with the build command ot rebuild the jar."
  else
      echo "Maven is NOT installed or not on the PATH. Will not be able to rebuild the jar after cleaning. Exiting instead..."
      exit 1
  fi
  $CLEAN
  exit 0
fi

# If ran script with command 'build' exit now before running program
if [[ "$1" == "build" ]]; then
  is_mvn_installed
  if [ $? -eq 1 ]; then
      echo "Maven is installed and on the PATH. Building..."
  else
      echo "Maven is NOT installed or not on the PATH. Exiting..."
      exit 1
  fi
  $BUILD
  exit 0
fi

# Run the program by default or with the 'run' argument
if [[ "$1" == "run" ]] || true; then
  $RUN
  exit 0
fi

$RUN
