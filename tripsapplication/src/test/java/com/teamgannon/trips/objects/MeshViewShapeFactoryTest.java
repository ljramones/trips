package com.teamgannon.trips.objects;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.shape.MeshView;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for MeshViewShapeFactory to verify that each call returns a unique instance.
 * <p>
 * This is critical because JavaFX Nodes can only belong to one parent at a time.
 * If the factory returned cached instances, adding the same node to multiple
 * parents would silently remove it from previous parents.
 * <p>
 * These tests require JavaFX to be initialized. They will be skipped in headless
 * environments where JavaFX cannot start.
 */
class MeshViewShapeFactoryTest {

    private static boolean javaFxInitialized = false;
    private MeshViewShapeFactory factory;

    @BeforeAll
    static void initJavaFx() {
        try {
            // Try to initialize JavaFX toolkit
            Platform.startup(() -> {});
            javaFxInitialized = true;
        } catch (IllegalStateException e) {
            // Already initialized
            javaFxInitialized = true;
        } catch (Exception e) {
            // JavaFX not available (headless environment)
            System.out.println("JavaFX not available, skipping MeshViewShapeFactory tests: " + e.getMessage());
            javaFxInitialized = false;
        }
    }

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(javaFxInitialized, "JavaFX not available");
        factory = new MeshViewShapeFactory();
    }

    // =========================================================================
    // Central Star Tests - Critical for the MeshView reuse bug fix
    // =========================================================================

    @Nested
    @DisplayName("Central Star Factory Tests")
    class CentralStarTests {

        @Test
        @DisplayName("starCentral() returns non-null Group")
        void starCentralReturnsNonNull() throws Exception {
            Group star = runOnFxThread(() -> factory.starCentral());
            assertNotNull(star, "starCentral() should return a non-null Group");
        }

        @Test
        @DisplayName("starCentral() returns distinct instances on each call")
        void starCentralReturnsDistinctInstances() throws Exception {
            Group star1 = runOnFxThread(() -> factory.starCentral());
            Group star2 = runOnFxThread(() -> factory.starCentral());
            Group star3 = runOnFxThread(() -> factory.starCentral());

            assertNotNull(star1);
            assertNotNull(star2);
            assertNotNull(star3);

            assertNotSame(star1, star2, "Each call should return a new instance");
            assertNotSame(star2, star3, "Each call should return a new instance");
            assertNotSame(star1, star3, "Each call should return a new instance");
        }

        @Test
        @DisplayName("Multiple central stars can have different parents")
        void multipleCentralStarsCanHaveDifferentParents() throws Exception {
            runOnFxThread(() -> {
                Group star1 = factory.starCentral();
                Group star2 = factory.starCentral();

                Group parent1 = new Group();
                Group parent2 = new Group();

                parent1.getChildren().add(star1);
                parent2.getChildren().add(star2);

                // Both stars should remain in their parents
                assertTrue(parent1.getChildren().contains(star1),
                        "star1 should remain in parent1");
                assertTrue(parent2.getChildren().contains(star2),
                        "star2 should remain in parent2");

                // Parents should have exactly one child each
                assertEquals(1, parent1.getChildren().size());
                assertEquals(1, parent2.getChildren().size());

                return null;
            });
        }
    }

    // =========================================================================
    // Moravian Star Tests (for highlight star)
    // =========================================================================

    @Nested
    @DisplayName("Moravian Star Factory Tests")
    class MoravianStarTests {

        @Test
        @DisplayName("starMoravian() returns non-null Group")
        void starMoravianReturnsNonNull() throws Exception {
            Group star = runOnFxThread(() -> factory.starMoravian());
            assertNotNull(star, "starMoravian() should return a non-null Group");
        }

        @Test
        @DisplayName("starMoravian() returns distinct instances on each call")
        void starMoravianReturnsDistinctInstances() throws Exception {
            Group star1 = runOnFxThread(() -> factory.starMoravian());
            Group star2 = runOnFxThread(() -> factory.starMoravian());

            assertNotNull(star1);
            assertNotNull(star2);
            assertNotSame(star1, star2, "Each call should return a new instance");
        }

        @Test
        @DisplayName("starMoravian() contains MeshView children")
        void starMoravianContainsMeshViews() throws Exception {
            Group star = runOnFxThread(() -> factory.starMoravian());
            assertNotNull(star);
            assertFalse(star.getChildren().isEmpty(), "Moravian star should have children");

            // All children should be MeshView instances
            for (var child : star.getChildren()) {
                assertTrue(child instanceof MeshView,
                        "All children should be MeshView instances");
            }
        }
    }

    // =========================================================================
    // Other Shape Tests
    // =========================================================================

    @Nested
    @DisplayName("Other Shape Factory Tests")
    class OtherShapeTests {

        @Test
        @DisplayName("star4pt() returns distinct instances")
        void star4ptReturnsDistinctInstances() throws Exception {
            MeshView star1 = runOnFxThread(() -> factory.star4pt());
            MeshView star2 = runOnFxThread(() -> factory.star4pt());

            assertNotNull(star1);
            assertNotNull(star2);
            assertNotSame(star1, star2);
        }

        @Test
        @DisplayName("star5pt() returns distinct instances")
        void star5ptReturnsDistinctInstances() throws Exception {
            Group star1 = runOnFxThread(() -> factory.star5pt());
            Group star2 = runOnFxThread(() -> factory.star5pt());

            assertNotNull(star1);
            assertNotNull(star2);
            assertNotSame(star1, star2);
        }

        @Test
        @DisplayName("cube() returns distinct instances")
        void cubeReturnsDistinctInstances() throws Exception {
            MeshView mesh1 = runOnFxThread(() -> factory.cube());
            MeshView mesh2 = runOnFxThread(() -> factory.cube());

            assertNotNull(mesh1);
            assertNotNull(mesh2);
            assertNotSame(mesh1, mesh2);
        }

        @Test
        @DisplayName("tetrahedron() returns distinct instances")
        void tetrahedronReturnsDistinctInstances() throws Exception {
            MeshView mesh1 = runOnFxThread(() -> factory.tetrahedron());
            MeshView mesh2 = runOnFxThread(() -> factory.tetrahedron());

            assertNotNull(mesh1);
            assertNotNull(mesh2);
            assertNotSame(mesh1, mesh2);
        }

        @Test
        @DisplayName("pyramid() returns distinct instances")
        void pyramidReturnsDistinctInstances() throws Exception {
            MeshView mesh1 = runOnFxThread(() -> factory.pyramid());
            MeshView mesh2 = runOnFxThread(() -> factory.pyramid());

            assertNotNull(mesh1);
            assertNotNull(mesh2);
            assertNotSame(mesh1, mesh2);
        }

        @Test
        @DisplayName("octahedron() returns distinct instances")
        void octahedronReturnsDistinctInstances() throws Exception {
            MeshView mesh1 = runOnFxThread(() -> factory.octahedron());
            MeshView mesh2 = runOnFxThread(() -> factory.octahedron());

            assertNotNull(mesh1);
            assertNotNull(mesh2);
            assertNotSame(mesh1, mesh2);
        }

        @Test
        @DisplayName("icosahedron() returns distinct instances")
        void icosahedronReturnsDistinctInstances() throws Exception {
            MeshView mesh1 = runOnFxThread(() -> factory.icosahedron());
            MeshView mesh2 = runOnFxThread(() -> factory.icosahedron());

            assertNotNull(mesh1);
            assertNotNull(mesh2);
            assertNotSame(mesh1, mesh2);
        }

        @Test
        @DisplayName("dodecahedron() returns distinct instances")
        void dodecahedronReturnsDistinctInstances() throws Exception {
            MeshView mesh1 = runOnFxThread(() -> factory.dodecahedron());
            MeshView mesh2 = runOnFxThread(() -> factory.dodecahedron());

            assertNotNull(mesh1);
            assertNotNull(mesh2);
            assertNotSame(mesh1, mesh2);
        }
    }

    // =========================================================================
    // Geometric Shape Tests
    // =========================================================================

    @Nested
    @DisplayName("Geometric Shape Factory Tests")
    class GeometricShapeTests {

        @Test
        @DisplayName("geometric0() returns distinct instances")
        void geometric0ReturnsDistinctInstances() throws Exception {
            MeshView mesh1 = runOnFxThread(() -> factory.geometric0());
            MeshView mesh2 = runOnFxThread(() -> factory.geometric0());

            assertNotNull(mesh1);
            assertNotNull(mesh2);
            assertNotSame(mesh1, mesh2);
        }

        @Test
        @DisplayName("All geometric shapes return distinct instances")
        void allGeometricShapesReturnDistinctInstances() throws Exception {
            // Test geometric0 through geometric8
            MeshView[] meshes1 = runOnFxThread(() -> new MeshView[] {
                    factory.geometric0(),
                    factory.geometric1(),
                    factory.geometric2(),
                    factory.geometric3(),
                    factory.geometric4(),
                    factory.geometric5(),
                    factory.geometric6(),
                    factory.geometric7(),
                    factory.geometric8()
            });

            MeshView[] meshes2 = runOnFxThread(() -> new MeshView[] {
                    factory.geometric0(),
                    factory.geometric1(),
                    factory.geometric2(),
                    factory.geometric3(),
                    factory.geometric4(),
                    factory.geometric5(),
                    factory.geometric6(),
                    factory.geometric7(),
                    factory.geometric8()
            });

            for (int i = 0; i < meshes1.length; i++) {
                assertNotNull(meshes1[i], "geometric" + i + "() should not return null");
                assertNotNull(meshes2[i], "geometric" + i + "() should not return null");
                assertNotSame(meshes1[i], meshes2[i],
                        "geometric" + i + "() should return distinct instances");
            }
        }
    }

    // =========================================================================
    // Scene Graph Constraint Tests - The core of the bug fix validation
    // =========================================================================

    @Nested
    @DisplayName("Scene Graph Constraint Tests")
    class SceneGraphConstraintTests {

        @Test
        @DisplayName("Adding same node to two parents moves it (demonstrates the bug)")
        void addingSameNodeToTwoParentsMoves() throws Exception {
            runOnFxThread(() -> {
                // This test demonstrates WHY we need unique instances
                Group sharedNode = new Group();

                Group parent1 = new Group();
                Group parent2 = new Group();

                parent1.getChildren().add(sharedNode);
                assertEquals(1, parent1.getChildren().size(), "parent1 should have the node");

                // Adding to parent2 removes from parent1!
                parent2.getChildren().add(sharedNode);
                assertEquals(0, parent1.getChildren().size(),
                        "Adding to parent2 should remove from parent1");
                assertEquals(1, parent2.getChildren().size(),
                        "parent2 should now have the node");

                return null;
            });
        }

        @Test
        @DisplayName("Factory-created nodes don't interfere with each other")
        void factoryNodesAreIndependent() throws Exception {
            runOnFxThread(() -> {
                // Create multiple stars
                Group star1 = factory.starCentral();
                Group star2 = factory.starCentral();
                Group star3 = factory.starCentral();

                // Add them all to different parents
                Group world1 = new Group();
                Group world2 = new Group();
                Group world3 = new Group();

                world1.getChildren().add(star1);
                world2.getChildren().add(star2);
                world3.getChildren().add(star3);

                // All should remain in their parents
                assertEquals(1, world1.getChildren().size());
                assertEquals(1, world2.getChildren().size());
                assertEquals(1, world3.getChildren().size());

                assertTrue(world1.getChildren().contains(star1));
                assertTrue(world2.getChildren().contains(star2));
                assertTrue(world3.getChildren().contains(star3));

                return null;
            });
        }

        @Test
        @DisplayName("Can display multiple central stars simultaneously")
        void canDisplayMultipleCentralStarsSimultaneously() throws Exception {
            runOnFxThread(() -> {
                // Simulate what StarPlotManager does - create multiple central stars
                Group solarSystemWorld = new Group();

                // Add 5 central stars (like navigating between systems)
                for (int i = 0; i < 5; i++) {
                    Group star = factory.starCentral();
                    assertNotNull(star, "Star " + i + " should be created");
                    star.setTranslateX(i * 100); // Offset for visibility
                    solarSystemWorld.getChildren().add(star);
                }

                assertEquals(5, solarSystemWorld.getChildren().size(),
                        "All 5 stars should be present in the scene");

                return null;
            });
        }
    }

    // =========================================================================
    // Utility Methods
    // =========================================================================

    /**
     * Run a task on the JavaFX Application Thread and wait for completion.
     * Returns the result of the callable.
     */
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

        assertTrue(latch.await(10, TimeUnit.SECONDS), "JavaFX operation timed out");

        if (exception.get() != null) {
            throw exception.get();
        }

        return result.get();
    }
}
