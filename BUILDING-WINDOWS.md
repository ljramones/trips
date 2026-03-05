Building TRIPS on Windows 10
============================

Prerequisites
-------------

### 1. Java 25 (Eclipse Temurin)

Download and install the **Temurin JDK 25** (x64) MSI installer:
https://adoptium.net/temurin/releases/?version=25&os=windows&arch=x64

During installation, check the option to **set JAVA_HOME**. The default
install path is `C:\Program Files\Eclipse Adoptium\jdk-25`.

If you install to a different location, edit the `JAVA_HOME` path in
`mvnw-java25.cmd` at the repository root.

Verify after install:
```cmd
java -version
```
Should show `openjdk version "25"` or similar.

### 2. Apache Maven

Download Maven 3.9+ from https://maven.apache.org/download.cgi and add
the `bin` directory to your system `PATH`.

Verify:
```cmd
mvn -version
```

### 3. Git

Download from https://git-scm.com/download/win. Use the default settings.

### 4. WiX Toolset (only for EXE/MSI installers)

Required by `jpackage` to create Windows installers.

Download **WiX 3.14+** from https://wixtoolset.org/docs/wix3/
and add its `bin` directory to your system `PATH`.

Verify:
```cmd
candle -help
```

WiX is NOT needed if you only want an APP_IMAGE (unpackaged directory).

Clone and Build
---------------

```cmd
git clone https://github.com/ljramones/trips.git
cd trips
```

### Install 3rd-party libraries

```cmd
mvnw-java25.cmd validate
```

This installs the JARs from `3rdpartylibs\` into your local Maven
repository. Only needed once (or after a clean Maven cache).

### Build the project

```cmd
mvnw-java25.cmd clean install
```

### Run from source (development)

```cmd
cd tripsapplication
..\mvnw-java25.cmd spring-boot:run
```

Packaging
---------

All packaging commands must be run from the **repository root**.

### APP_IMAGE (no installer, just a directory)

No WiX needed. Creates a standalone `TRIPS` folder:

```cmd
mvnw-java25.cmd -pl tripsapplication clean -Pjpackage-win -Djpackage.type=APP_IMAGE package
```

Output: `tripsapplication\target\jpackage\TRIPS\`

Run it: `tripsapplication\target\jpackage\TRIPS\TRIPS.exe`

### EXE installer

Requires WiX Toolset.

```cmd
bin\build-windows-exe.cmd
```

Or manually:
```cmd
mvnw-java25.cmd -pl tripsapplication clean -Pjpackage-win -Djpackage.type=EXE -Djpackage.appVersion=1.0.0 package
```

Output: `tripsapplication\target\jpackage\TRIPS-1.0.0.exe`

### MSI installer

Requires WiX Toolset.

```cmd
bin\build-windows-msi.cmd
```

Output: `tripsapplication\target\jpackage\TRIPS-1.0.0.msi`

Troubleshooting
---------------

### "Java 25 not found" error from mvnw-java25.cmd

Edit `mvnw-java25.cmd` and change the `JAVA_HOME` path to match your
actual Temurin 25 install location.

### Build fails with "release version 25 not supported"

Your Maven is picking up an older JDK. Make sure `JAVA_HOME` points to
Java 25 and that no older Java is shadowing it on `PATH`:

```cmd
echo %JAVA_HOME%
java -version
```

### jpackage fails with "Can not find WiX tools"

Install WiX Toolset and ensure `candle.exe` is on your `PATH`:

```cmd
set PATH=%PATH%;C:\Program Files (x86)\WiX Toolset v3.14\bin
```

### OutOfMemoryError during build

Increase Maven's heap:

```cmd
set MAVEN_OPTS=-Xmx2g
mvnw-java25.cmd clean install
```

### Application starts but shows blank/black window

JavaFX issue. Try adding to the jpackage `<javaOptions>`:

```
-Dprism.order=sw
```

This forces software rendering. If that works, the issue is your GPU driver.

### H2 database errors on first run

The `data\` directory is created automatically. If you see lock errors,
make sure no other TRIPS instance is running. To reset the database,
delete the files in the `data\` directory.
