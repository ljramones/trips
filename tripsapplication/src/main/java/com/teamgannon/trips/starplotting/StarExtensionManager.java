package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.graphics.entities.CustomObjectFactory;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * Manages extension lines from the grid plane to stars.
 * <p>
 * Extension lines are vertical lines that show the height of each star
 * above or below the reference plane (typically z=0). They help users
 * visualize the 3D position of stars in the interstellar view.
 * <p>
 * This class handles:
 * <ul>
 *   <li>Creating extension lines with appropriate styling</li>
 *   <li>Managing the extensions Group for scene graph</li>
 *   <li>Visibility toggling</li>
 *   <li>Clearing all extensions</li>
 * </ul>
 */
@Slf4j
public class StarExtensionManager {

    // =========================================================================
    // Constants
    // =========================================================================

    /**
     * Default width of extension lines.
     */
    private static final double DEFAULT_LINE_WIDTH = 0.3;

    // =========================================================================
    // State
    // =========================================================================

    /**
     * The Group containing all extension line nodes.
     * Added to the 3D world group.
     */
    @Getter
    private final Group extensionsGroup = new Group();

    /**
     * Whether extensions are currently visible.
     */
    @Getter
    private boolean extensionsVisible = true;

    /**
     * The Z coordinate of the reference plane (typically the grid).
     */
    private double referenceZ = 0.0;

    /**
     * Count of extensions for statistics.
     */
    @Getter
    private int extensionCount = 0;

    // =========================================================================
    // Initialization
    // =========================================================================

    /**
     * Initialize the extension manager by adding the extensions group to the world.
     *
     * @param world the 3D world Group to add extensions to
     */
    public void initialize(@NotNull Group world) {
        world.getChildren().add(extensionsGroup);
    }

    /**
     * Set the reference Z coordinate (the grid plane level).
     * Extensions are drawn from the star to this Z level.
     *
     * @param z the reference Z coordinate
     */
    public void setReferenceZ(double z) {
        this.referenceZ = z;
    }

    // =========================================================================
    // Extension Creation
    // =========================================================================

    /**
     * Create an extension line for a star.
     * The extension connects the star's position to the reference plane.
     *
     * @param record       the star display record
     * @param colorPalette the color palette for styling
     */
    public void createExtension(@NotNull StarDisplayRecord record,
                                 @NotNull ColorPalette colorPalette) {
        Point3D starPosition = record.getCoordinates();
        createExtension(
                starPosition.getX(),
                starPosition.getY(),
                starPosition.getZ(),
                colorPalette.getExtensionColor(),
                colorPalette.getStemLineWidth(),
                colorPalette.getLabelFont().toFont()
        );
    }

    /**
     * Create an extension line from a point to the reference plane.
     *
     * @param x         the X coordinate
     * @param y         the Y coordinate
     * @param z         the Z coordinate (star height)
     * @param color     the extension line color
     * @param lineWidth the line width
     * @param font      the font (used for line segment creation)
     */
    public void createExtension(double x, double y, double z,
                                 @NotNull Color color,
                                 double lineWidth,
                                 @NotNull Font font) {
        Point3D from = new Point3D(x, y, z);
        Point3D to = new Point3D(x, y, referenceZ);

        Node lineSegment = CustomObjectFactory.createLineSegment(
                from, to, lineWidth, color, font);

        extensionsGroup.getChildren().add(lineSegment);
        extensionCount++;

        if (extensionsVisible) {
            extensionsGroup.setVisible(true);
        }
    }

    /**
     * Create an extension line with default line width.
     *
     * @param x     the X coordinate
     * @param y     the Y coordinate
     * @param z     the Z coordinate (star height)
     * @param color the extension line color
     * @param font  the font (used for line segment creation)
     */
    public void createExtension(double x, double y, double z,
                                 @NotNull Color color,
                                 @NotNull Font font) {
        createExtension(x, y, z, color, DEFAULT_LINE_WIDTH, font);
    }

    // =========================================================================
    // Visibility Control
    // =========================================================================

    /**
     * Set extension visibility.
     *
     * @param visible true to show extensions, false to hide
     */
    public void setExtensionsVisible(boolean visible) {
        this.extensionsVisible = visible;
        extensionsGroup.setVisible(visible);
    }

    /**
     * Toggle extension visibility.
     *
     * @param visible true to show extensions
     */
    public void toggleExtensions(boolean visible) {
        setExtensionsVisible(visible);
    }

    // =========================================================================
    // Cleanup
    // =========================================================================

    /**
     * Clear all extension lines.
     * Call this when clearing the star plot or switching datasets.
     */
    public void clear() {
        extensionsGroup.getChildren().clear();
        extensionCount = 0;
    }

    // =========================================================================
    // Statistics
    // =========================================================================

    /**
     * Reset the extension count.
     */
    public void resetStatistics() {
        extensionCount = 0;
    }

    /**
     * Log extension statistics.
     */
    public void logStatistics() {
        log.info("Extension Statistics: count={}, visible={}",
                extensionCount, extensionsVisible);
    }
}
