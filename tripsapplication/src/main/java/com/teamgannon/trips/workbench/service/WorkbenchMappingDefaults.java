package com.teamgannon.trips.workbench.service;

import com.teamgannon.trips.workbench.MappingRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WorkbenchMappingDefaults {

    private WorkbenchMappingDefaults() {
    }

    public static List<MappingRow> defaultGaiaMappings(List<String> sourceFields) {
        Map<String, String> sourceByNormalized = normalizeSources(sourceFields);
        List<MappingRow> rows = new ArrayList<>();
        addMappingIfPresent(rows, sourceByNormalized, "source_id", "catalogIdList");
        addMappingIfPresent(rows, sourceByNormalized, "source_id", "Gaia DR3");
        addMappingIfPresent(rows, sourceByNormalized, "ra", "ra");
        addMappingIfPresent(rows, sourceByNormalized, "dec", "declination");
        addMappingIfPresent(rows, sourceByNormalized, "pmra", "pmra");
        addMappingIfPresent(rows, sourceByNormalized, "pmdec", "pmdec");
        addMappingIfPresent(rows, sourceByNormalized, "radial_velocity", "radialVelocity");
        addMappingIfPresent(rows, sourceByNormalized, "phot_g_mean_mag", "magr");
        addMappingIfPresent(rows, sourceByNormalized, "phot_bp_mean_mag", "magb");
        addMappingIfPresent(rows, sourceByNormalized, "phot_rp_mean_mag", "magr");
        addMappingIfPresent(rows, sourceByNormalized, "bp_rp", "bprp");
        return rows;
    }

    public static List<MappingRow> defaultSimbadMappings(List<String> sourceFields) {
        Map<String, String> sourceByNormalized = normalizeSources(sourceFields);
        List<MappingRow> rows = new ArrayList<>();
        addMappingIfPresent(rows, sourceByNormalized, "main_id", "displayName");
        addMappingIfPresent(rows, sourceByNormalized, "alt_id", "catalogIdList");
        addMappingIfPresent(rows, sourceByNormalized, "ra", "ra");
        addMappingIfPresent(rows, sourceByNormalized, "dec", "declination");
        addMappingIfPresent(rows, sourceByNormalized, "pmra", "pmra");
        addMappingIfPresent(rows, sourceByNormalized, "pmdec", "pmdec");
        addMappingIfPresent(rows, sourceByNormalized, "rvz_radvel", "radialVelocity");
        addMappingIfPresent(rows, sourceByNormalized, "sp_type", "spectralClass");
        return rows;
    }

    public static List<MappingRow> defaultVizierMappings(List<String> sourceFields) {
        Map<String, String> sourceByNormalized = normalizeSources(sourceFields);
        List<MappingRow> rows = new ArrayList<>();
        addMappingIfPresent(rows, sourceByNormalized, "raicrs", "ra");
        addMappingIfPresent(rows, sourceByNormalized, "deicrs", "declination");
        addMappingIfPresent(rows, sourceByNormalized, "ramdeg", "ra");
        addMappingIfPresent(rows, sourceByNormalized, "demdeg", "declination");
        addMappingIfPresent(rows, sourceByNormalized, "pmra", "pmra");
        addMappingIfPresent(rows, sourceByNormalized, "pmde", "pmdec");
        addMappingIfPresent(rows, sourceByNormalized, "plx", "distance");
        addMappingIfPresent(rows, sourceByNormalized, "vmag", "magv");
        addMappingIfPresent(rows, sourceByNormalized, "vtmag", "magv");
        addMappingIfPresent(rows, sourceByNormalized, "btmag", "magb");
        addMappingIfPresent(rows, sourceByNormalized, "hip", "catalogIdList");
        addMappingIfPresent(rows, sourceByNormalized, "tyc", "catalogIdList");
        return rows;
    }

    private static Map<String, String> normalizeSources(List<String> sourceFields) {
        Map<String, String> sourceByNormalized = new HashMap<>();
        for (String field : sourceFields) {
            sourceByNormalized.put(normalizeFieldName(field), field);
        }
        return sourceByNormalized;
    }

    private static String normalizeFieldName(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private static void addMappingIfPresent(List<MappingRow> rows,
                                            Map<String, String> sourceByNormalized,
                                            String sourceField,
                                            String targetField) {
        String source = sourceByNormalized.get(normalizeFieldName(sourceField));
        if (source != null) {
            rows.add(new MappingRow(source, targetField));
        }
    }
}
