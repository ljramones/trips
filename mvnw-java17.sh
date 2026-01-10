#!/bin/bash
# Wrapper script to run Maven with Java 17
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
exec mvn "$@"
