package com.teamgannon.trips.config.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Centralizes platform-specific application directories and exposes them as system properties.
 */
public final class TripsApplicationPaths {

    public static final String HOME_PROPERTY = "trips.home";
    public static final String DATA_DIRECTORY_PROPERTY = "trips.dataDirectory";
    public static final String FILES_DIRECTORY_PROPERTY = "trips.filesDirectory";
    public static final String PROGRAM_DATA_DIRECTORY_PROPERTY = "trips.programDataDirectory";
    public static final String SCRIPT_DIRECTORY_PROPERTY = "trips.scriptDirectory";
    public static final String REPORTS_DIRECTORY_PROPERTY = "trips.reportsDirectory";
    public static final String LOG_DIRECTORY_PROPERTY = "trips.logDirectory";
    public static final String LOG_FILE_PROPERTY = "trips.logFile";

    private static final String APP_NAME = "TRIPS";
    private static final String LOG_FILE_NAME = "terranrepublicviewer.log";

    private TripsApplicationPaths() {
    }

    public static Path appHome() {
        return pathFromProperty(HOME_PROPERTY, defaultAppHome());
    }

    public static Path dataDirectory() {
        return pathFromProperty(DATA_DIRECTORY_PROPERTY, appHome().resolve("data"));
    }

    public static Path filesDirectory() {
        return pathFromProperty(FILES_DIRECTORY_PROPERTY, appHome().resolve("files"));
    }

    public static Path programDataDirectory() {
        return pathFromProperty(PROGRAM_DATA_DIRECTORY_PROPERTY, filesDirectory().resolve("programdata"));
    }

    public static Path scriptDirectory() {
        return pathFromProperty(SCRIPT_DIRECTORY_PROPERTY, filesDirectory().resolve("scriptfiles"));
    }

    public static Path reportsDirectory() {
        return pathFromProperty(REPORTS_DIRECTORY_PROPERTY, appHome().resolve("reports"));
    }

    public static Path logDirectory() {
        return pathFromProperty(LOG_DIRECTORY_PROPERTY, defaultLogDirectory());
    }

    public static Path logFile() {
        return pathFromProperty(LOG_FILE_PROPERTY, logDirectory().resolve(LOG_FILE_NAME));
    }

    public static void initializeSystemPropertiesAndDirectories() {
        setPropertyIfAbsent(HOME_PROPERTY, appHome());
        setPropertyIfAbsent(DATA_DIRECTORY_PROPERTY, dataDirectory());
        setPropertyIfAbsent(FILES_DIRECTORY_PROPERTY, filesDirectory());
        setPropertyIfAbsent(PROGRAM_DATA_DIRECTORY_PROPERTY, programDataDirectory());
        setPropertyIfAbsent(SCRIPT_DIRECTORY_PROPERTY, scriptDirectory());
        setPropertyIfAbsent(REPORTS_DIRECTORY_PROPERTY, reportsDirectory());
        setPropertyIfAbsent(LOG_DIRECTORY_PROPERTY, logDirectory());
        setPropertyIfAbsent(LOG_FILE_PROPERTY, logFile());

        createDirectories(dataDirectory());
        createDirectories(programDataDirectory());
        createDirectories(scriptDirectory());
        createDirectories(reportsDirectory());
        createDirectories(logDirectory());
    }

    private static Path pathFromProperty(String propertyName, Path defaultPath) {
        String value = System.getProperty(propertyName);
        return (value == null || value.isBlank() ? defaultPath : Path.of(value)).toAbsolutePath().normalize();
    }

    private static void setPropertyIfAbsent(String propertyName, Path value) {
        if (System.getProperty(propertyName) == null) {
            System.setProperty(propertyName, value.toString());
        }
    }

    private static void createDirectories(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create TRIPS directory: " + directory, e);
        }
    }

    private static Path defaultAppHome() {
        String os = osName();
        if (os.contains("mac")) {
            return userHome().resolve("Library").resolve("Application Support").resolve(APP_NAME);
        }
        if (os.contains("win")) {
            return environmentPath("APPDATA", userHome().resolve("AppData").resolve("Roaming")).resolve(APP_NAME);
        }
        return environmentPath("XDG_DATA_HOME", userHome().resolve(".local").resolve("share")).resolve("trips");
    }

    private static Path defaultLogDirectory() {
        String os = osName();
        if (os.contains("mac")) {
            return userHome().resolve("Library").resolve("Logs").resolve(APP_NAME);
        }
        if (os.contains("win")) {
            return environmentPath("LOCALAPPDATA", userHome().resolve("AppData").resolve("Local"))
                    .resolve(APP_NAME)
                    .resolve("logs");
        }
        return environmentPath("XDG_STATE_HOME", userHome().resolve(".local").resolve("state"))
                .resolve("trips")
                .resolve("logs");
    }

    private static Path environmentPath(String name, Path fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : Path.of(value);
    }

    private static Path userHome() {
        return Path.of(System.getProperty("user.home"));
    }

    private static String osName() {
        return System.getProperty("os.name", "").toLowerCase();
    }
}
