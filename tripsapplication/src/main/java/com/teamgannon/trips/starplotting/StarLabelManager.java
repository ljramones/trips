package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.SerialFont;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.transform.Translate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages star labels for the interstellar space view.
 * <p>
 * This class handles:
 * <ul>
 *   <li>Creating labels with appropriate fonts and colors</li>
 *   <li>Positioning labels to track their associated 3D nodes (billboard effect)</li>
 *   <li>Visibility toggling</li>
 *   <li>Clipping labels that would extend outside the viewport</li>
 * </ul>
 * <p>
 * Labels are 2D elements that overlay the 3D scene. They're added to a separate
 * Group (labelDisplayGroup) that's a sibling of the SubScene, not part of the
 * 3D world. This allows them to always face the camera (billboard style).
 * <p>
 * The {@link #updateLabels} method must be called after any camera movement
 * (rotation, zoom, pan) to reposition labels to match their 3D node positions.
 */
@Slf4j
public class StarLabelManager {

    // =========================================================================
    // Constants
    // =========================================================================

    /**
     * Margin from viewport edge before labels are hidden (pixels).
     */
    private static final double VIEWPORT_MARGIN = 20.0;

    /**
     * Padding between label and viewport edge when clamping (pixels).
     */
    private static final double LABEL_PADDING = 5.0;

    /**
     * Padding around labels for collision detection (pixels).
     */
    private static final double LABEL_COLLISION_PADDING = 4.0;

    /**
     * Minimum movement threshold to avoid unnecessary redraws (pixels).
     */
    private static final double LABEL_MOVE_EPSILON = 0.5;

    /**
     * Minimum interval between label updates during continuous interactions (ms).
     * ~60fps = 16ms.
     */
    private static final long LABEL_UPDATE_THROTTLE_MS = 16;

    // =========================================================================
    // State
    // =========================================================================

    /**
     * The Group that contains all label nodes.
     * This is added to the scene root (2D overlay), not the 3D world.
     */
    @Getter
    private final Group labelDisplayGroup = new Group();

    /**
     * Maps 3D nodes (stars) to their associated labels.
     * Used by updateLabels() to reposition labels when the view changes.
     */
    private final Map<Node, Label> shapeToLabel = new HashMap<>();

    /**
     * Whether labels are currently visible.
     */
    @Getter
    private boolean labelsVisible = true;

    /**
     * Reference to the SubScene for bounds calculations.
     */
    private SubScene subScene;

    /**
     * Offset from top of viewport (e.g., for toolbar).
     */
    private double controlPaneOffset = 0;

    /**
     * Timestamp of last label update (for throttling).
     */
    private long lastLabelUpdateTime = 0;

    /**
     * Cache of last label positions to avoid unnecessary transforms.
     */
    private final Map<Node, Point2D> lastLabelPositions = new HashMap<>();

    // =========================================================================
    // Initialization
    // =========================================================================

    /**
     * Configure the label manager with scene references.
     *
     * @param sceneRoot the root Group for 2D overlays
     * @param subScene  the 3D SubScene for bounds calculations
     */
    public void initialize(@NotNull Group sceneRoot, @NotNull SubScene subScene) {
        this.subScene = subScene;
        sceneRoot.getChildren().add(labelDisplayGroup);
    }

    /**
     * Set the control pane offset (space at top of viewport for toolbar).
     *
     * @param offset pixels to offset from top
     */
    public void setControlPaneOffset(double offset) {
        this.controlPaneOffset = offset;
    }

    // =========================================================================
    // Label Creation
    // =========================================================================

    /**
     * Create a label for a star.
     *
     * @param record       the star record containing the name
     * @param colorPalette the color palette for font and color settings
     * @return the created label
     */
    public @NotNull Label createLabel(@NotNull StarDisplayRecord record,
                                       @NotNull ColorPalette colorPalette) {
        Label label = new Label(record.getStarName());
        SerialFont serialFont = colorPalette.getLabelFont();
        label.setFont(serialFont.toFont());
        label.setTextFill(colorPalette.getLabelColor());
        return label;
    }

    /**
     * Create and register a label for a star node.
     * The label is added to the display group and mapped to the node.
     *
     * @param starNode     the 3D node representing the star
     * @param record       the star record containing the name
     * @param colorPalette the color palette for styling
     * @return the created label
     */
    public @NotNull Label addLabel(@NotNull Node starNode,
                                    @NotNull StarDisplayRecord record,
                                    @NotNull ColorPalette colorPalette) {
        Label label = createLabel(record, colorPalette);
        label.setLabelFor(starNode);
        labelDisplayGroup.getChildren().add(label);
        shapeToLabel.put(starNode, label);
        return label;
    }

    /**
     * Register an existing label with a node.
     * Used for test/debug purposes where label is created externally.
     *
     * @param node  the 3D node
     * @param label the label to register
     */
    public void registerLabel(@NotNull Node node, @NotNull Label label) {
        labelDisplayGroup.getChildren().add(label);
        shapeToLabel.put(node, label);
    }

    /**
     * Get the label associated with a star node.
     *
     * @param starNode the 3D node
     * @return the associated label, or null if none exists
     */
    public @Nullable Label getLabel(@NotNull Node starNode) {
        return shapeToLabel.get(starNode);
    }

    /**
     * Remove the label for a specific star node.
     *
     * @param starNode the 3D node whose label should be removed
     */
    public void removeLabel(@NotNull Node starNode) {
        Label label = shapeToLabel.remove(starNode);
        if (label != null) {
            labelDisplayGroup.getChildren().remove(label);
        }
    }

    // =========================================================================
    // Visibility Control
    // =========================================================================

    /**
     * Toggle label visibility.
     *
     * @param visible true to show labels, false to hide
     */
    public void setLabelsVisible(boolean visible) {
        this.labelsVisible = visible;
        labelDisplayGroup.setVisible(visible);
    }

    /**
     * Toggle labels on/off.
     *
     * @param visible true to show labels
     */
    public void toggleLabels(boolean visible) {
        setLabelsVisible(visible);
    }

    // =========================================================================
    // Label Positioning (Billboard Effect)
    // =========================================================================

    /**
     * Update all label positions based on their associated 3D node positions.
     * <p>
     * This method projects each 3D node's position to 2D screen coordinates
     * and positions the label accordingly. Labels that would extend outside
     * the viewport are either hidden or clamped to the edge.
     * <p>
     * Includes collision detection: labels are sorted by depth (camera distance),
     * and overlapping labels are hidden with priority given to closer stars.
     * <p>
     * Call this method after any camera movement (rotation, zoom, pan).
     *
     * @param parentBounds the bounds of the parent container
     */
    public void updateLabels(@NotNull Bounds parentBounds) {
        if (subScene == null) {
            log.warn("SubScene not initialized, cannot update labels");
            return;
        }

        if (shapeToLabel.isEmpty()) {
            return;
        }

        // Phase 1: Build candidate list with screen positions and depths
        List<LabelCandidate> candidates = new ArrayList<>(shapeToLabel.size());
        for (Map.Entry<Node, Label> entry : shapeToLabel.entrySet()) {
            Node node = entry.getKey();
            Label label = entry.getValue();

            // Skip nodes with invalid coordinates (NaN check)
            if (Double.isNaN(node.getTranslateX())) {
                label.setVisible(false);
                continue;
            }

            // Project 3D position to 2D screen coordinates
            Point3D coordinates = node.localToScene(Point3D.ZERO, true);

            // Skip if localToScene returned NaN
            if (Double.isNaN(coordinates.getX()) || Double.isNaN(coordinates.getY())) {
                label.setVisible(false);
                continue;
            }

            double xs = coordinates.getX();
            double ys = coordinates.getY();

            // Skip labels outside viewport (with margin)
            if (xs < (parentBounds.getMinX() + VIEWPORT_MARGIN) ||
                xs > (parentBounds.getMaxX() - VIEWPORT_MARGIN)) {
                label.setVisible(false);
                continue;
            }

            if (ys < (controlPaneOffset + VIEWPORT_MARGIN) ||
                ys > (parentBounds.getMaxY() - VIEWPORT_MARGIN)) {
                label.setVisible(false);
                continue;
            }

            // Distance to camera (Z coordinate - closer = smaller value)
            double distanceToCamera = coordinates.getZ();
            candidates.add(new LabelCandidate(node, label, coordinates, distanceToCamera));
        }

        // Phase 2: Sort by distance (closest to camera first = highest priority)
        candidates.sort((a, b) -> Double.compare(a.distanceToCamera(), b.distanceToCamera()));

        // Phase 3: Process with collision detection
        List<Rectangle2D> occupied = new ArrayList<>();

        for (LabelCandidate candidate : candidates) {
            Node node = candidate.node();
            Label label = candidate.label();
            Point3D coordinates = candidate.scenePoint();

            double xs = coordinates.getX();
            double ys = coordinates.getY();

            // Convert from scene coordinates to local coordinates
            double x = parentBounds.getMinX() > 0 ? xs - parentBounds.getMinX() : xs;

            double y;
            if (parentBounds.getMinY() >= 0) {
                y = ys - parentBounds.getMinY() - controlPaneOffset;
            } else {
                y = ys < 0 ? ys - controlPaneOffset : ys + controlPaneOffset;
            }

            // Measure label dimensions
            label.applyCss();
            label.autosize();
            double labelWidth = label.getWidth();
            double labelHeight = label.getHeight();

            // Clamp to viewport bounds
            x = clampX(x, labelWidth);
            y = clampY(y, labelHeight);

            // Create bounds rectangle with padding for collision detection
            Rectangle2D bounds = new Rectangle2D(
                    x - LABEL_COLLISION_PADDING,
                    y - LABEL_COLLISION_PADDING,
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

            // Check if position changed enough to warrant redraw
            Point2D lastPosition = lastLabelPositions.get(node);
            if (lastPosition != null) {
                double dx = Math.abs(lastPosition.getX() - x);
                double dy = Math.abs(lastPosition.getY() - y);
                if (dx < LABEL_MOVE_EPSILON && dy < LABEL_MOVE_EPSILON) {
                    continue;
                }
            }

            // Apply position via transform
            label.getTransforms().setAll(new Translate(x, y));
            lastLabelPositions.put(node, new Point2D(x, y));
        }
    }

    /**
     * Throttled version of updateLabels() for use during continuous interactions.
     * <p>
     * During mouse drag and scroll events, label updates can be called many times
     * per second. This method limits updates to at most one per {@link #LABEL_UPDATE_THROTTLE_MS}
     * milliseconds, reducing CPU usage while maintaining smooth visual feedback.
     *
     * @param parentBounds the bounds of the parent container
     */
    public void throttledUpdateLabels(@NotNull Bounds parentBounds) {
        long now = System.currentTimeMillis();
        if (now - lastLabelUpdateTime >= LABEL_UPDATE_THROTTLE_MS) {
            lastLabelUpdateTime = now;
            updateLabels(parentBounds);
        }
    }

    /**
     * Clamp X coordinate to keep label within viewport.
     */
    private double clampX(double x, double labelWidth) {
        if (x < 0) {
            return 0;
        }
        if (subScene != null && (x + labelWidth + LABEL_PADDING) > subScene.getWidth()) {
            return subScene.getWidth() - (labelWidth + LABEL_PADDING);
        }
        return x;
    }

    /**
     * Clamp Y coordinate to keep label within viewport.
     */
    private double clampY(double y, double labelHeight) {
        if (y < 0) {
            return 0;
        }
        if (subScene != null && (y + labelHeight) > subScene.getHeight()) {
            return subScene.getHeight() - (labelHeight + LABEL_PADDING);
        }
        return y;
    }

    // =========================================================================
    // Cleanup
    // =========================================================================

    /**
     * Clear all labels.
     * Call this when switching datasets or clearing the plot.
     */
    public void clear() {
        labelDisplayGroup.getChildren().clear();
        shapeToLabel.clear();
        lastLabelPositions.clear();
    }

    /**
     * Get the number of labels currently managed.
     *
     * @return label count
     */
    public int getLabelCount() {
        return shapeToLabel.size();
    }

    /**
     * Check if a label exists for a given node.
     *
     * @param node the 3D node to check
     * @return true if a label exists
     */
    public boolean hasLabel(@NotNull Node node) {
        return shapeToLabel.containsKey(node);
    }

    // =========================================================================
    // Internal Records
    // =========================================================================

    /**
     * Internal record for sorting labels by distance to camera.
     * Holds all data needed to position a label during collision detection.
     *
     * @param node             the 3D node the label is attached to
     * @param label            the Label node to position
     * @param scenePoint       the 3D node's position in scene coordinates
     * @param distanceToCamera the Z-distance from the camera (for sorting)
     */
    private record LabelCandidate(
            Node node,
            Label label,
            Point3D scenePoint,
            double distanceToCamera
    ) {}
}
