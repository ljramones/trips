@echo off
setlocal

call mvnw-java25.cmd -q -pl tripsapplication -Dexpression=project.version -DforceStdout help:evaluate > "%TEMP%\trips_version.txt"
set /p VERSION=<"%TEMP%\trips_version.txt"
del "%TEMP%\trips_version.txt"
set APP_VERSION=%VERSION%
if "%APP_VERSION:~0,1%"=="0" set APP_VERSION=1%APP_VERSION:~1%

call mvnw-java25.cmd -pl tripsapplication clean -Pjpackage-win -Djpackage.type=MSI -Djpackage.appVersion=%APP_VERSION% package

endlocal
