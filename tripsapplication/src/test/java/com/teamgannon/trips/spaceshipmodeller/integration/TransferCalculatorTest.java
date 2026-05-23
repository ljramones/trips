package com.teamgannon.trips.spaceshipmodeller.integration;

import com.teamgannon.trips.spaceshipmodeller.builder.SpaceshipBuilder;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    private SpaceshipDesign electricTug() {
        return SpaceshipBuilder.create("Tug")
                .shipClass(ShipClass.FREIGHTER).driveType(DriveType.VASIMR)
                .structureTons(200).engineTons(100).propellantTons(150)
                .payloadTons(400).crewTons(20).radiatorTons(80).build();
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
    @DisplayName("different ships differ in propellant/ship-Δv for the same route (route Δv stays equal)")
    void transfersDifferByShip() {
        TransferBody earth = new TransferBody("Earth", 1.0);
        TransferBody mars = new TransferBody("Mars", 1.52);
        SpaceshipDesign chemicalLander = SpaceshipBuilder.create("Lander")
                .shipClass(ShipClass.LANDER).driveType(DriveType.CHEMICAL_BIPROPELLANT)
                .structureTons(30).engineTons(15).propellantTons(80).payloadTons(20).crewTons(5).build();
        TransferPlan a = TransferCalculator.plan(earth, mars, 1.0, chemicalLander, TransferType.HOHMANN);
        TransferPlan b = TransferCalculator.plan(earth, mars, 1.0, fusionFrigate(), TransferType.HOHMANN);

        // route-only quantities are identical (correct orbital mechanics)
        assertEquals(a.totalDeltaVKmps(), b.totalDeltaVKmps(), 1e-9);
        // ship-dependent quantities differ substantially
        assertNotEquals(a.totalPropellantTons(), b.totalPropellantTons(), 1.0);
        assertNotEquals(a.shipDeltaVKmps(), b.shipDeltaVKmps(), 1.0);
    }

    private SpaceshipDesign antimatterFrigate() {
        return SpaceshipBuilder.create("AM Frigate")
                .shipClass(ShipClass.FRIGATE).driveType(DriveType.ANTIMATTER_BEAM_CORE)
                .structureTons(300).engineTons(200).propellantTons(600)
                .payloadTons(100).crewTons(40).radiatorTons(400).build();
    }

    private SpaceshipDesign solarSail() {
        return SpaceshipBuilder.create("Sail")
                .shipClass(ShipClass.CORVETTE).driveType(DriveType.SOLAR_SAIL)
                .structureTons(1).payloadTons(1).build();
    }

    private SpaceshipDesign galaxyExplorer() {
        return SpaceshipBuilder.create("Galaxy-class Explorer")
                .shipClass(ShipClass.MOTHERSHIP).driveType(DriveType.FUSION_TORCH)
                .structureTons(2000).engineTons(800).propellantTons(3000)
                .payloadTons(1500).crewTons(400).radiatorTons(900).build();
    }

    @Test
    @DisplayName("Galaxy-class minimum-time to Uranus is MARGINAL (uses essentially all Δv/propellant)")
    void galaxyMinimumTimeToUranusIsMarginal() {
        TransferPlan plan = TransferCalculator.plan(
                new TransferBody("Earth", 1.0), new TransferBody("Uranus", 19.2), 1.0,
                galaxyExplorer(), TransferType.MINIMUM_TIME);
        assertEquals(Feasibility.MARGINAL, plan.feasibility());
        assertTrue(plan.feasible(), "marginal plans are still achievable");
    }

    @Test
    @DisplayName("every transfer type computes a plan with at least one node, no exceptions")
    void allTypesComputeWithoutError() {
        SpaceshipDesign ship = antimatterFrigate();
        TransferBody earth = new TransferBody("Earth", 1.0);
        TransferBody mars = new TransferBody("Mars", 1.52);
        for (TransferType type : TransferType.values()) {
            TransferPlan p = TransferCalculator.plan(earth, mars, 1.0, ship, type);
            assertNotNull(p, type + " produced no plan");
            assertFalse(p.nodes().isEmpty(), type + " produced no maneuver nodes");
        }
    }

    @Test
    @DisplayName("suitability gates types by drive (chemical basic, antimatter exotic, sail beam)")
    void suitabilityGating() {
        SpaceshipDesign chemical = chemicalCorvette();
        SpaceshipDesign antimatter = antimatterFrigate();
        SpaceshipDesign sail = solarSail();

        assertTrue(TransferSuitability.suitable(TransferType.HOHMANN, chemical));
        assertFalse(TransferSuitability.suitable(TransferType.BRACHISTOCHRONE, chemical));
        assertFalse(TransferSuitability.suitable(TransferType.WORMHOLE, chemical));
        assertTrue(TransferSuitability.suitable(TransferType.WORMHOLE, antimatter));
        assertTrue(TransferSuitability.suitable(TransferType.ANTIMATTER_TORCH, antimatter));
        assertTrue(TransferSuitability.suitable(TransferType.LASER_SAIL_BEAM, sail));
        assertFalse(TransferSuitability.suitable(TransferType.HOHMANN, sail));
    }

    @Test
    @DisplayName("an exotic transfer is far faster than Hohmann for the same route")
    void exoticIsFasterThanHohmann() {
        SpaceshipDesign ship = fusionFrigate();
        TransferBody earth = new TransferBody("Earth", 1.0);
        TransferBody jupiter = new TransferBody("Jupiter", 5.2);
        double hohmannDays = TransferCalculator.plan(earth, jupiter, 1.0, ship, TransferType.HOHMANN)
                .transferTimeDays();
        double relativisticDays = TransferCalculator.plan(earth, jupiter, 1.0, ship, TransferType.RELATIVISTIC)
                .transferTimeDays();
        assertTrue(relativisticDays < hohmannDays, "relativistic should be much faster than Hohmann");
    }

    @Test
    @DisplayName("type catalogue has the expected category counts")
    void typeCountsByCategory() {
        assertEquals(21, TransferType.values().length);
        assertEquals(8, TransferType.byCategory(TransferCategory.REALISTIC).size());
        assertEquals(5, TransferType.byCategory(TransferCategory.ADVANCED).size());
        assertEquals(8, TransferType.byCategory(TransferCategory.EXOTIC).size());
    }

    @Test
    @DisplayName("bi-elliptic plan has three burns summing to its total delta-V")
    void biEllipticHasThreeBurns() {
        TransferPlan plan = TransferCalculator.plan(
                new TransferBody("Earth", 1.0), new TransferBody("Neptune", 30.1), 1.0,
                fusionFrigate(), TransferType.BI_ELLIPTIC);
        assertEquals(3, plan.nodes().size());
        double sum = plan.nodes().stream().mapToDouble(ManeuverNode::deltaVKmps).sum();
        assertEquals(plan.totalDeltaVKmps(), sum, 1e-9);
        assertTrue(plan.totalDeltaVKmps() > 0);
    }

    @Test
    @DisplayName("low-thrust plan is a single continuous burn with a long duration")
    void lowThrustHasOneLongBurn() {
        TransferPlan plan = TransferCalculator.plan(
                new TransferBody("Earth", 1.0), new TransferBody("Mars", 1.52), 1.0,
                electricTug(), TransferType.LOW_THRUST_APPROX);
        assertEquals(1, plan.nodes().size());
        assertTrue(plan.transferTimeDays() > 0);
    }

    @Test
    @DisplayName("default transfer type follows the drive's thrust level")
    void defaultTypeByDrive() {
        assertEquals(TransferType.HOHMANN, TransferCalculator.defaultTypeFor(fusionFrigate()));
        assertEquals(TransferType.LOW_THRUST_APPROX, TransferCalculator.defaultTypeFor(electricTug()));
    }

    @Test
    @DisplayName("nodes carry cumulative mass; the last burn ends at dry mass")
    void nodesCarryMassAfter() {
        SpaceshipDesign ship = fusionFrigate();
        TransferPlan plan = TransferCalculator.plan(
                new TransferBody("Earth", 1.0), new TransferBody("Mars", 1.52), 1.0,
                ship, TransferType.HOHMANN);
        for (ManeuverNode n : plan.nodes()) {
            assertFalse(Double.isNaN(n.massAfterTons()));
        }
        ManeuverNode last = plan.nodes().get(plan.nodes().size() - 1);
        assertEquals(ship.massBudget().dryMassTons(), last.massAfterTons(), 1e-6);
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
