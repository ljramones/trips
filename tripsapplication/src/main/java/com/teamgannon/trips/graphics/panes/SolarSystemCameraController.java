package com.teamgannon.trips.graphics.panes;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

public class SolarSystemCameraController {

    private static final Duration DEFAULT_DURATION = Duration.millis(450);
    private static final double MIN_FOCUS_DISTANCE = 400.0;
    private static final double FOCUS_DISTANCE_MULTIPLIER = 8.0;

    private final PerspectiveCamera camera;
    private final Rotate rotateX;
    private final Rotate rotateY;
    private final Rotate rotateZ;
    private final Translate worldTranslate;
    private final Runnable onUpdate;
    private Timeline activeTimeline;

    public SolarSystemCameraController(PerspectiveCamera camera,
                                       Rotate rotateX,
                                       Rotate rotateY,
                                       Rotate rotateZ,
                                       Translate worldTranslate,
                                       Runnable onUpdate) {
        this.camera = camera;
        this.rotateX = rotateX;
        this.rotateY = rotateY;
        this.rotateZ = rotateZ;
        this.worldTranslate = worldTranslate;
        this.onUpdate = onUpdate;
    }

    public void animatePreset(double targetRotateX, double targetRotateY, double targetRotateZ) {
        animateTo(new CameraPose(targetRotateX, targetRotateY, targetRotateZ,
                camera.getTranslateZ(), 0, 0, 0));
    }

    /**
     * Reset view to default: top-down view, centered, default zoom
     */
    public void resetView() {
        animateTo(new CameraPose(90, 0, 0, -1600, 0, 0, 0));
    }

    /**
     * Reset pan only (keep current rotation and zoom)
     */
    public void resetPan() {
        animateTo(new CameraPose(rotateX.getAngle(), rotateY.getAngle(), rotateZ.getAngle(),
                camera.getTranslateZ(), 0, 0, 0));
    }

    public void focusOn(Node target, Group world) {
        if (target == null || world == null) {
            return;
        }
        Point3D scenePoint = target.localToScene(Point3D.ZERO);
        Point3D worldPoint = world.sceneToLocal(scenePoint);

        Bounds bounds = target.getBoundsInParent();
        double maxExtent = Math.max(bounds.getWidth(), Math.max(bounds.getHeight(), bounds.getDepth()));
        double targetDistance = -Math.max(MIN_FOCUS_DISTANCE, maxExtent * FOCUS_DISTANCE_MULTIPLIER);

        animateTo(new CameraPose(rotateX.getAngle(), rotateY.getAngle(), rotateZ.getAngle(),
                targetDistance, -worldPoint.getX(), -worldPoint.getY(), -worldPoint.getZ()));
    }

    private void animateTo(CameraPose target) {
        if (activeTimeline != null) {
            activeTimeline.stop();
        }

        KeyValue kvRotateX = new KeyValue(rotateX.angleProperty(), target.rotateX, Interpolator.EASE_BOTH);
        KeyValue kvRotateY = new KeyValue(rotateY.angleProperty(), target.rotateY, Interpolator.EASE_BOTH);
        KeyValue kvRotateZ = new KeyValue(rotateZ.angleProperty(), target.rotateZ, Interpolator.EASE_BOTH);
        KeyValue kvCameraZ = new KeyValue(camera.translateZProperty(), target.cameraZ, Interpolator.EASE_BOTH);
        KeyValue kvTranslateX = new KeyValue(worldTranslate.xProperty(), target.translateX, Interpolator.EASE_BOTH);
        KeyValue kvTranslateY = new KeyValue(worldTranslate.yProperty(), target.translateY, Interpolator.EASE_BOTH);
        KeyValue kvTranslateZ = new KeyValue(worldTranslate.zProperty(), target.translateZ, Interpolator.EASE_BOTH);

        activeTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, kvRotateX, kvRotateY, kvRotateZ, kvCameraZ, kvTranslateX, kvTranslateY, kvTranslateZ),
                new KeyFrame(DEFAULT_DURATION, kvRotateX, kvRotateY, kvRotateZ, kvCameraZ, kvTranslateX, kvTranslateY, kvTranslateZ)
        );
        activeTimeline.currentTimeProperty().addListener((obs, oldVal, newVal) -> onUpdate.run());
        activeTimeline.setOnFinished(e -> onUpdate.run());
        activeTimeline.play();
    }

    private record CameraPose(double rotateX, double rotateY, double rotateZ,
                              double cameraZ, double translateX, double translateY, double translateZ) {
    }
}
