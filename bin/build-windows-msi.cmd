@echo off
setlocal

for /f "usebackq delims=" %%v in (`mvnw.cmd -q -pl tripsapplication -Dexpression=project.version -DforceStdout help:evaluate`) do set VERSION=%%v
set APP_VERSION=%VERSION%
if "%APP_VERSION:~0,1%"=="0" set APP_VERSION=1%APP_VERSION:~1%

call mvnw.cmd -pl tripsapplication clean -Pjpackage -Djpackage.type=MSI -Djpackage.appVersion=%APP_VERSION% package

endlocal
