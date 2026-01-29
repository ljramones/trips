package com.teamgannon.trips.particlefields;

import javafx.animation.AnimationTimer;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * Standalone 3D visualization window for ring/particle field systems.
 * Uses {@link RingFieldRenderer} for the actual rendering, which can also be
 * used independently in other views (solar system, interstellar).
 *
 * <p>Supports multiple ring types: planetary rings, asteroid belts, debris disks,
 * nebulae, and accretion disks.
 *
 * <p>Keyboard controls:
 * <ul>
 *   <li>Space - pause/resume animation</li>
 *   <li>R - reset view</li>
 *   <li>1-5 - quick switch between presets</li>
 * </ul>
 *
 * <p>Mouse controls:
 * <ul>
 *   <li>Left-drag - rotate view</li>
 *   <li>Right-drag - pan view</li>
 *   <li>Scroll - zoom in/out</li>
 * </ul>
 */
@Slf4j
public class RingFieldWindow {

    private static final double WINDOW_WIDTH = 1200;
    private static final double WINDOW_HEIGHT = 900;

    private static final int MESH_REFRESH_INTERVAL = 5;
    private static final double TARGET_FRAME_SECONDS = 1.0 / 60.0;
    private static final double MAX_DELTA_SECONDS = 0.1;

    @Getter
    private final Stage stage = new Stage();
    private final Group world = new Group();
    private final Group sceneRoot = new Group();
    @Getter
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final SubScene subScene;

    private final Rotate rotateX = new Rotate(30, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;

    /** The renderer that handles element generation and mesh creation */
    @Getter
    private final RingFieldRenderer renderer = new RingFieldRenderer();

    /** Random number generator for reproducible results */
    private final Random random;

    /** Central body (star, planet, black hole, etc.) */
    private Sphere centralBody;

    /** Animation state */
    private AnimationTimer animationTimer;
    private boolean animating = true;
    private int frameCounter = 0;
    private long lastFrameNanos = 0;

    /**
     * Creates a ring field window with the given configuration.
     *
     * @param config the ring configuration
     */
    public RingFieldWindow(RingConfiguration config) {
        this(config, new Random(42));
    }

    /**
     * Creates a ring field window with the given configuration and random seed.
     *
     * @param config the ring configuration
     * @param random random number generator for reproducible results
     */
    public RingFieldWindow(RingConfiguration config, Random random) {
        this.random = random;

        world.getTransforms().addAll(rotateX, rotateY, rotateZ);
        subScene = new SubScene(world, WINDOW_WIDTH, WINDOW_HEIGHT, true, SceneAntialiasing.BALANCED);

        initialize(config);
    }

    /**
     * Creates a ring field window with a preset configuration.
     *
     * @param presetName name of the preset (e.g., "Saturn Ring")
     * @return new window instance
     */
    public static RingFieldWindow fromPreset(String presetName) {
        return new RingFieldWindow(RingFieldFactory.getPreset(presetName));
    }

    private void initialize(RingConfiguration config) {
        setupCamera(config);
        setupLighting(config);
        createCentralBody(config);

        // Initialize the renderer and add its group to the world
        renderer.initialize(config, random);
        world.getChildren().add(renderer.getGroup());

        subScene.setFill(Color.BLACK);
        subScene.setCamera(camera);

        setupMouseHandlers();

        sceneRoot.getChildren().add(subScene);

        Scene scene = new Scene(sceneRoot, WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setScene(scene);
        updateWindowTitle();

        setupKeyHandlers();
        subScene.setFocusTraversable(true);
        startAnimation();

        log.info("RingFieldWindow initialized: {} with {} elements",
                config.name(), renderer.getElementCount());
    }

    private void setupCamera(RingConfiguration config) {
        double maxRadius = config.outerRadius();
        double cameraDistance = maxRadius * 3.5;

        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(-cameraDistance);
        camera.setNearClip(0.1);
        camera.setFarClip(maxRadius * 20);
    }

    private void setupLighting(RingConfiguration config) {
        // Remove existing lights
        world.getChildren().removeIf(node -> node instanceof AmbientLight || node instanceof PointLight);

        AmbientLight ambient = new AmbientLight(Color.rgb(120, 120, 120));
        world.getChildren().add(ambient);

        // Central light (star/object)
        PointLight centralLight = new PointLight(Color.rgb(255, 250, 240));
        centralLight.setTranslateX(0);
        centralLight.setTranslateY(0);
        centralLight.setTranslateZ(0);
        world.getChildren().add(centralLight);

        // Fill light for better visibility
        PointLight fillLight = new PointLight(Color.rgb(80, 80, 100));
        fillLight.setTranslateX(config.outerRadius() * 2);
        fillLight.setTranslateY(config.outerRadius());
        fillLight.setTranslateZ(config.outerRadius());
        world.getChildren().add(fillLight);
    }

    private void createCentralBody(RingConfiguration config) {
        // Remove existing central body
        if (centralBody != null) {
            world.getChildren().remove(centralBody);
        }

        centralBody = new Sphere(config.centralBodyRadius());

        // Color based on ring type
        Color bodyColor = getCentralBodyColor(config.type());

        PhongMaterial material = new PhongMaterial(bodyColor);
        material.setSpecularColor(Color.WHITE);
        centralBody.setMaterial(material);

        world.getChildren().add(centralBody);
    }

    private Color getCentralBodyColor(RingType type) {
        return switch (type) {
            case PLANETARY_RING -> Color.LIGHTYELLOW;      // Gas giant
            case ASTEROID_BELT -> Color.ORANGE;            // Star
            case DEBRIS_DISK -> Color.YELLOW;              // Young star
            case DUST_CLOUD -> Color.WHITE;                // Hidden star
            case ACCRETION_DISK -> Color.rgb(20, 20, 40);  // Compact object (dark)
        };
    }

    private void startAnimation() {
        lastFrameNanos = System.nanoTime();

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!animating) {
                    lastFrameNanos = now;
                    return;
                }

                // Compute actual delta time
                double deltaSeconds = (now - lastFrameNanos) / 1_000_000_000.0;
                lastFrameNanos = now;

                // Clamp to avoid physics explosions after pauses
                deltaSeconds = Math.min(deltaSeconds, MAX_DELTA_SECONDS);

                // Scale relative to target frame rate
                double timeScale = deltaSeconds / TARGET_FRAME_SECONDS;

                // Update element positions
                renderer.update(timeScale);

                // Refresh meshes periodically (not every frame)
                frameCounter++;
                if (frameCounter % MESH_REFRESH_INTERVAL == 0) {
                    renderer.refreshMeshes();
                }
            }
        };
        animationTimer.start();
    }

    private void setupMouseHandlers() {
        subScene.setOnMousePressed(this::handleMousePressed);
        subScene.setOnMouseDragged(this::handleMouseDragged);
        subScene.setOnScroll(this::handleScroll);
    }

    private void handleMousePressed(MouseEvent e) {
        mouseOldX = mousePosX = e.getSceneX();
        mouseOldY = mousePosY = e.getSceneY();
        subScene.requestFocus();
    }

    private void handleMouseDragged(MouseEvent e) {
        mouseOldX = mousePosX;
        mouseOldY = mousePosY;
        mousePosX = e.getSceneX();
        mousePosY = e.getSceneY();

        double dx = mousePosX - mouseOldX;
        double dy = mousePosY - mouseOldY;

        if (e.isPrimaryButtonDown()) {
            rotateY.setAngle(rotateY.getAngle() + dx * 0.42);
            rotateX.setAngle(rotateX.getAngle() - dy * 0.42);
        } else if (e.isSecondaryButtonDown()) {
            camera.setTranslateX(camera.getTranslateX() + dx * 0.9);
            camera.setTranslateY(camera.getTranslateY() + dy * 0.9);
        }
    }

    private void handleScroll(ScrollEvent e) {
        camera.setTranslateZ(camera.getTranslateZ() + e.getDeltaY() * 2.2);
    }

    private void setupKeyHandlers() {
        Scene scene = stage.getScene();
        if (scene != null) {
            scene.setOnKeyPressed(e -> {
                switch (e.getCode()) {
                    case SPACE -> toggleAnimation();
                    case R -> resetView();
                    case DIGIT1 -> switchPreset("Saturn Ring");
                    case DIGIT2 -> switchPreset("Main Asteroid Belt");
                    case DIGIT3 -> switchPreset("Protoplanetary Disk");
                    case DIGIT4 -> switchPreset("Emission Nebula");
                    case DIGIT5 -> switchPreset("Black Hole Accretion");
                }
            });
        }
    }

    private void resetView() {
        rotateX.setAngle(30);
        rotateY.setAngle(0);
        rotateZ.setAngle(0);
        setupCamera(renderer.getConfig());
        log.info("View reset");
    }

    private void updateWindowTitle() {
        RingConfiguration config = renderer.getConfig();
        stage.setTitle(config.name() + " – " + config.type().getDisplayName());
    }

    /**
     * Switches to a different preset configuration.
     *
     * @param presetName name of the preset
     */
    public void switchPreset(String presetName) {
        try {
            RingConfiguration newConfig = RingFieldFactory.getPreset(presetName);

            // Remove old renderer group
            world.getChildren().remove(renderer.getGroup());

            // Reinitialize renderer with new config
            random.setSeed(42);
            renderer.initialize(newConfig, random);

            // Add renderer group back
            world.getChildren().add(renderer.getGroup());

            // Update central body and camera
            createCentralBody(newConfig);
            setupLighting(newConfig);
            setupCamera(newConfig);
            updateWindowTitle();

            log.info("Switched to preset: {}", presetName);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown preset: {}", presetName);
        }
    }

    /**
     * Applies a new configuration.
     *
     * @param newConfig the new ring configuration
     */
    public void setConfiguration(RingConfiguration newConfig) {
        world.getChildren().remove(renderer.getGroup());

        random.setSeed(42);
        renderer.initialize(newConfig, random);

        world.getChildren().add(renderer.getGroup());

        createCentralBody(newConfig);
        setupLighting(newConfig);
        setupCamera(newConfig);
        updateWindowTitle();

        log.info("Applied new configuration: {}", newConfig.name());
    }

    /**
     * Toggles animation pause/resume.
     */
    public void toggleAnimation() {
        animating = !animating;
        log.info("Animation {}", animating ? "resumed" : "paused");
    }

    /**
     * Shows the window.
     */
    public void show() {
        stage.show();
        subScene.requestFocus();
        log.info("RingFieldWindow shown: {}", renderer.getConfig().name());
    }

    /**
     * Disposes of resources. Call when the window is closed.
     */
    public void dispose() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        renderer.dispose();
    }

    /**
     * Returns the current configuration.
     */
    public RingConfiguration getConfiguration() {
        return renderer.getConfig();
    }

    /**
     * Returns the world group (for adding additional objects).
     */
    public Group getWorld() {
        return world;
    }
}
