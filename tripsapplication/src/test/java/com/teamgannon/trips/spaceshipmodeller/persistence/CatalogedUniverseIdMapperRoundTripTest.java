package com.teamgannon.trips.spaceshipmodeller.persistence;

import com.teamgannon.trips.spaceshipmodeller.core.MassBudget;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import com.terranrepublic.assets.CatalogOperationalStatus;
import com.terranrepublic.assets.CatalogProvenance;
import com.terranrepublic.assets.Emplacement;
import com.terranrepublic.assets.GateNetwork;
import com.terranrepublic.assets.GateNetworkLifecycle;
import com.terranrepublic.assets.InstallationType;
import com.terranrepublic.assets.InteriorGravityType;
import com.terranrepublic.assets.Megastructure;
import com.terranrepublic.assets.MegastructureArchetype;
import com.terranrepublic.assets.MegastructureOriginType;
import com.terranrepublic.assets.Mobility;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.SourceType;
import com.terranrepublic.assets.SpaceshipDesign;
import com.terranrepublic.assets.StationDesign;
import com.terranrepublic.assets.StationFunction;
import com.terranrepublic.assets.StationType;
import com.terranrepublic.assets.TechLevel;
import com.terranrepublic.assets.WeaponInstallation;
import com.terranrepublic.infrastructure.NodeType;
import com.terranrepublic.infrastructure.TransportNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * v2 Phase F.1 §4.4 — round-trip coverage for {@code universe_id} through the 6 catalog mappers.
 *
 * <p>For each mapper:
 * <ul>
 *   <li>A record with {@code universeId = null} round-trips back to {@code null} (canonical
 *       entries stay canonical through persistence).</li>
 *   <li>A record with {@code universeId = "catalog-universe-X"} round-trips back to the same
 *       value (universe-scoped entries preserve their scope).</li>
 * </ul>
 *
 * <p>Together with {@code CatalogedUniverseIdTest} (in-memory contract), this fixes the
 * persistence-layer side of the universe_id propagation. V16 migration adds the column; V17 will
 * populate the values for the existing fiction-canon entries.
 */
class CatalogedUniverseIdMapperRoundTripTest {

    private static final String LEGACY = "catalog-universe-legacy-of-the-aldenata";

    // ------------------------------------------------------------ SpaceshipDesign

    @ParameterizedTest
    @ValueSource(strings = {LEGACY, "catalog-universe-caine-riordan"})
    @DisplayName("SpaceshipDesignMapper: non-null universeId round-trips")
    void spaceshipMapperNonNullUniverseIdRoundTrips(String universeId) {
        SpaceshipDesignMapper mapper = new SpaceshipDesignMapper();
        SpaceshipDesign src = shipFixture(universeId);
        SpaceshipDesign back = mapper.toDomain(mapper.toEntity(src));
        assertEquals(universeId, back.universeId());
    }

    @Test
    @DisplayName("SpaceshipDesignMapper: null universeId round-trips as null (canonical/real)")
    void spaceshipMapperNullUniverseIdRoundTrips() {
        SpaceshipDesignMapper mapper = new SpaceshipDesignMapper();
        SpaceshipDesign src = shipFixture(null);
        SpaceshipDesign back = mapper.toDomain(mapper.toEntity(src));
        assertNull(back.universeId());
    }

    // ------------------------------------------------------------ StationDesign

    @ParameterizedTest
    @ValueSource(strings = {LEGACY, "catalog-universe-caine-riordan"})
    @DisplayName("StationDesignMapper: non-null universeId round-trips")
    void stationMapperNonNullUniverseIdRoundTrips(String universeId) {
        StationDesignMapper mapper = new StationDesignMapper();
        StationDesign src = stationFixture(universeId);
        StationDesign back = mapper.toDomain(mapper.toEntity(src));
        assertEquals(universeId, back.universeId());
    }

    @Test
    @DisplayName("StationDesignMapper: null universeId round-trips as null")
    void stationMapperNullUniverseIdRoundTrips() {
        StationDesignMapper mapper = new StationDesignMapper();
        StationDesign src = stationFixture(null);
        StationDesign back = mapper.toDomain(mapper.toEntity(src));
        assertNull(back.universeId());
    }

    // ------------------------------------------------------------ WeaponInstallation

    @ParameterizedTest
    @ValueSource(strings = {LEGACY, "catalog-universe-caine-riordan"})
    @DisplayName("WeaponInstallationMapper: non-null universeId round-trips")
    void weaponMapperNonNullUniverseIdRoundTrips(String universeId) {
        WeaponInstallationMapper mapper = new WeaponInstallationMapper();
        WeaponInstallation src = weaponFixture(universeId);
        WeaponInstallation back = mapper.toDomain(mapper.toEntity(src));
        assertEquals(universeId, back.universeId());
    }

    @Test
    @DisplayName("WeaponInstallationMapper: null universeId round-trips as null")
    void weaponMapperNullUniverseIdRoundTrips() {
        WeaponInstallationMapper mapper = new WeaponInstallationMapper();
        WeaponInstallation src = weaponFixture(null);
        WeaponInstallation back = mapper.toDomain(mapper.toEntity(src));
        assertNull(back.universeId());
    }

    // ------------------------------------------------------------ Megastructure

    @ParameterizedTest
    @ValueSource(strings = {LEGACY, "catalog-universe-caine-riordan"})
    @DisplayName("MegastructureDesignMapper: non-null universeId round-trips")
    void megastructureMapperNonNullUniverseIdRoundTrips(String universeId) {
        MegastructureDesignMapper mapper = new MegastructureDesignMapper();
        Megastructure src = megastructureFixture(universeId);
        Megastructure back = mapper.toDomain(mapper.toEntity(src));
        assertEquals(universeId, back.universeId());
    }

    @Test
    @DisplayName("MegastructureDesignMapper: null universeId round-trips as null")
    void megastructureMapperNullUniverseIdRoundTrips() {
        MegastructureDesignMapper mapper = new MegastructureDesignMapper();
        Megastructure src = megastructureFixture(null);
        Megastructure back = mapper.toDomain(mapper.toEntity(src));
        assertNull(back.universeId());
    }

    // ------------------------------------------------------------ GateNetwork

    @ParameterizedTest
    @ValueSource(strings = {LEGACY, "catalog-universe-caine-riordan"})
    @DisplayName("GateNetworkMapper: non-null universeId round-trips")
    void gateNetworkMapperNonNullUniverseIdRoundTrips(String universeId) {
        GateNetworkMapper mapper = new GateNetworkMapper();
        GateNetwork src = gateNetworkFixture(universeId);
        GateNetwork back = mapper.toDomain(mapper.toEntity(src));
        assertEquals(universeId, back.universeId());
    }

    @Test
    @DisplayName("GateNetworkMapper: null universeId round-trips as null")
    void gateNetworkMapperNullUniverseIdRoundTrips() {
        GateNetworkMapper mapper = new GateNetworkMapper();
        GateNetwork src = gateNetworkFixture(null);
        GateNetwork back = mapper.toDomain(mapper.toEntity(src));
        assertNull(back.universeId());
    }

    // ------------------------------------------------------------ TransportNode

    @ParameterizedTest
    @ValueSource(strings = {LEGACY, "catalog-universe-caine-riordan"})
    @DisplayName("TransportNodeMapper: non-null universeId round-trips")
    void transportNodeMapperNonNullUniverseIdRoundTrips(String universeId) {
        TransportNodeMapper mapper = new TransportNodeMapper();
        TransportNode src = transportNodeFixture(universeId);
        TransportNode back = mapper.toDomain(mapper.toEntity(src));
        assertEquals(universeId, back.universeId());
    }

    @Test
    @DisplayName("TransportNodeMapper: null universeId round-trips as null")
    void transportNodeMapperNullUniverseIdRoundTrips() {
        TransportNodeMapper mapper = new TransportNodeMapper();
        TransportNode src = transportNodeFixture(null);
        TransportNode back = mapper.toDomain(mapper.toEntity(src));
        assertNull(back.universeId());
    }

    // ----------------------------------------------- fixtures

    private static final Instant FIXED_TIME = Instant.parse("2026-01-01T00:00:00Z");

    private static SpaceshipDesign shipFixture(String universeId) {
        return new SpaceshipDesign("ship-1", "Test Ship", "TS-1", ShipClass.CRUISER,
                DriveType.CHEMICAL_BIPROPELLANT, new MassBudget(50, 20, 100, 30, 5, 5), 10, 50.0,
                List.of(), List.of(), "", "desc",
                SourceType.SCIENCE_FICTION, "Legacy of the Aldenata", "Posleen", false,
                OperationalState.OPERATIONAL, "During Invasion", FIXED_TIME,
                Set.of(), universeId);
    }

    private static StationDesign stationFixture(String universeId) {
        return new StationDesign("station-1", "Test Station", "TS-1", StationType.OUTPOST,
                "TestFaction", false, "TestAllegiance", "desc",
                100, 50, 1000, 5, 10, 5, 200,
                Mobility.FIXED, null, List.of(), List.of(), 0, false,
                TechLevel.CONTEMPORARY, "category", OperationalState.OPERATIONAL, FIXED_TIME, FIXED_TIME,
                StationFunction.UNKNOWN, Set.of(),
                new CatalogProvenance(SourceType.SCIENCE_FICTION, "Legacy of the Aldenata", null,
                        CatalogOperationalStatus.FICTIONAL),
                universeId);
    }

    private static WeaponInstallation weaponFixture(String universeId) {
        return new WeaponInstallation("weapon-1", "Test Weapon", "TW-1",
                InstallationType.DEFENCE_BATTERY, Emplacement.GROUND_FIXED,
                "Legacy of the Aldenata", "Posleen", false, "desc",
                100, 10, false, 5, List.of(),
                TechLevel.CONTEMPORARY, "category", OperationalState.OPERATIONAL, FIXED_TIME, FIXED_TIME,
                universeId);
    }

    private static Megastructure megastructureFixture(String universeId) {
        return new Megastructure("mega-1", "Test Mega", "TM-1", "desc", "category", "notes",
                MegastructureArchetype.CONVERTED_ASTEROID, 100.0, 1_000_000.0, 1e9,
                Mobility.MOBILE_LIMITED, null,
                MegastructureOriginType.BUILT_BY_KNOWN, "Solar Confederation",
                null, 2050, StationFunction.DEFENSIVE, Set.of(),
                true, 1_000_000L, InteriorGravityType.SPIN,
                OperationalState.OPERATIONAL, false, List.of(),
                CatalogProvenance.unknown(), "Solar Confederation", "Solar Confederation",
                TechLevel.ADVANCED, FIXED_TIME, FIXED_TIME, universeId);
    }

    private static GateNetwork gateNetworkFixture(String universeId) {
        return new GateNetwork("network-1", "Test Network", "TestPolity",
                GateNetworkLifecycle.ACTIVE, "TEST-XPDR", "desc", null, "category",
                CatalogProvenance.unknown(), FIXED_TIME, FIXED_TIME, universeId);
    }

    private static TransportNode transportNodeFixture(String universeId) {
        return new TransportNode("transport-1", "Test Transport", "Real / Proposed",
                "Earth", false, "desc",
                NodeType.RELAY, 1.0, 2.0, 3.0, List.of(), 100.0,
                false, 1.0, FIXED_TIME, FIXED_TIME, universeId);
    }
}
