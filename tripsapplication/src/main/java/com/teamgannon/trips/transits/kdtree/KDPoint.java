package com.teamgannon.trips.transits.kdtree;

import org.jetbrains.annotations.NotNull;

/**
 * A point in 3D space with associated data.
 * Used as the element type for {@link KDTree3D}.
 *
 * @param coordinates the 3D coordinates [x, y, z]
 * @param data        the data associated with this point
 * @param <T>         the type of associated data
 */
public record KDPoint<T>(double @NotNull [] coordinates, @NotNull T data) {

    /**
     * Creates a KDPoint with the given coordinates and data.
     *
     * @param x    x coordinate
     * @param y    y coordinate
     * @param z    z coordinate
     * @param data the associated data
     */
    public KDPoint(double x, double y, double z, @NotNull T data) {
        this(new double[]{x, y, z}, data);
    }

    /**
     * Returns the x coordinate.
     */
    public double x() {
        return coordinates[0];
    }

    /**
     * Returns the y coordinate.
     */
    public double y() {
        return coordinates[1];
    }

    /**
     * Returns the z coordinate.
     */
    public double z() {
        return coordinates[2];
    }

    /**
     * Calculates the Euclidean distance to another point.
     *
     * @param other the other point
     * @return the distance
     */
    public double distanceTo(@NotNull KDPoint<?> other) {
        return KDTree3D.distance(this.coordinates, other.coordinates);
    }

    /**
     * Calculates the Euclidean distance to coordinates.
     *
     * @param otherCoords the other coordinates
     * @return the distance
     */
    public double distanceTo(double @NotNull [] otherCoords) {
        return KDTree3D.distance(this.coordinates, otherCoords);
    }

    @Override
    public String toString() {
        return "KDPoint[(%.3f, %.3f, %.3f) -> %s]".formatted(
                coordinates[0], coordinates[1], coordinates[2], data);
    }
}
