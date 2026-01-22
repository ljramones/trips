package com.teamgannon.trips.routing.routemanagement;

import javafx.geometry.Point3D;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a route segment indexed in the spatial index.
 * <p>
 * Each segment stores:
 * <ul>
 *   <li>The route it belongs to (routeId)</li>
 *   <li>The segment index within the route</li>
 *   <li>Start and end points</li>
 *   <li>Midpoint (used as the index key)</li>
 *   <li>Bounding radius (half the segment length)</li>
 * </ul>
 *
 * @param routeId       the UUID of the route this segment belongs to
 * @param segmentIndex  the index of this segment within the route (0-based)
 * @param startPoint    the start point of the segment
 * @param endPoint      the end point of the segment
 * @param midpoint      the midpoint of the segment (used for indexing)
 * @param boundingRadius half the segment length (for bounding sphere queries)
 */
public record IndexedRouteSegment(
        @NotNull UUID routeId,
        int segmentIndex,
        @NotNull Point3D startPoint,
        @NotNull Point3D endPoint,
        @NotNull Point3D midpoint,
        double boundingRadius
) {

    /**
     * Creates an IndexedRouteSegment from start and end points.
     * Automatically calculates the midpoint and bounding radius.
     *
     * @param routeId      the route UUID
     * @param segmentIndex the segment index
     * @param start        the start point
     * @param end          the end point
     * @return a new IndexedRouteSegment
     */
    public static @NotNull IndexedRouteSegment create(
            @NotNull UUID routeId,
            int segmentIndex,
            @NotNull Point3D start,
            @NotNull Point3D end) {

        Point3D mid = start.midpoint(end);
        double radius = start.distance(end) / 2.0;
        return new IndexedRouteSegment(routeId, segmentIndex, start, end, mid, radius);
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
     * Checks if this segment's bounding sphere intersects with a query sphere.
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
     * Returns the segment length.
     *
     * @return the distance from start to end
     */
    public double getLength() {
        return boundingRadius * 2.0;
    }

    @Override
    public String toString() {
        return String.format("Segment[route=%s, idx=%d, mid=(%.1f,%.1f,%.1f), r=%.1f]",
                routeId.toString().substring(0, 8),
                segmentIndex,
                midpoint.getX(), midpoint.getY(), midpoint.getZ(),
                boundingRadius);
    }
}
