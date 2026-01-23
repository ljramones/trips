package com.teamgannon.trips.graphics.panes;

import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Rotate;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles mouse events for planetary sky visualization.
 * <p>
 * Handles:
 * <ul>
 *   <li>Scroll events for zooming</li>
 *   <li>Mouse drag for rotation</li>
 * </ul>
 */
@Slf4j
public class PlanetaryMouseHandler {

    private final SubScene subScene;
    private final PerspectiveCamera camera;
    private final Rotate rotateX;
    private final Rotate rotateY;
    private final Runnable updateCallback;

    // Mouse position tracking
    private double mousePosX, mousePosY = 0;
    private double mouseOldX, mouseOldY = 0;
    private double mouseDeltaX, mouseDeltaY = 0;

    public PlanetaryMouseHandler(SubScene subScene,
                                  PerspectiveCamera camera,
                                  Rotate rotateX,
                                  Rotate rotateY,
                                  Runnable updateCallback) {
        this.subScene = subScene;
        this.camera = camera;
        this.rotateX = rotateX;
        this.rotateY = rotateY;
        this.updateCallback = updateCallback;
    }

    /**
     * Initialize all mouse event handlers on the subscene.
     */
    public void initialize() {
        subScene.setOnScroll(this::handleScroll);
        subScene.setOnMousePressed(this::handleMousePressed);
        subScene.setOnMouseDragged(this::handleMouseDragged);
    }

    /**
     * Handle scroll events for zooming.
     */
    private void handleScroll(ScrollEvent event) {
        double deltaY = event.getDeltaY();
        zoomView(deltaY * 2);
        updateCallback.run();
    }

    /**
     * Handle mouse pressed for tracking position.
     */
    private void handleMousePressed(MouseEvent me) {
        mousePosX = me.getSceneX();
        mousePosY = me.getSceneY();
        mouseOldX = me.getSceneX();
        mouseOldY = me.getSceneY();
    }

    /**
     * Handle mouse drag for rotation.
     */
    private void handleMouseDragged(MouseEvent me) {
        mouseOldX = mousePosX;
        mouseOldY = mousePosY;
        mousePosX = me.getSceneX();
        mousePosY = me.getSceneY();
        mouseDeltaX = (mousePosX - mouseOldX);
        mouseDeltaY = (mousePosY - mouseOldY);

        double modifier = 1.0;
        double modifierFactor = 0.1;

        if (me.isPrimaryButtonDown()) {
            // Rotate view - negate X delta to match negated azimuth
            rotateY.setAngle(((rotateY.getAngle() - mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);
            rotateX.setAngle(Math.max(-90, Math.min(90,
                    rotateX.getAngle() + mouseDeltaY * modifierFactor * modifier * 2.0)));
            updateCallback.run();
        }
    }

    /**
     * Zoom the view.
     * Camera must stay INSIDE the sky dome (radius=500) for CullFace.FRONT to work.
     */
    private void zoomView(double zoomAmt) {
        double z = camera.getTranslateZ();
        double newZ = z - zoomAmt;
        // Clamp zoom to keep camera inside the 500-radius dome
        newZ = Math.max(-450, Math.min(-50, newZ));
        camera.setTranslateZ(newZ);
    }
}
