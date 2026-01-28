package com.teamgannon.trips.service.problemreport;

import com.teamgannon.trips.jpa.model.AppRegistration;
import com.teamgannon.trips.jpa.repository.AppRegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Startup runner that retries uploading pending problem reports.
 * Runs after application startup with a delay to avoid impacting initial load.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PendingReportRetryRunner implements ApplicationRunner {

    private static final Duration STARTUP_DELAY = Duration.ofSeconds(10);
    private static final Duration RETRY_INTERVAL = Duration.ofMinutes(5);

    private final ReportBundleService bundleService;
    private final DropboxUploadService uploadService;
    private final AppRegistrationRepository registrationRepository;

    @Value("${problemreport.enabled:true}")
    private boolean enabled;

    @Value("${problemreport.pendingReportMaxDays:30}")
    private int pendingReportMaxDays;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "PendingReportRetry");
        t.setDaemon(true);
        return t;
    });

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            log.debug("Problem report feature is disabled, skipping pending report retry");
            return;
        }

        // Schedule retry after startup delay
        scheduler.schedule(this::retryPendingReports, STARTUP_DELAY.toSeconds(), TimeUnit.SECONDS);
    }

    /**
     * Attempts to upload all pending reports.
     */
    private void retryPendingReports() {
        log.debug("Checking for pending problem reports...");

        Path pendingDir = bundleService.getPendingDirectory();
        if (!Files.exists(pendingDir)) {
            log.debug("No pending reports directory");
            return;
        }

        Optional<AppRegistration> registration = registrationRepository.findById(
                AppRegistration.REGISTRATION_ID);
        if (registration.isEmpty()) {
            log.debug("No user registration, skipping retry");
            return;
        }

        String installId = registration.get().getInstallId();
        List<Path> pendingReports = findPendingReports(pendingDir);

        if (pendingReports.isEmpty()) {
            log.debug("No pending reports to retry");
            return;
        }

        log.info("Found {} pending report(s) to retry", pendingReports.size());

        int uploaded = 0;
        int expired = 0;
        int failed = 0;

        Instant cutoff = Instant.now().minus(Duration.ofDays(pendingReportMaxDays));

        for (Path report : pendingReports) {
            try {
                Instant fileTime = Files.getLastModifiedTime(report).toInstant();

                if (fileTime.isBefore(cutoff)) {
                    // Report is too old, delete it
                    log.info("Deleting expired pending report: {}", report.getFileName());
                    Files.deleteIfExists(report);
                    expired++;
                    continue;
                }

                // Try to upload
                boolean success = uploadService.uploadReport(report, installId);
                if (success) {
                    bundleService.markAsSent(report);
                    uploaded++;
                    log.info("Successfully uploaded pending report: {}", report.getFileName());
                } else {
                    failed++;
                    log.warn("Failed to upload pending report: {}", report.getFileName());
                }

            } catch (IOException e) {
                log.error("Error processing pending report: {}", report, e);
                failed++;
            }
        }

        log.info("Pending report retry complete: {} uploaded, {} expired, {} failed",
                uploaded, expired, failed);

        // If there are still failed reports, schedule another retry
        if (failed > 0 && uploadService.isConfigured()) {
            log.debug("Scheduling retry in {} minutes", RETRY_INTERVAL.toMinutes());
            scheduler.schedule(this::retryPendingReports, RETRY_INTERVAL.toMinutes(), TimeUnit.MINUTES);
        }
    }

    /**
     * Finds all pending report ZIP files.
     */
    private List<Path> findPendingReports(Path pendingDir) {
        List<Path> reports = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pendingDir, "RPT-*.zip")) {
            for (Path file : stream) {
                if (Files.isRegularFile(file)) {
                    reports.add(file);
                }
            }
        } catch (IOException e) {
            log.error("Failed to scan pending reports directory", e);
        }

        return reports;
    }
}
