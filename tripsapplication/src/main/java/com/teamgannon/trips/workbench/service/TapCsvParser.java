package com.teamgannon.trips.workbench.service;

import java.util.List;
import java.util.Map;

/**
 * Stateless helpers for parsing TAP-returned CSV bodies.
 * <p>
 * Extracted from {@code WorkbenchEnrichmentService} in Phase 4.3 of the
 * codebase-review remediation. Used by the per-source clients (Gaia, SIMBAD,
 * VizieR / Hipparcos) to turn the {@code application/csv} response into typed
 * lookups.
 *
 * <h2>Splitting rules</h2>
 * {@link #splitCsvLine(String)} preserves quoted fields containing commas — TAP
 * CSV occasionally includes commas inside identifier strings (especially from
 * SIMBAD). {@link #unquote(String)} strips surrounding double quotes and
 * converts escaped doubled quotes (RFC 4180) back to single quotes.
 */
public final class TapCsvParser {

    private TapCsvParser() {
    }

    /**
     * Split a CSV line on unquoted commas. Quoted runs (including their commas)
     * stay intact; the surrounding quotes are <em>not</em> stripped — call
     * {@link #unquote(String)} on each field if needed.
     *
     * @return zero-length array for {@code null} input; otherwise the split
     *         fields with {@code -1} limit so trailing empty fields survive.
     */
    public static String[] splitCsvLine(String line) {
        if (line == null) {
            return new String[0];
        }
        return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
    }

    /**
     * Strip surrounding double quotes (if present) and undo {@code ""} →
     * {@code "} escapes. Leading / trailing whitespace is also trimmed.
     */
    public static String unquote(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            String inner = trimmed.substring(1, trimmed.length() - 1);
            return inner.replace("\"\"", "\"");
        }
        return trimmed;
    }

    /**
     * Parse a possibly-empty / possibly-malformed CSV cell as a double.
     * Returns {@code 0.0} on null, blank, or {@link NumberFormatException}.
     */
    public static double parseDoubleSafe(String value) {
        if (value == null || value.isBlank()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * Look up the first column index in {@code headerIndex} matching any of
     * the {@code candidates} (case-insensitive). Returns {@code -1} if none
     * match — caller decides whether that's fatal.
     */
    public static int findHeaderIndex(Map<String, Integer> headerIndex, List<String> candidates) {
        for (String candidate : candidates) {
            for (Map.Entry<String, Integer> entry : headerIndex.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(candidate)) {
                    return entry.getValue();
                }
            }
        }
        return -1;
    }
}
