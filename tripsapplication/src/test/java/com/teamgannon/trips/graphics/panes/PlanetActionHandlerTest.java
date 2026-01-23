package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.dialogs.solarsystem.PlanetEditResult;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import com.teamgannon.trips.service.SolarSystemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for PlanetActionHandler.
 */
@ExtendWith(MockitoExtension.class)
class PlanetActionHandlerTest {

    @Mock
    private SolarSystemService solarSystemService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AtomicBoolean refreshCalled;
    private PlanetActionHandler handler;

    @BeforeEach
    void setUp() {
        refreshCalled = new AtomicBoolean(false);
        handler = new PlanetActionHandler(solarSystemService, eventPublisher, () -> refreshCalled.set(true));
    }

    @Nested
    @DisplayName("handlePlanetEdit tests")
    class HandlePlanetEditTests {

        @Test
        @DisplayName("should do nothing when result has no changes")
        void shouldDoNothingWhenNoChanges() {
            ExoPlanet planet = createTestPlanet();
            PlanetEditResult result = new PlanetEditResult(planet, false, false);

            handler.handlePlanetEdit(result);

            verify(solarSystemService, never()).updateExoPlanet(any());
            assertFalse(refreshCalled.get());
        }

        @Test
        @DisplayName("should update planet when changes exist")
        void shouldUpdatePlanetWhenChangesExist() {
            ExoPlanet planet = createTestPlanet();
            PlanetEditResult result = new PlanetEditResult(planet, true, false);

            handler.handlePlanetEdit(result);

            verify(solarSystemService).updateExoPlanet(planet);
        }

        @Test
        @DisplayName("should refresh system when orbital changes and system exists")
        void shouldRefreshWhenOrbitalChanges() {
            ExoPlanet planet = createTestPlanet();
            PlanetEditResult result = new PlanetEditResult(planet, true, true);

            // Set current system
            SolarSystemDescription system = createTestSystem();
            handler.setCurrentSystem(system);

            handler.handlePlanetEdit(result);

            verify(solarSystemService).updateExoPlanet(planet);
            assertTrue(refreshCalled.get());
        }

        @Test
        @DisplayName("should not refresh when orbital changes but no system")
        void shouldNotRefreshWhenNoSystem() {
            ExoPlanet planet = createTestPlanet();
            PlanetEditResult result = new PlanetEditResult(planet, true, true);

            // No system set

            handler.handlePlanetEdit(result);

            verify(solarSystemService).updateExoPlanet(planet);
            assertFalse(refreshCalled.get());
        }
    }

    @Nested
    @DisplayName("handlePlanetDelete tests")
    class HandlePlanetDeleteTests {

        @Test
        @DisplayName("should delete planet and refresh")
        void shouldDeletePlanetAndRefresh() {
            ExoPlanet planet = createTestPlanet();
            SolarSystemDescription system = createTestSystem();
            handler.setCurrentSystem(system);

            handler.handlePlanetDelete(planet);

            verify(solarSystemService).deleteExoPlanet(planet.getId());
            assertTrue(refreshCalled.get());
        }

        @Test
        @DisplayName("should delete planet without refresh when no system")
        void shouldDeleteWithoutRefreshWhenNoSystem() {
            ExoPlanet planet = createTestPlanet();
            // No system set

            handler.handlePlanetDelete(planet);

            verify(solarSystemService).deleteExoPlanet(planet.getId());
            assertFalse(refreshCalled.get());
        }
    }

    @Nested
    @DisplayName("handleAddPlanet tests")
    class HandleAddPlanetTests {

        @Test
        @DisplayName("should do nothing when planet is null")
        void shouldDoNothingWhenPlanetNull() {
            handler.handleAddPlanet(null);

            verify(solarSystemService, never()).addExoPlanet(any());
            assertFalse(refreshCalled.get());
        }

        @Test
        @DisplayName("should add planet and refresh")
        void shouldAddPlanetAndRefresh() {
            ExoPlanet planet = createTestPlanet();

            handler.handleAddPlanet(planet);

            verify(solarSystemService).addExoPlanet(planet);
            assertTrue(refreshCalled.get());
        }
    }

    @Nested
    @DisplayName("handleLandOnPlanet tests")
    class HandleLandOnPlanetTests {

        @Test
        @DisplayName("should do nothing when planet is null")
        void shouldDoNothingWhenPlanetNull() {
            SolarSystemDescription system = createTestSystem();
            handler.setCurrentSystem(system);

            handler.handleLandOnPlanet(null);

            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("should do nothing when system is null")
        void shouldDoNothingWhenSystemNull() {
            ExoPlanet planet = createTestPlanet();
            // No system set

            handler.handleLandOnPlanet(planet);

            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("should publish event when planet and system exist")
        void shouldPublishEventWhenValid() {
            ExoPlanet planet = createTestPlanet();
            SolarSystemDescription system = createTestSystem();
            handler.setCurrentSystem(system);

            handler.handleLandOnPlanet(planet);

            verify(eventPublisher).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("findExoPlanetByName tests")
    class FindExoPlanetByNameTests {

        @Test
        @DisplayName("should delegate to service")
        void shouldDelegateToService() {
            ExoPlanet expected = createTestPlanet();
            when(solarSystemService.findExoPlanetByName("Test Planet")).thenReturn(expected);

            ExoPlanet result = handler.findExoPlanetByName("Test Planet");

            assertEquals(expected, result);
            verify(solarSystemService).findExoPlanetByName("Test Planet");
        }
    }

    private ExoPlanet createTestPlanet() {
        ExoPlanet planet = new ExoPlanet();
        planet.setId("test-planet-id-123");
        planet.setName("Test Planet");
        planet.setSemiMajorAxis(1.0);
        return planet;
    }

    private SolarSystemDescription createTestSystem() {
        StarDisplayRecord star = new StarDisplayRecord();
        star.setStarName("Test Star");
        star.setRecordId("test-star-id");

        SolarSystemDescription system = new SolarSystemDescription();
        system.setStarDisplayRecord(star);
        return system;
    }
}
