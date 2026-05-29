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
        assertEquals("Troy Rising", ((StationDesign) Catalog.TROY).provenance().sourceUniverse());
    }

    @Test
    @DisplayName("audit 8 — TROY.provenance.sourceWork == \"Troy Rising\" (not null — the work-name pin)")
    void troyProvenanceSourceWork() {
        StationDesign troy = (StationDesign) Catalog.TROY;
        assertNotNull(troy.provenance().sourceWork(),
                "TROY must carry an explicit sourceWork value; null would mean Step 6 didn't apply");
        assertEquals("Troy Rising", troy.provenance().sourceWork());
    }

    @Test
    @DisplayName("audit 9 — TROY.provenance.sourceType == SCIENCE_FICTION")
    void troyProvenanceSourceType() {
        assertEquals(SourceType.SCIENCE_FICTION,
                ((StationDesign) Catalog.TROY).provenance().sourceType());
    }

    @Test
    @DisplayName("audit 10 — TROY.primaryFunction == DEFENSIVE")
    void troyPrimaryFunction() {
        assertEquals(StationFunction.DEFENSIVE, ((StationDesign) Catalog.TROY).primaryFunction());
    }

    @Test
    @DisplayName("audit 11 — TROY.secondaryFunctions contains MILITARY_COMMAND")
    void troySecondaryFunctionsContainsMilitaryCommand() {
        assertTrue(((StationDesign) Catalog.TROY).secondaryFunctions()
                        .contains(StationFunction.MILITARY_COMMAND),
                "Troy must carry MILITARY_COMMAND as a secondary function per v2 §5 worked examples");
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
}
