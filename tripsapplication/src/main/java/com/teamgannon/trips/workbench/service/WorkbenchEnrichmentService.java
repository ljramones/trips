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

    private final StarService starService;

    public WorkbenchEnrichmentService(StarService starService) {
        this.starService = starService;
    }

    public void enrichMissingDistancesLive(String dataSetName,
                                           int batchSize,
                                           long delayMs,
                                           Consumer<String> statusConsumer) throws IOException, InterruptedException {
        int pageSize = Math.max(batchSize * 4, 200);
        int pageIndex = 0;
        long updated = 0;
        long processed = 0;
        while (true) {
            Page<StarObject> page = starService.findMissingDistanceWithIds(dataSetName, PageRequest.of(pageIndex, pageSize));
            if (!page.hasContent()) {
                break;
            }
            List<StarObject> candidates = page.getContent();
            List<StarObject> updatedStars = new ArrayList<>();
            Set<String> updatedIds = new HashSet<>();
            int pageNumber = pageIndex + 1;
            updateStatus(statusConsumer, "Live TAP enrichment: page " + pageNumber + ", candidates " + candidates.size());

            Map<String, List<StarObject>> gaiaMap = new HashMap<>();
            Map<String, List<StarObject>> hipMap = new HashMap<>();
            Map<String, List<StarObject>> simbadMap = new HashMap<>();
            for (StarObject star : candidates) {
                if (star.getDistance() > 0) {
                    continue;
                }
                String gaiaId = extractNumericId(star.getGaiaDR3CatId());
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
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            }

            for (int i = 0; i < hipIds.size(); i += batchSize) {
                int batchNumber = (i / batchSize) + 1;
                List<String> batch = hipIds.subList(i, Math.min(i + batchSize, hipIds.size()));
                updateStatus(statusConsumer, "Live TAP: HIP batch " + batchNumber + "/" + hipBatches + " (ids " + batch.size() + ")");
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
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            }

            if (!updatedStars.isEmpty()) {
                starService.updateStars(updatedStars);
                updated += updatedStars.size();
                updateStatus(statusConsumer, "Live TAP enrichment: updated " + updated + " stars");
                log.info("Live TAP enrichment: page {} saved {}, total updated {}",
                        pageNumber, updatedStars.size(), updated);
            }
            processed += candidates.size();
            if (processed % 1000 == 0 || !updatedStars.isEmpty()) {
                long remaining = starService.countMissingDistance(dataSetName);
                log.info("Live TAP enrichment: processed {}, remaining missing distance {}",
                        processed, remaining);
                updateStatus(statusConsumer, "Live TAP enrichment: updated " + updated
                        + " stars, remaining missing distance " + remaining);
            }
            pageIndex++;
            if (!page.hasNext()) {
                break;
            }
        }
    }

    private void updateStatus(Consumer<String> statusConsumer, String message) {
        if (statusConsumer != null) {
            statusConsumer.accept(message);
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

    private String submitTapSyncCsv(String baseUrl, String adql, String label) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        String body = "REQUEST=doQuery&LANG=ADQL&FORMAT=csv&QUERY="
                + URLEncoder.encode(adql, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(URI.create(baseUrl + "/sync"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("{} sync status: {}", label, response.statusCode());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String bodyPreview = response.body();
            if (bodyPreview != null && bodyPreview.length() > 400) {
                bodyPreview = bodyPreview.substring(0, 400) + "...";
            }
            log.error("{} sync error body: {}", label, bodyPreview);
            throw new IOException(label + " sync failed. HTTP " + response.statusCode());
        }
        return response.body();
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
}
