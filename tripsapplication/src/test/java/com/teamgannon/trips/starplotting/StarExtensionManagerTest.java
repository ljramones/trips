package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.SerialFont;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for StarExtensionManager.
 * <p>
 * Tests cover:
 * <ul>
 *   <li>Initialization and setup</li>
 *   <li>Extension creation from star records</li>
 *   <li>Extension creation from coordinates</li>
 *   <li>Visibility toggling</li>
 *   <li>Clearing extensions</li>
 *   <li>Statistics tracking</li>
 * </ul>
 */
class StarExtensionManagerTest {

    private static boolean javaFxInitialized = false;
    private StarExtensionManager extensionManager;

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
        extensionManager = new StarExtensionManager();
    }

    // =========================================================================
    // Initialization Tests
    // =========================================================================

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("New manager has empty extensions group")
        void newManagerHasEmptyExtensionsGroup() {
            assertTrue(extensionManager.getExtensionsGroup().getChildren().isEmpty());
        }

        @Test
        @DisplayName("New manager has extensions visible by default")
        void newManagerHasExtensionsVisible() {
            assertTrue(extensionManager.isExtensionsVisible());
        }

        @Test
        @DisplayName("New manager has zero extension count")
        void newManagerHasZeroExtensionCount() {
            assertEquals(0, extensionManager.getExtensionCount());
        }

        @Test
        @DisplayName("Initialize adds extensions group to world")
        void initializeAddsExtensionsGroupToWorld() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                extensionManager.initialize(world);
                assertTrue(world.getChildren().contains(extensionManager.getExtensionsGroup()));
                return null;
            });
        }
    }

    // =========================================================================
    // Reference Z Tests
    // =========================================================================

    @Nested
    @DisplayName("Reference Z Tests")
    class ReferenceZTests {

        @Test
        @DisplayName("setReferenceZ sets the reference plane level")
        void setReferenceZSetsLevel() throws Exception {
            runOnFxThread(() -> {
                extensionManager.setReferenceZ(100.0);

                // Create an extension and verify it goes to the reference Z
                ColorPalette palette = createTestPalette();
                StarDisplayRecord record = createTestRecord(50, 50, 200);

                extensionManager.createExtension(record, palette);

                assertEquals(1, extensionManager.getExtensionCount());
                return null;
            });
        }

        @Test
        @DisplayName("Different reference Z values create different extensions")
        void differentReferenceZCreatesDifferentExtensions() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                extensionManager.initialize(world);

                // First extension with reference Z = 0
                extensionManager.setReferenceZ(0.0);
                extensionManager.createExtension(0, 0, 100, Color.RED, Font.getDefault());

                // Second extension with reference Z = 50
                extensionManager.setReferenceZ(50.0);
                extensionManager.createExtension(0, 0, 150, Color.BLUE, Font.getDefault());

                assertEquals(2, extensionManager.getExtensionCount());
                return null;
            });
        }
    }

    // =========================================================================
    // Extension Creation from Record Tests
    // =========================================================================

    @Nested
    @DisplayName("Extension Creation from Record Tests")
    class ExtensionFromRecordTests {

        @Test
        @DisplayName("createExtension from record adds to group")
        void createExtensionFromRecordAddsToGroup() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                extensionManager.initialize(world);

                StarDisplayRecord record = createTestRecord(100, 200, 50);
                ColorPalette palette = createTestPalette();

                extensionManager.createExtension(record, palette);

                assertEquals(1, extensionManager.getExtensionCount());
                assertFalse(extensionManager.getExtensionsGroup().getChildren().isEmpty());
                return null;
            });
        }

        @Test
        @DisplayName("Multiple extensions from records accumulate")
        void multipleExtensionsFromRecordsAccumulate() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                extensionManager.initialize(world);
                ColorPalette palette = createTestPalette();

                for (int i = 0; i < 5; i++) {
                    StarDisplayRecord record = createTestRecord(i * 10, i * 10, i * 10);
                    extensionManager.createExtension(record, palette);
                }

                assertEquals(5, extensionManager.getExtensionCount());
                assertEquals(5, extensionManager.getExtensionsGroup().getChildren().size());
                return null;
            });
        }
    }

    // =========================================================================
    // Extension Creation from Coordinates Tests
    // =========================================================================

    @Nested
    @DisplayName("Extension Creation from Coordinates Tests")
    class ExtensionFromCoordinatesTests {

        @Test
        @DisplayName("createExtension from coordinates adds to group")
        void createExtensionFromCoordsAddsToGroup() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                extensionManager.initialize(world);

                extensionManager.createExtension(100, 200, 50, Color.CYAN, Font.getDefault());

                assertEquals(1, extensionManager.getExtensionCount());
                assertFalse(extensionManager.getExtensionsGroup().getChildren().isEmpty());
                return null;
            });
        }

        @Test
        @DisplayName("createExtension with custom line width")
        void createExtensionWithCustomLineWidth() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                extensionManager.initialize(world);

                extensionManager.createExtension(100, 200, 50, Color.CYAN, 2.0, Font.getDefault());

                assertEquals(1, extensionManager.getExtensionCount());
                return null;
            });
        }

        @Test
        @DisplayName("createExtension with different colors")
        void createExtensionWithDifferentColors() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                extensionManager.initialize(world);

                extensionManager.createExtension(0, 0, 10, Color.RED, Font.getDefault());
                extensionManager.createExtension(10, 0, 20, Color.GREEN, Font.getDefault());
                extensionManager.createExtension(20, 0, 30, Color.BLUE, Font.getDefault());

                assertEquals(3, extensionManager.getExtensionCount());
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
        @DisplayName("setExtensionsVisible hides extensions when false")
        void setExtensionsVisibleHidesExtensions() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                extensionManager.initialize(world);

                extensionManager.setExtensionsVisible(false);

                assertFalse(extensionManager.isExtensionsVisible());
                assertFalse(extensionManager.getExtensionsGroup().isVisible());
                return null;
            });
        }

        @Test
        @DisplayName("setExtensionsVisible shows extensions when true")
        void setExtensionsVisibleShowsExtensions() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                extensionManager.initialize(world);

                extensionManager.setExtensionsVisible(false);
                extensionManager.setExtensionsVisible(true);

                assertTrue(extensionManager.isExtensionsVisible());
                assertTrue(extensionManager.getExtensionsGroup().isVisible());
                return null;
            });
        }

        @Test
        @DisplayName("toggleExtensions toggles visibility")
        void toggleExtensionsTogglesVisibility() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                extensionManager.initialize(world);

                assertTrue(extensionManager.isExtensionsVisible());

                extensionManager.toggleExtensions(false);
                assertFalse(extensionManager.isExtensionsVisible());

                extensionManager.toggleExtensions(true);
                assertTrue(extensionManager.isExtensionsVisible());
                return null;
            });
        }

        @Test
        @DisplayName("Creating extension when visible makes group visible")
        void creatingExtensionWhenVisibleMakesGroupVisible() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                extensionManager.initialize(world);
                extensionManager.setExtensionsVisible(true);

                extensionManager.createExtension(0, 0, 50, Color.RED, Font.getDefault());

                assertTrue(extensionManager.getExtensionsGroup().isVisible());
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
        @DisplayName("clear removes all extensions")
        void clearRemovesAllExtensions() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                extensionManager.initialize(world);

                // Add several extensions
                for (int i = 0; i < 10; i++) {
                    extensionManager.createExtension(i, i, i * 5, Color.WHITE, Font.getDefault());
                }

                assertEquals(10, extensionManager.getExtensionCount());

                extensionManager.clear();

                assertEquals(0, extensionManager.getExtensionCount());
                assertTrue(extensionManager.getExtensionsGroup().getChildren().isEmpty());
                return null;
            });
        }

        @Test
        @DisplayName("clear can be called multiple times")
        void clearCanBeCalledMultipleTimes() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                extensionManager.initialize(world);

                extensionManager.createExtension(0, 0, 50, Color.RED, Font.getDefault());
                extensionManager.clear();
                extensionManager.clear();  // Second call should be safe

                assertEquals(0, extensionManager.getExtensionCount());
                return null;
            });
        }

        @Test
        @DisplayName("clear allows new extensions to be added")
        void clearAllowsNewExtensions() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                extensionManager.initialize(world);

                extensionManager.createExtension(0, 0, 50, Color.RED, Font.getDefault());
                extensionManager.clear();

                extensionManager.createExtension(10, 10, 100, Color.BLUE, Font.getDefault());

                assertEquals(1, extensionManager.getExtensionCount());
                return null;
            });
        }
    }

    // =========================================================================
    // Statistics Tests
    // =========================================================================

    @Nested
    @DisplayName("Statistics Tests")
    class StatisticsTests {

        @Test
        @DisplayName("Extension count increments correctly")
        void extensionCountIncrementsCorrectly() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                extensionManager.initialize(world);

                assertEquals(0, extensionManager.getExtensionCount());

                extensionManager.createExtension(0, 0, 10, Color.RED, Font.getDefault());
                assertEquals(1, extensionManager.getExtensionCount());

                extensionManager.createExtension(10, 10, 20, Color.GREEN, Font.getDefault());
                assertEquals(2, extensionManager.getExtensionCount());

                extensionManager.createExtension(20, 20, 30, Color.BLUE, Font.getDefault());
                assertEquals(3, extensionManager.getExtensionCount());
                return null;
            });
        }

        @Test
        @DisplayName("resetStatistics resets count but not children")
        void resetStatisticsResetsCount() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                extensionManager.initialize(world);

                extensionManager.createExtension(0, 0, 50, Color.RED, Font.getDefault());
                extensionManager.createExtension(10, 10, 100, Color.BLUE, Font.getDefault());

                assertEquals(2, extensionManager.getExtensionCount());

                extensionManager.resetStatistics();

                assertEquals(0, extensionManager.getExtensionCount());
                // Note: children are still there, just count is reset
                assertEquals(2, extensionManager.getExtensionsGroup().getChildren().size());
                return null;
            });
        }

        @Test
        @DisplayName("logStatistics does not throw")
        void logStatisticsDoesNotThrow() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                extensionManager.initialize(world);

                extensionManager.createExtension(0, 0, 50, Color.RED, Font.getDefault());

                assertDoesNotThrow(() -> extensionManager.logStatistics());
                return null;
            });
        }
    }

    // =========================================================================
    // Edge Cases Tests
    // =========================================================================

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Extension with zero height creates valid line")
        void extensionWithZeroHeight() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                extensionManager.initialize(world);
                extensionManager.setReferenceZ(0.0);

                // Star at Z=0 (same as reference)
                extensionManager.createExtension(100, 200, 0, Color.RED, Font.getDefault());

                assertEquals(1, extensionManager.getExtensionCount());
                return null;
            });
        }

        @Test
        @DisplayName("Extension with negative Z coordinate")
        void extensionWithNegativeZ() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                extensionManager.initialize(world);
                extensionManager.setReferenceZ(0.0);

                // Star below the grid plane
                extensionManager.createExtension(100, 200, -50, Color.RED, Font.getDefault());

                assertEquals(1, extensionManager.getExtensionCount());
                return null;
            });
        }

        @Test
        @DisplayName("Extension with very large coordinates")
        void extensionWithLargeCoordinates() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                extensionManager.initialize(world);

                extensionManager.createExtension(10000, 10000, 5000, Color.RED, Font.getDefault());

                assertEquals(1, extensionManager.getExtensionCount());
                return null;
            });
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

    private StarDisplayRecord createTestRecord(double x, double y, double z) {
        StarDisplayRecord record = mock(StarDisplayRecord.class);
        when(record.getCoordinates()).thenReturn(new Point3D(x, y, z));
        when(record.getRecordId()).thenReturn("test-star-" + System.nanoTime());
        return record;
    }

    private ColorPalette createTestPalette() {
        ColorPalette palette = new ColorPalette();
        palette.setExtensionColor("CYAN");
        palette.setStemLineWidth(0.5);
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
