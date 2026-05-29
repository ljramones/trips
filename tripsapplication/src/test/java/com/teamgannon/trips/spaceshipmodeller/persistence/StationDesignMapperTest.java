package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.teamgannon.trips.spaceshipmodeller.core.CarriedCraft;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import com.terranrepublic.assets.Armament;
import com.terranrepublic.assets.Catalog;
import com.terranrepublic.assets.CatalogOperationalStatus;
import com.terranrepublic.assets.CatalogProvenance;
import com.terranrepublic.assets.Mobility;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.SourceType;
import com.terranrepublic.assets.StationDesign;
import com.terranrepublic.assets.StationFunction;
import com.terranrepublic.assets.StationType;
import com.terranrepublic.assets.TechLevel;
import com.terranrepublic.assets.WeaponType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.util.List;
import java.util.Set;
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

    // v2 Phase D.7 Step 6 — the prior `troyRoundTrips()` test is gone: Troy is no longer a
    // StationDesign. Real-station round-trip coverage continues via `realStationRoundTrips`
    // below; Troy's round-trip is now covered by MegastructureDesignMapperTest.

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

    // ------------------------------------------------------------------
    // v2 Phase D.6 — function + provenance round-trip coverage
    // ------------------------------------------------------------------

    /**
     * Build a station via the canonical-29 constructor with the supplied function + provenance
     * triple. Everything else is a neutral fixture; only the three new fields matter to these
     * tests.
     */
    private static StationDesign withFunctionAndProvenance(StationFunction primary,
                                                           Set<StationFunction> secondaries,
                                                           CatalogProvenance provenance) {
        Instant now = Instant.parse("2025-09-01T08:00:00Z");
        return new StationDesign(
                UUID.randomUUID().toString(),
                "Phase D.6 Coverage",
                "PDC-1",
                StationType.OUTPOST,
                "Coverage Faction",
                false,
                "Coverage Allegiance",
                "Phase D.6 mapper round-trip fixture.",
                100, 80, 1.0e6, 5, 100, 50, 1.0e5,
                Mobility.FIXED, null,
                List.of(), List.of(),
                0, false,
                TechLevel.NEAR_FUTURE,
                "coverage",
                OperationalState.OPERATIONAL,
                now, now,
                primary, secondaries, provenance);
    }

    @Test
    @DisplayName("v2 Phase D.6: primaryFunction + secondaryFunctions + provenance all round-trip together")
    void allThreeNewFieldsRoundTrip() {
        StationDesign d = withFunctionAndProvenance(
                StationFunction.RESEARCH,
                Set.of(StationFunction.LOGISTICS_DEPOT, StationFunction.COMMERCIAL),
                new CatalogProvenance(SourceType.SCIENCE_FICTION, "Phase D.6 Coverage",
                        "Coverage Work", CatalogOperationalStatus.FICTIONAL));
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(d, back);
        assertEquals(StationFunction.RESEARCH, back.primaryFunction());
        assertEquals(Set.of(StationFunction.LOGISTICS_DEPOT, StationFunction.COMMERCIAL),
                back.secondaryFunctions());
        assertEquals(SourceType.SCIENCE_FICTION, back.provenance().sourceType());
        assertEquals("Phase D.6 Coverage", back.provenance().sourceUniverse());
        assertEquals("Coverage Work", back.provenance().sourceWork());
        assertEquals(CatalogOperationalStatus.FICTIONAL, back.provenance().status());
    }

    @Test
    @DisplayName("v2 Phase D.6: empty secondaryFunctions round-trips to empty set (null-safe LOB read)")
    void emptySecondaryFunctionsRoundTrips() {
        StationDesign d = withFunctionAndProvenance(StationFunction.RESEARCH, Set.of(),
                CatalogProvenance.unknown());
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(Set.of(), back.secondaryFunctions());
    }

    @Test
    @DisplayName("v2 Phase D.6: single-value secondary set round-trips")
    void singleSecondaryFunctionRoundTrips() {
        StationDesign d = withFunctionAndProvenance(StationFunction.RESEARCH,
                Set.of(StationFunction.LOGISTICS_DEPOT), CatalogProvenance.unknown());
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(Set.of(StationFunction.LOGISTICS_DEPOT), back.secondaryFunctions());
    }

    @Test
    @DisplayName("v2 Phase D.6: three-value secondary set (Tycho-shaped) round-trips")
    void threeSecondaryFunctionsRoundTrip() {
        Set<StationFunction> expected = Set.of(
                StationFunction.INDUSTRIAL, StationFunction.COMMERCIAL, StationFunction.RESIDENTIAL);
        StationDesign d = withFunctionAndProvenance(StationFunction.SHIPBUILDING, expected,
                CatalogProvenance.unknown());
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(expected, back.secondaryFunctions());
    }

    @Test
    @DisplayName("v2 Phase D.6: four-value secondary set (Citadel-shaped) round-trips")
    void fourSecondaryFunctionsRoundTrip() {
        Set<StationFunction> expected = Set.of(
                StationFunction.DIPLOMATIC, StationFunction.MILITARY_COMMAND,
                StationFunction.COMMERCIAL, StationFunction.RESIDENTIAL);
        StationDesign d = withFunctionAndProvenance(StationFunction.GOVERNMENT_ADMINISTRATION,
                expected, CatalogProvenance.unknown());
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(expected, back.secondaryFunctions());
    }

    @Test
    @DisplayName("v2 Phase D.6: null sourceWork preserved as null on round-trip")
    void nullSourceWorkPreserved() {
        StationDesign d = withFunctionAndProvenance(StationFunction.RESEARCH, Set.of(),
                new CatalogProvenance(SourceType.REAL, "Real / Proposed", null,
                        CatalogOperationalStatus.ACTIVE));
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertNull(back.provenance().sourceWork(),
                "null sourceWork is documented as the \"no specific work\" value and must round-trip as null");
    }

    @ParameterizedTest
    @EnumSource(StationFunction.class)
    @DisplayName("v2 Phase D.6: every StationFunction round-trips as primaryFunction")
    void everyPrimaryFunctionRoundTrips(StationFunction f) {
        StationDesign d = withFunctionAndProvenance(f, Set.of(), CatalogProvenance.unknown());
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(f, back.primaryFunction());
    }

    @ParameterizedTest
    @EnumSource(CatalogOperationalStatus.class)
    @DisplayName("v2 Phase D.6: every CatalogOperationalStatus round-trips through provenance")
    void everyCatalogStatusRoundTrips(CatalogOperationalStatus status) {
        // SCIENCE_FICTION + FICTIONAL is the only "real" combination; for status coverage we just
        // hold sourceType constant at REAL where status is in the {HISTORIC/ACTIVE/PLANNED/CANCELLED}
        // set, and SCIENCE_FICTION where FICTIONAL — but for the mapper round-trip, the cross-product
        // doesn't matter: every status must round-trip regardless of sourceType.
        StationDesign d = withFunctionAndProvenance(StationFunction.RESEARCH, Set.of(),
                new CatalogProvenance(SourceType.UNKNOWN, "Coverage", null, status));
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(status, back.provenance().status());
    }

    @ParameterizedTest
    @EnumSource(SourceType.class)
    @DisplayName("v2 Phase D.6: every SourceType round-trips through provenance")
    void everySourceTypeRoundTrips(SourceType type) {
        StationDesign d = withFunctionAndProvenance(StationFunction.RESEARCH, Set.of(),
                new CatalogProvenance(type, "Coverage", null, CatalogOperationalStatus.UNKNOWN));
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(type, back.provenance().sourceType());
    }

    // ------------------------------------------------------------------
    // Worked-example provenance shapes (the targets Step 6 will populate)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("worked example: Troy (SCIENCE_FICTION / Troy Rising / Troy Rising / FICTIONAL) round-trips")
    void workedExampleTroy() {
        CatalogProvenance troyProvenance = new CatalogProvenance(SourceType.SCIENCE_FICTION,
                "Troy Rising", "Troy Rising", CatalogOperationalStatus.FICTIONAL);
        StationDesign d = withFunctionAndProvenance(StationFunction.DEFENSIVE,
                Set.of(StationFunction.MILITARY_COMMAND), troyProvenance);
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(troyProvenance, back.provenance());
        assertEquals(StationFunction.DEFENSIVE, back.primaryFunction());
        assertEquals(Set.of(StationFunction.MILITARY_COMMAND), back.secondaryFunctions());
    }

    @Test
    @DisplayName("worked example: ISS (REAL / Real / Proposed / null / ACTIVE) round-trips")
    void workedExampleISS() {
        CatalogProvenance issProvenance = new CatalogProvenance(SourceType.REAL,
                "Real / Proposed", null, CatalogOperationalStatus.ACTIVE);
        StationDesign d = withFunctionAndProvenance(StationFunction.RESEARCH, Set.of(), issProvenance);
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(issProvenance, back.provenance());
        assertNull(back.provenance().sourceWork());
    }

    @Test
    @DisplayName("worked example: Mir (REAL / Real / Proposed / null / HISTORIC) round-trips")
    void workedExampleMir() {
        CatalogProvenance mirProvenance = new CatalogProvenance(SourceType.REAL,
                "Real / Proposed", null, CatalogOperationalStatus.HISTORIC);
        StationDesign d = withFunctionAndProvenance(StationFunction.RESEARCH, Set.of(), mirProvenance);
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(mirProvenance, back.provenance());
        assertEquals(CatalogOperationalStatus.HISTORIC, back.provenance().status());
    }

    @Test
    @DisplayName("worked example: Lunar Gateway (REAL / Real / Proposed / null / PLANNED) round-trips")
    void workedExampleLunarGateway() {
        CatalogProvenance lunarGatewayProvenance = new CatalogProvenance(SourceType.REAL,
                "Real / Proposed", null, CatalogOperationalStatus.PLANNED);
        StationDesign d = withFunctionAndProvenance(StationFunction.RESEARCH,
                Set.of(StationFunction.LOGISTICS_DEPOT), lunarGatewayProvenance);
        StationDesign back = mapper.toDomain(mapper.toEntity(d));
        assertEquals(lunarGatewayProvenance, back.provenance());
        assertEquals(Set.of(StationFunction.LOGISTICS_DEPOT), back.secondaryFunctions());
    }
}
