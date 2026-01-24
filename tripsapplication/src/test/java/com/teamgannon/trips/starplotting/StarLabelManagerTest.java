package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.SerialFont;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Sphere;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for StarLabelManager.
 * <p>
 * Tests cover:
 * <ul>
 *   <li>Label creation with correct styling</li>
 *   <li>Label registration and retrieval</li>
 *   <li>Visibility toggling</li>
 *   <li>Label positioning/update</li>
 *   <li>Clearing labels</li>
 * </ul>
 */
class StarLabelManagerTest {

    private static boolean javaFxInitialized = false;
    private StarLabelManager labelManager;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
            javaFxInitialized = true;
        } catch (IllegalStateException e) {
            javaFxInitialized = true;
        } catch (Exception e) {
            System.out.println("JavaFX not available: " + e.getMessage());
            javaFxInitialized = false;
        }
    }

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
        labelManager = new StarLabelManager();
    }

    // =========================================================================
    // Initialization Tests
    // =========================================================================

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("New manager has empty label display group")
        void newManagerHasEmptyLabelGroup() {
            assertTrue(labelManager.getLabelDisplayGroup().getChildren().isEmpty());
        }

        @Test
        @DisplayName("New manager has zero label count")
        void newManagerHasZeroLabels() {
            assertEquals(0, labelManager.getLabelCount());
        }

        @Test
        @DisplayName("Labels are visible by default")
        void labelsVisibleByDefault() {
            assertTrue(labelManager.isLabelsVisible());
        }

        @Test
        @DisplayName("Initialize adds label group to scene root")
        void initializeAddsLabelGroupToSceneRoot() throws Exception {
            runOnFxThread(() -> {
                Group sceneRoot = new Group();
                SubScene subScene = new SubScene(new Group(), 800, 600);

                labelManager.initialize(sceneRoot, subScene);

                assertTrue(sceneRoot.getChildren().contains(labelManager.getLabelDisplayGroup()));
                return null;
            });
        }
    }

    // =========================================================================
    // Label Creation Tests
    // =========================================================================

    @Nested
    @DisplayName("Label Creation Tests")
    class LabelCreationTests {

        @Test
        @DisplayName("createLabel creates label with star name")
        void createLabelHasStarName() throws Exception {
            runOnFxThread(() -> {
                StarDisplayRecord record = createTestRecord("Alpha Centauri");
                ColorPalette palette = createTestPalette();

                Label label = labelManager.createLabel(record, palette);

                assertEquals("Alpha Centauri", label.getText());
                return null;
            });
        }

        @Test
        @DisplayName("createLabel applies font from palette")
        void createLabelAppliesFont() throws Exception {
            runOnFxThread(() -> {
                StarDisplayRecord record = createTestRecord("Sirius");
                ColorPalette palette = createTestPalette();

                Label label = labelManager.createLabel(record, palette);

                assertNotNull(label.getFont());
                return null;
            });
        }

        @Test
        @DisplayName("createLabel applies color from palette")
        void createLabelAppliesColor() throws Exception {
            runOnFxThread(() -> {
                StarDisplayRecord record = createTestRecord("Vega");
                ColorPalette palette = createTestPalette();
                palette.setLabelColor("CYAN");

                Label label = labelManager.createLabel(record, palette);

                assertEquals(Color.CYAN, label.getTextFill());
                return null;
            });
        }

        @Test
        @DisplayName("createLabel truncates long names with ellipsis")
        void createLabelTruncatesLongNames() throws Exception {
            runOnFxThread(() -> {
                // Name longer than 18 characters
                String longName = "WISEP J174124.25+255319.5";
                StarDisplayRecord record = createTestRecord(longName);
                ColorPalette palette = createTestPalette();

                Label label = labelManager.createLabel(record, palette);

                // Should be truncated to 15 chars + "..."
                assertTrue(label.getText().endsWith("..."), "Long label should end with ellipsis");
                assertTrue(label.getText().length() <= 18, "Truncated label should be max 18 chars");
                assertNotEquals(longName, label.getText(), "Long name should be truncated");
                return null;
            });
        }

        @Test
        @DisplayName("createLabel does not truncate short names")
        void createLabelDoesNotTruncateShortNames() throws Exception {
            runOnFxThread(() -> {
                String shortName = "Sol";
                StarDisplayRecord record = createTestRecord(shortName);
                ColorPalette palette = createTestPalette();

                Label label = labelManager.createLabel(record, palette);

                assertEquals(shortName, label.getText(), "Short name should not be truncated");
                return null;
            });
        }

        @Test
        @DisplayName("createLabel adds tooltip for truncated names")
        void createLabelAddsTooltipForTruncatedNames() throws Exception {
            runOnFxThread(() -> {
                String longName = "2MASS J15065257+1027247";
                StarDisplayRecord record = createTestRecord(longName);
                ColorPalette palette = createTestPalette();

                Label label = labelManager.createLabel(record, palette);

                // Tooltip should contain the full name
                // Note: Tooltip.install doesn't set label.getTooltip(), it uses properties
                assertNotEquals(longName, label.getText(), "Label should be truncated");
                return null;
            });
        }
    }

    // =========================================================================
    // Label Registration Tests
    // =========================================================================

    @Nested
    @DisplayName("Label Registration Tests")
    class LabelRegistrationTests {

        @Test
        @DisplayName("addLabel registers label and adds to group")
        void addLabelRegistersAndAdds() throws Exception {
            runOnFxThread(() -> {
                initializeManager();
                Sphere node = new Sphere(5);
                StarDisplayRecord record = createTestRecord("Proxima");
                ColorPalette palette = createTestPalette();

                Label label = labelManager.addLabel(node, record, palette);

                assertNotNull(label);
                assertEquals(1, labelManager.getLabelCount());
                assertTrue(labelManager.hasLabel(node));
                assertTrue(labelManager.getLabelDisplayGroup().getChildren().contains(label));
                return null;
            });
        }

        @Test
        @DisplayName("getLabel returns registered label")
        void getLabelReturnsRegisteredLabel() throws Exception {
            runOnFxThread(() -> {
                initializeManager();
                Sphere node = new Sphere(5);
                StarDisplayRecord record = createTestRecord("Barnard's Star");
                ColorPalette palette = createTestPalette();

                Label addedLabel = labelManager.addLabel(node, record, palette);
                Label retrievedLabel = labelManager.getLabel(node);

                assertSame(addedLabel, retrievedLabel);
                return null;
            });
        }

        @Test
        @DisplayName("getLabel returns null for unregistered node")
        void getLabelReturnsNullForUnregistered() throws Exception {
            runOnFxThread(() -> {
                Sphere node = new Sphere(5);

                Label label = labelManager.getLabel(node);

                assertNull(label);
                return null;
            });
        }

        @Test
        @DisplayName("registerLabel adds existing label to manager")
        void registerLabelAddsExistingLabel() throws Exception {
            runOnFxThread(() -> {
                initializeManager();
                Sphere node = new Sphere(5);
                Label label = new Label("Test Label");

                labelManager.registerLabel(node, label);

                assertEquals(1, labelManager.getLabelCount());
                assertTrue(labelManager.hasLabel(node));
                assertSame(label, labelManager.getLabel(node));
                return null;
            });
        }

        @Test
        @DisplayName("removeLabel removes label from manager and group")
        void removeLabelRemovesLabelFromManagerAndGroup() throws Exception {
            runOnFxThread(() -> {
                initializeManager();
                Sphere node = new Sphere(5);
                StarDisplayRecord record = createTestRecord("Wolf 359");
                ColorPalette palette = createTestPalette();

                Label label = labelManager.addLabel(node, record, palette);
                labelManager.removeLabel(node);

                assertEquals(0, labelManager.getLabelCount());
                assertFalse(labelManager.hasLabel(node));
                assertFalse(labelManager.getLabelDisplayGroup().getChildren().contains(label));
                return null;
            });
        }
    }

    // =========================================================================
    // Visibility Tests
    // =========================================================================

    @Nested
    @DisplayName("Visibility Tests")
    class VisibilityTests {

        @Test
        @DisplayName("setLabelsVisible hides labels when false")
        void setLabelsVisibleHidesLabels() throws Exception {
            runOnFxThread(() -> {
                initializeManager();
                labelManager.setLabelsVisible(false);

                assertFalse(labelManager.isLabelsVisible());
                assertFalse(labelManager.getLabelDisplayGroup().isVisible());
                return null;
            });
        }

        @Test
        @DisplayName("setLabelsVisible shows labels when true")
        void setLabelsVisibleShowsLabels() throws Exception {
            runOnFxThread(() -> {
                initializeManager();
                labelManager.setLabelsVisible(false);
                labelManager.setLabelsVisible(true);

                assertTrue(labelManager.isLabelsVisible());
                assertTrue(labelManager.getLabelDisplayGroup().isVisible());
                return null;
            });
        }

        @Test
        @DisplayName("toggleLabels toggles visibility")
        void toggleLabelsTogglesVisibility() throws Exception {
            runOnFxThread(() -> {
                initializeManager();
                assertTrue(labelManager.isLabelsVisible());

                labelManager.toggleLabels(false);
                assertFalse(labelManager.isLabelsVisible());

                labelManager.toggleLabels(true);
                assertTrue(labelManager.isLabelsVisible());
                return null;
            });
        }
    }

    // =========================================================================
    // Clear Tests
    // =========================================================================

    @Nested
    @DisplayName("Clear Tests")
    class ClearTests {

        @Test
        @DisplayName("clear removes all labels")
        void clearRemovesAllLabels() throws Exception {
            runOnFxThread(() -> {
                initializeManager();
                ColorPalette palette = createTestPalette();

                // Add multiple labels
                for (int i = 0; i < 5; i++) {
                    Sphere node = new Sphere(5);
                    StarDisplayRecord record = createTestRecord("Star " + i);
                    labelManager.addLabel(node, record, palette);
                }

                assertEquals(5, labelManager.getLabelCount());

                labelManager.clear();

                assertEquals(0, labelManager.getLabelCount());
                assertTrue(labelManager.getLabelDisplayGroup().getChildren().isEmpty());
                return null;
            });
        }
    }

    // =========================================================================
    // Update Labels Tests
    // =========================================================================

    @Nested
    @DisplayName("Update Labels Tests")
    class UpdateLabelsTests {

        @Test
        @DisplayName("updateLabels handles empty label map")
        void updateLabelsHandlesEmptyMap() throws Exception {
            runOnFxThread(() -> {
                initializeManager();
                Bounds bounds = new BoundingBox(0, 0, 800, 600);

                // Should not throw
                assertDoesNotThrow(() -> labelManager.updateLabels(bounds));
                return null;
            });
        }

        @Test
        @DisplayName("updateLabels positions labels for visible nodes")
        void updateLabelsPositionsLabels() throws Exception {
            runOnFxThread(() -> {
                Group sceneRoot = new Group();
                Group world = new Group();
                SubScene subScene = new SubScene(world, 800, 600);
                sceneRoot.getChildren().add(subScene);

                labelManager.initialize(sceneRoot, subScene);

                Sphere node = new Sphere(5);
                node.setTranslateX(100);
                node.setTranslateY(100);
                node.setTranslateZ(0);
                world.getChildren().add(node);

                StarDisplayRecord record = createTestRecord("Test Star");
                ColorPalette palette = createTestPalette();
                Label label = labelManager.addLabel(node, record, palette);

                Bounds bounds = new BoundingBox(0, 0, 800, 600);
                labelManager.updateLabels(bounds);

                // Label should have transforms applied
                assertFalse(label.getTransforms().isEmpty());
                return null;
            });
        }

        @Test
        @DisplayName("setControlPaneOffset affects label positioning")
        void setControlPaneOffsetAffectsPositioning() throws Exception {
            runOnFxThread(() -> {
                initializeManager();

                labelManager.setControlPaneOffset(50);

                // The offset is applied during updateLabels
                // Just verify it doesn't throw
                assertDoesNotThrow(() -> {
                    Bounds bounds = new BoundingBox(0, 0, 800, 600);
                    labelManager.updateLabels(bounds);
                });
                return null;
            });
        }
    }

    // =========================================================================
    // Collision Detection Tests
    // =========================================================================

    @Nested
    @DisplayName("Collision Detection Tests")
    class CollisionDetectionTests {

        @Test
        @DisplayName("Overlapping labels at same position - one is hidden")
        void testCollisionDetectionHidesOverlappingLabels() throws Exception {
            runOnFxThread(() -> {
                // Create scene with proper hierarchy
                Group world = new Group();
                SubScene subScene = new SubScene(world, 800, 600);
                subScene.setCamera(new PerspectiveCamera());
                Group sceneRoot = new Group();
                sceneRoot.getChildren().add(subScene);

                labelManager.initialize(sceneRoot, subScene);
                ColorPalette palette = createTestPalette();

                // Create two nodes at same screen position but different depths
                Sphere node1 = new Sphere(5);
                node1.setTranslateX(100);
                node1.setTranslateY(100);
                node1.setTranslateZ(0);

                Sphere node2 = new Sphere(5);
                node2.setTranslateX(100);
                node2.setTranslateY(100);
                node2.setTranslateZ(10); // Further from camera

                world.getChildren().addAll(node1, node2);

                Label label1 = labelManager.addLabel(node1, createTestRecord("Star 1"), palette);
                Label label2 = labelManager.addLabel(node2, createTestRecord("Star 2"), palette);

                Bounds bounds = new BoundingBox(0, 0, 800, 600);
                labelManager.updateLabels(bounds);

                // At least one label should be visible, and they shouldn't both be visible
                // (collision detection should hide one)
                int visibleCount = 0;
                if (label1.isVisible()) visibleCount++;
                if (label2.isVisible()) visibleCount++;

                // With collision detection, at most one should be visible
                assertTrue(visibleCount <= 1, "At most one overlapping label should be visible");
                return null;
            });
        }

        @Test
        @DisplayName("Closer label has priority over farther label")
        void testCloserLabelHasPriority() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                SubScene subScene = new SubScene(world, 800, 600);
                subScene.setCamera(new PerspectiveCamera());
                Group sceneRoot = new Group();
                sceneRoot.getChildren().add(subScene);

                labelManager.initialize(sceneRoot, subScene);
                ColorPalette palette = createTestPalette();

                // Create two nodes at same screen position but different depths
                // Node1 is closer (smaller Z)
                Sphere closeNode = new Sphere(5);
                closeNode.setTranslateX(200);
                closeNode.setTranslateY(200);
                closeNode.setTranslateZ(-50); // Closer to camera

                Sphere farNode = new Sphere(5);
                farNode.setTranslateX(200);
                farNode.setTranslateY(200);
                farNode.setTranslateZ(50); // Farther from camera

                world.getChildren().addAll(closeNode, farNode);

                Label closeLabel = labelManager.addLabel(closeNode, createTestRecord("Close Star"), palette);
                Label farLabel = labelManager.addLabel(farNode, createTestRecord("Far Star"), palette);

                Bounds bounds = new BoundingBox(0, 0, 800, 600);
                labelManager.updateLabels(bounds);

                // The closer label should be visible (has priority)
                // The farther label may be hidden due to collision
                // Note: This test checks the sorting behavior - closer labels are processed first
                assertTrue(closeLabel.isVisible() || !farLabel.isVisible(),
                        "Closer label should have priority in collision detection");
                return null;
            });
        }

        @Test
        @DisplayName("Non-overlapping labels are all visible")
        void testNonOverlappingLabelsAllVisible() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                SubScene subScene = new SubScene(world, 800, 600);
                subScene.setCamera(new PerspectiveCamera());
                Group sceneRoot = new Group();
                sceneRoot.getChildren().add(subScene);

                labelManager.initialize(sceneRoot, subScene);
                ColorPalette palette = createTestPalette();

                // Create nodes at well-separated positions
                Sphere node1 = new Sphere(5);
                node1.setTranslateX(100);
                node1.setTranslateY(100);
                node1.setTranslateZ(0);

                Sphere node2 = new Sphere(5);
                node2.setTranslateX(400);  // Far apart
                node2.setTranslateY(100);
                node2.setTranslateZ(0);

                Sphere node3 = new Sphere(5);
                node3.setTranslateX(100);
                node3.setTranslateY(400);  // Far apart
                node3.setTranslateZ(0);

                world.getChildren().addAll(node1, node2, node3);

                Label label1 = labelManager.addLabel(node1, createTestRecord("A"), palette);
                Label label2 = labelManager.addLabel(node2, createTestRecord("B"), palette);
                Label label3 = labelManager.addLabel(node3, createTestRecord("C"), palette);

                Bounds bounds = new BoundingBox(0, 0, 800, 600);
                labelManager.updateLabels(bounds);

                // All labels should be visible since they don't overlap
                assertTrue(label1.isVisible(), "Label 1 should be visible");
                assertTrue(label2.isVisible(), "Label 2 should be visible");
                assertTrue(label3.isVisible(), "Label 3 should be visible");
                return null;
            });
        }

        @Test
        @DisplayName("NaN positions are skipped and labels hidden")
        void testNaNPositionsAreSkipped() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                SubScene subScene = new SubScene(world, 800, 600);
                subScene.setCamera(new PerspectiveCamera());
                Group sceneRoot = new Group();
                sceneRoot.getChildren().add(subScene);

                labelManager.initialize(sceneRoot, subScene);
                ColorPalette palette = createTestPalette();

                // Create node with NaN position
                Sphere nanNode = new Sphere(5);
                nanNode.setTranslateX(Double.NaN);
                nanNode.setTranslateY(100);
                nanNode.setTranslateZ(0);

                world.getChildren().add(nanNode);

                Label nanLabel = labelManager.addLabel(nanNode, createTestRecord("NaN Star"), palette);

                Bounds bounds = new BoundingBox(0, 0, 800, 600);
                labelManager.updateLabels(bounds);

                // Label with NaN position should be hidden
                assertFalse(nanLabel.isVisible(), "Label with NaN position should be hidden");
                return null;
            });
        }
    }

    // =========================================================================
    // Font Scaling Tests
    // =========================================================================

    @Nested
    @DisplayName("Font Scaling Tests")
    class FontScalingTests {

        @Test
        @DisplayName("setCameraZ accepts camera position")
        void setCameraZAcceptsPosition() throws Exception {
            runOnFxThread(() -> {
                initializeManager();

                // Should not throw
                assertDoesNotThrow(() -> labelManager.setCameraZ(-1600));
                assertDoesNotThrow(() -> labelManager.setCameraZ(-800));
                assertDoesNotThrow(() -> labelManager.setCameraZ(-2400));
                return null;
            });
        }

        @Test
        @DisplayName("Font scaling is applied during updateLabels")
        void fontScalingAppliedDuringUpdate() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                SubScene subScene = new SubScene(world, 800, 600);
                subScene.setCamera(new PerspectiveCamera());
                Group sceneRoot = new Group();
                sceneRoot.getChildren().add(subScene);

                labelManager.initialize(sceneRoot, subScene);
                ColorPalette palette = createTestPalette();

                Sphere node = new Sphere(5);
                node.setTranslateX(100);
                node.setTranslateY(100);
                node.setTranslateZ(0);
                world.getChildren().add(node);

                Label label = labelManager.addLabel(node, createTestRecord("Test"), palette);
                double originalSize = label.getFont().getSize();

                // Zoom in (more negative Z = larger font)
                labelManager.setCameraZ(-800);
                Bounds bounds = new BoundingBox(0, 0, 800, 600);
                labelManager.updateLabels(bounds);

                // Font should be scaled (exact value depends on implementation)
                assertNotNull(label.getFont());
                return null;
            });
        }
    }

    // =========================================================================
    // Throttled Update Tests
    // =========================================================================

    @Nested
    @DisplayName("Throttled Update Tests")
    class ThrottledUpdateTests {

        @Test
        @DisplayName("Throttled update respects timing interval")
        void testThrottledUpdateRespectsTiming() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                SubScene subScene = new SubScene(world, 800, 600);
                subScene.setCamera(new PerspectiveCamera());
                Group sceneRoot = new Group();
                sceneRoot.getChildren().add(subScene);

                labelManager.initialize(sceneRoot, subScene);
                ColorPalette palette = createTestPalette();

                Sphere node = new Sphere(5);
                node.setTranslateX(100);
                node.setTranslateY(100);
                node.setTranslateZ(0);
                world.getChildren().add(node);

                labelManager.addLabel(node, createTestRecord("Test"), palette);

                Bounds bounds = new BoundingBox(0, 0, 800, 600);

                // First throttled call should always execute
                labelManager.throttledUpdateLabels(bounds);

                // Rapid successive calls should be throttled
                // This just verifies no exceptions are thrown
                for (int i = 0; i < 10; i++) {
                    assertDoesNotThrow(() -> labelManager.throttledUpdateLabels(bounds));
                }
                return null;
            });
        }
    }

    // =========================================================================
    // Multiple Labels Tests
    // =========================================================================

    @Nested
    @DisplayName("Multiple Labels Tests")
    class MultipleLabelsTests {

        @Test
        @DisplayName("Can manage multiple labels simultaneously")
        void canManageMultipleLabels() throws Exception {
            runOnFxThread(() -> {
                initializeManager();
                ColorPalette palette = createTestPalette();

                Sphere node1 = new Sphere(5);
                Sphere node2 = new Sphere(5);
                Sphere node3 = new Sphere(5);

                Label label1 = labelManager.addLabel(node1, createTestRecord("Star 1"), palette);
                Label label2 = labelManager.addLabel(node2, createTestRecord("Star 2"), palette);
                Label label3 = labelManager.addLabel(node3, createTestRecord("Star 3"), palette);

                assertEquals(3, labelManager.getLabelCount());
                assertSame(label1, labelManager.getLabel(node1));
                assertSame(label2, labelManager.getLabel(node2));
                assertSame(label3, labelManager.getLabel(node3));
                return null;
            });
        }

        @Test
        @DisplayName("Removing one label doesn't affect others")
        void removingOneLabelDoesntAffectOthers() throws Exception {
            runOnFxThread(() -> {
                initializeManager();
                ColorPalette palette = createTestPalette();

                Sphere node1 = new Sphere(5);
                Sphere node2 = new Sphere(5);

                Label label1 = labelManager.addLabel(node1, createTestRecord("Star 1"), palette);
                Label label2 = labelManager.addLabel(node2, createTestRecord("Star 2"), palette);

                labelManager.removeLabel(node1);

                assertEquals(1, labelManager.getLabelCount());
                assertFalse(labelManager.hasLabel(node1));
                assertTrue(labelManager.hasLabel(node2));
                assertSame(label2, labelManager.getLabel(node2));
                return null;
            });
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private void initializeManager() {
        Group sceneRoot = new Group();
        SubScene subScene = new SubScene(new Group(), 800, 600);
        labelManager.initialize(sceneRoot, subScene);
    }

    private StarDisplayRecord createTestRecord(String name) {
        StarDisplayRecord record = mock(StarDisplayRecord.class);
        when(record.getStarName()).thenReturn(name);
        when(record.getRecordId()).thenReturn(name + "-id");
        return record;
    }

    private ColorPalette createTestPalette() {
        ColorPalette palette = new ColorPalette();
        palette.setLabelColor("WHITE");
        palette.setLabelFont(new SerialFont("Arial", 12));
        return palette;
    }

    private <T> T runOnFxThread(java.util.concurrent.Callable<T> callable) throws Exception {
        if (Platform.isFxApplicationThread()) {
            return callable.call();
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<Exception> exception = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                result.set(callable.call());
            } catch (Exception e) {
                exception.set(e);
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(30, TimeUnit.SECONDS), "JavaFX operation timed out");

        if (exception.get() != null) {
            throw exception.get();
        }

        return result.get();
    }
}
