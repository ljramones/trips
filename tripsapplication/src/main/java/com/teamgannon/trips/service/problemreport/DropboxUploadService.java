package com.teamgannon.trips.service.problemreport;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service for uploading problem reports to Dropbox.
 * Uses the Dropbox Content API for file uploads.
 */
@Slf4j
@Service
public class DropboxUploadService {

    private static final String DROPBOX_CONTENT_API = "https://content.dropboxapi.com/2/files/upload";
    private static final Duration UPLOAD_TIMEOUT = Duration.ofMinutes(2);

    @Value("${problemreport.dropbox.accessToken:}")
    private String accessToken;

    @Value("${problemreport.dropbox.uploadPath:/trips-problem-reports/}")
    private String baseUploadPath;

    @Value("${problemreport.enabled:true}")
    private boolean enabled;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(DROPBOX_CONTENT_API)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/octet-stream")
                .build();
    }

    /**
     * Checks if the upload service is properly configured.
     *
     * @return true if the service can upload files
     */
    public boolean isConfigured() {
        return enabled && accessToken != null && !accessToken.trim().isEmpty();
    }

    /**
     * Uploads a report ZIP file to Dropbox.
     *
     * @param reportFile Path to the ZIP file to upload
     * @param installId  User's installation ID for organizing uploads
     * @return true if upload succeeded
     */
    public boolean uploadReport(Path reportFile, String installId) {
        if (!isConfigured()) {
            log.warn("Dropbox upload not configured. Report saved locally: {}", reportFile);
            return false;
        }

        try {
            byte[] fileContent = Files.readAllBytes(reportFile);
            String dropboxPath = buildDropboxPath(installId, reportFile.getFileName().toString());

            log.info("Uploading report to Dropbox: {}", dropboxPath);

            String dropboxArg = String.format(
                    "{\"path\":\"%s\",\"mode\":\"add\",\"autorename\":true,\"mute\":false}",
                    dropboxPath
            );

            String response = webClient.post()
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Dropbox-API-Arg", dropboxArg)
                    .body(BodyInserters.fromValue(fileContent))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(UPLOAD_TIMEOUT)
                    .onErrorResume(WebClientResponseException.class, e -> {
                        log.error("Dropbox API error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
                        return Mono.empty();
                    })
                    .block();

            if (response != null) {
                log.info("Successfully uploaded report to Dropbox");
                log.debug("Dropbox response: {}", response);
                return true;
            } else {
                log.error("Failed to upload report to Dropbox - no response");
                return false;
            }

        } catch (IOException e) {
            log.error("Failed to read report file for upload: {}", reportFile, e);
            return false;
        } catch (Exception e) {
            log.error("Failed to upload report to Dropbox", e);
            return false;
        }
    }

    /**
     * Uploads a report asynchronously, returning a Mono for the result.
     *
     * @param reportFile Path to the ZIP file to upload
     * @param installId  User's installation ID
     * @return Mono<Boolean> indicating success
     */
    public Mono<Boolean> uploadReportAsync(Path reportFile, String installId) {
        if (!isConfigured()) {
            log.warn("Dropbox upload not configured");
            return Mono.just(false);
        }

        return Mono.fromCallable(() -> {
            try {
                return Files.readAllBytes(reportFile);
            } catch (IOException e) {
                throw new RuntimeException("Failed to read report file", e);
            }
        }).flatMap(fileContent -> {
            String dropboxPath = buildDropboxPath(installId, reportFile.getFileName().toString());

            String dropboxArg = String.format(
                    "{\"path\":\"%s\",\"mode\":\"add\",\"autorename\":true,\"mute\":false}",
                    dropboxPath
            );

            return webClient.post()
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Dropbox-API-Arg", dropboxArg)
                    .body(BodyInserters.fromValue(fileContent))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(UPLOAD_TIMEOUT)
                    .map(response -> {
                        log.info("Successfully uploaded report to Dropbox: {}", dropboxPath);
                        return true;
                    })
                    .onErrorResume(e -> {
                        log.error("Failed to upload report to Dropbox", e);
                        return Mono.just(false);
                    });
        });
    }

    /**
     * Builds the Dropbox path for a report file.
     * Format: /trips-problem-reports/<installId>/<YYYY-MM-DD>/<filename>
     */
    private String buildDropboxPath(String installId, String filename) {
        String date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String path = baseUploadPath;
        if (!path.endsWith("/")) {
            path += "/";
        }
        return path + installId + "/" + date + "/" + filename;
    }
}
