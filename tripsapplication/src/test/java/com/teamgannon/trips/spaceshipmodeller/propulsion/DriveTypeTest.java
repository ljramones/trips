package com.teamgannon.trips.spaceshipmodeller.propulsion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for the {@link DriveType} catalogue and its {@link DriveSpecs}. */
class DriveTypeTest {

    @Test
    @DisplayName("catalogue contains 20 drives")
    void catalogueHasExpectedDriveCount() {
        assertEquals(20, DriveType.values().length);
    }

    @Test
    @DisplayName("every drive has a category and a well-formed spec")
    void everyDriveHasCategoryAndSpecs() {
        for (DriveType drive : DriveType.values()) {
            DriveSpecs s = drive.specs();
            assertNotNull(drive.category(), drive + " missing category");
            assertNotNull(s, drive + " missing specs");
            assertNotNull(s.thrustLevel());
            assertNotNull(s.radiatorLevel());
            assertTrue(s.ispMinSeconds() <= s.ispMaxSeconds(), drive + " isp range inverted");
            assertTrue(s.thrustToWeightMin() <= s.thrustToWeightMax(), drive + " T/W range inverted");
            assertTrue(s.minDryMassPercent() >= 0 && s.minDryMassPercent() <= 100);
        }
    }

    @Test
    @DisplayName("byCategory returns only matching drives")
    void byCategoryReturnsMatching() {
        List<DriveType> fusion = DriveType.byCategory(Category.FUSION);
        assertTrue(fusion.contains(DriveType.FUSION_TORCH));
        assertTrue(fusion.contains(DriveType.FUSION_PULSE));
        fusion.forEach(d -> assertEquals(Category.FUSION, d.category()));
    }

    @Test
    @DisplayName("exhaust velocity is derived from specific impulse")
    void exhaustVelocityDerivedFromIsp() {
        DriveSpecs s = DriveType.CHEMICAL_BIPROPELLANT.specs();
        double expected = s.ispMinSeconds() * DriveSpecs.STANDARD_GRAVITY / 1000.0;
        assertEquals(expected, s.exhaustVelocityMinKmps(), 1e-9);
    }

    @Test
    @DisplayName("chemical can land, gridded ion cannot")
    void landingSuitability() {
        assertTrue(DriveType.CHEMICAL_BIPROPELLANT.suitableForLanding());
        assertFalse(DriveType.ION_GRIDDED.suitableForLanding());
    }

    @Test
    @DisplayName("sails are reaction-mass-free with infinite Isp")
    void sailsAreReactionless() {
        assertTrue(DriveType.SOLAR_SAIL.reactionless());
        assertTrue(DriveType.LASER_SAIL.reactionless());
        assertTrue(Double.isInfinite(DriveType.SOLAR_SAIL.specs().ispAverageSeconds()));
    }
}
