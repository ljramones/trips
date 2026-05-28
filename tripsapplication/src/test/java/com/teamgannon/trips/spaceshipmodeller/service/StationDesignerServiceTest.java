package com.teamgannon.trips.spaceshipmodeller.service;

import com.teamgannon.trips.spaceshipmodeller.persistence.StationDesignMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.StationEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.StationRepository;
import com.terranrepublic.assets.Catalog;
import com.terranrepublic.assets.SpaceAsset;
import com.terranrepublic.assets.StationDesign;
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

    private StationDesign troy() {
        return (StationDesign) Catalog.TROY;
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
    @DisplayName("seedFromCatalogIfEmpty seeds Catalog station entries when the table is empty")
    void seedWhenEmpty() {
        when(repository.count()).thenReturn(0L);
        int seeded = service.seedFromCatalogIfEmpty();

        long stationCount = Catalog.all().stream()
                .filter(StationDesign.class::isInstance)
                .count();
        assertEquals(stationCount, seeded, "seed count must match Catalog.all() station entries");
        verify(repository, times((int) stationCount)).save(any(StationEntity.class));
    }

    @Test
    @DisplayName("seedFromCatalogIfEmpty does not seed when the table is non-empty (idempotent)")
    void seedWhenNonEmptyIsNoop() {
        when(repository.count()).thenReturn(1L);
        int seeded = service.seedFromCatalogIfEmpty();
        assertEquals(0, seeded);
        verify(repository, never()).save(any(StationEntity.class));
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
