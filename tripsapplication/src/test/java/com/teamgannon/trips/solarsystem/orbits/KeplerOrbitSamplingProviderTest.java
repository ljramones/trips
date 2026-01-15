package com.teamgannon.trips.solarsystem.orbits;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeplerOrbitSamplingProviderTest {

    private static final double EPS = 1e-9;

    @Test
    void calculatesCircularOrbitPositions() {
        OrbitSamplingProvider provider = new KeplerOrbitSamplingProvider();

        double[] pos0 = provider.calculatePositionAu(1.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        assertEquals(1.0, pos0[0], EPS);
        assertEquals(0.0, pos0[1], EPS);
        assertEquals(0.0, pos0[2], EPS);

        double[] pos90 = provider.calculatePositionAu(1.0, 0.0, 0.0, 0.0, 0.0, 90.0);
        assertEquals(0.0, pos90[0], EPS);
        assertEquals(0.0, pos90[1], EPS);
        assertEquals(1.0, pos90[2], EPS);
    }

    @Test
    void samplesOrbitPlanePointsWithClosure() {
        OrbitSamplingProvider provider = new KeplerOrbitSamplingProvider();

        double[][] points = provider.sampleEllipsePlanePointsAu(1.0, 0.0, 4);
        assertEquals(5, points.length);
        assertEquals(points[0][0], points[4][0], EPS);
        assertEquals(points[0][1], points[4][1], EPS);
        assertEquals(points[0][2], points[4][2], EPS);
    }

    @Test
    void appliesInclinationToZAxisIntoY() {
        OrbitSamplingProvider provider = new KeplerOrbitSamplingProvider();

        double[] pos = provider.calculatePositionAu(1.0, 0.0, 90.0, 0.0, 0.0, 90.0);
        assertEquals(0.0, pos[0], EPS);
        assertEquals(-1.0, pos[1], EPS);
        assertEquals(0.0, pos[2], EPS);
    }

    @Test
    void appliesLongitudeOfAscendingNodeRotation() {
        OrbitSamplingProvider provider = new KeplerOrbitSamplingProvider();

        double[] pos = provider.calculatePositionAu(1.0, 0.0, 0.0, 90.0, 0.0, 0.0);
        assertEquals(0.0, pos[0], EPS);
        assertEquals(0.0, pos[1], EPS);
        assertEquals(-1.0, pos[2], EPS);
    }
}
