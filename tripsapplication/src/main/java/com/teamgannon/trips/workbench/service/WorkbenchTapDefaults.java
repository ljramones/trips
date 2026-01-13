package com.teamgannon.trips.workbench.service;

public final class WorkbenchTapDefaults {

    private WorkbenchTapDefaults() {
    }

    public static String defaultGaiaQuery(int limit) {
        return """
                SELECT TOP %d
                  source_id,
                  ra,
                  dec,
                  parallax,
                  pmra,
                  pmdec,
                  radial_velocity,
                  phot_g_mean_mag,
                  phot_bp_mean_mag,
                  phot_rp_mean_mag,
                  bp_rp
                FROM gaiadr3.gaia_source
                WHERE parallax > 33.3
                  AND phot_g_mean_mag < 15
                """.formatted(limit);
    }

    public static String defaultSimbadQuery(int limit) {
        return """
                SELECT TOP %d
                  b.main_id,
                  b.ra,
                  b.dec,
                  b.pmra,
                  b.pmdec,
                  b.plx_value,
                  b.rvz_radvel,
                  b.sp_type,
                  b.otype,
                  MIN(i.id) AS alt_id
                FROM basic b
                LEFT OUTER JOIN ident i ON i.oidref = b.oid
                WHERE b.ra IS NOT NULL
                  AND b.dec IS NOT NULL
                  AND b.plx_value IS NOT NULL
                  AND b.pmra IS NOT NULL
                  AND b.pmdec IS NOT NULL
                  AND b.sp_type IS NOT NULL
                GROUP BY
                  b.main_id,
                  b.ra,
                  b.dec,
                  b.pmra,
                  b.pmdec,
                  b.plx_value,
                  b.rvz_radvel,
                  b.sp_type,
                  b.otype
                """.formatted(limit);
    }

    public static String defaultVizierHipparcosQuery(int limit) {
        return """
                SELECT TOP %d *
                FROM "I/311/hip2"
                """.formatted(limit);
    }

    public static String defaultVizierTycho2Query(int limit) {
        return """
                SELECT TOP %d *
                FROM "I/259/tyc2"
                """.formatted(limit);
    }

    public static String defaultVizierRaveQuery(int limit) {
        return """
                SELECT TOP %d *
                FROM "III/279/rave_dr5"
                """.formatted(limit);
    }

    public static String defaultVizierLamostQuery(int limit) {
        return """
                SELECT TOP %d *
                FROM "V/164/dr5"
                """.formatted(limit);
    }

    public static String defaultVizierReconsQuery(int limit) {
        return """
                SELECT TOP %d *
                FROM "J/AJ/160/215/table2"
                """.formatted(limit);
    }

    public static String vizierTableLookupQuery(String keyword, int limit) {
        String escaped = keyword.replace("'", "''");
        return """
                SELECT TOP %d
                  table_name,
                  description
                FROM TAP_SCHEMA.tables
                WHERE ivo_nocasematch(table_name, '%%%s%%') = 1
                   OR ivo_nocasematch(description, '%%%s%%') = 1
                """.formatted(limit, escaped, escaped);
    }

    public static String sanitizeVizierTableId(String input) {
        String trimmed = input.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() > 1) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed.trim();
    }
}
