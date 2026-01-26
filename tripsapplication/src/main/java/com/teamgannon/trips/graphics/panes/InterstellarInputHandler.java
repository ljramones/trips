package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.config.application.model.UserControls;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.util.Duration;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * Handles mouse and keyboard input for the interstellar space pane.
 * Manages rotation, zooming, and panning via mouse and keyboard events.
 * <p>
 * Keyboard Controls:
 * <ul>
 *   <li>W/S - Zoom in/out (forward/backward)</li>
 *   <li>A/D - Strafe left/right (pan horizontally)</li>
 *   <li>Q/E - Pan up/down (vertical movement)</li>
 *   <li>Arrow Up/Down - Pitch (rotate around X axis)</li>
 *   <li>Arrow Left/Right - Yaw (rotate around Z axis)</li>
 *   <li>R/F - Roll left/right</li>
 *   <li>Shift - Hold for faster movement</li>
 *   <li>Ctrl - Hold for slower (precise) movement</li>
 * </ul>
 */
@Slf4j
public class InterstellarInputHandler {

    private final SubScene subScene;
    private final InterstellarCameraController cameraController;
    private final Runnable onInputChange;

    // Mouse position tracking
    private double mousePosX, mousePosY = 0;
    private double mouseOldX, mouseOldY = 0;
    private double mouseDeltaX, mouseDeltaY = 0;

    // Keyboard tracking for continuous movement
    private final Set<KeyCode> pressedKeyCodes = new HashSet<>();
    private Timeline keyboardTimeline;
    private boolean keyboardNavigationEnabled = true;

    // Movement speeds (units per frame at 60fps)
    private static final double ZOOM_SPEED = 10.0;        // Camera Z translation per frame
    private static final double PAN_SPEED = 5.0;          // Camera XY translation per frame
    private static final double ROTATION_SPEED = 1.0;     // Degrees per frame

    /**
     * User control settings (engineer vs pilot mode).
     */
    @Setter
    private UserControls userControls = new UserControls();

    /**
     * Minimum interval between label updates during continuous interactions (ms).
     */
    private static final long LABEL_UPDATE_THROTTLE_MS = 16;

    /**
     * Timestamp of the last label update.
     */
    private long lastLabelUpdateTime = 0;

    public InterstellarInputHandler(SubScene subScene,
                                    InterstellarCameraController cameraController,
                                    Runnable onInputChange) {
        this.subScene = subScene;
        this.cameraController = cameraController;
        this.onInputChange = onInputChange;
    }

    /**
     * Initialize all event handlers on the subscene.
     */
    public void initialize() {
        subScene.setOnKeyPressed(this::handleKeyPress);
        subScene.setOnKeyReleased(this::handleKeyRelease);
        subScene.setOnScroll(this::handleMouseScroll);
        subScene.setOnMousePressed(this::handleMousePress);
        subScene.setOnMouseDragged(this::handleMouseDrag);

        // Create timeline for continuous keyboard navigation
        initializeKeyboardTimeline();
    }

    /**
     * Initialize the timeline for continuous keyboard-based navigation.
     * Runs at approximately 60fps to check pressed keys and update view.
     */
    private void initializeKeyboardTimeline() {
        keyboardTimeline = new Timeline();
        keyboardTimeline.setCycleCount(Timeline.INDEFINITE);
        keyboardTimeline.getKeyFrames().add(new KeyFrame(Duration.millis(16), event -> {
            if (keyboardNavigationEnabled && !pressedKeyCodes.isEmpty()) {
                processKeyboardNavigation();
            }
        }));
        keyboardTimeline.play();
    }

    /**
     * Process all currently pressed keys for navigation.
     */
    private void processKeyboardNavigation() {
        int direction = userControls.isControlSense() ? +1 : -1;

        // Determine speed modifier based on held modifier keys
        double speedMultiplier = getSpeedMultiplier();

        boolean viewChanged = false;

        // W/S - Zoom (forward/backward along camera Z)
        if (pressedKeyCodes.contains(KeyCode.W)) {
            cameraController.zoomGraph(-ZOOM_SPEED * speedMultiplier);
            viewChanged = true;
        }
        if (pressedKeyCodes.contains(KeyCode.S)) {
            cameraController.zoomGraph(ZOOM_SPEED * speedMultiplier);
            viewChanged = true;
        }

        // A/D - Strafe left/right (pan X)
        if (pressedKeyCodes.contains(KeyCode.A)) {
            cameraController.moveXY(-PAN_SPEED * speedMultiplier * direction, 0);
            viewChanged = true;
        }
        if (pressedKeyCodes.contains(KeyCode.D)) {
            cameraController.moveXY(PAN_SPEED * speedMultiplier * direction, 0);
            viewChanged = true;
        }

        // Q/E - Pan up/down (Y axis)
        if (pressedKeyCodes.contains(KeyCode.Q)) {
            cameraController.moveXY(0, -PAN_SPEED * speedMultiplier * direction);
            viewChanged = true;
        }
        if (pressedKeyCodes.contains(KeyCode.E)) {
            cameraController.moveXY(0, PAN_SPEED * speedMultiplier * direction);
            viewChanged = true;
        }

        // Arrow keys - Rotation
        // Up/Down - Pitch (rotate around X)
        if (pressedKeyCodes.contains(KeyCode.UP)) {
            cameraController.rotateX(-ROTATION_SPEED * speedMultiplier * direction);
            viewChanged = true;
        }
        if (pressedKeyCodes.contains(KeyCode.DOWN)) {
            cameraController.rotateX(ROTATION_SPEED * speedMultiplier * direction);
            viewChanged = true;
        }

        // Left/Right - Yaw (rotate around Z)
        if (pressedKeyCodes.contains(KeyCode.LEFT)) {
            cameraController.rotateZAxis(-ROTATION_SPEED * speedMultiplier * direction);
            viewChanged = true;
        }
        if (pressedKeyCodes.contains(KeyCode.RIGHT)) {
            cameraController.rotateZAxis(ROTATION_SPEED * speedMultiplier * direction);
            viewChanged = true;
        }

        // R/F - Roll
        if (pressedKeyCodes.contains(KeyCode.R)) {
            cameraController.roll(direction, 1.0, ROTATION_SPEED * speedMultiplier);
            viewChanged = true;
        }
        if (pressedKeyCodes.contains(KeyCode.F)) {
            cameraController.roll(direction, 1.0, -ROTATION_SPEED * speedMultiplier);
            viewChanged = true;
        }

        if (viewChanged) {
            throttledUpdateLabels();
        }
    }

    /**
     * Calculate speed multiplier based on held modifier keys.
     * - Shift: Fast speed (2x)
     * - Ctrl: Slow speed (0.25x)
     * - Shift+Ctrl: Very fast (5x)
     * - None: Normal speed (1x)
     */
    private double getSpeedMultiplier() {
        boolean shiftHeld = pressedKeyCodes.contains(KeyCode.SHIFT);
        boolean ctrlHeld = pressedKeyCodes.contains(KeyCode.CONTROL);

        if (shiftHeld && ctrlHeld) {
            return userControls.getVeryFast() / UserControls.NORMAL_SPEED;
        } else if (shiftHeld) {
            return userControls.getFast() / UserControls.NORMAL_SPEED;
        } else if (ctrlHeld) {
            return userControls.getSlow() / UserControls.NORMAL_SPEED;
        }
        return 1.0;
    }

    private void handleMouseScroll(ScrollEvent event) {
        double deltaY = event.getDeltaY();
        cameraController.zoomGraph(deltaY * 5);
        throttledUpdateLabels();
    }

    private void handleMousePress(MouseEvent me) {
        mousePosX = me.getSceneX();
        mousePosY = me.getSceneY();
        mouseOldX = me.getSceneX();
        mouseOldY = me.getSceneY();
        subScene.requestFocus();
    }

    private void handleMouseDrag(MouseEvent me) {
        int direction = userControls.isControlSense() ? +1 : -1;
        mouseOldX = mousePosX;
        mouseOldY = mousePosY;
        mousePosX = me.getSceneX();
        mousePosY = me.getSceneY();
        mouseDeltaX = (mousePosX - mouseOldX);
        mouseDeltaY = (mousePosY - mouseOldY);
        double modifier = UserControls.NORMAL_SPEED;

        if (me.isPrimaryButtonDown() && me.isControlDown()) {
            double width = subScene.getWidth();
            double height = subScene.getHeight();
            cameraController.translateXY(width / 2 - mousePosX, height / 2 - mousePosY);
        } else if (me.isPrimaryButtonDown()) {
            if (me.isAltDown()) {
                // Roll
                cameraController.roll(direction, modifier, mouseDeltaX);
            } else {
                cameraController.rotateXY(direction, modifier, mouseDeltaX, mouseDeltaY);
            }
        }
        throttledUpdateLabels();
    }

    private void handleKeyPress(KeyEvent event) {
        pressedKeyCodes.add(event.getCode());

        // Handle non-navigation key presses (single-shot actions)
        switch (event.getCode()) {
            case ESCAPE:
                // Reset view to default
                cameraController.resetPosition();
                throttledUpdateLabels();
                break;
            case SPACE:
                // Toggle animation
                cameraController.toggleAnimation();
                break;
            case HOME:
                // Reset to initial view
                cameraController.setInitialView();
                throttledUpdateLabels();
                break;
            default:
                // Continuous navigation keys are handled by timeline
                break;
        }
    }

    private void handleKeyRelease(KeyEvent event) {
        pressedKeyCodes.remove(event.getCode());
    }

    /**
     * Enable or disable keyboard navigation.
     * Useful when focus should go to dialogs or text fields.
     */
    public void setKeyboardNavigationEnabled(boolean enabled) {
        this.keyboardNavigationEnabled = enabled;
    }

    /**
     * Stop the keyboard navigation timeline.
     * Should be called when the pane is no longer in use.
     */
    public void dispose() {
        if (keyboardTimeline != null) {
            keyboardTimeline.stop();
        }
        pressedKeyCodes.clear();
    }

    /**
     * Throttled version of label updates for use during continuous interactions.
     * Limits updates to at most one per LABEL_UPDATE_THROTTLE_MS milliseconds.
     */
    private void throttledUpdateLabels() {
        long now = System.currentTimeMillis();
        if (now - lastLabelUpdateTime >= LABEL_UPDATE_THROTTLE_MS) {
            lastLabelUpdateTime = now;
            onInputChange.run();
        }
    }
}
