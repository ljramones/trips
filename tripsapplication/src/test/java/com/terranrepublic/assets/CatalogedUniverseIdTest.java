package com.terranrepublic.assets;

import com.teamgannon.trips.spaceshipmodeller.core.MassBudget;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import com.terranrepublic.infrastructure.NodeType;
import com.terranrepublic.infrastructure.TransportNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * v2 Phase F.1 §4.4 — pins the {@link Cataloged#universeId()} contract:
 * <ul>
 *   <li>The default returns {@code null} (canonical/real-data — visible regardless of which
 *       universes are active).</li>
 *   <li>Each of the 6 persisted Cataloged subtypes
 *       (SpaceshipDesign, StationDesign, WeaponInstallation, Megastructure, GateNetwork,
 *       TransportNode) carries a {@code universeId} record component that satisfies the
 *       interface contract via Java's auto-generated accessor.</li>
 *   <li>{@link Universe} itself inherits the {@code null} default (universes don't have parent
 *       universes; the activation state lives on the {@code active} field, not the universeId).</li>
 * </ul>
 *
 * <p>Mapper round-trip coverage for the universe_id column lives in
 * {@code CatalogedUniverseIdMapperRoundTripTest}; this test exercises only the in-memory
 * record-level contract.
 */
class CatalogedUniverseIdTest {

    private static final String SAMPLE_UNIVERSE_ID = "catalog-universe-legacy-of-the-aldenata";

    // ------------------------------------------------------------ Cataloged default

    @Test
    @DisplayName("Cataloged.universeId() default returns null (canonical/real)")
    void catalogedDefaultReturnsNull() {
        Cataloged minimal = new Cataloged() {
            @Override public String id() { return "x"; }
            @Override public String name() { return "x"; }
            @Override public String source() { return ""; }
            @Override public String faction() { return ""; }
            @Override public boolean concealed() { return false; }
            @Override public String description() { return ""; }
            // universeId() inherits the default — should return null
        };
        assertNull(minimal.universeId(),
                "Cataloged.universeId() default must return null (canonical/real-data marker)");
    }

    @Test
    @DisplayName("Universe inherits the null universeId default (universes don't have parent universes)")
    void universeInheritsNullDefault() {
        Universe u = new Universe(
                "catalog-universe-test", "Test", "", "", "1.0",
                UniverseLifecycle.AVAILABLE, false);
        assertNull(u.universeId(),
                "Universe does not have a parent universe; the universeId() default returns null");
    }

    // ------------------------------------------------------------ SpaceshipDesign

    @Test
    @DisplayName("SpaceshipDesign: null universeId from 20-arg compat constructor")
    void spaceshipDesignCompatConstructorYieldsNullUniverseId() {
        SpaceshipDesign s = sampleShip(null);
        assertNull(s.universeId());
    }

    @Test
    @DisplayName("SpaceshipDesign: non-null universeId preserves the value through the record accessor")
    void spaceshipDesignCarriesNonNullUniverseId() {
        SpaceshipDesign s = sampleShip(SAMPLE_UNIVERSE_ID);
        assertEquals(SAMPLE_UNIVERSE_ID, s.universeId());
        assertInstanceOf(Cataloged.class, s);
    }

    // ------------------------------------------------------------ StationDesign

    @Test
    @DisplayName("StationDesign: null universeId from 29-arg compat constructor")
    void stationDesignCompatConstructorYieldsNullUniverseId() {
        StationDesign s = sampleStation(null);
        assertNull(s.universeId());
    }

    @Test
    @DisplayName("StationDesign: non-null universeId preserves the value through the record accessor")
    void stationDesignCarriesNonNullUniverseId() {
        StationDesign s = sampleStation(SAMPLE_UNIVERSE_ID);
        assertEquals(SAMPLE_UNIVERSE_ID, s.universeId());
        assertInstanceOf(Cataloged.class, s);
    }

    // ------------------------------------------------------------ WeaponInstallation

    @Test
    @DisplayName("WeaponInstallation: null universeId from 19-arg compat constructor")
    void weaponInstallationCompatConstructorYieldsNullUniverseId() {
        WeaponInstallation w = sampleWeapon(null);
        assertNull(w.universeId());
    }

    @Test
    @DisplayName("WeaponInstallation: non-null universeId preserves the value through the record accessor")
    void weaponInstallationCarriesNonNullUniverseId() {
        WeaponInstallation w = sampleWeapon(SAMPLE_UNIVERSE_ID);
        assertEquals(SAMPLE_UNIVERSE_ID, w.universeId());
        assertInstanceOf(Cataloged.class, w);
    }

    // ------------------------------------------------------------ Megastructure

    @Test
    @DisplayName("Megastructure: null universeId from 30-arg compat constructor")
    void megastructureCompatConstructorYieldsNullUniverseId() {
        Megastructure m = sampleMegastructure(null);
        assertNull(m.universeId());
    }

    @Test
    @DisplayName("Megastructure: non-null universeId preserves the value through the record accessor")
    void megastructureCarriesNonNullUniverseId() {
        Megastructure m = sampleMegastructure(SAMPLE_UNIVERSE_ID);
        assertEquals(SAMPLE_UNIVERSE_ID, m.universeId());
        assertInstanceOf(Cataloged.class, m);
    }

    // ------------------------------------------------------------ GateNetwork

    @Test
    @DisplayName("GateNetwork: null universeId from 11-arg compat constructor")
    void gateNetworkCompatConstructorYieldsNullUniverseId() {
        GateNetwork g = sampleGateNetwork(null, true);
        assertNull(g.universeId());
    }

    @Test
    @DisplayName("GateNetwork: non-null universeId preserves the value through the record accessor")
    void gateNetworkCarriesNonNullUniverseId() {
        GateNetwork g = sampleGateNetwork(SAMPLE_UNIVERSE_ID, false);
        assertEquals(SAMPLE_UNIVERSE_ID, g.universeId());
        assertInstanceOf(Cataloged.class, g);
    }

    // ------------------------------------------------------------ TransportNode

    @Test
    @DisplayName("TransportNode: null universeId from 16-arg compat constructor")
    void transportNodeCompatConstructorYieldsNullUniverseId() {
        TransportNode t = sampleTransportNode(null, true);
        assertNull(t.universeId());
    }

    @Test
    @DisplayName("TransportNode: non-null universeId preserves the value through the record accessor")
    void transportNodeCarriesNonNullUniverseId() {
        TransportNode t = sampleTransportNode(SAMPLE_UNIVERSE_ID, false);
        assertEquals(SAMPLE_UNIVERSE_ID, t.universeId());
        assertInstanceOf(Cataloged.class, t);
    }

    // ----------------------------------------------- fixtures

    private static SpaceshipDesign sampleShip(String universeId) {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        if (universeId == null) {
            // Use the 19-arg compat constructor (which delegates to 21-arg with both new
            // fields defaulted: defaultAccessibleNetworkIds=Set.of(), universeId=null)
            return new SpaceshipDesign("ship-1", "Test Ship", "TS-1", ShipClass.CRUISER,
                    DriveType.CHEMICAL_BIPROPELLANT, new MassBudget(50, 20, 100, 30, 5, 5), 10, 50.0,
                    List.of(), List.of(), "", "desc",
                    SourceType.REAL, "Real / Proposed", "NASA", false,
                    OperationalState.OPERATIONAL, "Near future", now);
        }
        // Use the 21-arg canonical with both extension fields set
        return new SpaceshipDesign("ship-1", "Test Ship", "TS-1", ShipClass.CRUISER,
                DriveType.CHEMICAL_BIPROPELLANT, new MassBudget(50, 20, 100, 30, 5, 5), 10, 50.0,
                List.of(), List.of(), "", "desc",
                SourceType.SCIENCE_FICTION, "Legacy of the Aldenata", "Posleen", false,
                OperationalState.OPERATIONAL, "During Invasion", now,
                Set.of(), universeId);
    }

    private static StationDesign sampleStation(String universeId) {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        if (universeId == null) {
            // 29-arg compat constructor — universeId defaults to null
            return new StationDesign("station-1", "Test Station", "TS-1", StationType.OUTPOST,
                    "TestFaction", false, "TestAllegiance", "desc",
                    100, 50, 1000, 5, 10, 5, 200,
                    Mobility.FIXED, null, List.of(), List.of(), 0, false,
                    TechLevel.CONTEMPORARY, "category", OperationalState.OPERATIONAL, now, now,
                    StationFunction.UNKNOWN, Set.of(),
                    new CatalogProvenance(SourceType.REAL, "Earth", null, CatalogOperationalStatus.ACTIVE));
        }
        // 30-arg canonical
        return new StationDesign("station-1", "Test Station", "TS-1", StationType.OUTPOST,
                "TestFaction", false, "TestAllegiance", "desc",
                100, 50, 1000, 5, 10, 5, 200,
                Mobility.FIXED, null, List.of(), List.of(), 0, false,
                TechLevel.CONTEMPORARY, "category", OperationalState.OPERATIONAL, now, now,
                StationFunction.UNKNOWN, Set.of(),
                new CatalogProvenance(SourceType.SCIENCE_FICTION, "Legacy of the Aldenata", null,
                        CatalogOperationalStatus.FICTIONAL),
                universeId);
    }

    private static WeaponInstallation sampleWeapon(String universeId) {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        if (universeId == null) {
            // 19-arg compat
            return new WeaponInstallation("weapon-1", "Test Weapon", "TW-1",
                    InstallationType.DEFENCE_BATTERY, Emplacement.GROUND_FIXED,
                    "Real / Proposed", "USA", false, "desc",
                    100, 10, false, 5, List.of(),
                    TechLevel.CONTEMPORARY, "category", OperationalState.OPERATIONAL, now, now);
        }
        // 20-arg canonical
        return new WeaponInstallation("weapon-1", "Test Weapon", "TW-1",
                InstallationType.DEFENCE_BATTERY, Emplacement.GROUND_FIXED,
                "Legacy of the Aldenata", "Posleen", false, "desc",
                100, 10, false, 5, List.of(),
                TechLevel.CONTEMPORARY, "category", OperationalState.OPERATIONAL, now, now,
                universeId);
    }

    private static Megastructure sampleMegastructure(String universeId) {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        if (universeId == null) {
            // 30-arg compat
            return new Megastructure("mega-1", "Test Mega", "TM-1", "desc", "category", "notes",
                    MegastructureArchetype.CONVERTED_ASTEROID, 100.0, 1_000_000.0, 1e9,
                    Mobility.MOBILE_LIMITED, null,
                    MegastructureOriginType.BUILT_BY_KNOWN, "Solar Confederation",
                    null, 2050, StationFunction.DEFENSIVE, Set.of(),
                    true, 1_000_000L, InteriorGravityType.SPIN,
                    OperationalState.OPERATIONAL, false, List.of(),
                    CatalogProvenance.unknown(), "Solar Confederation", "Solar Confederation",
                    TechLevel.ADVANCED, now, now);
        }
        // 31-arg canonical
        return new Megastructure("mega-1", "Test Mega", "TM-1", "desc", "category", "notes",
                MegastructureArchetype.CONVERTED_ASTEROID, 100.0, 1_000_000.0, 1e9,
                Mobility.MOBILE_LIMITED, null,
                MegastructureOriginType.BUILT_BY_KNOWN, "Solar Confederation",
                null, 2050, StationFunction.DEFENSIVE, Set.of(),
                true, 1_000_000L, InteriorGravityType.SPIN,
                OperationalState.OPERATIONAL, false, List.of(),
                CatalogProvenance.unknown(), "Solar Confederation", "Solar Confederation",
                TechLevel.ADVANCED, now, now, universeId);
    }

    private static GateNetwork sampleGateNetwork(String universeId, boolean useCompat) {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        if (useCompat) {
            // 11-arg compat — universeId defaults to null
            return new GateNetwork("network-1", "Test Network", "TestPolity",
                    GateNetworkLifecycle.ACTIVE, "TEST-XPDR", "desc", null, "category",
                    CatalogProvenance.unknown(), now, now);
        }
        // 12-arg canonical
        return new GateNetwork("network-1", "Test Network", "TestPolity",
                GateNetworkLifecycle.ACTIVE, "TEST-XPDR", "desc", null, "category",
                CatalogProvenance.unknown(), now, now, universeId);
    }

    private static TransportNode sampleTransportNode(String universeId, boolean useCompat) {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        if (useCompat) {
            // 16-arg compat
            return new TransportNode("transport-1", "Test Transport", "Real / Proposed",
                    "Earth", false, "desc",
                    NodeType.RELAY, 1.0, 2.0, 3.0, List.of(), 100.0,
                    false, 1.0, now, now);
        }
        // 17-arg canonical
        return new TransportNode("transport-1", "Test Transport", "Real / Proposed",
                "Earth", false, "desc",
                NodeType.RELAY, 1.0, 2.0, 3.0, List.of(), 100.0,
                false, 1.0, now, now, universeId);
    }
}
