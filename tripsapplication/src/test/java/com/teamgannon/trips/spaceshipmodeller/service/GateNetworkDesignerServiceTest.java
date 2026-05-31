package com.teamgannon.trips.spaceshipmodeller.service;

import com.teamgannon.trips.spaceshipmodeller.persistence.GateNetworkEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.GateNetworkMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.GateNetworkRepository;
import com.terranrepublic.assets.Cataloged;
import com.terranrepublic.assets.CatalogOperationalStatus;
import com.terranrepublic.assets.CatalogProvenance;
import com.terranrepublic.assets.GateNetwork;
import com.terranrepublic.assets.GateNetworkLifecycle;
import com.terranrepublic.assets.SourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
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
 * Unit tests for {@link GateNetworkDesignerService}. Mirrors
 * {@link MegastructureDesignerServiceTest}'s shape: mocked repository + real mapper, covers
 * find/save/delete + the D.8 sync-by-id contract.
 *
 * <p>v2 Phase E.1 §5 — first catalog-pipeline test for a service outside the
 * {@code SpaceAsset} / {@code SpaceInfrastructure} sealed hierarchies. The
 * {@code findAllAsCataloged} accessor returns {@code List<Cataloged>} (not
 * {@code List<SpaceAsset>}); this test exercises that distinction.
 */
@ExtendWith(MockitoExtension.class)
class GateNetworkDesignerServiceTest {

    @Mock
    private GateNetworkRepository repository;

    private final GateNetworkMapper mapper = new GateNetworkMapper();
    private GateNetworkDesignerService service;

    @BeforeEach
    void setUp() {
        service = new GateNetworkDesignerService(repository, mapper);
    }

    private static GateNetwork sample() {
        return new GateNetwork(
                "catalog-network-test",
                "Test Network",
                "Test Polity",
                GateNetworkLifecycle.ACTIVE,
                "TEST-XPDR",
                "A test gate network.",
                null,
                "test-category",
                new CatalogProvenance(SourceType.SCIENCE_FICTION, "Test Universe", null,
                        CatalogOperationalStatus.FICTIONAL),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-01T00:00:00Z"));
    }

    // ----------------------------------------------------- reads

    @Test
    @DisplayName("findAll maps entities back to domain objects, entities never escape")
    void findAllMapsEntities() {
        when(repository.findAll()).thenReturn(List.of(mapper.toEntity(sample())));
        List<GateNetwork> all = service.findAll();
        assertEquals(1, all.size());
        assertEquals("Test Network", all.get(0).name());
    }

    @Test
    @DisplayName("count delegates to the repository")
    void countDelegates() {
        when(repository.count()).thenReturn(5L);
        assertEquals(5L, service.count());
    }

    @Test
    @DisplayName("findById round-trips through the mapper")
    void findByIdRoundTrips() {
        when(repository.findById("catalog-network-test"))
                .thenReturn(Optional.of(mapper.toEntity(sample())));
        Optional<GateNetwork> result = service.findById("catalog-network-test");
        assertTrue(result.isPresent());
        assertEquals("Test Network", result.get().name());
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
        when(repository.existsByNameIgnoreCase("Test Network")).thenReturn(true);
        when(repository.existsByNameIgnoreCase("Phantom")).thenReturn(false);
        assertTrue(service.existsByName("Test Network"));
        assertFalse(service.existsByName("Phantom"));
    }

    @Test
    @DisplayName("findByLifecycle delegates and maps")
    void findByLifecycleDelegates() {
        when(repository.findByLifecycle(GateNetworkLifecycle.ACTIVE))
                .thenReturn(List.of(mapper.toEntity(sample())));
        List<GateNetwork> active = service.findByLifecycle(GateNetworkLifecycle.ACTIVE);
        assertEquals(1, active.size());
        assertEquals(GateNetworkLifecycle.ACTIVE, active.get(0).lifecycle());
    }

    // ----------------------------------------------------- writes

    @Test
    @DisplayName("save persists via the repository and returns the round-tripped domain object")
    void savePersists() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        GateNetwork saved = service.save(sample());
        assertEquals("Test Network", saved.name());
        verify(repository).save(any(GateNetworkEntity.class));
    }

    @Test
    @DisplayName("deleteById removes the entity when present")
    void deleteRemovesWhenPresent() {
        GateNetworkEntity entity = mapper.toEntity(sample());
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
    @DisplayName("syncCatalogEntries returns zero today — Catalog ships no canonical GateNetworks in E.1")
    void syncReturnsZeroWhenCatalogEmpty() {
        // Catalog.all() carries no GateNetwork constants in Phase E.1. The sync's filter
        // yields an empty stream; the loop is vacuous.
        int inserted = service.syncCatalogEntries();
        assertEquals(0, inserted,
                "Phase E.1 ships zero canonical GateNetwork constants; E.2 populates");
        verify(repository, never()).save(any(GateNetworkEntity.class));
    }

    @Test
    @DisplayName("syncCatalogEntries is idempotent — second run after first still returns zero")
    void syncIsIdempotent() {
        int first = service.syncCatalogEntries();
        int second = service.syncCatalogEntries();
        assertEquals(0, first);
        assertEquals(0, second);
        verify(repository, never()).save(any(GateNetworkEntity.class));
    }

    // ----------------------------------------------------- Cataloged-typed accessor

    @Test
    @DisplayName("findAllAsCataloged returns results typed as Cataloged (NOT SpaceAsset)")
    void findAllAsCatalogedReturnsCatalogedTyping() {
        when(repository.findAll()).thenReturn(List.of(mapper.toEntity(sample())));
        List<Cataloged> all = service.findAllAsCataloged();
        assertEquals(1, all.size());
        assertTrue(all.get(0) instanceof GateNetwork);
        assertTrue(all.get(0) instanceof Cataloged);
        // Sanity that GateNetwork is NOT a SpaceAsset (catalog-uniformity without
        // sealed-hierarchy membership).
        assertFalse(all.get(0) instanceof com.terranrepublic.assets.SpaceAsset);
    }
}
