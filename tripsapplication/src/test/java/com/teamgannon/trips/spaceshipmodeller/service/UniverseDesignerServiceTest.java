package com.teamgannon.trips.spaceshipmodeller.service;

import com.teamgannon.trips.spaceshipmodeller.persistence.UniverseEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.UniverseMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.UniverseRepository;
import com.terranrepublic.assets.Cataloged;
import com.terranrepublic.assets.SpaceAsset;
import com.terranrepublic.assets.Universe;
import com.terranrepublic.assets.UniverseLifecycle;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UniverseDesignerService}. Mirrors {@link GateNetworkDesignerServiceTest}'s
 * shape: mocked repository + real mapper, covers find/save/delete + the D.8 sync-by-id contract.
 *
 * <p>v2 Phase F.1 §4.2 — first F-series catalog-pipeline test. {@code findAllAsCataloged} returns
 * {@code List<Cataloged>} (not {@code List<SpaceAsset>}); the universe is outside the sealed
 * hierarchies like {@link com.terranrepublic.assets.GateNetwork}.
 *
 * <p>{@code activate} / {@code deactivate} + event publication are F.1 Step 5 work; this test
 * covers only the find/save/delete/sync surface of Step 2.
 */
@ExtendWith(MockitoExtension.class)
class UniverseDesignerServiceTest {

    @Mock
    private UniverseRepository repository;

    private final UniverseMapper mapper = new UniverseMapper();
    private UniverseDesignerService service;

    @BeforeEach
    void setUp() {
        service = new UniverseDesignerService(repository, mapper);
    }

    private static Universe sample() {
        return new Universe(
                "catalog-universe-test",
                "Test Universe",
                "A test universe.",
                "Test Author",
                "1.0",
                UniverseLifecycle.AVAILABLE,
                false);
    }

    // ----------------------------------------------------- reads

    @Test
    @DisplayName("findAll maps entities back to domain objects, entities never escape")
    void findAllMapsEntities() {
        when(repository.findAll()).thenReturn(List.of(mapper.toEntity(sample())));
        List<Universe> all = service.findAll();
        assertEquals(1, all.size());
        assertEquals("Test Universe", all.get(0).name());
    }

    @Test
    @DisplayName("findAllActive delegates to repository.findByActive(true)")
    void findAllActiveDelegatesToFindByActiveTrue() {
        Universe activeSrc = sample().withActive(true);
        when(repository.findByActive(true)).thenReturn(List.of(mapper.toEntity(activeSrc)));
        List<Universe> active = service.findAllActive();
        assertEquals(1, active.size());
        assertTrue(active.get(0).active());
    }

    @Test
    @DisplayName("count delegates to the repository")
    void countDelegates() {
        when(repository.count()).thenReturn(15L);
        assertEquals(15L, service.count(),
                "F.1's V16 migration creates 15 Universe rows; service.count should reflect that");
    }

    @Test
    @DisplayName("findById round-trips through the mapper")
    void findByIdRoundTrips() {
        when(repository.findById("catalog-universe-test"))
                .thenReturn(Optional.of(mapper.toEntity(sample())));
        Optional<Universe> result = service.findById("catalog-universe-test");
        assertTrue(result.isPresent());
        assertEquals("Test Universe", result.get().name());
    }

    @Test
    @DisplayName("findById returns Optional.empty for unknown ids")
    void findByIdReturnsEmptyForUnknown() {
        when(repository.findById("absent")).thenReturn(Optional.empty());
        assertTrue(service.findById("absent").isEmpty());
    }

    @Test
    @DisplayName("findByName delegates and round-trips")
    void findByNameRoundTrips() {
        when(repository.findByNameIgnoreCase("Test Universe"))
                .thenReturn(Optional.of(mapper.toEntity(sample())));
        Optional<Universe> result = service.findByName("Test Universe");
        assertTrue(result.isPresent());
        assertEquals("catalog-universe-test", result.get().id());
    }

    @Test
    @DisplayName("existsByName delegates to the repository")
    void existsByNameDelegates() {
        when(repository.existsByNameIgnoreCase("Test Universe")).thenReturn(true);
        when(repository.existsByNameIgnoreCase("Phantom")).thenReturn(false);
        assertTrue(service.existsByName("Test Universe"));
        assertFalse(service.existsByName("Phantom"));
    }

    @Test
    @DisplayName("findByLifecycle delegates and maps")
    void findByLifecycleDelegates() {
        when(repository.findByLifecycle(UniverseLifecycle.AVAILABLE))
                .thenReturn(List.of(mapper.toEntity(sample())));
        List<Universe> available = service.findByLifecycle(UniverseLifecycle.AVAILABLE);
        assertEquals(1, available.size());
        assertEquals(UniverseLifecycle.AVAILABLE, available.get(0).lifecycle());
    }

    // ----------------------------------------------------- writes

    @Test
    @DisplayName("save persists via the repository and returns the round-tripped domain object")
    void savePersists() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        Universe saved = service.save(sample());
        assertEquals("Test Universe", saved.name());
        verify(repository).save(any(UniverseEntity.class));
    }

    @Test
    @DisplayName("deleteById removes the entity when present")
    void deleteRemovesWhenPresent() {
        UniverseEntity entity = mapper.toEntity(sample());
        when(repository.findById(sample().id())).thenReturn(Optional.of(entity));
        service.deleteById(sample().id());
        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("deleteById on a missing id is a no-op")
    void deleteMissingIsNoop() {
        when(repository.findById("absent")).thenReturn(Optional.empty());
        service.deleteById("absent");
        verify(repository, never()).delete(any());
    }

    // ----------------------------------------------------- syncCatalogEntries

    @Test
    @DisplayName("syncCatalogEntries returns zero — Catalog ships zero canonical Universe constants in F.1")
    void syncReturnsZeroWhenCatalogEmpty() {
        // Catalog.all() carries no Universe constants in Phase F.1; the 15 actual Universe rows
        // ship via V16 migration INSERTs, not via Catalog.all(). The sync's filter yields an
        // empty stream; the loop is vacuous.
        int inserted = service.syncCatalogEntries();
        assertEquals(0, inserted,
                "Phase F.1 ships zero canonical Universe constants; V16 ships 15 via migration INSERT");
        verify(repository, never()).save(any(UniverseEntity.class));
    }

    @Test
    @DisplayName("syncCatalogEntries is idempotent — second run after first still returns zero")
    void syncIsIdempotent() {
        int first = service.syncCatalogEntries();
        int second = service.syncCatalogEntries();
        assertEquals(0, first);
        assertEquals(0, second);
        verify(repository, never()).save(any(UniverseEntity.class));
    }

    // ----------------------------------------------------- Cataloged-typed accessor

    @Test
    @DisplayName("findAllAsCataloged returns results typed as Cataloged (NOT SpaceAsset)")
    void findAllAsCatalogedReturnsCatalogedTyping() {
        when(repository.findAll()).thenReturn(List.of(mapper.toEntity(sample())));
        List<Cataloged> all = service.findAllAsCataloged();
        assertEquals(1, all.size());
        assertTrue(all.get(0) instanceof Universe);
        assertTrue(all.get(0) instanceof Cataloged);
        // Sanity that Universe is NOT a SpaceAsset (catalog-uniformity without sealed-hierarchy
        // membership — same architectural shape as GateNetwork).
        assertFalse(all.get(0) instanceof SpaceAsset);
    }
}
