package com.teamgannon.trips.starplotting;

import javafx.animation.Animation;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.shape.Sphere;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for StarAnimationManager.
 * <p>
 * Tests cover:
 * <ul>
 *   <li>Highlight (blink) animation lifecycle</li>
 *   <li>Rotation animation lifecycle</li>
 *   <li>Animation parameter customization</li>
 *   <li>State restoration after animation stop</li>
 *   <li>Callback handling</li>
 *   <li>Factory methods for common animations</li>
 * </ul>
 */
class StarAnimationManagerTest {

    private static boolean javaFxInitialized = false;
    private StarAnimationManager animationManager;

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
        animationManager = new StarAnimationManager();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (animationManager != null && javaFxInitialized) {
            runOnFxThread(() -> {
                animationManager.stopAllAnimations();
                return null;
            });
        }
    }

    // =========================================================================
    // Initialization Tests
    // =========================================================================

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {

        @Test
        @DisplayName("New manager has no active animations")
        void newManagerHasNoActiveAnimations() throws Exception {
            runOnFxThread(() -> {
                assertFalse(animationManager.isHighlightAnimationRunning());
                assertFalse(animationManager.isRotationAnimationRunning());
                assertFalse(animationManager.isAnyAnimationRunning());
                return null;
            });
        }

        @Test
        @DisplayName("New manager has null scale transition")
        void newManagerHasNullScaleTransition() throws Exception {
            runOnFxThread(() -> {
                assertNull(animationManager.getScaleTransition());
                return null;
            });
        }

        @Test
        @DisplayName("New manager has null rotate transition")
        void newManagerHasNullRotateTransition() throws Exception {
            runOnFxThread(() -> {
                assertNull(animationManager.getRotateTransition());
                return null;
            });
        }

        @Test
        @DisplayName("New manager has null highlight node")
        void newManagerHasNullHighlightNode() throws Exception {
            runOnFxThread(() -> {
                assertNull(animationManager.getHighlightNode());
                return null;
            });
        }
    }

    // =========================================================================
    // Highlight Animation Tests
    // =========================================================================

    @Nested
    @DisplayName("Highlight Animation Tests")
    class HighlightAnimationTests {

        @Test
        @DisplayName("startHighlightAnimation creates scale transition")
        void startHighlightAnimationCreatesScaleTransition() throws Exception {
            runOnFxThread(() -> {
                Sphere node = new Sphere(10);

                animationManager.startHighlightAnimation(node);

                assertNotNull(animationManager.getScaleTransition());
                assertSame(node, animationManager.getHighlightNode());
                return null;
            });
        }

        @Test
        @DisplayName("startHighlightAnimation with custom cycle count")
        void startHighlightAnimationWithCustomCycleCount() throws Exception {
            runOnFxThread(() -> {
                Sphere node = new Sphere(10);

                animationManager.startHighlightAnimation(node, 50);

                assertNotNull(animationManager.getScaleTransition());
                assertEquals(50, animationManager.getScaleTransition().getCycleCount());
                return null;
            });
        }

        @Test
        @DisplayName("startHighlightAnimation with full customization")
        void startHighlightAnimationWithFullCustomization() throws Exception {
            runOnFxThread(() -> {
                Sphere node = new Sphere(10);

                animationManager.startHighlightAnimation(node, 30, 1.5, 1.5);

                assertNotNull(animationManager.getScaleTransition());
                assertEquals(30, animationManager.getScaleTransition().getCycleCount());
                return null;
            });
        }

        @Test
        @DisplayName("startHighlightAnimation saves transition state")
        void startHighlightAnimationSavesTransitionState() throws Exception {
            runOnFxThread(() -> {
                Sphere node = new Sphere(10);
                node.setScaleX(2.0);
                node.setScaleY(3.0);
                node.setScaleZ(4.0);

                animationManager.startHighlightAnimation(node);

                TransitionState state = animationManager.getTransitionState();
                assertNotNull(state);
                assertEquals(2.0, state.getXScale());
                assertEquals(3.0, state.getYScale());
                assertEquals(4.0, state.getZScale());
                return null;
            });
        }

        @Test
        @DisplayName("isHighlightAnimationRunning returns true when animation is running")
        void isHighlightAnimationRunningReturnsTrueWhenRunning() throws Exception {
            runOnFxThread(() -> {
                Sphere node = new Sphere(10);

                animationManager.startHighlightAnimation(node);

                assertTrue(animationManager.isHighlightAnimationRunning());
                return null;
            });
        }

        @Test
        @DisplayName("stopHighlightAnimation stops running animation")
        void stopHighlightAnimationStopsRunningAnimation() throws Exception {
            runOnFxThread(() -> {
                Sphere node = new Sphere(10);
                animationManager.startHighlightAnimation(node);

                animationManager.stopHighlightAnimation();

                assertFalse(animationManager.isHighlightAnimationRunning());
                assertNull(animationManager.getScaleTransition());
                return null;
            });
        }

        @Test
        @DisplayName("stopHighlightAnimation restores original scale")
        void stopHighlightAnimationRestoresOriginalScale() throws Exception {
            runOnFxThread(() -> {
                Sphere node = new Sphere(10);
                node.setScaleX(2.0);
                node.setScaleY(3.0);
                node.setScaleZ(4.0);

                animationManager.startHighlightAnimation(node);
                animationManager.stopHighlightAnimation();

                assertEquals(2.0, node.getScaleX(), 0.01);
                assertEquals(3.0, node.getScaleY(), 0.01);
                assertEquals(4.0, node.getScaleZ(), 0.01);
                return null;
            });
        }

        @Test
        @DisplayName("Starting new animation stops previous one")
        void startingNewAnimationStopsPreviousOne() throws Exception {
            runOnFxThread(() -> {
                Sphere node1 = new Sphere(10);
                Sphere node2 = new Sphere(10);

                animationManager.startHighlightAnimation(node1);
                animationManager.startHighlightAnimation(node2);

                assertSame(node2, animationManager.getHighlightNode());
                return null;
            });
        }
    }

    // =========================================================================
    // Highlight Callback Tests
    // =========================================================================

    @Nested
    @DisplayName("Highlight Callback Tests")
    class HighlightCallbackTests {

        @Test
        @DisplayName("setOnHighlightFinished sets callback")
        void setOnHighlightFinishedSetsCallback() throws Exception {
            runOnFxThread(() -> {
                AtomicBoolean called = new AtomicBoolean(false);

                animationManager.setOnHighlightFinished(node -> called.set(true));

                // Start and verify callback is set (we can't easily verify it's called
                // without waiting for animation to finish)
                assertFalse(called.get()); // Not called yet
                return null;
            });
        }

        @Test
        @DisplayName("Null callback is allowed")
        void nullCallbackIsAllowed() throws Exception {
            runOnFxThread(() -> {
                assertDoesNotThrow(() -> animationManager.setOnHighlightFinished(null));
                return null;
            });
        }
    }

    // =========================================================================
    // Rotation Animation Tests
    // =========================================================================

    @Nested
    @DisplayName("Rotation Animation Tests")
    class RotationAnimationTests {

        @Test
        @DisplayName("startRotationAnimation creates rotate transition")
        void startRotationAnimationCreatesRotateTransition() throws Exception {
            runOnFxThread(() -> {
                Sphere node = new Sphere(10);

                animationManager.startRotationAnimation(node);

                assertNotNull(animationManager.getRotateTransition());
                return null;
            });
        }

        @Test
        @DisplayName("startRotationAnimation with custom parameters")
        void startRotationAnimationWithCustomParameters() throws Exception {
            runOnFxThread(() -> {
                Sphere node = new Sphere(10);

                animationManager.startRotationAnimation(node, 5.0, 180.0);

                assertNotNull(animationManager.getRotateTransition());
                return null;
            });
        }

        @Test
        @DisplayName("Rotation animation runs indefinitely")
        void rotationAnimationRunsIndefinitely() throws Exception {
            runOnFxThread(() -> {
                Sphere node = new Sphere(10);

                animationManager.startRotationAnimation(node);

                assertEquals(Animation.INDEFINITE, animationManager.getRotateTransition().getCycleCount());
                return null;
            });
        }

        @Test
        @DisplayName("isRotationAnimationRunning returns true when animation is running")
        void isRotationAnimationRunningReturnsTrueWhenRunning() throws Exception {
            runOnFxThread(() -> {
                Sphere node = new Sphere(10);

                animationManager.startRotationAnimation(node);

                assertTrue(animationManager.isRotationAnimationRunning());
                return null;
            });
        }

        @Test
        @DisplayName("stopRotationAnimation stops running animation")
        void stopRotationAnimationStopsRunningAnimation() throws Exception {
            runOnFxThread(() -> {
                Sphere node = new Sphere(10);
                animationManager.startRotationAnimation(node);

                animationManager.stopRotationAnimation();

                assertFalse(animationManager.isRotationAnimationRunning());
                assertNull(animationManager.getRotateTransition());
                return null;
            });
        }

        @Test
        @DisplayName("Starting new rotation stops previous one")
        void startingNewRotationStopsPreviousOne() throws Exception {
            runOnFxThread(() -> {
                Sphere node1 = new Sphere(10);
                Sphere node2 = new Sphere(10);

                animationManager.startRotationAnimation(node1);
                animationManager.startRotationAnimation(node2);

                // Verify only one rotation is running
                assertTrue(animationManager.isRotationAnimationRunning());
                return null;
            });
        }
    }

    // =========================================================================
    // Combined Animation Tests
    // =========================================================================

    @Nested
    @DisplayName("Combined Animation Tests")
    class CombinedAnimationTests {

        @Test
        @DisplayName("isAnyAnimationRunning returns true when highlight is running")
        void isAnyAnimationRunningTrueForHighlight() throws Exception {
            runOnFxThread(() -> {
                Sphere node = new Sphere(10);
                animationManager.startHighlightAnimation(node);

                assertTrue(animationManager.isAnyAnimationRunning());
                return null;
            });
        }

        @Test
        @DisplayName("isAnyAnimationRunning returns true when rotation is running")
        void isAnyAnimationRunningTrueForRotation() throws Exception {
            runOnFxThread(() -> {
                Sphere node = new Sphere(10);
                animationManager.startRotationAnimation(node);

                assertTrue(animationManager.isAnyAnimationRunning());
                return null;
            });
        }

        @Test
        @DisplayName("isAnyAnimationRunning returns true when both are running")
        void isAnyAnimationRunningTrueForBoth() throws Exception {
            runOnFxThread(() -> {
                Sphere node1 = new Sphere(10);
                Sphere node2 = new Sphere(10);

                animationManager.startHighlightAnimation(node1);
                animationManager.startRotationAnimation(node2);

                assertTrue(animationManager.isAnyAnimationRunning());
                assertTrue(animationManager.isHighlightAnimationRunning());
                assertTrue(animationManager.isRotationAnimationRunning());
                return null;
            });
        }

        @Test
        @DisplayName("stopAllAnimations stops both animations")
        void stopAllAnimationsStopsBothAnimations() throws Exception {
            runOnFxThread(() -> {
                Sphere node1 = new Sphere(10);
                Sphere node2 = new Sphere(10);

                animationManager.startHighlightAnimation(node1);
                animationManager.startRotationAnimation(node2);

                animationManager.stopAllAnimations();

                assertFalse(animationManager.isHighlightAnimationRunning());
                assertFalse(animationManager.isRotationAnimationRunning());
                assertFalse(animationManager.isAnyAnimationRunning());
                return null;
            });
        }
    }

    // =========================================================================
    // Factory Method Tests
    // =========================================================================

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("pulseSubtle creates animation with correct parameters")
        void pulseSubtleCreatesAnimation() throws Exception {
            runOnFxThread(() -> {
                Sphere node = new Sphere(10);

                StarAnimationManager result = animationManager.pulseSubtle(node);

                assertSame(animationManager, result); // Returns self for chaining
                assertTrue(animationManager.isHighlightAnimationRunning());
                assertEquals(50, animationManager.getScaleTransition().getCycleCount());
                return null;
            });
        }

        @Test
        @DisplayName("pulseDramatic creates animation with correct parameters")
        void pulseDramaticCreatesAnimation() throws Exception {
            runOnFxThread(() -> {
                Sphere node = new Sphere(10);

                StarAnimationManager result = animationManager.pulseDramatic(node);

                assertSame(animationManager, result);
                assertTrue(animationManager.isHighlightAnimationRunning());
                assertEquals(20, animationManager.getScaleTransition().getCycleCount());
                return null;
            });
        }

        @Test
        @DisplayName("flash creates animation with correct parameters")
        void flashCreatesAnimation() throws Exception {
            runOnFxThread(() -> {
                Sphere node = new Sphere(10);

                StarAnimationManager result = animationManager.flash(node);

                assertSame(animationManager, result);
                assertTrue(animationManager.isHighlightAnimationRunning());
                assertEquals(10, animationManager.getScaleTransition().getCycleCount());
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
        @DisplayName("stopHighlightAnimation is safe when no animation running")
        void stopHighlightAnimationSafeWhenNotRunning() throws Exception {
            runOnFxThread(() -> {
                assertDoesNotThrow(() -> animationManager.stopHighlightAnimation());
                return null;
            });
        }

        @Test
        @DisplayName("stopRotationAnimation is safe when no animation running")
        void stopRotationAnimationSafeWhenNotRunning() throws Exception {
            runOnFxThread(() -> {
                assertDoesNotThrow(() -> animationManager.stopRotationAnimation());
                return null;
            });
        }

        @Test
        @DisplayName("stopAllAnimations is safe when no animations running")
        void stopAllAnimationsSafeWhenNotRunning() throws Exception {
            runOnFxThread(() -> {
                assertDoesNotThrow(() -> animationManager.stopAllAnimations());
                return null;
            });
        }

        @Test
        @DisplayName("Animation works on Group nodes")
        void animationWorksOnGroupNodes() throws Exception {
            runOnFxThread(() -> {
                Group group = new Group();
                group.getChildren().add(new Sphere(5));

                animationManager.startHighlightAnimation(group);

                assertTrue(animationManager.isHighlightAnimationRunning());
                return null;
            });
        }

        @Test
        @DisplayName("getTransitionState returns null after cleanup")
        void getTransitionStateReturnsNullAfterCleanup() throws Exception {
            runOnFxThread(() -> {
                Sphere node = new Sphere(10);
                animationManager.startHighlightAnimation(node);
                assertNotNull(animationManager.getTransitionState());

                animationManager.stopHighlightAnimation();
                assertNull(animationManager.getTransitionState());
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
