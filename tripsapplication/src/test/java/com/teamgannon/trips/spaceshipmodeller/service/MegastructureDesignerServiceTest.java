package com.teamgannon.trips.spaceshipmodeller.service;

import com.teamgannon.trips.spaceshipmodeller.persistence.MegastructureDesignMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.MegastructureEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.MegastructureRepository;
import com.terranrepublic.assets.Catalog;
import com.terranrepublic.assets.Megastructure;
import com.terranrepublic.assets.SpaceAsset;
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

/**
 * Unit tests for {@link MegastructureDesignerService} using a mocked repository and the real
 * mapper. Mirrors {@link StationDesignerServiceTest}'s pattern. Integration-level concerns
 * (Flyway, schema validation, real sync against an empty DB) are covered by
 * {@code FlywayBaselineSmokeTest} and (Phase D.8 Step 7) {@code CatalogSyncIntegrationTest}.
 */
@ExtendWith(MockitoExtension.class)
class MegastructureDesignerServiceTest {

    @Mock
    private MegastructureRepository repository;

    private final MegastructureDesignMapper mapper = new MegastructureDesignMapper();
    private MegastructureDesignerService service;

    @BeforeEach
    void setUp() {
        service = new MegastructureDesignerService(repository, mapper);
    }

    private static Megastructure troy() {
        return (Megastructure) Catalog.TROY;
    }

    @Test
    @DisplayName("findAll maps entities back to domain objects, entities never escape")
    void findAllMapsEntities() {
        when(repository.findAll()).thenReturn(List.of(mapper.toEntity(troy())));
        List<Megastructure> all = service.findAll();
        assertEquals(1, all.size());
        assertEquals("Troy", all.get(0).name());
    }

    @Test
    @DisplayName("count delegates to the repository")
    void countDelegates() {
        when(repository.count()).thenReturn(7L);
        assertEquals(7L, service.count());
    }

    @Test
    @DisplayName("findById round-trips a persisted megastructure through the mapper")
    void findByIdRoundTrips() {
        when(repository.findById("catalog-troy")).thenReturn(Optional.of(mapper.toEntity(troy())));
        Optional<Megastructure> result = service.findById("catalog-troy");
        assertTrue(result.isPresent());
        assertEquals("Troy", result.get().name());
    }

    @Test
    @DisplayName("findById returns Optional.empty for unknown ids")
    void findByIdReturnsEmptyForUnknown() {
        when(repository.findById("absent")).thenReturn(Optional.empty());
        assertTrue(service.findById("absent").isEmpty());
    }

    @Test
    @DisplayName("existsByName delegates to the repository")
    void existsByNameDelegates() {
        when(repository.existsByNameIgnoreCase("Troy")).thenReturn(true);
        when(repository.existsByNameIgnoreCase("Phantom")).thenReturn(false);
        assertTrue(service.existsByName("Troy"));
        assertFalse(service.existsByName("Phantom"));
    }

    @Test
    @DisplayName("save persists via the repository and returns the round-tripped domain object")
    void savePersists() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Megastructure saved = service.save(troy());
        assertEquals("Troy", saved.name());
        verify(repository).save(any(MegastructureEntity.class));
    }

    @Test
    @DisplayName("deleteById removes the entity when present")
    void deleteRemovesWhenPresent() {
        MegastructureEntity entity = mapper.toEntity(troy());
        when(repository.findById(troy().id())).thenReturn(Optional.of(entity));
        service.deleteById(troy().id());
        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("deleteById on a missing id is a no-op (does not call repository.delete)")
    void deleteMissingIsNoop() {
        when(repository.findById("absent")).thenReturn(Optional.empty());
        service.deleteById("absent");
        verify(repository, never()).delete(any());
    }

    // ------------------------------------------------------------------
    // v2 Phase D.8 §3.1 — syncCatalogEntries contract
    // ------------------------------------------------------------------

    @Test
    @DisplayName("syncCatalogEntries inserts Troy when megastructure table is empty")
    void syncSeedsTroyWhenEmpty() {
        // Empty table → existsById is false for catalog-troy → save fires.
        when(repository.existsById("catalog-troy")).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        int inserted = service.syncCatalogEntries();

        assertEquals(1, inserted,
                "Catalog ships exactly one Megastructure (Troy) per v2 Phase D.7 Step 6");
        verify(repository, times(1)).save(any(MegastructureEntity.class));
    }

    @Test
    @DisplayName("syncCatalogEntries inserts zero rows when Troy already present (idempotent)")
    void syncIsIdempotentWhenAllCatalogEntriesPresent() {
        // Pre-seeded table → existsById is true for every catalog id → no saves.
        when(repository.existsById("catalog-troy")).thenReturn(true);

        int inserted = service.syncCatalogEntries();

        assertEquals(0, inserted, "idempotent re-runs must insert zero rows");
        verify(repository, never()).save(any(MegastructureEntity.class));
    }

    @Test
    @DisplayName("syncCatalogEntries reports the actual count of inserted rows")
    void syncReturnsActualInsertionCount() {
        // First call: empty table.
        when(repository.existsById("catalog-troy")).thenReturn(false);
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        assertEquals(1, service.syncCatalogEntries());

        // Second call: row now present. Mocking the existsById return as true mimics the
        // real-DB state after the first insert.
        when(repository.existsById("catalog-troy")).thenReturn(true);
        assertEquals(0, service.syncCatalogEntries(),
                "second sync within the same JVM must report zero inserts");
    }

    // ------------------------------------------------------------------
    // findAllAsAssets — registry-facing typing
    // ------------------------------------------------------------------

    @Test
    @DisplayName("findAllAsAssets exposes results as SpaceAsset for the construct registry")
    void findAllAsAssetsReturnsAssetView() {
        when(repository.findAll()).thenReturn(List.of(mapper.toEntity(troy())));
        List<SpaceAsset> assets = service.findAllAsAssets();
        assertEquals(1, assets.size());
        assertTrue(assets.get(0) instanceof Megastructure);
    }
}
