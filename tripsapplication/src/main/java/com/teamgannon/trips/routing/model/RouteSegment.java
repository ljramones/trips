package com.teamgannon.trips.routing.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@Getter
@Setter
public class RouteSegment {

    /**
     * the from point
     */
    private double[] pointA;

    /**
     * the index to start
     */
    private int indexA;

    /**
     * the to point
     */
    private double[] pointB;

    /**
     * the to index
     */
    private int indexB;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteSegment that = (RouteSegment) o;
        return (Arrays.equals(getPointA(), that.getPointA()) && Arrays.equals(getPointB(), that.getPointB())) ||
                (Arrays.equals(getPointA(), that.getPointB()) && Arrays.equals(getPointB(), that.getPointA()));
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(getPointA());
        result = 31 * result + Arrays.hashCode(getPointB());
        return result;
    }

    @Override
    public String toString() {
        return "RouteSegment{" +
                "pointA=" + Arrays.toString(pointA) +
                ", pointB=" + Arrays.toString(pointB) +
                '}';
    }
}
