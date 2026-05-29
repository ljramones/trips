package com.teamgannon.trips.spaceshipmodeller.service;

import com.teamgannon.trips.spaceshipmodeller.persistence.StationDesignMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.StationEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.StationRepository;
import com.terranrepublic.assets.Armament;
import com.terranrepublic.assets.Catalog;
import com.terranrepublic.assets.CatalogOperationalStatus;
import com.terranrepublic.assets.CatalogProvenance;
import com.terranrepublic.assets.Mobility;
import com.terranrepublic.assets.OperationalState;
import com.terranrepublic.assets.SourceType;
import com.terranrepublic.assets.SpaceAsset;
import com.terranrepublic.assets.StationDesign;
import com.terranrepublic.assets.StationFunction;
import com.terranrepublic.assets.StationType;
import com.terranrepublic.assets.TechLevel;
import com.terranrepublic.assets.WeaponType;
import com.teamgannon.trips.spaceshipmodeller.propulsion.DriveType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
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
 * Unit tests for {@link StationDesignerService} using a mocked repository and the real mapper.
 *
 * <p>Mirrors {@code SpaceshipServiceTest}'s pattern. Integration-level concerns (Flyway, schema
 * validation) are covered by {@code FlywayBaselineSmokeTest}.
 */
@ExtendWith(MockitoExtension.class)
class StationDesignerServiceTest {

    @Mock
    private StationRepository repository;

    private final StationDesignMapper mapper = new StationDesignMapper();
    private StationDesignerService service;

    @BeforeEach
    void setUp() {
        service = new StationDesignerService(repository, mapper);
    }

    /**
     * v2 Phase D.7 Step 6 — Troy migrated to {@link com.terranrepublic.assets.Megastructure}, so
     * the {@code troy()} fixture is now a hand-built StationDesign carrying the pre-migration
     * Troy-shaped values. The tests need a non-trivial StationDesign fixture; the {@code "Troy"}
     * name is preserved so the existsByName / "Phantom" assertions read sensibly.
     * <p>
     * Cached in a static field so {@code troy()} returns the SAME instance across calls — the
     * mock-stubbing tests (e.g. {@code deleteRemovesWhenPresent}) require id stability across
     * the {@code when(...).thenReturn(...)} setup and the {@code verify(...)} call.
     */
    private static final StationDesign TROY_FIXTURE = buildTroyFixture();

    private StationDesign troy() {
        return TROY_FIXTURE;
    }

    private static StationDesign buildTroyFixture() {
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        return new StationDesign(
                UUID.randomUUID().toString(),
                "Troy",
                "TR-T",
                StationType.GATE_FORT,
                "Test Faction",
                false,
                "Test Faction",
                "Synthetic Troy-shaped test fixture (Phase D.7 Step 6).",
                9_000,
                7_000,
                2.0e12,
                2_000,
                150_000,
                120_000,
                1.5e11,
                Mobility.MANEUVERABLE,
                DriveType.ORION,
                java.util.List.of(),
                java.util.List.of(new Armament("SAPL primary", WeaponType.SOLAR_PUMPED_LASER,
                        1, 1.0e6, 1.0e7, "main", null)),
                5.0e7,
                true,
                TechLevel.ADVANCED,
                "gate fortification",
                OperationalState.OPERATIONAL,
                now,
                now,
                StationFunction.DEFENSIVE,
                Set.of(StationFunction.MILITARY_COMMAND),
                new CatalogProvenance(SourceType.SCIENCE_FICTION, "Troy Rising", null,
                        CatalogOperationalStatus.FICTIONAL));
    }

    @Test
    @DisplayName("findAll maps entities back to domain objects, entities never escape")
    void findAllMapsEntities() {
        when(repository.findAll()).thenReturn(List.of(mapper.toEntity(troy())));
        List<StationDesign> all = service.findAll();
        assertEquals(1, all.size());
        assertEquals("Troy", all.get(0).name());
    }

    @Test
    @DisplayName("save persists via the repository and returns the round-tripped domain object")
    void savePersists() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        StationDesign saved = service.save(troy());
        assertEquals("Troy", saved.name());
        verify(repository).save(any(StationEntity.class));
    }

    @Test
    @DisplayName("deleteById removes the entity when present")
    void deleteRemovesWhenPresent() {
        StationEntity entity = mapper.toEntity(troy());
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

    @Test
    @DisplayName("syncCatalogEntries inserts every Catalog station when the table is empty")
    void syncFromEmptyInsertsAllCatalogEntries() {
        // Every existsById returns false → every catalog station is inserted.
        when(repository.existsById(any())).thenReturn(false);
        int inserted = service.syncCatalogEntries();

        long stationCount = Catalog.all().stream()
                .filter(StationDesign.class::isInstance)
                .count();
        assertEquals(stationCount, inserted,
                "sync from empty must insert every Catalog station entry");
        verify(repository, times((int) stationCount)).save(any(StationEntity.class));
    }

    @Test
    @DisplayName("syncCatalogEntries inserts zero rows when every Catalog id already present (idempotent)")
    void syncIsIdempotentWhenAllCatalogEntriesPresent() {
        // Every existsById returns true → no inserts.
        when(repository.existsById(any())).thenReturn(true);
        int inserted = service.syncCatalogEntries();
        assertEquals(0, inserted, "idempotent re-runs must insert zero rows");
        verify(repository, never()).save(any(StationEntity.class));
    }

    @Test
    @DisplayName("syncCatalogEntries inserts only the missing entries (partial-fill scenario)")
    void syncInsertsOnlyMissingEntries() {
        // Simulate: ISS already present, every other catalog station absent.
        when(repository.existsById("catalog-iss")).thenReturn(true);
        when(repository.existsById(org.mockito.ArgumentMatchers.argThat(
                id -> id != null && !id.equals("catalog-iss")))).thenReturn(false);

        int inserted = service.syncCatalogEntries();

        long catalogCount = Catalog.all().stream()
                .filter(StationDesign.class::isInstance)
                .count();
        assertEquals(catalogCount - 1, inserted,
                "sync must skip the one pre-existing entry and insert the rest");
        verify(repository, times((int) (catalogCount - 1))).save(any(StationEntity.class));
    }

    @Test
    @DisplayName("findAllAsAssets exposes results as SpaceAsset for the construct registry")
    void findAllAsAssetsReturnsAssetView() {
        when(repository.findAll()).thenReturn(List.of(mapper.toEntity(troy())));
        List<SpaceAsset> assets = service.findAllAsAssets();
        assertEquals(1, assets.size());
        assertTrue(assets.get(0) instanceof StationDesign);
    }

    @Test
    @DisplayName("existsByName delegates to the repository")
    void existsByNameDelegates() {
        when(repository.existsByNameIgnoreCase("Troy")).thenReturn(true);
        when(repository.existsByNameIgnoreCase("Phantom")).thenReturn(false);
        assertTrue(service.existsByName("Troy"));
        assertFalse(service.existsByName("Phantom"));
    }
}
