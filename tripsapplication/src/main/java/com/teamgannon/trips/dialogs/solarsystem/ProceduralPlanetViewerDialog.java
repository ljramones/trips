package com.teamgannon.trips.dialogs.solarsystem;

import com.teamgannon.trips.planetarymodelling.procedural.JavaFxPlanetMeshConverter;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetGenerator.GeneratedPlanet;
import com.teamgannon.trips.planetarymodelling.procedural.Polygon;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Dialog for viewing procedurally generated planet terrain.
 * Displays the icosahedral mesh terrain in a 3D viewer with rotation and zoom controls.
 */
@Slf4j
public class ProceduralPlanetViewerDialog extends Dialog<Void> {

    private static final double SCENE_WIDTH = 700;
    private static final double SCENE_HEIGHT = 550;
    private static final double PLANET_SCALE = 1.0;
    private static final double INITIAL_CAMERA_DISTANCE = -4.0;

    private final SubScene subScene;
    private final PerspectiveCamera camera;
    private final Group world;
    private final Group planetGroup;

    // Rotation transforms
    private final Rotate rotateX = new Rotate(25, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(25, Rotate.Y_AXIS);

    // Mouse drag state
    private double mouseX, mouseY;
    private double mouseOldX, mouseOldY;

    // Rendering options
    private boolean showWireframe = false;
    private boolean showRivers = false;
    private boolean useColorByHeight = true;

    // Generated planet data
    private final GeneratedPlanet generatedPlanet;
    private final String planetName;

    /**
     * Create a new procedural planet viewer dialog.
     *
     * @param planetName The name of the planet being viewed
     * @param planet     The generated planet data
     */
    public ProceduralPlanetViewerDialog(String planetName, GeneratedPlanet planet) {
        this.planetName = planetName;
        this.generatedPlanet = planet;

        setTitle("Terrain: " + planetName);
        setResizable(true);

        // Create 3D scene
        world = new Group();
        planetGroup = new Group();
        world.getChildren().add(planetGroup);

        // Apply rotation transforms to world
        world.getTransforms().addAll(rotateX, rotateY);

        // Create camera
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.01);
        camera.setFarClip(100);
        camera.setTranslateZ(INITIAL_CAMERA_DISTANCE);

        // Create SubScene
        subScene = new SubScene(world, SCENE_WIDTH, SCENE_HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.rgb(10, 10, 25));
        subScene.setCamera(camera);

        // Add lighting
        addLighting();

        // Render the planet mesh
        renderPlanet();

        // Set up mouse interaction
        setupMouseHandlers();

        // Create dialog content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(subScene, createControlBar());
        VBox.setVgrow(subScene, Priority.ALWAYS);

        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Set minimum size
        getDialogPane().setMinWidth(750);
        getDialogPane().setMinHeight(650);

        log.info("Created procedural planet viewer for: {}", planetName);
    }

    /**
     * Add ambient and directional lighting to the scene.
     */
    private void addLighting() {
        // Ambient light for overall illumination
        AmbientLight ambientLight = new AmbientLight(Color.rgb(60, 60, 70));
        world.getChildren().add(ambientLight);

        // Main directional light (sun-like)
        PointLight sunLight = new PointLight(Color.rgb(255, 250, 240));
        sunLight.setTranslateX(5);
        sunLight.setTranslateY(-3);
        sunLight.setTranslateZ(-8);
        world.getChildren().add(sunLight);

        // Fill light from opposite side (softer)
        PointLight fillLight = new PointLight(Color.rgb(100, 110, 140));
        fillLight.setTranslateX(-3);
        fillLight.setTranslateY(2);
        fillLight.setTranslateZ(-5);
        world.getChildren().add(fillLight);
    }

    /**
     * Render the planet terrain mesh.
     */
    private void renderPlanet() {
        planetGroup.getChildren().clear();

        List<Polygon> polygons = generatedPlanet.polygons();
        int[] heights = generatedPlanet.heights();
        double[] preciseHeights = generatedPlanet.preciseHeights();

        if (useColorByHeight) {
            // Render with separate meshes per height level for proper coloring
            Map<Integer, TriangleMesh> meshByHeight = JavaFxPlanetMeshConverter.convertByHeight(
                polygons, heights, PLANET_SCALE);

            for (Map.Entry<Integer, TriangleMesh> entry : meshByHeight.entrySet()) {
                int height = entry.getKey();
                TriangleMesh mesh = entry.getValue();

                MeshView meshView = new MeshView(mesh);
                meshView.setMaterial(JavaFxPlanetMeshConverter.createMaterialForHeight(height));
                meshView.setCullFace(CullFace.BACK);
                meshView.setDrawMode(showWireframe ? DrawMode.LINE : DrawMode.FILL);

                planetGroup.getChildren().add(meshView);
            }
        } else {
            // Single mesh with averaged color
            TriangleMesh mesh;
            PhongMaterial material;

            if (preciseHeights != null && preciseHeights.length > 0) {
                mesh = JavaFxPlanetMeshConverter.convertSmooth(polygons, preciseHeights, PLANET_SCALE);
                material = JavaFxPlanetMeshConverter.createSmoothTerrainMaterial(polygons, preciseHeights);
            } else {
                mesh = JavaFxPlanetMeshConverter.convert(polygons, heights, PLANET_SCALE);
                material = JavaFxPlanetMeshConverter.createTerrainMaterial(polygons, heights);
            }

            MeshView meshView = new MeshView(mesh);
            meshView.setMaterial(material);
            meshView.setCullFace(CullFace.BACK);
            meshView.setDrawMode(showWireframe ? DrawMode.LINE : DrawMode.FILL);

            planetGroup.getChildren().add(meshView);
        }

        // Add rivers if enabled
        if (showRivers && generatedPlanet.rivers() != null && !generatedPlanet.rivers().isEmpty()) {
            addRivers();
        }

        log.debug("Rendered planet with {} polygons, {} height-grouped meshes",
            polygons.size(), useColorByHeight ? 9 : 1);
    }

    /**
     * Add river visualization as lines.
     */
    private void addRivers() {
        List<List<Integer>> rivers = generatedPlanet.rivers();
        List<Polygon> polygons = generatedPlanet.polygons();
        boolean[] frozenTerminus = generatedPlanet.frozenRiverTerminus();

        for (int riverIdx = 0; riverIdx < rivers.size(); riverIdx++) {
            List<Integer> river = rivers.get(riverIdx);
            if (river.size() < 2) continue;

            boolean isFrozen = frozenTerminus != null && riverIdx < frozenTerminus.length && frozenTerminus[riverIdx];

            // Create river as a series of small cylinders or lines
            for (int i = 0; i < river.size() - 1; i++) {
                int polyIdx1 = river.get(i);
                int polyIdx2 = river.get(i + 1);

                if (polyIdx1 < 0 || polyIdx1 >= polygons.size() ||
                    polyIdx2 < 0 || polyIdx2 >= polygons.size()) {
                    continue;
                }

                Point3D p1 = toPoint3D(polygons.get(polyIdx1).center().normalize().scalarMultiply(PLANET_SCALE * 1.01));
                Point3D p2 = toPoint3D(polygons.get(polyIdx2).center().normalize().scalarMultiply(PLANET_SCALE * 1.01));

                // Create a simple line segment as a thin cylinder
                javafx.scene.shape.Cylinder riverSegment = createRiverSegment(p1, p2, isFrozen);
                planetGroup.getChildren().add(riverSegment);
            }
        }
    }

    private Point3D toPoint3D(org.hipparchus.geometry.euclidean.threed.Vector3D v) {
        return new Point3D(v.getX(), v.getY(), v.getZ());
    }

    private javafx.scene.shape.Cylinder createRiverSegment(Point3D start, Point3D end, boolean frozen) {
        Point3D midpoint = start.midpoint(end);
        double length = start.distance(end);

        javafx.scene.shape.Cylinder cylinder = new javafx.scene.shape.Cylinder(0.003, length);

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(frozen ? Color.LIGHTCYAN : Color.DODGERBLUE);
        material.setSpecularColor(Color.WHITE);
        cylinder.setMaterial(material);

        // Position at midpoint
        cylinder.setTranslateX(midpoint.getX());
        cylinder.setTranslateY(midpoint.getY());
        cylinder.setTranslateZ(midpoint.getZ());

        // Rotate to align with the segment direction
        Point3D direction = end.subtract(start).normalize();
        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D rotationAxis = yAxis.crossProduct(direction);
        double rotationAngle = Math.acos(yAxis.dotProduct(direction));

        if (rotationAxis.magnitude() > 0.0001) {
            Rotate rotate = new Rotate(Math.toDegrees(rotationAngle), rotationAxis);
            cylinder.getTransforms().add(rotate);
        }

        return cylinder;
    }

    /**
     * Create the control bar with options.
     */
    private HBox createControlBar() {
        HBox controlBar = new HBox(15);
        controlBar.setPadding(new Insets(10, 0, 0, 0));

        // Zoom slider
        Label zoomLabel = new Label("Zoom:");
        Slider zoomSlider = new Slider(-8, -1.5, INITIAL_CAMERA_DISTANCE);
        zoomSlider.setPrefWidth(150);
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            camera.setTranslateZ(newVal.doubleValue());
        });

        // Rivers checkbox
        CheckBox riversCheckBox = new CheckBox("Rivers");
        riversCheckBox.setSelected(showRivers);
        riversCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showRivers = newVal;
            renderPlanet();
        });

        // Wireframe checkbox
        CheckBox wireframeCheckBox = new CheckBox("Wireframe");
        wireframeCheckBox.setSelected(showWireframe);
        wireframeCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showWireframe = newVal;
            renderPlanet();
        });

        // Color mode checkbox
        CheckBox colorByHeightCheckBox = new CheckBox("Color by Height");
        colorByHeightCheckBox.setSelected(useColorByHeight);
        colorByHeightCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            useColorByHeight = newVal;
            renderPlanet();
        });

        // Reset view button
        Button resetButton = new Button("Reset View");
        resetButton.setOnAction(e -> resetView());

        // Info label
        Label infoLabel = new Label(String.format("Polygons: %d | Rivers: %d",
            generatedPlanet.polygons().size(),
            generatedPlanet.rivers() != null ? generatedPlanet.rivers().size() : 0));
        infoLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 10;");

        controlBar.getChildren().addAll(
            zoomLabel, zoomSlider,
            new Separator(),
            riversCheckBox,
            wireframeCheckBox,
            colorByHeightCheckBox,
            new Separator(),
            resetButton,
            new Separator(),
            infoLabel
        );

        return controlBar;
    }

    /**
     * Set up mouse handlers for rotation and zoom.
     */
    private void setupMouseHandlers() {
        // Mouse press - record initial position
        subScene.setOnMousePressed(event -> {
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        // Mouse drag - rotate the view
        subScene.setOnMouseDragged(event -> {
            mouseOldX = mouseX;
            mouseOldY = mouseY;
            mouseX = event.getSceneX();
            mouseY = event.getSceneY();

            double deltaX = mouseX - mouseOldX;
            double deltaY = mouseY - mouseOldY;

            double modifier = 0.3;

            if (event.isPrimaryButtonDown()) {
                // Rotate around Y axis (horizontal drag)
                rotateY.setAngle(rotateY.getAngle() + deltaX * modifier);
                // Rotate around X axis (vertical drag)
                rotateX.setAngle(rotateX.getAngle() - deltaY * modifier);

                // Clamp X rotation to prevent flipping
                if (rotateX.getAngle() > 85) rotateX.setAngle(85);
                if (rotateX.getAngle() < -85) rotateX.setAngle(-85);
            }
        });

        // Mouse scroll - zoom
        subScene.setOnScroll(event -> {
            double deltaY = event.getDeltaY();
            double newZ = camera.getTranslateZ() + deltaY * 0.01;
            // Clamp zoom
            newZ = Math.max(-8, Math.min(-1.5, newZ));
            camera.setTranslateZ(newZ);
        });
    }

    /**
     * Reset the view to initial state.
     */
    private void resetView() {
        rotateX.setAngle(25);
        rotateY.setAngle(25);
        camera.setTranslateZ(INITIAL_CAMERA_DISTANCE);
    }
}
