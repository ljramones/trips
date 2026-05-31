package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.teamgannon.trips.spaceshipmodeller.builder.SpaceshipBuilder;
import com.teamgannon.trips.spaceshipmodeller.core.MassBudget;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.terranrepublic.assets.SourceType;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.SpaceshipDesign;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
                .sourceType(SourceType.SCIENCE_FICTION).sourceUniverse("The Expanse").faction("MCRN").era("~2350")
                .carry("Viper", ShipClass.FIGHTER, 12, 8, "escort").build();
    }

    @Test
    @DisplayName("entity -> domain round-trip preserves all fields")
    void roundTripPreservesAllFields() {
        SpaceshipDesign original = sample();
        SpaceshipDesign back = mapper.toDomain(mapper.toEntity(original));
        assertEquals(original, back);
        assertEquals(SourceType.SCIENCE_FICTION, back.sourceType());
        assertEquals("The Expanse", back.sourceUniverse());
        assertEquals("MCRN", back.faction());
        assertEquals("~2350", back.era());
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

    // ------------------------------------------------------------------
    // Phase A0 (Constructs feature): round-trip-loss bug coverage
    //
    // Before V6 + this commit, concealed and operationalState were silently
    // dropped on persist. The bug was invisible because the existing
    // round-trip test happened to use the default values (concealed=false,
    // operationalState=OPERATIONAL) that the SpaceshipDesign compact
    // constructor reconstituted on read. These cases use non-default values
    // so the bug would have been visible.
    // ------------------------------------------------------------------

    /** Direct constructor — sidesteps the builder, which doesn't expose either field. */
    private static SpaceshipDesign withFlags(boolean concealed, OperationalState state) {
        return new SpaceshipDesign(
                UUID.randomUUID().toString(),
                "Test-" + (concealed ? "C" : "V") + "-" + state.name(),
                "TD-1",
                ShipClass.CORVETTE,
                DriveType.ION_GRIDDED,
                new MassBudget(10, 5, 5, 0, 0, 0),
                3,
                30.0,
                List.of(),
                List.of(),
                "icon.png",
                "round-trip fixture",
                SourceType.UNKNOWN,
                "",
                "Unknown",
                concealed,
                state,
                "",
                Instant.now());
    }

    @Test
    @DisplayName("concealed=true + DERELICT round-trips both fields (was silently lost pre-V6)")
    void concealedDerelictRoundTrips() {
        SpaceshipDesign back = mapper.toDomain(
                mapper.toEntity(withFlags(true, OperationalState.DERELICT)));
        assertTrue(back.concealed());
        assertEquals(OperationalState.DERELICT, back.operationalState());
    }

    @Test
    @DisplayName("concealed=false + OPERATIONAL round-trips (the default-collision case)")
    void notConcealedOperationalRoundTrips() {
        SpaceshipDesign back = mapper.toDomain(
                mapper.toEntity(withFlags(false, OperationalState.OPERATIONAL)));
        assertFalse(back.concealed());
        assertEquals(OperationalState.OPERATIONAL, back.operationalState());
    }

    @Test
    @DisplayName("concealed=true + UNDER_CONSTRUCTION round-trips both fields")
    void concealedUnderConstructionRoundTrips() {
        SpaceshipDesign back = mapper.toDomain(
                mapper.toEntity(withFlags(true, OperationalState.UNDER_CONSTRUCTION)));
        assertTrue(back.concealed());
        assertEquals(OperationalState.UNDER_CONSTRUCTION, back.operationalState());
    }

    @ParameterizedTest
    @EnumSource(OperationalState.class)
    @DisplayName("every OperationalState constant round-trips through the mapper")
    void everyOperationalStateRoundTrips(OperationalState state) {
        SpaceshipDesign back = mapper.toDomain(mapper.toEntity(withFlags(false, state)));
        assertEquals(state, back.operationalState());
    }

    // ==================================================================
    // v2 Phase E.1 §5.4 — defaultAccessibleNetworkIds round-trip
    // ==================================================================

    /** Direct constructor exposing the 20-arg canonical so the new field gets explicit values. */
    private static SpaceshipDesign withNetworks(java.util.Set<String> networkIds) {
        return new SpaceshipDesign(
                UUID.randomUUID().toString(),
                "Test-Network-Ship",
                "TN-1",
                ShipClass.CORVETTE,
                DriveType.ION_GRIDDED,
                new MassBudget(10, 5, 5, 0, 0, 0),
                3, 30.0,
                List.of(), List.of(),
                "icon.png", "round-trip fixture",
                SourceType.UNKNOWN, "", "Unknown",
                false,
                OperationalState.OPERATIONAL,
                "", Instant.now(),
                networkIds);
    }

    @Test
    @DisplayName("empty defaultAccessibleNetworkIds round-trips to empty set")
    void emptyNetworkIdsRoundTripsToEmpty() {
        SpaceshipDesign back = mapper.toDomain(mapper.toEntity(withNetworks(java.util.Set.of())));
        assertTrue(back.defaultAccessibleNetworkIds().isEmpty());
    }

    @Test
    @DisplayName("single-element defaultAccessibleNetworkIds round-trips")
    void singleNetworkIdRoundTrips() {
        java.util.Set<String> ids = java.util.Set.of("catalog-network-aldenata-civilian");
        SpaceshipDesign back = mapper.toDomain(mapper.toEntity(withNetworks(ids)));
        assertEquals(ids, back.defaultAccessibleNetworkIds());
    }

    @Test
    @DisplayName("multi-element defaultAccessibleNetworkIds round-trips")
    void multipleNetworkIdsRoundTrip() {
        java.util.Set<String> ids = java.util.Set.of(
                "catalog-network-aldenata-civilian",
                "catalog-network-aldenata-military",
                "catalog-network-posleen");
        SpaceshipDesign back = mapper.toDomain(mapper.toEntity(withNetworks(ids)));
        assertEquals(ids, back.defaultAccessibleNetworkIds());
        assertEquals(3, back.defaultAccessibleNetworkIds().size());
    }

    @Test
    @DisplayName("null defaultAccessibleNetworkIdsJson column reads back as empty set (legacy-row safety)")
    void nullColumnReadsBackAsEmpty() {
        // Build a normal entity, then null the column to simulate a row inserted before V14 ran.
        SpaceshipEntity entity = mapper.toEntity(withNetworks(java.util.Set.of()));
        entity.setDefaultAccessibleNetworkIdsJson(null);
        SpaceshipDesign back = mapper.toDomain(entity);
        assertNotNull(back.defaultAccessibleNetworkIds());
        assertTrue(back.defaultAccessibleNetworkIds().isEmpty());
    }
}
