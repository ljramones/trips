package com.teamgannon.trips.workbench;

import com.teamgannon.trips.workbench.service.WorkbenchCsvService;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Serialise / deserialise a list of {@link MappingRow}s as a small two-column
 * CSV ({@code sourceField,targetField}).
 * <p>
 * Extracted from {@code DataWorkbenchController} in Phase 4.4 of the
 * codebase-review remediation. Stateless utility; the caller decides where the
 * file lives (controllers typically cache a {@code last-mapping.map.csv} under
 * the workbench cache directory) and is responsible for surfacing
 * {@link IOException}s to the user.
 *
 * <h2>File format</h2>
 * <pre>
 * sourceField,targetField
 * source1,target1
 * source2,target2
 * ...
 * </pre>
 * Values are CSV-escaped on write and unquoted on read via the shared
 * {@link WorkbenchCsvService}.
 */
public final class WorkbenchMappingPersistence {

    /** Filename used by {@link DataWorkbenchController}'s cache-dir auto-save. */
    public static final String LAST_MAPPING_FILENAME = "last-mapping.map.csv";

    private static final String HEADER = "sourceField,targetField";

    private WorkbenchMappingPersistence() {
    }

    /**
     * Load mappings from {@code path}. The first line is expected to be the
     * header and is discarded.
     *
     * @throws IOException if the file can't be read or is empty
     */
    public static List<MappingRow> load(Path path, WorkbenchCsvService csvService) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                throw new IOException("Mapping file is empty.");
            }
            List<MappingRow> rows = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = csvService.splitCsvLine(line);
                if (values.length >= 2) {
                    rows.add(new MappingRow(csvService.unquote(values[0]), csvService.unquote(values[1])));
                }
            }
            return rows;
        }
    }

    /** Write {@code mappings} to {@code path} (UTF-8, header line + one row per mapping). */
    public static void save(Path path, List<MappingRow> mappings, WorkbenchCsvService csvService) throws IOException {
        StringBuilder output = new StringBuilder();
        output.append(HEADER).append(System.lineSeparator());
        for (MappingRow mapping : mappings) {
            output.append(csvService.escapeCsv(mapping.getSourceField()))
                    .append(",")
                    .append(csvService.escapeCsv(mapping.getTargetField()))
                    .append(System.lineSeparator());
        }
        Files.writeString(path, output.toString(), StandardCharsets.UTF_8);
    }

    /**
     * Normalise a column name for fuzzy field matching: lowercase, drop all
     * non-alphanumeric characters. Used by {@code onAutoMap} so "Star Name",
     * "starName" and "STAR_NAME" all collapse to {@code starname}.
     */
    public static String normalizeFieldName(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
