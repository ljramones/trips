package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.SerialFont;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
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

    /**
     * Maximum characters to display in a label before truncating.
     */
    private static final int MAX_LABEL_LENGTH = 18;

    /**
     * Ellipsis appended to truncated labels.
     */
    private static final String ELLIPSIS = "...";

    /**
     * Default camera Z position (used as baseline for font scaling).
     */
    private static final double BASE_CAMERA_Z = -1600.0;

    /**
     * Minimum font scale factor (when zoomed out).
     */
    private static final double MIN_FONT_SCALE = 0.7;

    /**
     * Maximum font scale factor (when zoomed in).
     */
    private static final double MAX_FONT_SCALE = 1.3;

    /**
     * Default label background color (semi-transparent dark).
     */
    private static final String LABEL_BACKGROUND_COLOR = "rgba(0, 0, 0, 0.6)";

    /**
     * Label padding for background.
     */
    private static final String LABEL_BACKGROUND_PADDING = "2px 4px";

    /**
     * Label corner radius.
     */
    private static final String LABEL_BACKGROUND_RADIUS = "3px";

    /**
     * Hover highlight color for labels.
     */
    private static final String LABEL_HOVER_BACKGROUND = "rgba(70, 130, 180, 0.8)";

    /**
     * Search highlight color (bright yellow-gold).
     */
    private static final String LABEL_SEARCH_HIGHLIGHT = "rgba(255, 215, 0, 0.9)";

    /**
     * Duration of one pulse cycle in milliseconds.
     */
    private static final double PULSE_CYCLE_MS = 500;

    /**
     * Number of pulse cycles for search highlight.
     */
    private static final int PULSE_CYCLES = 6;

    /**
     * Camera Z range over which font scaling is applied.
     * Scaling transitions from MIN to MAX over this range centered on BASE_CAMERA_Z.
     */
    private static final double FONT_SCALE_RANGE = 1600.0;

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

    /**
     * Base font name from color palette.
     */
    private String baseFontName = "System";

    /**
     * Base font size from color palette.
     */
    private double baseFontSize = 12.0;

    /**
     * Current camera Z position for font scaling.
     */
    private double currentCameraZ = BASE_CAMERA_Z;

    /**
     * Last applied font scale to avoid unnecessary font updates.
     */
    private double lastFontScale = 1.0;

    /**
     * Currently running search highlight animation.
     */
    private Timeline currentHighlightAnimation;

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

    /**
     * Set the current camera Z position for font scaling calculations.
     * <p>
     * When the camera is closer (more negative Z), labels are scaled larger.
     * When the camera is farther (less negative Z), labels are scaled smaller.
     *
     * @param cameraZ the camera's Z translation value
     */
    public void setCameraZ(double cameraZ) {
        this.currentCameraZ = cameraZ;
    }

    /**
     * Calculate the font scale factor based on current camera Z position.
     * <p>
     * Scale ranges from MIN_FONT_SCALE (zoomed out) to MAX_FONT_SCALE (zoomed in).
     *
     * @return scale factor to apply to base font size
     */
    private double calculateFontScale() {
        // How far from base position? Positive = zoomed in, negative = zoomed out
        double zOffset = BASE_CAMERA_Z - currentCameraZ;

        // Normalize to -1..+1 range
        double normalized = zOffset / FONT_SCALE_RANGE;

        // Clamp and map to scale range
        normalized = Math.max(-1.0, Math.min(1.0, normalized));

        // Map to MIN_FONT_SCALE..MAX_FONT_SCALE
        double midScale = (MIN_FONT_SCALE + MAX_FONT_SCALE) / 2.0;
        double scaleRange = (MAX_FONT_SCALE - MIN_FONT_SCALE) / 2.0;

        return midScale + (normalized * scaleRange);
    }

    /**
     * Apply font scaling to all labels based on current camera position.
     * Only updates fonts if the scale has changed significantly.
     */
    private void applyFontScaling() {
        double newScale = calculateFontScale();

        // Only update if scale changed significantly (avoid unnecessary font recreations)
        if (Math.abs(newScale - lastFontScale) < 0.02) {
            return;
        }

        lastFontScale = newScale;
        double scaledSize = baseFontSize * newScale;
        Font scaledFont = Font.font(baseFontName, scaledSize);

        for (Label label : shapeToLabel.values()) {
            label.setFont(scaledFont);
        }
    }

    // =========================================================================
    // Label Creation
    // =========================================================================

    /**
     * Create a label for a star.
     * <p>
     * Long star names are truncated with ellipsis, and a tooltip shows the full name.
     * Labels have a semi-transparent background for better readability and
     * highlight on hover.
     *
     * @param record       the star record containing the name
     * @param colorPalette the color palette for font and color settings
     * @return the created label
     */
    public @NotNull Label createLabel(@NotNull StarDisplayRecord record,
                                       @NotNull ColorPalette colorPalette) {
        String fullName = record.getStarName();
        String displayName = truncateName(fullName);

        Label label = new Label(displayName);
        SerialFont serialFont = colorPalette.getLabelFont();

        // Store base font settings for scaling
        baseFontName = serialFont.getName();
        baseFontSize = serialFont.getSize();

        label.setFont(serialFont.toFont());
        label.setTextFill(colorPalette.getLabelColor());

        // Add semi-transparent background for readability
        applyLabelBackground(label);

        // Add hover highlight effect
        setupLabelHoverEffect(label);

        // Add tooltip for truncated names
        if (!displayName.equals(fullName)) {
            Tooltip tooltip = new Tooltip(fullName);
            Tooltip.install(label, tooltip);
        }

        return label;
    }

    /**
     * Apply semi-transparent background styling to a label.
     *
     * @param label the label to style
     */
    private void applyLabelBackground(@NotNull Label label) {
        label.setStyle(
                "-fx-background-color: " + LABEL_BACKGROUND_COLOR + ";" +
                "-fx-padding: " + LABEL_BACKGROUND_PADDING + ";" +
                "-fx-background-radius: " + LABEL_BACKGROUND_RADIUS + ";"
        );
    }

    /**
     * Set up hover highlight effect on a label.
     *
     * @param label the label to add hover effect to
     */
    private void setupLabelHoverEffect(@NotNull Label label) {
        String normalStyle =
                "-fx-background-color: " + LABEL_BACKGROUND_COLOR + ";" +
                "-fx-padding: " + LABEL_BACKGROUND_PADDING + ";" +
                "-fx-background-radius: " + LABEL_BACKGROUND_RADIUS + ";";

        String hoverStyle =
                "-fx-background-color: " + LABEL_HOVER_BACKGROUND + ";" +
                "-fx-padding: " + LABEL_BACKGROUND_PADDING + ";" +
                "-fx-background-radius: " + LABEL_BACKGROUND_RADIUS + ";";

        label.setOnMouseEntered(e -> label.setStyle(hoverStyle));
        label.setOnMouseExited(e -> label.setStyle(normalStyle));
    }

    // =========================================================================
    // Search Highlight Animation
    // =========================================================================

    /**
     * Highlight a label with a pulsing animation to draw attention to it.
     * Used when a star is found via search.
     *
     * @param starNode the star node whose label should be highlighted
     */
    public void pulseHighlightLabel(@NotNull Node starNode) {
        Label label = shapeToLabel.get(starNode);
        if (label == null) {
            log.warn("No label found for star node to highlight");
            return;
        }

        pulseHighlightLabel(label);
    }

    /**
     * Highlight a label with a pulsing animation.
     *
     * @param label the label to highlight
     */
    public void pulseHighlightLabel(@NotNull Label label) {
        // Stop any existing animation
        stopHighlightAnimation();

        // Make sure label is visible
        label.setVisible(true);

        // Create glow effect for pulsing
        Glow glow = new Glow(0);
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.GOLD);
        dropShadow.setRadius(10);
        dropShadow.setSpread(0.5);
        glow.setInput(dropShadow);
        label.setEffect(glow);

        // Set highlight background
        String highlightStyle =
                "-fx-background-color: " + LABEL_SEARCH_HIGHLIGHT + ";" +
                "-fx-padding: " + LABEL_BACKGROUND_PADDING + ";" +
                "-fx-background-radius: " + LABEL_BACKGROUND_RADIUS + ";";
        label.setStyle(highlightStyle);

        // Create pulsing animation
        currentHighlightAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(glow.levelProperty(), 0.0),
                        new KeyValue(label.scaleXProperty(), 1.0),
                        new KeyValue(label.scaleYProperty(), 1.0)),
                new KeyFrame(Duration.millis(PULSE_CYCLE_MS / 2),
                        new KeyValue(glow.levelProperty(), 0.8),
                        new KeyValue(label.scaleXProperty(), 1.2),
                        new KeyValue(label.scaleYProperty(), 1.2)),
                new KeyFrame(Duration.millis(PULSE_CYCLE_MS),
                        new KeyValue(glow.levelProperty(), 0.0),
                        new KeyValue(label.scaleXProperty(), 1.0),
                        new KeyValue(label.scaleYProperty(), 1.0))
        );

        currentHighlightAnimation.setCycleCount(PULSE_CYCLES);
        currentHighlightAnimation.setOnFinished(e -> {
            // Restore normal appearance
            label.setEffect(null);
            label.setScaleX(1.0);
            label.setScaleY(1.0);
            applyLabelBackground(label);
            currentHighlightAnimation = null;
        });

        currentHighlightAnimation.play();
        log.info("Started pulse highlight animation for label: {}", label.getText());
    }

    /**
     * Stop the current highlight animation if running.
     */
    public void stopHighlightAnimation() {
        if (currentHighlightAnimation != null) {
            currentHighlightAnimation.stop();
            currentHighlightAnimation = null;
        }
    }

    /**
     * Truncate a star name if it exceeds the maximum length.
     *
     * @param name the full star name
     * @return truncated name with ellipsis, or original if short enough
     */
    private @NotNull String truncateName(@NotNull String name) {
        if (name.length() <= MAX_LABEL_LENGTH) {
            return name;
        }
        return name.substring(0, MAX_LABEL_LENGTH - ELLIPSIS.length()) + ELLIPSIS;
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

        // Apply font scaling based on current zoom level
        applyFontScaling();

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
