package com.teamgannon.trips.construct;

import com.teamgannon.trips.spaceshipmodeller.builder.SpaceshipBuilder;
import com.teamgannon.trips.spaceshipmodeller.core.ShipClass;
import com.teamgannon.trips.spaceshipmodeller.persistence.SpaceshipDesignMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.SpaceshipRepository;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;
import com.teamgannon.trips.spaceshipmodeller.service.StationDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.TransportNodeService;
import com.teamgannon.trips.spaceshipmodeller.service.WeaponInstallationDesignerService;
import com.terranrepublic.assets.AssetKind;
import com.terranrepublic.assets.Catalog;
import com.terranrepublic.assets.Cataloged;
import com.terranrepublic.assets.SpaceAsset;
import com.terranrepublic.assets.SpaceshipDesign;
import com.terranrepublic.assets.StationDesign;
import com.terranrepublic.assets.WeaponInstallation;
import com.terranrepublic.infrastructure.InfrastructureKind;
import com.terranrepublic.infrastructure.NodeType;
import com.terranrepublic.infrastructure.SpaceInfrastructure;
import com.terranrepublic.infrastructure.TransportNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * v2 Phase B coverage for {@link DefaultConstructRegistry}.
 *
 * <p>Phase A contract was "ships + stations flow through; everything else empty." Phase B step 1
 * flipped the WEAPON_INSTALLATION bucket; step 2 flips the TRANSPORT_NODE bucket on the
 * SpaceInfrastructure side. CONDUIT remains empty per v2 §6.1 deferral.
 *
 * <p>Every populated bucket asserts an exact-set or exact-count match against the upstream
 * service — not a loose {@code !isEmpty()} check. A loose check would mask a future
 * round-trip-loss bug the same way the spaceship mapper bug was masked before Phase A0.
 */
@ExtendWith(MockitoExtension.class)
class DefaultConstructRegistryTest {

    @Mock
    private SpaceshipRepository spaceshipRepository;

    @Mock
    private StationDesignerService stationDesignerService;

    @Mock
    private WeaponInstallationDesignerService weaponInstallationDesignerService;

    @Mock
    private TransportNodeService transportNodeService;

    private final SpaceshipDesignMapper mapper = new SpaceshipDesignMapper();
    private DefaultConstructRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DefaultConstructRegistry(
                spaceshipRepository, mapper, stationDesignerService,
                weaponInstallationDesignerService, transportNodeService);
    }

    private SpaceshipDesign ship(String name) {
        return SpaceshipBuilder.create(name)
                .shipClass(ShipClass.FRIGATE)
                .driveType(DriveType.FUSION_TORCH)
                .structureTons(200)
                .engineTons(150)
                .propellantTons(300)
                .payloadTons(50)
                .crewTons(20)
                .radiatorTons(120)
                .crew(4)
                .build();
    }

    private List<SpaceAsset> catalogStations() {
        return Catalog.all().stream()
                .filter(StationDesign.class::isInstance)
                .map(a -> (SpaceAsset) a)
                .toList();
    }

    private List<SpaceAsset> catalogWeaponInstallations() {
        return Catalog.all().stream()
                .filter(WeaponInstallation.class::isInstance)
                .map(a -> (SpaceAsset) a)
                .toList();
    }

    private TransportNode ringGate(String name) {
        Instant now = Instant.parse("2025-06-01T09:00:00Z");
        return new TransportNode(
                UUID.randomUUID().toString(),
                name,
                "src",
                "F",
                false,
                "desc",
                NodeType.RING_GATE,
                0, 0, 0,
                List.of(),
                100,
                false,
                10,
                now,
                now);
    }

    @Test
    @DisplayName("allById spans ships + stations + weapon installations + transport nodes")
    void allByIdSpansAllPersistedKinds() {
        SpaceshipDesign alpha = ship("Alpha");
        SpaceshipDesign beta = ship("Beta");
        TransportNode gate = ringGate("Sol Gate");
        when(spaceshipRepository.findAll())
                .thenReturn(List.of(mapper.toEntity(alpha), mapper.toEntity(beta)));
        when(stationDesignerService.findAllAsAssets()).thenReturn(catalogStations());
        when(weaponInstallationDesignerService.findAllAsAssets()).thenReturn(catalogWeaponInstallations());
        when(transportNodeService.findAllAsInfrastructure())
                .thenReturn(List.of((SpaceInfrastructure) gate));

        List<Cataloged> all = registry.allById();

        int expectedSize = 2 + catalogStations().size() + catalogWeaponInstallations().size() + 1;
        assertEquals(expectedSize, all.size(),
                "allById count = ship + station + weapon-installation + transport-node counts");
        assertEquals(2, all.stream().filter(SpaceshipDesign.class::isInstance).count());
        assertEquals(catalogStations().size(),
                all.stream().filter(StationDesign.class::isInstance).count());
        assertEquals(catalogWeaponInstallations().size(),
                all.stream().filter(WeaponInstallation.class::isInstance).count());
        assertEquals(1, all.stream().filter(TransportNode.class::isInstance).count());
    }

    @Test
    @DisplayName("assetsByKind(SHIP) returns ships from the repository")
    void assetsByKindShipReturnsShips() {
        SpaceshipDesign alpha = ship("Alpha");
        when(spaceshipRepository.findAll()).thenReturn(List.of(mapper.toEntity(alpha)));

        List<SpaceAsset> ships = registry.assetsByKind(AssetKind.SHIP);

        assertEquals(1, ships.size());
        assertEquals("Alpha", ships.get(0).name());
        assertEquals(AssetKind.SHIP, ships.get(0).kind());
    }

    @Test
    @DisplayName("assetsByKind(STATION) returns exactly the stations the service exposes (Catalog seed)")
    void assetsByKindStationReturnsCatalogSeed() {
        when(stationDesignerService.findAllAsAssets()).thenReturn(catalogStations());

        List<SpaceAsset> stations = registry.assetsByKind(AssetKind.STATION);

        Set<String> expectedNames = catalogStations().stream()
                .map(SpaceAsset::name)
                .collect(Collectors.toSet());
        Set<String> actualNames = stations.stream().map(SpaceAsset::name).collect(Collectors.toSet());
        assertEquals(expectedNames, actualNames,
                "STATION bucket must contain exactly the stations defined in Catalog.all()");
        assertTrue(stations.stream().allMatch(s -> s.kind() == AssetKind.STATION));
    }

    @Test
    @DisplayName("assetsByKind(WEAPON_INSTALLATION) returns exactly the weapons the service exposes (Catalog seed)")
    void assetsByKindWeaponInstallationReturnsCatalogSeed() {
        when(weaponInstallationDesignerService.findAllAsAssets()).thenReturn(catalogWeaponInstallations());

        List<SpaceAsset> weapons = registry.assetsByKind(AssetKind.WEAPON_INSTALLATION);

        Set<String> expectedNames = catalogWeaponInstallations().stream()
                .map(SpaceAsset::name)
                .collect(Collectors.toSet());
        Set<String> actualNames = weapons.stream().map(SpaceAsset::name).collect(Collectors.toSet());
        assertEquals(expectedNames, actualNames,
                "WEAPON_INSTALLATION bucket must contain exactly the entries defined in Catalog.all()");
        // Reality check: the Catalog ships SAPL + SHEVA_GUN today, so two entries.
        assertEquals(2, weapons.size(), "Catalog currently ships SAPL + SHEVA_GUN");
        assertTrue(weapons.stream().allMatch(w -> w.kind() == AssetKind.WEAPON_INSTALLATION));
    }

    @Test
    @DisplayName("infrastructureByKind(TRANSPORT_NODE) returns what the service exposes")
    void infrastructureByKindTransportNodeReadsService() {
        TransportNode a = ringGate("Alpha Gate");
        TransportNode b = ringGate("Beta Gate");
        when(transportNodeService.findAllAsInfrastructure())
                .thenReturn(List.of((SpaceInfrastructure) a, (SpaceInfrastructure) b));

        List<SpaceInfrastructure> nodes = registry.infrastructureByKind(InfrastructureKind.TRANSPORT_NODE);

        assertEquals(2, nodes.size(),
                "TRANSPORT_NODE bucket reflects exactly the nodes the service returns");
        Set<String> names = nodes.stream().map(SpaceInfrastructure::name).collect(Collectors.toSet());
        assertEquals(Set.of("Alpha Gate", "Beta Gate"), names);
        assertTrue(nodes.stream().allMatch(n -> n.kind() == InfrastructureKind.TRANSPORT_NODE));
    }

    @Test
    @DisplayName("infrastructureByKind(TRANSPORT_NODE) is empty when the table is empty (no Phase B seed)")
    void infrastructureByKindTransportNodeEmptyWhenServiceEmpty() {
        when(transportNodeService.findAllAsInfrastructure()).thenReturn(List.of());
        assertTrue(registry.infrastructureByKind(InfrastructureKind.TRANSPORT_NODE).isEmpty(),
                "v2 §5 Phase B explicitly ships no canonical transport nodes; default state is empty");
    }

    @Test
    @DisplayName("infrastructureByKind(CONDUIT) stays empty (v2 §6.1 deferral)")
    void infrastructureByKindConduitIsAlwaysEmpty() {
        assertTrue(registry.infrastructureByKind(InfrastructureKind.CONDUIT).isEmpty(),
                "Conduit persistence is deferred per v2 §6.1 Q3");
    }
}
