package com.teamgannon.trips.stellarmodelling;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StarMassNormalizerTest {

    private static final double EPS = 1e-12;

    @Test
    void zeroReturnsZero() {
        assertEquals(0.0, StarMassNormalizer.toSolarMasses(0.0));
    }

    @Test
    void negativeReturnsZero() {
        assertEquals(0.0, StarMassNormalizer.toSolarMasses(-1.0));
        assertEquals(0.0, StarMassNormalizer.toSolarMasses(-1.989e30));
    }

    @Test
    void solarMassValuesPassThroughUnchanged() {
        assertEquals(1.0, StarMassNormalizer.toSolarMasses(1.0), EPS);
        assertEquals(0.5, StarMassNormalizer.toSolarMasses(0.5), EPS);
        assertEquals(150.0, StarMassNormalizer.toSolarMasses(150.0), EPS);
        // boundary: exactly 1000 is still treated as solar (predicate is strictly >)
        assertEquals(1000.0, StarMassNormalizer.toSolarMasses(1000.0), EPS);
    }

    @Test
    void kgValuesAreConvertedToSolar() {
        // one solar mass in kg → 1.0 M☉
        assertEquals(1.0, StarMassNormalizer.toSolarMasses(1.989e30), EPS);
        // 0.5 solar masses in kg → 0.5 M☉
        assertEquals(0.5, StarMassNormalizer.toSolarMasses(0.5 * 1.989e30), EPS);
    }

    @Test
    void idempotent() {
        // Once normalised, re-applying does nothing more.
        double once = StarMassNormalizer.toSolarMasses(1.989e30);
        double twice = StarMassNormalizer.toSolarMasses(once);
        assertEquals(once, twice, EPS);
    }
}
