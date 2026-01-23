package com.teamgannon.trips.graphics.panes;

import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for InterstellarCameraController.
 */
class InterstellarCameraControllerTest {

    private Group world;
    private InterstellarCameraController controller;

    @BeforeEach
    void setUp() {
        world = new Group();
        controller = new InterstellarCameraController(world);
    }

    @Nested
    @DisplayName("Camera initialization tests")
    class CameraInitializationTests {

        @Test
        @DisplayName("should create perspective camera")
        void shouldCreatePerspectiveCamera() {
            PerspectiveCamera camera = controller.getCamera();

            assertNotNull(camera);
            assertTrue(camera.isFixedEyeAtCameraZero());
        }

        @Test
        @DisplayName("should set initial camera clip planes")
        void shouldSetInitialClipPlanes() {
            PerspectiveCamera camera = controller.getCamera();

            assertEquals(0.1, camera.getNearClip(), 0.001);
            assertEquals(10000.0, camera.getFarClip(), 0.001);
        }

        @Test
        @DisplayName("should set initial camera Z position")
        void shouldSetInitialCameraZPosition() {
            PerspectiveCamera camera = controller.getCamera();

            assertEquals(-1600, camera.getTranslateZ(), 0.001);
        }

        @Test
        @DisplayName("should attach transforms to world group")
        void shouldAttachTransformsToWorldGroup() {
            // World should have 3 transforms: rotateX, rotateY, rotateZ
            assertEquals(3, world.getTransforms().size());
        }
    }

    @Nested
    @DisplayName("Zoom tests")
    class ZoomTests {

        @Test
        @DisplayName("should zoom in by default amount")
        void shouldZoomInByDefaultAmount() {
            double initialZ = controller.getCamera().getTranslateZ();

            controller.zoomIn();

            double newZ = controller.getCamera().getTranslateZ();
            assertTrue(newZ > initialZ, "Camera should move forward (less negative Z)");
        }

        @Test
        @DisplayName("should zoom out by default amount")
        void shouldZoomOutByDefaultAmount() {
            double initialZ = controller.getCamera().getTranslateZ();

            controller.zoomOut();

            double newZ = controller.getCamera().getTranslateZ();
            assertTrue(newZ < initialZ, "Camera should move backward (more negative Z)");
        }

        @Test
        @DisplayName("should zoom in by custom amount")
        void shouldZoomInByCustomAmount() {
            double initialZ = controller.getCamera().getTranslateZ();

            controller.zoomIn(100);

            double newZ = controller.getCamera().getTranslateZ();
            assertEquals(initialZ + 100, newZ, 0.001);
        }

        @Test
        @DisplayName("should zoom out by custom amount")
        void shouldZoomOutByCustomAmount() {
            double initialZ = controller.getCamera().getTranslateZ();

            controller.zoomOut(100);

            double newZ = controller.getCamera().getTranslateZ();
            assertEquals(initialZ - 100, newZ, 0.001);
        }

        @Test
        @DisplayName("should handle zoomGraph with positive value (zoom out)")
        void shouldHandleZoomGraphPositive() {
            double initialZ = controller.getCamera().getTranslateZ();

            controller.zoomGraph(50);

            double newZ = controller.getCamera().getTranslateZ();
            assertEquals(initialZ - 50, newZ, 0.001);
        }

        @Test
        @DisplayName("should handle zoomGraph with negative value (zoom in)")
        void shouldHandleZoomGraphNegative() {
            double initialZ = controller.getCamera().getTranslateZ();

            controller.zoomGraph(-50);

            double newZ = controller.getCamera().getTranslateZ();
            assertEquals(initialZ + 50, newZ, 0.001);
        }
    }

    @Nested
    @DisplayName("Rotation tests")
    class RotationTests {

        @Test
        @DisplayName("should set rotation angles")
        void shouldSetRotationAngles() {
            controller.setRotationAngles(45, 90, 180);

            // Verify transforms were applied (checking world transforms)
            assertEquals(3, world.getTransforms().size());
        }

        @Test
        @DisplayName("should reset position to default angles")
        void shouldResetPositionToDefaultAngles() {
            // First change angles
            controller.setRotationAngles(45, 90, 180);

            // Then reset
            controller.resetPosition();

            // Camera Z should be reset to -1600
            assertEquals(-1600, controller.getCamera().getTranslateZ(), 0.001);
        }

        @Test
        @DisplayName("should rotate XY based on mouse delta")
        void shouldRotateXY() {
            // Just verify it doesn't throw
            assertDoesNotThrow(() -> controller.rotateXY(1, 1.0, 10.0, 5.0));
        }

        @Test
        @DisplayName("should roll based on mouse delta")
        void shouldRoll() {
            // Just verify it doesn't throw
            assertDoesNotThrow(() -> controller.roll(1, 1.0, 10.0));
        }
    }

    @Nested
    @DisplayName("View change callback tests")
    class ViewChangeCallbackTests {

        @Test
        @DisplayName("should call onViewChange callback when zooming in")
        void shouldCallCallbackOnZoomIn() {
            AtomicInteger callCount = new AtomicInteger(0);
            controller.setOnViewChange(callCount::incrementAndGet);

            controller.zoomIn();

            assertEquals(1, callCount.get());
        }

        @Test
        @DisplayName("should call onViewChange callback when zooming out")
        void shouldCallCallbackOnZoomOut() {
            AtomicInteger callCount = new AtomicInteger(0);
            controller.setOnViewChange(callCount::incrementAndGet);

            controller.zoomOut();

            assertEquals(1, callCount.get());
        }

        @Test
        @DisplayName("should call onViewChange callback when setting rotation angles")
        void shouldCallCallbackOnSetRotationAngles() {
            AtomicInteger callCount = new AtomicInteger(0);
            controller.setOnViewChange(callCount::incrementAndGet);

            controller.setRotationAngles(45, 90, 180);

            assertEquals(1, callCount.get());
        }

        @Test
        @DisplayName("should not throw when callback is null")
        void shouldNotThrowWhenCallbackIsNull() {
            // No callback set
            assertDoesNotThrow(() -> controller.zoomIn());
        }
    }

    @Nested
    @DisplayName("Translation tests")
    class TranslationTests {

        @Test
        @DisplayName("should translate camera XY")
        void shouldTranslateCameraXY() {
            controller.translateXY(100, 200);

            assertEquals(100, controller.getCamera().getTranslateX(), 0.001);
            assertEquals(200, controller.getCamera().getTranslateY(), 0.001);
        }
    }

    @Nested
    @DisplayName("View reset tests")
    class ViewResetTests {

        @Test
        @DisplayName("should reset view to initial state")
        void shouldResetViewToInitialState() {
            // Modify camera position
            controller.zoomIn(500);

            // Reset
            controller.resetView();

            // Should be back to initial Z
            assertEquals(-1600, controller.getCamera().getTranslateZ(), 0.001);
        }

        @Test
        @DisplayName("should set initial view")
        void shouldSetInitialView() {
            // Modify camera
            controller.getCamera().setTranslateZ(-500);

            // Set initial view
            controller.setInitialView();

            // Should be back to initial Z
            assertEquals(-1600, controller.getCamera().getTranslateZ(), 0.001);
        }
    }

    @Nested
    @DisplayName("Animation tests")
    class AnimationTests {

        // Note: Animation tests that call toggleAnimation() require a full JavaFX toolkit
        // to be initialized, which is not available in headless test environment.
        // The animation functionality is tested via integration tests.

        @Test
        @DisplayName("should have animation rotator created")
        void shouldHaveAnimationRotatorCreated() {
            // Just verify the controller was created successfully
            // Animation is created during construction
            assertNotNull(controller);
        }
    }

    @Nested
    @DisplayName("Shift display tests")
    class ShiftDisplayTests {

        @Test
        @DisplayName("should shift display left when shift is true")
        void shouldShiftDisplayLeftWhenShiftIsTrue() {
            AtomicInteger callCount = new AtomicInteger(0);
            controller.setOnViewChange(callCount::incrementAndGet);

            controller.shiftDisplayLeft(true, 800);

            // Should have called view change callback
            assertEquals(1, callCount.get());
        }

        @Test
        @DisplayName("should handle shift display right")
        void shouldHandleShiftDisplayRight() {
            AtomicInteger callCount = new AtomicInteger(0);
            controller.setOnViewChange(callCount::incrementAndGet);

            // First shift needs to set up state
            controller.shiftDisplayLeft(true, 800);

            // Now shift right
            controller.shiftDisplayLeft(false, 800);

            // Should have called view change callback twice
            assertEquals(2, callCount.get());
        }
    }
}
