package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.objects.MeshViewShapeFactory;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Factory for creating polity indicator objects.
 * <p>
 * Polity objects are 3D geometric shapes that indicate which civilization
 * controls a star system. Each polity type has a distinct shape:
 * <ul>
 *   <li>Terran / Slaasriithi - Cube</li>
 *   <li>Dornani / Other1 - Tetrahedron</li>
 *   <li>Ktor / Other3 - Icosahedron</li>
 *   <li>Arat Kur / Other2 - Octahedron</li>
 *   <li>Hkh'Rkh / Other4 - Dodecahedron</li>
 * </ul>
 * <p>
 * This factory consolidates polity object creation, reducing code duplication
 * and making it easier to add new polity types.
 */
@Slf4j
public class PolityObjectFactory {

    // =========================================================================
    // Constants
    // =========================================================================

    /**
     * Default scale for polity objects.
     */
    private static final double DEFAULT_SCALE = 1.0;

    /**
     * Default rotation angle for polity objects.
     */
    private static final double DEFAULT_ROTATION_ANGLE = 90.0;

    /**
     * Polity name constants.
     */
    public static final String POLITY_TERRAN = "polity_terran";
    public static final String POLITY_DORNANI = "polity_dornani";
    public static final String POLITY_KTOR = "polity_ktor";
    public static final String POLITY_ARAT_KUR = "polity_arat_kur";
    public static final String POLITY_HKH_RKH = "polity_hkh_rkh";

    // =========================================================================
    // Configuration
    // =========================================================================

    /**
     * Polity type configuration mapping polity names to their shape suppliers.
     */
    private final Map<String, PolityConfig> polityConfigs = new HashMap<>();

    /**
     * The mesh factory for creating geometric shapes.
     */
    private final MeshViewShapeFactory meshViewShapeFactory;

    // =========================================================================
    // Constructor
    // =========================================================================

    /**
     * Create a new PolityObjectFactory.
     *
     * @param meshViewShapeFactory the factory for creating mesh shapes
     */
    public PolityObjectFactory(@NotNull MeshViewShapeFactory meshViewShapeFactory) {
        this.meshViewShapeFactory = meshViewShapeFactory;
        initializePolityConfigs();
    }

    /**
     * Initialize the polity configuration mappings.
     */
    private void initializePolityConfigs() {
        // Terran and Slaasriithi use cube
        polityConfigs.put(CivilizationDisplayPreferences.TERRAN,
                new PolityConfig(POLITY_TERRAN, meshViewShapeFactory::cube));
        polityConfigs.put(CivilizationDisplayPreferences.SLAASRIITHI,
                new PolityConfig(POLITY_TERRAN, meshViewShapeFactory::cube));

        // Dornani and Other1 use tetrahedron
        polityConfigs.put(CivilizationDisplayPreferences.DORNANI,
                new PolityConfig(POLITY_DORNANI, meshViewShapeFactory::tetrahedron));
        polityConfigs.put(CivilizationDisplayPreferences.OTHER1,
                new PolityConfig(POLITY_DORNANI, meshViewShapeFactory::tetrahedron));

        // Ktor and Other3 use icosahedron
        polityConfigs.put(CivilizationDisplayPreferences.KTOR,
                new PolityConfig(POLITY_KTOR, meshViewShapeFactory::icosahedron));
        polityConfigs.put(CivilizationDisplayPreferences.OTHER3,
                new PolityConfig(POLITY_KTOR, meshViewShapeFactory::icosahedron));

        // Arat Kur and Other2 use octahedron
        polityConfigs.put(CivilizationDisplayPreferences.ARAKUR,
                new PolityConfig(POLITY_ARAT_KUR, meshViewShapeFactory::octahedron));
        polityConfigs.put(CivilizationDisplayPreferences.OTHER2,
                new PolityConfig(POLITY_ARAT_KUR, meshViewShapeFactory::octahedron));

        // Hkh'Rkh and Other4 use dodecahedron
        polityConfigs.put(CivilizationDisplayPreferences.HKHRKH,
                new PolityConfig(POLITY_HKH_RKH, meshViewShapeFactory::dodecahedron));
        polityConfigs.put(CivilizationDisplayPreferences.OTHER4,
                new PolityConfig(POLITY_HKH_RKH, meshViewShapeFactory::dodecahedron));
    }

    // =========================================================================
    // Factory Methods
    // =========================================================================

    /**
     * Create a polity object for the given polity type.
     *
     * @param polityName        the name of the polity (from CivilizationDisplayPreferences)
     * @param polityPreferences the preferences containing color information
     * @return the configured MeshView, or null if the polity is unknown or mesh creation failed
     */
    public @Nullable MeshView createPolityObject(@NotNull String polityName,
                                                  @NotNull CivilizationDisplayPreferences polityPreferences) {
        Color polityColor = polityPreferences.getColorForPolity(polityName);
        return createPolityObject(polityName, polityColor);
    }

    /**
     * Create a polity object with the specified color.
     *
     * @param polityName the name of the polity
     * @param color      the color for the polity object
     * @return the configured MeshView, or null if the polity is unknown or mesh creation failed
     */
    public @Nullable MeshView createPolityObject(@NotNull String polityName, @NotNull Color color) {
        PolityConfig config = polityConfigs.get(polityName);

        if (config == null) {
            log.error("Unknown polity type: {}", polityName);
            return null;
        }

        MeshView meshView = config.shapeSupplier().get();
        if (meshView == null) {
            log.error("Failed to create mesh for polity: {}", polityName);
            return null;
        }

        configureMeshView(meshView, color);
        return meshView;
    }

    /**
     * Create a MeshObjectDefinition for the given polity.
     * This is useful when you need the full definition including scale and rotation info.
     *
     * @param polityName the name of the polity
     * @return the MeshObjectDefinition, or an empty definition if creation failed
     */
    public @NotNull MeshObjectDefinition createPolityDefinition(@NotNull String polityName) {
        PolityConfig config = polityConfigs.get(polityName);

        if (config == null) {
            log.error("Unknown polity type: {}", polityName);
            return MeshObjectDefinition.builder().build();
        }

        MeshView meshView = config.shapeSupplier().get();
        if (meshView == null) {
            log.error("Failed to create mesh for polity definition: {}", polityName);
            return MeshObjectDefinition.builder().build();
        }

        return MeshObjectDefinition.builder()
                .name(config.name())
                .id(UUID.randomUUID())
                .object(meshView)
                .xScale(DEFAULT_SCALE)
                .yScale(DEFAULT_SCALE)
                .zScale(DEFAULT_SCALE)
                .axis(Rotate.X_AXIS)
                .rotateAngle(DEFAULT_ROTATION_ANGLE)
                .build();
    }

    /**
     * Create a fully configured polity object with position.
     *
     * @param polityName        the name of the polity
     * @param polityPreferences the preferences containing color information
     * @param x                 the X position
     * @param y                 the Y position
     * @param z                 the Z position
     * @return the configured and positioned MeshView, or null if creation failed
     */
    public @Nullable MeshView createPositionedPolityObject(@NotNull String polityName,
                                                            @NotNull CivilizationDisplayPreferences polityPreferences,
                                                            double x, double y, double z) {
        MeshView meshView = createPolityObject(polityName, polityPreferences);
        if (meshView != null) {
            meshView.setTranslateX(x);
            meshView.setTranslateY(y);
            meshView.setTranslateZ(z);
        }
        return meshView;
    }

    // =========================================================================
    // Configuration
    // =========================================================================

    /**
     * Configure a MeshView with the standard polity appearance.
     *
     * @param meshView the mesh view to configure
     * @param color    the color to apply
     */
    private void configureMeshView(@NotNull MeshView meshView, @NotNull Color color) {
        // Set material color
        PhongMaterial material = (PhongMaterial) meshView.getMaterial();
        if (material == null) {
            material = new PhongMaterial();
            meshView.setMaterial(material);
        }
        material.setDiffuseColor(color);
        material.setSpecularColor(color);

        // Set scale
        meshView.setScaleX(DEFAULT_SCALE);
        meshView.setScaleY(DEFAULT_SCALE);
        meshView.setScaleZ(DEFAULT_SCALE);

        // Set rotation
        meshView.setRotationAxis(Rotate.X_AXIS);
        meshView.setRotate(DEFAULT_ROTATION_ANGLE);
    }

    // =========================================================================
    // Query Methods
    // =========================================================================

    /**
     * Check if a polity type is supported.
     *
     * @param polityName the polity name to check
     * @return true if the polity type is known
     */
    public boolean isPolitySupported(@NotNull String polityName) {
        return polityConfigs.containsKey(polityName);
    }

    /**
     * Get the shape name for a polity.
     *
     * @param polityName the polity name
     * @return the internal shape name, or null if unknown
     */
    public @Nullable String getPolityShapeName(@NotNull String polityName) {
        PolityConfig config = polityConfigs.get(polityName);
        return config != null ? config.name() : null;
    }

    // =========================================================================
    // Inner Classes
    // =========================================================================

    /**
     * Configuration for a polity type.
     *
     * @param name          the internal name for the polity shape
     * @param shapeSupplier supplier that creates the mesh shape
     */
    private record PolityConfig(String name, Supplier<MeshView> shapeSupplier) {
    }
}
