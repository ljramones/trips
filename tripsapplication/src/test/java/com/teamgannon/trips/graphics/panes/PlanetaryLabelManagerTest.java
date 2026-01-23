package com.teamgannon.trips.graphics.panes;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for PlanetaryLabelManager.
 */
@ExtendWith(MockitoExtension.class)
class PlanetaryLabelManagerTest {

    @Mock
    private Group starLabelGroup;

    @Mock
    private SubScene subScene;

    @Mock
    private PerspectiveCamera camera;

    @Mock
    private Supplier<Map<Node, Label>> shapeToLabelSupplier;

    private PlanetaryLabelManager labelManager;

    @BeforeEach
    void setUp() {
        labelManager = new PlanetaryLabelManager(starLabelGroup, subScene, camera, shapeToLabelSupplier);
    }

    @Nested
    @DisplayName("setStarLabelsOn tests")
    class SetStarLabelsOnTests {

        @Test
        @DisplayName("should default to labels on")
        void shouldDefaultToLabelsOn() {
            assertTrue(labelManager.isStarLabelsOn());
        }

        @Test
        @DisplayName("should track star labels state")
        void shouldTrackStarLabelsState() {
            when(starLabelGroup.getChildren()).thenReturn(new javafx.collections.ObservableListBase<>() {
                private final java.util.List<Node> list = new java.util.ArrayList<>();
                @Override public Node get(int index) { return list.get(index); }
                @Override public int size() { return list.size(); }
                @Override public void clear() { list.clear(); }
            });
            when(shapeToLabelSupplier.get()).thenReturn(Collections.emptyMap());

            labelManager.setStarLabelsOn(false);
            assertFalse(labelManager.isStarLabelsOn());

            labelManager.setStarLabelsOn(true);
            assertTrue(labelManager.isStarLabelsOn());
        }

        @Test
        @DisplayName("should clear labels when turned off")
        void shouldClearLabelsWhenTurnedOff() {
            when(starLabelGroup.getChildren()).thenReturn(new javafx.collections.ObservableListBase<>() {
                private final java.util.List<Node> list = new java.util.ArrayList<>();
                @Override public Node get(int index) { return list.get(index); }
                @Override public int size() { return list.size(); }
                @Override public void clear() { list.clear(); }
            });

            labelManager.setStarLabelsOn(false);

            verify(starLabelGroup).getChildren();
        }

        @Test
        @DisplayName("should call updateLabels when turned on")
        void shouldCallUpdateLabelsWhenTurnedOn() {
            when(shapeToLabelSupplier.get()).thenReturn(Collections.emptyMap());

            labelManager.setStarLabelsOn(true);

            verify(shapeToLabelSupplier).get();
        }
    }

    @Nested
    @DisplayName("updateLabels tests")
    class UpdateLabelsTests {

        @Test
        @DisplayName("should return early when labels are off")
        void shouldReturnEarlyWhenLabelsOff() {
            when(starLabelGroup.getChildren()).thenReturn(new javafx.collections.ObservableListBase<>() {
                private final java.util.List<Node> list = new java.util.ArrayList<>();
                @Override public Node get(int index) { return list.get(index); }
                @Override public int size() { return list.size(); }
                @Override public void clear() { list.clear(); }
            });

            labelManager.setStarLabelsOn(false);
            reset(shapeToLabelSupplier); // Reset to track new calls

            labelManager.updateLabels();

            verify(shapeToLabelSupplier, never()).get();
        }

        @Test
        @DisplayName("should return early when shapeToLabel is empty")
        void shouldReturnEarlyWhenEmpty() {
            when(shapeToLabelSupplier.get()).thenReturn(Collections.emptyMap());

            labelManager.updateLabels();

            verify(shapeToLabelSupplier).get();
            // No further processing since map is empty
        }
    }

    @Nested
    @DisplayName("clear tests")
    class ClearTests {

        @Test
        @DisplayName("should clear star label group")
        void shouldClearStarLabelGroup() {
            when(starLabelGroup.getChildren()).thenReturn(new javafx.collections.ObservableListBase<>() {
                private final java.util.List<Node> list = new java.util.ArrayList<>();
                @Override public Node get(int index) { return list.get(index); }
                @Override public int size() { return list.size(); }
                @Override public void clear() { list.clear(); }
            });

            labelManager.clear();

            verify(starLabelGroup).getChildren();
        }
    }

    @Nested
    @DisplayName("isStarLabelsOn tests")
    class IsStarLabelsOnTests {

        @Test
        @DisplayName("should return current state")
        void shouldReturnCurrentState() {
            assertTrue(labelManager.isStarLabelsOn());

            when(starLabelGroup.getChildren()).thenReturn(new javafx.collections.ObservableListBase<>() {
                private final java.util.List<Node> list = new java.util.ArrayList<>();
                @Override public Node get(int index) { return list.get(index); }
                @Override public int size() { return list.size(); }
                @Override public void clear() { list.clear(); }
            });
            labelManager.setStarLabelsOn(false);

            assertFalse(labelManager.isStarLabelsOn());
        }
    }
}
