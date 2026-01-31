#!/bin/bash
# Wrapper script to run Maven with Java 21

set -euo pipefail

JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"

if [ ! -x "$JAVA_HOME/bin/java" ]; then
  echo "ERROR: JAVA_HOME not found or invalid: $JAVA_HOME" >&2
  exit 1
fi

export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

exec mvn "$@"
