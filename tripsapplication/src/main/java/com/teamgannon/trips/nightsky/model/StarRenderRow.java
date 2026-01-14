package com.teamgannon.trips.nightsky.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight star data optimized for rendering pipeline.
 * Extracted from StarObject to minimize memory footprint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StarRenderRow {
    private long starId;
    private double xLy;          // X coordinate in light years
    private double yLy;          // Y coordinate in light years
    private double zLy;          // Z coordinate in light years
    private float absMag;        // Absolute magnitude
    private float bpRpOrTeff;    // B-V color index or effective temperature
    private String spectralClass; // For color determination
    private String starName;     // Star name for display
}
