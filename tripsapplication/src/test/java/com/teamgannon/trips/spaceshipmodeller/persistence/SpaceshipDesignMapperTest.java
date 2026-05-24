package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.teamgannon.trips.spaceshipmodeller.builder.SpaceshipBuilder;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.core.SourceType;
import com.teamgannon.trips.spaceshipmodeller.core.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests for {@link SpaceshipDesignMapper} domain/entity conversion (incl. carried-craft JSON). */
class SpaceshipDesignMapperTest {

    private final SpaceshipDesignMapper mapper = new SpaceshipDesignMapper();

    private SpaceshipDesign sample() {
        return SpaceshipBuilder.create("Donnager").designation("MCRN-1")
                .shipClass(ShipClass.MOTHERSHIP).driveType(DriveType.FUSION_TORCH)
                .structureTons(1000).engineTons(500).propellantTons(2000)
                .payloadTons(800).crewTons(200).radiatorTons(400)
                .crew(80).lengthMeters(500).icon("ship.png").description("flagship")
                .sourceType(SourceType.SCIENCE_FICTION).series("The Expanse")
                .carry("Viper", ShipClass.FIGHTER, 12, 8, "escort").build();
    }

    @Test
    @DisplayName("entity -> domain round-trip preserves all fields")
    void roundTripPreservesAllFields() {
        SpaceshipDesign original = sample();
        SpaceshipDesign back = mapper.toDomain(mapper.toEntity(original));
        assertEquals(original, back);
        assertEquals(SourceType.SCIENCE_FICTION, back.sourceType());
        assertEquals("The Expanse", back.series());
    }

    @Test
    @DisplayName("carried craft is serialised into the entity's JSON column")
    void carriedCraftSerialisedToJson() {
        SpaceshipEntity entity = mapper.toEntity(sample());
        assertNotNull(entity.getCarriedCraftJson());
        assertTrue(entity.getCarriedCraftJson().contains("Viper"));
    }

    @Test
    @DisplayName("a design with no carried craft round-trips to an empty list")
    void emptyCarriedCraftRoundTrips() {
        SpaceshipDesign noCraft = SpaceshipBuilder.create("Probe")
                .shipClass(ShipClass.CORVETTE).driveType(DriveType.ION_GRIDDED)
                .structureTons(10).engineTons(5).propellantTons(5).build();
        SpaceshipDesign back = mapper.toDomain(mapper.toEntity(noCraft));
        assertTrue(back.carriedCraft().isEmpty());
    }
}
