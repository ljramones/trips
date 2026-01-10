# Building TRIPS

This document describes how to build TRIPS (Terran Republic Interstellar Plotting System) for different target platforms.

## Prerequisites

### Java 17
This project requires **Java 17**. The system may have other Java versions installed, but the project will not compile with versions other than 17.

```bash
# Verify Java 17 is available
/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home/bin/java -version
```

### Maven
The project includes Maven wrapper scripts. No separate Maven installation is required.

## Building the Application JAR

### Using the Java 17 Wrapper Script (Recommended)

From the repository root:

```bash
# Build the entire project
./mvnw-java17.sh clean install

# Build without running tests
./mvnw-java17.sh clean install -DskipTests
```

### Manual Java 17 Configuration

If you prefer to set JAVA_HOME manually:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
mvn clean install
```

The built JAR will be located at:
```
tripsapplication/target/trips-<version>.jar
```

## Building Native Packages with jpackage

The project includes a Maven profile for building native application packages using jpackage.

### macOS (.app bundle)

```bash
# Build macOS app image
./mvnw-java17.sh -pl tripsapplication clean -Pjpackage package
```

Output: `tripsapplication/target/jpackage/TRIPS.app`

To create a DMG installer instead of an app image:

```bash
./mvnw-java17.sh -pl tripsapplication clean -Pjpackage package -Djpackage.type=DMG
```

Output: `tripsapplication/target/jpackage/TRIPS-1.0.0.dmg`

### Windows (.exe)

Build on a Windows machine:

```cmd
REM Set JAVA_HOME to Java 17
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.x-hotspot

REM Build app image (directory with exe)
mvnw.cmd -pl tripsapplication clean -Pjpackage package -Djpackage.type=APP_IMAGE

REM Or build MSI installer
mvnw.cmd -pl tripsapplication clean -Pjpackage package -Djpackage.type=MSI

REM Or build EXE installer
mvnw.cmd -pl tripsapplication clean -Pjpackage package -Djpackage.type=EXE
```

**Windows Requirements:**
- Java 17 JDK with jpackage
- For MSI: [WiX Toolset 3.0+](https://wixtoolset.org/) must be installed and on PATH
- For EXE: [WiX Toolset 3.0+](https://wixtoolset.org/) must be installed and on PATH

### Linux (.deb / .rpm)

Build on a Linux machine:

```bash
# Set JAVA_HOME to Java 17
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk

# Build app image (directory structure)
./mvnw -pl tripsapplication clean -Pjpackage package -Djpackage.type=APP_IMAGE

# Or build DEB package (Debian/Ubuntu)
./mvnw -pl tripsapplication clean -Pjpackage package -Djpackage.type=DEB

# Or build RPM package (RHEL/Fedora/CentOS)
./mvnw -pl tripsapplication clean -Pjpackage package -Djpackage.type=RPM
```

**Linux Requirements:**
- Java 17 JDK with jpackage
- For DEB: `fakeroot` package must be installed
- For RPM: `rpm-build` package must be installed

## Package Types Summary

| Type | Platform | Description |
|------|----------|-------------|
| `APP_IMAGE` | All | Directory with application (no installer) |
| `DMG` | macOS | Disk image installer |
| `PKG` | macOS | PKG installer |
| `EXE` | Windows | EXE installer (requires WiX) |
| `MSI` | Windows | MSI installer (requires WiX) |
| `DEB` | Linux | Debian package |
| `RPM` | Linux | RPM package |

## Customizing the Build

### Application Name
Edit `pom.xml` property:
```xml
<jpackage.name>TRIPS</jpackage.name>
```

### Application Version
The jpackage version is set to `1.0.0` in the pom.xml because macOS requires the first version component to be non-zero. To change it, edit the `<appVersion>` in the jpackage plugin configuration.

### Adding an Application Icon

#### macOS
Add to jpackage configuration in `pom.xml`:
```xml
<icon>${project.basedir}/src/main/resources/icons/trips.icns</icon>
```

#### Windows
```xml
<icon>${project.basedir}/src/main/resources/icons/trips.ico</icon>
```

#### Linux
```xml
<icon>${project.basedir}/src/main/resources/icons/trips.png</icon>
```

### Adding File Associations

To associate file types with the application, add to the jpackage configuration:
```xml
<fileAssociations>
    <fileAssociation>${project.basedir}/src/main/resources/file-associations/trips-files.properties</fileAssociation>
</fileAssociations>
```

## Cross-Platform Build Notes

**Important:** jpackage can only build packages for the platform it runs on. You cannot build a Windows installer on macOS or vice versa.

To build for all platforms, you need to:
1. Build on macOS for `.app` / `.dmg` / `.pkg`
2. Build on Windows for `.exe` / `.msi`
3. Build on Linux for `.deb` / `.rpm`

Consider using CI/CD (GitHub Actions, GitLab CI, etc.) with runners for each platform to automate multi-platform builds.

## Troubleshooting

### "Module javafx.controls not found"
This error occurs if `<addModules>` is configured in the jpackage plugin. The Spring Boot fat jar includes JavaFX on the classpath, not as JPMS modules. Remove any `<addModules>` configuration.

### "The first number in an app-version cannot be zero"
macOS jpackage requires version numbers starting with a non-zero integer. Change `<appVersion>` from something like `0.7.1` to `1.0.0` or similar.

### "Application destination directory already exists"
Clean the target directory before building:
```bash
./mvnw-java17.sh -pl tripsapplication clean -Pjpackage package
```

### Build fails with file path too long
Ensure the `<input>` directory only contains the JAR file, not the entire target directory. The current configuration uses a staging directory (`jpackage-input`) to avoid this issue.

## Running the Application

### From JAR
```bash
./mvnw-java17.sh -pl tripsapplication spring-boot:run
```

Or directly:
```bash
java -jar tripsapplication/target/trips-0.7.1.jar
```

### From Native Package

#### macOS
Double-click `TRIPS.app` or:
```bash
open tripsapplication/target/jpackage/TRIPS.app
```

#### Windows
Run `TRIPS.exe` from the installation directory.

#### Linux
Run the installed binary or from the app-image directory:
```bash
./TRIPS/bin/TRIPS
```
