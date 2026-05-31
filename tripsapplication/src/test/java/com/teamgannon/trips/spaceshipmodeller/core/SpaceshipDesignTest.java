package com.teamgannon.trips.spaceshipmodeller.core;

import com.terranrepublic.assets.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.builder.SpaceshipBuilder;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link SpaceshipDesign} helper calculations. */
class SpaceshipDesignTest {

    private SpaceshipDesign frigate() {
        return SpaceshipBuilder.create("Test")
                .shipClass(ShipClass.FRIGATE).driveType(DriveType.FUSION_TORCH)
                .structureTons(200).engineTons(150).propellantTons(450)
                .payloadTons(50).crewTons(20).radiatorTons(120)
                .crew(4).lengthMeters(46).build();
    }

    @Test
    @DisplayName("delta-V is positive for a propellant-carrying drive")
    void deltaVPositiveForPropellantDrive() {
        double dv = frigate().estimateDeltaVKmps();
        assertTrue(dv > 0);
        assertFalse(Double.isNaN(dv));
    }

    @Test
    @DisplayName("delta-V is NaN for a reactionless drive")
    void deltaVNaNForReactionless() {
        SpaceshipDesign sail = SpaceshipBuilder.create("Sail")
                .shipClass(ShipClass.CORVETTE).driveType(DriveType.SOLAR_SAIL)
                .structureTons(1).payloadTons(1).build();
        assertTrue(Double.isNaN(sail.estimateDeltaVKmps()));
    }

    @Test
    @DisplayName("mothership detection and carried-mass total")
    void mothershipDetection() {
        SpaceshipDesign carrier = SpaceshipBuilder.create("Carrier")
                .shipClass(ShipClass.MOTHERSHIP).driveType(DriveType.NUCLEAR_THERMAL)
                .structureTons(1000).payloadTons(500)
                .carry("Viper", ShipClass.FIGHTER, 12, 8, "escort").build();
        assertTrue(carrier.isMothership());
        assertEquals(96.0, carrier.totalCarriedMassTons(), 1e-9);
        assertFalse(frigate().isMothership());
    }

    @Test
    @DisplayName("builder assigns id and creation timestamp")
    void idAndTimestampAssigned() {
        SpaceshipDesign d = frigate();
        assertNotNull(d.id());
        assertNotNull(d.createdAt());
    }

    // ==================================================================
    // v2 Phase E.1 §5.4 — defaultAccessibleNetworkIds field
    // ==================================================================

    @Test
    @DisplayName("defaultAccessibleNetworkIds defaults to empty set when 19-arg compat constructor used")
    void defaultAccessibleNetworkIdsDefaultsEmpty() {
        SpaceshipDesign d = frigate();
        assertNotNull(d.defaultAccessibleNetworkIds(),
                "defaultAccessibleNetworkIds must never be null");
        org.junit.jupiter.api.Assertions.assertTrue(d.defaultAccessibleNetworkIds().isEmpty(),
                "compat-constructor path must produce an empty set");
    }

    @Test
    @DisplayName("defaultAccessibleNetworkIds is immutable (defensive Set.copyOf in compact ctor)")
    void defaultAccessibleNetworkIdsImmutable() {
        SpaceshipDesign d = frigate();
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
                () -> d.defaultAccessibleNetworkIds().add("catalog-network-test"));
    }

    @Test
    @DisplayName("defaultAccessibleNetworkIds is defensively copied — mutating input after construction doesn't mutate the record")
    void defaultAccessibleNetworkIdsDefensivelyCopied() {
        java.util.Set<String> input = new java.util.HashSet<>();
        input.add("catalog-network-aldenata-civilian");
        SpaceshipDesign d = new SpaceshipDesign(
                "id", "Test Ship", "TS-1",
                com.teamgannon.trips.spaceshipmodeller.core.ShipClass.FRIGATE,
                com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType.FUSION_TORCH,
                new com.teamgannon.trips.spaceshipmodeller.core.MassBudget(100, 40, 150, 20, 10, 30),
                4, 80,
                java.util.List.of(), java.util.List.of(),
                "", "Test",
                com.terranrepublic.assets.SourceType.SCIENCE_FICTION, "Test", "Faction",
                false,
                com.terranrepublic.assets.OperationalState.OPERATIONAL,
                "2200", java.time.Instant.now(),
                input);
        // Mutate input after construction
        input.add("catalog-network-posleen");
        // Record's view unchanged
        org.junit.jupiter.api.Assertions.assertEquals(1, d.defaultAccessibleNetworkIds().size());
        org.junit.jupiter.api.Assertions.assertTrue(
                d.defaultAccessibleNetworkIds().contains("catalog-network-aldenata-civilian"));
        org.junit.jupiter.api.Assertions.assertFalse(
                d.defaultAccessibleNetworkIds().contains("catalog-network-posleen"));
    }
}
