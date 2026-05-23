package com.teamgannon.trips.spaceshipmodeller.integration;

import com.teamgannon.trips.spaceshipmodeller.builder.SpaceshipBuilder;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for the orbital-transfer maths in {@link TransferCalculator}. */
class TransferCalculatorTest {

    private SpaceshipDesign fusionFrigate() {
        return SpaceshipBuilder.create("Roci")
                .shipClass(ShipClass.FRIGATE).driveType(DriveType.FUSION_TORCH)
                .structureTons(200).engineTons(150).propellantTons(450)
                .payloadTons(50).crewTons(20).radiatorTons(120).crew(4).build();
    }

    private SpaceshipDesign chemicalCorvette() {
        return SpaceshipBuilder.create("Tin Can")
                .shipClass(ShipClass.CORVETTE).driveType(DriveType.CHEMICAL_BIPROPELLANT)
                .structureTons(100).engineTons(50).propellantTons(100).payloadTons(20).build();
    }

    @Test
    @DisplayName("Earth->Mars Hohmann delta-V is ~5.6 km/s")
    void earthToMarsDeltaV() {
        double dv = TransferCalculator.hohmannDeltaVKmps(1.0, 1.52, 1.0);
        assertEquals(5.6, dv, 0.4);
    }

    @Test
    @DisplayName("Earth->Mars transfer time is ~259 days")
    void earthToMarsTransferTime() {
        double days = TransferCalculator.hohmannTransferTimeDays(1.0, 1.52, 1.0);
        assertEquals(259, days, 10);
    }

    @Test
    @DisplayName("same orbit needs no delta-V")
    void sameOrbitNeedsNoDeltaV() {
        assertEquals(0.0, TransferCalculator.hohmannDeltaVKmps(1.0, 1.0, 1.0), 1e-6);
    }

    @Test
    @DisplayName("Hohmann delta-V is symmetric inbound vs outbound")
    void deltaVSymmetric() {
        double out = TransferCalculator.hohmannDeltaVKmps(1.0, 5.2, 1.0);
        double in = TransferCalculator.hohmannDeltaVKmps(5.2, 1.0, 1.0);
        assertEquals(out, in, 1e-6);
    }

    @Test
    @DisplayName("a fusion torch can feasibly reach Jupiter")
    void fusionShipReachesJupiter() {
        TransferEstimate e = TransferCalculator.estimate(1.0, 5.2, 1.0, fusionFrigate());
        assertTrue(e.requiredDeltaVKmps() > 0);
        assertTrue(e.transferTimeDays() > 0);
        assertTrue(e.propellantRequiredTons() > 0);
        assertTrue(e.feasible(), "fusion torch should have ample delta-V for an inner-system transfer");
    }

    @Test
    @DisplayName("a low-delta-V chemical ship cannot reach Jupiter")
    void chemicalShipCannotReachJupiter() {
        TransferEstimate e = TransferCalculator.estimate(1.0, 5.2, 1.0, chemicalCorvette());
        assertFalse(e.feasible());
        assertTrue(e.requiredDeltaVKmps() > e.shipDeltaVKmps());
    }

    @Test
    @DisplayName("estimate via named bodies matches the AU-based estimate")
    void estimateViaBodiesMatchesAu() {
        SpaceshipDesign ship = fusionFrigate();
        TransferEstimate byBodies = TransferCalculator.estimate(
                new TransferBody("Earth", 1.0), new TransferBody("Mars", 1.52), 1.0, ship);
        TransferEstimate byAu = TransferCalculator.estimate(1.0, 1.52, 1.0, ship);
        assertEquals(byAu.requiredDeltaVKmps(), byBodies.requiredDeltaVKmps(), 1e-9);
        assertEquals(byAu.transferTimeDays(), byBodies.transferTimeDays(), 1e-9);
    }

    @Test
    @DisplayName("plan produces two burns that sum to the total delta-V")
    void planHasTwoBurns() {
        TransferPlan plan = TransferCalculator.plan(
                new TransferBody("Earth", 1.0), new TransferBody("Mars", 1.52), 1.0,
                fusionFrigate(), TransferType.HOHMANN);
        assertEquals(2, plan.nodes().size());
        double sum = plan.nodes().stream().mapToDouble(ManeuverNode::deltaVKmps).sum();
        assertEquals(plan.totalDeltaVKmps(), sum, 1e-9);
        assertEquals(TransferCalculator.hohmannDeltaVKmps(1.0, 1.52, 1.0), plan.totalDeltaVKmps(), 1e-6);
        assertTrue(plan.feasible());
        assertTrue(plan.totalPropellantTons() > 0);
        assertTrue(plan.transferTimeDays() > 0);
    }

    @Test
    @DisplayName("plan is infeasible for a low-delta-V chemical ship to Jupiter")
    void planInfeasibleForChemical() {
        TransferPlan plan = TransferCalculator.plan(
                new TransferBody("Earth", 1.0), new TransferBody("Jupiter", 5.2), 1.0,
                chemicalCorvette(), TransferType.HOHMANN);
        assertFalse(plan.feasible());
    }
}
