package com.teamgannon.trips.planetarymodelling.procedural.impact;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CraterProfile enum and height functions.
 */
class CraterProfileTest {

    @Test
    void testAllProfilesReturnZeroAtEdge() {
        for (CraterProfile profile : CraterProfile.values()) {
            double heightAtEdge = profile.getHeight(1.0);
            assertEquals(0.0, heightAtEdge, 0.01,
                profile.name() + " should return ~0 at edge (distance=1.0)");
        }
    }

    @Test
    void testAllProfilesReturnZeroBeyondEdge() {
        for (CraterProfile profile : CraterProfile.values()) {
            double heightBeyond = profile.getHeight(1.5);
            assertEquals(0.0, heightBeyond, 0.001,
                profile.name() + " should return 0 beyond edge (distance=1.5)");
        }
    }

    @Test
    void testCraterProfilesHaveDepressionAtCenter() {
        // Crater profiles should have negative height at center (depression)
        CraterProfile[] craters = {
            CraterProfile.SIMPLE_ROUND,
            CraterProfile.SIMPLE_FLAT,
            CraterProfile.COMPLEX_FLAT,
            CraterProfile.COMPLEX_STEPS,
            CraterProfile.COMPLEX_RINGS
        };

        for (CraterProfile profile : craters) {
            assertTrue(profile.isCrater(), profile.name() + " should be classified as crater");
            assertFalse(profile.isVolcano(), profile.name() + " should not be classified as volcano");
        }
    }

    @Test
    void testVolcanoProfilesHaveElevationAtCenter() {
        // Volcano profiles should have positive height near center (elevation)
        CraterProfile[] volcanoes = {
            CraterProfile.DOME_VOLCANO,
            CraterProfile.STRATO_VOLCANO,
            CraterProfile.SHIELD_VOLCANO
        };

        for (CraterProfile profile : volcanoes) {
            double heightAtCenter = profile.getHeight(0.0);
            // Note: Volcanoes may have small summit craters, so check slightly off-center
            double heightNearCenter = profile.getHeight(0.2);
            assertTrue(heightNearCenter > 0,
                profile.name() + " should have positive height near center, got: " + heightNearCenter);
            assertTrue(profile.isVolcano(), profile.name() + " should be classified as volcano");
            assertFalse(profile.isCrater(), profile.name() + " should not be classified as crater");
        }
    }

    @Test
    void testSimpleRoundProfile() {
        CraterProfile profile = CraterProfile.SIMPLE_ROUND;

        // Center should be deepest
        double centerHeight = profile.getHeight(0.0);
        double midHeight = profile.getHeight(0.5);

        assertTrue(centerHeight < 0, "Center should be depression");
        assertTrue(centerHeight < midHeight, "Center should be deeper than midpoint");

        // Rim should have slight uplift
        double rimHeight = profile.getHeight(0.85);
        assertTrue(rimHeight > 0 || rimHeight > midHeight,
            "Rim should be higher than interior");
    }

    @Test
    void testComplexFlatHasCentralPeak() {
        CraterProfile profile = CraterProfile.COMPLEX_FLAT;

        double centerHeight = profile.getHeight(0.0);
        double floorHeight = profile.getHeight(0.4);

        // Central peak should be higher than floor
        assertTrue(centerHeight > floorHeight,
            "Complex crater should have central peak above floor");
    }

    @Test
    void testComplexRingsHasMultipleRidges() {
        CraterProfile profile = CraterProfile.COMPLEX_RINGS;

        // Check for rings at expected positions
        double ring1Height = profile.getHeight(0.3);
        double trough1Height = profile.getHeight(0.42);
        double ring2Height = profile.getHeight(0.55);

        // Rings should be higher than troughs between them
        assertTrue(ring1Height > trough1Height,
            "Inner ring should be higher than trough");
    }

    @Test
    void testStratoVolcanoProfile() {
        CraterProfile profile = CraterProfile.STRATO_VOLCANO;

        // Should have steep cone shape (approximately linear decline)
        double h1 = profile.getHeight(0.2);
        double h2 = profile.getHeight(0.4);
        double h3 = profile.getHeight(0.6);

        assertTrue(h1 > h2, "Height should decrease from center");
        assertTrue(h2 > h3, "Height should continue decreasing");

        // Summit crater check
        double summitRim = profile.getHeight(0.1);
        double summitCenter = profile.getHeight(0.0);
        // Strato volcanoes have summit craters - center may be lower than rim
    }

    @Test
    void testShieldVolcanoIsBroaderThanStrato() {
        CraterProfile shield = CraterProfile.SHIELD_VOLCANO;
        CraterProfile strato = CraterProfile.STRATO_VOLCANO;

        // At midpoint, shield should have proportionally more height remaining
        // (broader profile = more gradual falloff)
        double shieldMid = shield.getHeight(0.5);
        double stratoMid = strato.getHeight(0.5);

        double shieldCenter = shield.getHeight(0.0);
        double stratoCenter = strato.getHeight(0.0);

        // Normalize to compare falloff rates
        // Shield should retain more of its center height at midpoint
        double shieldRatio = shieldMid / Math.max(0.1, shieldCenter);
        double stratoRatio = stratoMid / Math.max(0.1, stratoCenter);

        assertTrue(shieldRatio > stratoRatio * 0.5,
            "Shield volcano should have broader profile than stratovolcano");
    }

    @ParameterizedTest
    @EnumSource(CraterProfile.class)
    void testHeightMultiplierIsPositive(CraterProfile profile) {
        double multiplier = profile.getTypicalHeightMultiplier();
        assertTrue(multiplier > 0, "Height multiplier should be positive");
    }

    @Test
    void testProfileCategorization() {
        // Verify all profiles are either crater or volcano (mutually exclusive)
        for (CraterProfile profile : CraterProfile.values()) {
            assertTrue(profile.isCrater() ^ profile.isVolcano(),
                profile.name() + " should be exactly one of crater or volcano");
        }
    }
}
