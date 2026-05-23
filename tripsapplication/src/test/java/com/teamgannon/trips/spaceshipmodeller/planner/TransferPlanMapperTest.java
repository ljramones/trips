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
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link TransferPlanMapper}, including maneuver-node JSON round-tripping. */
class TransferPlanMapperTest {

    private final TransferPlanMapper mapper = new TransferPlanMapper();

    private SavedTransferPlan sample() {
        TransferPlan plan = new TransferPlan("Roci", TransferType.HOHMANN,
                new TransferBody("Earth", 1.0), new TransferBody("Mars", 1.52),
                List.of(new ManeuverNode("Departure", 2.5, 0.0, 10.0, 100.0, 90.0),
                        new ManeuverNode("Arrival", 2.0, 259.0, 8.0, 80.0, 82.0)),
                4.5, 18.0, 30.0, 259.0, 3000.0, true, true);
        return SavedTransferPlan.fromComputed(plan, "ship-1", "sys-1", 1.0);
    }

    @Test
    @DisplayName("entity -> domain round-trip preserves all fields")
    void roundTripPreservesAllFields() {
        SavedTransferPlan original = sample();
        SavedTransferPlan back = mapper.toDomain(mapper.toEntity(original));
        assertEquals(original, back);
    }

    @Test
    @DisplayName("maneuver nodes are serialised into the entity JSON column")
    void nodesSerialisedToJson() {
        TransferPlanEntity entity = mapper.toEntity(sample());
        assertNotNull(entity.getManeuverNodesJson());
        assertTrue(entity.getManeuverNodesJson().contains("Departure"));
        assertTrue(entity.getManeuverNodesJson().contains("Arrival"));
    }
}
