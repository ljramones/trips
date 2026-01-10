PACKAGING
=========

This project uses `jpackage` to build a native app image that bundles a minimal
JRE, so end users do not need to install Java separately. You must build on each
target OS/architecture (macOS, Windows, Linux).

Prerequisites
-------------
- Java 17 JDK (includes jpackage)
- Maven (system install, or use the wrapper script)

Building
--------

**Run all commands from the repository root directory.**

Build an app image (default type):

```bash
./mvnw-java17.sh -pl tripsapplication clean -Pjpackage package
```

Output: `tripsapplication/target/jpackage/TRIPS.app` (macOS)

Build a DMG installer (macOS):

```bash
./mvnw-java17.sh -pl tripsapplication clean -Pjpackage -Djpackage.type=DMG package
```

Output: `tripsapplication/target/jpackage/TRIPS-1.0.0.dmg`

Platform-Specific Builds
------------------------

Run these commands on the corresponding OS:

| Platform | Command | Output |
|----------|---------|--------|
| macOS app | `./mvnw-java17.sh -pl tripsapplication clean -Pjpackage package` | `TRIPS.app` |
| macOS DMG | `./mvnw-java17.sh -pl tripsapplication clean -Pjpackage -Djpackage.type=DMG package` | `TRIPS-1.0.0.dmg` |
| Windows EXE | `mvnw.cmd -pl tripsapplication clean -Pjpackage -Djpackage.type=EXE package` | `TRIPS-1.0.0.exe` |
| Windows MSI | `mvnw.cmd -pl tripsapplication clean -Pjpackage -Djpackage.type=MSI package` | `TRIPS-1.0.0.msi` |
| Linux DEB | `./mvnw-java17.sh -pl tripsapplication clean -Pjpackage -Djpackage.type=DEB package` | `trips_1.0.0_amd64.deb` |
| Linux RPM | `./mvnw-java17.sh -pl tripsapplication clean -Pjpackage -Djpackage.type=RPM package` | `trips-1.0.0-1.x86_64.rpm` |

Windows requires [WiX Toolset 3.0+](https://wixtoolset.org/) for EXE/MSI installers.

Notes
-----
- The `clean` target is recommended to avoid "destination already exists" errors
- Type values must be uppercase (DMG, MSI, EXE, DEB, RPM, APP_IMAGE)
- jpackage can only build packages for the OS it runs on
