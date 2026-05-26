package com.teamgannon.trips.workbench.service;

import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.StarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WorkbenchEnrichmentService {

    private static final String GAIA_TAP_BASE_URL = "https://gea.esac.esa.int/tap-server/tap";
    private static final String SIMBAD_TAP_BASE_URL = "https://simbad.cds.unistra.fr/simbad/sim-tap";
    private static final String VIZIER_TAP_BASE_URL = "https://tapvizier.cds.unistra.fr/TAPVizieR/tap";
    private static final Pattern DIGIT_PATTERN = Pattern.compile("(\\d+)");

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private final StarService starService;

    public WorkbenchEnrichmentService(StarService starService) {
        this.starService = starService;
    }

    public void enrichMissingDistancesLive(String dataSetName,
                                           int batchSize,
                                           long delayMs,
                                           Consumer<String> statusConsumer) throws IOException, InterruptedException {
        int pageSize = Math.max(batchSize * 4, 200);
        long updated = 0;
        long iteration = 0;
        int consecutiveNoProgress = 0;
        while (true) {
            // Always query page 0 - enriched stars disappear from results
            Page<StarObject> page = starService.findMissingDistanceWithIds(dataSetName, PageRequest.of(0, pageSize));
            if (!page.hasContent()) {
                log.info("Live TAP enrichment: no more stars with missing distance");
                break;
            }
            List<StarObject> candidates = page.getContent();
            List<StarObject> updatedStars = new ArrayList<>();
            Set<String> updatedIds = new HashSet<>();
            iteration++;
            updateStatus(statusConsumer, "Live TAP enrichment: iteration " + iteration + ", candidates " + candidates.size() + ", remaining " + page.getTotalElements());

            Map<String, List<StarObject>> gaiaMap = new HashMap<>();
            Map<String, List<StarObject>> hipMap = new HashMap<>();
            Map<String, List<StarObject>> simbadMap = new HashMap<>();
            for (StarObject star : candidates) {
                if (star.getDistance() > 0) {
                    continue;
                }
                String gaiaId = extractGaiaSourceId(star.getGaiaDR3CatId());
                if (!gaiaId.isEmpty()) {
                    gaiaMap.computeIfAbsent(gaiaId, key -> new ArrayList<>()).add(star);
                    continue;
                }
                String hipId = star.getHipCatId();
                if (hipId == null || hipId.isBlank()) {
                    hipId = extractHipId(star.getRawCatalogIdList());
                }
                hipId = extractNumericId(hipId);
                if (!hipId.isEmpty()) {
                    hipMap.computeIfAbsent(hipId, key -> new ArrayList<>()).add(star);
                }
            }

            List<String> gaiaIds = new ArrayList<>(gaiaMap.keySet());
            List<String> hipIds = new ArrayList<>(hipMap.keySet());
            int gaiaBatches = (gaiaIds.size() + batchSize - 1) / batchSize;
            int hipBatches = (hipIds.size() + batchSize - 1) / batchSize;

            for (int i = 0; i < gaiaIds.size(); i += batchSize) {
                int batchNumber = (i / batchSize) + 1;
                List<String> batch = gaiaIds.subList(i, Math.min(i + batchSize, gaiaIds.size()));
                updateStatus(statusConsumer, "Live TAP: Gaia batch " + batchNumber + "/" + gaiaBatches + " (ids " + batch.size() + ")");
                try {
                    Map<String, Double> parallaxById = fetchGaiaParallax(batch);
                    int updatedBefore = updatedStars.size();
                    for (Map.Entry<String, Double> entry : parallaxById.entrySet()) {
                        List<StarObject> stars = gaiaMap.get(entry.getKey());
                        if (stars != null) {
                            for (StarObject star : stars) {
                                if (applyParallaxEnrichment(star, entry.getValue(), "Gaia DR3 parallax",
                                        "distance from Gaia DR3 parallax") && updatedIds.add(star.getId())) {
                                    updatedStars.add(star);
                                }
                            }
                        }
                    }
                    int updatedInBatch = updatedStars.size() - updatedBefore;
                    log.info("Gaia TAP batch {}/{}: ids={}, matches={}, updated={}",
                            batchNumber, gaiaBatches, batch.size(), parallaxById.size(), updatedInBatch);
                } catch (IOException e) {
                    log.error("Gaia TAP batch {}/{} failed after retries: {} - skipping batch",
                            batchNumber, gaiaBatches, e.getMessage());
                    updateStatus(statusConsumer, "Gaia batch " + batchNumber + " failed - skipping");
                }
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            }

            for (int i = 0; i < hipIds.size(); i += batchSize) {
                int batchNumber = (i / batchSize) + 1;
                List<String> batch = hipIds.subList(i, Math.min(i + batchSize, hipIds.size()));
                updateStatus(statusConsumer, "Live TAP: HIP batch " + batchNumber + "/" + hipBatches + " (ids " + batch.size() + ")");
                try {
                    Map<String, Double> parallaxById = fetchHipParallax(batch);
                    int updatedBefore = updatedStars.size();
                    for (Map.Entry<String, Double> entry : parallaxById.entrySet()) {
                        List<StarObject> stars = hipMap.get(entry.getKey());
                        if (stars != null) {
                            for (StarObject star : stars) {
                                if (applyParallaxEnrichment(star, entry.getValue(), "HIP parallax",
                                        "distance from HIP parallax") && updatedIds.add(star.getId())) {
                                    updatedStars.add(star);
                                }
                            }
                        }
                    }
                    int updatedInBatch = updatedStars.size() - updatedBefore;
                    log.info("HIP TAP batch {}/{}: ids={}, matches={}, updated={}",
                            batchNumber, hipBatches, batch.size(), parallaxById.size(), updatedInBatch);
                } catch (IOException e) {
                    log.error("HIP TAP batch {}/{} failed after retries: {} - skipping batch",
                            batchNumber, hipBatches, e.getMessage());
                    updateStatus(statusConsumer, "HIP batch " + batchNumber + " failed - skipping");
                }
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            }

            for (StarObject star : candidates) {
                if (star.getDistance() > 0 || updatedIds.contains(star.getId())) {
                    continue;
                }
                String name = getPreferredSimbadName(star);
                if (!name.isEmpty()) {
                    String key = normalizeSimbadKey(name);
                    simbadMap.computeIfAbsent(key, value -> new ArrayList<>()).add(star);
                }
            }

            List<String> simbadNames = new ArrayList<>(simbadMap.keySet());
            int simbadBatches = (simbadNames.size() + batchSize - 1) / batchSize;
            if (!simbadNames.isEmpty()) {
                updateStatus(statusConsumer, "Live TAP: SIMBAD batches " + simbadBatches + " (names " + simbadNames.size() + ")");
            }
            for (int i = 0; i < simbadNames.size(); i += batchSize) {
                int batchNumber = (i / batchSize) + 1;
                List<String> batch = simbadNames.subList(i, Math.min(i + batchSize, simbadNames.size()));
                updateStatus(statusConsumer, "Live TAP: SIMBAD batch " + batchNumber + "/" + simbadBatches + " (names " + batch.size() + ")");
                log.info("SIMBAD TAP batch {}/{} name sample: {}",
                        batchNumber, simbadBatches, batch.subList(0, Math.min(10, batch.size())));
                try {
                    Map<String, Double> parallaxByName = fetchSimbadParallax(batch);
                    int updatedBefore = updatedStars.size();
                    int positiveParallax = 0;
                    List<String> parallaxSamples = new ArrayList<>();
                    for (Map.Entry<String, Double> entry : parallaxByName.entrySet()) {
                        Double parallax = entry.getValue();
                        if (parallax != null && parallax > 0) {
                            positiveParallax++;
                            if (parallaxSamples.size() < 5) {
                                parallaxSamples.add(entry.getKey() + "=" + parallax);
                            }
                        }
                        List<StarObject> stars = simbadMap.get(normalizeSimbadKey(entry.getKey()));
                        if (stars != null) {
                            for (StarObject star : stars) {
                                if (applyParallaxEnrichment(star, parallax, "SIMBAD parallax",
                                        "distance from SIMBAD parallax") && updatedIds.add(star.getId())) {
                                    updatedStars.add(star);
                                }
                            }
                        }
                    }
                    int updatedInBatch = updatedStars.size() - updatedBefore;
                    log.info("SIMBAD TAP batch {}/{}: names={}, matches={}, updated={}",
                            batchNumber, simbadBatches, batch.size(), parallaxByName.size(), updatedInBatch);
                    log.info("SIMBAD TAP batch {}/{}: positive parallaxes={}, samples={}",
                            batchNumber, simbadBatches, positiveParallax, parallaxSamples);
                } catch (IOException e) {
                    log.error("SIMBAD TAP batch {}/{} failed after retries: {} - skipping batch",
                            batchNumber, simbadBatches, e.getMessage());
                    updateStatus(statusConsumer, "SIMBAD batch " + batchNumber + " failed - skipping");
                }
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            }

            if (!updatedStars.isEmpty()) {
                starService.updateStars(updatedStars);
                updated += updatedStars.size();
                consecutiveNoProgress = 0;
                log.info("Live TAP enrichment: iteration {} saved {}, total updated {}",
                        iteration, updatedStars.size(), updated);
            } else {
                consecutiveNoProgress++;
                log.info("Live TAP enrichment: iteration {} no matches, consecutive no-progress: {}",
                        iteration, consecutiveNoProgress);
            }

            long remaining = starService.countMissingDistance(dataSetName);
            updateStatus(statusConsumer, "Live TAP enrichment: updated " + updated
                    + " stars, remaining " + remaining);

            // Stop if we've had too many iterations with no progress (orphan stars)
            if (consecutiveNoProgress >= 3) {
                log.info("Live TAP enrichment: stopping after {} iterations with no progress. {} orphan stars remain.",
                        consecutiveNoProgress, remaining);
                updateStatus(statusConsumer, "Enrichment complete. " + remaining + " orphan stars could not be matched.");
                break;
            }
        }
    }

    private void updateStatus(Consumer<String> statusConsumer, String message) {
        if (statusConsumer != null) {
            statusConsumer.accept(message);
        }
    }

    /**
     * Estimates distances for orphan stars using photometric methods.
     * Uses magnitude and color index to estimate absolute magnitude,
     * then calculates distance from distance modulus.
     *
     * This should be run AFTER TAP enrichment to handle stars that
     * couldn't be matched in Gaia/SIMBAD catalogs.
     */
    public void enrichOrphanDistancesPhotometric(String dataSetName,
                                                  Consumer<String> statusConsumer) {
        int pageSize = 500;
        long estimated = 0;
        long skipped = 0;
        long iteration = 0;
        // Phase 4.3: debugSkipCount / distanceRejectCount removed alongside the
        // estimator extraction — the rate-limited info logs they gated are now
        // debug-level inside StellarEstimators.

        // Count how many are eligible for photometric estimation
        long eligibleCount = starService.countMissingDistanceWithPhotometry(dataSetName);
        long totalMissing = starService.countMissingDistance(dataSetName);
        log.info("Photometric enrichment: {} stars eligible (have BPRP), {} total missing distance",
                eligibleCount, totalMissing);
        updateStatus(statusConsumer, "Found " + eligibleCount + " stars with photometry data");

        while (true) {
            // Use the photometry-specific query that returns stars with valid BPRP
            Page<StarObject> page = starService.findMissingDistanceWithPhotometry(dataSetName, PageRequest.of(0, pageSize));
            if (!page.hasContent()) {
                log.info("Photometric enrichment: no more stars with photometry data");
                break;
            }

            List<StarObject> candidates = page.getContent();
            List<StarObject> updatedStars = new ArrayList<>();
            iteration++;

            updateStatus(statusConsumer, "Photometric estimation: iteration " + iteration
                    + ", candidates " + candidates.size() + ", remaining " + page.getTotalElements());

            for (StarObject star : candidates) {
                if (star.getDistance() > 0) {
                    continue;
                }

                // Skip Sol - it's the origin reference point, distance should remain 0
                String name = star.getDisplayName();
                if (name != null && (name.equalsIgnoreCase("Sol") || name.equalsIgnoreCase("Sun"))) {
                    continue;
                }

                Double estimatedDistance = StellarEstimators.estimatePhotometricDistance(star);
                if (estimatedDistance != null && estimatedDistance > 0) {
                    star.setDistance(estimatedDistance);
                    double[] coords = StellarEstimators.calculateCoordinatesFromRaDec(star.getRa(), star.getDeclination(),estimatedDistance);
                    star.setX(coords[0]);
                    star.setY(coords[1]);
                    star.setZ(coords[2]);
                    star.setSource(appendToken(star.getSource(), "photometric estimate", "|"));
                    star.setNotes(appendToken(star.getNotes(), "distance from photometric estimation (low confidence)", "; "));
                    updatedStars.add(star);
                } else {
                    skipped++;
                }
            }

            if (!updatedStars.isEmpty()) {
                starService.updateStars(updatedStars);
                estimated += updatedStars.size();
                log.info("Photometric enrichment: iteration {} estimated {}, total {}",
                        iteration, updatedStars.size(), estimated);
            }

            long remaining = starService.countMissingDistance(dataSetName);
            updateStatus(statusConsumer, "Photometric estimation: " + estimated
                    + " estimated, " + remaining + " remaining (no photometry data)");

            if (updatedStars.isEmpty()) {
                log.info("Photometric enrichment: no more stars can be estimated. {} remain without distance.",
                        remaining);
                break;
            }
        }

        log.info("Photometric enrichment complete: estimated {}, skipped {} (insufficient data)",
                estimated, skipped);
        updateStatus(statusConsumer, "Photometric estimation complete: " + estimated
                + " stars estimated, " + skipped + " skipped (insufficient data)");
    }

    /**
     * Enriches missing stellar parameters from Gaia DR3 astrophysical_parameters table.
     * Fetches: mass, radius, luminosity, temperature, metallicity
     * Only fills in values that are currently missing/zero in the star record.
     */
    public void enrichMissingMassesFromGaia(String dataSetName,
                                             int batchSize,
                                             long delayMs,
                                             Consumer<String> statusConsumer) throws IOException, InterruptedException {
        int pageSize = Math.max(batchSize * 4, 200);
        long updated = 0;
        long iteration = 0;
        int consecutiveNoProgress = 0;

        // Count how many are eligible
        long eligibleCount = starService.countMissingMassWithGaiaId(dataSetName);
        long totalMissing = starService.countMissingMass(dataSetName);
        log.info("Stellar params enrichment: {} stars with Gaia IDs eligible, {} total missing mass",
                eligibleCount, totalMissing);
        updateStatus(statusConsumer, "Found " + eligibleCount + " stars with Gaia IDs for stellar parameters lookup");

        while (true) {
            // Always query page 0 - enriched stars disappear from results
            Page<StarObject> page = starService.findMissingMassWithGaiaId(dataSetName, PageRequest.of(0, pageSize));
            if (!page.hasContent()) {
                log.info("Stellar params enrichment: no more stars with missing mass and Gaia IDs");
                break;
            }

            List<StarObject> candidates = page.getContent();
            List<StarObject> updatedStars = new ArrayList<>();
            Set<String> updatedIds = new HashSet<>();
            iteration++;

            updateStatus(statusConsumer, "Stellar params enrichment: iteration " + iteration +
                    ", candidates " + candidates.size() + ", remaining " + page.getTotalElements());

            // Build map of Gaia ID -> stars
            Map<String, List<StarObject>> gaiaMap = new HashMap<>();
            int skippedMass = 0;
            int skippedNoId = 0;
            for (StarObject star : candidates) {
                if (star.getMass() > 0) {
                    skippedMass++;
                    continue;
                }
                String gaiaId = extractGaiaSourceId(star.getGaiaDR3CatId());
                if (!gaiaId.isEmpty()) {
                    gaiaMap.computeIfAbsent(gaiaId, key -> new ArrayList<>()).add(star);
                } else {
                    skippedNoId++;
                }
            }
            log.info("Built gaiaMap: {} unique IDs from {} candidates (skipped: {} have mass, {} no Gaia ID)",
                    gaiaMap.size(), candidates.size(), skippedMass, skippedNoId);

            // Debug: log first few extracted IDs
            List<String> gaiaIds = new ArrayList<>(gaiaMap.keySet());
            if (!gaiaIds.isEmpty()) {
                log.info("First 5 extracted Gaia IDs: {}", gaiaIds.subList(0, Math.min(5, gaiaIds.size())));
            }

            int gaiaBatches = (gaiaIds.size() + batchSize - 1) / batchSize;

            for (int i = 0; i < gaiaIds.size(); i += batchSize) {
                int batchNumber = (i / batchSize) + 1;
                List<String> batch = gaiaIds.subList(i, Math.min(i + batchSize, gaiaIds.size()));
                updateStatus(statusConsumer, "Stellar params: Gaia batch " + batchNumber + "/" + gaiaBatches +
                        " (ids " + batch.size() + ")");

                try {
                    Map<String, GaiaStellarParams> paramsById = fetchGaiaStellarParams(batch);
                    int updatedBefore = updatedStars.size();

                    for (Map.Entry<String, GaiaStellarParams> entry : paramsById.entrySet()) {
                        List<StarObject> stars = gaiaMap.get(entry.getKey());
                        if (stars != null) {
                            for (StarObject star : stars) {
                                if (applyStellarParams(star, entry.getValue()) && updatedIds.add(star.getId())) {
                                    updatedStars.add(star);
                                }
                            }
                        }
                    }

                    int updatedInBatch = updatedStars.size() - updatedBefore;
                    log.info("Gaia stellar params batch {}/{}: ids={}, matches={}, updated={}",
                            batchNumber, gaiaBatches, batch.size(), paramsById.size(), updatedInBatch);
                } catch (IOException e) {
                    log.error("Gaia stellar params batch {}/{} failed after retries: {} - skipping batch",
                            batchNumber, gaiaBatches, e.getMessage());
                    updateStatus(statusConsumer, "Gaia batch " + batchNumber + " failed - skipping");
                }

                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            }

            if (!updatedStars.isEmpty()) {
                starService.updateStars(updatedStars);
                updated += updatedStars.size();
                consecutiveNoProgress = 0;
                log.info("Stellar params enrichment: iteration {} saved {}, total updated {}",
                        iteration, updatedStars.size(), updated);
            } else {
                consecutiveNoProgress++;
                log.info("Stellar params enrichment: iteration {} no matches, consecutive no-progress: {}",
                        iteration, consecutiveNoProgress);
            }

            long remaining = starService.countMissingMassWithGaiaId(dataSetName);
            updateStatus(statusConsumer, "Stellar params enrichment: updated " + updated +
                    " stars, remaining with Gaia IDs: " + remaining);

            // Stop if we've had too many iterations with no progress
            if (consecutiveNoProgress >= 3) {
                log.info("Stellar params enrichment: stopping after {} iterations with no progress. {} stars with Gaia IDs remain.",
                        consecutiveNoProgress, remaining);
                updateStatus(statusConsumer, "Stellar params enrichment complete. " + remaining +
                        " stars with Gaia IDs could not get data.");
                break;
            }
        }

        long finalMissing = starService.countMissingMass(dataSetName);
        log.info("Stellar params enrichment complete: {} stars updated, {} still missing mass",
                updated, finalMissing);
        updateStatus(statusConsumer, "Stellar params enrichment complete: " + updated +
                " stars updated, " + finalMissing + " still missing mass");
    }

    /**
     * Container for Gaia stellar parameters.
     */
    private static class GaiaStellarParams {
        Double mass;        // solar masses
        Double radius;      // solar radii
        Double luminosity;  // solar luminosities
        Double temperature; // Kelvin
        Double metallicity; // [M/H]
    }

    /**
     * Fetches stellar parameters from Gaia DR3 astrophysical_parameters table.
     */
    private Map<String, GaiaStellarParams> fetchGaiaStellarParams(List<String> gaiaIds) throws IOException, InterruptedException {
        if (gaiaIds.isEmpty()) {
            return Map.of();
        }
        String idList = String.join(",", gaiaIds);
        String adql = "SELECT source_id, mass_flame, radius_flame, lum_flame, teff_gspphot, mh_gspphot " +
                "FROM gaiadr3.astrophysical_parameters " +
                "WHERE source_id IN (" + idList + ")";
        String csv = submitTapSyncCsv(GAIA_TAP_BASE_URL, adql, "Gaia Stellar Params TAP");

        // Debug: log first few IDs and CSV response
        if (gaiaIds.size() > 0) {
            log.info("Gaia stellar params query: first 5 IDs = {}", gaiaIds.subList(0, Math.min(5, gaiaIds.size())));
        }
        if (csv != null) {
            String[] lines = csv.split("\\r?\\n");
            log.info("Gaia stellar params response: {} lines, header = {}", lines.length, lines.length > 0 ? lines[0] : "empty");
            if (lines.length > 1 && lines.length <= 6) {
                for (int i = 1; i < lines.length; i++) {
                    log.info("  Row {}: {}", i, lines[i]);
                }
            }
        }

        return parseGaiaStellarParamsCsv(csv);
    }

    /**
     * Parses CSV response for stellar parameters.
     */
    private Map<String, GaiaStellarParams> parseGaiaStellarParamsCsv(String csv) {
        Map<String, GaiaStellarParams> map = new HashMap<>();
        if (csv == null || csv.isBlank()) {
            return map;
        }
        String[] lines = csv.split("\\r?\\n");
        if (lines.length == 0) {
            return map;
        }
        String[] header = splitCsvLine(lines[0]);
        Map<String, Integer> headerIndex = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            headerIndex.put(header[i].trim().toLowerCase(), i);
        }

        int idIdx = findHeaderIndex(headerIndex, List.of("source_id"));
        int massIdx = findHeaderIndex(headerIndex, List.of("mass_flame"));
        int radiusIdx = findHeaderIndex(headerIndex, List.of("radius_flame"));
        int lumIdx = findHeaderIndex(headerIndex, List.of("lum_flame"));
        int tempIdx = findHeaderIndex(headerIndex, List.of("teff_gspphot"));
        int metalIdx = findHeaderIndex(headerIndex, List.of("mh_gspphot"));

        if (idIdx < 0) {
            log.warn("Gaia stellar params CSV missing source_id column");
            return map;
        }

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) {
                continue;
            }
            String[] values = splitCsvLine(line);
            if (idIdx >= values.length) {
                continue;
            }

            String id = extractNumericId(values[idIdx]);
            if (id.isEmpty()) {
                continue;
            }

            GaiaStellarParams params = new GaiaStellarParams();
            if (massIdx >= 0 && massIdx < values.length) {
                params.mass = parseDoubleOrNull(values[massIdx]);
            }
            if (radiusIdx >= 0 && radiusIdx < values.length) {
                params.radius = parseDoubleOrNull(values[radiusIdx]);
            }
            if (lumIdx >= 0 && lumIdx < values.length) {
                params.luminosity = parseDoubleOrNull(values[lumIdx]);
            }
            if (tempIdx >= 0 && tempIdx < values.length) {
                params.temperature = parseDoubleOrNull(values[tempIdx]);
            }
            if (metalIdx >= 0 && metalIdx < values.length) {
                params.metallicity = parseDoubleOrNull(values[metalIdx]);
            }

            // Only add if we got at least one useful value
            if (params.mass != null || params.radius != null || params.luminosity != null ||
                    params.temperature != null || params.metallicity != null) {
                map.putIfAbsent(id, params);
            }
        }
        return map;
    }

    /**
     * Applies Gaia stellar parameters to a star, only filling in missing values.
     * Returns true if any value was updated.
     */
    private boolean applyStellarParams(StarObject star, GaiaStellarParams params) {
        boolean updated = false;
        List<String> updatedFields = new ArrayList<>();

        if (params.mass != null && params.mass > 0 && star.getMass() <= 0) {
            star.setMass(params.mass);
            updated = true;
            updatedFields.add("mass");
        }
        if (params.radius != null && params.radius > 0 && star.getRadius() <= 0) {
            star.setRadius(params.radius);
            updated = true;
            updatedFields.add("radius");
        }
        if (params.luminosity != null && params.luminosity > 0 &&
                (star.getLuminosity() == null || star.getLuminosity().isBlank())) {
            star.setLuminosity(String.valueOf(params.luminosity));
            updated = true;
            updatedFields.add("luminosity");
        }
        if (params.temperature != null && params.temperature > 0 && star.getTemperature() <= 0) {
            star.setTemperature(params.temperature);
            updated = true;
            updatedFields.add("temperature");
        }
        if (params.metallicity != null && star.getMetallicity() == 0) {
            // Metallicity can be negative, so just check if it's the default 0
            star.setMetallicity(params.metallicity);
            updated = true;
            updatedFields.add("metallicity");
        }

        if (updated) {
            star.setSource(appendToken(star.getSource(), "Gaia DR3 astrophysical", "|"));
            star.setNotes(appendToken(star.getNotes(),
                    "stellar params from Gaia DR3: " + String.join(", ", updatedFields), "; "));
        }
        return updated;
    }

    /**
     * Estimates mass photometrically for stars with distance and magnitude data.
     * Uses the mass-luminosity relation for main-sequence stars.
     */
    public void enrichMassPhotometric(String dataSetName, Consumer<String> statusConsumer) {
        int batchSize = 5000;
        long estimated = 0;
        long skipped = 0;

        // Get all eligible star IDs in one query (much faster than paginated full objects)
        updateStatus(statusConsumer, "Photometric mass estimation: fetching eligible star IDs...");
        List<String> allIds = starService.findMissingMassWithPhotometryIds(dataSetName);
        log.info("Photometric mass estimation: found {} eligible stars", allIds.size());

        if (allIds.isEmpty()) {
            updateStatus(statusConsumer, "Photometric mass estimation: no eligible stars found");
            return;
        }

        int totalBatches = (allIds.size() + batchSize - 1) / batchSize;
        updateStatus(statusConsumer, "Photometric mass estimation: processing " + allIds.size() +
                " stars in " + totalBatches + " batches");

        // Process in batches
        for (int batchNum = 0; batchNum < totalBatches; batchNum++) {
            int start = batchNum * batchSize;
            int end = Math.min(start + batchSize, allIds.size());
            List<String> batchIds = allIds.subList(start, end);

            // Load full objects for this batch
            List<StarObject> candidates = starService.findStarsByIds(batchIds);
            List<StarObject> updatedStars = new ArrayList<>();

            for (StarObject star : candidates) {
                if (star.getMass() > 0) {
                    continue;
                }

                Double mass = StellarEstimators.estimatePhotometricMass(star);
                if (mass != null && mass > 0) {
                    star.setMass(mass);

                    // Also estimate radius if missing (from mass-radius relation)
                    if (star.getRadius() <= 0) {
                        double radius = StellarEstimators.estimateRadiusFromMass(mass);
                        if (radius > 0) {
                            star.setRadius(radius);
                        }
                    }

                    // Set luminosity if we calculated it
                    if (star.getLuminosity() == null || star.getLuminosity().isBlank()) {
                        Double luminosity = StellarEstimators.calculateLuminosityFromMagnitude(star);
                        if (luminosity != null && luminosity > 0) {
                            star.setLuminosity(String.valueOf(luminosity));
                        }
                    }

                    star.setSource(appendToken(star.getSource(), "photometric mass estimate", "|"));
                    star.setNotes(appendToken(star.getNotes(), "mass/radius from photometric estimation", "; "));
                    updatedStars.add(star);
                } else {
                    skipped++;
                }
            }

            if (!updatedStars.isEmpty()) {
                starService.updateStars(updatedStars);
                estimated += updatedStars.size();
                log.info("Photometric mass estimation: batch {}/{} estimated {}, total {}",
                        batchNum + 1, totalBatches, updatedStars.size(), estimated);
            }

            int remaining = allIds.size() - end;
            updateStatus(statusConsumer, "Photometric mass estimation: " + estimated +
                    " estimated, " + remaining + " remaining (batch " + (batchNum + 1) + "/" + totalBatches + ")");
        }

        long finalMissing = starService.countMissingMass(dataSetName);
        log.info("Photometric mass estimation complete: {} estimated, {} skipped, {} still missing",
                estimated, skipped, finalMissing);
        updateStatus(statusConsumer, "Photometric mass estimation complete: " + estimated +
                " estimated, " + finalMissing + " still missing mass");
    }




    /**
     * Parses a string to Double, returning null if empty or unparseable.
     */
    private Double parseDoubleOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            double d = Double.parseDouble(value.trim());
            return Double.isNaN(d) || Double.isInfinite(d) ? null : d;
        } catch (NumberFormatException e) {
            return null;
        }
    }






    private Map<String, Double> fetchGaiaParallax(List<String> gaiaIds) throws IOException, InterruptedException {
        if (gaiaIds.isEmpty()) {
            return Map.of();
        }
        String idList = String.join(",", gaiaIds);
        String adql = "SELECT source_id, parallax FROM gaiadr3.gaia_source WHERE source_id IN (" + idList + ")";
        String csv = submitTapSyncCsv(GAIA_TAP_BASE_URL, adql, "Gaia TAP");
        return parseParallaxCsv(csv, "source_id", "parallax");
    }

    private Map<String, Double> fetchHipParallax(List<String> hipIds) throws IOException, InterruptedException {
        if (hipIds.isEmpty()) {
            return Map.of();
        }
        String idList = String.join(",", hipIds);
        String adql = "SELECT HIP, Plx FROM \"I/239/hip_main\" WHERE HIP IN (" + idList + ")";
        String csv = submitTapSyncCsv(VIZIER_TAP_BASE_URL, adql, "VizieR TAP");
        return parseParallaxCsv(csv, "HIP", "Plx");
    }

    private Map<String, Double> fetchSimbadParallax(List<String> simbadNames) throws IOException, InterruptedException {
        if (simbadNames.isEmpty()) {
            return Map.of();
        }
        String idList = simbadNames.stream()
                .map(this::escapeAdqlString)
                .map(name -> "'" + name + "'")
                .collect(Collectors.joining(","));
        String adql = "SELECT i.id AS id, b.plx_value "
                + "FROM ident i JOIN basic b ON i.oidref = b.oid "
                + "WHERE i.id IN (" + idList + ")";
        String csv = submitTapSyncCsv(SIMBAD_TAP_BASE_URL, adql, "SIMBAD TAP");
        logSimbadCsvSample(csv);
        return parseParallaxCsvRawId(csv, "id", "plx_value");
    }

    private void logSimbadCsvSample(String csv) {
        if (csv == null || csv.isBlank()) {
            log.info("SIMBAD TAP CSV sample: <empty>");
            return;
        }
        String[] lines = csv.split("\\r?\\n");
        if (lines.length > 0) {
            log.info("SIMBAD TAP CSV header: {}", lines[0]);
        }
        int printed = 0;
        List<String> sample = new ArrayList<>();
        for (int i = 1; i < lines.length && printed < 5; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                sample.add(line);
                printed++;
            }
        }
        log.info("SIMBAD TAP CSV sample rows: {}", sample);
    }

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 5000;

    private String submitTapSyncCsv(String baseUrl, String adql, String label) throws IOException, InterruptedException {
        String body = "REQUEST=doQuery&LANG=ADQL&FORMAT=csv&QUERY="
                + URLEncoder.encode(adql, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/sync"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(60))
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
                    // Rate limited or server error - retry
                    log.warn("{} got {} on attempt {}, retrying after delay...", label, response.statusCode(), attempt);
                    Thread.sleep(RETRY_DELAY_MS * attempt);
                    continue;
                }
                // Client error (4xx except 429) - don't retry
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

    private Map<String, Double> parseParallaxCsv(String csv, String idHeader, String parallaxHeader) {
        Map<String, Double> map = new HashMap<>();
        if (csv == null || csv.isBlank()) {
            return map;
        }
        String[] lines = csv.split("\\r?\\n");
        if (lines.length == 0) {
            return map;
        }
        String[] header = splitCsvLine(lines[0]);
        Map<String, Integer> headerIndex = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            headerIndex.put(header[i].trim(), i);
        }
        int idIdx = findHeaderIndex(headerIndex, List.of(idHeader));
        int parallaxIdx = findHeaderIndex(headerIndex, List.of(parallaxHeader));
        if (idIdx < 0 || parallaxIdx < 0) {
            return map;
        }
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) {
                continue;
            }
            String[] values = splitCsvLine(line);
            if (idIdx >= values.length || parallaxIdx >= values.length) {
                continue;
            }
            String id = extractNumericId(values[idIdx]);
            double parallax = parseDoubleSafe(values[parallaxIdx]);
            if (!id.isEmpty() && parallax > 0) {
                map.putIfAbsent(id, parallax);
            }
        }
        return map;
    }

    private Map<String, Double> parseParallaxCsvRawId(String csv, String idHeader, String parallaxHeader) {
        Map<String, Double> map = new HashMap<>();
        if (csv == null || csv.isBlank()) {
            return map;
        }
        String[] lines = csv.split("\\r?\\n");
        if (lines.length == 0) {
            return map;
        }
        String[] header = splitCsvLine(lines[0]);
        Map<String, Integer> headerIndex = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            headerIndex.put(unquote(header[i]).trim(), i);
        }
        int idIdx = findHeaderIndex(headerIndex, List.of(idHeader));
        int parallaxIdx = findHeaderIndex(headerIndex, List.of(parallaxHeader));
        if (idIdx < 0 || parallaxIdx < 0) {
            return map;
        }
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) {
                continue;
            }
            String[] values = splitCsvLine(line);
            if (idIdx >= values.length || parallaxIdx >= values.length) {
                continue;
            }
            String id = normalizeSimbadKey(values[idIdx]);
            double parallax = parseDoubleSafe(values[parallaxIdx]);
            if (!id.isEmpty() && parallax > 0) {
                map.putIfAbsent(id, parallax);
            }
        }
        return map;
    }

    private boolean applyParallaxEnrichment(StarObject star,
                                            double parallaxMas,
                                            String sourceToken,
                                            String notesToken) {
        if (parallaxMas <= 0 || star.getDistance() > 0) {
            return false;
        }
        double distance = StellarEstimators.calculateDistanceFromParallax(parallaxMas);
        if (distance <= 0) {
            return false;
        }
        star.setParallax(parallaxMas);
        star.setDistance(distance);
        double[] coords = StellarEstimators.calculateCoordinatesFromRaDec(star.getRa(), star.getDeclination(),distance);
        star.setX(coords[0]);
        star.setY(coords[1]);
        star.setZ(coords[2]);
        star.setSource(appendToken(star.getSource(), sourceToken, "|"));
        star.setNotes(appendToken(star.getNotes(), notesToken, "; "));
        return true;
    }

    private String getPreferredSimbadName(StarObject star) {
        String name = star.getCommonName();
        if (name == null || name.isBlank() || "NA".equalsIgnoreCase(name.trim()) || isNumericToken(name)) {
            name = star.getDisplayName();
        }
        if (name == null || name.isBlank() || isNumericToken(name)) {
            String catalogId = extractSimbadCatalogId(star.getRawCatalogIdList());
            if (catalogId != null && !catalogId.isBlank()) {
                name = catalogId;
            }
        }
        if (name == null) {
            return "";
        }
        return name.trim();
    }

    private boolean isNumericToken(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return false;
        }
        for (int i = 0; i < trimmed.length(); i++) {
            if (!Character.isDigit(trimmed.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String extractSimbadCatalogId(String catalogIdList) {
        if (catalogIdList == null || catalogIdList.isBlank()) {
            return "";
        }
        String[] tokens = catalogIdList.split("\\|");
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.startsWith("TYC ")) {
                return trimmed;
            }
        }
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.startsWith("HD ") || trimmed.startsWith("HIP ") || trimmed.startsWith("HR ")
                    || trimmed.startsWith("BD ") || trimmed.startsWith("GJ ") || trimmed.startsWith("GL ")
                    || trimmed.startsWith("LHS ") || trimmed.startsWith("2MASS ")) {
                return trimmed;
            }
        }
        return "";
    }

    private String normalizeSimbadKey(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = unquote(value).trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.replaceAll("\\s+", " ");
    }

    private String escapeAdqlString(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''");
    }

    private int findHeaderIndex(Map<String, Integer> headerIndex, List<String> candidates) {
        for (String candidate : candidates) {
            for (Map.Entry<String, Integer> entry : headerIndex.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(candidate)) {
                    return entry.getValue();
                }
            }
        }
        return -1;
    }

    private String extractNumericId(String value) {
        if (value == null) {
            return "";
        }
        Matcher matcher = DIGIT_PATTERN.matcher(value);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * Extracts Gaia source_id from strings like "Gaia DR3 531415758077608192".
     * Returns the longest numeric sequence (the actual source_id), not "3" from "DR3".
     */
    private String extractGaiaSourceId(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        // Find all digit sequences and return the longest one (the source_id)
        Matcher matcher = DIGIT_PATTERN.matcher(value);
        String longest = "";
        while (matcher.find()) {
            String match = matcher.group(1);
            if (match.length() > longest.length()) {
                longest = match;
            }
        }
        return longest;
    }

    private String extractHipId(String catalogIdList) {
        if (catalogIdList == null || catalogIdList.isBlank()) {
            return "";
        }
        String[] tokens = catalogIdList.split("\\|");
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.startsWith("HIP ")) {
                return extractNumericId(trimmed);
            }
        }
        return "";
    }



    private String appendToken(String current, String token, String separator) {
        if (token == null || token.isBlank()) {
            return current == null ? "" : current;
        }
        if (current == null || current.isBlank()) {
            return token;
        }
        if (current.contains(token)) {
            return current;
        }
        return current + separator + token;
    }

    private String[] splitCsvLine(String line) {
        if (line == null) {
            return new String[0];
        }
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    private String unquote(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            String inner = trimmed.substring(1, trimmed.length() - 1);
            return inner.replace("\"\"", "\"");
        }
        return trimmed;
    }

    private double parseDoubleSafe(String value) {
        if (value == null || value.isBlank()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // ==================== Temperature & Spectral Class Enrichment ====================

    /**
     * Enriches temperature from BP-RP color for stars missing temperature.
     * Uses polynomial fit from Gaia DR2 color-temperature relations.
     */
    public void enrichTemperatureFromBprp(String dataSetName, Consumer<String> statusConsumer) {
        int batchSize = 5000;
        long estimated = 0;
        long skipped = 0;

        updateStatus(statusConsumer, "Temperature estimation: fetching eligible star IDs...");
        List<String> allIds = starService.findMissingTemperatureWithBprpIds(dataSetName);
        log.info("Temperature estimation: found {} eligible stars", allIds.size());

        if (allIds.isEmpty()) {
            updateStatus(statusConsumer, "Temperature estimation: no eligible stars found");
            return;
        }

        int totalBatches = (allIds.size() + batchSize - 1) / batchSize;
        updateStatus(statusConsumer, "Temperature estimation: processing " + allIds.size() +
                " stars in " + totalBatches + " batches");

        for (int batchNum = 0; batchNum < totalBatches; batchNum++) {
            int start = batchNum * batchSize;
            int end = Math.min(start + batchSize, allIds.size());
            List<String> batchIds = allIds.subList(start, end);

            List<StarObject> candidates = starService.findStarsByIds(batchIds);
            List<StarObject> updatedStars = new ArrayList<>();

            for (StarObject star : candidates) {
                if (star.getTemperature() > 0) {
                    continue;
                }

                Double temp = StellarEstimators.estimateTemperatureFromBprp(star.getBprp());
                if (temp != null && temp > 0) {
                    star.setTemperature(temp);
                    star.setSource(appendToken(star.getSource(), "temp from BP-RP", "|"));
                    updatedStars.add(star);
                } else {
                    skipped++;
                }
            }

            if (!updatedStars.isEmpty()) {
                starService.updateStars(updatedStars);
                estimated += updatedStars.size();
                log.info("Temperature estimation: batch {}/{} estimated {}, total {}",
                        batchNum + 1, totalBatches, updatedStars.size(), estimated);
            }

            int remaining = allIds.size() - end;
            updateStatus(statusConsumer, "Temperature estimation: " + estimated +
                    " estimated, " + remaining + " remaining (batch " + (batchNum + 1) + "/" + totalBatches + ")");
        }

        log.info("Temperature estimation complete: {} estimated, {} skipped", estimated, skipped);
        updateStatus(statusConsumer, "Temperature estimation complete: " + estimated + " estimated");
    }

    /**
     * Enriches spectral class from BP-RP color for stars missing spectral classification.
     */
    public void enrichSpectralFromBprp(String dataSetName, Consumer<String> statusConsumer) {
        int batchSize = 5000;
        long estimated = 0;
        long skipped = 0;

        updateStatus(statusConsumer, "Spectral classification: fetching eligible star IDs...");
        List<String> allIds = starService.findMissingSpectralWithBprpIds(dataSetName);
        log.info("Spectral classification: found {} eligible stars", allIds.size());

        if (allIds.isEmpty()) {
            updateStatus(statusConsumer, "Spectral classification: no eligible stars found");
            return;
        }

        int totalBatches = (allIds.size() + batchSize - 1) / batchSize;
        updateStatus(statusConsumer, "Spectral classification: processing " + allIds.size() +
                " stars in " + totalBatches + " batches");

        for (int batchNum = 0; batchNum < totalBatches; batchNum++) {
            int start = batchNum * batchSize;
            int end = Math.min(start + batchSize, allIds.size());
            List<String> batchIds = allIds.subList(start, end);

            List<StarObject> candidates = starService.findStarsByIds(batchIds);
            List<StarObject> updatedStars = new ArrayList<>();

            for (StarObject star : candidates) {
                if (star.getSpectralClass() != null && !star.getSpectralClass().isBlank()) {
                    continue;
                }

                String spectral = StellarEstimators.estimateSpectralClassFromBprp(star.getBprp());
                if (spectral != null && !spectral.isBlank()) {
                    star.setSpectralClass(spectral);
                    star.setSource(appendToken(star.getSource(), "spectral from BP-RP", "|"));
                    updatedStars.add(star);
                } else {
                    skipped++;
                }
            }

            if (!updatedStars.isEmpty()) {
                starService.updateStars(updatedStars);
                estimated += updatedStars.size();
                log.info("Spectral classification: batch {}/{} estimated {}, total {}",
                        batchNum + 1, totalBatches, updatedStars.size(), estimated);
            }

            int remaining = allIds.size() - end;
            updateStatus(statusConsumer, "Spectral classification: " + estimated +
                    " estimated, " + remaining + " remaining (batch " + (batchNum + 1) + "/" + totalBatches + ")");
        }

        log.info("Spectral classification complete: {} estimated, {} skipped", estimated, skipped);
        updateStatus(statusConsumer, "Spectral classification complete: " + estimated + " estimated");
    }



    // ==================== Cross-fill Temperature <-> Spectral ====================

    /**
     * Cross-fills temperature from spectral class for stars that have spectral but no temperature.
     */
    public void crossFillTemperatureFromSpectral(String dataSetName, Consumer<String> statusConsumer) {
        int batchSize = 5000;
        long estimated = 0;
        long skipped = 0;

        updateStatus(statusConsumer, "Cross-fill temp from spectral: fetching eligible star IDs...");
        List<String> allIds = starService.findMissingTempWithSpectralIds(dataSetName);
        log.info("Cross-fill temp from spectral: found {} eligible stars", allIds.size());

        if (allIds.isEmpty()) {
            updateStatus(statusConsumer, "Cross-fill temp from spectral: no eligible stars found");
            return;
        }

        int totalBatches = (allIds.size() + batchSize - 1) / batchSize;
        updateStatus(statusConsumer, "Cross-fill temp from spectral: processing " + allIds.size() +
                " stars in " + totalBatches + " batches");

        for (int batchNum = 0; batchNum < totalBatches; batchNum++) {
            int start = batchNum * batchSize;
            int end = Math.min(start + batchSize, allIds.size());
            List<String> batchIds = allIds.subList(start, end);

            List<StarObject> candidates = starService.findStarsByIds(batchIds);
            List<StarObject> updatedStars = new ArrayList<>();

            for (StarObject star : candidates) {
                if (star.getTemperature() > 0) {
                    continue;
                }

                Double temp = StellarEstimators.estimateTemperatureFromSpectral(star.getSpectralClass());
                if (temp != null && temp > 0) {
                    star.setTemperature(temp);
                    star.setSource(appendToken(star.getSource(), "temp from spectral", "|"));
                    updatedStars.add(star);
                } else {
                    skipped++;
                }
            }

            if (!updatedStars.isEmpty()) {
                starService.updateStars(updatedStars);
                estimated += updatedStars.size();
                log.info("Cross-fill temp from spectral: batch {}/{} estimated {}, total {}",
                        batchNum + 1, totalBatches, updatedStars.size(), estimated);
            }

            int remaining = allIds.size() - end;
            updateStatus(statusConsumer, "Cross-fill temp from spectral: " + estimated +
                    " estimated, " + remaining + " remaining (batch " + (batchNum + 1) + "/" + totalBatches + ")");
        }

        log.info("Cross-fill temp from spectral complete: {} estimated, {} skipped", estimated, skipped);
        updateStatus(statusConsumer, "Cross-fill temp from spectral complete: " + estimated + " estimated");
    }

    /**
     * Cross-fills spectral class from temperature for stars that have temperature but no spectral.
     */
    public void crossFillSpectralFromTemperature(String dataSetName, Consumer<String> statusConsumer) {
        int batchSize = 5000;
        long estimated = 0;
        long skipped = 0;

        updateStatus(statusConsumer, "Cross-fill spectral from temp: fetching eligible star IDs...");
        List<String> allIds = starService.findMissingSpectralWithTempIds(dataSetName);
        log.info("Cross-fill spectral from temp: found {} eligible stars", allIds.size());

        if (allIds.isEmpty()) {
            updateStatus(statusConsumer, "Cross-fill spectral from temp: no eligible stars found");
            return;
        }

        int totalBatches = (allIds.size() + batchSize - 1) / batchSize;
        updateStatus(statusConsumer, "Cross-fill spectral from temp: processing " + allIds.size() +
                " stars in " + totalBatches + " batches");

        for (int batchNum = 0; batchNum < totalBatches; batchNum++) {
            int start = batchNum * batchSize;
            int end = Math.min(start + batchSize, allIds.size());
            List<String> batchIds = allIds.subList(start, end);

            List<StarObject> candidates = starService.findStarsByIds(batchIds);
            List<StarObject> updatedStars = new ArrayList<>();

            for (StarObject star : candidates) {
                if (star.getSpectralClass() != null && !star.getSpectralClass().isBlank()) {
                    continue;
                }

                String spectral = StellarEstimators.estimateSpectralFromTemperature(star.getTemperature());
                if (spectral != null && !spectral.isBlank()) {
                    star.setSpectralClass(spectral);
                    star.setSource(appendToken(star.getSource(), "spectral from temp", "|"));
                    updatedStars.add(star);
                } else {
                    skipped++;
                }
            }

            if (!updatedStars.isEmpty()) {
                starService.updateStars(updatedStars);
                estimated += updatedStars.size();
                log.info("Cross-fill spectral from temp: batch {}/{} estimated {}, total {}",
                        batchNum + 1, totalBatches, updatedStars.size(), estimated);
            }

            int remaining = allIds.size() - end;
            updateStatus(statusConsumer, "Cross-fill spectral from temp: " + estimated +
                    " estimated, " + remaining + " remaining (batch " + (batchNum + 1) + "/" + totalBatches + ")");
        }

        log.info("Cross-fill spectral from temp complete: {} estimated, {} skipped", estimated, skipped);
        updateStatus(statusConsumer, "Cross-fill spectral from temp complete: " + estimated + " estimated");
    }


    // Photometric/spectral estimators moved to StellarEstimators in Phase 4.3:
    //   estimatePhotometricMass, calculateLuminosityFromMagnitude, estimateRadiusFromMass,
    //   estimatePhotometricDistance, estimateAbsoluteMagnitudeFromBV,
    //   calculateDistanceFromMagnitudes, calculateDistanceFromParallax,
    //   calculateCoordinatesFromRaDec, estimateTemperatureFromBprp,
    //   estimateSpectralClassFromBprp, estimateTemperatureFromSpectral,
    //   estimateSpectralFromTemperature.
}
