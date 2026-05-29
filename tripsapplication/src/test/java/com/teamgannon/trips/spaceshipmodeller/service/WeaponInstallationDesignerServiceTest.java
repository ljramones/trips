package com.teamgannon.trips.spaceshipmodeller.service;

import com.teamgannon.trips.spaceshipmodeller.persistence.WeaponInstallationEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.WeaponInstallationMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.WeaponInstallationRepository;
import com.terranrepublic.assets.Catalog;
import com.terranrepublic.assets.SpaceAsset;
import com.terranrepublic.assets.WeaponInstallation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeaponInstallationDesignerServiceTest {

    @Mock
    private WeaponInstallationRepository repository;

    private final WeaponInstallationMapper mapper = new WeaponInstallationMapper();
    private WeaponInstallationDesignerService service;

    @BeforeEach
    void setUp() {
        service = new WeaponInstallationDesignerService(repository, mapper);
    }

    private WeaponInstallation sapl() {
        return (WeaponInstallation) Catalog.SAPL;
    }

    @Test
    @DisplayName("findAll maps entities to domain objects")
    void findAllMapsEntities() {
        when(repository.findAll()).thenReturn(List.of(mapper.toEntity(sapl())));
        List<WeaponInstallation> all = service.findAll();
        assertEquals(1, all.size());
        assertEquals("SAPL", all.get(0).name());
    }

    @Test
    @DisplayName("save persists via the repository and returns the round-tripped domain object")
    void savePersists() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        WeaponInstallation saved = service.save(sapl());
        assertEquals("SAPL", saved.name());
        verify(repository).save(any(WeaponInstallationEntity.class));
    }

    @Test
    @DisplayName("deleteById removes the entity when present")
    void deleteRemovesWhenPresent() {
        WeaponInstallationEntity entity = mapper.toEntity(sapl());
        when(repository.findById(sapl().id())).thenReturn(Optional.of(entity));
        service.deleteById(sapl().id());
        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("deleteById on a missing id is a no-op")
    void deleteMissingIsNoop() {
        when(repository.findById("absent")).thenReturn(Optional.empty());
        service.deleteById("absent");
        verify(repository, never()).delete(any());
    }

    @Test
    @DisplayName("syncCatalogEntries inserts every Catalog weapon installation when the table is empty")
    void syncFromEmptyInsertsAllCatalogEntries() {
        when(repository.existsById(any())).thenReturn(false);
        int inserted = service.syncCatalogEntries();

        long count = Catalog.all().stream().filter(WeaponInstallation.class::isInstance).count();
        assertEquals(count, inserted, "sync must insert every Catalog weapon-installation entry");
        verify(repository, times((int) count)).save(any(WeaponInstallationEntity.class));
    }

    @Test
    @DisplayName("syncCatalogEntries inserts zero rows when every Catalog id already present (idempotent)")
    void syncIsIdempotentWhenAllCatalogEntriesPresent() {
        when(repository.existsById(any())).thenReturn(true);
        int inserted = service.syncCatalogEntries();
        assertEquals(0, inserted, "idempotent re-runs must insert zero rows");
        verify(repository, never()).save(any(WeaponInstallationEntity.class));
    }

    @Test
    @DisplayName("findAllAsAssets exposes results as SpaceAsset for the construct registry")
    void findAllAsAssetsReturnsAssetView() {
        when(repository.findAll()).thenReturn(List.of(mapper.toEntity(sapl())));
        List<SpaceAsset> assets = service.findAllAsAssets();
        assertEquals(1, assets.size());
        assertTrue(assets.get(0) instanceof WeaponInstallation);
    }

    @Test
    @DisplayName("existsByName delegates to the repository")
    void existsByNameDelegates() {
        when(repository.existsByNameIgnoreCase("SAPL")).thenReturn(true);
        when(repository.existsByNameIgnoreCase("Phantom")).thenReturn(false);
        assertTrue(service.existsByName("SAPL"));
        assertFalse(service.existsByName("Phantom"));
    }
}
