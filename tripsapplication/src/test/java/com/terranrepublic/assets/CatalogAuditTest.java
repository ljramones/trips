package com.terranrepublic.assets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Phase D.6 Step 8 — catalog audit invariants.
 *
 * <p>Pins the explicit function + provenance values that landed in Step 6 for the nine catalogued
 * {@link StationDesign} entries (TROY + 8 real space stations). Splits cleanly from the existing
 * {@code CatalogTest}: that class checks the catalog's content shape (counts, kind discriminators,
 * weapon-installation labels). This class enforces the cross-cutting invariants that prevent seed
 * data drift.
 *
 * <p>Each invariant is its own {@code @Test} method so a regression pinpoints which contract
 * broke. The global iteration tests collect every offender into a list and fail with the full
 * set so a single run surfaces all violations at once.
 */
class CatalogAuditTest {

    private static List<StationDesign> stations() {
        return Catalog.all().stream()
                .filter(StationDesign.class::isInstance)
                .map(StationDesign.class::cast)
                .toList();
    }

    /**
     * Run {@code check} against every catalogued station; if any violate the predicate, fail with
     * a multi-line message naming each offender + the requested context.
     */
    private static void enforce(String invariantName, java.util.function.Predicate<StationDesign> violates,
                                java.util.function.Function<StationDesign, String> contextFor) {
        List<String> offenders = new ArrayList<>();
        for (StationDesign s : stations()) {
            if (violates.test(s)) {
                offenders.add(s.name() + " — " + contextFor.apply(s));
            }
        }
        if (!offenders.isEmpty()) {
            fail(invariantName + " violated by " + offenders.size() + " entr(y/ies):\n  - "
                    + String.join("\n  - ", offenders));
        }
    }

    // ==================================================================
    // Global assertions over every StationDesign in Catalog.all()
    // ==================================================================

    @Test
    @DisplayName("audit 1 — every catalog station has primaryFunction != UNKNOWN")
    void everyStationHasNonUnknownPrimaryFunction() {
        enforce("primaryFunction must not be UNKNOWN",
                s -> s.primaryFunction() == StationFunction.UNKNOWN,
                s -> "primaryFunction=" + s.primaryFunction());
    }

    @Test
    @DisplayName("audit 2 — no catalog station has secondaryFunctions containing its primaryFunction")
    void noSecondarySetContainsPrimary() {
        // The compact constructor would have rejected this at construction time, but the catalog-
        // level check is defense-in-depth: a future change to the invariant location must not
        // leave this constraint silently weakened.
        enforce("secondaryFunctions must not contain primaryFunction",
                s -> s.secondaryFunctions().contains(s.primaryFunction()),
                s -> "primary=" + s.primaryFunction() + ", secondaries=" + s.secondaryFunctions());
    }

    @Test
    @DisplayName("audit 3 — every catalog station has a non-blank provenance.sourceUniverse")
    void everyStationHasNonBlankSourceUniverse() {
        enforce("provenance.sourceUniverse must be non-blank",
                s -> s.provenance().sourceUniverse() == null
                        || s.provenance().sourceUniverse().isBlank(),
                s -> "sourceUniverse=\"" + s.provenance().sourceUniverse() + "\"");
    }

    @Test
    @DisplayName("audit 4 — REAL provenance entries have a real-line status (HISTORIC / ACTIVE / PLANNED / CANCELLED)")
    void realProvenanceHasRealLineStatus() {
        Set<CatalogOperationalStatus> realLine = Set.of(
                CatalogOperationalStatus.HISTORIC,
                CatalogOperationalStatus.ACTIVE,
                CatalogOperationalStatus.PLANNED,
                CatalogOperationalStatus.CANCELLED);
        enforce("REAL provenance must carry a real-line status",
                s -> s.provenance().sourceType() == SourceType.REAL
                        && !realLine.contains(s.provenance().status()),
                s -> "sourceType=REAL, status=" + s.provenance().status());
    }

    @Test
    @DisplayName("audit 5 — SCIENCE_FICTION provenance entries have status == FICTIONAL")
    void scienceFictionProvenanceHasFictionalStatus() {
        enforce("SCIENCE_FICTION provenance must carry FICTIONAL status",
                s -> s.provenance().sourceType() == SourceType.SCIENCE_FICTION
                        && s.provenance().status() != CatalogOperationalStatus.FICTIONAL,
                s -> "sourceType=SCIENCE_FICTION, status=" + s.provenance().status());
    }

    @Test
    @DisplayName("audit 6 — MULTI_ROLE primary requires a non-blank description of ≥20 characters")
    void multiRolePrimaryRequiresDescription() {
        // Vacuous today — no catalog seed uses MULTI_ROLE. Becomes meaningful for future seed
        // additions: the test forces the seed author to write a rationale in the description.
        enforce("MULTI_ROLE primary must carry a description of ≥20 chars explaining the rationale",
                s -> s.primaryFunction() == StationFunction.MULTI_ROLE
                        && (s.description() == null || s.description().strip().length() < 20),
                s -> "description=\""
                        + (s.description() == null ? "" : s.description().strip()) + "\"");
    }

    // ==================================================================
    // Pinned-fact assertions for specific Catalog entries
    // ==================================================================

    @Test
    @DisplayName("audit 7 — TROY.provenance.sourceUniverse == \"Troy Rising\"")
    void troyProvenanceSourceUniverse() {
        assertEquals("Troy Rising", ((Megastructure) Catalog.TROY).provenance().sourceUniverse());
    }

    @Test
    @DisplayName("audit 8 — TROY.provenance.sourceWork == \"Troy Rising\" (not null — the work-name pin)")
    void troyProvenanceSourceWork() {
        Megastructure troy = (Megastructure) Catalog.TROY;
        assertNotNull(troy.provenance().sourceWork(),
                "TROY must carry an explicit sourceWork value; null would mean Step 6 didn't apply");
        assertEquals("Troy Rising", troy.provenance().sourceWork());
    }

    @Test
    @DisplayName("audit 9 — TROY.provenance.sourceType == SCIENCE_FICTION")
    void troyProvenanceSourceType() {
        assertEquals(SourceType.SCIENCE_FICTION,
                ((Megastructure) Catalog.TROY).provenance().sourceType());
    }

    @Test
    @DisplayName("audit 10 — TROY.primaryFunction == DEFENSIVE")
    void troyPrimaryFunction() {
        assertEquals(StationFunction.DEFENSIVE, ((Megastructure) Catalog.TROY).primaryFunction());
    }

    @Test
    @DisplayName("audit 11 — TROY.secondaryFunctions contains MILITARY_COMMAND and SHIPBUILDING (D.7 §7)")
    void troySecondaryFunctionsContainsMilitaryCommandAndShipbuilding() {
        Megastructure troy = (Megastructure) Catalog.TROY;
        assertTrue(troy.secondaryFunctions().containsAll(
                        Set.of(StationFunction.MILITARY_COMMAND, StationFunction.SHIPBUILDING)),
                "Troy must carry MILITARY_COMMAND and SHIPBUILDING as secondary functions "
                        + "per v2 Phase D.7 §7 worked example; actual: " + troy.secondaryFunctions());
    }

    @Test
    @DisplayName("audit 12 — ISS.provenance.status == ACTIVE")
    void issProvenanceStatusActive() {
        assertEquals(CatalogOperationalStatus.ACTIVE,
                ((StationDesign) Catalog.ISS).provenance().status());
    }

    @Test
    @DisplayName("audit 13 — MIR.provenance.status == HISTORIC")
    void mirProvenanceStatusHistoric() {
        assertEquals(CatalogOperationalStatus.HISTORIC,
                ((StationDesign) Catalog.MIR).provenance().status());
    }

    @Test
    @DisplayName("audit 14 — LUNAR_GATEWAY.provenance.status == PLANNED")
    void lunarGatewayProvenanceStatusPlanned() {
        assertEquals(CatalogOperationalStatus.PLANNED,
                ((StationDesign) Catalog.LUNAR_GATEWAY).provenance().status());
    }

    // ==================================================================
    // Anti-pattern guard — accidental-default substrings
    // ==================================================================

    @Test
    @DisplayName("audit 15 — no catalog station's sourceUniverse contains forbidden default substrings")
    void noForbiddenDefaultInSourceUniverse() {
        // Guards against accidental defaults / placeholders / WIP markers slipping into seed data.
        // The list is case-insensitive — sourceUniverse strings like "Test", "TODO", "FIXME" or a
        // historical defaulted "Dynamis" all violate the catalog's documentary contract.
        List<String> forbidden = List.of("dynamis", "test", "todo", "fixme", "placeholder", "xxx");
        List<String> offenders = stations().stream()
                .filter(s -> {
                    String u = s.provenance().sourceUniverse();
                    if (u == null) {
                        return false;
                    }
                    String lower = u.toLowerCase(Locale.ROOT);
                    return forbidden.stream().anyMatch(lower::contains);
                })
                .map(s -> s.name() + " (sourceUniverse=\"" + s.provenance().sourceUniverse() + "\")")
                .collect(Collectors.toList());
        if (!offenders.isEmpty()) {
            fail("provenance.sourceUniverse contains forbidden default substring(s) in:\n  - "
                    + String.join("\n  - ", offenders));
        }
    }

    // ==================================================================
    // v2 Phase D.7 Step 8 — Megastructure-side global invariants
    // ==================================================================

    private static List<Megastructure> megastructures() {
        return Catalog.all().stream()
                .filter(Megastructure.class::isInstance)
                .map(Megastructure.class::cast)
                .toList();
    }

    /**
     * Megastructure-flavoured {@link #enforce} — same multi-offender pattern, scoped to the
     * megastructure bucket. Failure messages name each offender + context.
     */
    private static void enforceMega(String invariantName,
                                    java.util.function.Predicate<Megastructure> violates,
                                    java.util.function.Function<Megastructure, String> contextFor) {
        List<String> offenders = new ArrayList<>();
        for (Megastructure m : megastructures()) {
            if (violates.test(m)) {
                offenders.add(m.name() + " — " + contextFor.apply(m));
            }
        }
        if (!offenders.isEmpty()) {
            fail(invariantName + " violated by " + offenders.size() + " megastructure(s):\n  - "
                    + String.join("\n  - ", offenders));
        }
    }

    @Test
    @DisplayName("audit 16 — every Megastructure has primaryFunction != UNKNOWN")
    void everyMegastructureHasNonUnknownPrimaryFunction() {
        enforceMega("Megastructure primaryFunction must not be UNKNOWN",
                m -> m.primaryFunction() == StationFunction.UNKNOWN,
                m -> "primaryFunction=" + m.primaryFunction());
    }

    @Test
    @DisplayName("audit 17 — no Megastructure has secondaryFunctions containing its primaryFunction")
    void noMegaSecondarySetContainsPrimary() {
        enforceMega("Megastructure secondaryFunctions must not contain primaryFunction",
                m -> m.secondaryFunctions().contains(m.primaryFunction()),
                m -> "primary=" + m.primaryFunction() + ", secondaries=" + m.secondaryFunctions());
    }

    @Test
    @DisplayName("audit 18 — every Megastructure has a non-blank provenance.sourceUniverse")
    void everyMegaHasNonBlankSourceUniverse() {
        enforceMega("Megastructure provenance.sourceUniverse must be non-blank",
                m -> m.provenance().sourceUniverse() == null
                        || m.provenance().sourceUniverse().isBlank(),
                m -> "sourceUniverse=\"" + m.provenance().sourceUniverse() + "\"");
    }

    @Test
    @DisplayName("audit 19 — REAL-sourced Megastructures have a real-line status (HISTORIC / ACTIVE / PLANNED / CANCELLED)")
    void megaRealProvenanceHasRealLineStatus() {
        Set<CatalogOperationalStatus> realLine = Set.of(
                CatalogOperationalStatus.HISTORIC,
                CatalogOperationalStatus.ACTIVE,
                CatalogOperationalStatus.PLANNED,
                CatalogOperationalStatus.CANCELLED);
        enforceMega("REAL-sourced Megastructure must carry a real-line status",
                m -> m.provenance().sourceType() == SourceType.REAL
                        && !realLine.contains(m.provenance().status()),
                m -> "sourceType=REAL, status=" + m.provenance().status());
    }

    @Test
    @DisplayName("audit 20 — SCIENCE_FICTION-sourced Megastructures have status == FICTIONAL")
    void megaScienceFictionProvenanceHasFictionalStatus() {
        enforceMega("SCIENCE_FICTION-sourced Megastructure must carry FICTIONAL status",
                m -> m.provenance().sourceType() == SourceType.SCIENCE_FICTION
                        && m.provenance().status() != CatalogOperationalStatus.FICTIONAL,
                m -> "sourceType=SCIENCE_FICTION, status=" + m.provenance().status());
    }

    @Test
    @DisplayName("audit 21 — MULTI_ROLE primary on a Megastructure requires a description of ≥20 chars")
    void megaMultiRolePrimaryRequiresDescription() {
        enforceMega("MULTI_ROLE primary on a Megastructure must carry a description of ≥20 chars",
                m -> m.primaryFunction() == StationFunction.MULTI_ROLE
                        && (m.description() == null || m.description().strip().length() < 20),
                m -> "description=\""
                        + (m.description() == null ? "" : m.description().strip()) + "\"");
    }

    @Test
    @DisplayName("audit 22 — no Megastructure's sourceUniverse contains forbidden default substrings")
    void noForbiddenDefaultInMegaSourceUniverse() {
        List<String> forbidden = List.of("dynamis", "test", "todo", "fixme", "placeholder", "xxx");
        List<String> offenders = megastructures().stream()
                .filter(m -> {
                    String u = m.provenance().sourceUniverse();
                    if (u == null) {
                        return false;
                    }
                    String lower = u.toLowerCase(Locale.ROOT);
                    return forbidden.stream().anyMatch(lower::contains);
                })
                .map(m -> m.name() + " (sourceUniverse=\"" + m.provenance().sourceUniverse() + "\")")
                .collect(Collectors.toList());
        if (!offenders.isEmpty()) {
            fail("Megastructure provenance.sourceUniverse contains forbidden default substring(s) in:\n  - "
                    + String.join("\n  - ", offenders));
        }
    }

    @Test
    @DisplayName("audit 23 — every Megastructure has archetype != UNKNOWN (UNKNOWN reserved for genuine mystery)")
    void everyMegaHasKnownArchetype() {
        enforceMega("Megastructure archetype must not be UNKNOWN (it is the primary categorization key)",
                m -> m.archetype() == MegastructureArchetype.UNKNOWN,
                m -> "archetype=UNKNOWN");
    }

    @Test
    @DisplayName("audit 24 — BUILT_BY_KNOWN-origin Megastructures have a non-blank builderPolity")
    void megaBuiltByKnownHasBuilderPolity() {
        enforceMega("BUILT_BY_KNOWN Megastructure must carry a non-blank builderPolity",
                m -> m.originType() == MegastructureOriginType.BUILT_BY_KNOWN
                        && (m.builderPolity() == null || m.builderPolity().isBlank()),
                m -> "originType=BUILT_BY_KNOWN, builderPolity=\""
                        + (m.builderPolity() == null ? "null" : m.builderPolity()) + "\"");
    }

    @Test
    @DisplayName("audit 25 — FOUND_INTACT / FOUND_DAMAGED Megastructures have a non-null discoveryYear")
    void megaFoundHasDiscoveryYear() {
        Set<MegastructureOriginType> foundFamily = Set.of(
                MegastructureOriginType.FOUND_INTACT,
                MegastructureOriginType.FOUND_DAMAGED);
        enforceMega("FOUND_* Megastructure must carry a non-null discoveryYear",
                m -> foundFamily.contains(m.originType()) && m.discoveryYear() == null,
                m -> "originType=" + m.originType() + ", discoveryYear=null");
    }

    // ==================================================================
    // v2 Phase D.7 Step 8 — Troy pinned-fact assertions (Megastructure-specific)
    // ==================================================================

    @Test
    @DisplayName("audit 26 — TROY.archetype == CONVERTED_ASTEROID")
    void troyArchetypeIsConvertedAsteroid() {
        assertEquals(MegastructureArchetype.CONVERTED_ASTEROID,
                ((Megastructure) Catalog.TROY).archetype());
    }

    @Test
    @DisplayName("audit 27 — TROY.originType == BUILT_BY_KNOWN")
    void troyOriginTypeIsBuiltByKnown() {
        assertEquals(MegastructureOriginType.BUILT_BY_KNOWN,
                ((Megastructure) Catalog.TROY).originType());
    }

    @Test
    @DisplayName("audit 28 — TROY.builderPolity is non-null and non-blank")
    void troyBuilderPolityIsPopulated() {
        Megastructure troy = (Megastructure) Catalog.TROY;
        assertNotNull(troy.builderPolity(), "Troy is BUILT_BY_KNOWN so builderPolity must be set");
        assertTrue(!troy.builderPolity().isBlank(),
                "Troy builderPolity must be non-blank; was: \"" + troy.builderPolity() + "\"");
    }

    @Test
    @DisplayName("audit 29 — TROY.hasInteriorSetting == true")
    void troyHasInteriorSetting() {
        assertTrue(((Megastructure) Catalog.TROY).hasInteriorSetting(),
                "Troy is a self-contained setting (thousands of crew, internal fabrication); "
                        + "hasInteriorSetting must be true");
    }

    @Test
    @DisplayName("audit 30 — TROY.dimensionsKm > 0")
    void troyDimensionsKmPositive() {
        assertTrue(((Megastructure) Catalog.TROY).dimensionsKm() > 0,
                "Troy dimensionsKm must be positive; was: "
                        + ((Megastructure) Catalog.TROY).dimensionsKm());
    }

    @Test
    @DisplayName("audit 31 — TROY.mobility == MOBILE_LIMITED (D.7 Divergence D)")
    void troyMobilityIsMobileLimited() {
        assertEquals(Mobility.MOBILE_LIMITED, ((Megastructure) Catalog.TROY).mobility(),
                "Troy moves under ORION pulses but stationkeeps most of the time; "
                        + "D.7 Divergence D resolved this to MOBILE_LIMITED");
    }

    @Test
    @DisplayName("audit 32 — TROY.auxiliaryDrive == DriveType.ORION (D.7 Divergence D — preserves the canon)")
    void troyAuxiliaryDriveIsOrion() {
        assertEquals(com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType.ORION,
                ((Megastructure) Catalog.TROY).auxiliaryDrive(),
                "Troy's ORION drive characterization from the prior StationDesign is preserved "
                        + "in the Megastructure migration per D.7 Divergence D");
    }

    @Test
    @DisplayName("audit 33 — TROY.interiorGravity == NATURAL_MASS (hollowed nickel-iron asteroid retains gravity)")
    void troyInteriorGravityIsNaturalMass() {
        assertEquals(InteriorGravityType.NATURAL_MASS,
                ((Megastructure) Catalog.TROY).interiorGravity());
    }
}
