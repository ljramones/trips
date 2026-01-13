package com.teamgannon.trips.workbench.service;

import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

@Component
@Slf4j
public class WorkbenchTapService {

    private static final String GAIA_TAP_BASE_URL = "https://gea.esac.esa.int/tap-server/tap";
    private static final String SIMBAD_TAP_BASE_URL = "https://simbad.cds.unistra.fr/simbad/sim-tap";
    private static final String VIZIER_TAP_BASE_URL = "https://tapvizier.cds.unistra.fr/TAPVizieR/tap";
    private static final long GAIA_TAP_MAX_WAIT_MS = 20 * 60 * 1000;
    private static final long GAIA_TAP_MAX_DELAY_MS = 60 * 1000;
    private static final long GAIA_TAP_INITIAL_DELAY_MS = 1000;

    private volatile boolean tapCancelRequested = false;
    private volatile String tapJobUrl;
    private volatile long tapStartMillis = 0L;
    private volatile String tapLabel = "TAP";

    public void downloadHttpFile(String url,
                                 Path outputPath,
                                 Runnable onSuccess,
                                 Consumer<String> statusConsumer,
                                 Consumer<String> errorConsumer) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
                HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(outputPath));
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IOException("HTTP " + response.statusCode());
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            if (statusConsumer != null) {
                statusConsumer.accept("Downloaded: " + outputPath.getFileName());
            }
            if (onSuccess != null) {
                onSuccess.run();
            }
        });
        task.setOnFailed(event -> {
            if (errorConsumer != null) {
                errorConsumer.accept(String.valueOf(task.getException().getMessage()));
            }
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public void downloadGaiaTapToFile(String adql,
                                      Path outputPath,
                                      Runnable onSuccess,
                                      Consumer<String> statusConsumer,
                                      Consumer<String> errorConsumer) {
        downloadTapToFile("Gaia TAP",
                GAIA_TAP_BASE_URL,
                adql,
                outputPath,
                onSuccess,
                statusConsumer,
                errorConsumer);
    }

    public void downloadSimbadTapToFile(String adql,
                                        Path outputPath,
                                        Runnable onSuccess,
                                        Consumer<String> statusConsumer,
                                        Consumer<String> errorConsumer) {
        downloadTapToFile("SIMBAD TAP",
                SIMBAD_TAP_BASE_URL,
                adql,
                outputPath,
                onSuccess,
                statusConsumer,
                errorConsumer);
    }

    public void downloadVizierTapToFile(String adql,
                                        Path outputPath,
                                        Runnable onSuccess,
                                        Consumer<String> statusConsumer,
                                        Consumer<String> errorConsumer) {
        downloadTapToFile("VizieR TAP",
                VIZIER_TAP_BASE_URL,
                adql,
                outputPath,
                onSuccess,
                statusConsumer,
                errorConsumer);
    }

    public void cancelCurrentJob(Consumer<String> statusConsumer, Consumer<String> errorConsumer) {
        tapCancelRequested = true;
        String jobUrl = tapJobUrl;
        if (jobUrl == null || jobUrl.isBlank()) {
            if (statusConsumer != null) {
                statusConsumer.accept("No TAP job to cancel.");
            }
            return;
        }
        if (statusConsumer != null) {
            statusConsumer.accept("Cancelling TAP job...");
        }
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                HttpClient client = HttpClient.newHttpClient();
                String body = "PHASE=ABORT";
                HttpRequest request = HttpRequest.newBuilder(URI.create(jobUrl + "/phase"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
                client.send(request, HttpResponse.BodyHandlers.discarding());
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            if (statusConsumer != null) {
                statusConsumer.accept("TAP job cancelled.");
            }
        });
        task.setOnFailed(event -> {
            if (errorConsumer != null) {
                errorConsumer.accept(String.valueOf(task.getException().getMessage()));
            }
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void downloadTapToFile(String label,
                                   String baseUrl,
                                   String adql,
                                   Path outputPath,
                                   Runnable onSuccess,
                                   Consumer<String> statusConsumer,
                                   Consumer<String> errorConsumer) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                tapCancelRequested = false;
                tapLabel = label;
                HttpClient client = HttpClient.newHttpClient();
                String body = "REQUEST=doQuery&LANG=ADQL&FORMAT=csv&PHASE=RUN&QUERY="
                        + URLEncoder.encode(adql, StandardCharsets.UTF_8);
                HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/async"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .build();
                HttpResponse<Void> submitResponse = client.send(request, HttpResponse.BodyHandlers.discarding());
                log.info("{} submit status: {}", label, submitResponse.statusCode());
                Optional<String> locationHeader = submitResponse.headers().firstValue("location");
                if (locationHeader.isEmpty()) {
                    throw new IOException(label + " submission failed. HTTP " + submitResponse.statusCode());
                }
                String jobUrl = locationHeader.get();
                log.info("{} job URL: {}", label, jobUrl);
                tapJobUrl = jobUrl;
                tapStartMillis = System.currentTimeMillis();
                if (statusConsumer != null) {
                    statusConsumer.accept(label + " job submitted.");
                }
                startTapJobIfPending(client, jobUrl, statusConsumer);
                waitForTapCompletion(client, jobUrl, statusConsumer);
                if (statusConsumer != null) {
                    statusConsumer.accept(label + " completed. Downloading results...");
                }
                log.info("{} downloading results to {}", label, outputPath);
                HttpRequest resultRequest = HttpRequest.newBuilder(URI.create(jobUrl + "/results/result"))
                        .GET()
                        .build();
                HttpResponse<Path> resultResponse = client.send(resultRequest, HttpResponse.BodyHandlers.ofFile(outputPath));
                log.info("{} result status: {}", label, resultResponse.statusCode());
                if (resultResponse.statusCode() < 200 || resultResponse.statusCode() >= 300) {
                    throw new IOException(label + " download failed. HTTP " + resultResponse.statusCode());
                }
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            resetTapState();
            if (onSuccess != null) {
                onSuccess.run();
            }
        });
        task.setOnFailed(event -> {
            resetTapState();
            if (errorConsumer != null) {
                errorConsumer.accept(String.valueOf(task.getException().getMessage()));
            }
        });
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void waitForTapCompletion(HttpClient client,
                                      String jobUrl,
                                      Consumer<String> statusConsumer) throws IOException, InterruptedException {
        long delayMs = GAIA_TAP_INITIAL_DELAY_MS;
        long maxDelayMs = GAIA_TAP_MAX_DELAY_MS;
        long maxWaitMs = GAIA_TAP_MAX_WAIT_MS;
        long waitedMs = 0;
        while (waitedMs < maxWaitMs) {
            if (tapCancelRequested) {
                throw new IOException("TAP job cancelled.");
            }
            HttpRequest phaseRequest = HttpRequest.newBuilder(URI.create(jobUrl + "/phase"))
                    .GET()
                    .build();
            HttpResponse<String> phaseResponse = client.send(phaseRequest, HttpResponse.BodyHandlers.ofString());
            String phase = phaseResponse.body().trim();
            log.info("TAP phase status: {} body: {}", phaseResponse.statusCode(), phase);
            if (statusConsumer != null) {
                statusConsumer.accept(tapLabel + " phase: " + phase + " (" + formatElapsed(tapStartMillis) + ")");
            }
            if ("COMPLETED".equalsIgnoreCase(phase)) {
                return;
            }
            if ("ERROR".equalsIgnoreCase(phase) || "ABORTED".equalsIgnoreCase(phase)) {
                logTapError(client, jobUrl);
                throw new IOException("TAP job " + phase);
            }
            Thread.sleep(delayMs);
            waitedMs += delayMs;
            delayMs = Math.min(delayMs * 2, maxDelayMs);
        }
        throw new IOException(tapLabel + " job timed out.");
    }

    private void startTapJobIfPending(HttpClient client,
                                      String jobUrl,
                                      Consumer<String> statusConsumer) throws IOException, InterruptedException {
        HttpRequest phaseRequest = HttpRequest.newBuilder(URI.create(jobUrl + "/phase"))
                .GET()
                .build();
        HttpResponse<String> phaseResponse = client.send(phaseRequest, HttpResponse.BodyHandlers.ofString());
        String phase = phaseResponse.body().trim();
        if (!"PENDING".equalsIgnoreCase(phase)) {
            return;
        }
        log.info("{} phase is PENDING, sending PHASE=RUN", tapLabel);
        if (statusConsumer != null) {
            statusConsumer.accept(tapLabel + " phase is PENDING, starting job...");
        }
        HttpRequest runRequest = HttpRequest.newBuilder(URI.create(jobUrl + "/phase"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("PHASE=RUN"))
                .build();
        client.send(runRequest, HttpResponse.BodyHandlers.discarding());
    }

    private void logTapError(HttpClient client, String jobUrl) {
        try {
            HttpRequest errorRequest = HttpRequest.newBuilder(URI.create(jobUrl + "/error"))
                    .GET()
                    .build();
            HttpResponse<String> errorResponse = client.send(errorRequest, HttpResponse.BodyHandlers.ofString());
            if (errorResponse.statusCode() == 303) {
                Optional<String> location = errorResponse.headers().firstValue("location");
                if (location.isPresent()) {
                    HttpRequest redirectRequest = HttpRequest.newBuilder(URI.create(location.get()))
                            .GET()
                            .build();
                    HttpResponse<String> redirectResponse = client.send(redirectRequest, HttpResponse.BodyHandlers.ofString());
                    log.error("TAP error redirect status: {} body: {}",
                            redirectResponse.statusCode(), redirectResponse.body());
                }
            } else {
                log.error("TAP error status: {} body: {}", errorResponse.statusCode(), errorResponse.body());
            }
        } catch (Exception ex) {
            log.error("Failed to read TAP error response", ex);
        }
    }

    private void resetTapState() {
        tapJobUrl = null;
        tapStartMillis = 0L;
    }

    private String formatElapsed(long startMillis) {
        if (startMillis <= 0L) {
            return "0s";
        }
        long elapsedSeconds = Math.max(0L, (System.currentTimeMillis() - startMillis) / 1000L);
        long minutes = elapsedSeconds / 60;
        long seconds = elapsedSeconds % 60;
        if (minutes > 0) {
            return minutes + "m " + seconds + "s";
        }
        return seconds + "s";
    }
}
