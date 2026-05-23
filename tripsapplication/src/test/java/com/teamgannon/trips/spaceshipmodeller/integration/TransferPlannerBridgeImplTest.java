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

/**
 * Tests for {@link TransferPlannerBridgeImpl#canPlan(SpaceshipDesign)}.
 * <p>
 * {@code planTransfer} is not exercised here because it shows a JavaFX alert; the precondition logic is the
 * testable part.
 */
class TransferPlannerBridgeImplTest {

    private final TransferPlannerBridge bridge = new TransferPlannerBridgeImpl();

    @Test
    @DisplayName("a valid, reaction-mass-carrying design can be planned")
    void canPlanValidDesign() {
        SpaceshipDesign frigate = SpaceshipBuilder.create("Roci")
                .shipClass(ShipClass.FRIGATE).driveType(DriveType.FUSION_TORCH)
                .structureTons(200).engineTons(150).propellantTons(450)
                .payloadTons(50).crewTons(20).radiatorTons(120).crew(4).build();
        assertTrue(bridge.canPlan(frigate));
    }

    @Test
    @DisplayName("a reactionless (sail) design cannot be planned")
    void cannotPlanReactionless() {
        SpaceshipDesign sail = SpaceshipBuilder.create("Sail")
                .shipClass(ShipClass.CORVETTE).driveType(DriveType.SOLAR_SAIL)
                .structureTons(1).payloadTons(1).build();
        assertFalse(bridge.canPlan(sail));
    }

    @Test
    @DisplayName("an invalid design cannot be planned")
    void cannotPlanInvalid() {
        SpaceshipDesign bad = SpaceshipBuilder.create("Bad")
                .shipClass(ShipClass.LANDER).driveType(DriveType.ION_GRIDDED)
                .structureTons(1).propellantTons(99).build();
        assertFalse(bridge.canPlan(bad));
    }

    @Test
    @DisplayName("null is not plannable")
    void cannotPlanNull() {
        assertFalse(bridge.canPlan(null));
    }

    @Test
    @DisplayName("estimateTransfer delegates to the calculator")
    void estimateTransferDelegates() {
        SpaceshipDesign frigate = SpaceshipBuilder.create("Roci")
                .shipClass(ShipClass.FRIGATE).driveType(DriveType.FUSION_TORCH)
                .structureTons(200).engineTons(150).propellantTons(450)
                .payloadTons(50).crewTons(20).radiatorTons(120).crew(4).build();
        TransferEstimate e = bridge.estimateTransfer(1.0, 1.52, 1.0, frigate);
        assertEquals(TransferCalculator.hohmannDeltaVKmps(1.0, 1.52, 1.0),
                e.requiredDeltaVKmps(), 1e-9);
        assertTrue(e.feasible());
    }

    @Test
    @DisplayName("createTransferPlan returns a feasible two-burn plan for a capable ship")
    void createTransferPlanDelegates() {
        SpaceshipDesign frigate = SpaceshipBuilder.create("Roci")
                .shipClass(ShipClass.FRIGATE).driveType(DriveType.FUSION_TORCH)
                .structureTons(200).engineTons(150).propellantTons(450)
                .payloadTons(50).crewTons(20).radiatorTons(120).crew(4).build();
        TransferPlan plan = bridge.createTransferPlan(
                new TransferBody("Earth", 1.0), new TransferBody("Mars", 1.52), 1.0,
                frigate, TransferType.HOHMANN);
        assertEquals(2, plan.nodes().size());
        assertTrue(plan.feasible());
    }
}
