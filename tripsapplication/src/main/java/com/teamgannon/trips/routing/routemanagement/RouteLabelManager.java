package com.teamgannon.trips.routing.routemanagement;

import com.teamgannon.trips.routing.RoutingConstants;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.transform.Translate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Manages 2D labels that track 3D anchor points in the route visualization.
 * <p>
 * This class implements the <b>billboard labels pattern</b> where 2D labels are
 * positioned to track 3D anchor points while remaining flat on screen. Labels
 * are stored in a separate group from the 3D scene to allow independent rendering.
 * <p>
 * <b>Architecture:</b>
 * <pre>
 * sceneRoot (2D)
 * ├── subScene (3D viewport)
 * │   └── world (3D group - rotates with camera)
 * │       └── Route cylinders and endpoint spheres
 * └── labelDisplayGroup (2D group - stays fixed)
 *     └── Distance labels (always flat to screen)
 * </pre>
 *
 * @see #updateLabels() for the coordinate transformation algorithm
 */
@Slf4j
public class RouteLabelManager {

    /**
     * Padding around labels for collision detection (pixels).
     */
    private static final double LABEL_COLLISION_PADDING = 4.0;

    /**
     * Maps 3D anchor nodes to their associated 2D labels.
     */
    private final Map<Node, Label> shapeToLabel = new HashMap<>();

    /**
     * Reverse lookup: maps labels back to their 3D anchor nodes.
     */
    private final Map<Label, Node> reverseLabelLookup = new HashMap<>();

    /**
     * The group containing all route labels (added to scene root, not 3D world).
     */
    @Getter
    private final Group labelDisplayGroup = new Group();

    /**
     * The 3D subscene for coordinate transformation calculations.
     */
    private final SubScene subScene;

    /**
     * Supplier for viewport bounds (typically from InterstellarSpacePane).
     */
    private final Supplier<Bounds> boundsSupplier;

    /**
     * Vertical offset to account for control panes at the top of the view.
     */
    private double controlPaneOffset = 0;

    /**
     * Creates a new RouteLabelManager.
     *
     * @param subScene       the 3D subscene for coordinate calculations
     * @param boundsSupplier supplier for viewport bounds (e.g., {@code pane::getBoundsInParent})
     */
    public RouteLabelManager(SubScene subScene, Supplier<Bounds> boundsSupplier) {
        this.subScene = subScene;
        this.boundsSupplier = boundsSupplier;
    }

    /**
     * Sets the vertical offset for control panes at the top of the viewport.
     *
     * @param offset the offset in pixels
     */
    public void setControlPaneOffset(double offset) {
        this.controlPaneOffset = offset;
    }

    /**
     * Links a 3D anchor node to a 2D label.
     * <p>
     * The label is added to the label display group and tracked for position updates.
     *
     * @param anchorNode the 3D node that the label should track
     * @param label      the 2D label to display
     */
    public void linkLabel(Node anchorNode, Label label) {
        labelDisplayGroup.getChildren().add(label);
        shapeToLabel.put(anchorNode, label);
        reverseLabelLookup.put(label, anchorNode);
    }

    /**
     * Checks if a label is already registered.
     *
     * @param label the label to check
     * @return true if the label is already managed
     */
    public boolean containsLabel(Label label) {
        return shapeToLabel.containsValue(label);
    }

    /**
     * Removes the label associated with a 3D anchor node.
     *
     * @param anchorNode the anchor node whose label should be removed
     */
    public void removeLabelForNode(Node anchorNode) {
        Label label = shapeToLabel.remove(anchorNode);
        if (label != null) {
            reverseLabelLookup.remove(label);
            labelDisplayGroup.getChildren().remove(label);
        }
    }

    /**
     * Removes a label and its association with the anchor node.
     *
     * @param label the label to remove
     */
    public void removeLabel(Label label) {
        Node node = reverseLabelLookup.remove(label);
        if (node != null) {
            shapeToLabel.remove(node);
        }
        labelDisplayGroup.getChildren().remove(label);
    }

    /**
     * Clears all labels and their associations.
     */
    public void clear() {
        shapeToLabel.clear();
        reverseLabelLookup.clear();
        labelDisplayGroup.getChildren().clear();
    }

    /**
     * Sets the visibility of all labels.
     *
     * @param visible true to show labels, false to hide
     */
    public void setLabelsVisible(boolean visible) {
        labelDisplayGroup.setVisible(visible);
    }

    /**
     * Updates all label positions to match current 3D anchor positions.
     * <p>
     * This method should be called after:
     * <ul>
     *   <li>Mouse drag (rotation)</li>
     *   <li>Scroll (zoom)</li>
     *   <li>Initial render</li>
     * </ul>
     * <p>
     * <b>Coordinate Transformation Algorithm:</b>
     * <ol>
     *   <li>Project 3D anchor position to scene coordinates using {@code localToScene()}</li>
     *   <li>Check if position is within visible viewport bounds</li>
     *   <li>Adjust coordinates relative to viewport origin</li>
     *   <li>Clamp coordinates to prevent labels going off-screen</li>
     *   <li>Apply position via {@link Translate} transform</li>
     * </ol>
     * <p>
     * Includes collision detection: labels are sorted by depth and overlapping labels
     * are hidden with priority given to labels closer to the camera.
     *
     * @see RoutingConstants#LABEL_CLIPPING_PADDING
     * @see RoutingConstants#LABEL_EDGE_MARGIN
     */
    public void updateLabels() {
        if (shapeToLabel.isEmpty()) {
            return;
        }

        final Bounds viewportBounds = boundsSupplier.get();

        // Phase 1: Build candidate list with screen positions and depths
        List<LabelCandidate> candidates = new ArrayList<>(shapeToLabel.size());

        for (Map.Entry<Node, Label> entry : shapeToLabel.entrySet()) {
            Node anchorNode = entry.getKey();
            Label label = entry.getValue();

            // Project 3D position to scene coordinates
            Point3D sceneCoords = anchorNode.localToScene(Point3D.ZERO, true);

            // Skip if NaN
            if (Double.isNaN(sceneCoords.getX()) || Double.isNaN(sceneCoords.getY())) {
                label.setVisible(false);
                continue;
            }

            double sceneX = sceneCoords.getX();
            double sceneY = sceneCoords.getY();

            // Check visibility within viewport
            if (!isWithinViewport(sceneX, sceneY, viewportBounds)) {
                label.setVisible(false);
                continue;
            }

            // Distance to camera (Z coordinate)
            double distanceToCamera = sceneCoords.getZ();
            candidates.add(new LabelCandidate(anchorNode, label, sceneCoords, distanceToCamera));
        }

        // Phase 2: Sort by distance (closest to camera first = highest priority)
        candidates.sort((a, b) -> Double.compare(a.distanceToCamera(), b.distanceToCamera()));

        // Phase 3: Process with collision detection
        List<Rectangle2D> occupied = new ArrayList<>();

        for (LabelCandidate candidate : candidates) {
            Label label = candidate.label();
            Point3D sceneCoords = candidate.scenePoint();

            double sceneX = sceneCoords.getX();
            double sceneY = sceneCoords.getY();

            // Convert scene coordinates to local viewport coordinates
            double localX = toLocalX(sceneX, viewportBounds);
            double localY = toLocalY(sceneY, viewportBounds);

            // Measure label dimensions
            label.applyCss();
            label.autosize();
            double labelWidth = label.getWidth();
            double labelHeight = label.getHeight();

            // Clamp to prevent going off-screen
            double clampedX = clampX(localX, labelWidth);
            double clampedY = clampY(localY, labelHeight);

            // Create bounds rectangle with padding for collision detection
            Rectangle2D bounds = new Rectangle2D(
                    clampedX - LABEL_COLLISION_PADDING,
                    clampedY - LABEL_COLLISION_PADDING,
                    labelWidth + (LABEL_COLLISION_PADDING * 2),
                    labelHeight + (LABEL_COLLISION_PADDING * 2));

            // Check for collision with already-placed labels
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

            // No collision - show label and track its bounds
            label.setVisible(true);
            occupied.add(bounds);

            // Apply position
            label.getTransforms().setAll(new Translate(clampedX, clampedY));
        }
    }

    /**
     * Internal record for sorting labels by distance to camera.
     */
    private record LabelCandidate(
            Node node,
            Label label,
            Point3D scenePoint,
            double distanceToCamera
    ) {}

    /**
     * Checks if a point is within the visible viewport bounds.
     *
     * @param sceneX         X coordinate in scene space
     * @param sceneY         Y coordinate in scene space
     * @param viewportBounds the viewport bounds
     * @return true if the point is visible
     */
    private boolean isWithinViewport(double sceneX, double sceneY, Bounds viewportBounds) {
        double padding = RoutingConstants.LABEL_CLIPPING_PADDING;

        boolean withinX = sceneX >= (viewportBounds.getMinX() + padding)
                && sceneX <= (viewportBounds.getMaxX() - padding);
        boolean withinY = sceneY >= (controlPaneOffset + padding)
                && sceneY <= (viewportBounds.getMaxY() - padding);

        return withinX && withinY;
    }

    /**
     * Converts scene X coordinate to local viewport X coordinate.
     *
     * @param sceneX         X coordinate in scene space
     * @param viewportBounds the viewport bounds
     * @return X coordinate relative to viewport origin
     */
    private double toLocalX(double sceneX, Bounds viewportBounds) {
        if (viewportBounds.getMinX() > 0) {
            return sceneX - viewportBounds.getMinX();
        }
        return sceneX;
    }

    /**
     * Converts scene Y coordinate to local viewport Y coordinate.
     *
     * @param sceneY         Y coordinate in scene space
     * @param viewportBounds the viewport bounds
     * @return Y coordinate relative to viewport origin, adjusted for control pane offset
     */
    private double toLocalY(double sceneY, Bounds viewportBounds) {
        if (viewportBounds.getMinY() >= 0) {
            return sceneY - viewportBounds.getMinY() - controlPaneOffset;
        }
        // Handle negative viewport origin (edge case)
        return sceneY < 0 ? sceneY - controlPaneOffset : sceneY + controlPaneOffset;
    }

    /**
     * Clamps X coordinate to keep label within viewport.
     *
     * @param x          the X coordinate
     * @param labelWidth the label width
     * @return clamped X coordinate
     */
    private double clampX(double x, double labelWidth) {
        double margin = RoutingConstants.LABEL_EDGE_MARGIN;

        // Clamp to left edge
        if (x < 0) {
            return 0;
        }

        // Clamp to right edge
        double maxX = subScene.getWidth() - labelWidth - margin;
        if (x > maxX) {
            return maxX;
        }

        return x;
    }

    /**
     * Clamps Y coordinate to keep label within viewport.
     *
     * @param y           the Y coordinate
     * @param labelHeight the label height
     * @return clamped Y coordinate
     */
    private double clampY(double y, double labelHeight) {
        double margin = RoutingConstants.LABEL_EDGE_MARGIN;

        // Clamp to top edge
        if (y < 0) {
            return 0;
        }

        // Clamp to bottom edge
        double maxY = subScene.getHeight() - labelHeight - margin;
        if (y > maxY) {
            return maxY;
        }

        return y;
    }
}
