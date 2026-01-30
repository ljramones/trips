package com.teamgannon.trips.particlefields;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.CullFace;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.fxyz3d.geometry.Point3D;
import org.fxyz3d.shapes.primitives.ScatterMesh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Renders ring/particle field elements to a JavaFX Group.
 * This class is decoupled from window management, allowing rings to be
 * rendered in any 3D scene (standalone window, solar system view, interstellar view).
 *
 * <p>Uses ScatterMesh with per-particle attributes for efficient rendering:
 * <ul>
 *   <li>Per-particle color via colorIndex (maps to palette texture)</li>
 *   <li>Per-particle size via scale factor</li>
 *   <li>Per-particle opacity via opacity field</li>
 *   <li>Efficient position updates without full mesh rebuild</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * RingFieldRenderer renderer = new RingFieldRenderer();
 * renderer.initialize(config, new Random(42));
 * parentGroup.getChildren().add(renderer.getGroup());
 *
 * // In animation loop:
 * renderer.update(timeScale);
 * renderer.updateMeshPositions();  // Efficient update, can call every frame
 * }</pre>
 */
@Slf4j
public class RingFieldRenderer {

    /** Number of colors in the palette (matches ScatterMesh palette size) */
    private static final int PALETTE_SIZE = 256;

    /** The group containing all rendered ring elements */
    @Getter
    private final Group group = new Group();

    /** Current configuration */
    @Getter
    private RingConfiguration config;

    /** Generated ring elements */
    @Getter
    private List<RingElement> elements = new ArrayList<>();

    /** Single mesh for all particles (replaces meshSmall/meshMedium/meshLarge) */
    private ScatterMesh mesh;

    /** Color palette for per-particle coloring */
    private List<Color> colorPalette;

    /** Base particle size (used as reference for scale factors) */
    private double baseSize;

    /** Cached Point3D list for efficient position updates */
    private List<Point3D> cachedPoints;

    /** Whether the renderer has been initialized */
    @Getter
    private boolean initialized = false;

    /** Whether per-particle opacity is enabled (based on element colors having opacity < 1) */
    private boolean useOpacity = false;

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
     * Generates new elements and builds the mesh with per-particle attributes.
     *
     * @param config the ring configuration
     * @param random random number generator for reproducible results
     */
    public void initialize(RingConfiguration config, Random random) {
        this.config = config;

        // Clear existing mesh
        group.getChildren().clear();
        mesh = null;
        cachedPoints = null;

        // Generate elements using the appropriate generator
        elements = RingFieldFactory.generateElements(config, random);

        // Compute base size (midpoint of size range)
        baseSize = (config.minSize() + config.maxSize()) / 2.0;

        // Build color palette from primary and secondary colors
        colorPalette = buildColorPalette(config.primaryColor(), config.secondaryColor());

        // Check if any elements have opacity < 1
        useOpacity = elements.stream()
                .anyMatch(e -> e.getColor().getOpacity() < 0.99);

        // Mark as initialized BEFORE building mesh
        initialized = true;

        // Build the mesh with per-particle attributes
        buildMesh();

        log.debug("RingFieldRenderer initialized: {} with {} elements, using per-particle attributes (opacity={})",
                config.name(), elements.size(), useOpacity);
    }

    /**
     * Builds the color palette as a gradient from primary to secondary color.
     * The palette has 256 entries for smooth color mapping.
     */
    private List<Color> buildColorPalette(Color primary, Color secondary) {
        List<Color> palette = new ArrayList<>(PALETTE_SIZE);
        for (int i = 0; i < PALETTE_SIZE; i++) {
            double t = i / (double) (PALETTE_SIZE - 1);
            palette.add(primary.interpolate(secondary, t));
        }
        return palette;
    }

    /**
     * Maps a color to a palette index (0-255) by finding its position in the
     * primary-to-secondary gradient.
     */
    private int colorToIndex(Color color, Color primary, Color secondary) {
        // Calculate how close this color is to secondary vs primary
        // Using a simple hue/saturation/brightness distance metric
        double primaryDist = colorDistance(color, primary);
        double secondaryDist = colorDistance(color, secondary);
        double totalDist = primaryDist + secondaryDist;

        if (totalDist < 0.001) {
            return 0; // Identical to primary
        }

        // Position in gradient: 0 = primary, 1 = secondary
        double t = primaryDist / totalDist;
        return Math.min(255, Math.max(0, (int) (t * 255)));
    }

    /**
     * Calculates a simple color distance metric.
     */
    private double colorDistance(Color c1, Color c2) {
        double dr = c1.getRed() - c2.getRed();
        double dg = c1.getGreen() - c2.getGreen();
        double db = c1.getBlue() - c2.getBlue();
        return Math.sqrt(dr * dr + dg * dg + db * db);
    }

    /**
     * Builds the mesh with per-particle color, scale, and opacity.
     */
    private void buildMesh() {
        if (elements.isEmpty()) return;

        // Build Point3D list with per-particle attributes
        cachedPoints = new ArrayList<>(elements.size());

        for (RingElement element : elements) {
            // Map element color to palette index
            int colorIndex = colorToIndex(element.getColor(), config.primaryColor(), config.secondaryColor());

            // Calculate scale relative to base size
            float scale = (float) (element.getSize() / baseSize);

            // Get opacity from element color
            float opacity = (float) element.getColor().getOpacity();

            // Create Point3D with all attributes
            Point3D point = new Point3D(
                    (float) element.getX(),
                    (float) element.getY(),
                    (float) element.getZ(),
                    colorIndex,
                    scale,
                    opacity
            );
            cachedPoints.add(point);
        }

        // Create single mesh with all particles
        mesh = new ScatterMesh(cachedPoints, true, baseSize, 0);

        // Enable per-particle attributes
        if (useOpacity) {
            mesh.enableAllPerParticleAttributes(colorPalette);
        } else {
            mesh.enablePerParticleAttributes(colorPalette);
        }

        // Disable culling so particles are visible from all angles
        mesh.setCullFace(CullFace.NONE);

        group.getChildren().add(mesh);
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
     * Updates the mesh positions efficiently without full rebuild.
     * This can be called every frame as it only updates the vertex buffer.
     *
     * @return true if efficient update was used, false if full rebuild was needed
     */
    public boolean updateMeshPositions() {
        if (!initialized || elements.isEmpty() || mesh == null || cachedPoints == null) {
            return false;
        }

        // Update cached Point3D positions from elements
        for (int i = 0; i < elements.size(); i++) {
            RingElement element = elements.get(i);
            Point3D point = cachedPoints.get(i);
            point.x = (float) element.getX();
            point.y = (float) element.getY();
            point.z = (float) element.getZ();
        }

        // Use efficient position update (doesn't rebuild mesh, just updates vertices)
        return mesh.updatePositions(cachedPoints);
    }

    /**
     * Rebuilds the meshes from current element positions.
     * This is more expensive than updateMeshPositions() but handles changes
     * in particle count or attributes.
     *
     * @deprecated Use {@link #updateMeshPositions()} for animation loops.
     * This method is kept for compatibility but now just calls updateMeshPositions().
     */
    @Deprecated
    public void refreshMeshes() {
        if (!updateMeshPositions()) {
            // If efficient update failed, do a full rebuild
            group.getChildren().clear();
            mesh = null;
            buildMesh();
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
     * Returns the number of meshes used (always 1 with new implementation).
     */
    public int getMeshCount() {
        return mesh != null ? 1 : 0;
    }

    /**
     * Clears all elements and meshes.
     */
    public void clear() {
        elements.clear();
        group.getChildren().clear();
        mesh = null;
        cachedPoints = null;
        colorPalette = null;
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
