@echo off
rem Wrapper script to run Maven with Java 25
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25"
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo ERROR: Java 25 not found at %JAVA_HOME%
    echo Download from: https://adoptium.net/temurin/releases/?version=25
    echo If installed elsewhere, edit JAVA_HOME in this script.
    exit /b 1
)
mvn %*
