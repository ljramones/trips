package com.teamgannon.trips.spaceshipmodeller.planner;

import com.teamgannon.trips.spaceshipmodeller.integration.ManeuverNode;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferBody;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferPlan;
import com.teamgannon.trips.spaceshipmodeller.integration.TransferType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Tests for {@link SavedTransferPlan#fromComputed}. */
class SavedTransferPlanTest {

    private TransferPlan plan(boolean feasible, boolean propellantSufficient) {
        return new TransferPlan("Roci", TransferType.HOHMANN,
                new TransferBody("Earth", 1.0), new TransferBody("Mars", 1.52),
                List.of(new ManeuverNode("Departure", 2.5, 0.0, 10.0, 100.0, 90.0)),
                5.0, 18.0, 259.0, 3000.0, feasible, propellantSufficient);
    }

    @Test
    @DisplayName("a fully capable plan is FEASIBLE and carries its context")
    void feasibleStatus() {
        SavedTransferPlan s = SavedTransferPlan.fromComputed(plan(true, true), "ship-1", "sys-1", 1.0);
        assertEquals(TransferPlanStatus.FEASIBLE, s.status());
        assertEquals("Earth → Mars", s.route());
        assertEquals("ship-1", s.shipId());
        assertEquals("sys-1", s.solarSystemId());
        assertNotNull(s.id());
        assertNotNull(s.createdAt());
    }

    @Test
    @DisplayName("an under-powered plan is INSUFFICIENT_DELTA_V")
    void insufficientDeltaV() {
        assertEquals(TransferPlanStatus.INSUFFICIENT_DELTA_V,
                SavedTransferPlan.fromComputed(plan(false, false), null, null, 1.0).status());
    }

    @Test
    @DisplayName("enough delta-V but not enough propellant is INSUFFICIENT_PROPELLANT")
    void insufficientPropellant() {
        assertEquals(TransferPlanStatus.INSUFFICIENT_PROPELLANT,
                SavedTransferPlan.fromComputed(plan(true, false), null, null, 1.0).status());
    }
}
