package com.teamgannon.trips.solarsystem.nightsky;

import com.teamgannon.trips.jpa.model.StarObject;

public final class StarObjectNightSkyAdapter {

    public StarCatalogEntry fromStarObject(StarObject star) {
        if (star == null) {
            return null;
        }
        String name = selectName(star);
        double raHours = star.getRa() / 15.0;
        double decDeg = star.getDeclination();
        double magnitude = selectMagnitude(star);
        String constellation = star.getConstellationName();
        return new StarCatalogEntry(name, raHours, decDeg, magnitude, constellation);
    }

    private String selectName(StarObject star) {
        if (isNotBlank(star.getCommonName())) {
            return star.getCommonName().trim();
        }
        if (isNotBlank(star.getDisplayName())) {
            return star.getDisplayName().trim();
        }
        if (isNotBlank(star.getSystemName())) {
            return star.getSystemName().trim();
        }
        if (isNotBlank(star.getId())) {
            return star.getId().trim();
        }
        return "Unknown";
    }

    private double selectMagnitude(StarObject star) {
        double magv = star.getMagv();
        if (magv != 0.0 && !Double.isNaN(magv)) {
            return magv;
        }
        String apparent = star.getApparentMagnitude();
        if (isNotBlank(apparent)) {
            try {
                return Double.parseDouble(apparent.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return Double.POSITIVE_INFINITY;
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
