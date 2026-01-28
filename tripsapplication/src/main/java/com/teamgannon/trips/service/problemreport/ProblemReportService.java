package com.teamgannon.trips.service.problemreport;

import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.dialogs.problemreport.AppRegistrationDialog;
import com.teamgannon.trips.dialogs.problemreport.ReportProblemDialog;
import com.teamgannon.trips.dialogs.problemreport.ReportProblemResult;
import com.teamgannon.trips.jpa.model.AppRegistration;
import com.teamgannon.trips.jpa.repository.AppRegistrationRepository;
import com.teamgannon.trips.measure.OshiMeasure;
import com.teamgannon.trips.service.problemreport.model.ReportMetadata;
import com.teamgannon.trips.service.problemreport.model.SystemInfoSnapshot;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrator service for the problem report feature.
 * Coordinates the entire flow from registration through upload.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemReportService {

    private final AppRegistrationRepository registrationRepository;
    private final ReportBundleService bundleService;
    private final DropboxUploadService uploadService;
    private final CrashFileDetector crashFileDetector;
    private final OshiMeasure oshiMeasure;
    private final Localization localization;

    @Value("${problemreport.enabled:true}")
    private boolean enabled;

    /**
     * Checks if the problem report feature is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the current user registration, if present.
     */
    public Optional<AppRegistration> getRegistration() {
        return registrationRepository.findById(AppRegistration.REGISTRATION_ID);
    }

    /**
     * Checks if the user is registered.
     */
    public boolean isRegistered() {
        return getRegistration().isPresent();
    }

    /**
     * Shows the registration dialog and saves the registration.
     *
     * @return the new registration, or empty if cancelled
     */
    public Optional<AppRegistration> showRegistrationDialog() {
        AppRegistrationDialog dialog = new AppRegistrationDialog();
        Optional<AppRegistration> result = dialog.showAndGetResult();

        result.ifPresent(registration -> {
            registrationRepository.save(registration);
            log.info("User registered for problem reporting: {}", registration.getEmail());
        });

        return result;
    }

    /**
     * Main entry point: handles the entire problem report flow.
     * Called from the Help menu.
     *
     * @param stage The primary stage for screenshot capture
     */
    public void reportProblem(Stage stage) {
        if (!enabled) {
            log.warn("Problem report feature is disabled");
            showAlert("Feature Disabled",
                    "The problem report feature is currently disabled.");
            return;
        }

        // Step 1: Ensure user is registered
        AppRegistration registration = getRegistration().orElse(null);
        if (registration == null) {
            Optional<AppRegistration> newReg = showRegistrationDialog();
            if (newReg.isEmpty()) {
                log.debug("User cancelled registration");
                return;
            }
            registration = newReg.get();
        }

        // Step 2: Check for crash files
        boolean hasCrashFiles = crashFileDetector.hasCrashFiles();

        // Step 3: Show problem report dialog
        ReportProblemDialog dialog = new ReportProblemDialog(registration, hasCrashFiles);
        Optional<ReportProblemResult> result = dialog.showAndGetResult();

        if (result.isEmpty()) {
            log.debug("User cancelled problem report");
            return;
        }

        // Step 4: Submit the report
        submitReport(stage, registration, result.get());
    }

    /**
     * Submits the problem report.
     */
    private void submitReport(Stage stage, AppRegistration registration, ReportProblemResult input) {
        try {
            String reportId = bundleService.generateReportId();
            log.info("Creating problem report: {}", reportId);

            // Capture screenshot if requested
            byte[] screenshotData = null;
            if (input.isIncludeScreenshot() && stage != null) {
                screenshotData = captureScreenshot(stage);
            }

            // Collect system info if requested
            SystemInfoSnapshot systemInfo = null;
            if (input.isIncludeSystemInfo()) {
                systemInfo = collectSystemInfo();
            }

            // Find crash files if requested
            List<Path> crashFiles = null;
            if (input.isIncludeCrashFiles()) {
                crashFiles = crashFileDetector.findCrashFiles();
            }

            // Build metadata
            ReportMetadata metadata = ReportMetadata.builder()
                    .reportId(reportId)
                    .installId(registration.getInstallId())
                    .email(registration.getEmail())
                    .displayName(registration.getDisplayName())
                    .appVersion(localization.getVersion())
                    .timestamp(Instant.now())
                    .description(input.getDescription())
                    .attachments(ReportMetadata.Attachments.builder()
                            .hasScreenshot(screenshotData != null)
                            .hasLogTail(input.isIncludeLogs())
                            .hasSystemInfo(systemInfo != null)
                            .hasCrashFiles(crashFiles != null && !crashFiles.isEmpty())
                            .build())
                    .platform(ReportMetadata.Platform.builder()
                            .os(System.getProperty("os.name"))
                            .osVersion(System.getProperty("os.version"))
                            .javaVersion(System.getProperty("java.version"))
                            .javafxVersion(System.getProperty("javafx.version"))
                            .build())
                    .build();

            // Create the bundle
            Path zipPath = bundleService.createReportBundle(
                    reportId, metadata, systemInfo, screenshotData, crashFiles);

            // Upload to Dropbox
            boolean uploaded = uploadService.uploadReport(zipPath, registration.getInstallId());

            if (uploaded) {
                // Move to sent directory
                bundleService.markAsSent(zipPath);

                // Update last report timestamp
                registration.setLastReportAt(Instant.now());
                registrationRepository.save(registration);

                showAlert("Report Submitted",
                        "Thank you for your report!\n\n" +
                                "Report ID: " + reportId + "\n" +
                                "We'll review it and follow up if needed.");
            } else {
                showAlert("Report Saved Locally",
                        "Your report has been saved and will be uploaded\n" +
                                "when connectivity is restored.\n\n" +
                                "Report ID: " + reportId);
            }

        } catch (Exception e) {
            log.error("Failed to submit problem report", e);
            showAlert("Error",
                    "Failed to create problem report:\n" + e.getMessage());
        }
    }

    /**
     * Captures a screenshot of the given stage.
     */
    private byte[] captureScreenshot(Stage stage) {
        try {
            WritableImage image = stage.getScene().snapshot(null);
            BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            return baos.toByteArray();

        } catch (Exception e) {
            log.warn("Failed to capture screenshot", e);
            return null;
        }
    }

    /**
     * Collects system information using OSHI.
     */
    private SystemInfoSnapshot collectSystemInfo() {
        try {
            return SystemInfoSnapshot.builder()
                    .osName(System.getProperty("os.name"))
                    .osVersion(System.getProperty("os.version"))
                    .osArch(System.getProperty("os.arch"))
                    .osManufacturer(oshiMeasure.getManufacturer())
                    .osBitness(oshiMeasure.getOperatingSystemInfo().getBuildNumber() != null ? 64 : 32)
                    .javaVersion(System.getProperty("java.version"))
                    .javaVendor(System.getProperty("java.vendor"))
                    .javaHome(System.getProperty("java.home"))
                    .javafxVersion(System.getProperty("javafx.version"))
                    .physicalProcessors(oshiMeasure.numberOfPhysicalProcessors())
                    .logicalProcessors(oshiMeasure.numberOfLogicalProcessors())
                    .processorName(oshiMeasure.getProcessorIdentifier().getName())
                    .totalMemoryMb(oshiMeasure.getTotalMemoryInMb())
                    .availableMemoryMb(oshiMeasure.getAvailableMemoryInMb())
                    .graphicsCards(oshiMeasure.getGraphicsCardsAsString().toString())
                    .processId(oshiMeasure.getCurrentProcessPid())
                    .uptimeMs(oshiMeasure.getCurrentOsProcess().getUpTime())
                    .threadCount(oshiMeasure.getCurrentOsProcess().getThreadCount())
                    .appVersion(localization.getVersion())
                    .workingDirectory(System.getProperty("user.dir"))
                    .build();
        } catch (Exception e) {
            log.warn("Failed to collect complete system info", e);
            // Return minimal info
            return SystemInfoSnapshot.builder()
                    .osName(System.getProperty("os.name"))
                    .osVersion(System.getProperty("os.version"))
                    .javaVersion(System.getProperty("java.version"))
                    .appVersion(localization.getVersion())
                    .build();
        }
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
