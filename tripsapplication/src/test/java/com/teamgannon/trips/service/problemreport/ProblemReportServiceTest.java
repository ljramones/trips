package com.teamgannon.trips.service.problemreport;

import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.config.application.TripsApplicationPaths;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.zip.ZipFile;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemReportServiceTest {

    @TempDir
    Path tempDir;

    @AfterEach
    void clearPathProperties() {
        System.clearProperty(TripsApplicationPaths.REPORTS_DIRECTORY_PROPERTY);
        System.clearProperty(TripsApplicationPaths.LOG_FILE_PROPERTY);
    }

    @Test
    void createCrashReportCreatesPendingBundleWithMetadataAndLogTail() throws Exception {
        Path reportsDirectory = tempDir.resolve("reports");
        Path logFile = tempDir.resolve("logs").resolve("trips.log");
        Files.createDirectories(logFile.getParent());
        Files.writeString(logFile, "first line\nlast line\n", StandardCharsets.UTF_8);
        System.setProperty(TripsApplicationPaths.REPORTS_DIRECTORY_PROPERTY, reportsDirectory.toString());
        System.setProperty(TripsApplicationPaths.LOG_FILE_PROPERTY, logFile.toString());

        Localization localization = new Localization();
        localization.setVersion("test-version");
        ReportBundleService bundleService = new ReportBundleService(localization);
        ProblemReportService service = new ProblemReportService(
                null,
                bundleService,
                null,
                null,
                null,
                localization
        );
        ReflectionTestUtils.setField(service, "enabled", true);

        Optional<Path> reportPath = service.createCrashReport(new IllegalStateException("boom"));

        assertThat(reportPath).isPresent();
        assertThat(reportPath.get()).exists();
        assertThat(reportPath.get().getParent()).isEqualTo(reportsDirectory.resolve("pending"));

        try (ZipFile zipFile = new ZipFile(reportPath.get().toFile())) {
            assertThat(zipFile.getEntry("report.json")).isNotNull();
            assertThat(zipFile.getEntry("system.json")).isNotNull();
            assertThat(zipFile.getEntry("log_tail.txt")).isNotNull();

            String reportJson = new String(
                    zipFile.getInputStream(zipFile.getEntry("report.json")).readAllBytes(),
                    StandardCharsets.UTF_8
            );
            assertThat(reportJson)
                    .contains("Uncaught exception captured by TRIPS")
                    .contains("java.lang.IllegalStateException")
                    .contains("boom")
                    .contains("unregistered")
                    .contains("test-version");

            String logTail = new String(
                    zipFile.getInputStream(zipFile.getEntry("log_tail.txt")).readAllBytes(),
                    StandardCharsets.UTF_8
            );
            assertThat(logTail).contains("last line");
        }
    }
}
