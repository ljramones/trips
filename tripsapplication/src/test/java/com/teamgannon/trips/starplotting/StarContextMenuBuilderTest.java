package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.shape.Sphere;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for StarContextMenuBuilder.
 * <p>
 * Tests cover:
 * <ul>
 *   <li>Builder initialization</li>
 *   <li>Title section creation</li>
 *   <li>Action item creation with callbacks</li>
 *   <li>Section headers and separators</li>
 *   <li>Custom actions</li>
 *   <li>Builder chaining</li>
 * </ul>
 */
class StarContextMenuBuilderTest {

    private static boolean javaFxInitialized = false;
    private StarDisplayRecord mockRecord;
    private Sphere testNode;

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
    void setUp() throws Exception {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

        runOnFxThread(() -> {
            mockRecord = mock(StarDisplayRecord.class);
            when(mockRecord.getStarName()).thenReturn("Test Star");
            when(mockRecord.getPolity()).thenReturn("Terran");
            when(mockRecord.getRecordId()).thenReturn("test-id-123");
            when(mockRecord.getCoordinates()).thenReturn(new Point3D(10, 20, 30));

            testNode = new Sphere(5);
            return null;
        });
    }

    // =========================================================================
    // Initialization Tests
    // =========================================================================

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("Builder creates empty context menu initially")
        void builderCreatesEmptyContextMenuInitially() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                ContextMenu menu = builder.build();

                assertNotNull(menu);
                assertTrue(menu.getItems().isEmpty());
                return null;
            });
        }

        @Test
        @DisplayName("Builder stores star node reference")
        void builderStoresStarNodeReference() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);

                assertSame(testNode, builder.getStarNode());
                return null;
            });
        }

        @Test
        @DisplayName("Builder stores star record reference")
        void builderStoresStarRecordReference() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);

                assertSame(mockRecord, builder.getRecord());
                return null;
            });
        }
    }

    // =========================================================================
    // Title Section Tests
    // =========================================================================

    @Nested
    @DisplayName("Title Section Tests")
    class TitleSectionTests {

        @Test
        @DisplayName("withTitle adds title with star name and polity")
        void withTitleAddsFormattedTitle() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                builder.withTitle();
                ContextMenu menu = builder.build();

                assertEquals(2, menu.getItems().size()); // Title + separator
                MenuItem titleItem = menu.getItems().get(0);
                assertTrue(titleItem.getText().contains("Test Star"));
                assertTrue(titleItem.getText().contains("Terran"));
                assertTrue(titleItem.isDisable());
                return null;
            });
        }

        @Test
        @DisplayName("withTitle handles NA polity")
        void withTitleHandlesNAPolity() throws Exception {
            runOnFxThread(() -> {
                when(mockRecord.getPolity()).thenReturn("NA");

                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                builder.withTitle();
                ContextMenu menu = builder.build();

                MenuItem titleItem = menu.getItems().get(0);
                assertTrue(titleItem.getText().contains("Non-aligned"));
                return null;
            });
        }

        @Test
        @DisplayName("withTitle with custom text")
        void withTitleWithCustomText() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                builder.withTitle("Custom Title");
                ContextMenu menu = builder.build();

                MenuItem titleItem = menu.getItems().get(0);
                assertEquals("Custom Title", titleItem.getText());
                return null;
            });
        }
    }

    // =========================================================================
    // Action Item Tests
    // =========================================================================

    @Nested
    @DisplayName("Action Item Tests")
    class ActionItemTests {

        @Test
        @DisplayName("withHighlightAction adds menu item")
        void withHighlightActionAddsMenuItem() throws Exception {
            runOnFxThread(() -> {
                AtomicBoolean called = new AtomicBoolean(false);

                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                builder.withHighlightAction(r -> called.set(true));
                ContextMenu menu = builder.build();

                assertEquals(1, menu.getItems().size());
                MenuItem item = menu.getItems().get(0);
                assertEquals("Highlight star", item.getText());
                return null;
            });
        }

        @Test
        @DisplayName("withPropertiesAction adds menu item")
        void withPropertiesActionAddsMenuItem() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                builder.withPropertiesAction(r -> {});
                ContextMenu menu = builder.build();

                assertEquals(1, menu.getItems().size());
                MenuItem item = menu.getItems().get(0);
                assertEquals("Properties", item.getText());
                return null;
            });
        }

        @Test
        @DisplayName("withRecenterAction adds menu item")
        void withRecenterActionAddsMenuItem() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                builder.withRecenterAction(r -> {});
                ContextMenu menu = builder.build();

                assertEquals(1, menu.getItems().size());
                MenuItem item = menu.getItems().get(0);
                assertEquals("Recenter on this star", item.getText());
                return null;
            });
        }

        @Test
        @DisplayName("withEditAction adds menu item")
        void withEditActionAddsMenuItem() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                builder.withEditAction(r -> {});
                ContextMenu menu = builder.build();

                assertEquals(1, menu.getItems().size());
                MenuItem item = menu.getItems().get(0);
                assertEquals("Edit star", item.getText());
                return null;
            });
        }

        @Test
        @DisplayName("withDeleteAction adds menu item")
        void withDeleteActionAddsMenuItem() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                builder.withDeleteAction(r -> {});
                ContextMenu menu = builder.build();

                assertEquals(1, menu.getItems().size());
                MenuItem item = menu.getItems().get(0);
                assertEquals("Delete star", item.getText());
                return null;
            });
        }
    }

    // =========================================================================
    // Routing Section Tests
    // =========================================================================

    @Nested
    @DisplayName("Routing Section Tests")
    class RoutingSectionTests {

        @Test
        @DisplayName("withRoutingHeader adds header and separator")
        void withRoutingHeaderAddsHeaderAndSeparator() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                builder.withRoutingHeader();
                ContextMenu menu = builder.build();

                assertEquals(2, menu.getItems().size());
                assertTrue(menu.getItems().get(0) instanceof SeparatorMenuItem);
                MenuItem header = menu.getItems().get(1);
                assertEquals("Routing", header.getText());
                assertTrue(header.isDisable());
                return null;
            });
        }

        @Test
        @DisplayName("withAutomatedRoutingAction adds menu item")
        void withAutomatedRoutingActionAddsMenuItem() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                builder.withAutomatedRoutingAction(r -> {});
                ContextMenu menu = builder.build();

                assertEquals(1, menu.getItems().size());
                assertTrue(menu.getItems().get(0).getText().contains("route finder"));
                return null;
            });
        }

        @Test
        @DisplayName("withManualRoutingAction adds menu item")
        void withManualRoutingActionAddsMenuItem() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                builder.withManualRoutingAction(r -> {});
                ContextMenu menu = builder.build();

                assertEquals(1, menu.getItems().size());
                assertTrue(menu.getItems().get(0).getText().contains("clicking stars"));
                return null;
            });
        }

        @Test
        @DisplayName("withStartRouteAction adds menu item")
        void withStartRouteActionAddsMenuItem() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                builder.withStartRouteAction(r -> {});
                ContextMenu menu = builder.build();

                assertEquals(1, menu.getItems().size());
                assertEquals("Start Route", menu.getItems().get(0).getText());
                return null;
            });
        }

        @Test
        @DisplayName("withContinueRouteAction adds menu item")
        void withContinueRouteActionAddsMenuItem() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                builder.withContinueRouteAction(r -> {});
                ContextMenu menu = builder.build();

                assertEquals(1, menu.getItems().size());
                assertEquals("Continue Route", menu.getItems().get(0).getText());
                return null;
            });
        }

        @Test
        @DisplayName("withFinishRouteAction adds menu item")
        void withFinishRouteActionAddsMenuItem() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                builder.withFinishRouteAction(r -> {});
                ContextMenu menu = builder.build();

                assertEquals(1, menu.getItems().size());
                assertEquals("Finish Route", menu.getItems().get(0).getText());
                return null;
            });
        }
    }

    // =========================================================================
    // Solar System Section Tests
    // =========================================================================

    @Nested
    @DisplayName("Solar System Section Tests")
    class SolarSystemSectionTests {

        @Test
        @DisplayName("withEnterSystemAction adds separator and menu item")
        void withEnterSystemActionAddsMenuItem() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                builder.withEnterSystemAction(r -> {});
                ContextMenu menu = builder.build();

                assertEquals(2, menu.getItems().size());
                assertTrue(menu.getItems().get(0) instanceof SeparatorMenuItem);
                assertEquals("Enter System", menu.getItems().get(1).getText());
                return null;
            });
        }

        @Test
        @DisplayName("withGenerateSolarSystemAction adds menu item")
        void withGenerateSolarSystemActionAddsMenuItem() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                builder.withGenerateSolarSystemAction(r -> {});
                ContextMenu menu = builder.build();

                assertEquals(1, menu.getItems().size());
                assertTrue(menu.getItems().get(0).getText().contains("Generate"));
                return null;
            });
        }
    }

    // =========================================================================
    // Utility Method Tests
    // =========================================================================

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {

        @Test
        @DisplayName("withSeparator adds separator")
        void withSeparatorAddsSeparator() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                builder.withSeparator();
                ContextMenu menu = builder.build();

                assertEquals(1, menu.getItems().size());
                assertTrue(menu.getItems().get(0) instanceof SeparatorMenuItem);
                return null;
            });
        }

        @Test
        @DisplayName("withCustomAction adds custom menu item")
        void withCustomActionAddsMenuItem() throws Exception {
            runOnFxThread(() -> {
                AtomicReference<StarDisplayRecord> received = new AtomicReference<>();

                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                builder.withCustomAction("Custom Action", r -> received.set(r));
                ContextMenu menu = builder.build();

                assertEquals(1, menu.getItems().size());
                assertEquals("Custom Action", menu.getItems().get(0).getText());
                return null;
            });
        }

        @Test
        @DisplayName("withSectionHeader adds disabled header")
        void withSectionHeaderAddsHeader() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                builder.withSectionHeader("My Section");
                ContextMenu menu = builder.build();

                assertEquals(1, menu.getItems().size());
                MenuItem header = menu.getItems().get(0);
                assertEquals("My Section", header.getText());
                assertTrue(header.isDisable());
                return null;
            });
        }

        @Test
        @DisplayName("getItemCount returns correct count")
        void getItemCountReturnsCorrectCount() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                assertEquals(0, builder.getItemCount());

                builder.withHighlightAction(r -> {});
                assertEquals(1, builder.getItemCount());

                builder.withPropertiesAction(r -> {});
                assertEquals(2, builder.getItemCount());
                return null;
            });
        }
    }

    // =========================================================================
    // Builder Chaining Tests
    // =========================================================================

    @Nested
    @DisplayName("Builder Chaining Tests")
    class BuilderChainingTests {

        @Test
        @DisplayName("All builder methods return builder for chaining")
        void allMethodsReturnBuilder() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);

                assertSame(builder, builder.withTitle());
                assertSame(builder, builder.withHighlightAction(r -> {}));
                assertSame(builder, builder.withPropertiesAction(r -> {}));
                assertSame(builder, builder.withRecenterAction(r -> {}));
                assertSame(builder, builder.withEditAction(r -> {}));
                assertSame(builder, builder.withDeleteAction(r -> {}));
                assertSame(builder, builder.withRoutingHeader());
                assertSame(builder, builder.withSeparator());
                assertSame(builder, builder.withSectionHeader("Test"));
                assertSame(builder, builder.withCustomAction("Test", r -> {}));
                return null;
            });
        }

        @Test
        @DisplayName("Full menu can be built with chaining")
        void fullMenuCanBeBuiltWithChaining() throws Exception {
            runOnFxThread(() -> {
                ContextMenu menu = new StarContextMenuBuilder(testNode, mockRecord)
                        .withTitle()
                        .withHighlightAction(r -> {})
                        .withPropertiesAction(r -> {})
                        .withRecenterAction(r -> {})
                        .withEditAction(r -> {})
                        .withDeleteAction(r -> {})
                        .withRoutingHeader()
                        .withAutomatedRoutingAction(r -> {})
                        .withManualRoutingAction(r -> {})
                        .withDistanceReportAction(r -> {})
                        .withEnterSystemAction(r -> {})
                        .withGenerateSolarSystemAction(r -> {})
                        .build();

                assertNotNull(menu);
                assertTrue(menu.getItems().size() > 10);
                return null;
            });
        }
    }

    // =========================================================================
    // Distance Report Tests
    // =========================================================================

    @Nested
    @DisplayName("Distance Report Tests")
    class DistanceReportTests {

        @Test
        @DisplayName("withDistanceReportAction adds separator and menu item")
        void withDistanceReportActionAddsMenuItem() throws Exception {
            runOnFxThread(() -> {
                StarContextMenuBuilder builder = new StarContextMenuBuilder(testNode, mockRecord);
                builder.withDistanceReportAction(r -> {});
                ContextMenu menu = builder.build();

                assertEquals(2, menu.getItems().size());
                assertTrue(menu.getItems().get(0) instanceof SeparatorMenuItem);
                assertTrue(menu.getItems().get(1).getText().contains("distance report"));
                return null;
            });
        }
    }

    // =========================================================================
    // Helper Methods
    // =========================================================================

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
