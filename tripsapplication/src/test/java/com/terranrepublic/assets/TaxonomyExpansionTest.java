package com.terranrepublic.assets;

import com.terranrepublic.infrastructure.NodeType;
import com.terranrepublic.infrastructure.TransportNode;
import com.teamgannon.trips.spaceshipmodeller.core.MassBudget;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.core.SourceType;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaxonomyExpansionTest {

    @Test
    void assetCanBeMarkedDerelictAndTransportNodeCanBeRelay() {
        Instant now = Instant.now();
        SpaceshipDesign derelict = new SpaceshipDesign(
                "ship-derelict",
                "Derelict Probe",
                "DP-1",
                ShipClass.PROBE,
                DriveType.NONE,
                new MassBudget(5, 0, 0, 1, 0, 0),
                0,
                12,
                List.of(),
                List.of(),
                "",
                "Abandoned survey craft",
                SourceType.UNKNOWN,
                "",
                "Unknown",
                false,
                OperationalState.DERELICT,
                "",
                now);
        TransportNode relay = new TransportNode(
                "node-relay",
                "Beacon Relay",
                "Test",
                "Terran",
                false,
                "Information relay",
                NodeType.RELAY,
                1,
                2,
                3,
                List.of(),
                0,
                true,
                0,
                now,
                now);

        assertEquals(OperationalState.DERELICT, derelict.operationalState());
        assertEquals(AssetKind.SHIP, derelict.kind());
        assertEquals(NodeType.RELAY, relay.type());
    }
}
