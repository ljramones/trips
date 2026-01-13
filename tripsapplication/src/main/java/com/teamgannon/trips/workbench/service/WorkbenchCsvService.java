package com.teamgannon.trips.workbench.service;

import com.teamgannon.trips.workbench.MappingRow;
import com.teamgannon.trips.workbench.model.WorkbenchCsvSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;

@Service
@Slf4j
public class WorkbenchCsvService {

    public void convertHygCsvToTripsCsv(Path inputPath,
                                        Path outputPath,
                                        String datasetName,
                                        Consumer<Long> progressConsumer) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(inputPath, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("Input file is empty.");
            }
            boolean tabDelimited = isTabDelimited(headerLine);
            String[] headers = splitHygLine(headerLine, tabDelimited);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                headerIndex.put(unquote(headers[i]).trim(), i);
            }

            Map<String, Integer> csvIndex = buildCsvIndex();
            writer.write(String.join(",", WorkbenchCsvSchema.CSV_HEADER_COLUMNS));
            writer.newLine();

            long count = 0;
            writeSolRow(writer, csvIndex, datasetName);
            count++;
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = splitHygLine(line, tabDelimited);

                String hygId = getTsvField(headerIndex, values, "id");
                String tyc = getTsvField(headerIndex, values, "tyc");
                String gaia = getTsvField(headerIndex, values, "gaia");
                String hip = getTsvField(headerIndex, values, "hip");
                String hd = getTsvField(headerIndex, values, "hd");
                String hr = getTsvField(headerIndex, values, "hr");
                String gl = getTsvField(headerIndex, values, "gl");
                String bayer = getTsvField(headerIndex, values, "bayer");
                String flam = getTsvField(headerIndex, values, "flam");
                String con = getTsvField(headerIndex, values, "con");
                String proper = getTsvField(headerIndex, values, "proper");

                String ra = getTsvField(headerIndex, values, "ra");
                String dec = getTsvField(headerIndex, values, "dec");
                String dist = getTsvField(headerIndex, values, "dist");
                String x0 = getTsvField(headerIndex, values, "x0");
                String y0 = getTsvField(headerIndex, values, "y0");
                String z0 = getTsvField(headerIndex, values, "z0");
                String pmRa = getTsvField(headerIndex, values, "pm_ra");
                String pmDec = getTsvField(headerIndex, values, "pm_dec");
                String rv = getTsvField(headerIndex, values, "rv");
                String spect = getTsvField(headerIndex, values, "spect");
                String colorIndex = getTsvField(headerIndex, values, "ci");
                String mag = getTsvField(headerIndex, values, "mag");
                String absMag = getTsvField(headerIndex, values, "absmag");
                String vx = getTsvField(headerIndex, values, "vx");
                String vy = getTsvField(headerIndex, values, "vy");
                String vz = getTsvField(headerIndex, values, "vz");
                String resolvedDistance = resolveDistance(dist, x0, y0, z0);
                Coordinates hygCoordinates = resolveCoordinates(x0, y0, z0, ra, dec, resolvedDistance);

                String displayName = chooseDisplayName(proper, bayer, flam, con, gl, hd, hip, hr, tyc, gaia, hygId);
                if (isSolRow(displayName, proper)) {
                    continue;
                }
                String catalogIdList = buildHygCatalogIdList(hygId, tyc, gaia, hip, hd, hr, gl);

                String[] row = new String[WorkbenchCsvSchema.CSV_HEADER_COLUMNS.size()];
                Arrays.fill(row, "");
                setCsvValue(row, csvIndex, "dataSetName", datasetName);
                setCsvValue(row, csvIndex, "displayName", displayName);
                setCsvValue(row, csvIndex, "commonName", proper);
                setCsvValue(row, csvIndex, "Epoch", "J2000");
                setCsvValue(row, csvIndex, "constellationName", con);
                setCsvValue(row, csvIndex, "source", WorkbenchCsvSchema.HYG_SOURCE_NAME);
                setCsvValue(row, csvIndex, "catalogIdList", catalogIdList);
                setCsvValue(row, csvIndex, "Gaia DR3", gaia);
                setCsvValue(row, csvIndex, "ra", ra);
                setCsvValue(row, csvIndex, "declination", dec);
                setCsvValue(row, csvIndex, "x", hygCoordinates.x);
                setCsvValue(row, csvIndex, "y", hygCoordinates.y);
                setCsvValue(row, csvIndex, "z", hygCoordinates.z);
                setCsvValue(row, csvIndex, "pmra", pmRa);
                setCsvValue(row, csvIndex, "pmdec", pmDec);
                setCsvValue(row, csvIndex, "distance", resolvedDistance);
                setCsvValue(row, csvIndex, "radialVelocity", rv);
                setCsvValue(row, csvIndex, "spectralClass", spect);
                setCsvValue(row, csvIndex, "bprp", colorIndex);
                setCsvValue(row, csvIndex, "magv", mag);
                setCsvValue(row, csvIndex, "absmag", absMag);
                setCsvValue(row, csvIndex, "miscText2", "vx");
                setCsvValue(row, csvIndex, "miscText3", "vy");
                setCsvValue(row, csvIndex, "miscText4", "vz");
                setCsvValue(row, csvIndex, "miscNum1", vx);
                setCsvValue(row, csvIndex, "miscNum2", vy);
                setCsvValue(row, csvIndex, "miscNum3", vz);
                setCsvValue(row, csvIndex, "realStar", "true");
                setCsvValue(row, csvIndex, "other", "false");
                setCsvValue(row, csvIndex, "anomaly", "false");
                setCsvValue(row, csvIndex, "polity", "NA");
                setCsvValue(row, csvIndex, "worldType", "NA");
                setCsvValue(row, csvIndex, "fuelType", "NA");
                setCsvValue(row, csvIndex, "portType", "NA");
                setCsvValue(row, csvIndex, "populationType", "NA");
                setCsvValue(row, csvIndex, "techType", "NA");
                setCsvValue(row, csvIndex, "productType", "NA");
                setCsvValue(row, csvIndex, "milSpaceType", "NA");
                setCsvValue(row, csvIndex, "milPlanType", "NA");

                List<String> rowValues = new ArrayList<>(row.length);
                for (String value : row) {
                    rowValues.add(escapeCsv(value));
                }
                writer.write(String.join(",", rowValues));
                writer.newLine();

                count++;
                if (count % 50000 == 0) {
                    if (progressConsumer != null) {
                        progressConsumer.accept(count);
                    }
                    log.info("HYG converter: {} rows written", count);
                }
            }

            log.info("HYG converter complete. rows={}", count);
            if (progressConsumer != null) {
                progressConsumer.accept(count);
            }
        }
    }

    public void enrichDistances(Path baseCsv,
                                Path gaiaCsv,
                                Path hipCsv,
                                Path outputCsv,
                                Consumer<Long> progressConsumer) throws IOException {
        Map<String, Double> gaiaParallax = gaiaCsv != null
                ? loadParallaxMap(gaiaCsv, List.of("source_id", "Gaia DR3", "dr3_source_id", "sourceId"),
                List.of("parallax", "plx", "plx_value"))
                : new HashMap<>();
        Map<String, Double> hipParallax = hipCsv != null
                ? loadParallaxMap(hipCsv, List.of("HIP", "hip", "hip_id", "HIP_ID"),
                List.of("Plx", "plx", "plx_value", "parallax"))
                : new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(baseCsv, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(outputCsv, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                throw new IOException("Base file is empty.");
            }
            writer.write(header);
            writer.newLine();

            String[] headerFields = splitCsvLine(header);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headerFields.length; i++) {
                headerIndex.put(headerFields[i].trim(), i);
            }
            int distIdx = findHeaderIndex(headerIndex, List.of("distance"));
            int raIdx = findHeaderIndex(headerIndex, List.of("ra"));
            int decIdx = findHeaderIndex(headerIndex, List.of("declination"));
            int xIdx = findHeaderIndex(headerIndex, List.of("x"));
            int yIdx = findHeaderIndex(headerIndex, List.of("y"));
            int zIdx = findHeaderIndex(headerIndex, List.of("z"));
            int gaiaIdx = findHeaderIndex(headerIndex, List.of("Gaia DR3"));
            int catalogIdx = findHeaderIndex(headerIndex, List.of("catalogIdList"));
            int sourceIdx = findHeaderIndex(headerIndex, List.of("source"));
            int notesIdx = findHeaderIndex(headerIndex, List.of("notes"));

            long count = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = splitCsvLine(line);
                values = linePad(values, headerFields.length);
                double distance = distIdx >= 0 ? parseDoubleSafe(values[distIdx]) : 0.0;
                if (distance <= 0 && (gaiaIdx >= 0 || catalogIdx >= 0)) {
                    String gaiaId = gaiaIdx >= 0 ? extractNumericId(values[gaiaIdx]) : "";
                    String hipId = catalogIdx >= 0 ? extractHipId(values[catalogIdx]) : "";
                    Double parallax = null;
                    String sourceTag = null;
                    String notesTag = null;
                    if (!gaiaId.isEmpty()) {
                        parallax = gaiaParallax.get(gaiaId);
                        if (parallax != null && parallax > 0) {
                            sourceTag = "Gaia DR3 parallax";
                            notesTag = "distance from Gaia DR3 parallax";
                        }
                    }
                    if ((parallax == null || parallax <= 0) && !hipId.isEmpty()) {
                        parallax = hipParallax.get(hipId);
                        if (parallax != null && parallax > 0) {
                            sourceTag = "HIP parallax";
                            notesTag = "distance from HIP parallax";
                        }
                    }
                    if (parallax != null && parallax > 0 && distIdx >= 0 && raIdx >= 0 && decIdx >= 0) {
                        distance = calculateDistanceFromParallax(parallax);
                        values[distIdx] = Double.toString(distance);
                        double raDeg = parseDoubleSafe(values[raIdx]);
                        double decDeg = parseDoubleSafe(values[decIdx]);
                        double[] coords = calculateCoordinatesFromRaDec(raDeg, decDeg, distance);
                        if (xIdx >= 0) {
                            values[xIdx] = Double.toString(coords[0]);
                        }
                        if (yIdx >= 0) {
                            values[yIdx] = Double.toString(coords[1]);
                        }
                        if (zIdx >= 0) {
                            values[zIdx] = Double.toString(coords[2]);
                        }
                        if (sourceIdx >= 0 && sourceTag != null) {
                            values[sourceIdx] = appendToken(values[sourceIdx], sourceTag, "|");
                        }
                        if (notesIdx >= 0 && notesTag != null) {
                            values[notesIdx] = appendToken(values[notesIdx], notesTag, "; ");
                        }
                    }
                }

                List<String> rowValues = new ArrayList<>(values.length);
                for (String value : values) {
                    rowValues.add(escapeCsv(value));
                }
                writer.write(String.join(",", rowValues));
                writer.newLine();
                count++;
                if (count % 50000 == 0 && progressConsumer != null) {
                    progressConsumer.accept(count);
                }
            }
        }
    }

    public ExportResult exportMappedCsv(Path sourcePath,
                                        Path outputPath,
                                        List<String> headerColumns,
                                        List<MappingRow> mappings) throws IOException {
        if (!Files.exists(sourcePath)) {
            return new ExportResult(0, List.of("Source file not found."));
        }
        try (BufferedReader reader = Files.newBufferedReader(sourcePath, StandardCharsets.UTF_8);
             BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                return new ExportResult(0, List.of("Source file is empty."));
            }
            Map<String, Integer> headerIndex = buildHeaderIndex(header);
            Map<String, String> targetToSource = buildTargetToSourceMap(mappings);
            List<String> validationMessages = new ArrayList<>();
            writer.write(String.join(",", headerColumns));
            writer.newLine();

            int rowCount = 0;
            String line;
            int rowIndex = 1;
            while ((line = reader.readLine()) != null) {
                String[] values = splitCsvLine(line);
                Map<String, String> mappedRow = mapRow(values, headerIndex, targetToSource);
                List<String> rowValues = new ArrayList<>();
                for (String column : headerColumns) {
                    rowValues.add(escapeCsv(mappedRow.getOrDefault(column, "")));
                }
                writer.write(String.join(",", rowValues));
                writer.newLine();
                rowCount++;
                validationMessages.addAll(validateRow(mappedRow, rowIndex));
                rowIndex++;
            }
            if (validationMessages.isEmpty()) {
                validationMessages.add("Validation complete. Errors: 0");
            } else {
                validationMessages.add(0, "Validation complete. Errors: " + validationMessages.size());
            }
            return new ExportResult(rowCount, validationMessages);
        }
    }

    public ExportResult validateMappedCsv(Path sourcePath, List<MappingRow> mappings) throws IOException {
        if (!Files.exists(sourcePath)) {
            return new ExportResult(0, List.of("Source file not found."));
        }
        try (BufferedReader reader = Files.newBufferedReader(sourcePath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                return new ExportResult(0, List.of("Source file is empty."));
            }
            Map<String, Integer> headerIndex = buildHeaderIndex(header);
            Map<String, String> targetToSource = buildTargetToSourceMap(mappings);
            List<String> validationMessages = new ArrayList<>();
            int rowCount = 0;
            String line;
            int rowIndex = 1;
            while ((line = reader.readLine()) != null) {
                String[] values = splitCsvLine(line);
                Map<String, String> mappedRow = mapRow(values, headerIndex, targetToSource);
                validationMessages.addAll(validateRow(mappedRow, rowIndex));
                rowCount++;
                rowIndex++;
            }
            if (validationMessages.isEmpty()) {
                validationMessages.add("Validation complete. Errors: 0");
            } else {
                validationMessages.add(0, "Validation complete. Errors: " + validationMessages.size());
            }
            return new ExportResult(rowCount, validationMessages);
        }
    }

    public Map<String, Integer> buildHeaderIndex(String headerLine) {
        String[] headerFields = splitCsvLine(headerLine);
        Map<String, Integer> headerIndex = new HashMap<>();
        for (int i = 0; i < headerFields.length; i++) {
            headerIndex.put(headerFields[i].trim(), i);
        }
        return headerIndex;
    }

    public Map<String, String> buildTargetToSourceMap(List<MappingRow> mappings) {
        Map<String, String> targetToSource = new HashMap<>();
        for (MappingRow mapping : mappings) {
            targetToSource.put(mapping.getTargetField(), mapping.getSourceField());
        }
        return targetToSource;
    }

    public Map<String, String> mapRow(String[] values,
                                      Map<String, Integer> headerIndex,
                                      Map<String, String> targetToSource) {
        Map<String, String> mappedRow = new HashMap<>();
        for (Map.Entry<String, String> entry : targetToSource.entrySet()) {
            Integer index = headerIndex.get(entry.getValue());
            if (index != null && index < values.length) {
                mappedRow.put(entry.getKey(), unquote(values[index]));
            }
        }
        return mappedRow;
    }

    public String[] splitCsvLine(String line) {
        if (line == null) {
            return new String[0];
        }
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    public String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.contains(",") || trimmed.contains("\"") || trimmed.contains("\n")) {
            String escaped = trimmed.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }
        return trimmed;
    }

    public String unquote(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            String inner = trimmed.substring(1, trimmed.length() - 1);
            return inner.replace("\"\"", "\"");
        }
        return trimmed;
    }

    private Map<String, Integer> buildCsvIndex() {
        Map<String, Integer> index = new HashMap<>();
        for (int i = 0; i < WorkbenchCsvSchema.CSV_HEADER_COLUMNS.size(); i++) {
            index.put(WorkbenchCsvSchema.CSV_HEADER_COLUMNS.get(i), i);
        }
        return index;
    }

    private void setCsvValue(String[] row, Map<String, Integer> index, String column, String value) {
        Integer idx = index.get(column);
        if (idx != null) {
            row[idx] = value == null ? "" : value;
        }
    }

    private boolean isTabDelimited(String headerLine) {
        return headerLine.contains("\t") && !headerLine.contains(",");
    }

    private String[] splitHygLine(String line, boolean tabDelimited) {
        if (tabDelimited) {
            return line.split("\t", -1);
        }
        return splitCsvLine(line);
    }

    private String resolveDistance(String dist, String x0, String y0, String z0) {
        String normalized = dist == null ? "" : dist.trim();
        if (!normalized.isEmpty()) {
            double value = parseDoubleSafe(normalized);
            if (value > 0) {
                return normalized;
            }
        }
        double x = parseDoubleSafe(x0);
        double y = parseDoubleSafe(y0);
        double z = parseDoubleSafe(z0);
        double computed = Math.sqrt((x * x) + (y * y) + (z * z));
        if (computed > 0) {
            return Double.toString(computed);
        }
        return "0.0";
    }

    private double parseDoubleSafe(String value) {
        if (value == null) {
            return 0.0;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private Coordinates resolveCoordinates(String x0, String y0, String z0,
                                           String ra, String dec, String dist) {
        double x = parseDoubleSafe(x0);
        double y = parseDoubleSafe(y0);
        double z = parseDoubleSafe(z0);
        if (x != 0.0 || y != 0.0 || z != 0.0) {
            return Coordinates.from(x, y, z);
        }
        double distance = parseDoubleSafe(dist);
        if (distance <= 0.0) {
            return Coordinates.from(0.0, 0.0, 0.0);
        }
        double raDeg = parseDoubleSafe(ra);
        double decDeg = parseDoubleSafe(dec);
        double raRad = Math.toRadians(raDeg);
        double decRad = Math.toRadians(decDeg);
        double cosDec = Math.cos(decRad);
        double calcX = distance * cosDec * Math.cos(raRad);
        double calcY = distance * cosDec * Math.sin(raRad);
        double calcZ = distance * Math.sin(decRad);
        return Coordinates.from(calcX, calcY, calcZ);
    }

    private static class Coordinates {
        private final String x;
        private final String y;
        private final String z;

        private Coordinates(String x, String y, String z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private static Coordinates from(double x, double y, double z) {
            return new Coordinates(Double.toString(x), Double.toString(y), Double.toString(z));
        }
    }

    private void writeSolRow(BufferedWriter writer,
                             Map<String, Integer> csvIndex,
                             String datasetName) throws IOException {
        String[] row = new String[WorkbenchCsvSchema.CSV_HEADER_COLUMNS.size()];
        Arrays.fill(row, "");
        setCsvValue(row, csvIndex, "dataSetName", datasetName);
        setCsvValue(row, csvIndex, "displayName", "Sol");
        setCsvValue(row, csvIndex, "commonName", "Sol");
        setCsvValue(row, csvIndex, "Epoch", "J2000");
        setCsvValue(row, csvIndex, "mass", "1.99E+30");
        setCsvValue(row, csvIndex, "notes", "none");
        setCsvValue(row, csvIndex, "source", WorkbenchCsvSchema.HYG_SOURCE_NAME);
        setCsvValue(row, csvIndex, "catalogIdList", "HYG 0");
        setCsvValue(row, csvIndex, "radius", "695700.0");
        setCsvValue(row, csvIndex, "ra", "0.0");
        setCsvValue(row, csvIndex, "declination", "0.0");
        setCsvValue(row, csvIndex, "distance", "0.0");
        setCsvValue(row, csvIndex, "spectralClass", "G2V");
        setCsvValue(row, csvIndex, "temperature", "5772.0");
        setCsvValue(row, csvIndex, "realStar", "true");
        setCsvValue(row, csvIndex, "other", "false");
        setCsvValue(row, csvIndex, "anomaly", "false");
        setCsvValue(row, csvIndex, "polity", "NA");
        setCsvValue(row, csvIndex, "worldType", "NA");
        setCsvValue(row, csvIndex, "fuelType", "NA");
        setCsvValue(row, csvIndex, "portType", "NA");
        setCsvValue(row, csvIndex, "populationType", "NA");
        setCsvValue(row, csvIndex, "techType", "NA");
        setCsvValue(row, csvIndex, "productType", "NA");
        setCsvValue(row, csvIndex, "milSpaceType", "NA");
        setCsvValue(row, csvIndex, "milPlanType", "NA");

        List<String> rowValues = new ArrayList<>(row.length);
        for (String value : row) {
            rowValues.add(escapeCsv(value));
        }
        writer.write(String.join(",", rowValues));
        writer.newLine();
    }

    private boolean isSolRow(String displayName, String proper) {
        if (displayName != null && displayName.trim().equalsIgnoreCase("Sol")) {
            return true;
        }
        return proper != null && proper.trim().equalsIgnoreCase("Sol");
    }

    private String getTsvField(Map<String, Integer> index, String[] values, String name) {
        Integer pos = index.get(name);
        if (pos == null || pos < 0 || pos >= values.length) {
            return "";
        }
        return unquote(values[pos]).trim();
    }

    private String chooseDisplayName(String proper,
                                     String bayer,
                                     String flam,
                                     String con,
                                     String gl,
                                     String hd,
                                     String hip,
                                     String hr,
                                     String tyc,
                                     String gaia,
                                     String hygId) {
        return firstNonEmpty(
                proper,
                formatWithConstellation(bayer, con),
                formatWithConstellation(flam, con),
                formatCatalogId("GJ", gl),
                formatCatalogId("HD", hd),
                formatCatalogId("HIP", hip),
                formatCatalogId("HR", hr),
                formatCatalogId("TYC", tyc),
                formatCatalogId("Gaia", gaia),
                formatCatalogId("HYG", hygId)
        );
    }

    private String formatWithConstellation(String value, String con) {
        if (value == null || value.isBlank()) {
            return "";
        }
        if (con == null || con.isBlank()) {
            return value.trim();
        }
        return value.trim() + " " + con.trim();
    }

    private String formatCatalogId(String prefix, String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return prefix + " " + value.trim();
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private String buildHygCatalogIdList(String hygId,
                                         String tyc,
                                         String gaia,
                                         String hip,
                                         String hd,
                                         String hr,
                                         String gl) {
        List<String> entries = new ArrayList<>();
        if (hygId != null && !hygId.isBlank()) {
            entries.add("HYG " + hygId.trim());
        }
        if (tyc != null && !tyc.isBlank()) {
            entries.add("TYC " + tyc.trim());
        }
        if (gaia != null && !gaia.isBlank()) {
            entries.add("Gaia DR3 " + gaia.trim());
        }
        if (hip != null && !hip.isBlank()) {
            entries.add("HIP " + hip.trim());
        }
        if (hd != null && !hd.isBlank()) {
            entries.add("HD " + hd.trim());
        }
        if (hr != null && !hr.isBlank()) {
            entries.add("HR " + hr.trim());
        }
        if (gl != null && !gl.isBlank()) {
            entries.add("GJ " + gl.trim());
        }
        return String.join("|", entries);
    }

    private Map<String, Double> loadParallaxMap(Path csvPath,
                                                List<String> idHeaders,
                                                List<String> parallaxHeaders) throws IOException {
        Map<String, Double> map = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                return map;
            }
            String[] headerFields = splitCsvLine(header);
            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < headerFields.length; i++) {
                headerIndex.put(headerFields[i].trim(), i);
            }
            int idIdx = findHeaderIndex(headerIndex, idHeaders);
            int parallaxIdx = findHeaderIndex(headerIndex, parallaxHeaders);
            if (idIdx < 0 || parallaxIdx < 0) {
                return map;
            }
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = splitCsvLine(line);
                if (idIdx >= values.length || parallaxIdx >= values.length) {
                    continue;
                }
                String id = extractNumericId(values[idIdx]);
                if (id.isEmpty()) {
                    continue;
                }
                double parallax = parseDoubleSafe(values[parallaxIdx]);
                if (parallax > 0) {
                    map.putIfAbsent(id, parallax);
                }
            }
        }
        return map;
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
        Matcher matcher = WorkbenchCsvSchema.DIGIT_PATTERN.matcher(value);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private String extractHipId(String catalogIdList) {
        if (catalogIdList == null) {
            return "";
        }
        String[] tokens = catalogIdList.split("\\|");
        for (String token : tokens) {
            String trimmed = token.trim();
            if (trimmed.toUpperCase().startsWith("HIP")) {
                return extractNumericId(trimmed);
            }
        }
        return "";
    }

    private double calculateDistanceFromParallax(double parallaxMas) {
        if (parallaxMas <= 0) {
            return 0.0;
        }
        double distanceParsecs = 1000.0 / parallaxMas;
        return distanceParsecs * 3.26156;
    }

    private double[] calculateCoordinatesFromRaDec(double raDeg, double decDeg, double distance) {
        double raRad = Math.toRadians(raDeg);
        double decRad = Math.toRadians(decDeg);
        double cosDec = Math.cos(decRad);
        double x = distance * cosDec * Math.cos(raRad);
        double y = distance * cosDec * Math.sin(raRad);
        double z = distance * Math.sin(decRad);
        return new double[]{x, y, z};
    }

    private String appendToken(String current, String token, String separator) {
        String base = current == null ? "" : current.trim();
        if (base.isEmpty()) {
            return token;
        }
        if (base.contains(token)) {
            return base;
        }
        return base + separator + token;
    }

    private String[] linePad(String[] values, int length) {
        if (values.length >= length) {
            return values;
        }
        return Arrays.copyOf(values, length);
    }

    public List<String> validateRow(Map<String, String> row, int rowIndex) {
        List<String> messages = new ArrayList<>();
        for (String req : WorkbenchCsvSchema.REQUIRED_FIELDS) {
            String value = row.getOrDefault(req, "").trim();
            if (value.isEmpty()) {
                messages.add("Row " + rowIndex + ": missing required field " + req);
            }
        }
        validateRange(row, rowIndex, "ra", 0.0, 360.0, messages);
        validateRange(row, rowIndex, "declination", -90.0, 90.0, messages);
        validateMin(row, rowIndex, "distance", 0.0, messages);
        validateBoolean(row, rowIndex, "realStar", messages);
        String spectralClass = row.getOrDefault("spectralClass", "").trim();
        if (!spectralClass.isEmpty()) {
            char type = Character.toUpperCase(spectralClass.charAt(0));
            if ("OBAFGKMLTY".indexOf(type) == -1) {
                messages.add("Row " + rowIndex + ": invalid spectralClass '" + spectralClass + "'");
            }
        }
        return messages;
    }

    private void validateRange(Map<String, String> row,
                               int rowIndex,
                               String field,
                               double min,
                               double max,
                               List<String> messages) {
        String value = row.getOrDefault(field, "").trim();
        if (value.isEmpty()) {
            return;
        }
        try {
            double parsed = Double.parseDouble(value);
            if (parsed < min || parsed > max) {
                messages.add("Row " + rowIndex + ": " + field + " out of range (" + min + " to " + max + ")");
            }
        } catch (NumberFormatException e) {
            messages.add("Row " + rowIndex + ": " + field + " is not numeric");
        }
    }

    private void validateMin(Map<String, String> row,
                             int rowIndex,
                             String field,
                             double min,
                             List<String> messages) {
        String value = row.getOrDefault(field, "").trim();
        if (value.isEmpty()) {
            return;
        }
        try {
            double parsed = Double.parseDouble(value);
            if (parsed <= min) {
                messages.add("Row " + rowIndex + ": " + field + " must be > " + min);
            }
        } catch (NumberFormatException e) {
            messages.add("Row " + rowIndex + ": " + field + " is not numeric");
        }
    }

    private void validateBoolean(Map<String, String> row,
                                 int rowIndex,
                                 String field,
                                 List<String> messages) {
        String value = row.getOrDefault(field, "").trim().toLowerCase();
        if (value.isEmpty()) {
            return;
        }
        if (!value.equals("true") && !value.equals("false")) {
            messages.add("Row " + rowIndex + ": " + field + " must be true/false");
        }
    }

    public static class ExportResult {
        private final int rowCount;
        private final List<String> validationMessages;

        public ExportResult(int rowCount, List<String> validationMessages) {
            this.rowCount = rowCount;
            this.validationMessages = validationMessages;
        }

        public int getRowCount() {
            return rowCount;
        }

        public List<String> getValidationMessages() {
            return validationMessages;
        }
    }
}
