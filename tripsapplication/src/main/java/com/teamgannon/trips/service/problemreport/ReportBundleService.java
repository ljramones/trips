package com.teamgannon.trips.service.problemreport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.service.problemreport.model.ReportMetadata;
import com.teamgannon.trips.service.problemreport.model.SystemInfoSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service for creating problem report ZIP bundles.
 * Handles platform-specific directory paths and ZIP file creation.
 */
@Slf4j
@Service
public class ReportBundleService {

    private final Localization localization;
    private final Gson gson;

    @Value("${problemreport.logTailLines:500}")
    private int logTailLines;

    public ReportBundleService(Localization localization) {
        this.localization = localization;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .create();
    }

    /**
     * Gets the base reports directory for the current platform.
     */
    public Path getReportsBaseDirectory() {
        String os = System.getProperty("os.name", "").toLowerCase();
        Path basePath;

        if (os.contains("mac")) {
            // macOS: ~/Library/Application Support/TRIPS/reports/
            basePath = Path.of(System.getProperty("user.home"), "Library", "Application Support", "TRIPS", "reports");
        } else if (os.contains("win")) {
            // Windows: %APPDATA%\TRIPS\reports\
            String appData = System.getenv("APPDATA");
            if (appData == null) {
                appData = System.getProperty("user.home");
            }
            basePath = Path.of(appData, "TRIPS", "reports");
        } else {
            // Linux and others: ~/.trips/reports/
            basePath = Path.of(System.getProperty("user.home"), ".trips", "reports");
        }

        return basePath;
    }

    /**
     * Gets the pending reports directory.
     */
    public Path getPendingDirectory() {
        return getReportsBaseDirectory().resolve("pending");
    }

    /**
     * Gets the sent reports directory.
     */
    public Path getSentDirectory() {
        return getReportsBaseDirectory().resolve("sent");
    }

    /**
     * Ensures report directories exist.
     */
    public void ensureDirectoriesExist() throws IOException {
        Files.createDirectories(getPendingDirectory());
        Files.createDirectories(getSentDirectory());
    }

    /**
     * Generates a unique report ID.
     */
    public String generateReportId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Creates a problem report ZIP bundle.
     *
     * @param reportId       Unique report identifier
     * @param metadata       Report metadata
     * @param systemInfo     Optional system info snapshot
     * @param screenshotData Optional screenshot bytes (PNG)
     * @param crashFiles     Optional list of crash file paths
     * @return Path to the created ZIP file in the pending directory
     */
    public Path createReportBundle(
            String reportId,
            ReportMetadata metadata,
            SystemInfoSnapshot systemInfo,
            byte[] screenshotData,
            List<Path> crashFiles
    ) throws IOException {
        ensureDirectoriesExist();

        String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(
                metadata.getTimestamp().atZone(java.time.ZoneId.systemDefault()));
        String filename = String.format("RPT-%s.zip", reportId);
        Path zipPath = getPendingDirectory().resolve(filename);

        try (ZipOutputStream zos = new ZipOutputStream(
                new BufferedOutputStream(Files.newOutputStream(zipPath)))) {

            // Add report.json
            addJsonEntry(zos, "report.json", metadata);

            // Add system.json if present
            if (systemInfo != null && metadata.getAttachments().isHasSystemInfo()) {
                addJsonEntry(zos, "system.json", systemInfo);
            }

            // Add screenshot.png if present
            if (screenshotData != null && metadata.getAttachments().isHasScreenshot()) {
                addBinaryEntry(zos, "screenshot.png", screenshotData);
            }

            // Add log_tail.txt if enabled
            if (metadata.getAttachments().isHasLogTail()) {
                String logTail = readLogTail();
                if (logTail != null && !logTail.isEmpty()) {
                    addTextEntry(zos, "log_tail.txt", logTail);
                }
            }

            // Add crash files if present
            if (crashFiles != null && !crashFiles.isEmpty()) {
                for (Path crashFile : crashFiles) {
                    if (Files.exists(crashFile)) {
                        String entryName = "crash/" + crashFile.getFileName().toString();
                        addFileEntry(zos, entryName, crashFile);
                    }
                }
            }
        }

        log.info("Created problem report bundle: {}", zipPath);
        return zipPath;
    }

    /**
     * Moves a report from pending to sent directory.
     */
    public void markAsSent(Path pendingReport) throws IOException {
        Path sentPath = getSentDirectory().resolve(pendingReport.getFileName());
        Files.move(pendingReport, sentPath);
        log.debug("Moved report to sent: {}", sentPath);
    }

    /**
     * Reads the tail of the application log file.
     */
    private String readLogTail() {
        // Look for common Spring Boot log locations
        Path[] logLocations = {
                Path.of("logs", "spring.log"),
                Path.of("logs", "trips.log"),
                Path.of("spring.log"),
                Path.of("application.log")
        };

        for (Path logPath : logLocations) {
            if (Files.exists(logPath)) {
                try {
                    return readLastLines(logPath, logTailLines);
                } catch (IOException e) {
                    log.warn("Failed to read log file: {}", logPath, e);
                }
            }
        }

        log.debug("No log file found to include in report");
        return null;
    }

    /**
     * Reads the last N lines from a file.
     */
    private String readLastLines(Path file, int numLines) throws IOException {
        List<String> allLines = Files.readAllLines(file, StandardCharsets.UTF_8);
        int start = Math.max(0, allLines.size() - numLines);
        List<String> tailLines = allLines.subList(start, allLines.size());
        return String.join("\n", tailLines);
    }

    private void addJsonEntry(ZipOutputStream zos, String name, Object data) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        String json = gson.toJson(data);
        zos.write(json.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private void addTextEntry(ZipOutputStream zos, String name, String content) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        zos.write(content.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private void addBinaryEntry(ZipOutputStream zos, String name, byte[] data) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        zos.write(data);
        zos.closeEntry();
    }

    private void addFileEntry(ZipOutputStream zos, String name, Path file) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        Files.copy(file, zos);
        zos.closeEntry();
    }

    /**
     * Custom Gson adapter for Instant serialization.
     */
    private static class InstantAdapter implements com.google.gson.JsonSerializer<Instant>,
            com.google.gson.JsonDeserializer<Instant> {
        @Override
        public com.google.gson.JsonElement serialize(Instant src, java.lang.reflect.Type typeOfSrc,
                                                      com.google.gson.JsonSerializationContext context) {
            return new com.google.gson.JsonPrimitive(src.toString());
        }

        @Override
        public Instant deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT,
                                   com.google.gson.JsonDeserializationContext context) {
            return Instant.parse(json.getAsString());
        }
    }
}
