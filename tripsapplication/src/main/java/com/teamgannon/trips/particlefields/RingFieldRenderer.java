package com.teamgannon.trips.particlefields;

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.fxyz3d.geometry.Point3D;
import org.fxyz3d.shapes.primitives.ScatterMesh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Rendering mode for particle clouds.
     * ScatterMesh modes are default since JavaFX 25 fixed frustum culling.
     */
    public enum RenderingMode {
        /** Default: auto-choose chunked or single mesh based on particle count */
        SCATTER_MESH_AUTO,
        /** Force single combined ScatterMesh */
        SCATTER_MESH_SINGLE,
        /** Force chunked ScatterMesh (many small meshes) */
        SCATTER_MESH_CHUNKED,
        /** Debug only: individual Sphere objects (matches star rendering) */
        INDIVIDUAL_SPHERES
    }

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

    /** Meshes for particles - either single combined mesh or chunked meshes */
    private ScatterMesh mesh;

    /** List of chunked meshes when using chunked rendering mode */
    private List<ScatterMesh> chunkedMeshes = new ArrayList<>();

    /** Maximum particles per chunk - increased from 500 since JavaFX 25 fixed frustum culling */
    private static final int MAX_PARTICLES_PER_CHUNK = 2000;

    /** Current rendering mode */
    @Getter
    private RenderingMode renderingMode = RenderingMode.SCATTER_MESH_AUTO;

    /** Whether to use chunked rendering (derived from renderingMode) */
    private boolean useChunkedRendering = true;

    /** Whether spatial binning was used (requires full rebuild for animation) */
    private boolean usedSpatialBinning = false;

    /** Whether to use individual Sphere objects (derived from renderingMode) */
    private boolean useIndividualSpheres = false;

    /** Current LOD level (for tracking and logging) */
    @Getter
    private String currentLodLevel = "FULL";

    /** Maximum particles when using individual spheres (for performance) */
    private static final int MAX_INDIVIDUAL_SPHERES = 10000;

    /** List of individual sphere nodes when using sphere rendering mode */
    private List<Sphere> individualSpheres = new ArrayList<>();

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

    /** World position offset applied directly to particle coordinates */
    private double worldOffsetX = 0, worldOffsetY = 0, worldOffsetZ = 0;

    /** Whether to use world coordinate positioning (particles at world coords) vs group translation */
    private boolean useWorldCoordinates = false;

    /**
     * Centroid of particle positions (computed in double precision).
     * Used for rebasing vertices to keep float values small.
     */
    private double centroidX = 0, centroidY = 0, centroidZ = 0;

    /** Whether to use centroid rebasing to avoid float precision issues */
    private boolean useCentroidRebasing = true;

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

        // Build color palette based on gradient mode
        colorPalette = buildColorPalette(config);

        // Check if any elements have opacity < 1
        useOpacity = elements.stream()
                .anyMatch(e -> e.getColor().getOpacity() < 0.99);

        // Mark as initialized BEFORE building mesh
        initialized = true;

        // Build the mesh with per-particle attributes
        buildMesh();

        // Apply glow effect if enabled
        applyGlowEffect();

        log.debug("RingFieldRenderer initialized: {} with {} elements, mode={}, using per-particle attributes (opacity={}), glow={}",
                config.name(), elements.size(), renderingMode, useOpacity,
                config.glowEnabled() ? config.glowIntensity() : "disabled");

        // Log performance stats for larger particle clouds
        if (elements.size() > 10000) {
            logRenderingStats();
        }
    }

    /**
     * Applies glow effect to the group if enabled in configuration.
     */
    private void applyGlowEffect() {
        if (config.glowEnabled() && config.glowIntensity() > 0) {
            Glow glow = new Glow(config.glowIntensity());
            group.setEffect(glow);
            log.debug("Applied glow effect with intensity {} to {}", config.glowIntensity(), config.name());
        } else {
            group.setEffect(null);
        }
    }

    /**
     * Updates the glow intensity dynamically.
     * Can be called to adjust glow without full rebuild.
     *
     * @param intensity new glow intensity (0.0 - 1.0), or 0 to disable
     */
    public void setGlowIntensity(double intensity) {
        if (intensity > 0) {
            group.setEffect(new Glow(Math.min(1.0, intensity)));
        } else {
            group.setEffect(null);
        }
    }

    /**
     * Builds the color palette based on the gradient mode.
     * The palette has 256 entries for smooth color mapping.
     */
    private List<Color> buildColorPalette(RingConfiguration config) {
        Color primary = config.primaryColor();
        Color secondary = config.secondaryColor();
        Color tertiary = config.tertiaryColor() != null ? config.tertiaryColor() :
                primary.interpolate(secondary, 0.5);
        ColorGradientMode mode = config.colorGradientMode() != null ?
                config.colorGradientMode() : ColorGradientMode.LINEAR;

        List<Color> palette = new ArrayList<>(PALETTE_SIZE);

        switch (mode) {
            case LINEAR:
            case RADIAL:  // Same palette, different index calculation
            case NOISE_BASED:
                // Simple linear gradient
                for (int i = 0; i < PALETTE_SIZE; i++) {
                    double t = i / (double) (PALETTE_SIZE - 1);
                    palette.add(primary.interpolate(secondary, t));
                }
                break;

            case TEMPERATURE:
                // Hot (red/white) at index 0, cool (blue) at index 255
                Color hot = Color.color(1.0, 0.9, 0.7);  // Yellow-white
                Color warm = primary;
                Color cool = secondary;
                for (int i = 0; i < PALETTE_SIZE; i++) {
                    double t = i / (double) (PALETTE_SIZE - 1);
                    if (t < 0.3) {
                        // Hot core (0-30%): hot to primary
                        palette.add(hot.interpolate(warm, t / 0.3));
                    } else if (t < 0.7) {
                        // Middle (30-70%): primary to secondary
                        palette.add(warm.interpolate(cool, (t - 0.3) / 0.4));
                    } else {
                        // Cool edge (70-100%): secondary to darker
                        palette.add(cool.interpolate(cool.darker(), (t - 0.7) / 0.3));
                    }
                }
                break;

            case MULTI_ZONE:
                // Three-color bands: 0-85: primary->tertiary, 86-170: tertiary->secondary, 171-255: secondary
                for (int i = 0; i < PALETTE_SIZE; i++) {
                    if (i <= 85) {
                        double t = i / 85.0;
                        palette.add(primary.interpolate(tertiary, t));
                    } else if (i <= 170) {
                        double t = (i - 86) / 84.0;
                        palette.add(tertiary.interpolate(secondary, t));
                    } else {
                        // Keep secondary color for outer region
                        double t = (i - 171) / 84.0;
                        palette.add(secondary.interpolate(secondary.darker(), t * 0.3));
                    }
                }
                break;

            default:
                // Default to linear
                for (int i = 0; i < PALETTE_SIZE; i++) {
                    double t = i / (double) (PALETTE_SIZE - 1);
                    palette.add(primary.interpolate(secondary, t));
                }
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
     * Uses centroid rebasing to keep vertex coordinates small (float-friendly)
     * while using double-precision group transforms for world placement.
     */
    private void buildMesh() {
        if (elements.isEmpty()) return;

        // Use individual spheres mode - matches how stars are rendered (never clips)
        if (useIndividualSpheres) {
            buildIndividualSpheres();
            return;
        }

        // Chunked rendering with per-bin local rebasing - handles its own centroid computation
        if (useChunkedRendering && elements.size() > MAX_PARTICLES_PER_CHUNK) {
            // Reset main group translation - each bin has its own
            group.setTranslateX(0);
            group.setTranslateY(0);
            group.setTranslateZ(0);
            buildChunkedMeshes();
            return;
        }

        // Single mesh mode - compute global centroid for rebasing
        if (useCentroidRebasing) {
            computeCentroid();
            log.info("Centroid rebasing: centroid=({}, {}, {})", centroidX, centroidY, centroidZ);
        }

        // Build Point3D list with per-particle attributes
        cachedPoints = new ArrayList<>(elements.size());

        for (RingElement element : elements) {
            // Map element color to palette index
            int colorIndex = colorToIndex(element.getColor(), config.primaryColor(), config.secondaryColor());

            // Calculate scale relative to base size
            float scale = (float) (element.getSize() / baseSize);

            // Get opacity from element color
            float opacity = (float) element.getColor().getOpacity();

            // World position (including offset)
            double wx = element.getX() + worldOffsetX;
            double wy = element.getY() + worldOffsetY;
            double wz = element.getZ() + worldOffsetZ;

            float px, py, pz;
            if (useCentroidRebasing) {
                // Rebase to centroid - keeps float values small
                px = (float) (wx - centroidX);
                py = (float) (wy - centroidY);
                pz = (float) (wz - centroidZ);
            } else {
                px = (float) wx;
                py = (float) wy;
                pz = (float) wz;
            }

            Point3D point = new Point3D(px, py, pz, colorIndex, scale, opacity);
            cachedPoints.add(point);
        }

        // Apply centroid translation to group (double precision for world placement)
        if (useCentroidRebasing) {
            group.setTranslateX(centroidX);
            group.setTranslateY(centroidY);
            group.setTranslateZ(centroidZ);
            log.info("Group translation set to ({}, {}, {})",
                    group.getTranslateX(), group.getTranslateY(), group.getTranslateZ());
        }

        // Single mesh rendering
        buildSingleMesh();

        // Log vertex range for diagnostics
        if (!cachedPoints.isEmpty()) {
            float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
            for (Point3D p : cachedPoints) {
                minX = Math.min(minX, p.x); maxX = Math.max(maxX, p.x);
                minY = Math.min(minY, p.y); maxY = Math.max(maxY, p.y);
                minZ = Math.min(minZ, p.z); maxZ = Math.max(maxZ, p.z);
            }
            log.info("Vertex range after rebasing: X[{}, {}], Y[{}, {}], Z[{}, {}]",
                    minX, maxX, minY, maxY, minZ, maxZ);
        }
    }

    /**
     * Computes the centroid of all particle positions in double precision.
     * Includes worldOffset to get the centroid in world coordinates.
     */
    private void computeCentroid() {
        if (elements.isEmpty()) {
            centroidX = centroidY = centroidZ = 0;
            return;
        }

        double sumX = 0, sumY = 0, sumZ = 0;
        for (RingElement element : elements) {
            sumX += element.getX() + worldOffsetX;
            sumY += element.getY() + worldOffsetY;
            sumZ += element.getZ() + worldOffsetZ;
        }

        centroidX = sumX / elements.size();
        centroidY = sumY / elements.size();
        centroidZ = sumZ / elements.size();
    }

    /**
     * Builds individual Sphere objects for each particle.
     * This matches how stars are rendered and avoids frustum clipping issues.
     * Samples particles if count exceeds MAX_INDIVIDUAL_SPHERES for performance.
     * Uses low-poly spheres and shared materials for better performance.
     */
    private void buildIndividualSpheres() {
        individualSpheres.clear();

        // Sample particles if too many for individual sphere rendering
        List<RingElement> particlesToRender;
        if (elements.size() > MAX_INDIVIDUAL_SPHERES) {
            particlesToRender = sampleParticles(elements, MAX_INDIVIDUAL_SPHERES);
            log.info("Sampled {} particles down to {} for sphere rendering", elements.size(), particlesToRender.size());
        } else {
            particlesToRender = elements;
        }

        log.info("Building {} individual low-poly spheres for nebula particles", particlesToRender.size());

        // Pre-create materials for color palette to share between spheres
        Map<Integer, PhongMaterial> materialCache = new HashMap<>();

        for (RingElement element : particlesToRender) {
            double radius = element.getSize();
            // Use low-poly sphere (4 divisions instead of default 64) for better performance
            Sphere sphere = new Sphere(radius, 4);

            // Position
            double px = element.getX() + (useWorldCoordinates ? worldOffsetX : 0);
            double py = element.getY() + (useWorldCoordinates ? worldOffsetY : 0);
            double pz = element.getZ() + (useWorldCoordinates ? worldOffsetZ : 0);
            sphere.setTranslateX(px);
            sphere.setTranslateY(py);
            sphere.setTranslateZ(pz);

            // Get or create shared material based on color index
            int colorIndex = colorToIndex(element.getColor(), config.primaryColor(), config.secondaryColor());
            PhongMaterial material = materialCache.computeIfAbsent(colorIndex, idx -> {
                PhongMaterial mat = new PhongMaterial();
                Color color = colorPalette.get(idx);
                mat.setDiffuseColor(color);
                mat.setSpecularColor(color.brighter());
                return mat;
            });
            sphere.setMaterial(material);

            individualSpheres.add(sphere);
            group.getChildren().add(sphere);
        }

        log.info("Built {} low-poly spheres with {} shared materials", individualSpheres.size(), materialCache.size());
    }

    /**
     * Samples particles evenly from the list to reduce count.
     */
    private List<RingElement> sampleParticles(List<RingElement> particles, int targetCount) {
        List<RingElement> sampled = new ArrayList<>(targetCount);
        double step = (double) particles.size() / targetCount;
        for (int i = 0; i < targetCount; i++) {
            int idx = (int) (i * step);
            sampled.add(particles.get(idx));
        }
        return sampled;
    }

    /**
     * Builds multiple smaller meshes using spatial binning with per-bin local rebasing.
     * Each bin gets its own local centroid, keeping mesh vertices small (float-friendly),
     * with the bin's Group translated to its centroid position.
     * This avoids frustum culling issues caused by large transformed bounds.
     */
    private void buildChunkedMeshes() {
        chunkedMeshes.clear();

        // Find bounding box of all particles (in world coords)
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;

        for (RingElement element : elements) {
            double wx = element.getX() + worldOffsetX;
            double wy = element.getY() + worldOffsetY;
            double wz = element.getZ() + worldOffsetZ;
            minX = Math.min(minX, wx); maxX = Math.max(maxX, wx);
            minY = Math.min(minY, wy); maxY = Math.max(maxY, wy);
            minZ = Math.min(minZ, wz); maxZ = Math.max(maxZ, wz);
        }

        // Use 3x3x3 = 27 bins (octree-style but finer)
        int divisions = 3;
        double cellSizeX = (maxX - minX) / divisions + 0.001;
        double cellSizeY = (maxY - minY) / divisions + 0.001;
        double cellSizeZ = (maxZ - minZ) / divisions + 0.001;

        // Bin elements by their position in the grid
        Map<String, List<RingElement>> bins = new HashMap<>();

        for (RingElement element : elements) {
            double wx = element.getX() + worldOffsetX;
            double wy = element.getY() + worldOffsetY;
            double wz = element.getZ() + worldOffsetZ;

            int ix = Math.min(divisions - 1, (int) ((wx - minX) / cellSizeX));
            int iy = Math.min(divisions - 1, (int) ((wy - minY) / cellSizeY));
            int iz = Math.min(divisions - 1, (int) ((wz - minZ) / cellSizeZ));
            String key = ix + "_" + iy + "_" + iz;
            bins.computeIfAbsent(key, k -> new ArrayList<>()).add(element);
        }

        log.info("Spatial binning with per-bin rebasing: {} particles into {} bins ({}x{}x{} grid)",
                elements.size(), bins.size(), divisions, divisions, divisions);

        // Create a mesh for each non-empty bin with LOCAL centroid rebasing
        for (Map.Entry<String, List<RingElement>> entry : bins.entrySet()) {
            List<RingElement> binElements = entry.getValue();
            if (binElements.isEmpty()) continue;

            // Compute LOCAL centroid for this bin (in double precision)
            double localCentroidX = 0, localCentroidY = 0, localCentroidZ = 0;
            for (RingElement e : binElements) {
                localCentroidX += e.getX() + worldOffsetX;
                localCentroidY += e.getY() + worldOffsetY;
                localCentroidZ += e.getZ() + worldOffsetZ;
            }
            localCentroidX /= binElements.size();
            localCentroidY /= binElements.size();
            localCentroidZ /= binElements.size();

            // Build points relative to LOCAL centroid (small float values)
            List<Point3D> localPoints = new ArrayList<>(binElements.size());
            for (RingElement element : binElements) {
                double wx = element.getX() + worldOffsetX;
                double wy = element.getY() + worldOffsetY;
                double wz = element.getZ() + worldOffsetZ;

                // Rebase to local centroid - keeps float values small
                float px = (float) (wx - localCentroidX);
                float py = (float) (wy - localCentroidY);
                float pz = (float) (wz - localCentroidZ);

                int colorIndex = colorToIndex(element.getColor(), config.primaryColor(), config.secondaryColor());
                float scale = (float) (element.getSize() / baseSize);
                float opacity = (float) element.getColor().getOpacity();

                localPoints.add(new Point3D(px, py, pz, colorIndex, scale, opacity));
            }

            // Create mesh with locally-rebased vertices
            ScatterMesh chunkMesh = new ScatterMesh(localPoints, true, baseSize, 0);

            // Enable per-particle attributes
            if (useOpacity) {
                chunkMesh.enableAllPerParticleAttributes(colorPalette);
            } else {
                chunkMesh.enablePerParticleAttributes(colorPalette);
            }
            chunkMesh.setCullFace(CullFace.NONE);

            // Create a sub-Group for this bin, translated to its local centroid
            Group binGroup = new Group(chunkMesh);
            binGroup.setTranslateX(localCentroidX);
            binGroup.setTranslateY(localCentroidY);
            binGroup.setTranslateZ(localCentroidZ);

            chunkedMeshes.add(chunkMesh);
            group.getChildren().add(binGroup);
        }

        log.info("Built {} spatially-binned meshes with per-bin local rebasing", chunkedMeshes.size());

        // Mark that spatial binning was used (affects animation updates)
        usedSpatialBinning = true;

        // Set mesh to first chunk for compatibility with existing code
        mesh = chunkedMeshes.isEmpty() ? null : chunkedMeshes.get(0);
    }

    /**
     * Builds a single combined mesh (original approach).
     */
    private void buildSingleMesh() {
        usedSpatialBinning = false;
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
     * Sets the rendering mode for this renderer.
     * ScatterMesh modes are recommended since JavaFX 25 fixed frustum culling.
     *
     * @param mode the rendering mode to use
     */
    public void setRenderingMode(RenderingMode mode) {
        this.renderingMode = mode;
        switch (mode) {
            case SCATTER_MESH_AUTO:
                this.useIndividualSpheres = false;
                this.useChunkedRendering = true;  // Will auto-choose based on count
                break;
            case SCATTER_MESH_SINGLE:
                this.useIndividualSpheres = false;
                this.useChunkedRendering = false;
                break;
            case SCATTER_MESH_CHUNKED:
                this.useIndividualSpheres = false;
                this.useChunkedRendering = true;
                break;
            case INDIVIDUAL_SPHERES:
                this.useIndividualSpheres = true;
                this.useChunkedRendering = false;
                break;
        }
    }

    /**
     * Sets whether to use chunked rendering (many small meshes) to avoid frustum culling issues.
     * @param useChunked true to use chunked rendering, false for single large mesh
     * @deprecated Use {@link #setRenderingMode(RenderingMode)} instead
     */
    @Deprecated
    public void setUseChunkedRendering(boolean useChunked) {
        this.useChunkedRendering = useChunked;
        if (!useIndividualSpheres) {
            this.renderingMode = useChunked ? RenderingMode.SCATTER_MESH_CHUNKED : RenderingMode.SCATTER_MESH_SINGLE;
        }
    }

    /**
     * Sets whether to use individual Sphere objects instead of ScatterMesh.
     * Individual spheres match how stars are rendered and avoid frustum clipping issues.
     * @param useSpheres true to use individual spheres, false for ScatterMesh
     * @deprecated Use {@link #setRenderingMode(RenderingMode)} instead. Individual spheres
     * are now debug-only since JavaFX 25 fixed ScatterMesh frustum culling.
     */
    @Deprecated
    public void setUseIndividualSpheres(boolean useSpheres) {
        this.useIndividualSpheres = useSpheres;
        if (useSpheres) {
            this.renderingMode = RenderingMode.INDIVIDUAL_SPHERES;
        }
    }

    /**
     * Sets the current LOD level (for tracking purposes).
     * @param lodLevel the LOD level name (e.g., "FULL", "HIGH", "MEDIUM", "LOW", "MINIMAL")
     */
    public void setCurrentLodLevel(String lodLevel) {
        this.currentLodLevel = lodLevel;
    }

    /**
     * Logs rendering performance statistics.
     * Useful for debugging and performance tuning.
     */
    public void logRenderingStats() {
        String mode = renderingMode.name();
        int elementCount = elements.size();
        int meshCount = getMeshCount();
        String rendering = useIndividualSpheres ? "Individual Spheres" :
                (usedSpatialBinning ? "Chunked ScatterMesh" : "Single ScatterMesh");

        log.info("PERF [{}]: mode={}, elements={}, meshes={}, rendering={}, lod={}",
                config != null ? config.name() : "unknown",
                mode, elementCount, meshCount, rendering, currentLodLevel);

        if (mesh != null && !useIndividualSpheres) {
            Bounds bounds = mesh.getBoundsInLocal();
            log.info("PERF [{}]: mesh bounds size=({:.1f}, {:.1f}, {:.1f})",
                    config != null ? config.name() : "unknown",
                    bounds.getWidth(), bounds.getHeight(), bounds.getDepth());
        }
    }

    /**
     * Sets whether to use centroid rebasing to avoid float precision issues.
     * When enabled, vertex coordinates are stored as (position - centroid) to keep
     * float values small, while the group is translated to the centroid position
     * using double precision.
     * @param useRebasing true to enable centroid rebasing (recommended)
     */
    public void setUseCentroidRebasing(boolean useRebasing) {
        this.useCentroidRebasing = useRebasing;
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
     * Note: When spatial binning is used, this triggers a full rebuild since
     * particles may move between bins.
     *
     * @return true if efficient update was used, false if full rebuild was needed
     */
    public boolean updateMeshPositions() {
        if (!initialized || elements.isEmpty()) {
            return false;
        }

        // Individual spheres mode - update sphere positions directly
        if (useIndividualSpheres && !individualSpheres.isEmpty()) {
            for (int i = 0; i < elements.size() && i < individualSpheres.size(); i++) {
                RingElement element = elements.get(i);
                Sphere sphere = individualSpheres.get(i);
                sphere.setTranslateX(element.getX() + (useWorldCoordinates ? worldOffsetX : 0));
                sphere.setTranslateY(element.getY() + (useWorldCoordinates ? worldOffsetY : 0));
                sphere.setTranslateZ(element.getZ() + (useWorldCoordinates ? worldOffsetZ : 0));
            }
            return true;
        }

        // Spatial binning requires full rebuild since particles can move between cells
        if (usedSpatialBinning) {
            group.getChildren().clear();
            chunkedMeshes.clear();
            mesh = null;
            buildMesh();
            return false;
        }

        if (cachedPoints == null) {
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

        // Single mesh mode
        if (mesh == null) {
            return false;
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
     * Uses group translation by default. Call {@link #setUseWorldCoordinates(boolean)}
     * with true to instead offset particle positions directly (better for frustum culling).
     *
     * @param x x coordinate
     * @param y y coordinate
     * @param z z coordinate
     */
    public void setPosition(double x, double y, double z) {
        // Store the world offset
        worldOffsetX = x;
        worldOffsetY = y;
        worldOffsetZ = z;

        if (useCentroidRebasing) {
            // With centroid rebasing, we need to rebuild to recompute centroid
            // The group translation will be set to (centroid + offset) in buildMesh()
            if (initialized) {
                group.getChildren().clear();
                chunkedMeshes.clear();
                mesh = null;
                buildMesh();
            }
        } else if (useWorldCoordinates) {
            // Legacy world coordinates mode - bake offset into vertices
            group.setTranslateX(0);
            group.setTranslateY(0);
            group.setTranslateZ(0);
            if (initialized) {
                group.getChildren().clear();
                mesh = null;
                buildMesh();
            }
        } else {
            // Standard group translation (no rebasing)
            group.setTranslateX(x);
            group.setTranslateY(y);
            group.setTranslateZ(z);
        }
    }

    /**
     * Sets whether to use world coordinates for particle positions.
     * When true, setPosition() offsets particle coordinates directly in the mesh,
     * which can help prevent frustum culling issues with large particle clouds
     * during scene rotation. When false (default), uses Group translation.
     *
     * @param useWorldCoordinates true to offset particle positions, false for group translation
     */
    public void setUseWorldCoordinates(boolean useWorldCoordinates) {
        this.useWorldCoordinates = useWorldCoordinates;
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
     * Returns the number of meshes/spheres used.
     */
    public int getMeshCount() {
        if (!individualSpheres.isEmpty()) {
            return individualSpheres.size();
        }
        if (!chunkedMeshes.isEmpty()) {
            return chunkedMeshes.size();
        }
        return mesh != null ? 1 : 0;
    }

    /**
     * Clears all elements and meshes.
     */
    public void clear() {
        elements.clear();
        group.getChildren().clear();
        mesh = null;
        chunkedMeshes.clear();
        individualSpheres.clear();
        cachedPoints = null;
        colorPalette = null;
        initialized = false;
        usedSpatialBinning = false;
        centroidX = centroidY = centroidZ = 0;
        group.setTranslateX(0);
        group.setTranslateY(0);
        group.setTranslateZ(0);
    }

    /**
     * Disposes of resources. Call when the renderer is no longer needed.
     */
    public void dispose() {
        clear();
        config = null;
    }

    // ==================== Diagnostic Methods ====================

    /** Visual bounding box for debugging */
    private Box debugBoundingBox;

    /** Whether debug mode is enabled */
    @Getter
    private boolean debugMode = false;

    /**
     * Enables or disables debug mode.
     * When enabled, shows a wireframe bounding box around the particle cloud
     * and logs diagnostic information.
     *
     * @param enabled true to enable debug mode
     */
    public void setDebugMode(boolean enabled) {
        this.debugMode = enabled;
        if (enabled) {
            updateDebugBoundingBox();
            logDiagnostics();
        } else {
            removeDebugBoundingBox();
        }
    }

    /**
     * Logs diagnostic information about the mesh bounds and position.
     */
    public void logDiagnostics() {
        if (mesh == null) {
            log.info("DIAG [{}]: Mesh is null", config != null ? config.name() : "unknown");
            return;
        }

        Bounds localBounds = mesh.getBoundsInLocal();
        Bounds parentBounds = mesh.getBoundsInParent();
        Bounds groupLocalBounds = group.getBoundsInLocal();
        Bounds groupParentBounds = group.getBoundsInParent();

        log.info("DIAG [{}]: Element count={}", config.name(), elements.size());
        log.info("DIAG [{}]: World offset=({}, {}, {}), useWorldCoords={}",
                config.name(), worldOffsetX, worldOffsetY, worldOffsetZ, useWorldCoordinates);
        log.info("DIAG [{}]: Group translate=({}, {}, {})",
                config.name(), group.getTranslateX(), group.getTranslateY(), group.getTranslateZ());
        log.info("DIAG [{}]: Mesh boundsInLocal: min=({}, {}, {}), max=({}, {}, {}), size=({}, {}, {})",
                config.name(),
                localBounds.getMinX(), localBounds.getMinY(), localBounds.getMinZ(),
                localBounds.getMaxX(), localBounds.getMaxY(), localBounds.getMaxZ(),
                localBounds.getWidth(), localBounds.getHeight(), localBounds.getDepth());
        log.info("DIAG [{}]: Mesh boundsInParent: min=({}, {}, {}), max=({}, {}, {})",
                config.name(),
                parentBounds.getMinX(), parentBounds.getMinY(), parentBounds.getMinZ(),
                parentBounds.getMaxX(), parentBounds.getMaxY(), parentBounds.getMaxZ());
        log.info("DIAG [{}]: Group boundsInLocal: min=({}, {}, {}), max=({}, {}, {})",
                config.name(),
                groupLocalBounds.getMinX(), groupLocalBounds.getMinY(), groupLocalBounds.getMinZ(),
                groupLocalBounds.getMaxX(), groupLocalBounds.getMaxY(), groupLocalBounds.getMaxZ());
        log.info("DIAG [{}]: Group boundsInParent: min=({}, {}, {}), max=({}, {}, {})",
                config.name(),
                groupParentBounds.getMinX(), groupParentBounds.getMinY(), groupParentBounds.getMinZ(),
                groupParentBounds.getMaxX(), groupParentBounds.getMaxY(), groupParentBounds.getMaxZ());

        // Log mesh visibility and other properties
        log.info("DIAG [{}]: Mesh visible={}, managed={}, cullFace={}, depthTest={}",
                config.name(), mesh.isVisible(), mesh.isManaged(), mesh.getCullFace(), mesh.getDepthTest());

        // Log child mesh info
        if (!mesh.getChildren().isEmpty()) {
            mesh.getChildren().forEach(child -> {
                Bounds childLocal = child.getBoundsInLocal();
                Bounds childParent = child.getBoundsInParent();
                log.info("DIAG [{}]: Child '{}' boundsInLocal: size=({}, {}, {}), boundsInParent: min=({}, {}, {})",
                        config.name(), child.getClass().getSimpleName(),
                        childLocal.getWidth(), childLocal.getHeight(), childLocal.getDepth(),
                        childParent.getMinX(), childParent.getMinY(), childParent.getMinZ());
            });
        }
    }

    /**
     * Creates or updates the debug bounding box visualization.
     */
    private void updateDebugBoundingBox() {
        if (mesh == null) return;

        Bounds bounds = mesh.getBoundsInLocal();

        if (debugBoundingBox == null) {
            debugBoundingBox = new Box(bounds.getWidth(), bounds.getHeight(), bounds.getDepth());
            PhongMaterial material = new PhongMaterial(Color.TRANSPARENT);
            material.setSpecularColor(Color.YELLOW);
            debugBoundingBox.setMaterial(material);
            debugBoundingBox.setDrawMode(DrawMode.LINE);
            debugBoundingBox.setCullFace(CullFace.NONE);
        } else {
            debugBoundingBox.setWidth(bounds.getWidth());
            debugBoundingBox.setHeight(bounds.getHeight());
            debugBoundingBox.setDepth(bounds.getDepth());
        }

        // Position at center of bounds
        debugBoundingBox.setTranslateX(bounds.getCenterX());
        debugBoundingBox.setTranslateY(bounds.getCenterY());
        debugBoundingBox.setTranslateZ(bounds.getCenterZ());

        if (!group.getChildren().contains(debugBoundingBox)) {
            group.getChildren().add(debugBoundingBox);
        }

        log.info("DIAG [{}]: Debug bounding box at ({}, {}, {}) size ({}, {}, {})",
                config.name(),
                bounds.getCenterX(), bounds.getCenterY(), bounds.getCenterZ(),
                bounds.getWidth(), bounds.getHeight(), bounds.getDepth());
    }

    /**
     * Removes the debug bounding box.
     */
    private void removeDebugBoundingBox() {
        if (debugBoundingBox != null) {
            group.getChildren().remove(debugBoundingBox);
            debugBoundingBox = null;
        }
    }

    /**
     * Gets a diagnostic summary string.
     */
    public String getDiagnosticSummary() {
        if (mesh == null) return "Mesh is null";

        Bounds bounds = mesh.getBoundsInParent();
        return String.format("Elements=%d, BoundsInParent=[%.1f,%.1f,%.1f to %.1f,%.1f,%.1f], GroupTranslate=[%.1f,%.1f,%.1f]",
                elements.size(),
                bounds.getMinX(), bounds.getMinY(), bounds.getMinZ(),
                bounds.getMaxX(), bounds.getMaxY(), bounds.getMaxZ(),
                group.getTranslateX(), group.getTranslateY(), group.getTranslateZ());
    }
}
