package com.teamgannon.trips.planetarymodelling.procedural.impact;

import java.util.List;

/**
 * Immutable result of impact feature (crater/volcano) calculation.
 * Contains the modified height data and metadata about placed features.
 *
 * @param modifiedHeights Heights array after crater/volcano modifications (may be same array as input if modified in place)
 * @param craterCenters Polygon indices that are crater centers
 * @param volcanoCenters Polygon indices that are volcano centers
 * @param craterProfiles Profile type used for each crater (parallel to craterCenters)
 * @param volcanoProfiles Profile type used for each volcano (parallel to volcanoCenters)
 * @param craterRadii Radius in polygon hops for each crater
 * @param volcanoRadii Radius in polygon hops for each volcano
 */
public record ImpactResult(
    double[] modifiedHeights,
    List<Integer> craterCenters,
    List<Integer> volcanoCenters,
    List<CraterProfile> craterProfiles,
    List<CraterProfile> volcanoProfiles,
    List<Integer> craterRadii,
    List<Integer> volcanoRadii
) {

    /**
     * Returns the total number of craters placed.
     */
    public int craterCount() {
        return craterCenters != null ? craterCenters.size() : 0;
    }

    /**
     * Returns the total number of volcanoes placed.
     */
    public int volcanoCount() {
        return volcanoCenters != null ? volcanoCenters.size() : 0;
    }

    /**
     * Returns the total number of impact features (craters + volcanoes).
     */
    public int totalFeatureCount() {
        return craterCount() + volcanoCount();
    }

    /**
     * Returns true if any impact features were placed.
     */
    public boolean hasFeatures() {
        return totalFeatureCount() > 0;
    }

    /**
     * Returns true if the specified polygon is a crater center.
     */
    public boolean isCraterCenter(int polygonIndex) {
        return craterCenters != null && craterCenters.contains(polygonIndex);
    }

    /**
     * Returns true if the specified polygon is a volcano center.
     */
    public boolean isVolcanoCenter(int polygonIndex) {
        return volcanoCenters != null && volcanoCenters.contains(polygonIndex);
    }

    /**
     * Creates an empty result indicating no impact features were placed.
     */
    public static ImpactResult empty(double[] heights) {
        return new ImpactResult(
            heights,
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of()
        );
    }
}
