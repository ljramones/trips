package com.teamgannon.trips.transits;

import javafx.geometry.Point3D;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a transit segment indexed in the spatial index.
 * <p>
 * Each transit stores:
 * <ul>
 *   <li>The band ID it belongs to</li>
 *   <li>Source and target star names</li>
 *   <li>Start and end points</li>
 *   <li>Midpoint (used as the index key)</li>
 *   <li>Bounding radius (half the transit distance)</li>
 *   <li>Reference to the original TransitRoute</li>
 * </ul>
 *
 * @param bandId         the transit band ID this transit belongs to
 * @param sourceName     the source star name
 * @param targetName     the target star name
 * @param startPoint     the start point (source star coordinates)
 * @param endPoint       the end point (target star coordinates)
 * @param midpoint       the midpoint of the transit (used for indexing)
 * @param boundingRadius half the transit distance (for bounding sphere queries)
 * @param distance       the actual transit distance in light-years
 * @param transitRoute   reference to the original TransitRoute object
 */
public record IndexedTransit(
        @NotNull String bandId,
        @NotNull String sourceName,
        @NotNull String targetName,
        @NotNull Point3D startPoint,
        @NotNull Point3D endPoint,
        @NotNull Point3D midpoint,
        double boundingRadius,
        double distance,
        @NotNull TransitRoute transitRoute
) {

    /**
     * Creates an IndexedTransit from a TransitRoute.
     * Automatically calculates the midpoint and bounding radius.
     *
     * @param bandId       the transit band ID
     * @param transitRoute the transit route to index
     * @return a new IndexedTransit
     */
    public static @NotNull IndexedTransit create(
            @NotNull String bandId,
            @NotNull TransitRoute transitRoute) {

        Point3D start = transitRoute.getSourceEndpoint();
        Point3D end = transitRoute.getTargetEndpoint();
        Point3D mid = start.midpoint(end);
        double radius = transitRoute.getDistance() / 2.0;

        return new IndexedTransit(
                bandId,
                transitRoute.getSource().getStarName(),
                transitRoute.getTarget().getStarName(),
                start,
                end,
                mid,
                radius,
                transitRoute.getDistance(),
                transitRoute
        );
    }

    /**
     * Returns the 3D coordinates of the midpoint as an array.
     *
     * @return [x, y, z] coordinates
     */
    public double[] getMidpointCoordinates() {
        return new double[]{midpoint.getX(), midpoint.getY(), midpoint.getZ()};
    }

    /**
     * Checks if this transit's bounding sphere intersects with a query sphere.
     *
     * @param queryX      query center X
     * @param queryY      query center Y
     * @param queryZ      query center Z
     * @param queryRadius query sphere radius
     * @return true if the bounding spheres intersect
     */
    public boolean intersectsBoundingSphere(double queryX, double queryY, double queryZ, double queryRadius) {
        double dx = midpoint.getX() - queryX;
        double dy = midpoint.getY() - queryY;
        double dz = midpoint.getZ() - queryZ;
        double distanceSquared = dx * dx + dy * dy + dz * dz;
        double combinedRadius = boundingRadius + queryRadius;
        return distanceSquared <= combinedRadius * combinedRadius;
    }

    /**
     * Gets a unique key for this transit (for deduplication).
     * The key is bidirectional - A-B equals B-A.
     *
     * @return a unique key string
     */
    public @NotNull String getKey() {
        if (sourceName.compareTo(targetName) < 0) {
            return sourceName + "-" + targetName;
        } else {
            return targetName + "-" + sourceName;
        }
    }

    @Override
    public String toString() {
        return String.format("Transit[%s→%s, band=%s, dist=%.1f ly, mid=(%.1f,%.1f,%.1f)]",
                sourceName, targetName, bandId, distance,
                midpoint.getX(), midpoint.getY(), midpoint.getZ());
    }
}
