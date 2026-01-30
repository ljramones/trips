package com.teamgannon.trips.experimental;

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
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.fxyz3d.geometry.Point3D;
import org.fxyz3d.shapes.primitives.ScatterMesh;
import org.ode4j.ode.DBody;
import org.ode4j.ode.DMass;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.PVCoordinates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Simple, reliable procedural asteroid field (no Orekit).
 * Restored from your earlier working versions.
 *
 * <p>Uses ScatterMesh with per-particle size for efficient rendering:
 * <ul>
 *   <li>Single mesh instead of 3 size-categorized meshes</li>
 *   <li>Per-particle scale from asteroid size</li>
 *   <li>Efficient position updates without mesh rebuild</li>
 * </ul>
 */
@Slf4j
public class AsteroidFieldWindow {

    /**
     * Immutable orbital parameters for a single asteroid.
     * Angle is stored separately since it's updated each frame.
     */
    private record AsteroidData(
            double radius,
            double speed,
            double height,
            double eccentricity,
            double inclination,
            double size
    ) {}

    private static final double WINDOW_WIDTH  = 1200;
    private static final double WINDOW_HEIGHT = 900;

    private static final int    NUM_ASTEROIDS      = 5000;
    private static final double FIELD_INNER_RADIUS = 95;
    private static final double FIELD_OUTER_RADIUS = 105;
    private static final double FIELD_THICKNESS    = 8.0;
    private static final double MAX_INCLINATION_DEG = 12.0;

    private static final double BASE_ANGULAR_SPEED = 0.002;

    /** Base size for scale calculations (midpoint of size range) */
    private static final double BASE_SIZE = 1.4;
    /** Minimum asteroid size */
    private static final double MIN_SIZE = 0.9;
    /** Maximum asteroid size */
    private static final double MAX_SIZE = 2.6;

    private static final AbsoluteDate ORBIT_EPOCH = AbsoluteDate.J2000_EPOCH;
    // Use a synthetic GM that works with visual units (radius ~100) instead of real meters
    // This gives orbital velocities that match the visual scale
    private static final double VISUAL_SCALE_MU = 1.0e6;

    private static final String WINDOW_TITLE_BASE = "Asteroid Field – Procedural (working version)";

    private static final int HERO_BODY_COUNT = 120;
    // Target physics step size (will be adjusted based on actual frame time)
    private static final double TARGET_FRAME_SECONDS = 1.0 / 60.0;
    // Clamp delta to avoid physics explosions on long pauses
    private static final double MAX_DELTA_SECONDS = 0.1;

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

    // Immutable orbital parameters for each asteroid
    private final List<AsteroidData> asteroids = new ArrayList<>();
    // Mutable current angle for each asteroid (updated every frame)
    private final double[] angles = new double[NUM_ASTEROIDS];

    /** Cached Point3D list with per-particle scale for efficient updates */
    private final List<Point3D> displayPoints = new ArrayList<>();

    private int[] shuffleMapping;

    private boolean useOrekit = true;
    private boolean useOde = false;

    private final boolean[] heroBodies = new boolean[NUM_ASTEROIDS];
    private final List<DBody> odeBodies = new ArrayList<>();
    private final List<Integer> odeBodyIndices = new ArrayList<>();
    private DWorld odeWorld;
    private boolean odeInitialized = false;

    /** Single mesh for all asteroids (replaces meshSmall/meshMedium/meshLarge) */
    private ScatterMesh mesh;

    /** Color palette for per-particle coloring (gray gradient for asteroids) */
    private final List<Color> colorPalette = Arrays.asList(
            Color.DARKGRAY,
            Color.GRAY,
            Color.LIGHTGRAY
    );

    private AnimationTimer animationTimer;
    private boolean animating = true;
    private int frameCounter = 0;
    private long lastFrameNanos = 0;

    private final Random random = new Random(42);

    public AsteroidFieldWindow() {
        world.getTransforms().addAll(rotateX, rotateY, rotateZ);
        subScene = new SubScene(world, WINDOW_WIDTH, WINDOW_HEIGHT, true, SceneAntialiasing.BALANCED);
        initialize();
    }

    /**
     * Initializes camera, lighting, asteroids, and event handlers
     */
    private void initialize() {
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(-420);
        camera.setNearClip(0.1);
        camera.setFarClip(20000);

        setupLighting();
        world.getChildren().add(createCentralBody());

        generateAsteroidField();

        subScene.setFill(Color.BLACK);
        subScene.setCamera(camera);

        setupMouseHandlers();

        sceneRoot.getChildren().add(subScene);

        Scene scene = new Scene(sceneRoot, WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setScene(scene);
        updateWindowTitle();

        setupKeyHandlers();  // safe now

        subScene.setFocusTraversable(true);
        startAnimation();

        log.info("AsteroidFieldWindow initialized with {} asteroids (single mesh with per-particle scale)",
                NUM_ASTEROIDS);
    }

    /**
     * Adds ambient and directional lighting to the scene
     */
    private void setupLighting() {
        AmbientLight ambient = new AmbientLight(Color.rgb(180, 180, 180));
        PointLight sun = new PointLight(Color.rgb(220, 200, 150));
        sun.setTranslateX(0);
        sun.setTranslateY(0);
        sun.setTranslateZ(0);
        world.getChildren().addAll(ambient, sun);
    }

    private Sphere createCentralBody() {
        Sphere sphere = new Sphere(8);
        PhongMaterial mat = new PhongMaterial(Color.ORANGE);
        mat.setSpecularColor(Color.YELLOW);
        sphere.setMaterial(mat);
        return sphere;
    }

    /**
     * Generates randomized asteroid field and builds the mesh with per-particle scale.
     */
    private void generateAsteroidField() {
        asteroids.clear();
        displayPoints.clear();

        // Populates asteroid field with randomized orbital parameters
        for (int i = 0; i < NUM_ASTEROIDS; i++) {
            double radius = FIELD_INNER_RADIUS + random.nextDouble() * (FIELD_OUTER_RADIUS - FIELD_INNER_RADIUS);
            double eccentricity = random.nextDouble() * 0.06;
            double inclinationDeg = (random.nextDouble() - 0.5) * MAX_INCLINATION_DEG;
            double inclinationRad = Math.toRadians(inclinationDeg);
            double height = (random.nextDouble() - 0.5) * FIELD_THICKNESS;
            double angle = random.nextDouble() * 2 * Math.PI;
            double speed = BASE_ANGULAR_SPEED / Math.sqrt(radius / FIELD_INNER_RADIUS);
            speed *= 0.8 + random.nextDouble() * 0.4;
            double size = MIN_SIZE + random.nextDouble() * (MAX_SIZE - MIN_SIZE);

            asteroids.add(new AsteroidData(radius, speed, height, eccentricity, inclinationRad, size));
            angles[i] = angle;

            // Compute initial display position
            double r = radius * (1 - eccentricity * eccentricity) / (1 + eccentricity * Math.cos(angle));
            double x = r * Math.cos(angle);
            double z = r * Math.sin(angle);

            // Calculate scale relative to base size
            float scale = (float) (size / BASE_SIZE);

            // Map size to color index (smaller = darker, larger = lighter)
            int colorIndex = (int) ((size - MIN_SIZE) / (MAX_SIZE - MIN_SIZE) * 255);

            // Create Point3D with position, colorIndex, and scale
            displayPoints.add(new Point3D((float) x, (float) height, (float) z, colorIndex, scale));
        }

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < NUM_ASTEROIDS; i++) indices.add(i);
        Collections.shuffle(indices, random);

        shuffleMapping = new int[NUM_ASTEROIDS];
        for (int i = 0; i < NUM_ASTEROIDS; i++) {
            shuffleMapping[i] = indices.get(i);
        }

        assignHeroBodies();
        buildMesh();
    }

    /**
     * Builds the single mesh with per-particle scale.
     */
    private void buildMesh() {
        if (mesh != null) {
            world.getChildren().remove(mesh);
        }

        mesh = new ScatterMesh(displayPoints, true, BASE_SIZE, 0);
        mesh.enablePerParticleAttributes(colorPalette);
        mesh.setCullFace(CullFace.NONE);
        world.getChildren().add(mesh);
    }

    /**
     * Updates mesh positions efficiently without full rebuild.
     * Can be called every frame.
     *
     * @return true if efficient update was used
     */
    private boolean updateMeshPositions() {
        if (mesh == null || displayPoints.isEmpty()) {
            return false;
        }
        return mesh.updatePositions(displayPoints);
    }

    private void startAnimation() {
        lastFrameNanos = System.nanoTime();

        // Defines frame handler; updates positions; uses efficient mesh updates
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

                // Clamp to avoid physics explosions after pauses or debugger stops
                deltaSeconds = Math.min(deltaSeconds, MAX_DELTA_SECONDS);

                // Scale relative to target frame rate for consistent speed
                double timeScale = deltaSeconds / TARGET_FRAME_SECONDS;

                updateAsteroidPositions(timeScale, deltaSeconds);

                // Use efficient position update every frame
                if (!updateMeshPositions()) {
                    // Fall back to full rebuild if efficient update fails
                    buildMesh();
                }
            }
        };
        animationTimer.start();
    }

    /**
     * Updates asteroid positions using ODE or analytic model.
     *
     * @param timeScale multiplier for angular speed (1.0 at 60fps)
     * @param deltaSeconds actual elapsed time for ODE physics
     */
    private void updateAsteroidPositions(double timeScale, double deltaSeconds) {
        boolean odeActive = false;
        if (useOde) {
            initializeOdeIfNeeded(deltaSeconds);
            odeActive = odeInitialized && !odeBodies.isEmpty();
            if (odeActive) {
                stepOdeSimulation(deltaSeconds);
            }
        }

        // Updates asteroid positions using ODE or analytic method
        for (int i = 0; i < NUM_ASTEROIDS; i++) {
            if (odeActive && heroBodies[i]) {
                continue;
            }

            AsteroidData asteroid = asteroids.get(i);
            // Scale angular movement by actual frame time
            double angle = angles[i] + asteroid.speed() * timeScale;
            angles[i] = angle;

            Point3D position = useOrekit
                    ? computeOrekitPosition(asteroid, angle)
                    : computeAnalyticPosition(asteroid, angle);

            // Update position while preserving colorIndex and scale
            Point3D existing = displayPoints.get(i);
            existing.x = position.x;
            existing.y = position.y;
            existing.z = position.z;
        }

        if (odeActive) {
            syncHeroPositionsFromOde();
        }
    }

    private void assignHeroBodies() {
        for (int i = 0; i < heroBodies.length; i++) {
            heroBodies[i] = false;
        }
        for (int i = 0; i < HERO_BODY_COUNT && i < shuffleMapping.length; i++) {
            int idx = shuffleMapping[i];
            heroBodies[idx] = true;
        }
    }

    /**
     * Initializes ODE world and bodies if needed.
     *
     * @param deltaSeconds time step for velocity calculation
     */
    private void initializeOdeIfNeeded(double deltaSeconds) {
        if (odeInitialized) return;

        OdeHelper.initODE();
        odeWorld = OdeHelper.createWorld();
        odeWorld.setGravity(0, 0, 0);

        odeBodies.clear();
        odeBodyIndices.clear();
        // Creates and positions ODE bodies for hero asteroids
        for (int i = 0; i < NUM_ASTEROIDS; i++) {
            if (!heroBodies[i]) continue;

            AsteroidData asteroid = asteroids.get(i);
            double angle = angles[i];

            DBody body = OdeHelper.createBody(odeWorld);
            DMass mass = OdeHelper.createMass();
            mass.setSphereTotal(0.1, 0.2);
            body.setMass(mass);

            // Use stored orbital parameters including height
            Point3D startPos = useOrekit
                    ? computeOrekitPosition(asteroid, angle)
                    : computeAnalyticPosition(asteroid, angle);
            body.setPosition(startPos.x, startPos.y, startPos.z);

            // Tangential velocity based on angular speed
            // v = r * omega, where omega = speed (radians per frame at 60fps)
            // Convert to velocity per second: v = r * speed / targetFrameTime
            double v = asteroid.radius() * asteroid.speed() / TARGET_FRAME_SECONDS;
            double vx = -v * Math.sin(angle);
            double vz = v * Math.cos(angle);
            double vy = 0.0;
            double inc = asteroid.inclination();
            double vyRot = vy * Math.cos(inc) - vz * Math.sin(inc);
            double vzRot = vy * Math.sin(inc) + vz * Math.cos(inc);
            body.setLinearVel(vx, vyRot, vzRot);

            odeBodies.add(body);
            odeBodyIndices.add(i);
        }

        odeInitialized = true;
        log.info("ODE initialized with {} hero bodies", odeBodies.size());
    }

    /**
     * Steps the ODE physics simulation.
     *
     * @param deltaSeconds actual elapsed time for this frame
     */
    private void stepOdeSimulation(double deltaSeconds) {
        // Applies inward acceleration based on hero speed
        for (int i = 0; i < odeBodies.size(); i++) {
            DBody body = odeBodies.get(i);
            int idx = odeBodyIndices.get(i);
            AsteroidData asteroid = asteroids.get(idx);

            double x = body.getPosition().get0();
            double y = body.getPosition().get1();
            double z = body.getPosition().get2();

            double r2 = x * x + y * y + z * z;
            double r = Math.sqrt(r2);
            if (r < 1e-6) {
                continue;
            }

            double radius = Math.max(r, 1e-6);
            // Velocity per second (not per frame)
            double v = radius * asteroid.speed() / TARGET_FRAME_SECONDS;
            double accel = -(v * v) / radius;
            double invR = 1.0 / radius;
            body.addForce(x * accel * invR, y * accel * invR, z * accel * invR);
        }

        // Step physics with actual delta time
        odeWorld.quickStep(deltaSeconds);
    }

    private void syncHeroPositionsFromOde() {
        int bodyIndex = 0;
        // Copies ODE positions to display points
        for (int i = 0; i < NUM_ASTEROIDS; i++) {
            if (!heroBodies[i]) continue;

            DBody body = odeBodies.get(bodyIndex++);
            double x = body.getPosition().get0();
            double y = body.getPosition().get1();
            double z = body.getPosition().get2();

            // Update position while preserving colorIndex and scale
            Point3D existing = displayPoints.get(i);
            existing.x = (float) x;
            existing.y = (float) y;
            existing.z = (float) z;
        }
    }

    /**
     * Computes position from Keplerian orbital elements
     */
    private Point3D computeOrekitPosition(AsteroidData asteroid, double trueAnomaly) {
        KeplerianOrbit orbit = new KeplerianOrbit(
                asteroid.radius(),
                asteroid.eccentricity(),
                asteroid.inclination(),
                0.0,
                0.0,
                trueAnomaly,
                PositionAngle.TRUE,
                FramesFactory.getGCRF(),
                ORBIT_EPOCH,
                VISUAL_SCALE_MU
        );

        PVCoordinates pv = orbit.getPVCoordinates(ORBIT_EPOCH, FramesFactory.getGCRF());
        double x = pv.getPosition().getX();
        double y = pv.getPosition().getY() + asteroid.height();
        double z = pv.getPosition().getZ();

        return new Point3D((float) x, (float) y, (float) z);
    }

    /**
     * Computes position from orbital elements and height
     */
    private Point3D computeAnalyticPosition(AsteroidData asteroid, double trueAnomaly) {
        // Correct formula for radius at true anomaly: r = a(1-e²)/(1+e*cos(ν))
        double r = asteroid.radius() * (1 - asteroid.eccentricity() * asteroid.eccentricity())
                / (1 + asteroid.eccentricity() * Math.cos(trueAnomaly));

        double x = r * Math.cos(trueAnomaly);
        double z = r * Math.sin(trueAnomaly);

        double inc = asteroid.inclination();
        double height = asteroid.height();
        double y = height * Math.cos(inc) - z * Math.sin(inc);
        z = height * Math.sin(inc) + z * Math.cos(inc);

        return new Point3D((float) x, (float) y, (float) z);
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

        // Rotates or translates camera based on mouse button
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
        // Sets key press event handler on the scene
        if (scene != null) {
            scene.setOnKeyPressed(e -> {
                switch (e.getCode()) {
                    case SPACE -> toggleAnimation();
                    case O     -> toggleOrekit();
                    case D     -> toggleOde();
                    case R     -> resetView();
                }
            });
        }
    }

    /**
     * Resets camera transform and logs action
     */
    private void resetView() {
        rotateX.setAngle(30);
        rotateY.setAngle(0);
        rotateZ.setAngle(0);
        camera.setTranslateX(0);
        camera.setTranslateY(0);
        camera.setTranslateZ(-420);
        log.info("View reset");
    }

    public void toggleAnimation() {
        animating = !animating;
        log.info("Animation {}", animating ? "resumed" : "paused");
    }

    public void toggleOrekit() {
        useOrekit = !useOrekit;
        updateWindowTitle();
        log.info("Orekit {}", useOrekit ? "enabled" : "disabled");
    }

    public void toggleOde() {
        useOde = !useOde;
        if (useOde) {
            cleanupOde();
        }
        updateWindowTitle();
        log.info("ODE {}", useOde ? "enabled" : "disabled");
    }

    /**
     * Cleans up ODE resources
     */
    private void cleanupOde() {
        if (odeWorld != null) {
            for (DBody body : odeBodies) {
                body.destroy();
            }
            odeWorld.destroy();
            odeWorld = null;
        }
        odeBodies.clear();
        odeBodyIndices.clear();
        odeInitialized = false;
    }

    private void updateWindowTitle() {
        String mode = useOrekit ? "Orekit" : "Analytic";
        String odeSuffix = useOde ? " + ODE" : "";
        stage.setTitle(WINDOW_TITLE_BASE + " [" + mode + odeSuffix + "]");
    }

    public void show() {
        stage.show();
        subScene.requestFocus();
        log.info("AsteroidFieldWindow shown");
    }

    public void dispose() {
        if (animationTimer != null) animationTimer.stop();
        cleanupOde();
    }

    public Stage getStage() { return stage; }
    public Group getWorld() { return world; }
    public PerspectiveCamera getCamera() { return camera; }
}
