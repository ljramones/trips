#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

export JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home}"

STAMP="$(date +%Y%m%d-%H%M%S)"
JFR_DIR="${JFR_DIR:-$ROOT_DIR/tripsapplication/target/jfr}"
JFR_FILE="${JFR_FILE:-$JFR_DIR/trips-release-$STAMP.jfr}"
JFR_SETTINGS="${JFR_SETTINGS:-profile}"
JFR_DELAY="${JFR_DELAY:-10s}"
JFR_MAXAGE="${JFR_MAXAGE:-45m}"
JFR_MAXSIZE="${JFR_MAXSIZE:-768m}"
LOG_FILE="${LOG_FILE:-$JFR_DIR/trips-jfr-$STAMP.log}"

mkdir -p "$JFR_DIR"

JFR_ARGS="-XX:StartFlightRecording=name=trips-release,filename=$JFR_FILE,settings=$JFR_SETTINGS,dumponexit=true,delay=$JFR_DELAY,maxage=$JFR_MAXAGE,maxsize=$JFR_MAXSIZE"
APP_ARGS="$JFR_ARGS -Dtrips.logFile=$LOG_FILE"

echo "Starting TRIPS with Java Flight Recorder"
echo "JAVA_HOME: $JAVA_HOME"
echo "JFR file:  $JFR_FILE"
echo "Log file:  $LOG_FILE"
echo
echo "Close the app normally to flush the recording, or run:"
echo "  $JAVA_HOME/bin/jcmd <pid> JFR.dump name=trips-release filename=$JFR_FILE"
echo

exec ./mvnw-java25.sh -pl tripsapplication -am spring-boot:run \
  -Dspring-boot.run.jvmArguments="$APP_ARGS"
