package com.teamgannon.trips.graphics.panes;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.transform.Translate;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages billboard-style star labels for planetary sky visualization.
 * <p>
 * Handles:
 * <ul>
 *   <li>2D labels that face the camera</li>
 *   <li>Collision detection to prevent overlap</li>
 *   <li>Position caching to prevent jitter</li>
 * </ul>
 */
@Slf4j
public class PlanetaryLabelManager {

    private static final double LABEL_COLLISION_PADDING = 4.0;
    private static final double LABEL_MOVE_EPSILON = 0.5;

    private final Group starLabelGroup;
    private final SubScene subScene;
    private final PerspectiveCamera camera;
    private final java.util.function.Supplier<Map<Node, Label>> shapeToLabelSupplier;

    private final Map<Label, Point2D> lastLabelPositions = new HashMap<>();
    private boolean starLabelsOn = true;

    public PlanetaryLabelManager(Group starLabelGroup,
                                  SubScene subScene,
                                  PerspectiveCamera camera,
                                  java.util.function.Supplier<Map<Node, Label>> shapeToLabelSupplier) {
        this.starLabelGroup = starLabelGroup;
        this.subScene = subScene;
        this.camera = camera;
        this.shapeToLabelSupplier = shapeToLabelSupplier;
    }

    /**
     * Toggle star labels on/off.
     */
    public void setStarLabelsOn(boolean on) {
        this.starLabelsOn = on;
        if (!on) {
            starLabelGroup.getChildren().clear();
            lastLabelPositions.clear();
        } else {
            updateLabels();
        }
    }

    public boolean isStarLabelsOn() {
        return starLabelsOn;
    }

    /**
     * Clear all labels and cached positions.
     */
    public void clear() {
        starLabelGroup.getChildren().clear();
        lastLabelPositions.clear();
    }

    /**
     * Update billboard-style star labels to track their 3D positions.
     * Labels are rendered in a 2D overlay group and positioned to match their 3D star nodes.
     */
    public void updateLabels() {
        if (!starLabelsOn) {
            return;
        }

        Map<Node, Label> shapeToLabel = shapeToLabelSupplier.get();
        if (shapeToLabel.isEmpty()) {
            return;
        }

        // Build list of label candidates with distance to camera
        List<LabelCandidate> candidates = new ArrayList<>(shapeToLabel.size());
        for (Map.Entry<Node, Label> entry : shapeToLabel.entrySet()) {
            Node node = entry.getKey();
            Label label = entry.getValue();

            // Skip nodes with invalid positions
            if (Double.isNaN(node.getTranslateX())) {
                label.setVisible(false);
                continue;
            }

            // Step 1: Get 3D node's position in scene coordinates
            Point3D sceneCoords = node.localToScene(Point3D.ZERO, true);
            if (Double.isNaN(sceneCoords.getX()) || Double.isNaN(sceneCoords.getY())) {
                label.setVisible(false);
                continue;
            }

            double distanceToCamera = Math.abs(sceneCoords.getZ() - camera.getTranslateZ());
            candidates.add(new LabelCandidate(node, label, sceneCoords, distanceToCamera));
        }

        // Sort by distance to camera (closest first - they get label priority)
        candidates.sort((a, b) -> Double.compare(a.distanceToCamera(), b.distanceToCamera()));

        // Track occupied screen areas for collision detection
        List<Rectangle2D> occupiedBounds = new ArrayList<>();

        for (LabelCandidate candidate : candidates) {
            Node node = candidate.node();
            Label label = candidate.label();
            Point3D sceneCoords = candidate.sceneCoords();

            // Ensure label is in the display group
            if (!starLabelGroup.getChildren().contains(label)) {
                starLabelGroup.getChildren().add(label);
            }

            // Step 2: Convert scene coordinates to starLabelGroup's local coordinates
            Point2D localPoint = starLabelGroup.sceneToLocal(sceneCoords.getX(), sceneCoords.getY());
            if (localPoint == null) {
                label.setVisible(false);
                continue;
            }

            double x = localPoint.getX();
            double y = localPoint.getY();

            // Viewport bounds check
            double sceneWidth = subScene.getWidth();
            double sceneHeight = subScene.getHeight();
            if (x < 20 || x > (sceneWidth - 20) || y < 20 || y > (sceneHeight - 50)) {
                label.setVisible(false);
                continue;
            }

            // Offset label slightly to the right and up from the star
            x += 5;
            y -= 3;

            // Get label dimensions (estimate if not yet laid out)
            double labelWidth = label.getWidth() > 0 ? label.getWidth() : label.getText().length() * 6;
            double labelHeight = label.getHeight() > 0 ? label.getHeight() : 12;

            // Collision detection
            Rectangle2D candidateBounds = new Rectangle2D(
                    x - LABEL_COLLISION_PADDING,
                    y - LABEL_COLLISION_PADDING,
                    labelWidth + 2 * LABEL_COLLISION_PADDING,
                    labelHeight + 2 * LABEL_COLLISION_PADDING
            );

            boolean collision = false;
            for (Rectangle2D occupied : occupiedBounds) {
                if (candidateBounds.intersects(occupied)) {
                    collision = true;
                    break;
                }
            }

            if (collision) {
                label.setVisible(false);
                continue;
            }

            // Position caching to prevent jitter
            Point2D lastPos = lastLabelPositions.get(label);
            if (lastPos != null) {
                double dx = Math.abs(x - lastPos.getX());
                double dy = Math.abs(y - lastPos.getY());
                if (dx < LABEL_MOVE_EPSILON && dy < LABEL_MOVE_EPSILON) {
                    x = lastPos.getX();
                    y = lastPos.getY();
                }
            }
            lastLabelPositions.put(label, new Point2D(x, y));

            // Apply position via Translate transform
            label.getTransforms().setAll(new Translate(x, y));
            label.setVisible(true);
            occupiedBounds.add(candidateBounds);
        }
    }

    /**
     * Record for sorting labels by distance to camera.
     */
    private record LabelCandidate(Node node, Label label, Point3D sceneCoords, double distanceToCamera) {}
}
