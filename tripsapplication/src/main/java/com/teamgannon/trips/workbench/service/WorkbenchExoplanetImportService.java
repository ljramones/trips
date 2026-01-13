package com.teamgannon.trips.workbench.service;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.jpa.model.SolarSystem;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.repository.ExoPlanetRepository;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;
import com.teamgannon.trips.service.SolarSystemService;
import com.teamgannon.trips.workbench.model.ExoplanetCsvSchema;
import com.teamgannon.trips.workbench.model.ExoplanetMatchRow;
import com.teamgannon.trips.workbench.model.ExoplanetPreviewRow;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for importing exoplanets from exoplanet.eu CSV catalog files.
 * Handles parsing, matching to existing stars, and import.
 */
@Slf4j
@Service
public class WorkbenchExoplanetImportService {

    private final ExoPlanetRepository exoPlanetRepository;
    private final StarObjectRepository starObjectRepository;
    private final SolarSystemService solarSystemService;

    // Pattern to strip binary/component suffixes from star names
    private static final Pattern COMPONENT_SUFFIX = Pattern.compile("\\s*\\(?[ABCabc]+\\)?\\s*$");
    private static final Pattern CATALOG_PREFIX = Pattern.compile("^(HD|HIP|GJ|Gl|HR|TYC|2MASS|Gaia)\\s*", Pattern.CASE_INSENSITIVE);

    public WorkbenchExoplanetImportService(ExoPlanetRepository exoPlanetRepository,
                                            StarObjectRepository starObjectRepository,
                                            SolarSystemService solarSystemService) {
        this.exoPlanetRepository = exoPlanetRepository;
        this.starObjectRepository = starObjectRepository;
        this.solarSystemService = solarSystemService;
    }

    // ==================== CSV Parsing ====================

    /**
     * Parse an exoplanet.eu CSV file.
     *
     * @param csvPath        path to the CSV file
     * @param statusConsumer callback for status updates
     * @return list of parsed exoplanet rows
     */
    public List<ExoplanetCsvRow> parseCsvFile(Path csvPath, Consumer<String> statusConsumer) throws IOException {
        List<ExoplanetCsvRow> results = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            // Skip header line
            String header = reader.readLine();
            if (header == null) {
                throw new IOException("Empty CSV file");
            }

            statusConsumer.accept("Parsing CSV file...");

            String line;
            int lineNum = 1;
            int errors = 0;

            while ((line = reader.readLine()) != null) {
                lineNum++;
                try {
                    ExoplanetCsvRow row = parseCsvLine(line);
                    if (row != null && row.getName() != null && !row.getName().isBlank()) {
                        results.add(row);
                    }
                } catch (Exception e) {
                    errors++;
                    if (errors <= 5) {
                        log.warn("Error parsing line {}: {}", lineNum, e.getMessage());
                    }
                }

                if (lineNum % 1000 == 0) {
                    statusConsumer.accept("Parsed " + results.size() + " exoplanets...");
                }
            }

            statusConsumer.accept("Parsed " + results.size() + " exoplanets from " + lineNum + " lines");
            if (errors > 0) {
                log.warn("Total parse errors: {}", errors);
            }
        }

        return results;
    }

    /**
     * Parse a single CSV line into an ExoplanetCsvRow.
     */
    private ExoplanetCsvRow parseCsvLine(String line) {
        String[] fields = splitCsvLine(line);

        ExoplanetCsvRow row = new ExoplanetCsvRow();

        // Planet identification
        row.setName(getField(fields, ExoplanetCsvSchema.COL_NAME));
        row.setPlanetStatus(getField(fields, ExoplanetCsvSchema.COL_PLANET_STATUS));

        // Physical properties
        row.setMass(parseDouble(fields, ExoplanetCsvSchema.COL_MASS));
        row.setMassSini(parseDouble(fields, ExoplanetCsvSchema.COL_MASS_SINI));
        row.setRadius(parseDouble(fields, ExoplanetCsvSchema.COL_RADIUS));

        // Orbital parameters
        row.setOrbitalPeriod(parseDouble(fields, ExoplanetCsvSchema.COL_ORBITAL_PERIOD));
        row.setSemiMajorAxis(parseDouble(fields, ExoplanetCsvSchema.COL_SEMI_MAJOR_AXIS));
        row.setEccentricity(parseDouble(fields, ExoplanetCsvSchema.COL_ECCENTRICITY));
        row.setInclination(parseDouble(fields, ExoplanetCsvSchema.COL_INCLINATION));
        row.setOmega(parseDouble(fields, ExoplanetCsvSchema.COL_OMEGA));
        row.setAngularDistance(parseDouble(fields, ExoplanetCsvSchema.COL_ANGULAR_DISTANCE));

        // Time parameters
        row.setTperi(parseDouble(fields, ExoplanetCsvSchema.COL_TPERI));
        row.setTconj(parseDouble(fields, ExoplanetCsvSchema.COL_TCONJ));
        row.setTzeroTr(parseDouble(fields, ExoplanetCsvSchema.COL_TZERO_TR));
        row.setTzeroTrSec(parseDouble(fields, ExoplanetCsvSchema.COL_TZERO_TR_SEC));
        row.setLambdaAngle(parseDouble(fields, ExoplanetCsvSchema.COL_LAMBDA_ANGLE));
        row.setImpactParameter(parseDouble(fields, ExoplanetCsvSchema.COL_IMPACT_PARAMETER));
        row.setTzeroVr(parseDouble(fields, ExoplanetCsvSchema.COL_TZERO_VR));
        row.setK(parseDouble(fields, ExoplanetCsvSchema.COL_K));

        // Temperature and other
        row.setTempCalculated(parseDouble(fields, ExoplanetCsvSchema.COL_TEMP_CALCULATED));
        row.setTempMeasured(parseDouble(fields, ExoplanetCsvSchema.COL_TEMP_MEASURED));
        row.setHotPointLon(parseDouble(fields, ExoplanetCsvSchema.COL_HOT_POINT_LON));
        row.setGeometricAlbedo(parseDouble(fields, ExoplanetCsvSchema.COL_GEOMETRIC_ALBEDO));
        row.setLogG(parseDouble(fields, ExoplanetCsvSchema.COL_LOG_G));

        // Discovery info
        row.setDiscovered(parseInt(fields, ExoplanetCsvSchema.COL_DISCOVERED));
        row.setUpdated(getField(fields, ExoplanetCsvSchema.COL_UPDATED));

        // Detection info
        row.setPublication(getField(fields, ExoplanetCsvSchema.COL_PUBLICATION));
        row.setDetectionType(getField(fields, ExoplanetCsvSchema.COL_DETECTION_TYPE));
        row.setMassDetectionType(getField(fields, ExoplanetCsvSchema.COL_MASS_DETECTION_TYPE));
        row.setRadiusDetectionType(getField(fields, ExoplanetCsvSchema.COL_RADIUS_DETECTION_TYPE));
        row.setAlternateNames(getField(fields, ExoplanetCsvSchema.COL_ALTERNATE_NAMES));
        row.setMolecules(getField(fields, ExoplanetCsvSchema.COL_MOLECULES));

        // Host star properties
        row.setStarName(getField(fields, ExoplanetCsvSchema.COL_STAR_NAME));
        row.setRa(parseDouble(fields, ExoplanetCsvSchema.COL_RA));
        row.setDec(parseDouble(fields, ExoplanetCsvSchema.COL_DEC));
        row.setMagV(parseDouble(fields, ExoplanetCsvSchema.COL_MAG_V));
        row.setMagI(parseDouble(fields, ExoplanetCsvSchema.COL_MAG_I));
        row.setMagJ(parseDouble(fields, ExoplanetCsvSchema.COL_MAG_J));
        row.setMagH(parseDouble(fields, ExoplanetCsvSchema.COL_MAG_H));
        row.setMagK(parseDouble(fields, ExoplanetCsvSchema.COL_MAG_K));
        row.setStarDistance(parseDouble(fields, ExoplanetCsvSchema.COL_STAR_DISTANCE));
        row.setStarMetallicity(parseDouble(fields, ExoplanetCsvSchema.COL_STAR_METALLICITY));
        row.setStarMass(parseDouble(fields, ExoplanetCsvSchema.COL_STAR_MASS));
        row.setStarRadius(parseDouble(fields, ExoplanetCsvSchema.COL_STAR_RADIUS));
        row.setStarSpType(getField(fields, ExoplanetCsvSchema.COL_STAR_SP_TYPE));
        row.setStarAge(parseDouble(fields, ExoplanetCsvSchema.COL_STAR_AGE));
        row.setStarTeff(parseDouble(fields, ExoplanetCsvSchema.COL_STAR_TEFF));
        row.setStarDetectedDisc(getField(fields, ExoplanetCsvSchema.COL_STAR_DETECTED_DISC));
        row.setStarMagneticField(getField(fields, ExoplanetCsvSchema.COL_STAR_MAGNETIC_FIELD));
        row.setStarAlternateNames(getField(fields, ExoplanetCsvSchema.COL_STAR_ALTERNATE_NAMES));

        return row;
    }

    /**
     * Split CSV line handling quoted fields with commas.
     */
    private String[] splitCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString().trim());

        return fields.toArray(new String[0]);
    }

    private String getField(String[] fields, int index) {
        if (index >= 0 && index < fields.length) {
            String value = fields[index].trim();
            // Remove surrounding quotes
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            return value.isEmpty() ? null : value;
        }
        return null;
    }

    private Double parseDouble(String[] fields, int index) {
        String value = getField(fields, index);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInt(String[] fields, int index) {
        String value = getField(fields, index);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean parseBoolean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String lower = value.toLowerCase().trim();
        return "true".equals(lower) || "yes".equals(lower) || "1".equals(lower);
    }

    // ==================== Star Matching ====================

    /**
     * Match exoplanets to stars in a dataset using a multi-tier algorithm.
     *
     * @param exoplanets     list of parsed exoplanets
     * @param dataSetName    the dataset to match against
     * @param statusConsumer callback for status updates
     * @return match results with statistics
     */
    @Transactional(readOnly = true)
    public ExoplanetMatchResult matchExoplanetsToStars(List<ExoplanetCsvRow> exoplanets,
                                                        String dataSetName,
                                                        Consumer<String> statusConsumer) {
        List<ExoplanetMatch> matches = new ArrayList<>();
        int exactMatches = 0, fuzzyMatches = 0, raDecMatches = 0, unmatched = 0;

        statusConsumer.accept("Building star lookup maps...");

        // Build lookup maps for fast matching
        Map<String, StarObject> displayNameMap = buildDisplayNameMap(dataSetName);
        Map<String, StarObject> commonNameMap = buildCommonNameMap(dataSetName);

        statusConsumer.accept("Matching " + exoplanets.size() + " exoplanets to " +
                (displayNameMap.size() + commonNameMap.size()) + " star names...");

        for (int i = 0; i < exoplanets.size(); i++) {
            ExoplanetCsvRow exoplanet = exoplanets.get(i);
            ExoplanetMatch match = new ExoplanetMatch();
            match.setExoplanet(exoplanet);

            String starName = exoplanet.getStarName();
            StarObject matched = null;
            MatchType matchType = MatchType.NO_MATCH;
            double confidence = 0.0;

            if (starName != null && !starName.isBlank()) {
                // Strategy 1: Exact name match on displayName
                matched = displayNameMap.get(starName.toLowerCase().trim());
                if (matched != null) {
                    matchType = MatchType.EXACT_NAME;
                    confidence = 1.0;
                    exactMatches++;
                } else {
                    // Strategy 2: Exact match on commonName
                    matched = commonNameMap.get(starName.toLowerCase().trim());
                    if (matched != null) {
                        matchType = MatchType.EXACT_NAME;
                        confidence = 1.0;
                        exactMatches++;
                    }
                }

                // Strategy 3: Fuzzy name match (strip suffixes, normalize)
                if (matched == null) {
                    String normalized = normalizeStarName(starName);
                    matched = findByNormalizedName(displayNameMap, commonNameMap, normalized);
                    if (matched != null) {
                        matchType = MatchType.FUZZY_NAME;
                        confidence = 0.8;
                        fuzzyMatches++;
                    }
                }
            }

            // Strategy 4: RA/Dec proximity
            if (matched == null && exoplanet.getRa() != null && exoplanet.getDec() != null) {
                matched = findByRaDecProximity(dataSetName, exoplanet.getRa(), exoplanet.getDec());
                if (matched != null) {
                    matchType = MatchType.RA_DEC_PROXIMITY;
                    confidence = calculateAngularConfidence(matched, exoplanet.getRa(), exoplanet.getDec());
                    raDecMatches++;
                }
            }

            if (matched == null) {
                unmatched++;
            }

            match.setMatchedStar(matched);
            match.setMatchType(matchType);
            match.setConfidence(confidence);
            matches.add(match);

            // Progress reporting
            if ((i + 1) % 500 == 0) {
                statusConsumer.accept("Matching: " + (i + 1) + " / " + exoplanets.size());
            }
        }

        statusConsumer.accept(String.format("Matching complete: %d exact, %d fuzzy, %d RA/Dec, %d unmatched",
                exactMatches, fuzzyMatches, raDecMatches, unmatched));

        return new ExoplanetMatchResult(matches, exoplanets.size(),
                exactMatches, fuzzyMatches, raDecMatches, unmatched);
    }

    /**
     * Build a map of lowercase display names to stars.
     */
    private Map<String, StarObject> buildDisplayNameMap(String dataSetName) {
        Map<String, StarObject> map = new HashMap<>();
        try (var stream = starObjectRepository.findByDataSetName(dataSetName)) {
            stream.forEach(star -> {
                if (star.getDisplayName() != null && !star.getDisplayName().isBlank()) {
                    map.put(star.getDisplayName().toLowerCase().trim(), star);
                }
            });
        }
        return map;
    }

    /**
     * Build a map of lowercase common names to stars.
     */
    private Map<String, StarObject> buildCommonNameMap(String dataSetName) {
        Map<String, StarObject> map = new HashMap<>();
        try (var stream = starObjectRepository.findByDataSetName(dataSetName)) {
            stream.forEach(star -> {
                if (star.getCommonName() != null && !star.getCommonName().isBlank()) {
                    map.put(star.getCommonName().toLowerCase().trim(), star);
                }
            });
        }
        return map;
    }

    /**
     * Normalize a star name for fuzzy matching.
     * Strips component suffixes (A, B, AB), standardizes catalog prefixes.
     */
    private String normalizeStarName(String name) {
        if (name == null) return null;

        String normalized = name.toLowerCase().trim();

        // Remove component suffixes like " A", " B", " (AB)", etc.
        normalized = COMPONENT_SUFFIX.matcher(normalized).replaceAll("");

        // Standardize common catalog prefixes
        normalized = normalized
                .replaceAll("^gl\\s+", "gj ")  // Gl -> GJ
                .replaceAll("^gliese\\s+", "gj ")
                .replaceAll("\\s+", " ")  // Normalize whitespace
                .trim();

        return normalized;
    }

    /**
     * Find a star by normalized name in both lookup maps.
     */
    private StarObject findByNormalizedName(Map<String, StarObject> displayNameMap,
                                            Map<String, StarObject> commonNameMap,
                                            String normalizedName) {
        if (normalizedName == null) return null;

        // Try display name map with normalized key
        for (Map.Entry<String, StarObject> entry : displayNameMap.entrySet()) {
            String normalizedKey = normalizeStarName(entry.getKey());
            if (normalizedName.equals(normalizedKey)) {
                return entry.getValue();
            }
        }

        // Try common name map with normalized key
        for (Map.Entry<String, StarObject> entry : commonNameMap.entrySet()) {
            String normalizedKey = normalizeStarName(entry.getKey());
            if (normalizedName.equals(normalizedKey)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Find a star by RA/Dec proximity (within 0.01 degrees).
     */
    private StarObject findByRaDecProximity(String dataSetName, double ra, double dec) {
        // Use a simple box search - this could be optimized with a spatial index
        double tolerance = 0.01;

        try (var stream = starObjectRepository.findByDataSetName(dataSetName)) {
            List<StarObject> nearby = stream
                    .filter(star -> star.getRa() != 0 || star.getDeclination() != 0)
                    .filter(star -> Math.abs(star.getRa() - ra) <= tolerance)
                    .filter(star -> Math.abs(star.getDeclination() - dec) <= tolerance)
                    .collect(Collectors.toList());

            if (nearby.isEmpty()) {
                return null;
            }

            // Return the closest star
            return nearby.stream()
                    .min(Comparator.comparingDouble(star ->
                            angularDistance(star.getRa(), star.getDeclination(), ra, dec)))
                    .orElse(null);
        }
    }

    /**
     * Calculate angular distance between two positions (simple approximation).
     */
    private double angularDistance(double ra1, double dec1, double ra2, double dec2) {
        double dRa = ra1 - ra2;
        double dDec = dec1 - dec2;
        return Math.sqrt(dRa * dRa + dDec * dDec);
    }

    /**
     * Calculate confidence based on angular distance.
     */
    private double calculateAngularConfidence(StarObject star, double ra, double dec) {
        double distance = angularDistance(star.getRa(), star.getDeclination(), ra, dec);
        // 0.001 degrees -> 0.9 confidence, 0.01 degrees -> 0.5 confidence
        return Math.max(0.5, 1.0 - (distance / 0.02));
    }

    // ==================== Import ====================

    /**
     * Import matched exoplanets into the database.
     *
     * @param matches        list of matches to import
     * @param skipDuplicates whether to skip planets that already exist
     * @param statusConsumer callback for status updates
     * @return import results with statistics
     */
    @Transactional
    public ExoplanetImportResult importMatchedExoplanets(List<ExoplanetMatch> matches,
                                                          boolean skipDuplicates,
                                                          Consumer<String> statusConsumer) {
        int imported = 0, skipped = 0, solarSystemsCreated = 0;
        List<String> errors = new ArrayList<>();

        statusConsumer.accept("Starting import of " + matches.size() + " exoplanets...");

        for (int i = 0; i < matches.size(); i++) {
            ExoplanetMatch match = matches.get(i);

            // Skip unmatched
            if (match.getMatchType() == MatchType.NO_MATCH || match.getMatchedStar() == null) {
                skipped++;
                continue;
            }

            try {
                ExoplanetCsvRow csv = match.getExoplanet();
                StarObject star = match.getMatchedStar();

                // Check for duplicate
                if (skipDuplicates && exoPlanetRepository.existsByName(csv.getName())) {
                    skipped++;
                    continue;
                }

                // Find or create SolarSystem
                SolarSystem solarSystem = solarSystemService.findOrCreateSolarSystem(star);
                if (solarSystem.getPlanetCount() == 0) {
                    solarSystemsCreated++;
                }

                // Create ExoPlanet entity
                ExoPlanet exoPlanet = createExoPlanetFromCsv(csv);

                // Save the planet
                exoPlanet = exoPlanetRepository.save(exoPlanet);

                // Link to solar system
                solarSystemService.linkPlanetToSystem(exoPlanet, solarSystem, star);

                imported++;

            } catch (Exception e) {
                String errorMsg = "Error importing " + match.getExoplanet().getName() + ": " + e.getMessage();
                errors.add(errorMsg);
                log.error(errorMsg, e);
            }

            // Progress reporting
            if ((i + 1) % 100 == 0) {
                statusConsumer.accept("Imported: " + imported + " / " + (i + 1));
            }
        }

        statusConsumer.accept(String.format("Import complete: %d imported, %d skipped, %d solar systems created",
                imported, skipped, solarSystemsCreated));

        if (!errors.isEmpty()) {
            statusConsumer.accept("Errors: " + errors.size());
        }

        return new ExoplanetImportResult(imported, skipped, solarSystemsCreated, errors);
    }

    /**
     * Create an ExoPlanet entity from a CSV row.
     */
    private ExoPlanet createExoPlanetFromCsv(ExoplanetCsvRow csv) {
        ExoPlanet exoPlanet = new ExoPlanet();

        // Generate unique ID
        exoPlanet.setId(UUID.randomUUID().toString());

        // Basic info
        exoPlanet.setName(csv.getName());
        exoPlanet.setPlanetStatus(csv.getPlanetStatus());

        // Mass (in Jupiter masses from exoplanet.eu)
        exoPlanet.setMass(csv.getMass());
        exoPlanet.setMassSini(csv.getMassSini());

        // Radius (in Jupiter radii from exoplanet.eu)
        exoPlanet.setRadius(csv.getRadius());

        // Orbital parameters
        exoPlanet.setOrbitalPeriod(csv.getOrbitalPeriod());
        exoPlanet.setSemiMajorAxis(csv.getSemiMajorAxis());
        exoPlanet.setEccentricity(csv.getEccentricity());
        exoPlanet.setInclination(csv.getInclination());
        exoPlanet.setOmega(csv.getOmega());
        exoPlanet.setAngularDistance(csv.getAngularDistance());

        // Time parameters
        exoPlanet.setTperi(csv.getTperi());
        exoPlanet.setTconj(csv.getTconj());
        exoPlanet.setTzeroTr(csv.getTzeroTr());
        exoPlanet.setTzeroTrSec(csv.getTzeroTrSec());
        exoPlanet.setLambdaAngle(csv.getLambdaAngle());
        exoPlanet.setImpactParameter(csv.getImpactParameter());
        exoPlanet.setTzeroVr(csv.getTzeroVr());
        exoPlanet.setK(csv.getK());

        // Temperature and other physical properties
        exoPlanet.setTempCalculated(csv.getTempCalculated());
        exoPlanet.setTempMeasured(csv.getTempMeasured());
        exoPlanet.setHotPoIntegerLon(csv.getHotPointLon());
        exoPlanet.setGeometricAlbedo(csv.getGeometricAlbedo());
        exoPlanet.setLogG(csv.getLogG());

        // Discovery info
        exoPlanet.setDiscovered(csv.getDiscovered());
        exoPlanet.setUpdated(csv.getUpdated());

        // Detection info
        exoPlanet.setPublication(csv.getPublication());
        exoPlanet.setDetectionType(csv.getDetectionType());
        exoPlanet.setMassDetectionType(csv.getMassDetectionType());
        exoPlanet.setRadiusDetectionType(csv.getRadiusDetectionType());
        exoPlanet.setAlternateNames(csv.getAlternateNames());
        exoPlanet.setMolecules(csv.getMolecules());

        // Host star properties (denormalized copy from catalog)
        exoPlanet.setStarName(csv.getStarName());
        exoPlanet.setRa(csv.getRa());
        exoPlanet.setDec(csv.getDec());
        exoPlanet.setMagV(csv.getMagV());
        exoPlanet.setMagI(csv.getMagI());
        exoPlanet.setMagJ(csv.getMagJ());
        exoPlanet.setMagH(csv.getMagH());
        exoPlanet.setMagK(csv.getMagK());
        exoPlanet.setStarDistance(csv.getStarDistance());
        exoPlanet.setStarMetallicity(csv.getStarMetallicity());
        exoPlanet.setStarMass(csv.getStarMass());
        exoPlanet.setStarRadius(csv.getStarRadius());
        exoPlanet.setStarSpType(csv.getStarSpType());
        exoPlanet.setStarAge(csv.getStarAge());
        exoPlanet.setStarTeff(csv.getStarTeff());
        exoPlanet.setStarDetectedDisc(parseBoolean(csv.getStarDetectedDisc()));
        exoPlanet.setStarMagneticField(csv.getStarMagneticField());
        exoPlanet.setStarAlternateNames(csv.getStarAlternateNames());

        return exoPlanet;
    }

    // ==================== Conversion to Display Models ====================

    /**
     * Convert parsed CSV rows to preview display rows.
     */
    public List<ExoplanetPreviewRow> toPreviewRows(List<ExoplanetCsvRow> csvRows) {
        return csvRows.stream()
                .map(this::toPreviewRow)
                .collect(Collectors.toList());
    }

    private ExoplanetPreviewRow toPreviewRow(ExoplanetCsvRow csv) {
        ExoplanetPreviewRow row = new ExoplanetPreviewRow();
        row.setName(csv.getName());
        row.setPlanetStatus(csv.getPlanetStatus());
        row.setStarName(csv.getStarName());
        row.setSemiMajorAxis(formatDouble(csv.getSemiMajorAxis(), "%.4f AU"));
        row.setMass(formatDouble(csv.getMass(), "%.2f Mj"));
        row.setRadius(formatDouble(csv.getRadius(), "%.2f Rj"));
        row.setOrbitalPeriod(formatDouble(csv.getOrbitalPeriod(), "%.2f d"));
        row.setEccentricity(formatDouble(csv.getEccentricity(), "%.3f"));
        row.setInclination(formatDouble(csv.getInclination(), "%.1f°"));
        row.setRa(formatDouble(csv.getRa(), "%.4f"));
        row.setDec(formatDouble(csv.getDec(), "%.4f"));
        row.setStarDistance(formatDouble(csv.getStarDistance(), "%.2f pc"));
        row.setSpectralType(csv.getStarSpType());
        return row;
    }

    /**
     * Convert match results to display rows.
     */
    public List<ExoplanetMatchRow> toMatchRows(ExoplanetMatchResult result) {
        return result.getMatches().stream()
                .map(this::toMatchRow)
                .collect(Collectors.toList());
    }

    private ExoplanetMatchRow toMatchRow(ExoplanetMatch match) {
        ExoplanetMatchRow row = new ExoplanetMatchRow();
        row.setExoplanetName(match.getExoplanet().getName());
        row.setCsvStarName(match.getExoplanet().getStarName());

        if (match.getMatchedStar() != null) {
            row.setMatchedStarName(match.getMatchedStar().getDisplayName());
            row.setMatchedStarId(match.getMatchedStar().getId());
        } else {
            row.setMatchedStarName("(no match)");
            row.setMatchedStarId(null);
        }

        row.setMatchType(match.getMatchType().getDisplayName());
        row.setConfidence(String.format("%.0f%%", match.getConfidence() * 100));

        // Pre-select matched rows for import
        row.setSelected(match.getMatchType() != MatchType.NO_MATCH);

        return row;
    }

    private String formatDouble(Double value, String format) {
        if (value == null || value.isNaN()) {
            return "";
        }
        return String.format(format, value);
    }

    // ==================== Inner Classes ====================

    /**
     * Parsed row from exoplanet.eu CSV file.
     */
    @Data
    public static class ExoplanetCsvRow {
        // Planet identification
        private String name;
        private String planetStatus;

        // Physical properties
        private Double mass;
        private Double massSini;
        private Double radius;

        // Orbital parameters
        private Double orbitalPeriod;
        private Double semiMajorAxis;
        private Double eccentricity;
        private Double inclination;
        private Double omega;
        private Double angularDistance;

        // Time parameters
        private Double tperi;
        private Double tconj;
        private Double tzeroTr;
        private Double tzeroTrSec;
        private Double lambdaAngle;
        private Double impactParameter;
        private Double tzeroVr;
        private Double k;

        // Temperature and other
        private Double tempCalculated;
        private Double tempMeasured;
        private Double hotPointLon;
        private Double geometricAlbedo;
        private Double logG;

        // Discovery info
        private Integer discovered;
        private String updated;
        private String publication;
        private String detectionType;
        private String massDetectionType;
        private String radiusDetectionType;
        private String alternateNames;
        private String molecules;

        // Host star properties
        private String starName;
        private Double ra;
        private Double dec;
        private Double magV;
        private Double magI;
        private Double magJ;
        private Double magH;
        private Double magK;
        private Double starDistance;
        private Double starMetallicity;
        private Double starMass;
        private Double starRadius;
        private String starSpType;
        private Double starAge;
        private Double starTeff;
        private String starDetectedDisc;
        private String starMagneticField;
        private String starAlternateNames;
    }

    /**
     * Match result between an exoplanet and a star.
     */
    @Data
    public static class ExoplanetMatch {
        private ExoplanetCsvRow exoplanet;
        private StarObject matchedStar;
        private MatchType matchType;
        private double confidence;
    }

    /**
     * Summary of match results.
     */
    @Data
    public static class ExoplanetMatchResult {
        private final List<ExoplanetMatch> matches;
        private final int totalExoplanets;
        private final int exactMatches;
        private final int fuzzyMatches;
        private final int raDecMatches;
        private final int unmatched;

        public int getTotalMatched() {
            return exactMatches + fuzzyMatches + raDecMatches;
        }
    }

    /**
     * Summary of import results.
     */
    @Data
    public static class ExoplanetImportResult {
        private final int imported;
        private final int skipped;
        private final int solarSystemsCreated;
        private final List<String> errors;
    }

    /**
     * Type of match found between exoplanet and star.
     */
    public enum MatchType {
        EXACT_NAME("Exact Name"),
        FUZZY_NAME("Fuzzy Name"),
        RA_DEC_PROXIMITY("RA/Dec"),
        NO_MATCH("No Match");

        private final String displayName;

        MatchType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
