package com.teamgannon.trips.spaceshipmodeller.service;

import com.teamgannon.trips.spaceshipmodeller.persistence.TransportNodeEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.TransportNodeMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.TransportNodeRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransportNodeServiceTest {

    @Mock
    private TransportNodeRepository repository;

    private final TransportNodeMapper mapper = new TransportNodeMapper();
    private TransportNodeService service;

    @BeforeEach
    void setUp() {
        service = new TransportNodeService(repository, mapper);
    }

    private TransportNode ringGate(String name) {
        Instant now = Instant.parse("2025-05-15T10:00:00Z");
        return new TransportNode(
                UUID.randomUUID().toString(),
                name,
                "Coverage Source",
                "Coverage Faction",
                false,
                "description",
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
    @DisplayName("findAll maps entities to domain objects")
    void findAllMapsEntities() {
        TransportNode node = ringGate("Sol Gate");
        when(repository.findAll()).thenReturn(List.of(mapper.toEntity(node)));
        List<TransportNode> all = service.findAll();
        assertEquals(1, all.size());
        assertEquals("Sol Gate", all.get(0).name());
    }

    @Test
    @DisplayName("save persists via the repository and returns the round-tripped domain object")
    void savePersists() {
        TransportNode node = ringGate("Sol Gate");
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        TransportNode saved = service.save(node);
        assertEquals("Sol Gate", saved.name());
        verify(repository).save(any(TransportNodeEntity.class));
    }

    @Test
    @DisplayName("deleteById removes the entity when present")
    void deleteRemovesWhenPresent() {
        TransportNode node = ringGate("Sol Gate");
        TransportNodeEntity entity = mapper.toEntity(node);
        when(repository.findById(node.id())).thenReturn(Optional.of(entity));
        service.deleteById(node.id());
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
    @DisplayName("syncCatalogEntries returns zero (no canonical transport nodes in Catalog today)")
    void syncReturnsZeroBecauseNoCatalogEntries() {
        int inserted = service.syncCatalogEntries();
        assertEquals(0, inserted,
                "Catalog has no TransportNode entries; D.8 sync-by-id pattern is in place but vacuous");
        verify(repository, never()).save(any(TransportNodeEntity.class));
    }

    @Test
    @DisplayName("syncCatalogEntries is idempotent — second run after first still returns zero")
    void syncIsIdempotent() {
        int first = service.syncCatalogEntries();
        int second = service.syncCatalogEntries();
        assertEquals(0, first);
        assertEquals(0, second);
        verify(repository, never()).save(any(TransportNodeEntity.class));
    }

    @Test
    @DisplayName("findAllAsInfrastructure exposes results as SpaceInfrastructure for the construct registry")
    void findAllAsInfrastructureReturnsInfrastructureView() {
        when(repository.findAll()).thenReturn(List.of(mapper.toEntity(ringGate("Sol Gate"))));
        List<SpaceInfrastructure> infra = service.findAllAsInfrastructure();
        assertEquals(1, infra.size());
        assertTrue(infra.get(0) instanceof TransportNode);
    }

    @Test
    @DisplayName("existsByName delegates to the repository")
    void existsByNameDelegates() {
        when(repository.existsByNameIgnoreCase("Sol Gate")).thenReturn(true);
        when(repository.existsByNameIgnoreCase("Phantom")).thenReturn(false);
        assertTrue(service.existsByName("Sol Gate"));
        assertFalse(service.existsByName("Phantom"));
    }
}
