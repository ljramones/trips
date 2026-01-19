package com.teamgannon.trips.dialogs.solarsystem;

import com.teamgannon.trips.planetarymodelling.procedural.AdjacencyGraph;
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
import javafx.scene.shape.Sphere;
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
    private boolean showRainfallHeatmap = false;
    private boolean useSmoothTerrain = false;
    private boolean showAtmosphere = true;

    // Atmosphere visualization
    private Sphere atmosphereSphere;

    // Generated planet data
    private final GeneratedPlanet generatedPlanet;
    private final String planetName;
    private final double[] rainfall;
    private final double[] preciseHeights;
    private final AdjacencyGraph adjacency;

    /**
     * Create a new procedural planet viewer dialog.
     *
     * @param planetName The name of the planet being viewed
     * @param planet     The generated planet data
     */
    public ProceduralPlanetViewerDialog(String planetName, GeneratedPlanet planet) {
        this.planetName = planetName;
        this.generatedPlanet = planet;
        this.rainfall = planet.rainfall();
        this.preciseHeights = planet.preciseHeights();
        this.adjacency = planet.adjacency();

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

        // Add atmosphere glow
        createAtmosphere();

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
     * Create an atmosphere glow effect around the planet.
     * Uses a semi-transparent blue sphere slightly larger than the planet.
     */
    private void createAtmosphere() {
        // Remove existing atmosphere if any
        if (atmosphereSphere != null) {
            world.getChildren().remove(atmosphereSphere);
        }

        if (!showAtmosphere) {
            return;
        }

        // Create atmosphere sphere (5% larger than planet)
        double atmosphereRadius = PLANET_SCALE * 1.05;
        atmosphereSphere = new Sphere(atmosphereRadius);

        // Create semi-transparent blue atmosphere material
        PhongMaterial atmosphereMaterial = new PhongMaterial();

        // Atmosphere color varies based on planet water content
        // More water = bluer atmosphere (Earth-like)
        // Less water = thinner/lighter atmosphere
        double waterFraction = generatedPlanet.config() != null
            ? generatedPlanet.config().waterFraction()
            : 0.66;

        // Color ranges from pale white (dry) to blue (wet)
        Color atmosphereColor;
        if (waterFraction > 0.5) {
            // Water world - deeper blue atmosphere
            atmosphereColor = Color.rgb(100, 150, 255, 0.12);
        } else if (waterFraction > 0.2) {
            // Moderate water - lighter blue
            atmosphereColor = Color.rgb(135, 180, 255, 0.08);
        } else {
            // Dry world - thin, pale atmosphere
            atmosphereColor = Color.rgb(180, 200, 230, 0.05);
        }

        atmosphereMaterial.setDiffuseColor(atmosphereColor);
        atmosphereMaterial.setSpecularColor(Color.TRANSPARENT);
        atmosphereSphere.setMaterial(atmosphereMaterial);

        // Render both sides for atmosphere glow effect
        atmosphereSphere.setCullFace(CullFace.NONE);

        world.getChildren().add(atmosphereSphere);
        log.debug("Created atmosphere with radius {} and water fraction {}",
            atmosphereRadius, waterFraction);
    }

    /**
     * Toggle atmosphere visibility.
     */
    private void updateAtmosphere() {
        if (atmosphereSphere != null) {
            atmosphereSphere.setVisible(showAtmosphere);
        }
    }

    /**
     * Render the planet terrain mesh.
     * Supports multiple visualization modes: height-colored, rainfall heatmap, smooth terrain.
     */
    private void renderPlanet() {
        planetGroup.getChildren().clear();

        List<Polygon> polygons = generatedPlanet.polygons();
        int[] heights = generatedPlanet.heights();

        if (showRainfallHeatmap && rainfall != null && rainfall.length > 0) {
            // Render with rainfall heatmap coloring
            Map<Integer, TriangleMesh> meshByRainfall = JavaFxPlanetMeshConverter.convertByRainfall(
                polygons, heights, rainfall, PLANET_SCALE);

            for (Map.Entry<Integer, TriangleMesh> entry : meshByRainfall.entrySet()) {
                int bucket = entry.getKey();
                TriangleMesh mesh = entry.getValue();

                MeshView meshView = new MeshView(mesh);
                meshView.setMaterial(JavaFxPlanetMeshConverter.createMaterialForRainfall(bucket));
                meshView.setCullFace(CullFace.BACK);
                meshView.setDrawMode(showWireframe ? DrawMode.LINE : DrawMode.FILL);

                planetGroup.getChildren().add(meshView);
            }
            log.debug("Rendered planet with rainfall heatmap: {} polygons, {} rainfall buckets",
                polygons.size(), meshByRainfall.size());

        } else if (useColorByHeight) {
            // Render with separate meshes per height level, using neighbor-averaged vertices
            // Pass preciseHeights for finer averaging when available
            Map<Integer, TriangleMesh> meshByHeight = adjacency != null
                ? JavaFxPlanetMeshConverter.convertByHeightWithAveraging(
                    polygons, heights, adjacency, PLANET_SCALE, preciseHeights)
                : JavaFxPlanetMeshConverter.convertByHeight(polygons, heights, PLANET_SCALE);

            for (Map.Entry<Integer, TriangleMesh> entry : meshByHeight.entrySet()) {
                int height = entry.getKey();
                TriangleMesh mesh = entry.getValue();

                MeshView meshView = new MeshView(mesh);
                meshView.setMaterial(JavaFxPlanetMeshConverter.createMaterialForHeight(height));
                meshView.setCullFace(CullFace.BACK);
                meshView.setDrawMode(showWireframe ? DrawMode.LINE : DrawMode.FILL);

                planetGroup.getChildren().add(meshView);
            }
            log.debug("Rendered planet with {} polygons, {} height-grouped meshes",
                polygons.size(), meshByHeight.size());

        } else {
            // Single mesh with averaged color or smooth terrain
            TriangleMesh mesh;
            PhongMaterial material;

            if (useSmoothTerrain && preciseHeights != null && preciseHeights.length > 0) {
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
            log.debug("Rendered planet with {} polygons, single mesh",
                polygons.size());
        }

        // Add rivers if enabled
        if (showRivers && generatedPlanet.rivers() != null && !generatedPlanet.rivers().isEmpty()) {
            addRivers();
        }
    }

    /**
     * Add river visualization as gradient-colored lines.
     * Rivers transition from thin/light at source to thick/dark at mouth.
     * Frozen rivers (ending in polar zones) use ice-blue coloring.
     */
    private void addRivers() {
        List<List<Integer>> rivers = generatedPlanet.rivers();
        List<Polygon> polygons = generatedPlanet.polygons();
        boolean[] frozenTerminus = generatedPlanet.frozenRiverTerminus();
        int[] heights = generatedPlanet.heights();

        for (int riverIdx = 0; riverIdx < rivers.size(); riverIdx++) {
            List<Integer> river = rivers.get(riverIdx);
            if (river.size() < 2) continue;

            boolean isFrozen = frozenTerminus != null && riverIdx < frozenTerminus.length && frozenTerminus[riverIdx];
            int riverLength = river.size();

            // Create river as a series of cylinders with gradient coloring
            for (int i = 0; i < river.size() - 1; i++) {
                int polyIdx1 = river.get(i);
                int polyIdx2 = river.get(i + 1);

                if (polyIdx1 < 0 || polyIdx1 >= polygons.size() ||
                    polyIdx2 < 0 || polyIdx2 >= polygons.size()) {
                    continue;
                }

                // Calculate progress along river (0 = source, 1 = mouth)
                double progress = (double) i / (riverLength - 1);

                // Get height displacement for river to follow terrain
                int height1 = heights[polyIdx1];
                int height2 = heights[polyIdx2];
                double avgHeight = (height1 + height2) / 2.0;
                double displacement = 1.0 + avgHeight * 0.025; // Slightly above terrain

                // Position river segment above the terrain
                Point3D p1 = toPoint3D(polygons.get(polyIdx1).center().normalize()
                    .scalarMultiply(PLANET_SCALE * displacement * 1.003));
                Point3D p2 = toPoint3D(polygons.get(polyIdx2).center().normalize()
                    .scalarMultiply(PLANET_SCALE * displacement * 1.003));

                // Create gradient-styled river segment
                javafx.scene.shape.Cylinder riverSegment = createGradientRiverSegment(
                    p1, p2, progress, isFrozen);
                planetGroup.getChildren().add(riverSegment);
            }
        }

        log.debug("Added {} rivers to planet visualization", rivers.size());
    }

    private Point3D toPoint3D(org.hipparchus.geometry.euclidean.threed.Vector3D v) {
        return new Point3D(v.getX(), v.getY(), v.getZ());
    }

    /**
     * Create a gradient-styled river segment with varying width and color.
     *
     * @param start    Start point
     * @param end      End point
     * @param progress Progress along river (0=source, 1=mouth)
     * @param frozen   Whether this river ends frozen
     * @return Cylinder representing the river segment
     */
    private javafx.scene.shape.Cylinder createGradientRiverSegment(
            Point3D start, Point3D end, double progress, boolean frozen) {

        Point3D midpoint = start.midpoint(end);
        double length = start.distance(end);

        // River width increases toward mouth (0.002 at source, 0.006 at mouth)
        double radius = 0.002 + progress * 0.004;
        javafx.scene.shape.Cylinder cylinder = new javafx.scene.shape.Cylinder(radius, length);

        // Color transitions along river
        PhongMaterial material = new PhongMaterial();
        Color riverColor;

        if (frozen) {
            // Frozen river: light cyan at source → white/ice at terminus
            Color sourceColor = Color.rgb(135, 206, 250);  // Light sky blue
            Color terminusColor = Color.rgb(224, 255, 255); // Light cyan/ice
            riverColor = sourceColor.interpolate(terminusColor, progress);
        } else {
            // Normal river: light blue at source → dark blue at mouth
            Color sourceColor = Color.rgb(100, 180, 255);  // Light blue (mountain spring)
            Color mouthColor = Color.rgb(0, 80, 160);      // Dark blue (wide river)
            riverColor = sourceColor.interpolate(mouthColor, progress);
        }

        material.setDiffuseColor(riverColor);
        material.setSpecularColor(Color.WHITE.deriveColor(0, 1, 0.5, 1));
        material.setSpecularPower(25.0); // Shiny water effect
        cylinder.setMaterial(material);

        // Position at midpoint
        cylinder.setTranslateX(midpoint.getX());
        cylinder.setTranslateY(midpoint.getY());
        cylinder.setTranslateZ(midpoint.getZ());

        // Rotate to align with the segment direction
        Point3D direction = end.subtract(start).normalize();
        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D rotationAxis = yAxis.crossProduct(direction);
        double rotationAngle = Math.acos(Math.max(-1, Math.min(1, yAxis.dotProduct(direction))));

        if (rotationAxis.magnitude() > 0.0001) {
            Rotate rotate = new Rotate(Math.toDegrees(rotationAngle), rotationAxis);
            cylinder.getTransforms().add(rotate);
        }

        return cylinder;
    }

    /**
     * Create a simple river segment (legacy method, kept for reference).
     */
    private javafx.scene.shape.Cylinder createRiverSegment(Point3D start, Point3D end, boolean frozen) {
        return createGradientRiverSegment(start, end, 0.5, frozen);
    }

    /**
     * Create the control bar with options.
     */
    private HBox createControlBar() {
        HBox controlBar = new HBox(10);
        controlBar.setPadding(new Insets(10, 0, 0, 0));

        // Zoom slider
        Label zoomLabel = new Label("Zoom:");
        Slider zoomSlider = new Slider(-8, -1.5, INITIAL_CAMERA_DISTANCE);
        zoomSlider.setPrefWidth(120);
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
        // Disable rivers checkbox if no rivers exist
        int riverCount = generatedPlanet.rivers() != null ? generatedPlanet.rivers().size() : 0;
        riversCheckBox.setDisable(riverCount == 0);

        // Wireframe checkbox
        CheckBox wireframeCheckBox = new CheckBox("Wireframe");
        wireframeCheckBox.setSelected(showWireframe);
        wireframeCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showWireframe = newVal;
            renderPlanet();
        });

        // Color mode checkbox
        CheckBox colorByHeightCheckBox = new CheckBox("Terrain");
        colorByHeightCheckBox.setSelected(useColorByHeight);
        colorByHeightCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            useColorByHeight = newVal;
            if (newVal) {
                showRainfallHeatmap = false;
            }
            renderPlanet();
        });

        // Rainfall heatmap checkbox
        CheckBox rainfallCheckBox = new CheckBox("Rainfall");
        rainfallCheckBox.setSelected(showRainfallHeatmap);
        rainfallCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showRainfallHeatmap = newVal;
            if (newVal) {
                useColorByHeight = false;
                colorByHeightCheckBox.setSelected(false);
            }
            renderPlanet();
        });
        // Disable if no rainfall data
        boolean hasRainfall = rainfall != null && rainfall.length > 0;
        rainfallCheckBox.setDisable(!hasRainfall);

        // Smooth terrain checkbox
        CheckBox smoothCheckBox = new CheckBox("Smooth");
        smoothCheckBox.setSelected(useSmoothTerrain);
        smoothCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            useSmoothTerrain = newVal;
            renderPlanet();
        });
        // Disable if no precise heights
        boolean hasPreciseHeights = preciseHeights != null && preciseHeights.length > 0;
        smoothCheckBox.setDisable(!hasPreciseHeights);

        // Atmosphere checkbox
        CheckBox atmosphereCheckBox = new CheckBox("Atmo");
        atmosphereCheckBox.setSelected(showAtmosphere);
        atmosphereCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showAtmosphere = newVal;
            updateAtmosphere();
        });

        // Reset view button
        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> resetView());

        // Info label with more details
        String infoText = String.format("Poly: %d | Rivers: %d",
            generatedPlanet.polygons().size(),
            riverCount);
        if (hasRainfall) {
            // Calculate average rainfall for display
            double avgRain = 0;
            for (double r : rainfall) avgRain += r;
            avgRain /= rainfall.length;
            infoText += String.format(" | Avg Rain: %.1f", avgRain);
        }
        Label infoLabel = new Label(infoText);
        infoLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 10;");

        controlBar.getChildren().addAll(
            zoomLabel, zoomSlider,
            new Separator(),
            riversCheckBox,
            wireframeCheckBox,
            atmosphereCheckBox,
            new Separator(),
            colorByHeightCheckBox,
            rainfallCheckBox,
            smoothCheckBox,
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
