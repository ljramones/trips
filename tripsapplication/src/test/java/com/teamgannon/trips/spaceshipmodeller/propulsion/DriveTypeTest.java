package com.teamgannon.trips.spaceshipmodeller.propulsion;

import com.terranrepublic.assets.TransitMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for the {@link DriveType} catalogue and its {@link DriveSpecs}. */
class DriveTypeTest {

    @Test
    @DisplayName("catalogue contains 25 drives")
    void catalogueHasExpectedDriveCount() {
        assertEquals(25, DriveType.values().length);
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

    // ==================================================================
    // v2 Phase E.1 §4 — TransitMode mapping
    // ==================================================================

    @Test
    @DisplayName("every drive has a non-null TransitMode set (covers all 25 values)")
    void everyDriveHasTransitModeSet() {
        for (DriveType drive : DriveType.values()) {
            assertNotNull(drive.transitModes(), drive + " missing transit modes");
        }
    }

    @Test
    @DisplayName("transitModes() returns an immutable Set")
    void transitModesIsImmutable() {
        assertThrows(UnsupportedOperationException.class,
                () -> DriveType.ORION.transitModes().add(TransitMode.WARP));
    }

    @Test
    @DisplayName("22 of 25 drives carry exactly {SUBLIGHT}")
    void sublightOnlyDrivesPerDesign() {
        Set<DriveType> sublightOnly = Set.of(
                DriveType.CHEMICAL_BIPROPELLANT, DriveType.SOLID_ROCKET,
                DriveType.ION_GRIDDED, DriveType.HALL_EFFECT, DriveType.VASIMR,
                DriveType.NUCLEAR_THERMAL, DriveType.NUCLEAR_ELECTRIC, DriveType.GAS_CORE_NUCLEAR,
                DriveType.ORION_PULSE, DriveType.ORION,
                DriveType.FUSION_TORCH, DriveType.FUSION_PULSE, DriveType.EPSTEIN_DRIVE,
                DriveType.TERRAN_FUSION_DRIVE, DriveType.HKHRKH_THRUST,
                DriveType.ANTIMATTER_BEAM_CORE,
                DriveType.LASER_SAIL, DriveType.SOLAR_SAIL, DriveType.BUSSARD_RAMJET,
                DriveType.KTORAN_ADVANCED, DriveType.POSLEEN_NORMAL_SPACE, DriveType.SPIN_DRIVE);
        assertEquals(22, sublightOnly.size(), "design §4.3 locks 22 SUBLIGHT-only drives");
        for (DriveType drive : sublightOnly) {
            assertEquals(Set.of(TransitMode.SUBLIGHT), drive.transitModes(),
                    drive + " must be exactly {SUBLIGHT} per the design §4.3 mapping");
        }
    }

    @Test
    @DisplayName("GRTUL_GATE carries {JUMP_GATE} (Caine Riordan canon: gate-only, no onboard thrust)")
    void grtulGateIsJumpGateOnly() {
        assertEquals(Set.of(TransitMode.JUMP_GATE), DriveType.GRTUL_GATE.transitModes());
    }

    @Test
    @DisplayName("GALACTIC_HYPER carries {WARP} (continuous strategic FTL; no SUBLIGHT companion)")
    void galacticHyperIsWarpOnly() {
        assertEquals(Set.of(TransitMode.WARP), DriveType.GALACTIC_HYPER.transitModes());
    }

    @Test
    @DisplayName("NONE carries the empty set (structural absence of a drive)")
    void noneIsEmpty() {
        assertEquals(Set.of(), DriveType.NONE.transitModes());
    }

    @Test
    @DisplayName("partition: 22 SUBLIGHT-only + 1 JUMP_GATE-only + 1 WARP-only + 1 empty = 25 (full coverage)")
    void partitionMatches25() {
        long sublight = java.util.Arrays.stream(DriveType.values())
                .filter(d -> d.transitModes().equals(Set.of(TransitMode.SUBLIGHT)))
                .count();
        long jumpGate = java.util.Arrays.stream(DriveType.values())
                .filter(d -> d.transitModes().equals(Set.of(TransitMode.JUMP_GATE)))
                .count();
        long warp = java.util.Arrays.stream(DriveType.values())
                .filter(d -> d.transitModes().equals(Set.of(TransitMode.WARP)))
                .count();
        long empty = java.util.Arrays.stream(DriveType.values())
                .filter(d -> d.transitModes().isEmpty())
                .count();
        assertEquals(22, sublight);
        assertEquals(1, jumpGate);
        assertEquals(1, warp);
        assertEquals(1, empty);
        assertEquals(25, sublight + jumpGate + warp + empty,
                "the four partition cells must sum to the full 25-value enum (no DriveType outside this design's §4.3 mapping)");
    }
}
