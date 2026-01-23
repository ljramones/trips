package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.AppViewPreferences;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.routing.RouteManager;
import com.teamgannon.trips.routing.RouteFindingService;
import com.teamgannon.trips.service.SolarSystemService;
import com.teamgannon.trips.service.StarService;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.*;
import org.springframework.context.ApplicationEventPublisher;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for StarPlotManager mesh creation methods.
 * <p>
 * These tests verify that createCentralStar() and createHighlightStar()
 * create fresh Node instances each time they're called. This is critical
 * because JavaFX Nodes can only belong to one scene graph at a time.
 * <p>
 * Requires JavaFX to be initialized for FXML loading.
 */
class StarPlotManagerMeshTest {

    private static boolean javaFxInitialized = false;
    private StarPlotManager starPlotManager;

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
            javaFxInitialized = true;
        } catch (IllegalStateException e) {
            // Already initialized
            javaFxInitialized = true;
        } catch (Exception e) {
            System.out.println("JavaFX not available, skipping StarPlotManager mesh tests: " + e.getMessage());
            javaFxInitialized = false;
        }
    }

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");

        // Create mock dependencies
        TripsContext tripsContext = mock(TripsContext.class);
        AppViewPreferences appViewPreferences = mock(AppViewPreferences.class);
        ColorPalette colorPalette = new ColorPalette();

        when(tripsContext.getAppViewPreferences()).thenReturn(appViewPreferences);
        when(appViewPreferences.getColorPalette()).thenReturn(colorPalette);

        RouteManager routeManager = mock(RouteManager.class);
        RouteFindingService routeFindingService = mock(RouteFindingService.class);
        StarService starService = mock(StarService.class);
        SolarSystemService solarSystemService = mock(SolarSystemService.class);
        StarContextMenuHandler contextMenuHandler = mock(StarContextMenuHandler.class);
        ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

        starPlotManager = new StarPlotManager(
                tripsContext,
                routeManager,
                starService,
                solarSystemService,
                routeFindingService,
                contextMenuHandler,
                eventPublisher
        );
    }

    // =========================================================================
    // Central Star Tests
    // =========================================================================

    @Nested
    @DisplayName("createCentralStar() Tests")
    class CreateCentralStarTests {

        @Test
        @DisplayName("createCentralStar() returns non-null Node")
        void createCentralStarReturnsNonNull() throws Exception {
            Node star = runOnFxThread(() -> starPlotManager.getMeshManager().createCentralStar());
            assertNotNull(star, "createCentralStar() should return a non-null Node");
        }

        @Test
        @DisplayName("createCentralStar() returns distinct instances on each call")
        void createCentralStarReturnsDistinctInstances() throws Exception {
            Node star1 = runOnFxThread(() -> starPlotManager.getMeshManager().createCentralStar());
            Node star2 = runOnFxThread(() -> starPlotManager.getMeshManager().createCentralStar());
            Node star3 = runOnFxThread(() -> starPlotManager.getMeshManager().createCentralStar());

            assertNotNull(star1);
            assertNotNull(star2);
            assertNotNull(star3);

            assertNotSame(star1, star2, "Each call should return a new instance");
            assertNotSame(star2, star3, "Each call should return a new instance");
            assertNotSame(star1, star3, "Each call should return a new instance");
        }

        @Test
        @DisplayName("Multiple central stars can coexist in scene graph")
        void multipleCentralStarsCanCoexist() throws Exception {
            runOnFxThread(() -> {
                Node star1 = starPlotManager.getMeshManager().createCentralStar();
                Node star2 = starPlotManager.getMeshManager().createCentralStar();

                Group parent1 = new Group();
                Group parent2 = new Group();

                parent1.getChildren().add(star1);
                parent2.getChildren().add(star2);

                // Both should remain in their respective parents
                assertTrue(parent1.getChildren().contains(star1),
                        "star1 should remain in parent1");
                assertTrue(parent2.getChildren().contains(star2),
                        "star2 should remain in parent2");
                assertEquals(1, parent1.getChildren().size());
                assertEquals(1, parent2.getChildren().size());

                return null;
            });
        }

        @Test
        @DisplayName("Central star has correct scaling applied")
        void centralStarHasScaling() throws Exception {
            Node star = runOnFxThread(() -> starPlotManager.getMeshManager().createCentralStar());

            // Default scaling should be applied (30 for all axes)
            // Note: actual value depends on whether specialObjects is populated
            assertTrue(star.getScaleX() > 0, "Star should have positive X scale");
            assertTrue(star.getScaleY() > 0, "Star should have positive Y scale");
            assertTrue(star.getScaleZ() > 0, "Star should have positive Z scale");
        }
    }

    // =========================================================================
    // Highlight Star Tests
    // =========================================================================

    @Nested
    @DisplayName("createHighlightStar() Tests")
    class CreateHighlightStarTests {

        @Test
        @DisplayName("createHighlightStar() returns non-null Node")
        void createHighlightStarReturnsNonNull() throws Exception {
            Node star = runOnFxThread(() -> starPlotManager.getMeshManager().createHighlightStar(Color.RED));
            assertNotNull(star, "createHighlightStar() should return a non-null Node");
        }

        @Test
        @DisplayName("createHighlightStar() returns distinct instances on each call")
        void createHighlightStarReturnsDistinctInstances() throws Exception {
            Node star1 = runOnFxThread(() -> starPlotManager.getMeshManager().createHighlightStar(Color.RED));
            Node star2 = runOnFxThread(() -> starPlotManager.getMeshManager().createHighlightStar(Color.BLUE));
            Node star3 = runOnFxThread(() -> starPlotManager.getMeshManager().createHighlightStar(Color.GREEN));

            assertNotNull(star1);
            assertNotNull(star2);
            assertNotNull(star3);

            assertNotSame(star1, star2, "Each call should return a new instance");
            assertNotSame(star2, star3, "Each call should return a new instance");
            assertNotSame(star1, star3, "Each call should return a new instance");
        }

        @Test
        @DisplayName("createHighlightStar() with same color returns distinct instances")
        void createHighlightStarSameColorReturnsDistinctInstances() throws Exception {
            Color color = Color.YELLOW;
            Node star1 = runOnFxThread(() -> starPlotManager.getMeshManager().createHighlightStar(color));
            Node star2 = runOnFxThread(() -> starPlotManager.getMeshManager().createHighlightStar(color));

            assertNotNull(star1);
            assertNotNull(star2);
            assertNotSame(star1, star2, "Same color should still return distinct instances");
        }

        @Test
        @DisplayName("Multiple highlight stars can coexist in scene graph")
        void multipleHighlightStarsCanCoexist() throws Exception {
            runOnFxThread(() -> {
                Node star1 = starPlotManager.getMeshManager().createHighlightStar(Color.RED);
                Node star2 = starPlotManager.getMeshManager().createHighlightStar(Color.BLUE);

                Group parent1 = new Group();
                Group parent2 = new Group();

                parent1.getChildren().add(star1);
                parent2.getChildren().add(star2);

                assertTrue(parent1.getChildren().contains(star1));
                assertTrue(parent2.getChildren().contains(star2));
                assertEquals(1, parent1.getChildren().size());
                assertEquals(1, parent2.getChildren().size());

                return null;
            });
        }
    }

    // =========================================================================
    // Combined Tests - Simulating Real Usage
    // =========================================================================

    @Nested
    @DisplayName("Real-World Scenario Tests")
    class RealWorldScenarioTests {

        @Test
        @DisplayName("Simulating multiple star system navigation")
        void simulateMultipleStarSystemNavigation() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();

                // Simulate navigating between 5 star systems
                // Each navigation creates a new central star
                for (int i = 0; i < 5; i++) {
                    Node centralStar = starPlotManager.getMeshManager().createCentralStar();
                    assertNotNull(centralStar, "Navigation " + i + " should create a star");

                    // In real code, previous content would be cleared
                    // Here we just add all to verify they're independent
                    centralStar.setTranslateX(i * 100);
                    world.getChildren().add(centralStar);
                }

                assertEquals(5, world.getChildren().size(),
                        "All 5 central stars should exist independently");

                return null;
            });
        }

        @Test
        @DisplayName("Simulating selecting multiple highlighted stars")
        void simulateMultipleHighlightedStars() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();
                Color[] colors = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE};

                // Simulate highlighting 5 different stars
                for (int i = 0; i < colors.length; i++) {
                    Node highlightStar = starPlotManager.getMeshManager().createHighlightStar(colors[i]);
                    assertNotNull(highlightStar, "Highlight " + i + " should create a star");

                    highlightStar.setTranslateX(i * 50);
                    highlightStar.setTranslateY(i * 50);
                    world.getChildren().add(highlightStar);
                }

                assertEquals(5, world.getChildren().size(),
                        "All 5 highlight stars should exist independently");

                return null;
            });
        }

        @Test
        @DisplayName("Mixed central and highlight stars in same scene")
        void mixedCentralAndHighlightStars() throws Exception {
            runOnFxThread(() -> {
                Group world = new Group();

                // Add central stars
                Node central1 = starPlotManager.getMeshManager().createCentralStar();
                Node central2 = starPlotManager.getMeshManager().createCentralStar();

                // Add highlight stars
                Node highlight1 = starPlotManager.getMeshManager().createHighlightStar(Color.CYAN);
                Node highlight2 = starPlotManager.getMeshManager().createHighlightStar(Color.MAGENTA);

                world.getChildren().addAll(central1, central2, highlight1, highlight2);

                assertEquals(4, world.getChildren().size(),
                        "All 4 stars should coexist");

                // Verify all are distinct
                assertNotSame(central1, central2);
                assertNotSame(highlight1, highlight2);
                assertNotSame(central1, highlight1);

                return null;
            });
        }
    }

    // =========================================================================
    // Utility Methods
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
