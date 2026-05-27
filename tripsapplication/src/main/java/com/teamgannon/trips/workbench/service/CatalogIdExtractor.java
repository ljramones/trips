package com.teamgannon.trips.workbench.service;

import com.teamgannon.trips.jpa.model.StarObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pure-function helpers for parsing catalog IDs, choosing a SIMBAD-friendly
 * star name, and the small string-manipulation utilities that the
 * enrichment pipeline shares across every TAP source.
 * <p>
 * Extracted from {@link WorkbenchEnrichmentService} in Phase 4.3 of the
 * codebase-review remediation (Issue 18). The original Phase 4.3 status
 * called out a per-catalogue ID-extractor utility as the missing
 * pre-requisite for the bigger "EnrichmentSource interface" split; this
 * class is that utility.
 * <p>
 * Every method is static + side-effect-free. No JPA / Spring / JavaFX
 * dependencies; safe to unit-test without any harness.
 */
public final class CatalogIdExtractor {

    private static final Pattern DIGIT_PATTERN = Pattern.compile("(\\d+)");

    private CatalogIdExtractor() {
    }

    /**
     * Pull a SIMBAD-resolvable catalog identifier out of a pipe-separated
     * catalog-id list. Preference order: {@code TYC} (most globally
     * resolvable), then any of {@code HD / HIP / HR / BD / GJ / GL / LHS /
     * 2MASS}. Returns the empty string if nothing matches.
     */
    public static String extractSimbadCatalogId(String catalogIdList) {
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

    /**
     * Pull the {@code source_id} out of a Gaia identifier string like
     * {@code "Gaia DR3 531415758077608192"}: returns the longest digit
     * sequence so we don't accidentally pick {@code "3"} from {@code "DR3"}.
     * Empty string for null / blank / no-digits input.
     */
    public static String extractGaiaSourceId(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
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

    /**
     * Find the {@code HIP } token in a pipe-separated catalog-id list and
     * return its numeric portion. Empty string if no HIP entry is present.
     */
    public static String extractHipId(String catalogIdList) {
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

    /** First digit sequence in {@code value}, or empty string if there isn't one. */
    public static String extractNumericId(String value) {
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
     * Strip surrounding quotes (via {@link TapCsvParser#unquote}) and
     * collapse runs of whitespace to single spaces — produces a stable
     * key for SIMBAD identifiers that may arrive with extra whitespace.
     */
    public static String normalizeSimbadKey(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = TapCsvParser.unquote(value).trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.replaceAll("\\s+", " ");
    }

    /**
     * Escape single quotes for safe inclusion in an ADQL string literal.
     * (ADQL uses SQL-92 quote-doubling.)
     */
    public static String escapeAdqlString(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''");
    }

    /**
     * Pick the most likely SIMBAD-resolvable name for a star: prefer
     * {@code commonName}, fall back to {@code displayName}, then to a
     * SIMBAD-friendly catalog ID. Numeric-only names are rejected at each
     * step (they're database internal IDs, not catalog identifiers).
     */
    public static String getPreferredSimbadName(StarObject star) {
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

    /** True if the trimmed string contains nothing but ASCII digits. */
    public static boolean isNumericToken(String value) {
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

    /**
     * Append {@code token} to {@code current} (with {@code separator} between
     * the two), skipping when {@code token} is already a substring of
     * {@code current}. Used to keep {@code source} / {@code notes} fields
     * from accumulating duplicates across re-enrichment runs.
     */
    public static String appendToken(String current, String token, String separator) {
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
}
