@echo off
setlocal

call mvnw.cmd -pl tripsapplication clean -Pjpackage -Djpackage.type=MSI package

endlocal
