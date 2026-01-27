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
        debugSkipCount = 0;  // Reset debug counter
        distanceRejectCount = 0;  // Reset distance rejection counter

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

                Double estimatedDistance = estimatePhotometricDistance(star);
                if (estimatedDistance != null && estimatedDistance > 0) {
                    star.setDistance(estimatedDistance);
                    double[] coords = calculateCoordinatesFromRaDec(star.getRa(), star.getDeclination(), estimatedDistance);
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

                Double mass = estimatePhotometricMass(star);
                if (mass != null && mass > 0) {
                    star.setMass(mass);

                    // Also estimate radius if missing (from mass-radius relation)
                    if (star.getRadius() <= 0) {
                        double radius = estimateRadiusFromMass(mass);
                        if (radius > 0) {
                            star.setRadius(radius);
                        }
                    }

                    // Set luminosity if we calculated it
                    if (star.getLuminosity() == null || star.getLuminosity().isBlank()) {
                        Double luminosity = calculateLuminosityFromMagnitude(star);
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
     * Estimates stellar mass from luminosity using the mass-luminosity relation.
     * L/L☉ ∝ (M/M☉)^α where α varies by mass range.
     */
    private Double estimatePhotometricMass(StarObject star) {
        Double luminosity = calculateLuminosityFromMagnitude(star);
        if (luminosity == null || luminosity <= 0) {
            return null;
        }

        // Mass-luminosity relation for main-sequence stars
        // L/L☉ = (M/M☉)^α
        // Solving for M: M/M☉ = (L/L☉)^(1/α)
        double mass;
        if (luminosity > 0.033) {
            // For M > 0.43 M☉: L ∝ M^4 (approximately)
            // More refined: L ∝ M^3.5 for 0.43 < M < 2
            //               L ∝ M^4 for 2 < M < 20
            if (luminosity < 2) {
                // Low-mass main sequence: α ≈ 4
                mass = Math.pow(luminosity, 1.0 / 4.0);
            } else if (luminosity < 16) {
                // Solar-mass range: α ≈ 3.5
                mass = Math.pow(luminosity, 1.0 / 3.5);
            } else {
                // High-mass stars: α ≈ 3
                mass = Math.pow(luminosity, 1.0 / 3.0);
            }
        } else {
            // For M < 0.43 M☉: L ∝ 0.23 * M^2.3
            // Solving: M = (L / 0.23)^(1/2.3)
            mass = Math.pow(luminosity / 0.23, 1.0 / 2.3);
        }

        // Sanity check: stellar masses range from ~0.08 to ~150 M☉
        // Allow lower values since V-band luminosity underestimates red dwarf masses
        if (mass < 0.01 || mass > 200) {
            return null;
        }

        return mass;
    }

    /**
     * Calculates luminosity in solar units from apparent magnitude and distance.
     * L/L☉ = 10^((M_V(Sun) - M_V) / 2.5) where M_V is absolute magnitude
     */
    private Double calculateLuminosityFromMagnitude(StarObject star) {
        double magV = star.getMagv();
        double distance = star.getDistance();

        // Check for valid data
        if (distance <= 0) {
            return null;
        }

        // Determine if distance is in parsecs or light years
        // HYG data stores distance in parsecs, TRIPS convention is light years
        // Heuristic: if distance < 100 and star has x,y,z coordinates that suggest parsecs, use parsecs
        // For nearby stars like Proxima (1.3 pc = 4.24 ly), the distance field contains parsecs
        double distanceParsecs;
        double x = star.getX();
        double y = star.getY();
        double z = star.getZ();
        double xyzDistance = Math.sqrt(x * x + y * y + z * z);

        // If xyz distance is close to distance value, assume both are in same unit (likely parsecs from HYG)
        // If xyz distance is ~3.26x the distance value, xyz is in ly and distance is in pc
        if (xyzDistance > 0 && Math.abs(xyzDistance - distance) / distance < 0.1) {
            // xyz and distance are in same unit - assume parsecs (HYG data)
            distanceParsecs = distance;
        } else if (xyzDistance > 0 && Math.abs(xyzDistance / 3.26156 - distance) / distance < 0.1) {
            // xyz is in light years, distance is in parsecs
            distanceParsecs = distance;
        } else if (distance > 100) {
            // Larger distances - assume light years (TRIPS convention)
            distanceParsecs = distance / 3.26156;
        } else {
            // Default: assume parsecs for HYG data
            distanceParsecs = distance;
        }

        // Calculate absolute magnitude: M = m - 5*log10(d/10)
        double absoluteMag = magV - 5 * Math.log10(distanceParsecs / 10);

        // Calculate luminosity: L/L☉ = 10^((M_V(Sun) - M_V) / 2.5)
        // Sun's absolute V magnitude is 4.83
        double luminosity = Math.pow(10, (4.83 - absoluteMag) / 2.5);

        // Sanity check: luminosities range from ~0.00001 to ~10 million L☉
        // Red dwarfs can have L ~ 0.0001 L☉
        if (luminosity < 0.000001 || luminosity > 10000000) {
            log.debug("Luminosity out of range for star {}: L={}, magV={}, dist={} pc",
                    star.getDisplayName(), luminosity, magV, distanceParsecs);
            return null;
        }

        return luminosity;
    }

    /**
     * Estimates stellar radius from mass using the mass-radius relation.
     * For main-sequence stars: R/R☉ ≈ (M/M☉)^α
     */
    private double estimateRadiusFromMass(double mass) {
        double radius;
        if (mass < 1.0) {
            // Low-mass stars: R ∝ M^0.8
            radius = Math.pow(mass, 0.8);
        } else {
            // Higher-mass stars: R ∝ M^0.57
            radius = Math.pow(mass, 0.57);
        }

        // Sanity check
        if (radius < 0.01 || radius > 2000) {
            return 0;
        }

        return radius;
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

    /**
     * Estimates distance using photometric methods.
     * Tries B-V color index first, then Gaia BP-RP as fallback.
     *
     * @return estimated distance in light-years, or null if cannot estimate
     */
    private Double estimatePhotometricDistance(StarObject star) {
        // Try Johnson B-V photometry first
        double magV = star.getMagv();
        double magB = star.getMagb();

        // Note: magnitudes can be negative for bright stars (e.g., Sirius = -1.46)
        // so we check for non-zero, not positive values
        boolean hasMagV = (magV != 0);
        boolean hasMagB = (magB != 0);

        if (hasMagV && hasMagB) {
            double bMinusV = magB - magV;
            Double absoluteMag = estimateAbsoluteMagnitudeFromBV(bMinusV);
            if (absoluteMag != null) {
                double dist = calculateDistanceFromMagnitudes(magV, absoluteMag);
                if (dist > 0) {
                    return dist;
                }
                log.debug("B-V path rejected: star={}, magV={}, magB={}, B-V={}, absMag={}, dist={}",
                        star.getDisplayName(), magV, magB, bMinusV, absoluteMag, dist);
            }
        }

        // Fallback to Gaia photometry (using BP-RP as color proxy)
        // Gaia G magnitude can be approximated from magV or we use apparent magnitude string
        double bprp = star.getBprp();
        if (bprp != 0 && hasMagV) {
            // BP-RP roughly correlates with B-V but with different scale
            // Approximate conversion: B-V ≈ 0.98 * (BP-RP) - 0.02 for main-sequence
            double approxBV = 0.98 * bprp - 0.02;
            Double absoluteMag = estimateAbsoluteMagnitudeFromBV(approxBV);
            if (absoluteMag != null) {
                double dist = calculateDistanceFromMagnitudes(magV, absoluteMag);
                if (dist > 0) {
                    return dist;
                }
                log.debug("BP-RP path rejected: star={}, magV={}, bprp={}, approxBV={}, absMag={}, dist={}",
                        star.getDisplayName(), magV, bprp, approxBV, absoluteMag, dist);
            }
        }

        // Log first few skipped stars to understand why
        if (debugSkipCount < 10) {
            log.info("Photometric skip: star={}, magV={}, magB={}, bprp={}",
                    star.getDisplayName(), magV, magB, bprp);
            debugSkipCount++;
        }

        return null;
    }

    // Debug counter for logging skipped stars
    private int debugSkipCount = 0;

    /**
     * Estimates absolute V magnitude from B-V color index.
     * Uses polynomial fit for main-sequence stars.
     *
     * Valid range: -0.3 < B-V < 2.0 (O through M stars)
     *
     * @param bMinusV the B-V color index
     * @return absolute magnitude M_V, or null if outside valid range
     */
    private Double estimateAbsoluteMagnitudeFromBV(double bMinusV) {
        // Extended range to handle hot blue stars (O, B types have B-V down to -0.35 or so)
        // and very red M dwarfs (B-V up to ~2.0)
        // We extrapolate cautiously for extreme values
        if (bMinusV < -2.0 || bMinusV > 3.0) {
            return null;  // Truly invalid - likely bad data
        }

        double bv = bMinusV;
        double bv2 = bv * bv;

        // Piecewise function for better accuracy across spectral range
        double absoluteMag;
        if (bv < -0.3) {
            // Very hot stars (O type): M_V ~ -4 to -6
            // Linear extrapolation for extreme blue
            absoluteMag = -4.0 + 2.0 * (bv + 0.3);
        } else if (bv < 0.0) {
            // Hot stars (B, early A): roughly linear
            absoluteMag = -0.5 + 3.5 * bv;
        } else if (bv < 0.4) {
            // A, F stars
            absoluteMag = -0.5 + 5.0 * bv - 2.0 * bv2;
        } else if (bv < 0.8) {
            // G stars (Sun-like)
            absoluteMag = 1.2 + 4.5 * bv - 1.5 * bv2;
        } else if (bv < 1.4) {
            // K stars
            absoluteMag = 2.0 + 4.0 * bv - 0.5 * bv2;
        } else if (bv < 2.0) {
            // M stars (red dwarfs)
            absoluteMag = 3.5 + 4.5 * bv;
        } else {
            // Very red stars (late M, brown dwarfs): extrapolate
            absoluteMag = 12.5 + 2.0 * (bv - 2.0);
        }

        // Sanity check: main-sequence stars range from about -8 to +20
        if (absoluteMag < -10 || absoluteMag > 22) {
            return null;
        }

        return absoluteMag;
    }

    /**
     * Calculates distance from apparent and absolute magnitudes.
     * Uses the distance modulus formula: d = 10^((m - M + 5) / 5)
     *
     * @param apparentMag apparent magnitude (m)
     * @param absoluteMag absolute magnitude (M)
     * @return distance in light-years
     */
    private double calculateDistanceFromMagnitudes(double apparentMag, double absoluteMag) {
        // Distance modulus: m - M = 5 * log10(d) - 5
        // Solving for d: d = 10^((m - M + 5) / 5) parsecs
        double distanceModulus = apparentMag - absoluteMag;
        double distanceParsecs = Math.pow(10, (distanceModulus + 5) / 5);

        // Convert parsecs to light-years (1 pc = 3.26156 ly)
        double distanceLy = distanceParsecs * 3.26156;

        // Sanity check: reject obviously wrong distances
        // 500,000 ly allows for distant halo stars; Milky Way diameter is ~100,000 ly
        if (distanceLy < 0.1 || distanceLy > 500000) {
            if (distanceRejectCount < 10) {
                log.info("Distance rejected: {} ly (apparentMag={}, absoluteMag={}, modulus={})",
                        "%.1f".formatted(distanceLy), apparentMag, absoluteMag, distanceModulus);
                distanceRejectCount++;
            }
            return 0;
        }

        return distanceLy;
    }

    // Debug counter for distance rejections
    private int distanceRejectCount = 0;

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
        double distance = calculateDistanceFromParallax(parallaxMas);
        if (distance <= 0) {
            return false;
        }
        star.setParallax(parallaxMas);
        star.setDistance(distance);
        double[] coords = calculateCoordinatesFromRaDec(star.getRa(), star.getDeclination(), distance);
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

    private double calculateDistanceFromParallax(double parallaxMas) {
        if (parallaxMas <= 0) {
            return 0.0;
        }
        double parsec = 1000.0 / parallaxMas;
        return parsec * 3.26156;
    }

    private double[] calculateCoordinatesFromRaDec(double raDeg, double decDeg, double distance) {
        double raRad = Math.toRadians(raDeg * 15.0);
        double decRad = Math.toRadians(decDeg);
        double x = distance * Math.cos(decRad) * Math.cos(raRad);
        double y = distance * Math.cos(decRad) * Math.sin(raRad);
        double z = distance * Math.sin(decRad);
        return new double[]{x, y, z};
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

                Double temp = estimateTemperatureFromBprp(star.getBprp());
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

                String spectral = estimateSpectralClassFromBprp(star.getBprp());
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

    /**
     * Estimates effective temperature from Gaia BP-RP color.
     * Based on polynomial fits from Gaia DR2/DR3 documentation and Pecaut & Mamajek (2013).
     * Valid for main-sequence stars with BP-RP roughly between -0.5 and 5.0
     *
     * @param bprp Gaia BP-RP color index
     * @return estimated temperature in Kelvin, or null if out of range
     */
    private Double estimateTemperatureFromBprp(double bprp) {
        // Sanity check - BP-RP typically ranges from -0.5 (hot O/B) to 5+ (cool M/L)
        if (bprp < -0.6 || bprp > 6.0) {
            return null;
        }

        double temp;

        if (bprp < 0.0) {
            // Hot stars (O, B): T ~ 10000-40000K
            // Linear approximation for very blue stars
            temp = 10000 - bprp * 30000;
        } else if (bprp < 0.5) {
            // A stars: BP-RP 0.0-0.5 → T ~ 7500-10000K
            temp = 10000 - bprp * 5000;
        } else if (bprp < 0.8) {
            // F stars: BP-RP 0.5-0.8 → T ~ 6000-7500K
            temp = 8333 - bprp * 3333;
        } else if (bprp < 1.0) {
            // G stars: BP-RP 0.8-1.0 → T ~ 5300-6000K
            temp = 7800 - bprp * 2500;
        } else if (bprp < 1.5) {
            // K stars: BP-RP 1.0-1.5 → T ~ 4000-5300K
            temp = 7700 - bprp * 2400;
        } else if (bprp < 3.0) {
            // M stars (early-mid): BP-RP 1.5-3.0 → T ~ 2800-4000K
            temp = 5200 - bprp * 800;
        } else {
            // Late M / L dwarfs: BP-RP 3.0-6.0 → T ~ 1500-2800K
            temp = 3700 - bprp * 400;
        }

        // Sanity check
        if (temp < 1000 || temp > 50000) {
            return null;
        }

        return temp;
    }

    /**
     * Estimates spectral class from Gaia BP-RP color.
     * Returns MK spectral classification (e.g., "G2V", "K5V", "M3V").
     *
     * @param bprp Gaia BP-RP color index
     * @return estimated spectral class, or null if out of range
     */
    private String estimateSpectralClassFromBprp(double bprp) {
        // Sanity check
        if (bprp < -0.6 || bprp > 6.0) {
            return null;
        }

        // Map BP-RP ranges to spectral types (main sequence assumed - luminosity class V)
        // Based on Pecaut & Mamajek (2013) and Gaia documentation
        if (bprp < -0.35) {
            return "O";  // Very hot
        } else if (bprp < -0.15) {
            return "B0V";
        } else if (bprp < 0.0) {
            return "B5V";
        } else if (bprp < 0.15) {
            return "A0V";
        } else if (bprp < 0.30) {
            return "A5V";
        } else if (bprp < 0.45) {
            return "F0V";
        } else if (bprp < 0.60) {
            return "F5V";
        } else if (bprp < 0.75) {
            return "G0V";
        } else if (bprp < 0.90) {
            return "G5V";
        } else if (bprp < 1.05) {
            return "K0V";
        } else if (bprp < 1.25) {
            return "K3V";
        } else if (bprp < 1.50) {
            return "K5V";
        } else if (bprp < 1.85) {
            return "M0V";
        } else if (bprp < 2.20) {
            return "M1V";
        } else if (bprp < 2.55) {
            return "M2V";
        } else if (bprp < 2.90) {
            return "M3V";
        } else if (bprp < 3.30) {
            return "M4V";
        } else if (bprp < 3.90) {
            return "M5V";
        } else if (bprp < 4.50) {
            return "M6V";
        } else if (bprp < 5.20) {
            return "M7V";
        } else {
            return "M8V";  // Very cool
        }
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

                Double temp = estimateTemperatureFromSpectral(star.getSpectralClass());
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

                String spectral = estimateSpectralFromTemperature(star.getTemperature());
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

    /**
     * Estimates temperature from spectral class.
     * Based on standard MK spectral classification temperature scales.
     */
    private Double estimateTemperatureFromSpectral(String spectralClass) {
        if (spectralClass == null || spectralClass.isBlank()) {
            return null;
        }

        String spec = spectralClass.trim().toUpperCase();
        char type = spec.charAt(0);

        // Try to extract subtype number (e.g., "G2V" -> 2)
        int subtype = 5; // default to middle of range
        if (spec.length() > 1 && Character.isDigit(spec.charAt(1))) {
            subtype = spec.charAt(1) - '0';
        }

        // Temperature ranges for each spectral type (main sequence)
        // Based on Pecaut & Mamajek (2013) and Gray & Corbally
        switch (type) {
            case 'O':
                // O0-O9: 50000K - 30000K
                return 50000.0 - subtype * 2000.0;
            case 'B':
                // B0-B9: 30000K - 10000K
                return 30000.0 - subtype * 2000.0;
            case 'A':
                // A0-A9: 10000K - 7500K
                return 10000.0 - subtype * 250.0;
            case 'F':
                // F0-F9: 7500K - 6000K
                return 7500.0 - subtype * 150.0;
            case 'G':
                // G0-G9: 6000K - 5200K
                return 6000.0 - subtype * 80.0;
            case 'K':
                // K0-K9: 5200K - 3700K
                return 5200.0 - subtype * 150.0;
            case 'M':
                // M0-M9: 3700K - 2400K
                return 3700.0 - subtype * 130.0;
            case 'L':
                // L0-L9: 2400K - 1300K
                return 2400.0 - subtype * 110.0;
            case 'T':
                // T0-T9: 1300K - 600K
                return 1300.0 - subtype * 70.0;
            case 'Y':
                // Y dwarfs: < 600K
                return 500.0;
            default:
                return null;
        }
    }

    /**
     * Estimates spectral class from temperature.
     * Returns MK classification assuming main sequence.
     */
    private String estimateSpectralFromTemperature(double temp) {
        if (temp <= 0 || temp > 60000) {
            return null;
        }

        // Map temperature ranges to spectral types
        if (temp >= 30000) {
            int subtype = Math.min(9, (int) ((50000 - temp) / 2000));
            return "O" + subtype + "V";
        } else if (temp >= 10000) {
            int subtype = Math.min(9, (int) ((30000 - temp) / 2000));
            return "B" + subtype + "V";
        } else if (temp >= 7500) {
            int subtype = Math.min(9, (int) ((10000 - temp) / 250));
            return "A" + subtype + "V";
        } else if (temp >= 6000) {
            int subtype = Math.min(9, (int) ((7500 - temp) / 150));
            return "F" + subtype + "V";
        } else if (temp >= 5200) {
            int subtype = Math.min(9, (int) ((6000 - temp) / 80));
            return "G" + subtype + "V";
        } else if (temp >= 3700) {
            int subtype = Math.min(9, (int) ((5200 - temp) / 150));
            return "K" + subtype + "V";
        } else if (temp >= 2400) {
            int subtype = Math.min(9, (int) ((3700 - temp) / 130));
            return "M" + subtype + "V";
        } else if (temp >= 1300) {
            int subtype = Math.min(9, (int) ((2400 - temp) / 110));
            return "L" + subtype;
        } else if (temp >= 600) {
            int subtype = Math.min(9, (int) ((1300 - temp) / 70));
            return "T" + subtype;
        } else {
            return "Y0";
        }
    }
}
