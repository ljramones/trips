package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.teamgannon.trips.spaceshipmodeller.core.CarriedCraft;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import com.terranrepublic.assets.Armament;
import com.terranrepublic.assets.Catalog;
import com.terranrepublic.assets.Mobility;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.StationDesign;
import com.terranrepublic.assets.StationType;
import com.terranrepublic.assets.TechLevel;
import com.terranrepublic.assets.WeaponType;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comprehensive round-trip coverage for {@link StationDesignMapper}.
 *
 * <p>The test discipline here is informed by Phase A0: the spaceship mapper silently dropped two
 * fields for many months, and the existing tests didn't catch it because the sample fixture used
 * the same default values the compact constructor was reconstituting on read. The fix for that
 * pattern is to use non-default values for every field a mapper handles. v2 Phase A's prompt
 * promoted this to a hard requirement: parameterised coverage for every {@link StationType},
 * {@link Mobility}, and {@link OperationalState} constant, plus the cases where a default could
 * silently substitute (allegiance &ne; faction, auxiliaryDrive != null on a non-fixed station).
 */
class StationDesignMapperTest {

    private final StationDesignMapper mapper = new StationDesignMapper();

    // ------------------------------------------------------------------
    // Fixtures
    // ------------------------------------------------------------------

    private StationDesign sample() {
        Instant now = Instant.parse("2025-01-15T10:30:00Z");
        return new StationDesign(
                UUID.randomUUID().toString(),
                "Test Station",
                "TS-1",
                StationType.GATE_FORT,
                "Test Source",
                "Test Faction",
                false,
                "Test Allegiance",
                "Test description with non-trivial length so the LOB column actually carries content.",
                9000,
                7000,
                2.0e12,
                2000,
                150_000,
                120_000,
                1.5e11,
                Mobility.MANEUVERABLE,
                DriveType.ORION,
                List.of(new CarriedCraft("Viper", ShipClass.FIGHTER, 12, 8, "escort")),
                List.of(new Armament("Heavy laser", WeaponType.LASER, 500, 0, 0, "Anti-ship", "test")),
                5.0e7,
                true,
                TechLevel.ADVANCED,
                "gate fortification",
                OperationalState.OPERATIONAL,
                now,
                now);
    }

    /**
     * Direct constructor that exercises a specific enum combination. The compact constructor on
     * {@link StationDesign} forbids {@code auxiliaryDrive != null} when {@code mobility == FIXED},
     * so the helper threads that invariant rather than passing a fixed value.
     */
    private static StationDesign with(StationType stationType,
                                      Mobility mobility,
                                      OperationalState state,
                                      boolean concealed,
                                      String allegiance) {
        DriveType auxDrive = mobility == Mobility.FIXED ? null : DriveType.ORION;
        Instant now = Instant.parse("2025-02-20T14:45:00Z");
        return new StationDesign(
                UUID.randomUUID().toString(),
                "Coverage-" + stationType.name() + "-" + mobility.name() + "-" + state.name(),
                "CV-1",
                stationType,
                "Coverage Source",
                "Coverage Faction",
                concealed,
                allegiance,
                "Round-trip coverage fixture for the mapper.",
                500,
                400,
                1.0e9,
                50,
                1000,
                800,
                1.0e7,
                mobility,
                auxDrive,
                List.of(),
                List.of(),
                1.0e5,
                false,
                TechLevel.NEAR_FUTURE,
                "coverage",
                state,
                now,
                now);
    }

    // ------------------------------------------------------------------
    // All-fields round-trip — the existing-pattern test
    // ------------------------------------------------------------------

    @Test
    @DisplayName("entity -> domain round-trip preserves every field on a non-default sample")
    void roundTripPreservesAllFields() {
        StationDesign original = sample();
        StationDesign back = mapper.toDomain(mapper.toEntity(original));
        assertEquals(original, back);
        assertEquals(StationType.GATE_FORT, back.stationType());
        assertEquals("Test Faction", back.faction());
        assertEquals("Test Allegiance", back.allegiance());
        assertEquals(Mobility.MANEUVERABLE, back.mobility());
        assertEquals(DriveType.ORION, back.auxiliaryDrive());
        assertEquals(TechLevel.ADVANCED, back.techLevel());
        assertEquals(OperationalState.OPERATIONAL, back.operationalState());
    }

    @Test
    @DisplayName("Catalog.TROY (canonical station seed) round-trips intact")
    void troyRoundTrips() {
        StationDesign troy = (StationDesign) Catalog.TROY;
        StationDesign back = mapper.toDomain(mapper.toEntity(troy));
        assertEquals(troy, back, "TROY is the seed source for V7; round-trip-loss here would corrupt seed data");
    }

    @org.junit.jupiter.params.ParameterizedTest(name = "{0}")
    @org.junit.jupiter.params.provider.MethodSource("realStations")
    @DisplayName("every Phase D.5 real station from Catalog round-trips intact")
    void realStationRoundTrips(String label, StationDesign station) {
        StationDesign back = mapper.toDomain(mapper.toEntity(station));
        assertEquals(station, back,
                "real-station seed " + label + " must round-trip without field loss");
    }

    static java.util.stream.Stream<org.junit.jupiter.params.provider.Arguments> realStations() {
        return Catalog.all().stream()
                .filter(StationDesign.class::isInstance)
                .map(a -> (StationDesign) a)
                .filter(s -> "Real / Proposed".equals(s.source()))
                .map(s -> org.junit.jupiter.params.provider.Arguments.of(s.name(), s));
    }

    // ------------------------------------------------------------------
    // Collection LOB columns
    // ------------------------------------------------------------------

    @Test
    @DisplayName("carriedCraft and armaments are serialised into the entity's JSON LOB columns")
    void collectionsSerialisedToLobs() {
        StationEntity entity = mapper.toEntity(sample());
        assertNotNull(entity.getCarriedCraftJson());
        assertTrue(entity.getCarriedCraftJson().contains("Viper"));
        assertNotNull(entity.getArmamentsJson());
        assertTrue(entity.getArmamentsJson().contains("Heavy laser"));
    }

    @Test
    @DisplayName("a station with no carried craft or armaments round-trips to empty lists, not null")
    void emptyCollectionsRoundTripToEmptyLists() {
        StationDesign empty = with(StationType.OUTPOST, Mobility.FIXED, OperationalState.OPERATIONAL, false, "F");
        StationDesign back = mapper.toDomain(mapper.toEntity(empty));
        assertTrue(back.carriedCraft().isEmpty());
        assertTrue(back.armaments().isEmpty());
    }

    @Test
    @DisplayName("null carriedCraftJson + null armamentsJson on the entity surface as empty lists (legacy-row safety)")
    void nullLobsDefaultToEmptyLists() {
        StationEntity entity = mapper.toEntity(sample());
        entity.setCarriedCraftJson(null);
        entity.setArmamentsJson(null);
        StationDesign back = mapper.toDomain(entity);
        assertTrue(back.carriedCraft().isEmpty(), "null LOB should not NPE");
        assertTrue(back.armaments().isEmpty(), "null LOB should not NPE");
    }

    // ------------------------------------------------------------------
    // Phase A0 round-trip-loss-class coverage
    // ------------------------------------------------------------------

    @Test
    @DisplayName("concealed=true survives the round-trip (was the lost-field bug class on spaceships)")
    void concealedRoundTrips() {
        StationDesign d = with(StationType.PIRATE_BASE, Mobility.STATIONKEEPING, OperationalState.OPERATIONAL,
                true, "Pirates");
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertTrue(back.concealed(), "concealed=true must round-trip");
    }

    @Test
    @DisplayName("allegiance distinct from faction is preserved across the round-trip")
    void allegianceDistinctFromFactionRoundTrips() {
        StationDesign d = with(StationType.OUTPOST, Mobility.FIXED, OperationalState.OPERATIONAL,
                false, "Captured-By-Pirates");
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertEquals("Captured-By-Pirates", back.allegiance(),
                "allegiance default-substitutes from faction; non-default values must survive");
        assertFalse(back.allegiance().equals(back.faction()),
                "this is the non-default case the round-trip must not collapse");
    }

    @Test
    @DisplayName("auxiliaryDrive != null on a non-fixed station round-trips intact")
    void auxiliaryDriveRoundTripsForNonFixedStations() {
        StationDesign d = with(StationType.BATTLESTATION, Mobility.MANEUVERABLE, OperationalState.OPERATIONAL,
                false, "Coverage Faction");
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(DriveType.ORION, back.auxiliaryDrive());
    }

    @Test
    @DisplayName("auxiliaryDrive stays null on a FIXED station (domain invariant)")
    void auxiliaryDriveNullOnFixedStations() {
        StationDesign d = with(StationType.HABITAT, Mobility.FIXED, OperationalState.OPERATIONAL,
                false, "Coverage Faction");
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertNull(back.auxiliaryDrive(),
                "the domain compact constructor forbids auxiliaryDrive on FIXED; mapper must not invent one");
    }

    // ------------------------------------------------------------------
    // Enum constant coverage — every value, in case the schema or
    // EnumType.STRING mapping changes upstream.
    // ------------------------------------------------------------------

    @ParameterizedTest
    @EnumSource(StationType.class)
    @DisplayName("every StationType constant round-trips through the mapper")
    void everyStationTypeRoundTrips(StationType stationType) {
        StationDesign d = with(stationType, Mobility.FIXED, OperationalState.OPERATIONAL, false, "F");
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(stationType, back.stationType());
    }

    @ParameterizedTest
    @EnumSource(Mobility.class)
    @DisplayName("every Mobility constant round-trips through the mapper")
    void everyMobilityRoundTrips(Mobility mobility) {
        StationDesign d = with(StationType.OUTPOST, mobility, OperationalState.OPERATIONAL, false, "F");
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(mobility, back.mobility());
    }

    @ParameterizedTest
    @EnumSource(OperationalState.class)
    @DisplayName("every OperationalState constant round-trips through the mapper")
    void everyOperationalStateRoundTrips(OperationalState state) {
        StationDesign d = with(StationType.OUTPOST, Mobility.FIXED, state, false, "F");
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(state, back.operationalState());
    }

    @ParameterizedTest
    @EnumSource(TechLevel.class)
    @DisplayName("every TechLevel constant round-trips through the mapper")
    void everyTechLevelRoundTrips(TechLevel techLevel) {
        Instant now = Instant.parse("2025-03-10T09:00:00Z");
        StationDesign d = new StationDesign(
                UUID.randomUUID().toString(),
                "TL-" + techLevel.name(),
                "T-1",
                StationType.OUTPOST,
                "src",
                "F",
                false,
                "F",
                "desc",
                100, 80, 1.0e6, 5, 100, 50, 1.0e5,
                Mobility.FIXED, null,
                List.of(), List.of(),
                0, false,
                techLevel,
                "cat",
                OperationalState.OPERATIONAL,
                now, now);
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(techLevel, back.techLevel());
    }
}
