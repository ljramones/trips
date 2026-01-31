#!/bin/bash
# Wrapper script to run Maven with Java 25
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home
exec mvn "$@"
