package com.teamgannon.trips.construct;

import com.teamgannon.trips.spaceshipmodeller.persistence.SpaceshipDesignMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.SpaceshipRepository;
import com.teamgannon.trips.spaceshipmodeller.service.MegastructureDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.StationDesignerService;
import com.teamgannon.trips.spaceshipmodeller.service.TransportNodeService;
import com.teamgannon.trips.spaceshipmodeller.service.WeaponInstallationDesignerService;
import com.terranrepublic.assets.AssetKind;
import com.terranrepublic.assets.Cataloged;
import com.terranrepublic.assets.SpaceAsset;
import com.terranrepublic.infrastructure.InfrastructureKind;
import com.terranrepublic.infrastructure.SpaceInfrastructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Phase B implementation of {@link ConstructRegistry}: ships, stations, weapon installations
 * (the SpaceAsset hierarchy) and transport nodes (the first SpaceInfrastructure subtype).
 * {@link InfrastructureKind#CONDUIT} stays empty: v2 §6.1 explicitly defers Conduit persistence.
 *
 * <p>Each bucket reads through its owning service: {@link SpaceshipRepository} +
 * {@link SpaceshipDesignMapper} for ships, {@link StationDesignerService} for stations,
 * {@link WeaponInstallationDesignerService} for weapon installations, {@link TransportNodeService}
 * for transport nodes. The registry stays read-only and transactional so mappers can detach
 * results before they cross to the JavaFX thread.
 */
@Component
public class DefaultConstructRegistry implements ConstructRegistry {

    private final SpaceshipRepository spaceshipRepository;
    private final SpaceshipDesignMapper spaceshipDesignMapper;
    private final StationDesignerService stationDesignerService;
    private final WeaponInstallationDesignerService weaponInstallationDesignerService;
    private final TransportNodeService transportNodeService;
    private final MegastructureDesignerService megastructureDesignerService;

    @Autowired
    public DefaultConstructRegistry(SpaceshipRepository spaceshipRepository,
                                    SpaceshipDesignMapper spaceshipDesignMapper,
                                    StationDesignerService stationDesignerService,
                                    WeaponInstallationDesignerService weaponInstallationDesignerService,
                                    TransportNodeService transportNodeService,
                                    MegastructureDesignerService megastructureDesignerService) {
        this.spaceshipRepository = spaceshipRepository;
        this.spaceshipDesignMapper = spaceshipDesignMapper;
        this.stationDesignerService = stationDesignerService;
        this.weaponInstallationDesignerService = weaponInstallationDesignerService;
        this.transportNodeService = transportNodeService;
        this.megastructureDesignerService = megastructureDesignerService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cataloged> allById() {
        List<Cataloged> all = new ArrayList<>();
        all.addAll(loadShips());
        all.addAll(loadStations());
        all.addAll(loadWeaponInstallations());
        all.addAll(loadMegastructures());
        all.addAll(loadTransportNodes());
        return List.copyOf(all);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpaceAsset> assetsByKind(AssetKind kind) {
        Objects.requireNonNull(kind, "kind");
        return switch (kind) {
            case SHIP -> List.copyOf(loadShips());
            case STATION -> List.copyOf(loadStations());
            case WEAPON_INSTALLATION -> List.copyOf(loadWeaponInstallations());
            case MEGASTRUCTURE -> List.copyOf(loadMegastructures());
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpaceInfrastructure> infrastructureByKind(InfrastructureKind kind) {
        Objects.requireNonNull(kind, "kind");
        return switch (kind) {
            case TRANSPORT_NODE -> List.copyOf(loadTransportNodes());
            // v2 §6.1 explicitly defers Conduit persistence; the bucket stays empty.
            case CONDUIT -> List.of();
        };
    }

    private List<SpaceAsset> loadShips() {
        return spaceshipRepository.findAll()
                .stream()
                .map(spaceshipDesignMapper::toDomain)
                .map(d -> (SpaceAsset) d)
                .toList();
    }

    private List<SpaceAsset> loadStations() {
        return stationDesignerService.findAllAsAssets();
    }

    private List<SpaceAsset> loadWeaponInstallations() {
        return weaponInstallationDesignerService.findAllAsAssets();
    }

    /**
     * v2 Phase D.8 Step 6 — Megastructure bucket now reads through {@link MegastructureDesignerService}'s
     * JPA-backed {@code findAllAsAssets()}, matching the SHIP / STATION / WEAPON_INSTALLATION
     * pattern. The earlier D.7 Step 7 implementation read directly from {@code Catalog.all()}
     * because the service + repository + seeder triple did not yet exist; D.8 Step 3 added them
     * and D.8 Step 6 swaps this read in lockstep.
     */
    private List<SpaceAsset> loadMegastructures() {
        return megastructureDesignerService.findAllAsAssets();
    }

    private List<SpaceInfrastructure> loadTransportNodes() {
        return transportNodeService.findAllAsInfrastructure();
    }
}
