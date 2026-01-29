package com.teamgannon.trips.experimental;

import javafx.animation.AnimationTimer;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;
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
import org.ode4j.ode.DBody;
import org.ode4j.ode.DMass;
import org.ode4j.ode.DWorld;
import org.ode4j.ode.OdeHelper;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.PVCoordinates;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Simple, reliable procedural asteroid field (no Orekit).
 * Restored from your earlier working versions.
 */
@Slf4j
public class AsteroidFieldWindow {

    private static final double WINDOW_WIDTH  = 1200;
    private static final double WINDOW_HEIGHT = 900;

    private static final int    NUM_ASTEROIDS      = 5000;
    private static final double FIELD_INNER_RADIUS = 95;
    private static final double FIELD_OUTER_RADIUS = 105;
    private static final double FIELD_THICKNESS    = 8.0;
    private static final double MAX_INCLINATION_DEG = 12.0;

    private static final double BASE_ANGULAR_SPEED = 0.002;
    private static final int    MESH_REFRESH_INTERVAL = 5;

    private static final double SIZE_SMALL   = 1.0;
    private static final double SIZE_MEDIUM  = 1.8;
    private static final double SIZE_LARGE   = 2.6;

    private static final double THRESH_MEDIUM = 1.4;
    private static final double THRESH_LARGE  = 2.2;

    private static final AbsoluteDate ORBIT_EPOCH = AbsoluteDate.J2000_EPOCH;
    private static final double CENTRAL_MU = Constants.JPL_SSD_SUN_GM;

    private static final String WINDOW_TITLE_BASE = "Asteroid Field – Procedural (working version)";

    private static final int HERO_BODY_COUNT = 120;
    private static final double ODE_STEP_SECONDS = 0.016;
    // ODE uses a visual-time scale to match the analytic per-frame angular speeds.
    private static final double ODE_TIME_STEP = ODE_STEP_SECONDS;

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

    private final List<Double> radii         = new ArrayList<>();
    private final List<Double> angles        = new ArrayList<>();
    private final List<Double> speeds        = new ArrayList<>();
    private final List<Double> heights       = new ArrayList<>();
    private final List<Double> eccentricities = new ArrayList<>();
    private final List<Double> inclinations  = new ArrayList<>();
    private final List<Double> sizes         = new ArrayList<>();

    private List<Point3D> displayPositions = new ArrayList<>();

    private int[] shuffleMapping;

    private boolean useOrekit = true;
    private boolean useOde = false;

    private final boolean[] heroBodies = new boolean[NUM_ASTEROIDS];
    private final List<DBody> odeBodies = new ArrayList<>();
    private final List<Integer> odeBodyIndices = new ArrayList<>();
    private DWorld odeWorld;
    private boolean odeInitialized = false;

    private ScatterMesh meshSmall;
    private ScatterMesh meshMedium;
    private ScatterMesh meshLarge;

    private AnimationTimer animationTimer;
    private boolean animating = true;
    private int frameCounter = 0;

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

        log.info("AsteroidFieldWindow initialized with {} asteroids", NUM_ASTEROIDS);
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
     * Generates randomized asteroid field and assigns hero bodies
     */
    private void generateAsteroidField() {
        clearLists();

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
            double size = 0.9 + random.nextDouble() * 1.7;

            radii.add(radius);
            eccentricities.add(eccentricity);
            inclinations.add(inclinationRad);
            heights.add(height);
            angles.add(angle);
            speeds.add(speed);
            sizes.add(size);

            double r = radius * (1 - eccentricity * Math.cos(angle));
            double x = r * Math.cos(angle);
            double z = r * Math.sin(angle);
            displayPositions.add(new Point3D((float) x, (float) height, (float) z));
        }

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < NUM_ASTEROIDS; i++) indices.add(i);
        Collections.shuffle(indices, random);

        shuffleMapping = new int[NUM_ASTEROIDS];
        for (int i = 0; i < NUM_ASTEROIDS; i++) {
            shuffleMapping[i] = indices.get(i);
        }

        assignHeroBodies();
        refreshMeshes();
    }

    /**
     * Clears lists to prepare for recalculation
     */
    private void clearLists() {
        radii.clear();
        angles.clear();
        speeds.clear();
        heights.clear();
        eccentricities.clear();
        inclinations.clear();
        sizes.clear();
        displayPositions.clear();
    }

    /**
     * Rebuilds meshes from size‑partitioned display positions
     */
    private void refreshMeshes() {
        world.getChildren().removeAll(meshSmall, meshMedium, meshLarge);

        List<Point3D> small  = new ArrayList<>();
        List<Point3D> medium = new ArrayList<>();
        List<Point3D> large  = new ArrayList<>();

        // Populates size‑based mesh lists from shuffled positions
        for (int dispIdx = 0; dispIdx < NUM_ASTEROIDS; dispIdx++) {
            int origIdx = shuffleMapping[dispIdx];
            Point3D p = displayPositions.get(origIdx);
            double sz = sizes.get(origIdx);

            if (sz >= THRESH_LARGE)      large.add(p);
            else if (sz >= THRESH_MEDIUM) medium.add(p);
            else                          small.add(p);
        }

        if (!small.isEmpty()) {
            meshSmall = new ScatterMesh(small, true, SIZE_SMALL, 0);
            meshSmall.setTextureModeNone(Color.LIGHTGRAY);
            world.getChildren().add(meshSmall);
        }

        if (!medium.isEmpty()) {
            meshMedium = new ScatterMesh(medium, true, SIZE_MEDIUM, 0);
            meshMedium.setTextureModeNone(Color.LIGHTGRAY);
            world.getChildren().add(meshMedium);
        }

        if (!large.isEmpty()) {
            meshLarge = new ScatterMesh(large, true, SIZE_LARGE, 0);
            meshLarge.setTextureModeNone(Color.LIGHTGRAY);
            world.getChildren().add(meshLarge);
        }
    }

    private void startAnimation() {
        // Defines frame handler; updates positions; refreshes meshes periodically
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!animating) return;

                updateAsteroidPositions();

                frameCounter++;
                if (frameCounter % MESH_REFRESH_INTERVAL == 0) {
                    refreshMeshes();
                }
            }
        };
        animationTimer.start();
    }

    /**
     * Updates asteroid positions using ODE or analytic model
     */
    private void updateAsteroidPositions() {
        boolean odeActive = false;
        if (useOde) {
            initializeOdeIfNeeded();
            odeActive = odeInitialized && !odeBodies.isEmpty();
            if (odeActive) {
                stepOdeSimulation();
            }
        }

        // Updates asteroid positions using ODE or analytic method
        for (int i = 0; i < NUM_ASTEROIDS; i++) {
            if (odeActive && heroBodies[i]) {
                continue;
            }

            double angle = angles.get(i) + speeds.get(i);
            angles.set(i, angle);

            double radius = radii.get(i);
            double ecc = 0.0;
            double inc = inclinations.get(i);

            Point3D position = useOrekit
                    ? computeOrekitPosition(radius, ecc, inc, angle, heights.get(i))
                    : computeAnalyticPosition(radius, ecc, inc, angle, heights.get(i));

            displayPositions.set(i, position);
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
     * Initializes ODE world and bodies if needed
     */
    private void initializeOdeIfNeeded() {
        if (odeInitialized) return;

        OdeHelper.initODE();
        odeWorld = OdeHelper.createWorld();
        odeWorld.setGravity(0, 0, 0);

        odeBodies.clear();
        odeBodyIndices.clear();
        // Creates and positions ODE bodies for hero asteroids
        for (int i = 0; i < NUM_ASTEROIDS; i++) {
            if (!heroBodies[i]) continue;

            double angle = angles.get(i);
            double radius = radii.get(i);
            double height = 0.0;
            double ecc = 0.0;
            double inc = 0.0;

            DBody body = OdeHelper.createBody(odeWorld);
            DMass mass = OdeHelper.createMass();
            mass.setSphereTotal(0.1, 0.2);
            body.setMass(mass);
            Point3D startPos = useOrekit
                    ? computeOrekitPosition(radius, ecc, inc, angle, height)
                    : computeAnalyticPosition(radius, ecc, inc, angle, height);
            body.setPosition(startPos.x, startPos.y, startPos.z);

            // Tangential velocity to match the analytic per-frame angular speed.
            double v = radius * speeds.get(i) / ODE_TIME_STEP;
            double vx = -v * Math.sin(angle);
            double vz = v * Math.cos(angle);
            double vy = 0.0;
            double vyRot = vy * Math.cos(inc) - vz * Math.sin(inc);
            double vzRot = vy * Math.sin(inc) + vz * Math.cos(inc);
            body.setLinearVel(vx, vyRot, vzRot);

            odeBodies.add(body);
            odeBodyIndices.add(i);
        }

        odeInitialized = true;
        log.info("ODE initialized with {} hero bodies", odeBodies.size());
    }

    private void stepOdeSimulation() {
        // Applies inward acceleration based on hero speed
        for (int i = 0; i < odeBodies.size(); i++) {
            DBody body = odeBodies.get(i);
            int idx = odeBodyIndices.get(i);
            double x = body.getPosition().get0();
            double y = body.getPosition().get1();
            double z = body.getPosition().get2();

            double r2 = x * x + y * y + z * z;
            double r = Math.sqrt(r2);
            if (r < 1e-6) {
                continue;
            }

            double radius = Math.max(r, 1e-6);
            double v = radius * speeds.get(idx) / ODE_TIME_STEP;
            double accel = -(v * v) / radius;
            double invR = 1.0 / radius;
            body.addForce(x * accel * invR, y * accel * invR, z * accel * invR);
        }

        odeWorld.quickStep(ODE_STEP_SECONDS);
    }

    private void syncHeroPositionsFromOde() {
        int bodyIndex = 0;
        // Copies ODE positions to display positions
        for (int i = 0; i < NUM_ASTEROIDS; i++) {
            if (!heroBodies[i]) continue;

            DBody body = odeBodies.get(bodyIndex++);
            double x = body.getPosition().get0();
            double y = body.getPosition().get1();
            double z = body.getPosition().get2();
            displayPositions.set(i, new Point3D((float) x, (float) y, (float) z));
        }
    }

    /**
     * Computes position from Keplerian orbital elements
     */
    private Point3D computeOrekitPosition(double radius, double eccentricity, double inclination,
                                          double trueAnomaly, double heightOffset) {
        KeplerianOrbit orbit = new KeplerianOrbit(
                radius,
                eccentricity,
                inclination,
                0.0,
                0.0,
                trueAnomaly,
                PositionAngle.TRUE,
                FramesFactory.getGCRF(),
                ORBIT_EPOCH,
                CENTRAL_MU
        );

        PVCoordinates pv = orbit.getPVCoordinates(ORBIT_EPOCH, FramesFactory.getGCRF());
        double x = pv.getPosition().getX();
        double y = pv.getPosition().getY() + heightOffset;
        double z = pv.getPosition().getZ();

        return new Point3D((float) x, (float) y, (float) z);
    }

    /**
     * Computes position from orbital elements and height
     */
    private Point3D computeAnalyticPosition(double radius, double eccentricity, double inclination,
                                            double trueAnomaly, double heightOffset) {
        double r = radius * (1 - eccentricity * Math.cos(trueAnomaly));

        double x = r * Math.cos(trueAnomaly);
        double z = r * Math.sin(trueAnomaly);

        double y = heightOffset * Math.cos(inclination) - z * Math.sin(inclination);
        z = heightOffset * Math.sin(inclination) + z * Math.cos(inclination);

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
            odeInitialized = false;
            odeBodies.clear();
        }
        updateWindowTitle();
        log.info("ODE {}", useOde ? "enabled" : "disabled");
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
    }

    public Stage getStage() { return stage; }
    public Group getWorld() { return world; }
    public PerspectiveCamera getCamera() { return camera; }
}
