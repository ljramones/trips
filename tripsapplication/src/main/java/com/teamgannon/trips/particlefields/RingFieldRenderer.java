package com.teamgannon.trips.particlefields;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.CullFace;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.fxyz3d.geometry.Point3D;
import org.fxyz3d.shapes.primitives.ScatterMesh;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Renders ring/particle field elements to a JavaFX Group.
 * This class is decoupled from window management, allowing rings to be
 * rendered in any 3D scene (standalone window, solar system view, interstellar view).
 *
 * <p>Usage:
 * <pre>{@code
 * RingFieldRenderer renderer = new RingFieldRenderer();
 * renderer.initialize(config, new Random(42));
 * parentGroup.getChildren().add(renderer.getGroup());
 *
 * // In animation loop:
 * renderer.update(timeScale);
 * renderer.refreshMeshes();  // Call periodically, not every frame
 * }</pre>
 */
@Slf4j
public class RingFieldRenderer {

    // Size thresholds for mesh partitioning (as fraction of size range)
    private static final double THRESH_MEDIUM_FRACTION = 0.4;
    private static final double THRESH_LARGE_FRACTION = 0.7;

    /** The group containing all rendered ring elements */
    @Getter
    private final Group group = new Group();

    /** Current configuration */
    @Getter
    private RingConfiguration config;

    /** Generated ring elements */
    @Getter
    private List<RingElement> elements = new ArrayList<>();

    /** Pre-computed size categories (0=small, 1=medium, 2=large) */
    private int[] sizeCategories;

    /** Size thresholds computed from config */
    private double smallThreshold;
    private double largeThreshold;

    /** Meshes for different size particles */
    private ScatterMesh meshSmall;
    private ScatterMesh meshMedium;
    private ScatterMesh meshLarge;

    /** Reusable lists for mesh building (minimizes GC) */
    private final List<Point3D> smallPoints = new ArrayList<>();
    private final List<Point3D> mediumPoints = new ArrayList<>();
    private final List<Point3D> largePoints = new ArrayList<>();

    /** Computed mesh sizes based on config */
    private double smallSize;
    private double mediumSize;
    private double largeSize;

    /** Whether the renderer has been initialized */
    @Getter
    private boolean initialized = false;

    /**
     * Creates an uninitialized renderer.
     * Call {@link #initialize(RingConfiguration, Random)} before use.
     */
    public RingFieldRenderer() {
    }

    /**
     * Creates and initializes a renderer with the given configuration.
     *
     * @param config the ring configuration
     * @param random random number generator for reproducible results
     */
    public RingFieldRenderer(RingConfiguration config, Random random) {
        initialize(config, random);
    }

    /**
     * Creates and initializes a renderer with a preset configuration.
     *
     * @param presetName name of the preset (e.g., "Saturn Ring")
     * @return initialized renderer
     */
    public static RingFieldRenderer fromPreset(String presetName) {
        return fromPreset(presetName, new Random(42));
    }

    /**
     * Creates and initializes a renderer with a preset configuration.
     *
     * @param presetName name of the preset
     * @param random random number generator
     * @return initialized renderer
     */
    public static RingFieldRenderer fromPreset(String presetName, Random random) {
        RingConfiguration config = RingFieldFactory.getPreset(presetName);
        return new RingFieldRenderer(config, random);
    }

    /**
     * Initializes or reinitializes the renderer with a new configuration.
     * Generates new elements and rebuilds meshes.
     *
     * @param config the ring configuration
     * @param random random number generator for reproducible results
     */
    public void initialize(RingConfiguration config, Random random) {
        this.config = config;

        // Clear existing meshes
        group.getChildren().clear();
        meshSmall = null;
        meshMedium = null;
        meshLarge = null;

        // Generate elements using the appropriate generator
        elements = RingFieldFactory.generateElements(config, random);

        // Compute size thresholds and categories
        computeSizeCategories();

        // Compute mesh sizes
        double sizeRange = config.maxSize() - config.minSize();
        smallSize = config.minSize() + sizeRange * 0.2;
        mediumSize = config.minSize() + sizeRange * 0.5;
        largeSize = config.minSize() + sizeRange * 0.85;

        // Mark as initialized BEFORE building meshes (refreshMeshes checks this flag)
        initialized = true;

        // Build initial meshes
        refreshMeshes();

        log.debug("RingFieldRenderer initialized: {} with {} elements, group children: {}",
                config.name(), elements.size(), group.getChildren().size());
    }

    /**
     * Reinitializes with a new configuration, reusing the existing random seed behavior.
     *
     * @param config the new ring configuration
     */
    public void setConfiguration(RingConfiguration config) {
        initialize(config, new Random(42));
    }

    /**
     * Switches to a preset configuration.
     *
     * @param presetName name of the preset
     */
    public void switchPreset(String presetName) {
        RingConfiguration newConfig = RingFieldFactory.getPreset(presetName);
        initialize(newConfig, new Random(42));
    }

    /**
     * Pre-computes size categories for all elements to avoid per-frame comparisons.
     */
    private void computeSizeCategories() {
        double sizeRange = config.maxSize() - config.minSize();
        smallThreshold = config.minSize() + sizeRange * THRESH_MEDIUM_FRACTION;
        largeThreshold = config.minSize() + sizeRange * THRESH_LARGE_FRACTION;

        sizeCategories = new int[elements.size()];
        for (int i = 0; i < elements.size(); i++) {
            double size = elements.get(i).getSize();
            if (size >= largeThreshold) {
                sizeCategories[i] = 2;
            } else if (size >= smallThreshold) {
                sizeCategories[i] = 1;
            } else {
                sizeCategories[i] = 0;
            }
        }
    }

    /**
     * Updates all element positions based on time scale.
     * Call this every frame for smooth animation.
     *
     * @param timeScale multiplier for angular movement (1.0 = normal speed at 60fps)
     */
    public void update(double timeScale) {
        if (!initialized) return;

        for (RingElement element : elements) {
            element.advance(timeScale);
        }
    }

    /**
     * Rebuilds the meshes from current element positions.
     * This is expensive - call periodically (e.g., every 5 frames), not every frame.
     */
    public void refreshMeshes() {
        if (!initialized || elements.isEmpty()) return;

        // Remove old meshes
        group.getChildren().removeAll(meshSmall, meshMedium, meshLarge);

        // Clear and reuse lists
        smallPoints.clear();
        mediumPoints.clear();
        largePoints.clear();

        // Partition elements by size
        for (int i = 0; i < elements.size(); i++) {
            RingElement element = elements.get(i);
            Point3D p = new Point3D(
                    (float) element.getX(),
                    (float) element.getY(),
                    (float) element.getZ()
            );

            switch (sizeCategories[i]) {
                case 2 -> largePoints.add(p);
                case 1 -> mediumPoints.add(p);
                default -> smallPoints.add(p);
            }
        }

        // Create meshes for each size category
        // Disable backface culling so particles are visible from all angles
        if (!smallPoints.isEmpty()) {
            meshSmall = new ScatterMesh(smallPoints, true, smallSize, 0);
            meshSmall.setTextureModeNone(config.primaryColor());
            disableCulling(meshSmall);
            group.getChildren().add(meshSmall);
        }

        if (!mediumPoints.isEmpty()) {
            meshMedium = new ScatterMesh(mediumPoints, true, mediumSize, 0);
            Color mediumColor = config.primaryColor().interpolate(config.secondaryColor(), 0.3);
            meshMedium.setTextureModeNone(mediumColor);
            disableCulling(meshMedium);
            group.getChildren().add(meshMedium);
        }

        if (!largePoints.isEmpty()) {
            meshLarge = new ScatterMesh(largePoints, true, largeSize, 0);
            meshLarge.setTextureModeNone(config.secondaryColor());
            disableCulling(meshLarge);
            group.getChildren().add(meshLarge);
        }
    }

    /**
     * Disables backface culling on a ScatterMesh so particles are visible from all angles.
     * ScatterMesh is a Parent containing MeshView children, so we need to set CullFace.NONE
     * on each child MeshView.
     */
    private void disableCulling(ScatterMesh mesh) {
        // ScatterMesh extends TexturedMesh which extends Group
        // The actual MeshView is inside the group hierarchy
        disableCullingRecursive(mesh);
    }

    /**
     * Recursively sets CullFace.NONE on all MeshView nodes in the hierarchy.
     */
    private void disableCullingRecursive(Node node) {
        if (node instanceof javafx.scene.shape.MeshView meshView) {
            meshView.setCullFace(CullFace.NONE);
        }
        if (node instanceof javafx.scene.Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                disableCullingRecursive(child);
            }
        }
    }

    /**
     * Sets the position of the ring group in the parent scene.
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    public void setPosition(double x, double y, double z) {
        group.setTranslateX(x);
        group.setTranslateY(y);
        group.setTranslateZ(z);
    }

    /**
     * Sets the scale of the ring group.
     *
     * @param scale uniform scale factor
     */
    public void setScale(double scale) {
        group.setScaleX(scale);
        group.setScaleY(scale);
        group.setScaleZ(scale);
    }

    /**
     * Sets non-uniform scale of the ring group.
     *
     * @param scaleX x scale factor
     * @param scaleY y scale factor
     * @param scaleZ z scale factor
     */
    public void setScale(double scaleX, double scaleY, double scaleZ) {
        group.setScaleX(scaleX);
        group.setScaleY(scaleY);
        group.setScaleZ(scaleZ);
    }

    /**
     * Sets visibility of the ring group.
     *
     * @param visible true to show, false to hide
     */
    public void setVisible(boolean visible) {
        group.setVisible(visible);
    }

    /**
     * Returns the number of elements in this ring.
     */
    public int getElementCount() {
        return elements.size();
    }

    /**
     * Clears all elements and meshes.
     */
    public void clear() {
        elements.clear();
        group.getChildren().clear();
        meshSmall = null;
        meshMedium = null;
        meshLarge = null;
        sizeCategories = null;
        initialized = false;
    }

    /**
     * Disposes of resources. Call when the renderer is no longer needed.
     */
    public void dispose() {
        clear();
        config = null;
    }
}
