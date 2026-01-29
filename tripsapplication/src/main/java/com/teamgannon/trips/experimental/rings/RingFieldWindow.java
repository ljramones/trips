package com.teamgannon.trips.experimental.rings;

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
import lombok.extern.slf4j.Slf4j;
import org.fxyz3d.geometry.Point3D;
import org.fxyz3d.shapes.primitives.ScatterMesh;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 3D visualization window for ring/particle field systems.
 * Supports multiple ring types: planetary rings, asteroid belts, debris disks, nebulae, and accretion disks.
 */
@Slf4j
public class RingFieldWindow {

    private static final double WINDOW_WIDTH = 1200;
    private static final double WINDOW_HEIGHT = 900;

    private static final int MESH_REFRESH_INTERVAL = 5;
    private static final double TARGET_FRAME_SECONDS = 1.0 / 60.0;
    private static final double MAX_DELTA_SECONDS = 0.1;

    // Size thresholds for mesh partitioning
    private static final double THRESH_MEDIUM = 0.4;
    private static final double THRESH_LARGE = 0.7;

    private final Stage stage = new Stage();
    private final Group world = new Group();
    private final Group sceneRoot = new Group();
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final SubScene subScene;

    private final Rotate rotateX = new Rotate(30, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    private double mousePosX, mousePosY;
    private double mouseOldX, mouseOldY;

    // Configuration and elements
    private RingConfiguration config;
    private List<RingElement> elements;
    private final Random random;

    // Pre-computed size categories (0=small, 1=medium, 2=large)
    private int[] sizeCategories;

    // Meshes for different size particles
    private ScatterMesh meshSmall;
    private ScatterMesh meshMedium;
    private ScatterMesh meshLarge;

    // Reusable lists for mesh building
    private final List<Point3D> smallPoints = new ArrayList<>();
    private final List<Point3D> mediumPoints = new ArrayList<>();
    private final List<Point3D> largePoints = new ArrayList<>();

    // Central body
    private Sphere centralBody;

    // Animation state
    private AnimationTimer animationTimer;
    private boolean animating = true;
    private int frameCounter = 0;
    private long lastFrameNanos = 0;

    /**
     * Creates a ring field window with the given configuration.
     */
    public RingFieldWindow(RingConfiguration config) {
        this(config, new Random(42));
    }

    /**
     * Creates a ring field window with the given configuration and random seed.
     */
    public RingFieldWindow(RingConfiguration config, Random random) {
        this.config = config;
        this.random = random;
        this.elements = new ArrayList<>();

        world.getTransforms().addAll(rotateX, rotateY, rotateZ);
        subScene = new SubScene(world, WINDOW_WIDTH, WINDOW_HEIGHT, true, SceneAntialiasing.BALANCED);

        initialize();
    }

    /**
     * Creates a ring field window with a preset configuration.
     */
    public static RingFieldWindow fromPreset(String presetName) {
        return new RingFieldWindow(RingFieldFactory.getPreset(presetName));
    }

    private void initialize() {
        setupCamera();
        setupLighting();
        createCentralBody();
        generateRingField();

        subScene.setFill(Color.BLACK);
        subScene.setCamera(camera);

        setupMouseHandlers();

        sceneRoot.getChildren().add(subScene);

        Scene scene = new Scene(sceneRoot, WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setScene(scene);
        stage.setTitle(config.name() + " – " + config.type().getDisplayName());

        setupKeyHandlers();
        subScene.setFocusTraversable(true);
        startAnimation();

        log.info("RingFieldWindow initialized: {} with {} elements",
                config.name(), config.numElements());
    }

    private void setupCamera() {
        // Position camera based on ring size
        double maxRadius = config.outerRadius();
        double cameraDistance = maxRadius * 3.5;

        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(-cameraDistance);
        camera.setNearClip(0.1);
        camera.setFarClip(maxRadius * 20);
    }

    private void setupLighting() {
        AmbientLight ambient = new AmbientLight(Color.rgb(120, 120, 120));
        world.getChildren().add(ambient);

        // Central light (star/object)
        PointLight centralLight = new PointLight(Color.rgb(255, 250, 240));
        centralLight.setTranslateX(0);
        centralLight.setTranslateY(0);
        centralLight.setTranslateZ(0);
        world.getChildren().add(centralLight);

        // Add fill light for better visibility
        PointLight fillLight = new PointLight(Color.rgb(80, 80, 100));
        fillLight.setTranslateX(config.outerRadius() * 2);
        fillLight.setTranslateY(config.outerRadius());
        fillLight.setTranslateZ(config.outerRadius());
        world.getChildren().add(fillLight);
    }

    private void createCentralBody() {
        centralBody = new Sphere(config.centralBodyRadius());

        // Color based on ring type
        Color bodyColor = switch (config.type()) {
            case PLANETARY_RING -> Color.LIGHTYELLOW;      // Gas giant
            case ASTEROID_BELT -> Color.ORANGE;            // Star
            case DEBRIS_DISK -> Color.YELLOW;              // Young star
            case DUST_CLOUD -> Color.WHITE;                // Hidden star
            case ACCRETION_DISK -> Color.rgb(20, 20, 40);  // Compact object (dark)
        };

        PhongMaterial material = new PhongMaterial(bodyColor);
        material.setSpecularColor(Color.WHITE);
        centralBody.setMaterial(material);

        world.getChildren().add(centralBody);
    }

    private void generateRingField() {
        // Generate elements using the appropriate generator
        elements = RingFieldFactory.generateElements(config, random);

        // Pre-compute size categories
        double sizeRange = config.maxSize() - config.minSize();
        double smallThresh = config.minSize() + sizeRange * THRESH_MEDIUM;
        double largeThresh = config.minSize() + sizeRange * THRESH_LARGE;

        sizeCategories = new int[elements.size()];
        for (int i = 0; i < elements.size(); i++) {
            double size = elements.get(i).getSize();
            if (size >= largeThresh) {
                sizeCategories[i] = 2;
            } else if (size >= smallThresh) {
                sizeCategories[i] = 1;
            } else {
                sizeCategories[i] = 0;
            }
        }

        refreshMeshes();
    }

    private void refreshMeshes() {
        world.getChildren().removeAll(meshSmall, meshMedium, meshLarge);

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

        // Calculate mesh sizes based on config
        double sizeRange = config.maxSize() - config.minSize();
        double smallSize = config.minSize() + sizeRange * 0.2;
        double mediumSize = config.minSize() + sizeRange * 0.5;
        double largeSize = config.minSize() + sizeRange * 0.85;

        if (!smallPoints.isEmpty()) {
            meshSmall = new ScatterMesh(smallPoints, true, smallSize, 0);
            meshSmall.setTextureModeNone(config.primaryColor());
            world.getChildren().add(meshSmall);
        }

        if (!mediumPoints.isEmpty()) {
            meshMedium = new ScatterMesh(mediumPoints, true, mediumSize, 0);
            // Blend primary and secondary colors
            Color mediumColor = config.primaryColor().interpolate(config.secondaryColor(), 0.3);
            meshMedium.setTextureModeNone(mediumColor);
            world.getChildren().add(meshMedium);
        }

        if (!largePoints.isEmpty()) {
            meshLarge = new ScatterMesh(largePoints, true, largeSize, 0);
            meshLarge.setTextureModeNone(config.secondaryColor());
            world.getChildren().add(meshLarge);
        }
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

                updatePositions(timeScale);

                frameCounter++;
                if (frameCounter % MESH_REFRESH_INTERVAL == 0) {
                    refreshMeshes();
                }
            }
        };
        animationTimer.start();
    }

    private void updatePositions(double timeScale) {
        for (RingElement element : elements) {
            element.advance(timeScale);
        }
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
        setupCamera();
        log.info("View reset");
    }

    /**
     * Switches to a different preset configuration.
     */
    public void switchPreset(String presetName) {
        try {
            this.config = RingFieldFactory.getPreset(presetName);
            random.setSeed(42);  // Reset for reproducibility

            // Recreate central body
            world.getChildren().remove(centralBody);
            createCentralBody();

            // Regenerate ring field
            generateRingField();
            setupCamera();

            stage.setTitle(config.name() + " – " + config.type().getDisplayName());
            log.info("Switched to preset: {}", presetName);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown preset: {}", presetName);
        }
    }

    /**
     * Applies a new configuration.
     */
    public void setConfiguration(RingConfiguration newConfig) {
        this.config = newConfig;
        random.setSeed(42);

        world.getChildren().remove(centralBody);
        createCentralBody();

        generateRingField();
        setupCamera();

        stage.setTitle(config.name() + " – " + config.type().getDisplayName());
        log.info("Applied new configuration: {}", newConfig.name());
    }

    public void toggleAnimation() {
        animating = !animating;
        log.info("Animation {}", animating ? "resumed" : "paused");
    }

    public void show() {
        stage.show();
        subScene.requestFocus();
        log.info("RingFieldWindow shown: {}", config.name());
    }

    public void dispose() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    // Getters
    public Stage getStage() { return stage; }
    public Group getWorld() { return world; }
    public PerspectiveCamera getCamera() { return camera; }
    public RingConfiguration getConfiguration() { return config; }
    public List<RingElement> getElements() { return elements; }
}
