package com.terranrepublic.assets;

import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * v2 Phase D.7 Step 3 — pins {@link Megastructure}'s contract:
 * <ul>
 *   <li>30-arg canonical constructor with the design §3.1 + Divergence D + Gap 1 (a) field list</li>
 *   <li>10 throwing-or-defaulting invariants from design §3.2 plus 3 pure defaults for SpaceAsset
 *       bookkeeping fields (designation, createdAt, modifiedAt)</li>
 *   <li>{@code kind()} returns {@link AssetKind#MEGASTRUCTURE}</li>
 *   <li>{@code source()} reads from {@code provenance.sourceUniverse()}</li>
 *   <li>{@code dryMassTons()} derives from {@code dryMassMegatons} × 10⁶</li>
 *   <li>Defensive-copy semantics for {@code secondaryFunctions} and {@code armaments}</li>
 *   <li>Sealed-permits-extension on {@link SpaceAsset} now includes Megastructure</li>
 *   <li>Parameterized coverage across every enum value in every Megastructure-relevant axis</li>
 * </ul>
 */
class MegastructureTest {

    // ----------------------------------------------------------------- helpers

    private static Megastructure withDefaults() {
        return new Megastructure(
                "id-1",
                "Test Megastructure",
                "designation-1",
                "Test description",
                "test-category",
                "test-notes",
                MegastructureArchetype.CONVERTED_ASTEROID,
                23.0,
                1.0e9,
                5000.0,
                Mobility.STATIONKEEPING,
                null,
                MegastructureOriginType.BUILT_BY_KNOWN,
                "Solar Confederation",
                null,
                2014,
                StationFunction.DEFENSIVE,
                Set.of(StationFunction.MILITARY_COMMAND),
                true,
                50000L,
                InteriorGravityType.NATURAL_MASS,
                OperationalState.OPERATIONAL,
                false,
                List.of(),
                new CatalogProvenance(SourceType.SCIENCE_FICTION, "Troy Rising", "Troy Rising",
                        CatalogOperationalStatus.FICTIONAL),
                "Solar Confederation",
                "Solar Confederation",
                TechLevel.ADVANCED,
                Instant.parse("2014-01-01T00:00:00Z"),
                Instant.parse("2014-06-01T00:00:00Z"));
    }

    /** Builds a Megastructure with every reference field nulled — exercises every default. */
    private static Megastructure withAllNullableFieldsNull() {
        return new Megastructure(
                null, null, null, null, null, null,
                null,             // archetype
                0, 0, 0,          // doubles
                null,             // mobility
                null,             // auxiliaryDrive
                null,             // originType
                null,             // builderPolity
                null, null,       // discoveryYear, constructionYear (boxed Integer)
                null,             // primaryFunction
                null,             // secondaryFunctions
                false,            // hasInteriorSetting
                0L,               // interiorPopulation
                null,             // interiorGravity
                null,             // operationalState
                false,            // concealed
                null,             // armaments
                null,             // provenance
                null, null,       // faction, allegiance
                null,             // techLevel
                null, null);      // createdAt, modifiedAt
    }

    // ------------------------------------------------------------ kind / source

    @Test
    @DisplayName("kind() returns AssetKind.MEGASTRUCTURE")
    void kindReturnsMegastructure() {
        assertEquals(AssetKind.MEGASTRUCTURE, withDefaults().kind());
    }

    @Test
    @DisplayName("source() reads from provenance.sourceUniverse()")
    void sourceReadsFromProvenance() {
        Megastructure m = withDefaults();
        assertEquals(m.provenance().sourceUniverse(), m.source());
        assertEquals("Troy Rising", m.source());
    }

    @Test
    @DisplayName("source() reflects provenance changes (via construction)")
    void sourceReflectsProvenance() {
        Megastructure base = withDefaults();
        Megastructure rebuilt = new Megastructure(
                base.id(), base.name(), base.designation(), base.description(), base.category(), base.notes(),
                base.archetype(), base.dimensionsKm(), base.dryMassMegatons(), base.internalVolumeKm3(),
                base.mobility(), base.auxiliaryDrive(),
                base.originType(), base.builderPolity(), base.discoveryYear(), base.constructionYear(),
                base.primaryFunction(), base.secondaryFunctions(),
                base.hasInteriorSetting(), base.interiorPopulation(), base.interiorGravity(),
                base.operationalState(), base.concealed(), base.armaments(),
                new CatalogProvenance(SourceType.REAL, "Earth Real", null, CatalogOperationalStatus.ACTIVE),
                base.faction(), base.allegiance(), base.techLevel(),
                base.createdAt(), base.modifiedAt());
        assertEquals("Earth Real", rebuilt.source());
    }

    // ------------------------------------------------------------ dryMassTons

    @Test
    @DisplayName("dryMassTons() derives from dryMassMegatons × 10^6 — zero")
    void dryMassTonsZero() {
        Megastructure m = withAllNullableFieldsNull();
        assertEquals(0.0, m.dryMassTons(), 0.0);
    }

    @Test
    @DisplayName("dryMassTons() derives from dryMassMegatons × 10^6 — one megaton")
    void dryMassTonsOne() {
        Megastructure base = withDefaults();
        Megastructure m = rebuildWithDryMass(base, 1.0);
        assertEquals(1_000_000.0, m.dryMassTons(), 0.0);
    }

    @Test
    @DisplayName("dryMassTons() derives from dryMassMegatons × 10^6 — 10^6 megatons (~1 teraton scale)")
    void dryMassTonsMillion() {
        Megastructure base = withDefaults();
        Megastructure m = rebuildWithDryMass(base, 1.0e6);
        assertEquals(1.0e12, m.dryMassTons(), 0.0);
    }

    @Test
    @DisplayName("dryMassTons() derives from dryMassMegatons × 10^6 — Troy-scale (~10^9 megatons)")
    void dryMassTonsTroyScale() {
        Megastructure base = withDefaults();
        Megastructure m = rebuildWithDryMass(base, 1.0e9);
        assertEquals(1.0e15, m.dryMassTons(), 0.0);
    }

    private static Megastructure rebuildWithDryMass(Megastructure b, double newDryMassMegatons) {
        return new Megastructure(
                b.id(), b.name(), b.designation(), b.description(), b.category(), b.notes(),
                b.archetype(), b.dimensionsKm(), newDryMassMegatons, b.internalVolumeKm3(),
                b.mobility(), b.auxiliaryDrive(),
                b.originType(), b.builderPolity(), b.discoveryYear(), b.constructionYear(),
                b.primaryFunction(), b.secondaryFunctions(),
                b.hasInteriorSetting(), b.interiorPopulation(), b.interiorGravity(),
                b.operationalState(), b.concealed(), b.armaments(),
                b.provenance(), b.faction(), b.allegiance(), b.techLevel(),
                b.createdAt(), b.modifiedAt());
    }

    // ------------------------------------------------------------ defaults (10 design + 3 bookkeeping)

    @Test
    @DisplayName("archetype defaults to UNKNOWN when null")
    void archetypeDefaultsUnknown() {
        assertEquals(MegastructureArchetype.UNKNOWN, withAllNullableFieldsNull().archetype());
    }

    @Test
    @DisplayName("mobility defaults to STATIONKEEPING when null (design §3.2)")
    void mobilityDefaultsStationkeeping() {
        assertEquals(Mobility.STATIONKEEPING, withAllNullableFieldsNull().mobility());
    }

    @Test
    @DisplayName("originType defaults to UNKNOWN when null")
    void originTypeDefaultsUnknown() {
        assertEquals(MegastructureOriginType.UNKNOWN, withAllNullableFieldsNull().originType());
    }

    @Test
    @DisplayName("primaryFunction defaults to UNKNOWN when null")
    void primaryFunctionDefaultsUnknown() {
        assertEquals(StationFunction.UNKNOWN, withAllNullableFieldsNull().primaryFunction());
    }

    @Test
    @DisplayName("secondaryFunctions defaults to empty immutable set when null")
    void secondaryFunctionsDefaultsEmpty() {
        Set<StationFunction> empty = withAllNullableFieldsNull().secondaryFunctions();
        assertNotNull(empty);
        assertTrue(empty.isEmpty());
        assertThrows(UnsupportedOperationException.class,
                () -> empty.add(StationFunction.RESIDENTIAL));
    }

    @Test
    @DisplayName("interiorGravity defaults to UNKNOWN when null")
    void interiorGravityDefaultsUnknown() {
        assertEquals(InteriorGravityType.UNKNOWN, withAllNullableFieldsNull().interiorGravity());
    }

    @Test
    @DisplayName("operationalState defaults to OPERATIONAL when null")
    void operationalStateDefaultsOperational() {
        assertEquals(OperationalState.OPERATIONAL, withAllNullableFieldsNull().operationalState());
    }

    @Test
    @DisplayName("armaments defaults to empty immutable list when null")
    void armamentsDefaultsEmpty() {
        List<Armament> empty = withAllNullableFieldsNull().armaments();
        assertNotNull(empty);
        assertTrue(empty.isEmpty());
        assertThrows(UnsupportedOperationException.class,
                () -> empty.add(null));
    }

    @Test
    @DisplayName("provenance defaults to CatalogProvenance.unknown() when null")
    void provenanceDefaultsUnknown() {
        assertEquals(CatalogProvenance.unknown(), withAllNullableFieldsNull().provenance());
    }

    @Test
    @DisplayName("designation defaults to \"\" when null (SpaceAsset bookkeeping)")
    void designationDefaultsEmpty() {
        assertEquals("", withAllNullableFieldsNull().designation());
    }

    @Test
    @DisplayName("createdAt defaults to a current-ish Instant when null (SpaceAsset bookkeeping)")
    void createdAtDefaultsToNow() {
        Instant before = Instant.now().minus(Duration.ofSeconds(1));
        Megastructure m = withAllNullableFieldsNull();
        Instant after = Instant.now().plus(Duration.ofSeconds(1));
        assertNotNull(m.createdAt());
        assertTrue(!m.createdAt().isBefore(before) && !m.createdAt().isAfter(after),
                "createdAt should fall within [now-1s, now+1s], was " + m.createdAt());
    }

    @Test
    @DisplayName("modifiedAt defaults to createdAt when null (SpaceAsset bookkeeping)")
    void modifiedAtDefaultsToCreatedAt() {
        Megastructure m = withAllNullableFieldsNull();
        assertSame(m.createdAt(), m.modifiedAt());
    }

    // ------------------------------------------------------------ throwing invariant

    @Test
    @DisplayName("secondaryFunctions containing primary throws IllegalArgumentException")
    void secondaryContainingPrimaryThrows() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Megastructure(
                        "id", "Name", null, null, null, null,
                        null, 0, 0, 0, null, null, null, null, null, null,
                        StationFunction.DEFENSIVE,
                        Set.of(StationFunction.DEFENSIVE, StationFunction.MILITARY_COMMAND),
                        false, 0L, null, null, false, null, null,
                        null, null, null, null, null));
        assertTrue(ex.getMessage().contains("DEFENSIVE"),
                "IAE message should name the offending primary function; was: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("secondary"),
                "IAE message should explain the rule; was: " + ex.getMessage());
    }

    // ------------------------------------------------------------ defensive copy

    @Test
    @DisplayName("mutating input secondaryFunctions set after construction does not mutate the record")
    void secondaryFunctionsDefensivelyCopied() {
        Set<StationFunction> input = new HashSet<>();
        input.add(StationFunction.RESEARCH);
        Megastructure m = new Megastructure(
                "id", "Name", null, null, null, null,
                null, 0, 0, 0, null, null, null, null, null, null,
                StationFunction.DEFENSIVE,
                input,
                false, 0L, null, null, false, null, null,
                null, null, null, null, null);
        // mutate the input AFTER construction
        input.add(StationFunction.MILITARY_COMMAND);
        // the record's view must be unchanged
        assertEquals(Set.of(StationFunction.RESEARCH), m.secondaryFunctions());
        assertFalse(m.secondaryFunctions().contains(StationFunction.MILITARY_COMMAND));
    }

    @Test
    @DisplayName("the record's secondaryFunctions is itself immutable")
    void secondaryFunctionsImmutable() {
        Megastructure m = withDefaults();
        assertThrows(UnsupportedOperationException.class,
                () -> m.secondaryFunctions().add(StationFunction.RESEARCH));
    }

    @Test
    @DisplayName("mutating input armaments list after construction does not mutate the record")
    void armamentsDefensivelyCopied() {
        List<Armament> input = new java.util.ArrayList<>();
        input.add(new Armament("Battery 1", WeaponType.LASER, 1, 1.0, 1.0, "main", null));
        Megastructure m = new Megastructure(
                "id", "Name", null, null, null, null,
                null, 0, 0, 0, null, null, null, null, null, null,
                null, null, false, 0L, null, null, false,
                input,
                null, null, null, null, null, null);
        input.add(new Armament("Battery 2", WeaponType.KINETIC_RAIL, 5, 10.0, 100.0, "secondary", null));
        assertEquals(1, m.armaments().size());
        assertEquals("Battery 1", m.armaments().get(0).name());
    }

    // ------------------------------------------------------------ sealed permits

    @Test
    @DisplayName("SpaceAsset is sealed and permits exactly four subtypes including Megastructure")
    void spaceAssetPermitsFourSubtypesIncludingMegastructure() {
        Class<?>[] permitted = SpaceAsset.class.getPermittedSubclasses();
        assertNotNull(permitted, "SpaceAsset must be sealed");
        assertEquals(4, permitted.length, "SpaceAsset should permit 4 subtypes after Phase D.7");
        Set<Class<?>> permittedSet = Set.of(permitted);
        assertTrue(permittedSet.contains(SpaceshipDesign.class));
        assertTrue(permittedSet.contains(StationDesign.class));
        assertTrue(permittedSet.contains(WeaponInstallation.class));
        assertTrue(permittedSet.contains(Megastructure.class));
    }

    @Test
    @DisplayName("SpaceAsset.class.isSealed() returns true")
    void spaceAssetIsSealed() {
        assertTrue(SpaceAsset.class.isSealed());
    }

    // ------------------------------------------------------------ parameterized: every axis

    @ParameterizedTest
    @EnumSource(MegastructureArchetype.class)
    @DisplayName("every MegastructureArchetype value can be set as the archetype")
    void everyArchetypeAccepted(MegastructureArchetype archetype) {
        Megastructure b = withDefaults();
        Megastructure m = new Megastructure(
                b.id(), b.name(), b.designation(), b.description(), b.category(), b.notes(),
                archetype, b.dimensionsKm(), b.dryMassMegatons(), b.internalVolumeKm3(),
                b.mobility(), b.auxiliaryDrive(),
                b.originType(), b.builderPolity(), b.discoveryYear(), b.constructionYear(),
                b.primaryFunction(), b.secondaryFunctions(),
                b.hasInteriorSetting(), b.interiorPopulation(), b.interiorGravity(),
                b.operationalState(), b.concealed(), b.armaments(),
                b.provenance(), b.faction(), b.allegiance(), b.techLevel(),
                b.createdAt(), b.modifiedAt());
        assertEquals(archetype, m.archetype());
    }

    @ParameterizedTest
    @EnumSource(MegastructureOriginType.class)
    @DisplayName("every MegastructureOriginType value can be set as the originType")
    void everyOriginTypeAccepted(MegastructureOriginType originType) {
        Megastructure b = withDefaults();
        Megastructure m = new Megastructure(
                b.id(), b.name(), b.designation(), b.description(), b.category(), b.notes(),
                b.archetype(), b.dimensionsKm(), b.dryMassMegatons(), b.internalVolumeKm3(),
                b.mobility(), b.auxiliaryDrive(),
                originType, b.builderPolity(), b.discoveryYear(), b.constructionYear(),
                b.primaryFunction(), b.secondaryFunctions(),
                b.hasInteriorSetting(), b.interiorPopulation(), b.interiorGravity(),
                b.operationalState(), b.concealed(), b.armaments(),
                b.provenance(), b.faction(), b.allegiance(), b.techLevel(),
                b.createdAt(), b.modifiedAt());
        assertEquals(originType, m.originType());
    }

    @ParameterizedTest
    @EnumSource(InteriorGravityType.class)
    @DisplayName("every InteriorGravityType value can be set as the interiorGravity")
    void everyInteriorGravityAccepted(InteriorGravityType gravity) {
        Megastructure b = withDefaults();
        Megastructure m = new Megastructure(
                b.id(), b.name(), b.designation(), b.description(), b.category(), b.notes(),
                b.archetype(), b.dimensionsKm(), b.dryMassMegatons(), b.internalVolumeKm3(),
                b.mobility(), b.auxiliaryDrive(),
                b.originType(), b.builderPolity(), b.discoveryYear(), b.constructionYear(),
                b.primaryFunction(), b.secondaryFunctions(),
                b.hasInteriorSetting(), b.interiorPopulation(), gravity,
                b.operationalState(), b.concealed(), b.armaments(),
                b.provenance(), b.faction(), b.allegiance(), b.techLevel(),
                b.createdAt(), b.modifiedAt());
        assertEquals(gravity, m.interiorGravity());
    }

    @ParameterizedTest
    @EnumSource(StationFunction.class)
    @DisplayName("every StationFunction value can be set as the primaryFunction")
    void everyStationFunctionAcceptedAsPrimary(StationFunction fn) {
        Megastructure b = withDefaults();
        // Empty secondary set — primary can be anything since it cannot collide with itself.
        Megastructure m = new Megastructure(
                b.id(), b.name(), b.designation(), b.description(), b.category(), b.notes(),
                b.archetype(), b.dimensionsKm(), b.dryMassMegatons(), b.internalVolumeKm3(),
                b.mobility(), b.auxiliaryDrive(),
                b.originType(), b.builderPolity(), b.discoveryYear(), b.constructionYear(),
                fn, Set.of(),
                b.hasInteriorSetting(), b.interiorPopulation(), b.interiorGravity(),
                b.operationalState(), b.concealed(), b.armaments(),
                b.provenance(), b.faction(), b.allegiance(), b.techLevel(),
                b.createdAt(), b.modifiedAt());
        assertEquals(fn, m.primaryFunction());
    }

    @ParameterizedTest
    @EnumSource(CatalogOperationalStatus.class)
    @DisplayName("every CatalogOperationalStatus value flows through provenance")
    void everyCatalogStatusAccepted(CatalogOperationalStatus status) {
        Megastructure b = withDefaults();
        CatalogProvenance prov = new CatalogProvenance(SourceType.SCIENCE_FICTION, "X", null, status);
        Megastructure m = new Megastructure(
                b.id(), b.name(), b.designation(), b.description(), b.category(), b.notes(),
                b.archetype(), b.dimensionsKm(), b.dryMassMegatons(), b.internalVolumeKm3(),
                b.mobility(), b.auxiliaryDrive(),
                b.originType(), b.builderPolity(), b.discoveryYear(), b.constructionYear(),
                b.primaryFunction(), b.secondaryFunctions(),
                b.hasInteriorSetting(), b.interiorPopulation(), b.interiorGravity(),
                b.operationalState(), b.concealed(), b.armaments(),
                prov,
                b.faction(), b.allegiance(), b.techLevel(),
                b.createdAt(), b.modifiedAt());
        assertEquals(status, m.provenance().status());
    }

    @ParameterizedTest
    @EnumSource(SourceType.class)
    @DisplayName("every SourceType value flows through provenance")
    void everySourceTypeAccepted(SourceType srcType) {
        Megastructure b = withDefaults();
        CatalogProvenance prov = new CatalogProvenance(srcType, "Origin", null, CatalogOperationalStatus.UNKNOWN);
        Megastructure m = new Megastructure(
                b.id(), b.name(), b.designation(), b.description(), b.category(), b.notes(),
                b.archetype(), b.dimensionsKm(), b.dryMassMegatons(), b.internalVolumeKm3(),
                b.mobility(), b.auxiliaryDrive(),
                b.originType(), b.builderPolity(), b.discoveryYear(), b.constructionYear(),
                b.primaryFunction(), b.secondaryFunctions(),
                b.hasInteriorSetting(), b.interiorPopulation(), b.interiorGravity(),
                b.operationalState(), b.concealed(), b.armaments(),
                prov,
                b.faction(), b.allegiance(), b.techLevel(),
                b.createdAt(), b.modifiedAt());
        assertEquals(srcType, m.provenance().sourceType());
    }

    @ParameterizedTest
    @EnumSource(Mobility.class)
    @DisplayName("every Mobility value can be set on a megastructure")
    void everyMobilityAccepted(Mobility mob) {
        Megastructure b = withDefaults();
        Megastructure m = new Megastructure(
                b.id(), b.name(), b.designation(), b.description(), b.category(), b.notes(),
                b.archetype(), b.dimensionsKm(), b.dryMassMegatons(), b.internalVolumeKm3(),
                mob, b.auxiliaryDrive(),
                b.originType(), b.builderPolity(), b.discoveryYear(), b.constructionYear(),
                b.primaryFunction(), b.secondaryFunctions(),
                b.hasInteriorSetting(), b.interiorPopulation(), b.interiorGravity(),
                b.operationalState(), b.concealed(), b.armaments(),
                b.provenance(), b.faction(), b.allegiance(), b.techLevel(),
                b.createdAt(), b.modifiedAt());
        assertEquals(mob, m.mobility());
    }

    // ------------------------------------------------------------ Divergence D — auxiliaryDrive

    @Test
    @DisplayName("auxiliaryDrive accepts a non-null DriveType (Divergence D)")
    void auxiliaryDriveAcceptsValue() {
        Megastructure b = withDefaults();
        Megastructure m = new Megastructure(
                b.id(), b.name(), b.designation(), b.description(), b.category(), b.notes(),
                b.archetype(), b.dimensionsKm(), b.dryMassMegatons(), b.internalVolumeKm3(),
                Mobility.MOBILE_LIMITED, DriveType.ORION,
                b.originType(), b.builderPolity(), b.discoveryYear(), b.constructionYear(),
                b.primaryFunction(), b.secondaryFunctions(),
                b.hasInteriorSetting(), b.interiorPopulation(), b.interiorGravity(),
                b.operationalState(), b.concealed(), b.armaments(),
                b.provenance(), b.faction(), b.allegiance(), b.techLevel(),
                b.createdAt(), b.modifiedAt());
        assertEquals(DriveType.ORION, m.auxiliaryDrive());
        assertEquals(Mobility.MOBILE_LIMITED, m.mobility());
    }

    @Test
    @DisplayName("auxiliaryDrive is allowed to be null (no throwing check at the Megastructure layer)")
    void auxiliaryDriveAllowedNull() {
        assertNotNull(withDefaults());        // sanity — default helper passes null aux drive
        assertEquals(null, withDefaults().auxiliaryDrive());
    }
}
