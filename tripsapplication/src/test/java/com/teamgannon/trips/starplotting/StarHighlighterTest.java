package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.CurrentPlot;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.withSettings;

/**
 * Tests for StarHighlighter.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StarHighlighterTest {

    @Mock
    private TripsContext tripsContext;

    @Mock
    private SpecialStarMeshManager meshManager;

    @Mock
    private StarAnimationManager animationManager;

    @Mock
    private StarClickHandler clickHandler;

    @Mock
    private CurrentPlot currentPlot;

    private StarHighlighter highlighter;
    private Group stellarDisplayGroup;

    @BeforeEach
    void setUp() {
        highlighter = new StarHighlighter(tripsContext, meshManager, animationManager, clickHandler);
        stellarDisplayGroup = new Group();
        highlighter.initialize(stellarDisplayGroup);

        when(tripsContext.getCurrentPlot()).thenReturn(currentPlot);
    }

    @Nested
    @DisplayName("Initialization tests")
    class InitializationTests {

        @Test
        @DisplayName("should start with no highlight")
        void shouldStartWithNoHighlight() {
            assertFalse(highlighter.hasHighlight());
            assertNull(highlighter.getHighlightStar());
        }

        @Test
        @DisplayName("should accept display group during initialization")
        void shouldAcceptDisplayGroupDuringInitialization() {
            StarHighlighter newHighlighter = new StarHighlighter(
                    tripsContext, meshManager, animationManager, clickHandler
            );
            Group group = new Group();

            assertDoesNotThrow(() -> newHighlighter.initialize(group));
        }
    }

    @Nested
    @DisplayName("Highlight star tests")
    class HighlightStarTests {

        @Test
        @DisplayName("should create highlight star for valid star ID")
        void shouldCreateHighlightStarForValidStarId() {
            // Setup star in current plot
            StarDisplayRecord record = createMockRecord("TestStar", 10, 20, 30);
            Sphere originalStar = new Sphere(5);
            originalStar.setUserData(record);
            when(currentPlot.getStar("star-123")).thenReturn(originalStar);

            // Setup mesh manager to return highlight
            Sphere highlightSphere = new Sphere(8);
            when(meshManager.createHighlightStar(any(Color.class))).thenReturn(highlightSphere);

            highlighter.highlightStar("star-123");

            assertTrue(highlighter.hasHighlight());
            assertNotNull(highlighter.getHighlightStar());
            verify(meshManager).createHighlightStar(Color.YELLOW);
        }

        @Test
        @DisplayName("should position highlight at star coordinates")
        void shouldPositionHighlightAtStarCoordinates() {
            StarDisplayRecord record = createMockRecord("Star", 100, 200, 300);
            Sphere originalStar = new Sphere(5);
            originalStar.setUserData(record);
            when(currentPlot.getStar("star-id")).thenReturn(originalStar);

            Sphere highlightSphere = new Sphere(8);
            when(meshManager.createHighlightStar(any(Color.class))).thenReturn(highlightSphere);

            highlighter.highlightStar("star-id");

            Node highlight = highlighter.getHighlightStar();
            assertEquals(100, highlight.getTranslateX(), 0.001);
            assertEquals(200, highlight.getTranslateY(), 0.001);
            assertEquals(300, highlight.getTranslateZ(), 0.001);
        }

        @Test
        @DisplayName("should add highlight to display group")
        void shouldAddHighlightToDisplayGroup() {
            StarDisplayRecord record = createMockRecord("Star", 0, 0, 0);
            Sphere originalStar = new Sphere(5);
            originalStar.setUserData(record);
            when(currentPlot.getStar("star-id")).thenReturn(originalStar);

            Sphere highlightSphere = new Sphere(8);
            when(meshManager.createHighlightStar(any(Color.class))).thenReturn(highlightSphere);

            int childCountBefore = stellarDisplayGroup.getChildren().size();
            highlighter.highlightStar("star-id");
            int childCountAfter = stellarDisplayGroup.getChildren().size();

            assertEquals(childCountBefore + 1, childCountAfter);
            assertTrue(stellarDisplayGroup.getChildren().contains(highlighter.getHighlightStar()));
        }

        @Test
        @DisplayName("should start blink animation")
        void shouldStartBlinkAnimation() {
            StarDisplayRecord record = createMockRecord("Star", 0, 0, 0);
            Sphere originalStar = new Sphere(5);
            originalStar.setUserData(record);
            when(currentPlot.getStar("star-id")).thenReturn(originalStar);

            Sphere highlightSphere = new Sphere(8);
            when(meshManager.createHighlightStar(any(Color.class))).thenReturn(highlightSphere);

            highlighter.highlightStar("star-id");

            verify(animationManager).startHighlightAnimation(
                    eq(highlightSphere),
                    eq(100),  // HIGHLIGHT_BLINK_CYCLES
                    eq(2.0),  // SCALE_TRANSITION_DURATION_SECONDS
                    eq(2.0)   // ANIMATION_SCALE_MULTIPLIER
            );
        }

        @Test
        @DisplayName("should setup context menu on highlight")
        void shouldSetupContextMenuOnHighlight() {
            StarDisplayRecord record = createMockRecord("Star", 0, 0, 0);
            Sphere originalStar = new Sphere(5);
            originalStar.setUserData(record);
            when(currentPlot.getStar("star-id")).thenReturn(originalStar);

            Sphere highlightSphere = new Sphere(8);
            when(meshManager.createHighlightStar(any(Color.class))).thenReturn(highlightSphere);

            highlighter.highlightStar("star-id");

            verify(clickHandler).setupContextMenu(eq(record), eq(highlightSphere));
        }

        @Test
        @DisplayName("should not throw when star not found")
        void shouldNotThrowWhenStarNotFound() {
            when(currentPlot.getStar("nonexistent")).thenReturn(null);

            assertDoesNotThrow(() -> highlighter.highlightStar("nonexistent"));
            assertFalse(highlighter.hasHighlight());
        }

        @Test
        @DisplayName("should not throw when star has no user data")
        void shouldNotThrowWhenStarHasNoUserData() {
            Sphere starWithoutData = new Sphere(5);
            // No user data set
            when(currentPlot.getStar("star-id")).thenReturn(starWithoutData);

            assertDoesNotThrow(() -> highlighter.highlightStar("star-id"));
            assertFalse(highlighter.hasHighlight());
        }

        @Test
        @DisplayName("should not throw when not initialized")
        void shouldNotThrowWhenNotInitialized() {
            StarHighlighter uninitializedHighlighter = new StarHighlighter(
                    tripsContext, meshManager, animationManager, clickHandler
            );
            // Not calling initialize()

            assertDoesNotThrow(() -> uninitializedHighlighter.highlightStar("star-id"));
        }
    }

    @Nested
    @DisplayName("Remove highlight tests")
    class RemoveHighlightTests {

        @Test
        @DisplayName("should remove existing highlight")
        void shouldRemoveExistingHighlight() {
            // First create a highlight
            StarDisplayRecord record = createMockRecord("Star", 0, 0, 0);
            Sphere originalStar = new Sphere(5);
            originalStar.setUserData(record);
            when(currentPlot.getStar("star-id")).thenReturn(originalStar);

            Sphere highlightSphere = new Sphere(8);
            when(meshManager.createHighlightStar(any(Color.class))).thenReturn(highlightSphere);

            highlighter.highlightStar("star-id");
            assertTrue(highlighter.hasHighlight());

            // Now remove it
            highlighter.removeCurrentHighlight();

            assertFalse(highlighter.hasHighlight());
            assertNull(highlighter.getHighlightStar());
            assertFalse(stellarDisplayGroup.getChildren().contains(highlightSphere));
        }

        @Test
        @DisplayName("should not throw when no highlight exists")
        void shouldNotThrowWhenNoHighlightExists() {
            assertDoesNotThrow(() -> highlighter.removeCurrentHighlight());
        }

        @Test
        @DisplayName("should remove previous highlight when creating new one")
        void shouldRemovePreviousHighlightWhenCreatingNewOne() {
            // Create first highlight
            StarDisplayRecord record1 = createMockRecord("Star1", 0, 0, 0);
            Sphere originalStar1 = new Sphere(5);
            originalStar1.setUserData(record1);
            when(currentPlot.getStar("star-1")).thenReturn(originalStar1);

            Sphere highlight1 = new Sphere(8);
            when(meshManager.createHighlightStar(Color.YELLOW)).thenReturn(highlight1);

            highlighter.highlightStar("star-1");
            Node firstHighlight = highlighter.getHighlightStar();

            // Create second highlight
            StarDisplayRecord record2 = createMockRecord("Star2", 10, 10, 10);
            when(record2.getStarColor()).thenReturn(Color.RED);
            Sphere originalStar2 = new Sphere(5);
            originalStar2.setUserData(record2);
            when(currentPlot.getStar("star-2")).thenReturn(originalStar2);

            Sphere highlight2 = new Sphere(8);
            when(meshManager.createHighlightStar(Color.RED)).thenReturn(highlight2);

            highlighter.highlightStar("star-2");

            // First highlight should be removed
            assertFalse(stellarDisplayGroup.getChildren().contains(firstHighlight));
            assertTrue(stellarDisplayGroup.getChildren().contains(highlighter.getHighlightStar()));
        }
    }

    @Nested
    @DisplayName("Context menu factory tests")
    class ContextMenuFactoryTests {

        @Test
        @DisplayName("should use custom context menu factory when set")
        void shouldUseCustomContextMenuFactoryWhenSet() {
            // Setup custom factory
            StarHighlighter.ContextMenuFactory customFactory = mock(StarHighlighter.ContextMenuFactory.class);
            highlighter.setContextMenuFactory(customFactory);

            // Create highlight
            StarDisplayRecord record = createMockRecord("Star", 0, 0, 0);
            Sphere originalStar = new Sphere(5);
            originalStar.setUserData(record);
            when(currentPlot.getStar("star-id")).thenReturn(originalStar);

            Sphere highlightSphere = new Sphere(8);
            when(meshManager.createHighlightStar(any(Color.class))).thenReturn(highlightSphere);

            highlighter.highlightStar("star-id");

            // Custom factory should be called instead of clickHandler
            verify(customFactory).createContextMenu(eq(record), eq(highlightSphere));
            verify(clickHandler, never()).setupContextMenu(any(), any());
        }
    }

    @Nested
    @DisplayName("Animation callback tests")
    class AnimationCallbackTests {

        @Test
        @DisplayName("should setup animation completion callback")
        void shouldSetupAnimationCompletionCallback() {
            StarDisplayRecord record = createMockRecord("Star", 0, 0, 0);
            Sphere originalStar = new Sphere(5);
            originalStar.setUserData(record);
            when(currentPlot.getStar("star-id")).thenReturn(originalStar);

            Sphere highlightSphere = new Sphere(8);
            when(meshManager.createHighlightStar(any(Color.class))).thenReturn(highlightSphere);

            highlighter.highlightStar("star-id");

            verify(animationManager).setOnHighlightFinished(any());
        }
    }

    private StarDisplayRecord createMockRecord(String name, double x, double y, double z) {
        StarDisplayRecord record = mock(StarDisplayRecord.class, withSettings().lenient());
        lenient().when(record.getStarName()).thenReturn(name);
        lenient().when(record.getRecordId()).thenReturn("id-" + name);
        lenient().when(record.getCoordinates()).thenReturn(new Point3D(x, y, z));
        lenient().when(record.getStarColor()).thenReturn(Color.YELLOW);
        return record;
    }
}
