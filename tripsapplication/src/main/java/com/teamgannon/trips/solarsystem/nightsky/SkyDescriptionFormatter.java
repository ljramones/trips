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
}
