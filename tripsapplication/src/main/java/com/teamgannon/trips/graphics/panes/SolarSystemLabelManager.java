package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.solarsystem.rendering.SolarSystemRenderer;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages label positioning and collision detection for solar system visualization.
 * <p>
 * Handles:
 * <ul>
 *   <li>2D billboard-style labels that face the camera</li>
 *   <li>Collision detection to prevent label overlap</li>
 *   <li>Throttled updates during continuous interactions</li>
 *   <li>Moon label proximity filtering</li>
 * </ul>
 */
@Slf4j
public class SolarSystemLabelManager {

    private static final double LABEL_COLLISION_PADDING = 4.0;
    private static final double LABEL_MOVE_EPSILON = 0.5;
    private static final double MIN_MOON_LABEL_DISTANCE = 12.0;

    /**
     * Minimum interval between label updates during continuous interactions (ms).
     * ~60fps = 16ms, but we use a slightly higher value for smoother performance.
     */
    private static final long LABEL_UPDATE_THROTTLE_MS = 16;

    private final Group labelDisplayGroup;
    private final SubScene subScene;
    private final PerspectiveCamera camera;
    private final SolarSystemRenderer renderer;

    private boolean labelsOn = true;
    private long lastLabelUpdateTime = 0;
    private final Map<Node, Point2D> lastLabelPositions = new HashMap<>();

    public SolarSystemLabelManager(Group labelDisplayGroup,
                                   SubScene subScene,
                                   PerspectiveCamera camera,
                                   SolarSystemRenderer renderer) {
        this.labelDisplayGroup = labelDisplayGroup;
        this.subScene = subScene;
        this.camera = camera;
        this.renderer = renderer;
    }

    /**
     * Update label positions to follow their associated 3D nodes.
     * Labels stay flat (billboard-style) because they're in a 2D overlay group.
     */
    public void updateLabels() {
        if (!labelsOn) {
            return;
        }

        Map<Node, Label> shapeToLabel = renderer.getShapeToLabel();
        if (shapeToLabel.isEmpty()) {
            return;
        }

        List<LabelCandidate> candidates = new ArrayList<>(shapeToLabel.size());
        for (Map.Entry<Node, Label> entry : shapeToLabel.entrySet()) {
            Node node = entry.getKey();
            Label label = entry.getValue();

            // Skip nodes with invalid coordinates
            if (Double.isNaN(node.getTranslateX())) {
                label.setVisible(false);
                continue;
            }

            // Get 3D node's position in root Scene coordinates
            Point3D coordinates = node.localToScene(Point3D.ZERO, true);

            // Skip if localToScene returned NaN
            if (Double.isNaN(coordinates.getX()) || Double.isNaN(coordinates.getY())) {
                label.setVisible(false);
                continue;
            }

            double distanceToCamera = Math.abs(coordinates.getZ() - camera.getTranslateZ());
            candidates.add(new LabelCandidate(node, label, coordinates, distanceToCamera));
        }

        candidates.sort((a, b) -> Double.compare(a.distanceToCamera(), b.distanceToCamera()));
        List<Rectangle2D> occupied = new ArrayList<>();

        for (LabelCandidate candidate : candidates) {
            Node node = candidate.node();
            Label label = candidate.label();
            Point2D localPoint = labelDisplayGroup.sceneToLocal(candidate.scenePoint().getX(),
                    candidate.scenePoint().getY());
            double x = localPoint.getX();
            double y = localPoint.getY();

            // Clipping Logic - keep labels within the visible overlay
            if (x < 20 || x > (subScene.getWidth() - 20)) {
                label.setVisible(false);
                continue;
            } else {
                label.setVisible(true);
            }
            if (y < 20 || y > (subScene.getHeight() - 20)) {
                label.setVisible(false);
                continue;
            } else {
                label.setVisible(true);
            }

            label.applyCss();
            label.autosize();
            // Boundary checks
            if (x < 0) {
                x = 0;
            }
            double labelWidth = label.getWidth();
            double labelHeight = label.getHeight();
            if ((x + labelWidth + 5) > subScene.getWidth()) {
                x = subScene.getWidth() - (labelWidth + 5);
            }
            if (y < 0) {
                y = 0;
            }
            if ((y + labelHeight) > subScene.getHeight()) {
                y = subScene.getHeight() - (labelHeight + 5);
            }

            Rectangle2D bounds = new Rectangle2D(
                    x - LABEL_COLLISION_PADDING,
                    y - LABEL_COLLISION_PADDING,
                    labelWidth + (LABEL_COLLISION_PADDING * 2),
                    labelHeight + (LABEL_COLLISION_PADDING * 2));
            boolean collides = false;
            for (Rectangle2D occupiedBounds : occupied) {
                if (occupiedBounds.intersects(bounds)) {
                    collides = true;
                    break;
                }
            }
            if (collides) {
                label.setVisible(false);
                continue;
            }
            occupied.add(bounds);

            Point2D lastPosition = lastLabelPositions.get(node);
            if (lastPosition != null) {
                double dx = Math.abs(lastPosition.getX() - x);
                double dy = Math.abs(lastPosition.getY() - y);
                if (dx < LABEL_MOVE_EPSILON && dy < LABEL_MOVE_EPSILON) {
                    continue;
                }
            }

            // Use Translate transform - same as StarPlotManager
            label.getTransforms().setAll(new Translate(x, y));
            lastLabelPositions.put(node, new Point2D(x, y));
        }

        // Update moon orbit visibility based on current zoom level
        renderer.updateMoonOrbitVisibility(camera.getTranslateZ());
    }

    /**
     * Throttled version of updateLabels() for use during continuous interactions.
     * <p>
     * During mouse drag and scroll events, label updates can be called many times
     * per second. This method limits updates to at most one per {@link #LABEL_UPDATE_THROTTLE_MS}
     * milliseconds, reducing CPU usage while maintaining smooth visual feedback.
     */
    public void throttledUpdateLabels() {
        long now = System.currentTimeMillis();
        if (now - lastLabelUpdateTime >= LABEL_UPDATE_THROTTLE_MS) {
            lastLabelUpdateTime = now;
            updateLabels();
        }
    }

    /**
     * Toggle label visibility.
     *
     * @param labelsOn true to show labels
     */
    public void toggleLabels(boolean labelsOn) {
        this.labelsOn = labelsOn;
        labelDisplayGroup.setVisible(labelsOn);
    }

    /**
     * Check if labels are currently visible.
     *
     * @return true if labels are on
     */
    public boolean isLabelsOn() {
        return labelsOn;
    }

    /**
     * Clear the label display group.
     */
    public void clearLabels() {
        labelDisplayGroup.getChildren().clear();
        lastLabelPositions.clear();
    }

    /**
     * Create 2D labels for all rendered 3D objects (planets and stars).
     * Labels are added to the 2D labelDisplayGroup and registered with the renderer.
     *
     * @param solarSystemDescription the system being rendered
     */
    public void createLabelsForRenderedObjects(SolarSystemDescription solarSystemDescription) {
        Map<String, Sphere> planetNodes = renderer.getPlanetNodes();

        Map<String, PlanetDescription> planetsById = new HashMap<>();
        Map<String, Integer> moonCountsByParentId = new HashMap<>();

        if (solarSystemDescription != null && solarSystemDescription.getPlanetDescriptionList() != null) {
            for (PlanetDescription planet : solarSystemDescription.getPlanetDescriptionList()) {
                if (planet == null) {
                    continue;
                }
                planetsById.put(planet.getId(), planet);
                if (planet.isMoon() && planet.getParentPlanetId() != null) {
                    moonCountsByParentId.merge(planet.getParentPlanetId(), 1, Integer::sum);
                }
            }
        }

        // Create labels for planets/moons (skip moon labels if too close to parent)
        if (solarSystemDescription != null && solarSystemDescription.getPlanetDescriptionList() != null) {
            for (PlanetDescription planet : solarSystemDescription.getPlanetDescriptionList()) {
                if (planet == null) {
                    continue;
                }
                Sphere planetSphere = planetNodes.get(planet.getName());
                if (planetSphere == null) {
                    continue;
                }

                if (planet.isMoon()) {
                    PlanetDescription parent = planetsById.get(planet.getParentPlanetId());
                    if (parent != null) {
                        Sphere parentSphere = planetNodes.get(parent.getName());
                        if (parentSphere != null && isMoonLabelTooClose(planetSphere, parentSphere)) {
                            continue;
                        }
                    }
                }

                String labelText = planet.getName() != null ? planet.getName() : "Unknown";
                if (!planet.isMoon()) {
                    int moonCount = moonCountsByParentId.getOrDefault(planet.getId(), 0);
                    if (moonCount > 0) {
                        labelText = labelText + " (" + moonCount + ")";
                    }
                }

                Label label = renderer.createLabel(labelText);
                label.setLabelFor(planetSphere);
                labelDisplayGroup.getChildren().add(label);
                renderer.registerLabel(planetSphere, label);
            }
        }

        // Create label for the central star
        if (solarSystemDescription.getStarDisplayRecord() != null) {
            // Find the star sphere in the planets group (it's rendered there)
            Group planetsGroup = renderer.getPlanetsGroup();
            for (Node node : planetsGroup.getChildren()) {
                if (node.getUserData() instanceof StarDisplayRecord star) {
                    Label label = renderer.createLabel(star.getStarName());
                    label.setLabelFor(node);
                    labelDisplayGroup.getChildren().add(label);
                    renderer.registerLabel(node, label);
                    break; // Only label the primary star for now
                }
            }
        }

        log.debug("Created {} labels for solar system objects", renderer.getShapeToLabel().size());
    }

    /**
     * Check if a moon label would be too close to its parent planet.
     */
    private boolean isMoonLabelTooClose(Sphere moon, Sphere parent) {
        double dx = moon.getTranslateX() - parent.getTranslateX();
        double dy = moon.getTranslateY() - parent.getTranslateY();
        double dz = moon.getTranslateZ() - parent.getTranslateZ();
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        return distance < MIN_MOON_LABEL_DISTANCE;
    }

    /**
     * Internal record for sorting labels by distance to camera.
     */
    private record LabelCandidate(Node node, Label label, Point3D scenePoint, double distanceToCamera) {
    }
}
