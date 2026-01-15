package com.teamgannon.trips.solarsystem.nightsky;

import java.util.List;

public final class SkyDescriptionFormatter {

    private SkyDescriptionFormatter() {
    }

    public static String format(List<AltAzResult> results, SkyQueryOptions opts) {
        StringBuilder builder = new StringBuilder();
        for (AltAzResult result : results) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(result.getStar().getName())
                    .append(": alt ")
                    .append(String.format("%.1f", result.getAltitudeDeg()))
                    .append("\u00B0, az ")
                    .append(String.format("%.1f", result.getAzimuthDeg()))
                    .append("\u00B0 (mag ")
                    .append(String.format("%.2f", result.getStar().getMagnitude()))
                    .append(")");
        }
        return builder.toString();
    }

    public static String format3D(List<VisibleStarResult> results) {
        StringBuilder builder = new StringBuilder();
        for (VisibleStarResult result : results) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(resolveName(result.getStar()))
                    .append(": alt ")
                    .append(String.format("%.1f", result.getAltitudeDeg()))
                    .append("\u00B0, az ")
                    .append(String.format("%.1f", result.getAzimuthDeg()))
                    .append("\u00B0 (Vmag ")
                    .append(String.format("%.2f", result.getMagnitude()))
                    .append(", dist ")
                    .append(String.format("%.1f", result.getDistanceLy()))
                    .append(" ly)");
        }
        return builder.toString();
    }

    private static String resolveName(com.teamgannon.trips.jpa.model.StarObject star) {
        if (star == null) {
            return "Unknown";
        }
        if (star.getCommonName() != null && !star.getCommonName().trim().isEmpty()) {
            return star.getCommonName().trim();
        }
        if (star.getDisplayName() != null && !star.getDisplayName().trim().isEmpty()) {
            return star.getDisplayName().trim();
        }
        if (star.getSystemName() != null && !star.getSystemName().trim().isEmpty()) {
            return star.getSystemName().trim();
        }
        if (star.getId() != null && !star.getId().trim().isEmpty()) {
            return star.getId().trim();
        }
        return "Unknown";
    }
}
