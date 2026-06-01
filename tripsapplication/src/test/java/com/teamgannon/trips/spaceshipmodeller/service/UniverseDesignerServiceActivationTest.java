package com.teamgannon.trips.spaceshipmodeller.service;

import com.teamgannon.trips.spaceshipmodeller.persistence.UniverseEntity;
import com.teamgannon.trips.spaceshipmodeller.persistence.UniverseMapper;
import com.teamgannon.trips.spaceshipmodeller.persistence.UniverseRepository;
import com.teamgannon.trips.worldbuilding.UniverseActivationChangedEvent;
import com.terranrepublic.assets.Universe;
import com.terranrepublic.assets.UniverseLifecycle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * v2 Phase F.1 §5 — behavior tests for {@link UniverseDesignerService#activate(String)} and
 * {@link UniverseDesignerService#deactivate(String)}.
 *
 * <p>Pins the contract:
 * <ul>
 *   <li>activate(id) sets {@code active=true}, persists, publishes a
 *       {@link UniverseActivationChangedEvent} with the post-toggle universe.</li>
 *   <li>deactivate(id) is the mirror operation.</li>
 *   <li>Always-publish: redundant activate (already-active) still publishes an event. Same for
 *       redundant deactivate. Consumers deduplicate if they care.</li>
 *   <li>Unknown id throws {@link IllegalArgumentException} (no Optional / no silent no-op).</li>
 *   <li>Event payload's nowActive matches the post-toggle state on the universe record.</li>
 * </ul>
 *
 * <p>Sibling to {@code UniverseDesignerServiceTest} (Step 2): that test covers find/save/delete +
 * sync; this one covers activation. Split for cohesion.
 */
@ExtendWith(MockitoExtension.class)
class UniverseDesignerServiceActivationTest {

    @Mock
    private UniverseRepository repository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private final UniverseMapper mapper = new UniverseMapper();
    private UniverseDesignerService service;

    @BeforeEach
    void setUp() {
        service = new UniverseDesignerService(repository, mapper, eventPublisher);
    }

    /** Returns an inactive universe entity (matching the F.1 default-real-only invariant). */
    private static UniverseEntity inactiveEntity(String id) {
        UniverseEntity entity = new UniverseEntity("Test Universe");
        entity.setId(id);
        entity.setActive(false);
        return entity;
    }

    /** Returns an active universe entity (for redundant-activate / deactivate scenarios). */
    private static UniverseEntity activeEntity(String id) {
        UniverseEntity entity = new UniverseEntity("Test Universe");
        entity.setId(id);
        entity.setActive(true);
        return entity;
    }

    // ============================================================
    // activate()
    // ============================================================

    @Test
    @DisplayName("activate() flips active=false -> true and persists")
    void activateFlipsToActive() {
        String id = "catalog-universe-test";
        UniverseEntity entity = inactiveEntity(id);
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(any(UniverseEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Universe result = service.activate(id);

        assertTrue(result.active(), "post-activation universe must have active=true");
        verify(repository).save(entity);
        assertTrue(entity.isActive(), "entity's active state mutated to true before save");
    }

    @Test
    @DisplayName("activate() publishes UniverseActivationChangedEvent with post-toggle state")
    void activatePublishesEvent() {
        String id = "catalog-universe-test";
        when(repository.findById(id)).thenReturn(Optional.of(inactiveEntity(id)));
        when(repository.save(any(UniverseEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.activate(id);

        ArgumentCaptor<UniverseActivationChangedEvent> captor =
                ArgumentCaptor.forClass(UniverseActivationChangedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        UniverseActivationChangedEvent event = captor.getValue();
        assertTrue(event.nowActive(), "event.nowActive must be true after activate");
        assertTrue(event.universe().active(), "event.universe.active must match nowActive");
        assertEquals(id, event.universe().id());
    }

    @Test
    @DisplayName("activate() on already-active universe still publishes (always-publish semantics)")
    void redundantActivateStillPublishes() {
        String id = "catalog-universe-test";
        when(repository.findById(id)).thenReturn(Optional.of(activeEntity(id)));
        when(repository.save(any(UniverseEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Universe result = service.activate(id);

        assertTrue(result.active(), "still active after redundant activate");
        verify(eventPublisher, times(1)).publishEvent(any(UniverseActivationChangedEvent.class));
    }

    @Test
    @DisplayName("activate() throws IllegalArgumentException for unknown id")
    void activateThrowsForUnknownId() {
        when(repository.findById("does-not-exist")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.activate("does-not-exist"));
        assertTrue(ex.getMessage().contains("does-not-exist"),
                "exception message must include the offending id; was: " + ex.getMessage());
        verify(eventPublisher, never()).publishEvent(any());
        verify(repository, never()).save(any());
    }

    // ============================================================
    // deactivate()
    // ============================================================

    @Test
    @DisplayName("deactivate() flips active=true -> false and persists")
    void deactivateFlipsToInactive() {
        String id = "catalog-universe-test";
        UniverseEntity entity = activeEntity(id);
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(any(UniverseEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Universe result = service.deactivate(id);

        assertFalse(result.active());
        assertFalse(entity.isActive());
        verify(repository).save(entity);
    }

    @Test
    @DisplayName("deactivate() publishes UniverseActivationChangedEvent with nowActive=false")
    void deactivatePublishesEvent() {
        String id = "catalog-universe-test";
        when(repository.findById(id)).thenReturn(Optional.of(activeEntity(id)));
        when(repository.save(any(UniverseEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.deactivate(id);

        ArgumentCaptor<UniverseActivationChangedEvent> captor =
                ArgumentCaptor.forClass(UniverseActivationChangedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        UniverseActivationChangedEvent event = captor.getValue();
        assertFalse(event.nowActive());
        assertFalse(event.universe().active());
    }

    @Test
    @DisplayName("deactivate() on already-inactive universe still publishes (always-publish)")
    void redundantDeactivateStillPublishes() {
        String id = "catalog-universe-test";
        when(repository.findById(id)).thenReturn(Optional.of(inactiveEntity(id)));
        when(repository.save(any(UniverseEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Universe result = service.deactivate(id);

        assertFalse(result.active());
        verify(eventPublisher, times(1)).publishEvent(any(UniverseActivationChangedEvent.class));
    }

    @Test
    @DisplayName("deactivate() throws IllegalArgumentException for unknown id")
    void deactivateThrowsForUnknownId() {
        when(repository.findById("phantom")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.deactivate("phantom"));
        verify(eventPublisher, never()).publishEvent(any());
        verify(repository, never()).save(any());
    }

    // ============================================================
    // State churn sequence
    // ============================================================

    @Test
    @DisplayName("activate-deactivate-activate sequence publishes 3 events with alternating nowActive values")
    void activateDeactivateActivateSequence() {
        String id = "catalog-universe-test";
        UniverseEntity entity = inactiveEntity(id);
        // Each repository.findById call returns the same entity instance; its active state
        // mutates between calls because the service mutates it in setActiveState.
        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(any(UniverseEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Universe afterActivate1 = service.activate(id);
        Universe afterDeactivate = service.deactivate(id);
        Universe afterActivate2 = service.activate(id);

        assertTrue(afterActivate1.active());
        assertFalse(afterDeactivate.active());
        assertTrue(afterActivate2.active());

        ArgumentCaptor<UniverseActivationChangedEvent> captor =
                ArgumentCaptor.forClass(UniverseActivationChangedEvent.class);
        verify(eventPublisher, times(3)).publishEvent(captor.capture());
        assertEquals(true, captor.getAllValues().get(0).nowActive());
        assertEquals(false, captor.getAllValues().get(1).nowActive());
        assertEquals(true, captor.getAllValues().get(2).nowActive());
    }
}
