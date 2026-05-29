package com.terranrepublic.assets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the v2 Phase D.6 contract on {@link StationDesign}:
 * <ul>
 *   <li>Three new fields ({@code primaryFunction}, {@code secondaryFunctions}, {@code provenance})
 *       with compact-constructor invariants.</li>
 *   <li>{@code source()} is an interface override reading from {@code provenance.sourceUniverse()}
 *       — the dedicated {@code source} field was dropped.</li>
 *   <li>Compatibility constructor (the pre-D.6 27-arg signature) preserves every existing call
 *       site by wrapping the dropped {@code source} value into a default-shape
 *       {@link CatalogProvenance}.</li>
 * </ul>
 */
class StationDesignTest {

    /**
     * Builds a station via the 27-arg backwards-compatibility constructor (the pre-D.6 canonical
     * signature that included {@code source}). This routes through the 27-compat shim, exercising
     * the documented default behaviour for the three new fields.
     */
    private static StationDesign viaCompat27(String sourceLabel) {
        Instant now = Instant.parse("2025-01-01T00:00:00Z");
        return new StationDesign(
                "test-id",
                "Test Station",
                "TS-1",
                StationType.OUTPOST,
                sourceLabel,
                "Test Faction",
                false,
                "Test Allegiance",
                "Test description",
                10, 10, 100, 1, 6, 6, 1000,
                com.terranrepublic.assets.Mobility.FIXED,
                null,
                List.of(),
                List.of(),
                0,
                false,
                TechLevel.CONTEMPORARY,
                "Test category",
                OperationalState.OPERATIONAL,
                now,
                now);
    }

    /**
     * Builds a station via the canonical 29-arg constructor (explicit primary/secondary/provenance).
     */
    private static StationDesign viaCanonical29(StationFunction primary,
                                                Set<StationFunction> secondaries,
                                                CatalogProvenance provenance) {
        Instant now = Instant.parse("2025-01-01T00:00:00Z");
        return new StationDesign(
                "test-id",
                "Test Station",
                "TS-1",
                StationType.OUTPOST,
                "Test Faction",
                false,
                "Test Allegiance",
                "Test description",
                10, 10, 100, 1, 6, 6, 1000,
                com.terranrepublic.assets.Mobility.FIXED,
                null,
                List.of(),
                List.of(),
                0,
                false,
                TechLevel.CONTEMPORARY,
                "Test category",
                OperationalState.OPERATIONAL,
                now,
                now,
                primary,
                secondaries,
                provenance);
    }

    // ------------------------------------------------------------------
    // Compatibility constructor defaults
    // ------------------------------------------------------------------

    @Test
    @DisplayName("27-compat constructor defaults primaryFunction to UNKNOWN")
    void compat27DefaultsPrimaryFunctionToUnknown() {
        StationDesign d = viaCompat27("Some Universe");
        assertSame(StationFunction.UNKNOWN, d.primaryFunction());
    }

    @Test
    @DisplayName("27-compat constructor defaults secondaryFunctions to the empty set")
    void compat27DefaultsSecondaryFunctionsToEmptySet() {
        StationDesign d = viaCompat27("Some Universe");
        assertEquals(Set.of(), d.secondaryFunctions());
    }

    @Test
    @DisplayName("27-compat constructor wraps the dropped source string into provenance.sourceUniverse")
    void compat27WrapsSourceIntoProvenance() {
        StationDesign d = viaCompat27("Troy Rising");
        assertNotNull(d.provenance());
        assertSame(SourceType.UNKNOWN, d.provenance().sourceType());
        assertEquals("Troy Rising", d.provenance().sourceUniverse());
        assertSame(CatalogOperationalStatus.UNKNOWN, d.provenance().status());
    }

    // ------------------------------------------------------------------
    // source() accessor override
    // ------------------------------------------------------------------

    @Test
    @DisplayName("source() now reads from provenance.sourceUniverse (the field was dropped)")
    void sourceAccessorReadsFromProvenance() {
        StationDesign viaCompat = viaCompat27("Troy Rising");
        assertEquals("Troy Rising", viaCompat.source(),
                "compat path: source() must return the universe label that was wrapped into provenance");

        StationDesign viaCanonical = viaCanonical29(
                StationFunction.RESEARCH, Set.of(),
                new CatalogProvenance(SourceType.REAL, "Real / Proposed", null,
                        CatalogOperationalStatus.ACTIVE));
        assertEquals("Real / Proposed", viaCanonical.source(),
                "canonical path: source() must read from the provenance composite");
    }

    // ------------------------------------------------------------------
    // Compact-constructor invariant 3 — secondary cannot contain primary
    // ------------------------------------------------------------------

    @Test
    @DisplayName("secondaryFunctions containing primaryFunction throws IllegalArgumentException")
    void secondaryCannotContainPrimary() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> viaCanonical29(
                        StationFunction.RESEARCH,
                        Set.of(StationFunction.RESEARCH, StationFunction.COMMERCIAL),
                        CatalogProvenance.unknown()));
        assertTrue(ex.getMessage().contains("primary"),
                "exception message must explain the violation; got: " + ex.getMessage());
        assertTrue(ex.getMessage().contains("RESEARCH"),
                "exception message should name the offending primary value; got: " + ex.getMessage());
    }

    @Test
    @DisplayName("secondaryFunctions can contain other StationFunction values that differ from primary")
    void secondaryCanContainOtherFunctions() {
        StationDesign d = viaCanonical29(
                StationFunction.RESEARCH,
                Set.of(StationFunction.COMMERCIAL, StationFunction.RESIDENTIAL),
                CatalogProvenance.unknown());
        assertEquals(Set.of(StationFunction.COMMERCIAL, StationFunction.RESIDENTIAL),
                d.secondaryFunctions());
    }

    // ------------------------------------------------------------------
    // Compact-constructor invariant 2 — defensive copy of secondary set
    // ------------------------------------------------------------------

    @Test
    @DisplayName("secondaryFunctions is defensively copied (mutating the input set does not mutate the record)")
    void secondaryFunctionsIsDefensivelyCopied() {
        Set<StationFunction> mutable = new HashSet<>();
        mutable.add(StationFunction.COMMERCIAL);
        mutable.add(StationFunction.RESIDENTIAL);

        StationDesign d = viaCanonical29(StationFunction.RESEARCH, mutable, CatalogProvenance.unknown());
        assertNotSame(mutable, d.secondaryFunctions(),
                "the record's set must not be the same reference as the input");

        // Mutate the input set; the record's set must be unaffected.
        mutable.add(StationFunction.MILITARY_COMMAND);
        mutable.remove(StationFunction.COMMERCIAL);

        assertEquals(Set.of(StationFunction.COMMERCIAL, StationFunction.RESIDENTIAL),
                d.secondaryFunctions(),
                "post-construction mutation of the input set must not be visible on the record");
    }

    @Test
    @DisplayName("the defensive-copy set is immutable (cannot be mutated through the accessor)")
    void secondaryFunctionsIsImmutable() {
        StationDesign d = viaCanonical29(StationFunction.RESEARCH,
                Set.of(StationFunction.COMMERCIAL),
                CatalogProvenance.unknown());
        assertThrows(UnsupportedOperationException.class,
                () -> d.secondaryFunctions().add(StationFunction.RESIDENTIAL));
    }

    // ------------------------------------------------------------------
    // Compact-constructor invariants 1 + 4 — primaryFunction + provenance defaults
    // ------------------------------------------------------------------

    @Test
    @DisplayName("null primaryFunction defaults to StationFunction.UNKNOWN")
    void nullPrimaryFunctionDefaultsToUnknown() {
        StationDesign d = viaCanonical29(null, Set.of(), CatalogProvenance.unknown());
        assertSame(StationFunction.UNKNOWN, d.primaryFunction());
    }

    @Test
    @DisplayName("null secondaryFunctions defaults to the empty immutable set")
    void nullSecondaryFunctionsDefaultsToEmpty() {
        StationDesign d = viaCanonical29(StationFunction.RESEARCH, null, CatalogProvenance.unknown());
        assertEquals(Set.of(), d.secondaryFunctions());
    }

    @Test
    @DisplayName("null provenance defaults to CatalogProvenance.unknown()")
    void nullProvenanceDefaultsToUnknown() {
        StationDesign d = viaCanonical29(StationFunction.RESEARCH, Set.of(), null);
        assertNotNull(d.provenance());
        assertSame(SourceType.UNKNOWN, d.provenance().sourceType());
        assertEquals("", d.provenance().sourceUniverse());
        assertSame(CatalogOperationalStatus.UNKNOWN, d.provenance().status());
    }

    // ------------------------------------------------------------------
    // Parameterized: every StationFunction can be primaryFunction
    // ------------------------------------------------------------------

    @ParameterizedTest
    @EnumSource(StationFunction.class)
    @DisplayName("every StationFunction can be set as primaryFunction")
    void everyStationFunctionCanBePrimary(StationFunction f) {
        StationDesign d = viaCanonical29(f, Set.of(), CatalogProvenance.unknown());
        assertSame(f, d.primaryFunction());
    }
}
