package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.controller.MainPane;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * Manages camera positioning, rotation, zoom, and animation for the interstellar space view.
 */
@Slf4j
public class InterstellarCameraController {

    private static final double ROTATE_SECS = 60;

    // Default rotation angles
    private static final double DEFAULT_ROTATE_X = 105;
    private static final double DEFAULT_ROTATE_Y = 0;
    private static final double DEFAULT_ROTATE_Z = 30;

    @Getter
    private final PerspectiveCamera camera = new PerspectiveCamera(true);

    private final Rotate rotateX = new Rotate(DEFAULT_ROTATE_X, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(DEFAULT_ROTATE_Y, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(DEFAULT_ROTATE_Z, Rotate.Z_AXIS);

    private final Group world;
    private final RotateTransition rotator;

    private final double baseCameraTranslateX;
    private double deltaX;

    private boolean sidePanelShiftKludgeFirstTime = true;
    private boolean animationPlay = false;

    /**
     * Callback to notify when view changes require label updates.
     */
    private Runnable onViewChange;

    public InterstellarCameraController(Group world) {
        this.world = world;

        // Attach rotation transforms to world group
        world.getTransforms().addAll(rotateX, rotateY, rotateZ);

        // Initialize camera
        setPerspectiveCamera();
        baseCameraTranslateX = camera.getTranslateX();

        // Create rotation animation
        rotator = createRotateAnimation();
    }

    /**
     * Set callback for view changes that require label updates.
     */
    public void setOnViewChange(Runnable onViewChange) {
        this.onViewChange = onViewChange;
    }

    /**
     * Set rotation angles and update labels.
     */
    public void setRotationAngles(double xAngle, double yAngle, double zAngle) {
        rotateX.setAngle(xAngle);
        rotateY.setAngle(yAngle);
        rotateZ.setAngle(zAngle);
        notifyViewChange();
    }

    /**
     * Reset position to default view.
     */
    public void resetPosition() {
        setRotationAngles(DEFAULT_ROTATE_X, DEFAULT_ROTATE_Y, DEFAULT_ROTATE_Z);
        setPerspectiveCamera();
    }

    /**
     * Set camera to initial perspective settings.
     */
    public void setPerspectiveCamera() {
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-1600);
    }

    /**
     * Alias for setPerspectiveCamera.
     */
    public void setInitialView() {
        setPerspectiveCamera();
    }

    /**
     * Reset view to initial state.
     */
    public void resetView() {
        setInitialView();
    }

    /**
     * Zoom in by default amount.
     */
    public void zoomIn() {
        zoomGraph(-200);
        notifyViewChange();
    }

    /**
     * Zoom in by specified amount.
     */
    public void zoomIn(int amount) {
        zoomGraph(-amount);
        notifyViewChange();
    }

    /**
     * Zoom out by default amount.
     */
    public void zoomOut() {
        zoomGraph(200);
        notifyViewChange();
    }

    /**
     * Zoom out by specified amount.
     */
    public void zoomOut(int amount) {
        zoomGraph(amount);
        notifyViewChange();
    }

    /**
     * Perform zoom operation.
     * @param zoomAmt positive zooms out, negative zooms in
     */
    public void zoomGraph(double zoomAmt) {
        double z = camera.getTranslateZ();
        double newZ = z - zoomAmt;
        camera.setTranslateZ(newZ);
    }

    /**
     * Translate camera in XY plane.
     */
    public void translateXY(double x, double y) {
        camera.setTranslateX(x);
        camera.setTranslateY(y);
    }

    /**
     * Rotate around X and Z axes.
     */
    public void rotateXY(int direction, double modifier, double mouseDeltaX, double mouseDeltaY) {
        rotateZ.setAngle(((rotateZ.getAngle() + direction * mouseDeltaX * modifier) % 360));
        rotateX.setAngle(((rotateX.getAngle() - direction * mouseDeltaY * modifier) % 360));
    }

    /**
     * Roll (rotate around Z axis only).
     */
    public void roll(int direction, double modifier, double mouseDeltaX) {
        rotateZ.setAngle(((rotateZ.getAngle() + direction * mouseDeltaX * modifier)) % 360);
    }

    /**
     * Shift display left/right to accommodate side panel.
     * @param shift true to shift left, false to reset
     * @param paneWidth width of the pane for deferred calculation
     */
    public void shiftDisplayLeft(boolean shift, double paneWidth) {
        if (shift) {
            if (paneWidth <= 0) {
                // Defer until width is available
                Platform.runLater(() -> shiftDisplayLeft(true, paneWidth));
                return;
            }
            deltaX = MainPane.SIDE_PANEL_SIZE / 2.0;
            log.info("shift display left by {}", deltaX);
            camera.setTranslateX(baseCameraTranslateX + deltaX);
        } else {
            if (!sidePanelShiftKludgeFirstTime) {
                log.info("shift display right!!");
                camera.setTranslateX(baseCameraTranslateX);
            } else {
                sidePanelShiftKludgeFirstTime = false;
            }
        }
        notifyViewChange();
    }

    /**
     * Toggle rotation animation on/off.
     */
    public void toggleAnimation() {
        animationPlay = !animationPlay;
        if (animationPlay) {
            rotator.play();
        } else {
            rotator.pause();
        }
    }

    /**
     * Create the rotation animation.
     */
    private @NotNull RotateTransition createRotateAnimation() {
        RotateTransition rotate = new RotateTransition(
                Duration.seconds(ROTATE_SECS),
                world
        );
        rotate.setAxis(Rotate.Y_AXIS);
        rotate.setFromAngle(360);
        rotate.setToAngle(0);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        return rotate;
    }

    private void notifyViewChange() {
        if (onViewChange != null) {
            onViewChange.run();
        }
    }
}
