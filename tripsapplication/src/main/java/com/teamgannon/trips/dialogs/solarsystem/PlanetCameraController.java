package com.teamgannon.trips.dialogs.solarsystem;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

import java.util.HashSet;
import java.util.Set;

/**
 * Mouse, scroll, keyboard, and idle-rotation controller for the procedural
 * planet viewer's 3D {@link SubScene}.
 * <p>
 * Extracted from {@code ProceduralPlanetViewerDialog} in Phase 4.2 of the
 * codebase-review remediation. Owns:
 * <ul>
 *   <li>mouse drag → rotateX / rotateY angles;</li>
 *   <li>scroll → camera Z translate (clamped to a sane range);</li>
 *   <li>WASD/QE/R/SPACE keys → flight-sim-style camera + reset/auto-spin;</li>
 *   <li>a 30 ms {@link Timeline} that drives auto-spin and per-frame keyboard
 *       polling.</li>
 * </ul>
 * The dialog still owns the {@link SubScene}, {@link PerspectiveCamera}, and the
 * {@link Rotate} transforms — this controller borrows shared references and
 * mutates their angles / translations directly.
 *
 * <h2>Threading</h2>
 * All public methods must be called on the JavaFX Application thread. The
 * controller manipulates the scene graph and Timeline state directly.
 */
public final class PlanetCameraController {

    /**
     * Camera-Z clamp window. Outside this range zooming is suppressed both for
     * scroll wheel and W/S keys.
     */
    private static final double MIN_CAMERA_Z = -8.0;
    private static final double MAX_CAMERA_Z = -1.5;

    /** Per-frame rotation step (degrees) when WASD/QE are held. */
    private static final double KEYBOARD_ROTATE_STEP = 1.0;

    /** Per-frame camera-Z step when W/S are held. */
    private static final double KEYBOARD_ZOOM_STEP = 0.05;

    /** Auto-spin step (degrees per frame) when auto-rotate is on. */
    private static final double AUTO_SPIN_STEP = 0.3;

    /** Mouse-drag rotation sensitivity. */
    private static final double MOUSE_DRAG_MODIFIER = 0.3;

    /** Latitude clamp (degrees) so the camera doesn't flip over the poles. */
    private static final double MAX_PITCH = 85.0;

    /** Animation tick interval. 30 ms ≈ 33 fps; smooth enough for spin + key polling. */
    private static final Duration TICK = Duration.millis(30);

    private final SubScene subScene;
    private final PerspectiveCamera camera;
    private final Rotate rotateX;
    private final Rotate rotateY;
    private final Rotate spinRotate;
    private final double initialCameraDistance;

    private final Set<KeyCode> pressedKeys = new HashSet<>();

    private double mouseX;
    private double mouseY;
    private double mouseOldX;
    private double mouseOldY;

    private Timeline rotationAnimation;
    private boolean autoRotate = false;

    public PlanetCameraController(SubScene subScene,
                                  PerspectiveCamera camera,
                                  Rotate rotateX,
                                  Rotate rotateY,
                                  Rotate spinRotate,
                                  double initialCameraDistance) {
        this.subScene = subScene;
        this.camera = camera;
        this.rotateX = rotateX;
        this.rotateY = rotateY;
        this.spinRotate = spinRotate;
        this.initialCameraDistance = initialCameraDistance;
    }

    /** Wire up mouse + keyboard handlers and start the animation loop. Idempotent. */
    public void install() {
        setupMouseHandlers();
        initializeAnimation();
        ensureAnimationRunning();
    }

    public void setAutoRotate(boolean enabled) {
        this.autoRotate = enabled;
        ensureAnimationRunning();
    }

    public boolean isAutoRotate() {
        return autoRotate;
    }

    /** Restore the camera to the initial pitch / yaw / distance. */
    public void resetView() {
        rotateX.setAngle(25);
        rotateY.setAngle(25);
        camera.setTranslateZ(initialCameraDistance);
    }

    /** Stop the animation timer (call on dialog close to release the FX-frame tick). */
    public void stop() {
        if (rotationAnimation != null) {
            rotationAnimation.stop();
        }
    }

    // ----- internal -----

    private void setupMouseHandlers() {
        subScene.setOnMousePressed(event -> {
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        subScene.setOnMouseDragged(event -> {
            mouseOldX = mouseX;
            mouseOldY = mouseY;
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();

            double deltaX = mouseX - mouseOldX;
            double deltaY = mouseY - mouseOldY;

            if (event.isPrimaryButtonDown()) {
                rotateY.setAngle(rotateY.getAngle() + deltaX * MOUSE_DRAG_MODIFIER);
                double newPitch = rotateX.getAngle() - deltaY * MOUSE_DRAG_MODIFIER;
                rotateX.setAngle(clamp(newPitch, -MAX_PITCH, MAX_PITCH));
            }
        });

        subScene.setOnScroll(event -> {
            double newZ = camera.getTranslateZ() + event.getDeltaY() * 0.01;
            camera.setTranslateZ(clamp(newZ, MIN_CAMERA_Z, MAX_CAMERA_Z));
        });

        subScene.setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode());
            event.consume();
        });
        subScene.setOnKeyReleased(event -> {
            pressedKeys.remove(event.getCode());
            event.consume();
        });
        subScene.setFocusTraversable(true);
        subScene.setOnMouseClicked(event -> subScene.requestFocus());
    }

    private void initializeAnimation() {
        if (rotationAnimation != null) {
            return;
        }
        rotationAnimation = new Timeline(
                new KeyFrame(TICK, event -> {
                    if (autoRotate) {
                        spinRotate.setAngle(spinRotate.getAngle() + AUTO_SPIN_STEP);
                    }
                    processKeyboardInput();
                }));
        rotationAnimation.setCycleCount(Animation.INDEFINITE);
    }

    private void ensureAnimationRunning() {
        if (rotationAnimation == null) {
            initializeAnimation();
        }
        if (rotationAnimation.getStatus() != Animation.Status.RUNNING) {
            rotationAnimation.play();
        }
    }

    /**
     * Flight-simulator style camera input.
     * <ul>
     *   <li>W / S — zoom in / out</li>
     *   <li>A / D — yaw left / right</li>
     *   <li>Q / E — pitch up / down (clamped to ±{@value #MAX_PITCH}°)</li>
     *   <li>R — reset view (one-shot, consumed)</li>
     *   <li>SPACE — toggle auto-spin (one-shot, consumed)</li>
     * </ul>
     */
    private void processKeyboardInput() {
        if (pressedKeys.contains(KeyCode.W)) {
            camera.setTranslateZ(camera.getTranslateZ() + KEYBOARD_ZOOM_STEP);
        }
        if (pressedKeys.contains(KeyCode.S)) {
            camera.setTranslateZ(camera.getTranslateZ() - KEYBOARD_ZOOM_STEP);
        }
        if (pressedKeys.contains(KeyCode.A)) {
            rotateY.setAngle(rotateY.getAngle() - KEYBOARD_ROTATE_STEP);
        }
        if (pressedKeys.contains(KeyCode.D)) {
            rotateY.setAngle(rotateY.getAngle() + KEYBOARD_ROTATE_STEP);
        }
        if (pressedKeys.contains(KeyCode.Q)) {
            double newAngle = rotateX.getAngle() + KEYBOARD_ROTATE_STEP;
            rotateX.setAngle(Math.min(MAX_PITCH, newAngle));
        }
        if (pressedKeys.contains(KeyCode.E)) {
            double newAngle = rotateX.getAngle() - KEYBOARD_ROTATE_STEP;
            rotateX.setAngle(Math.max(-MAX_PITCH, newAngle));
        }
        if (pressedKeys.contains(KeyCode.R)) {
            resetView();
            pressedKeys.remove(KeyCode.R);
        }
        if (pressedKeys.contains(KeyCode.SPACE)) {
            setAutoRotate(!autoRotate);
            pressedKeys.remove(KeyCode.SPACE);
        }
        camera.setTranslateZ(clamp(camera.getTranslateZ(), MIN_CAMERA_Z, MAX_CAMERA_Z));
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
