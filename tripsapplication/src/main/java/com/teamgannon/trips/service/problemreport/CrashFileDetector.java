package com.teamgannon.trips.service.problemreport;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for detecting JVM crash files (hs_err_pid*.log).
 * These files are created by the JVM when it crashes due to a fatal error.
 */
@Slf4j
@Service
public class CrashFileDetector {

    @Value("${problemreport.crashfile.enabled:true}")
    private boolean crashFileDetectionEnabled;

    @Value("${problemreport.crashfile.maxAgeDays:7}")
    private int maxAgeDays;

    /**
     * Finds recent crash files in the working directory.
     *
     * @return List of paths to crash files, empty if none found or feature disabled
     */
    public List<Path> findCrashFiles() {
        List<Path> crashFiles = new ArrayList<>();

        if (!crashFileDetectionEnabled) {
            log.debug("Crash file detection is disabled");
            return crashFiles;
        }

        // Search in the current working directory
        Path workingDir = Path.of(System.getProperty("user.dir"));
        findCrashFilesInDirectory(workingDir, crashFiles);

        // Also check the user's home directory (some JVMs put crash logs there)
        Path homeDir = Path.of(System.getProperty("user.home"));
        if (!homeDir.equals(workingDir)) {
            findCrashFilesInDirectory(homeDir, crashFiles);
        }

        log.info("Found {} crash file(s)", crashFiles.size());
        return crashFiles;
    }

    /**
     * Finds crash files in a specific directory.
     */
    private void findCrashFilesInDirectory(Path directory, List<Path> result) {
        if (!Files.isDirectory(directory)) {
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "hs_err_pid*.log")) {
            Instant cutoff = Instant.now().minus(Duration.ofDays(maxAgeDays));

            for (Path file : stream) {
                try {
                    Instant fileTime = Files.getLastModifiedTime(file).toInstant();
                    if (fileTime.isAfter(cutoff)) {
                        result.add(file);
                        log.debug("Found crash file: {} (modified {})", file, fileTime);
                    } else {
                        log.debug("Skipping old crash file: {} (modified {})", file, fileTime);
                    }
                } catch (IOException e) {
                    log.warn("Failed to check file time for: {}", file, e);
                }
            }
        } catch (IOException e) {
            log.warn("Failed to scan directory for crash files: {}", directory, e);
        }
    }

    /**
     * Checks if there are any recent crash files.
     *
     * @return true if crash files exist
     */
    public boolean hasCrashFiles() {
        return !findCrashFiles().isEmpty();
    }
}
