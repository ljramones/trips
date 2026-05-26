package com.teamgannon.trips.workbench.service;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Tiny TAP (Table Access Protocol) sync client — POSTs an ADQL query to a
 * TAP endpoint and returns the CSV body, with retry-on-transient-failure
 * (HTTP 429 / 5xx / connection errors).
 * <p>
 * Extracted from {@code WorkbenchEnrichmentService} in Phase 4.3 of the
 * codebase-review remediation. Stateless utility — safe to share across
 * threads. The underlying {@link HttpClient} is a single shared instance.
 *
 * <h2>Known TAP endpoints</h2>
 * The service-side caller picks the {@code baseUrl}; this client doesn't bind
 * to a specific catalogue. The canonical endpoints in TRIPS are:
 * <ul>
 *   <li>Gaia: {@value #GAIA_TAP_BASE_URL}</li>
 *   <li>SIMBAD: {@value #SIMBAD_TAP_BASE_URL}</li>
 *   <li>VizieR (Hipparcos etc.): {@value #VIZIER_TAP_BASE_URL}</li>
 * </ul>
 */
@Slf4j
public final class TapHttpClient {

    public static final String GAIA_TAP_BASE_URL = "https://gea.esac.esa.int/tap-server/tap";
    public static final String SIMBAD_TAP_BASE_URL = "https://simbad.cds.unistra.fr/simbad/sim-tap";
    public static final String VIZIER_TAP_BASE_URL = "https://tapvizier.cds.unistra.fr/TAPVizieR/tap";

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 5000;
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .build();

    private TapHttpClient() {
    }

    /**
     * Submit an ADQL query to a TAP {@code /sync} endpoint and return the CSV
     * response body.
     *
     * @param baseUrl TAP endpoint root, e.g. {@value #GAIA_TAP_BASE_URL}.
     *                {@code "/sync"} is appended internally.
     * @param adql    the ADQL query (URL-encoded internally)
     * @param label   short label for logs (e.g. {@code "Gaia TAP"})
     * @return the CSV response body
     * @throws IOException          on permanent client error (non-429 4xx) or
     *                              after {@value #MAX_RETRIES} transient retries
     * @throws InterruptedException if the retry sleep is interrupted
     */
    public static String submitSyncCsv(String baseUrl, String adql, String label)
            throws IOException, InterruptedException {
        String body = "REQUEST=doQuery&LANG=ADQL&FORMAT=csv&QUERY="
                + URLEncoder.encode(adql, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/sync"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(REQUEST_TIMEOUT)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        IOException lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                log.info("{} sync status: {} (attempt {})", label, response.statusCode(), attempt);
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return response.body();
                }
                if (response.statusCode() == 429 || response.statusCode() >= 500) {
                    // Rate limited or transient server error — back off and retry.
                    log.warn("{} got {} on attempt {}, retrying after delay...",
                            label, response.statusCode(), attempt);
                    Thread.sleep(RETRY_DELAY_MS * attempt);
                    continue;
                }
                // Other client errors are permanent — surface and abort.
                String bodyPreview = response.body();
                if (bodyPreview != null && bodyPreview.length() > 400) {
                    bodyPreview = bodyPreview.substring(0, 400) + "...";
                }
                log.error("{} sync error body: {}", label, bodyPreview);
                throw new IOException(label + " sync failed. HTTP " + response.statusCode());
            } catch (IOException e) {
                lastException = e;
                log.warn("{} connection error on attempt {}: {} - retrying after delay...",
                        label, attempt, e.getMessage());
                if (attempt < MAX_RETRIES) {
                    Thread.sleep(RETRY_DELAY_MS * attempt);
                }
            }
        }
        log.error("{} failed after {} attempts", label, MAX_RETRIES);
        throw lastException != null ? lastException : new IOException(label + " failed after retries");
    }
}
