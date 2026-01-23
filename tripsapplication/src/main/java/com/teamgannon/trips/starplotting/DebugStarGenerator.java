package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.config.application.model.ColorPalette;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Generates random stars for testing and debugging purposes.
 * <p>
 * This class is used for visual testing of the star plot rendering system.
 * It creates random stars with varying sizes, colors, and positions.
 */
@Slf4j
public class DebugStarGenerator {

    // =========================================================================
    // Random Generation Constants
    // =========================================================================

    /**
     * Maximum radius for randomly generated test stars.
     */
    private static final double MAX_RANDOM_STAR_RADIUS = 7.0;

    /**
     * Fraction of max range used for random star positions.
     */
    private static final double RANDOM_POSITION_FRACTION = 2.0 / 3.0;

    /**
     * Maximum X coordinate for random star generation.
     */
    private static final double X_MAX = 300.0;

    /**
     * Maximum Y coordinate for random star generation.
     */
    private static final double Y_MAX = 300.0;

    /**
     * Maximum Z coordinate for random star generation.
     */
    private static final double Z_MAX = 300.0;

    // =========================================================================
    // Instance Fields
    // =========================================================================

    /**
     * Source of random numbers.
     */
    private final Random random = new Random();

    /**
     * The world group to add stars to.
     */
    private final Group world;

    /**
     * The label manager for creating star labels.
     */
    private final StarLabelManager labelManager;

    /**
     * The extension manager for creating star extensions.
     */
    private final StarExtensionManager extensionManager;

    /**
     * Constructor.
     *
     * @param world            the world group to add stars to
     * @param labelManager     the label manager
     * @param extensionManager the extension manager
     */
    public DebugStarGenerator(Group world,
                              StarLabelManager labelManager,
                              StarExtensionManager extensionManager) {
        this.world = world;
        this.labelManager = labelManager;
        this.extensionManager = extensionManager;
    }

    /**
     * Generate random stars for testing/debug purposes.
     *
     * @param numberStars  number of stars to generate
     * @param colorPalette the color palette for labels
     */
    public void generateRandomStars(int numberStars, ColorPalette colorPalette) {
        if (colorPalette == null) {
            log.warn("color palette not initialized; cannot generate random stars");
            return;
        }

        for (int i = 0; i < numberStars; i++) {
            double radius = random.nextDouble() * MAX_RANDOM_STAR_RADIUS;
            Color color = randomColor();
            double x = randomCoordinate(X_MAX);
            double y = randomCoordinate(Y_MAX);
            double z = randomCoordinate(Z_MAX);

            String labelText = "Star " + i;
            createSphereAndLabel(radius, x, y, z, color, colorPalette.getLabelFont().toFont(), labelText);
            extensionManager.createExtension(x, y, z, Color.VIOLET, colorPalette.getLabelFont().toFont());
        }

        log.info("Generated {} random stars, total labels: {}", numberStars, labelManager.getLabelCount());
    }

    /**
     * Generates a random coordinate within the specified max range.
     */
    private double randomCoordinate(double max) {
        return random.nextDouble() * max * RANDOM_POSITION_FRACTION * (random.nextBoolean() ? 1 : -1);
    }

    /**
     * Generates a random color.
     */
    private @NotNull Color randomColor() {
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);
        return Color.rgb(r, g, b);
    }

    /**
     * Creates a sphere with a label at the specified position.
     */
    private void createSphereAndLabel(double radius, double x, double y, double z,
                                       Color color, Font font, String labelText) {
        Sphere sphere = new Sphere(radius);
        sphere.setTranslateX(x);
        sphere.setTranslateY(y);
        sphere.setTranslateZ(z);
        sphere.setMaterial(new PhongMaterial(color));

        // Add to scene graph
        world.getChildren().add(sphere);

        // Create label
        Label label = new Label(labelText);
        label.setTextFill(color);
        label.setFont(font);

        // Create descriptor for tooltip
        ObjectDescriptor descriptor = ObjectDescriptor
                .builder()
                .name(labelText)
                .color(color)
                .x(x)
                .y(y)
                .z(z)
                .build();
        sphere.setUserData(descriptor);

        // Add tooltip
        Tooltip tooltip = new Tooltip(descriptor.toString());
        Tooltip.install(sphere, tooltip);

        // Register the label with the label manager
        labelManager.registerLabel(sphere, label);
    }
}
