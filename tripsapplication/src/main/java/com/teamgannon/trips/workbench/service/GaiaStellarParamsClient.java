package com.teamgannon.trips.workbench.service;

import com.teamgannon.trips.jpa.model.StarObject;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fetches stellar parameters (mass / radius / luminosity / temperature /
 * metallicity) from the Gaia DR3 {@code astrophysical_parameters} table
 * keyed by {@code source_id}, and applies them to {@link StarObject}s
 * without overwriting any value the star already has.
 * <p>
 * Extracted from {@link WorkbenchEnrichmentService} in Phase 4.3 of the
 * codebase-review remediation (Issue 18) as the first step toward the
 * eventual per-source {@code EnrichmentSource} interface — Gaia is the
 * most fleshed-out source today, so isolating its TAP query + response
 * mapping into its own class makes the future polymorphism easier.
 * <p>
 * Stateless; all methods are static. TAP I/O goes through
 * {@link TapHttpClient}; column parsing through {@link TapCsvParser}.
 */
@Slf4j
public final class GaiaStellarParamsClient {

    /**
     * Stellar parameters parsed out of one row of the Gaia DR3
     * astrophysical-parameters response. Every field is nullable — Gaia
     * returns NaN / blank for parameters it couldn't determine.
     */
    public static final class StellarParams {
        /** Solar masses (M☉). */
        public Double mass;
        /** Solar radii (R☉). */
        public Double radius;
        /** Solar luminosities (L☉). */
        public Double luminosity;
        /** Effective temperature, Kelvin. */
        public Double temperature;
        /** Metallicity [M/H], dex. */
        public Double metallicity;
    }

    private GaiaStellarParamsClient() {
    }

    /**
     * Query Gaia DR3 for stellar parameters keyed by source_id. Returns an
     * empty map if {@code gaiaIds} is empty (no TAP call made). The keys
     * are the canonical numeric source_id strings; rows with no usable
     * parameter values are omitted.
     */
    public static Map<String, StellarParams> fetchByGaiaIds(List<String> gaiaIds)
            throws IOException, InterruptedException {
        if (gaiaIds.isEmpty()) {
            return Map.of();
        }
        String idList = String.join(",", gaiaIds);
        String adql = "SELECT source_id, mass_flame, radius_flame, lum_flame, teff_gspphot, mh_gspphot " +
                "FROM gaiadr3.astrophysical_parameters " +
                "WHERE source_id IN (" + idList + ")";
        String csv = TapHttpClient.submitSyncCsv(TapHttpClient.GAIA_TAP_BASE_URL, adql, "Gaia Stellar Params TAP");

        if (!gaiaIds.isEmpty()) {
            log.info("Gaia stellar params query: first 5 IDs = {}",
                    gaiaIds.subList(0, Math.min(5, gaiaIds.size())));
        }
        if (csv != null) {
            String[] lines = csv.split("\\r?\\n");
            log.info("Gaia stellar params response: {} lines, header = {}",
                    lines.length, lines.length > 0 ? lines[0] : "empty");
            if (lines.length > 1 && lines.length <= 6) {
                for (int i = 1; i < lines.length; i++) {
                    log.info("  Row {}: {}", i, lines[i]);
                }
            }
        }

        return parseCsv(csv);
    }

    /** Parse the Gaia astrophysical-parameters CSV into an id-keyed map. */
    static Map<String, StellarParams> parseCsv(String csv) {
        Map<String, StellarParams> map = new HashMap<>();
        if (csv == null || csv.isBlank()) {
            return map;
        }
        String[] lines = csv.split("\\r?\\n");
        if (lines.length == 0) {
            return map;
        }
        String[] header = TapCsvParser.splitCsvLine(lines[0]);
        Map<String, Integer> headerIndex = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            headerIndex.put(header[i].trim().toLowerCase(), i);
        }

        int idIdx = TapCsvParser.findHeaderIndex(headerIndex, List.of("source_id"));
        int massIdx = TapCsvParser.findHeaderIndex(headerIndex, List.of("mass_flame"));
        int radiusIdx = TapCsvParser.findHeaderIndex(headerIndex, List.of("radius_flame"));
        int lumIdx = TapCsvParser.findHeaderIndex(headerIndex, List.of("lum_flame"));
        int tempIdx = TapCsvParser.findHeaderIndex(headerIndex, List.of("teff_gspphot"));
        int metalIdx = TapCsvParser.findHeaderIndex(headerIndex, List.of("mh_gspphot"));

        if (idIdx < 0) {
            log.warn("Gaia stellar params CSV missing source_id column");
            return map;
        }

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) {
                continue;
            }
            String[] values = TapCsvParser.splitCsvLine(line);
            if (idIdx >= values.length) {
                continue;
            }

            String id = CatalogIdExtractor.extractNumericId(values[idIdx]);
            if (id.isEmpty()) {
                continue;
            }

            StellarParams params = new StellarParams();
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

            if (params.mass != null || params.radius != null || params.luminosity != null
                    || params.temperature != null || params.metallicity != null) {
                map.putIfAbsent(id, params);
            }
        }
        return map;
    }

    /**
     * Apply Gaia stellar parameters to a star, filling in only missing /
     * default-valued fields. Returns {@code true} if any field was updated.
     * Updates the star's {@code source} + {@code notes} fields to record
     * provenance (deduplicated via {@link CatalogIdExtractor#appendToken}).
     */
    public static boolean applyTo(StarObject star, StellarParams params) {
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
        if (params.luminosity != null && params.luminosity > 0
                && (star.getLuminosity() == null || star.getLuminosity().isBlank())) {
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
            // Metallicity can be negative, so the "missing" sentinel is the default 0.
            star.setMetallicity(params.metallicity);
            updated = true;
            updatedFields.add("metallicity");
        }

        if (updated) {
            star.setSource(CatalogIdExtractor.appendToken(star.getSource(), "Gaia DR3 astrophysical", "|"));
            star.setNotes(CatalogIdExtractor.appendToken(star.getNotes(),
                    "stellar params from Gaia DR3: " + String.join(", ", updatedFields), "; "));
        }
        return updated;
    }

    /** Tolerant double parser used by the CSV parser. Null on blank / NaN / unparseable. */
    private static Double parseDoubleOrNull(String value) {
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
}
