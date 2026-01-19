package com.teamgannon.trips.dialogs.solarsystem;

import com.teamgannon.trips.planetarymodelling.procedural.AdjacencyGraph;
import com.teamgannon.trips.planetarymodelling.procedural.BoundaryDetector;
import com.teamgannon.trips.planetarymodelling.procedural.BoundaryDetector.BoundaryType;
import com.teamgannon.trips.planetarymodelling.procedural.JavaFxPlanetMeshConverter;
import com.teamgannon.trips.planetarymodelling.procedural.PlateAssigner;
import com.teamgannon.trips.planetarymodelling.procedural.PlanetGenerator.GeneratedPlanet;
import com.teamgannon.trips.planetarymodelling.procedural.Polygon;
import javafx.geometry.Insets;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

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
    private boolean showPlateBoundaries = false;
    private boolean showClimateZones = false;
    private boolean autoRotate = false;

    // Animation
    private Timeline rotationAnimation;

    // Legend
    private VBox legendPanel;
    private boolean showLegend = false;

    // Atmosphere visualization
    private Sphere atmosphereSphere;

    // Generated planet data
    private final GeneratedPlanet generatedPlanet;
    private final String planetName;
    private final double[] rainfall;
    private final double[] preciseHeights;
    private final AdjacencyGraph adjacency;
    private final PlateAssigner.PlateAssignment plateAssignment;
    private final BoundaryDetector.BoundaryAnalysis boundaryAnalysis;

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
        this.plateAssignment = planet.plateAssignment();
        this.boundaryAnalysis = planet.boundaryAnalysis();

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

        // Create the legend panel (initially hidden)
        legendPanel = createLegend();
        legendPanel.setVisible(showLegend);

        // Wrap SubScene and legend in StackPane for overlay
        StackPane viewPane = new StackPane();
        viewPane.getChildren().addAll(subScene, legendPanel);
        StackPane.setAlignment(legendPanel, Pos.TOP_RIGHT);
        StackPane.setMargin(legendPanel, new Insets(10));

        // Create dialog content
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(viewPane, createControlBar());
        VBox.setVgrow(viewPane, Priority.ALWAYS);

        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Set minimum size
        getDialogPane().setMinWidth(750);
        getDialogPane().setMinHeight(650);

        // Stop animation when dialog closes
        setOnCloseRequest(event -> stopAnimation());
        setResultConverter(button -> {
            stopAnimation();
            return null;
        });

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

        // Add plate boundaries if enabled
        if (showPlateBoundaries) {
            addPlateBoundaries();
        }

        // Add climate zone indicators if enabled
        if (showClimateZones) {
            addClimateZones();
        }
    }

    /**
     * Add river visualization as gradient-colored lines.
     * Rivers transition from thin/light at source to thick/dark at mouth.
     * Width is based on cumulative flow (sum of rainfall from upstream polygons).
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

            // Calculate cumulative flow along river based on rainfall
            double[] cumulativeFlow = calculateCumulativeFlow(river);
            double maxFlow = cumulativeFlow[cumulativeFlow.length - 1];
            if (maxFlow <= 0) maxFlow = 1.0;  // Avoid division by zero

            // Create river as a series of cylinders with flow-based width
            for (int i = 0; i < river.size() - 1; i++) {
                int polyIdx1 = river.get(i);
                int polyIdx2 = river.get(i + 1);

                if (polyIdx1 < 0 || polyIdx1 >= polygons.size() ||
                    polyIdx2 < 0 || polyIdx2 >= polygons.size()) {
                    continue;
                }

                // Calculate normalized flow at this segment (0 to 1)
                double flowRatio = cumulativeFlow[i + 1] / maxFlow;

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

                // Create flow-weighted river segment
                javafx.scene.shape.Cylinder riverSegment = createFlowBasedRiverSegment(
                    p1, p2, flowRatio, isFrozen);
                planetGroup.getChildren().add(riverSegment);
            }
        }

        log.debug("Added {} rivers to planet visualization", rivers.size());
    }

    /**
     * Calculate cumulative flow along a river path.
     * Flow accumulates based on rainfall at each polygon, simulating tributaries.
     *
     * @param river List of polygon indices from source to mouth
     * @return Array of cumulative flow values (one per river segment endpoint)
     */
    private double[] calculateCumulativeFlow(List<Integer> river) {
        double[] flow = new double[river.size()];
        double cumulative = 0.0;

        // Base flow contribution per polygon
        double baseFlowPerSegment = 0.5;

        for (int i = 0; i < river.size(); i++) {
            int polyIdx = river.get(i);

            // Add rainfall contribution if available
            double contribution = baseFlowPerSegment;
            if (rainfall != null && polyIdx >= 0 && polyIdx < rainfall.length) {
                // More rainfall = more water entering the river
                contribution += rainfall[polyIdx] * 0.5;
            }

            cumulative += contribution;
            flow[i] = cumulative;
        }

        return flow;
    }

    /**
     * Create a river segment with width based on cumulative flow.
     *
     * @param start     Start point
     * @param end       End point
     * @param flowRatio Normalized flow (0 = source, 1 = max flow at mouth)
     * @param frozen    Whether this river ends frozen
     * @return Cylinder representing the river segment
     */
    private javafx.scene.shape.Cylinder createFlowBasedRiverSegment(
            Point3D start, Point3D end, double flowRatio, boolean frozen) {

        Point3D midpoint = start.midpoint(end);
        double length = start.distance(end);

        // Flow-based width: use square root scaling for natural appearance
        // Small streams: 0.002, Large rivers: 0.008
        // Square root gives better visual distribution than linear
        double minRadius = 0.002;
        double maxRadius = 0.008;
        double radius = minRadius + Math.sqrt(flowRatio) * (maxRadius - minRadius);

        javafx.scene.shape.Cylinder cylinder = new javafx.scene.shape.Cylinder(radius, length);

        // Color transitions along river (based on flow for color too)
        PhongMaterial material = new PhongMaterial();
        Color riverColor;

        if (frozen) {
            // Frozen river: light cyan → white/ice as flow increases
            Color sourceColor = Color.rgb(135, 206, 250);   // Light sky blue
            Color terminusColor = Color.rgb(224, 255, 255); // Light cyan/ice
            riverColor = sourceColor.interpolate(terminusColor, flowRatio);
        } else {
            // Normal river: light blue (stream) → dark blue (wide river)
            Color sourceColor = Color.rgb(100, 180, 255);   // Light blue (mountain spring)
            Color mouthColor = Color.rgb(0, 80, 160);       // Dark blue (wide river)
            riverColor = sourceColor.interpolate(mouthColor, flowRatio);
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
     * Add plate boundary visualization as colored lines.
     * Boundary types are color-coded:
     * - CONVERGENT (subduction/collision): Red
     * - DIVERGENT (spreading): Cyan/Green
     * - TRANSFORM (lateral): Yellow/Orange
     * - INACTIVE: Gray
     */
    private void addPlateBoundaries() {
        if (plateAssignment == null || boundaryAnalysis == null || adjacency == null) {
            log.warn("Cannot render plate boundaries: missing plate data");
            return;
        }

        List<Polygon> polygons = generatedPlanet.polygons();
        int[] plateIndex = plateAssignment.plateIndex();
        int[] heights = generatedPlanet.heights();

        // Track which boundary edges we've already drawn to avoid duplicates
        java.util.Set<String> drawnEdges = new java.util.HashSet<>();
        int boundaryCount = 0;

        // Iterate through all polygons to find boundaries
        for (int polyIdx = 0; polyIdx < polygons.size(); polyIdx++) {
            int plate1 = plateIndex[polyIdx];
            int[] neighbors = adjacency.neighborsOnly(polyIdx);

            for (int neighborIdx : neighbors) {
                int plate2 = plateIndex[neighborIdx];

                // Skip if same plate (no boundary)
                if (plate1 == plate2) continue;

                // Create unique edge key to avoid drawing same edge twice
                String edgeKey = Math.min(polyIdx, neighborIdx) + "-" + Math.max(polyIdx, neighborIdx);
                if (drawnEdges.contains(edgeKey)) continue;
                drawnEdges.add(edgeKey);

                // Get boundary type between these plates
                BoundaryDetector.PlatePair pair = new BoundaryDetector.PlatePair(plate1, plate2);
                BoundaryType boundaryType = boundaryAnalysis.boundaries().get(pair);
                if (boundaryType == null) {
                    boundaryType = BoundaryType.INACTIVE;
                }

                // Get positions for the boundary segment
                Polygon poly1 = polygons.get(polyIdx);
                Polygon poly2 = polygons.get(neighborIdx);

                // Calculate boundary line position (midpoint between polygon centers)
                // Offset slightly above terrain
                double height1 = heights[polyIdx];
                double height2 = heights[neighborIdx];
                double avgHeight = (height1 + height2) / 2.0;
                double displacement = 1.0 + avgHeight * 0.025;

                Point3D p1 = toPoint3D(poly1.center().normalize()
                    .scalarMultiply(PLANET_SCALE * displacement * 1.004));
                Point3D p2 = toPoint3D(poly2.center().normalize()
                    .scalarMultiply(PLANET_SCALE * displacement * 1.004));

                // Create boundary segment with appropriate color
                javafx.scene.shape.Cylinder boundarySegment = createBoundarySegment(p1, p2, boundaryType);
                planetGroup.getChildren().add(boundarySegment);
                boundaryCount++;
            }
        }

        log.debug("Added {} plate boundary segments", boundaryCount);
    }

    /**
     * Create a colored cylinder for a plate boundary segment.
     */
    private javafx.scene.shape.Cylinder createBoundarySegment(Point3D start, Point3D end, BoundaryType type) {
        Point3D midpoint = start.midpoint(end);
        double length = start.distance(end);

        // Boundary line width based on type
        double radius = (type == BoundaryType.CONVERGENT || type == BoundaryType.DIVERGENT) ? 0.004 : 0.003;
        javafx.scene.shape.Cylinder cylinder = new javafx.scene.shape.Cylinder(radius, length);

        // Color based on boundary type
        PhongMaterial material = new PhongMaterial();
        Color boundaryColor = switch (type) {
            case CONVERGENT -> Color.rgb(220, 60, 60);     // Red - subduction/collision
            case DIVERGENT -> Color.rgb(60, 200, 180);     // Cyan - spreading ridges
            case TRANSFORM -> Color.rgb(220, 180, 60);     // Yellow/Orange - strike-slip
            case INACTIVE -> Color.rgb(120, 120, 120);     // Gray - inactive
        };

        material.setDiffuseColor(boundaryColor);
        material.setSpecularColor(Color.WHITE.deriveColor(0, 1, 0.3, 1));
        cylinder.setMaterial(material);

        // Position at midpoint
        cylinder.setTranslateX(midpoint.getX());
        cylinder.setTranslateY(midpoint.getY());
        cylinder.setTranslateZ(midpoint.getZ());

        // Rotate to align with segment direction
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
     * Add climate zone visualization as translucent colored bands.
     * - Tropical (±30°): Red/Orange tint
     * - Temperate (30°-60°): Green tint
     * - Polar (60°-90°): Blue/White tint
     */
    private void addClimateZones() {
        List<Polygon> polygons = generatedPlanet.polygons();
        var climates = generatedPlanet.climates();

        if (climates == null || climates.length == 0) {
            log.warn("Cannot render climate zones: no climate data");
            return;
        }

        // Create translucent spherical shells for each climate zone
        // We'll create latitude rings to indicate zone boundaries

        // Tropical zone boundary ring (±30°)
        addLatitudeRing(30.0, Color.rgb(255, 200, 100, 0.4), "Tropical boundary");
        addLatitudeRing(-30.0, Color.rgb(255, 200, 100, 0.4), "Tropical boundary");

        // Temperate-Polar boundary ring (±60°)
        addLatitudeRing(60.0, Color.rgb(100, 200, 255, 0.5), "Polar boundary");
        addLatitudeRing(-60.0, Color.rgb(100, 200, 255, 0.5), "Polar boundary");

        log.debug("Added climate zone boundaries");
    }

    /**
     * Add a latitude ring at the specified latitude.
     */
    private void addLatitudeRing(double latitudeDegrees, Color color, String description) {
        double latRad = Math.toRadians(latitudeDegrees);
        double ringRadius = PLANET_SCALE * Math.cos(latRad) * 1.008; // Slightly above surface
        double y = PLANET_SCALE * Math.sin(latRad) * 1.008;

        // Create a torus-like ring using small spheres along the latitude
        int segments = 72; // Number of points around the ring
        for (int i = 0; i < segments; i++) {
            double angle = 2.0 * Math.PI * i / segments;
            double x = ringRadius * Math.cos(angle);
            double z = ringRadius * Math.sin(angle);

            Sphere dot = new Sphere(0.008);
            PhongMaterial material = new PhongMaterial(color);
            dot.setMaterial(material);
            dot.setTranslateX(x);
            dot.setTranslateY(y);
            dot.setTranslateZ(z);

            planetGroup.getChildren().add(dot);
        }
    }

    private Point3D toPoint3D(org.hipparchus.geometry.euclidean.threed.Vector3D v) {
        return new Point3D(v.getX(), v.getY(), v.getZ());
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

        // Plate boundaries checkbox
        CheckBox plateBoundariesCheckBox = new CheckBox("Plates");
        plateBoundariesCheckBox.setSelected(showPlateBoundaries);
        plateBoundariesCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showPlateBoundaries = newVal;
            renderPlanet();
        });
        // Disable if no plate data
        boolean hasPlateData = plateAssignment != null && boundaryAnalysis != null;
        plateBoundariesCheckBox.setDisable(!hasPlateData);

        // Climate zones checkbox
        CheckBox climateZonesCheckBox = new CheckBox("Climate");
        climateZonesCheckBox.setSelected(showClimateZones);
        climateZonesCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showClimateZones = newVal;
            renderPlanet();
        });
        // Disable if no climate data
        boolean hasClimateData = generatedPlanet.climates() != null && generatedPlanet.climates().length > 0;
        climateZonesCheckBox.setDisable(!hasClimateData);

        // Auto-rotate checkbox
        CheckBox autoRotateCheckBox = new CheckBox("Spin");
        autoRotateCheckBox.setSelected(autoRotate);
        autoRotateCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            setAutoRotate(newVal);
        });

        // Legend checkbox
        CheckBox legendCheckBox = new CheckBox("Legend");
        legendCheckBox.setSelected(showLegend);
        legendCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            showLegend = newVal;
            if (legendPanel != null) {
                legendPanel.setVisible(showLegend);
            }
        });

        // Reset view button
        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> {
            resetView();
            autoRotateCheckBox.setSelected(false);
            setAutoRotate(false);
        });

        // Save screenshot button
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> saveScreenshot());

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
            plateBoundariesCheckBox,
            climateZonesCheckBox,
            new Separator(),
            colorByHeightCheckBox,
            rainfallCheckBox,
            smoothCheckBox,
            new Separator(),
            autoRotateCheckBox,
            legendCheckBox,
            resetButton,
            saveButton,
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

    /**
     * Initialize the auto-rotation animation.
     */
    private void initializeAnimation() {
        rotationAnimation = new Timeline(
            new KeyFrame(Duration.millis(30), event -> {
                if (autoRotate) {
                    rotateY.setAngle(rotateY.getAngle() + 0.3);
                }
            })
        );
        rotationAnimation.setCycleCount(Animation.INDEFINITE);
    }

    /**
     * Toggle auto-rotation on/off.
     */
    private void setAutoRotate(boolean enabled) {
        this.autoRotate = enabled;
        if (enabled) {
            if (rotationAnimation == null) {
                initializeAnimation();
            }
            rotationAnimation.play();
        } else {
            if (rotationAnimation != null) {
                rotationAnimation.pause();
            }
        }
    }

    /**
     * Stop animation when dialog closes.
     */
    private void stopAnimation() {
        if (rotationAnimation != null) {
            rotationAnimation.stop();
        }
    }

    /**
     * Create a color legend panel showing height-to-color mapping.
     * The legend displays terrain elevation colors from deep ocean to mountain peaks.
     */
    private VBox createLegend() {
        VBox legend = new VBox(3);
        legend.setPadding(new Insets(8));
        legend.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-background-radius: 5;");
        legend.setMaxWidth(100);
        legend.setMaxHeight(200);

        Label title = new Label("Elevation");
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11;");
        legend.getChildren().add(title);

        // Height levels with their colors (from JavaFxPlanetMeshConverter color mapping)
        // Heights range from -4 (deep ocean) to +4 (mountain peaks)
        String[][] legendItems = {
            {"4", "Snow Peak", "#FFFFFF"},
            {"3", "Mountain", "#8B7355"},
            {"2", "Highland", "#9B8B5B"},
            {"1", "Lowland", "#6B8E23"},
            {"0", "Coast", "#90B060"},
            {"-1", "Shallow", "#5090C0"},
            {"-2", "Ocean", "#3070A0"},
            {"-3", "Deep Sea", "#204080"},
            {"-4", "Abyss", "#102050"}
        };

        for (String[] item : legendItems) {
            HBox row = new HBox(5);
            row.setAlignment(Pos.CENTER_LEFT);

            Rectangle colorBox = new Rectangle(14, 12);
            colorBox.setFill(Color.web(item[2]));
            colorBox.setStroke(Color.gray(0.5));
            colorBox.setStrokeWidth(0.5);

            Label label = new Label(item[1]);
            label.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 9;");

            row.getChildren().addAll(colorBox, label);
            legend.getChildren().add(row);
        }

        // Add plate boundary legend if plate data exists
        if (plateAssignment != null && boundaryAnalysis != null) {
            legend.getChildren().add(new Separator());
            Label plateTitle = new Label("Boundaries");
            plateTitle.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11;");
            legend.getChildren().add(plateTitle);

            String[][] plateItems = {
                {"Convergent", "#DC3C3C"},
                {"Divergent", "#3CC8B4"},
                {"Transform", "#DCB43C"},
                {"Inactive", "#787878"}
            };

            for (String[] item : plateItems) {
                HBox row = new HBox(5);
                row.setAlignment(Pos.CENTER_LEFT);

                Rectangle colorBox = new Rectangle(14, 4);
                colorBox.setFill(Color.web(item[1]));

                Label label = new Label(item[0]);
                label.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 9;");

                row.getChildren().addAll(colorBox, label);
                legend.getChildren().add(row);
            }
        }

        return legend;
    }

    /**
     * Save the current view as a PNG screenshot.
     */
    private void saveScreenshot() {
        // Pause rotation during screenshot
        boolean wasRotating = autoRotate;
        if (wasRotating) {
            setAutoRotate(false);
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Planet Screenshot");
        fileChooser.setInitialFileName(planetName.replaceAll("[^a-zA-Z0-9]", "_") + "_terrain.png");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PNG Image", "*.png")
        );

        File file = fileChooser.showSaveDialog(getDialogPane().getScene().getWindow());
        if (file != null) {
            try {
                WritableImage image = subScene.snapshot(null, null);
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                log.info("Saved screenshot to: {}", file.getAbsolutePath());
            } catch (IOException e) {
                log.error("Failed to save screenshot: {}", e.getMessage());
            }
        }

        // Resume rotation if it was active
        if (wasRotating) {
            setAutoRotate(true);
        }
    }
}
