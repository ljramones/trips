package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.measure.TrackExecutionTime;
import com.teamgannon.trips.objects.MeshViewShapeFactory;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages special star mesh objects (central star, moravian star, 4pt/5pt stars, etc.).
 * <p>
 * This class handles:
 * <ul>
 *   <li>Loading special mesh objects from the factory</li>
 *   <li>Storing mesh definitions with scale/rotation settings</li>
 *   <li>Creating fresh instances of meshes (JavaFX nodes can only be in one scene)</li>
 *   <li>Creating highlight stars for star blinking animation</li>
 * </ul>
 */
@Slf4j
public class SpecialStarMeshManager {

    // =========================================================================
    // Display Constants
    // =========================================================================

    /**
     * Default scale factor for special star meshes (central star, 4pt, 5pt stars).
     */
    private static final double DEFAULT_STAR_MESH_SCALE = 30.0;

    /**
     * Scale factor for smaller mesh objects (pyramid, geometric shapes).
     */
    private static final double SMALL_MESH_SCALE = 10.0;

    /**
     * Default rotation angle for star meshes (degrees).
     */
    private static final double DEFAULT_ROTATION_ANGLE = 90.0;

    /**
     * Inverted rotation angle for some mesh objects (degrees).
     */
    private static final double INVERTED_ROTATION_ANGLE = -90.0;

    /**
     * Fallback sphere radius when mesh loading fails.
     */
    private static final double FALLBACK_SPHERE_RADIUS = 10.0;

    // =========================================================================
    // Mesh Object Keys
    // =========================================================================

    public static final String CENTRAL_STAR = "centralStar";
    public static final String MORAVIAN_STAR = "moravianStar";
    public static final String FOUR_PT_STAR = "4PtStar";
    public static final String FIVE_PT_STAR = "5PtStar";
    public static final String PYRAMID = "pyramid";
    public static final String GEOMETRIC_0 = "geometric0";

    // =========================================================================
    // Instance Fields
    // =========================================================================

    /**
     * Factory for creating 3D mesh objects.
     */
    private final MeshViewShapeFactory meshViewShapeFactory;

    /**
     * Stored definitions for special objects (scale, rotation settings).
     */
    private final Map<String, MeshObjectDefinition> specialObjects = new HashMap<>();

    /**
     * Constructor.
     *
     * @param meshViewShapeFactory the factory for creating mesh objects
     */
    public SpecialStarMeshManager(MeshViewShapeFactory meshViewShapeFactory) {
        this.meshViewShapeFactory = meshViewShapeFactory;
        loadSpecialObjects();
    }

    /**
     * Default constructor that creates its own mesh factory.
     */
    public SpecialStarMeshManager() {
        this(new MeshViewShapeFactory());
    }

    /**
     * Load all special mesh object definitions.
     */
    @TrackExecutionTime
    private void loadSpecialObjects() {
        loadCentralStar();
        loadFourPointStar();
        loadFivePointStar();
        loadPyramid();
        loadGeometric0();

        log.info("All special MeshView objects loaded");
    }

    private void loadCentralStar() {
        Group centralStar = meshViewShapeFactory.starCentral();
        if (centralStar != null) {
            MeshObjectDefinition objectDefinition = MeshObjectDefinition
                    .builder()
                    .name(CENTRAL_STAR)
                    .id(UUID.randomUUID())
                    .object(centralStar)
                    .xScale(DEFAULT_STAR_MESH_SCALE)
                    .yScale(DEFAULT_STAR_MESH_SCALE)
                    .zScale(DEFAULT_STAR_MESH_SCALE)
                    .axis(Rotate.X_AXIS)
                    .rotateAngle(DEFAULT_ROTATION_ANGLE)
                    .build();
            specialObjects.put(CENTRAL_STAR, objectDefinition);
        } else {
            log.error("Unable to load the central star object");
        }
    }

    private void loadFourPointStar() {
        Node fourPtStar = meshViewShapeFactory.star4pt();
        if (fourPtStar != null) {
            MeshObjectDefinition objectDefinition = MeshObjectDefinition
                    .builder()
                    .name(FOUR_PT_STAR)
                    .id(UUID.randomUUID())
                    .object(fourPtStar)
                    .xScale(DEFAULT_STAR_MESH_SCALE)
                    .yScale(DEFAULT_STAR_MESH_SCALE)
                    .zScale(DEFAULT_STAR_MESH_SCALE)
                    .axis(Rotate.X_AXIS)
                    .rotateAngle(DEFAULT_ROTATION_ANGLE)
                    .build();
            specialObjects.put(FOUR_PT_STAR, objectDefinition);
        } else {
            log.error("Unable to load the 4 pt star object");
        }
    }

    private void loadFivePointStar() {
        Group fivePtStar = meshViewShapeFactory.star5pt();
        if (fivePtStar != null) {
            MeshObjectDefinition objectDefinition = MeshObjectDefinition
                    .builder()
                    .name(FIVE_PT_STAR)
                    .id(UUID.randomUUID())
                    .object(fivePtStar)
                    .xScale(DEFAULT_STAR_MESH_SCALE)
                    .yScale(DEFAULT_STAR_MESH_SCALE)
                    .zScale(DEFAULT_STAR_MESH_SCALE)
                    .axis(Rotate.X_AXIS)
                    .rotateAngle(DEFAULT_ROTATION_ANGLE)
                    .build();
            specialObjects.put(FIVE_PT_STAR, objectDefinition);
        } else {
            log.error("Unable to load the 5 pt star object");
        }
    }

    private void loadPyramid() {
        MeshView pyramid = meshViewShapeFactory.pyramid();
        if (pyramid != null) {
            MeshObjectDefinition objectDefinition = MeshObjectDefinition
                    .builder()
                    .name(PYRAMID)
                    .id(UUID.randomUUID())
                    .object(pyramid)
                    .xScale(SMALL_MESH_SCALE)
                    .yScale(SMALL_MESH_SCALE)
                    .zScale(SMALL_MESH_SCALE)
                    .axis(Rotate.X_AXIS)
                    .rotateAngle(INVERTED_ROTATION_ANGLE)
                    .build();
            specialObjects.put(PYRAMID, objectDefinition);
        } else {
            log.error("Unable to load the pyramid object");
        }
    }

    private void loadGeometric0() {
        MeshView geometric0 = meshViewShapeFactory.geometric0();
        if (geometric0 != null) {
            MeshObjectDefinition objectDefinition = MeshObjectDefinition
                    .builder()
                    .name(GEOMETRIC_0)
                    .id(UUID.randomUUID())
                    .object(geometric0)
                    .xScale(SMALL_MESH_SCALE)
                    .yScale(SMALL_MESH_SCALE)
                    .zScale(SMALL_MESH_SCALE)
                    .axis(Rotate.X_AXIS)
                    .rotateAngle(INVERTED_ROTATION_ANGLE)
                    .build();
            specialObjects.put(GEOMETRIC_0, objectDefinition);
        } else {
            log.error("Unable to load the geometric object");
        }
    }

    /**
     * Get a mesh object definition by name.
     *
     * @param name the mesh object name
     * @return the definition, or null if not found
     */
    @Nullable
    public MeshObjectDefinition getDefinition(String name) {
        return specialObjects.get(name);
    }

    /**
     * Create a fresh central star mesh for display.
     * <p>
     * IMPORTANT: Each call creates a NEW instance because JavaFX Nodes can only
     * be in one scene graph at a time. Reusing the same Node would cause it to
     * be removed from its previous parent.
     *
     * @return a new central star Node
     */
    public Node createCentralStar() {
        // Get the definition for scale/rotation settings
        MeshObjectDefinition meshObjectDefinition = specialObjects.get(CENTRAL_STAR);

        // Create a FRESH mesh instance - don't reuse cached objects!
        // JavaFX Nodes can only exist in one scene graph at a time.
        Group centralStar = meshViewShapeFactory.starCentral();

        if (centralStar == null) {
            log.error("Failed to load central star mesh from factory");
            return new Sphere(FALLBACK_SPHERE_RADIUS);
        }

        // Apply scale and rotation from the definition
        if (meshObjectDefinition != null) {
            centralStar.setScaleX(meshObjectDefinition.getXScale());
            centralStar.setScaleY(meshObjectDefinition.getYScale());
            centralStar.setScaleZ(meshObjectDefinition.getZScale());
            centralStar.setRotationAxis(meshObjectDefinition.getAxis());
            centralStar.setRotate(meshObjectDefinition.getRotateAngle());
        } else {
            // Default settings if definition is missing
            centralStar.setScaleX(DEFAULT_STAR_MESH_SCALE);
            centralStar.setScaleY(DEFAULT_STAR_MESH_SCALE);
            centralStar.setScaleZ(DEFAULT_STAR_MESH_SCALE);
            centralStar.setRotationAxis(Rotate.X_AXIS);
            centralStar.setRotate(DEFAULT_ROTATION_ANGLE);
        }

        return centralStar;
    }

    /**
     * Create a highlight star (moravian star) with the specified color.
     * <p>
     * The highlight star is used for the blinking animation when a star is highlighted.
     *
     * @param color the color to display it as (used to match the underlying star)
     * @return the star to display, or null if loading fails
     */
    @Nullable
    public Node createHighlightStar(Color color) {
        // Load the moravian star - we need a fresh instance each time
        Group highLightStar = meshViewShapeFactory.starMoravian();

        if (highLightStar != null) {
            // Extract the various meshviews and set the color to match
            // We need to do this because the moravian object is a group of mesh objects
            // and we need to set the material color on each one.
            for (Node node : highLightStar.getChildren()) {
                MeshView meshView = (MeshView) node;
                PhongMaterial material = (PhongMaterial) meshView.getMaterial();
                material.setSpecularColor(color);
                material.setDiffuseColor(color);
            }

            // Scale and rotate to display properly
            highLightStar.setScaleX(DEFAULT_STAR_MESH_SCALE);
            highLightStar.setScaleY(DEFAULT_STAR_MESH_SCALE);
            highLightStar.setScaleZ(DEFAULT_STAR_MESH_SCALE);
            highLightStar.setRotationAxis(Rotate.X_AXIS);
            highLightStar.setRotate(DEFAULT_ROTATION_ANGLE);

            return highLightStar;
        } else {
            log.error("Unable to load the moravian star object");
            return null;
        }
    }

    /**
     * Get the default star mesh scale.
     *
     * @return the default scale factor
     */
    public double getDefaultStarMeshScale() {
        return DEFAULT_STAR_MESH_SCALE;
    }

    /**
     * Get the fallback sphere radius.
     *
     * @return the fallback radius
     */
    public double getFallbackSphereRadius() {
        return FALLBACK_SPHERE_RADIUS;
    }
}
