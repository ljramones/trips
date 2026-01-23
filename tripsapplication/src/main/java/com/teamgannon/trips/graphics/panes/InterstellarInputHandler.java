package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.config.application.model.UserControls;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles mouse and keyboard input for the interstellar space pane.
 * Manages rotation, zooming, and panning via mouse and keyboard events.
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
        subScene.setOnScroll(this::handleMouseScroll);
        subScene.setOnMousePressed(this::handleMousePress);
        subScene.setOnMouseDragged(this::handleMouseDrag);
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
        switch (event.getCode()) {
            case Z:
                if (event.isShiftDown()) {
                    log.info("shift pressed -> Z");
                }
                break;
            case X:
                if (event.isControlDown()) {
                    log.info("control pressed -> X");
                }
                break;
            case S:
            case SPACE:
                break;
            case UP:
                handleUpKey(event);
                break;
            case DOWN:
                handleDownKey(event);
                break;
            case RIGHT:
                handleRightKey(event);
                break;
            case LEFT:
                handleLeftKey(event);
                break;
            default:
                log.info("keyboard Event is {}", event.getCode());
        }
    }

    private void handleUpKey(KeyEvent event) {
        if (event.isControlDown() && event.isShiftDown()) {
            log.info("control and shift pressed -> up");
        } else if (event.isAltDown() && event.isShiftDown()) {
            log.info("alt and shift pressed -> up");
        } else if (event.isControlDown()) {
            log.info("control pressed -> up");
        } else if (event.isAltDown()) {
            log.info("alt pressed -> up");
        } else if (event.isShiftDown()) {
            log.info("shift pressed -> up");
        }
    }

    private void handleDownKey(KeyEvent event) {
        if (event.isControlDown() && event.isShiftDown()) {
            log.info("control shift pressed -> down");
        } else if (event.isAltDown() && event.isShiftDown()) {
            log.info("alt and shift pressed -> down");
        } else if (event.isControlDown()) {
            log.info("control pressed -> down");
        } else if (event.isAltDown()) {
            log.info("alt pressed -> down");
        } else if (event.isShiftDown()) {
            log.info("shift pressed -> down");
        }
    }

    private void handleRightKey(KeyEvent event) {
        if (event.isControlDown() && event.isShiftDown()) {
            log.info("shift and control pressed -> right");
        } else if (event.isAltDown() && event.isShiftDown()) {
            log.info("shift and alt pressed -> right");
        } else if (event.isControlDown()) {
            log.info("control pressed -> right");
        } else if (event.isAltDown()) {
            log.info("alt pressed -> right");
        }
    }

    private void handleLeftKey(KeyEvent event) {
        if (event.isControlDown() && event.isShiftDown()) {
            log.info("shift and control pressed -> left");
        } else if (event.isAltDown() && event.isShiftDown()) {
            log.info("shift and alt pressed -> left");
        } else if (event.isControlDown()) {
            log.info("control pressed -> left");
        } else if (event.isAltDown()) {
            log.info("alt pressed -> left");
        }
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
