package com.teamgannon.trips.solarsystem.rendering;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Sphere;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for SelectionStyleManager.
 * Note: Tests avoid mocking JavaFX Label which requires toolkit initialization.
 */
@ExtendWith(MockitoExtension.class)
class SelectionStyleManagerTest {

    private SelectionStyleManager styleManager;

    @BeforeEach
    void setUp() {
        styleManager = new SelectionStyleManager();
    }

    @Nested
    @DisplayName("registerSelectableNode tests")
    class RegisterSelectableNodeTests {

        @Test
        @DisplayName("should handle null node gracefully")
        void shouldHandleNullNode() {
            assertDoesNotThrow(() -> styleManager.registerSelectableNode(null));
            assertTrue(styleManager.getBaseScales().isEmpty());
        }

        @Test
        @DisplayName("should store base scale for node")
        void shouldStoreBaseScale() {
            Sphere sphere = mock(Sphere.class);
            when(sphere.getScaleX()).thenReturn(2.5);
            when(sphere.getOpacity()).thenReturn(1.0);

            styleManager.registerSelectableNode(sphere);

            assertEquals(2.5, styleManager.getBaseScales().get(sphere), 0.001);
        }

        @Test
        @DisplayName("should store base opacity for node")
        void shouldStoreBaseOpacity() {
            Sphere sphere = mock(Sphere.class);
            when(sphere.getScaleX()).thenReturn(1.0);
            when(sphere.getOpacity()).thenReturn(0.8);

            styleManager.registerSelectableNode(sphere);

            assertEquals(0.8, styleManager.getBaseOpacities().get(sphere), 0.001);
        }
    }

    @Nested
    @DisplayName("registerOrbitSegments tests")
    class RegisterOrbitSegmentsTests {

        @Test
        @DisplayName("should handle empty group")
        void shouldHandleEmptyGroup() {
            Group orbitGroup = new Group();

            assertDoesNotThrow(() -> styleManager.registerOrbitSegments(orbitGroup));
        }
    }

    @Nested
    @DisplayName("clearSelection tests")
    class ClearSelectionTests {

        @Test
        @DisplayName("should clear selection state")
        void shouldClearSelectionState() {
            // Just verify it doesn't throw
            assertDoesNotThrow(() -> styleManager.clearSelection());
        }
    }

    @Nested
    @DisplayName("clear tests")
    class ClearTests {

        @Test
        @DisplayName("should clear all cached data")
        void shouldClearAllCachedData() {
            // Register some nodes first
            Sphere sphere = mock(Sphere.class);
            when(sphere.getScaleX()).thenReturn(1.0);
            when(sphere.getOpacity()).thenReturn(1.0);

            styleManager.registerSelectableNode(sphere);
            assertFalse(styleManager.getBaseScales().isEmpty());

            styleManager.clear();

            assertTrue(styleManager.getBaseScales().isEmpty());
            assertTrue(styleManager.getBaseOpacities().isEmpty());
        }
    }

    @Nested
    @DisplayName("applySelection tests")
    class ApplySelectionTests {

        @Test
        @DisplayName("should handle empty maps gracefully")
        void shouldHandleEmptyMaps() {
            assertDoesNotThrow(() -> styleManager.applySelection(
                    null,
                    null,
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap()
            ));
        }

        @Test
        @DisplayName("should skip null planet nodes")
        void shouldSkipNullPlanetNodes() {
            Map<String, Sphere> planetNodes = new HashMap<>();
            planetNodes.put("Planet1", null);

            assertDoesNotThrow(() -> styleManager.applySelection(
                    null,
                    null,
                    planetNodes,
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap()
            ));
        }

        @Test
        @DisplayName("should skip null star nodes")
        void shouldSkipNullStarNodes() {
            Map<String, Node> starNodes = new HashMap<>();
            starNodes.put("Star1", null);

            assertDoesNotThrow(() -> styleManager.applySelection(
                    null,
                    null,
                    Collections.emptyMap(),
                    starNodes,
                    Collections.emptyMap(),
                    Collections.emptyMap()
            ));
        }

        @Test
        @DisplayName("should skip null orbit groups")
        void shouldSkipNullOrbitGroups() {
            Map<String, Group> orbitGroups = new HashMap<>();
            orbitGroups.put("Orbit1", null);

            assertDoesNotThrow(() -> styleManager.applySelection(
                    null,
                    null,
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    orbitGroups,
                    Collections.emptyMap()
            ));
        }

        @Test
        @DisplayName("should apply selected style to matching planet")
        void shouldApplySelectedStyleToMatchingPlanet() {
            Sphere selectedPlanet = mock(Sphere.class);
            when(selectedPlanet.getScaleX()).thenReturn(1.0);
            when(selectedPlanet.getOpacity()).thenReturn(1.0);

            styleManager.registerSelectableNode(selectedPlanet);

            Map<String, Sphere> planetNodes = new HashMap<>();
            planetNodes.put("Planet1", selectedPlanet);

            styleManager.applySelection(
                    selectedPlanet,
                    null,
                    planetNodes,
                    Collections.emptyMap(),
                    Collections.emptyMap(),
                    Collections.emptyMap()
            );

            // Verify glow effect was applied
            verify(selectedPlanet).setEffect(any());
            verify(selectedPlanet).setOpacity(1.0);
        }
    }
}
