package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import com.teamgannon.trips.solarsystem.rendering.SolarSystemRenderer;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.shape.Sphere;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for SolarSystemLabelManager.
 * Note: Tests that require full JavaFX toolkit are limited to basic state verification.
 */
@ExtendWith(MockitoExtension.class)
class SolarSystemLabelManagerTest {

    @Mock
    private Group labelDisplayGroup;

    @Mock
    private SubScene subScene;

    @Mock
    private PerspectiveCamera camera;

    @Mock
    private SolarSystemRenderer renderer;

    private SolarSystemLabelManager labelManager;

    @BeforeEach
    void setUp() {
        labelManager = new SolarSystemLabelManager(labelDisplayGroup, subScene, camera, renderer);
    }

    @Nested
    @DisplayName("toggleLabels tests")
    class ToggleLabelsTests {

        @Test
        @DisplayName("should default to labels on")
        void shouldDefaultToLabelsOn() {
            assertTrue(labelManager.isLabelsOn());
        }

        @Test
        @DisplayName("should track labels on state")
        void shouldTrackLabelsOnState() {
            labelManager.toggleLabels(false);
            assertFalse(labelManager.isLabelsOn());

            labelManager.toggleLabels(true);
            assertTrue(labelManager.isLabelsOn());
        }

        @Test
        @DisplayName("should update group visibility when toggled off")
        void shouldUpdateGroupVisibility() {
            labelManager.toggleLabels(false);

            verify(labelDisplayGroup).setVisible(false);
        }

        @Test
        @DisplayName("should update group visibility when toggled on")
        void shouldShowGroupWhenToggledOn() {
            labelManager.toggleLabels(true);

            verify(labelDisplayGroup).setVisible(true);
        }
    }

    @Nested
    @DisplayName("updateLabels tests")
    class UpdateLabelsTests {

        @Test
        @DisplayName("should return early when labels are off")
        void shouldReturnEarlyWhenLabelsOff() {
            labelManager.toggleLabels(false);

            labelManager.updateLabels();

            // Should not call getShapeToLabel since we return early
            verify(renderer, never()).getShapeToLabel();
        }

        @Test
        @DisplayName("should return early when shapeToLabel is empty")
        void shouldReturnEarlyWhenEmpty() {
            when(renderer.getShapeToLabel()).thenReturn(Collections.emptyMap());

            labelManager.updateLabels();

            verify(renderer).getShapeToLabel();
            // No further processing needed
        }
    }

    @Nested
    @DisplayName("createLabelsForRenderedObjects tests")
    class CreateLabelsForRenderedObjectsTests {

        @Test
        @DisplayName("should handle null planet descriptions gracefully")
        void shouldHandleNullPlanetDescriptions() {
            SolarSystemDescription solarSystem = new SolarSystemDescription();
            solarSystem.setPlanetDescriptionList(null);

            // Should not throw - returns early when planet list is null
            assertDoesNotThrow(() -> labelManager.createLabelsForRenderedObjects(solarSystem));
        }

        @Test
        @DisplayName("should handle empty planet list")
        void shouldHandleEmptyPlanetList() {
            SolarSystemDescription solarSystem = new SolarSystemDescription();
            solarSystem.setPlanetDescriptionList(Collections.emptyList());

            // Should not throw - returns early when planet list is empty
            assertDoesNotThrow(() -> labelManager.createLabelsForRenderedObjects(solarSystem));
        }

        @Test
        @DisplayName("should skip planets with null sphere nodes")
        void shouldSkipPlanetsWithNullSphereNodes() {
            PlanetDescription planet = new PlanetDescription();
            planet.setId("planet-1");
            planet.setName("Test Planet");
            planet.setMoon(false);

            SolarSystemDescription solarSystem = new SolarSystemDescription();
            solarSystem.setPlanetDescriptionList(List.of(planet));

            // Return empty map - no sphere for this planet
            when(renderer.getPlanetNodes()).thenReturn(Collections.emptyMap());

            labelManager.createLabelsForRenderedObjects(solarSystem);

            // Should not create any labels since no sphere exists
            verify(renderer, never()).createLabel(anyString());
        }

        @Test
        @DisplayName("should skip null planets in list")
        void shouldSkipNullPlanets() {
            PlanetDescription validPlanet = new PlanetDescription();
            validPlanet.setId("valid-1");
            validPlanet.setName("Valid Planet");
            validPlanet.setMoon(false);

            SolarSystemDescription solarSystem = new SolarSystemDescription();
            solarSystem.setPlanetDescriptionList(java.util.Arrays.asList(null, validPlanet, null));

            when(renderer.getPlanetNodes()).thenReturn(Collections.emptyMap());

            // Should not throw
            assertDoesNotThrow(() -> labelManager.createLabelsForRenderedObjects(solarSystem));
        }
    }
}
