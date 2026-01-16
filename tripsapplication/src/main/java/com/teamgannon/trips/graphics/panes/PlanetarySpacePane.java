package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.config.application.ScreenSize;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.events.ContextSelectionType;
import com.teamgannon.trips.events.ContextSelectorEvent;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.planetary.PlanetaryContext;
import com.teamgannon.trips.planetary.rendering.PlanetarySkyRenderer;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import com.teamgannon.trips.solarsystem.nightsky.PlanetarySkyModel;
import com.teamgannon.trips.solarsystem.nightsky.PlanetarySkyModelBuilder;
import com.teamgannon.trips.solarsystem.nightsky.VisibleStarResult;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.transform.Rotate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import com.teamgannon.trips.jpa.repository.StarObjectRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Displays a night sky view from a planet's surface.
 * Shows stars repositioned based on the planet's location in 3D space,
 * with the host star visible as the "sun" and proper magnitude adjustments.
 */
@Slf4j
@Component
public class PlanetarySpacePane extends Pane {

    /**
     * Rotation controls
     */
    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    private final TripsContext tripsContext;
    private final ApplicationEventPublisher eventPublisher;
    private final StarObjectRepository starObjectRepository;

    // Mouse position tracking
    private double mousePosX, mousePosY = 0;
    private double mouseOldX, mouseOldY = 0;
    private double mouseDeltaX, mouseDeltaY = 0;

    /**
     * Graphical groups
     */
    private final Group world = new Group();
    private final Group root = new Group();
    private final Group labelGroup = new Group();
    private final Group skyGroup = new Group();
    private final Canvas orientationCanvas = new Canvas();

    /**
     * SubScene for 3D rendering
     */
    private final SubScene subScene;

    /**
     * Perspective camera
     */
    private final PerspectiveCamera camera = new PerspectiveCamera(true);

    /**
     * Sky renderer
     */
    @Getter
    private final PlanetarySkyRenderer skyRenderer;

    /**
     * Current planetary context
     */
    @Getter
    private PlanetaryContext currentContext;

    /**
     * List of nearby stars for rendering
     */
    private List<StarDisplayRecord> nearbyStars = new ArrayList<>();
    private List<PlanetarySkyRenderer.BrightStarEntry> computedBrightestStars = new ArrayList<>();
    private int visibleStarCount = 0;
    private final PlanetarySkyModelBuilder skyModelBuilder = new PlanetarySkyModelBuilder();

    /**
     * Constructor
     */
    public PlanetarySpacePane(TripsContext tripsContext,
                              ApplicationEventPublisher eventPublisher,
                              StarObjectRepository starObjectRepository) {
        this.tripsContext = tripsContext;
        this.eventPublisher = eventPublisher;
        this.starObjectRepository = starObjectRepository;

        ScreenSize screenSize = tripsContext.getScreenSize();

        // Initialize the sky renderer
        this.skyRenderer = new PlanetarySkyRenderer();

        // Add the sky group to world
        world.getChildren().add(skyGroup);

        // Attach rotation transforms
        world.getTransforms().addAll(rotateX, rotateY, rotateZ);

        subScene = new SubScene(world, screenSize.getSceneWidth(), screenSize.getSceneHeight(),
                true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        setInitialView();

        subScene.setCamera(camera);
        Group sceneRoot = new Group(subScene);
        orientationCanvas.setMouseTransparent(true);
        orientationCanvas.setVisible(false);
        orientationCanvas.widthProperty().bind(subScene.widthProperty());
        orientationCanvas.heightProperty().bind(subScene.heightProperty());
        sceneRoot.getChildren().add(orientationCanvas);
        sceneRoot.getChildren().add(labelGroup);

        this.setBackground(Background.EMPTY);
        this.getChildren().add(sceneRoot);
        this.setPickOnBounds(false);

        subScene.widthProperty().bind(this.widthProperty());
        subScene.heightProperty().bind(this.heightProperty());
        subScene.widthProperty().addListener((obs, oldVal, newVal) -> redrawOrientationGrid());
        subScene.heightProperty().addListener((obs, oldVal, newVal) -> redrawOrientationGrid());

        root.getChildren().add(this);

        handleMouseEvents();
    }

    /**
     * Set the planetary context and render the sky.
     *
     * @param context the planetary viewing context
     */
    public void setContext(PlanetaryContext context) {
        this.currentContext = context;

        if (context == null) {
            log.warn("Null planetary context provided");
            return;
        }

        // Create the title legend
        createLegend(context);

        setViewingDirection(context.getViewingAzimuth(), context.getViewingAltitude());

        // Render the sky dome
        recomputeSky();
    }

    /**
     * Set the list of nearby stars for rendering.
     *
     * @param stars list of star display records
     */
    public void setNearbyStars(List<StarDisplayRecord> stars) {
        this.nearbyStars = stars != null ? new ArrayList<>(stars) : new ArrayList<>();
    }

    /**
     * Render the sky dome.
     */
    private void renderSky() {
        // Clear previous rendering
        skyGroup.getChildren().clear();

        if (currentContext == null) {
            return;
        }

        // Use the renderer to create the sky dome
        Group renderedSky = skyRenderer.render(currentContext, nearbyStars);
        skyGroup.getChildren().add(renderedSky);

        log.info("Rendered planetary sky from {}", currentContext.getPlanetName());
    }

    public void recomputeSky() {
        if (currentContext == null) {
            return;
        }
        List<StarObject> allStars = new ArrayList<>();
        for (StarObject star : starObjectRepository.findAll()) {
            allStars.add(star);
        }

        PlanetarySkyModel model = skyModelBuilder.build(currentContext, allStars);
        nearbyStars = toStarDisplayRecords(model.getVisibleStars());
        computedBrightestStars = toBrightestEntries(model.getTopBrightest());
        visibleStarCount = model.getVisibleCount();
        boolean isDay = model.getHostStarAltitudeDeg() > 0.0;
        subScene.setFill(isDay ? Color.web("#6FA9E6") : Color.BLACK);
        currentContext.setShowHostStar(isDay);
        currentContext.setShowHorizon(!isDay);
        currentContext.setDaylight(isDay);
        currentContext.setHostStarAltitudeDeg(model.getHostStarAltitudeDeg());
        renderSky();
        redrawOrientationGrid();
    }

    public void updateLocalTime(double time) {
        if (currentContext == null) {
            return;
        }
        currentContext.setLocalTime(time);
        recomputeSky();
    }

    public void updateMagnitudeLimit(double magnitudeLimit) {
        if (currentContext == null) {
            return;
        }
        currentContext.setMagnitudeLimit(magnitudeLimit);
        recomputeSky();
    }

    public void updateAtmosphere(boolean enabled) {
        if (currentContext == null) {
            return;
        }
        currentContext.setShowAtmosphereEffects(enabled);
        renderSky();
    }

    public void updateOrientationGrid(boolean enabled) {
        if (currentContext == null) {
            return;
        }
        currentContext.setShowOrientationGrid(enabled);
        skyRenderer.setOrientationGridVisible(enabled);
        redrawOrientationGrid();
    }

    /**
     * Reset the view.
     * Camera must be INSIDE the sky dome (radius=500) for CullFace.FRONT to work.
     */
    public void reset() {
        log.info(">>> reset CALLED - rotateX BEFORE: {}", rotateX.getAngle());
        labelGroup.getChildren().clear();
        skyGroup.getChildren().clear();
        skyRenderer.clear();
        currentContext = null;
        nearbyStars.clear();

        // Reset camera position but NOT rotateX/Y - those are set by setContext
        rotateZ.setAngle(0);
        camera.setNearClip(5.0);
        camera.setTranslateY(-150);  // Vertical offset to push horizon down
        camera.setTranslateZ(-300);  // Inside the 500-radius dome
        log.info("    reset done - rotateX AFTER: {}", rotateX.getAngle());
    }

    /**
     * Set initial camera view.
     * Camera must be INSIDE the sky dome (radius=500) for CullFace.FRONT to work.
     */
    private void setInitialView() {
        log.info(">>> setInitialView CALLED - rotateX BEFORE: {}", rotateX.getAngle());
        camera.setNearClip(5.0);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-300);  // Inside the 500-radius dome (was -800)
        camera.setTranslateY(-150);  // Vertical offset to push horizon down

        // NOTE: Do NOT set rotateX here - let setContext/setViewingDirection control it
        log.info("    setInitialView done - rotateX AFTER: {}", rotateX.getAngle());
    }

    /**
     * Create the title legend.
     */
    private void createLegend(PlanetaryContext context) {
        labelGroup.getChildren().clear();

        GridPane titlePane = new GridPane();
        titlePane.setPrefWidth(550);
        labelGroup.getChildren().add(titlePane);

        // Planet name label
        Label locationLabel = new Label("Sky from " + context.getPlanetName());
        locationLabel.setFont(Font.font("Verdana", FontPosture.ITALIC, 18));
        locationLabel.setTextFill(Color.WHEAT);
        titlePane.add(locationLabel, 0, 0);

        Separator separator1 = new Separator();
        separator1.setMinWidth(20);
        titlePane.add(separator1, 1, 0);

        // Host star info
        Label hostLabel = new Label("Host: " + context.getHostStarName());
        hostLabel.setFont(Font.font("Verdana", FontPosture.REGULAR, 12));
        hostLabel.setTextFill(Color.LIGHTGRAY);
        titlePane.add(hostLabel, 2, 0);

        Separator separator2 = new Separator();
        separator2.setMinWidth(20);
        titlePane.add(separator2, 3, 0);

        // Return to solar system button
        Button returnToSystemButton = new Button("Return to System");
        returnToSystemButton.setOnAction(e -> returnToSolarSystem());
        titlePane.add(returnToSystemButton, 4, 0);

        Separator separator3 = new Separator();
        separator3.setMinWidth(10);
        titlePane.add(separator3, 5, 0);

        // Jump back to interstellar button
        Button jumpBackButton = new Button("Jump Back");
        jumpBackButton.setOnAction(e -> jumpBackToInterstellarSpace());
        titlePane.add(jumpBackButton, 6, 0);

        // Position at bottom of screen
        titlePane.setTranslateX(subScene.getWidth() - 530);
        titlePane.setTranslateY(subScene.getHeight() - 30);
        titlePane.setTranslateZ(0);
    }

    /**
     * Return to the solar system view.
     */
    private void returnToSolarSystem() {
        if (currentContext == null || currentContext.getHostStar() == null) {
            log.warn("No context to return to solar system");
            return;
        }

        // Publish event to switch back to solar system view
        eventPublisher.publishEvent(new ContextSelectorEvent(
                this,
                ContextSelectionType.SOLARSYSTEM,
                currentContext.getHostStar(),
                new HashMap<>()));
    }

    /**
     * Jump back to interstellar space.
     */
    private void jumpBackToInterstellarSpace() {
        eventPublisher.publishEvent(new ContextSelectorEvent(
                this,
                ContextSelectionType.INTERSTELLAR,
                null,
                new HashMap<>()));
    }

    /**
     * Handle mouse events for rotation and zoom.
     */
    private void handleMouseEvents() {
        // Scroll to zoom
        subScene.setOnScroll((ScrollEvent event) -> {
            double deltaY = event.getDeltaY();
            zoomView(deltaY * 2);
            redrawOrientationGrid();
        });

        // Mouse press
        subScene.setOnMousePressed((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });

        // Mouse drag for rotation
        subScene.setOnMouseDragged((MouseEvent me) -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);

            double modifier = 1.0;
            double modifierFactor = 0.1;

            if (me.isPrimaryButtonDown()) {
                // Rotate view - negate X delta to match negated azimuth
                rotateY.setAngle(((rotateY.getAngle() - mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);
                rotateX.setAngle(Math.max(-90, Math.min(90,
                        rotateX.getAngle() + mouseDeltaY * modifierFactor * modifier * 2.0)));
                redrawOrientationGrid();
            }
        });
    }

    /**
     * Zoom the view.
     * Camera must stay INSIDE the sky dome (radius=500) for CullFace.FRONT to work.
     */
    private void zoomView(double zoomAmt) {
        double z = camera.getTranslateZ();
        double newZ = z - zoomAmt;
        // Clamp zoom to keep camera inside the 500-radius dome
        newZ = Math.max(-450, Math.min(-50, newZ));
        camera.setTranslateZ(newZ);
    }

    /**
     * Update viewing direction.
     * rotateX controls pitch: positive = look up, negative = look down
     * rotateY controls azimuth: 0=North, 90=East, etc.
     */
    public void setViewingDirection(double azimuth, double altitude) {
        log.info(">>> setViewingDirection CALLED: azimuth={}, altitude={}", azimuth, altitude);
        log.info("    rotateX BEFORE: {}", rotateX.getAngle());
        log.info("    rotateY BEFORE: {}", rotateY.getAngle());

        rotateY.setAngle(-azimuth);  // NEGATED - rotating world vs rotating camera
        rotateX.setAngle(altitude);  // Positive: Y is negated in sky coords, so rotation flips

        log.info("    rotateX AFTER: {}", rotateX.getAngle());
        log.info("    rotateY AFTER: {}", rotateY.getAngle());
        log.info("    world.getTransforms() contains rotateX: {}", world.getTransforms().contains(rotateX));

        redrawOrientationGrid();
    }

    /**
     * Get the brightest stars from the current render.
     */
    public List<PlanetarySkyRenderer.BrightStarEntry> getBrightestStars() {
        return new ArrayList<>(computedBrightestStars);
    }

    public int getVisibleStarCount() {
        return visibleStarCount;
    }

    private List<StarDisplayRecord> toStarDisplayRecords(List<VisibleStarResult> results) {
        List<StarDisplayRecord> records = new ArrayList<>();
        for (VisibleStarResult result : results) {
            StarObject star = result.getStar();
            if (star == null) {
                continue;
            }
            StarDisplayRecord record = new StarDisplayRecord();
            record.setRecordId(star.getId());
            record.setStarName(resolveName(star));
            record.setMagnitude(result.getMagnitude());
            record.setDistance(star.getDistance());
            record.setSpectralClass(star.getSpectralClass());
            record.setX(star.getX());
            record.setY(star.getY());
            record.setZ(star.getZ());
            records.add(record);
        }
        return records;
    }

    private List<PlanetarySkyRenderer.BrightStarEntry> toBrightestEntries(List<VisibleStarResult> results) {
        List<PlanetarySkyRenderer.BrightStarEntry> entries = new ArrayList<>();
        for (VisibleStarResult result : results) {
            StarObject star = result.getStar();
            if (star == null) {
                continue;
            }
            StarDisplayRecord record = new StarDisplayRecord();
            record.setRecordId(star.getId());
            record.setStarName(resolveName(star));
            record.setMagnitude(result.getMagnitude());
            record.setDistance(star.getDistance());
            record.setSpectralClass(star.getSpectralClass());
            record.setX(star.getX());
            record.setY(star.getY());
            record.setZ(star.getZ());
            entries.add(new PlanetarySkyRenderer.BrightStarEntry(
                    record.getStarName(),
                    result.getDistanceLy(),
                    result.getMagnitude(),
                    result.getAzimuthDeg(),
                    result.getAltitudeDeg(),
                    record
            ));
        }
        return entries;
    }

    private String resolveName(StarObject star) {
        if (star.getCommonName() != null && !star.getCommonName().trim().isEmpty()) {
            return star.getCommonName().trim();
        }
        if (star.getDisplayName() != null && !star.getDisplayName().trim().isEmpty()) {
            return star.getDisplayName().trim();
        }
        if (star.getSystemName() != null && !star.getSystemName().trim().isEmpty()) {
            return star.getSystemName().trim();
        }
        if (star.getId() != null && !star.getId().trim().isEmpty()) {
            return star.getId().trim();
        }
        return "Unknown";
    }

    private void redrawOrientationGrid() {
        GraphicsContext gc = orientationCanvas.getGraphicsContext2D();
        double width = orientationCanvas.getWidth();
        double height = orientationCanvas.getHeight();
        gc.clearRect(0, 0, width, height);

        boolean showGrid = currentContext != null && currentContext.isShowOrientationGrid();
        orientationCanvas.setVisible(showGrid);
        if (!showGrid || width <= 0 || height <= 0) {
            return;
        }

        // Styling per spec: subtle violet-blue grid
        Color horizonColor = Color.rgb(140, 130, 255, 0.30);   // Brighter for horizon
        Color altitudeColor = Color.rgb(130, 120, 255, 0.20);  // Subtle for altitude rings
        Color spokeColor = Color.rgb(130, 120, 255, 0.18);     // Very subtle for spokes
        Color labelColor = Color.rgb(170, 160, 255, 0.65);     // More visible for labels

        double radius = skyRenderer.getSkyDomeRadius() * 0.995;

        // Draw altitude rings: horizon (0°), 30°, 60°
        gc.setLineWidth(2.0);  // Thicker horizon line
        drawAltitudeRing(gc, radius, 0, horizonColor);

        gc.setLineWidth(1.0);  // Standard width for other rings
        drawAltitudeRing(gc, radius, 30, altitudeColor);
        drawAltitudeRing(gc, radius, 60, altitudeColor);

        // Draw cardinal direction spokes (N, E, S, W)
        gc.setLineWidth(1.0);
        for (int az = 0; az < 360; az += 90) {
            drawAzimuthSpoke(gc, radius, az, 0, 80, spokeColor);
        }

        // Draw cardinal labels at the horizon
        gc.setFill(labelColor);
        gc.setFont(Font.font("Verdana", 14));
        drawCardinalLabel(gc, radius, 0, "N");
        drawCardinalLabel(gc, radius, 90, "E");
        drawCardinalLabel(gc, radius, 180, "S");
        drawCardinalLabel(gc, radius, 270, "W");

        // Draw altitude labels along the North spoke
        gc.setFont(Font.font("Verdana", 10));
        Color altLabelColor = Color.rgb(150, 140, 255, 0.50);
        gc.setFill(altLabelColor);
        drawAltitudeLabel(gc, radius, 0, 30, "30°");
        drawAltitudeLabel(gc, radius, 0, 60, "60°");
    }

    private void drawAltitudeLabel(GraphicsContext gc, double radius, double azimuthDeg,
                                   double altitudeDeg, String label) {
        Point2D point = projectToOverlay(radius, azimuthDeg, altitudeDeg);
        if (point == null) {
            return;
        }
        // Offset slightly to the right of the spoke
        gc.fillText(label, point.getX() + 8, point.getY() + 4);
    }

    private void drawAltitudeRing(GraphicsContext gc, double radius, double altitudeDeg, Color color) {
        gc.setStroke(color);
        double step = 3.0;  // Smaller step for smoother curves
        Point2D prev = null;
        boolean wasGap = true;  // Track if previous point was null (gap in ring)

        for (double az = 0; az <= 360.0 + step; az += step) {
            double azNorm = az % 360.0;
            Point2D next = projectToOverlay(radius, azNorm, altitudeDeg);

            if (prev != null && next != null) {
                // Check for wraparound artifacts (large jumps across screen)
                double dx = next.getX() - prev.getX();
                double dy = next.getY() - prev.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);

                // Skip if segment jumps across more than 1/4 of screen (behind camera wrap)
                double maxDist = Math.max(orientationCanvas.getWidth(), orientationCanvas.getHeight()) * 0.4;
                if (dist < maxDist) {
                    gc.strokeLine(prev.getX(), prev.getY(), next.getX(), next.getY());
                }
            }

            wasGap = (next == null);
            prev = next;
        }
    }

    private void drawAzimuthSpoke(GraphicsContext gc, double radius, double azimuthDeg,
                                  double altStart, double altEnd, Color color) {
        gc.setStroke(color);
        double step = 3.0;  // Smaller step for smoother lines
        Point2D prev = null;

        for (double alt = Math.max(0, altStart); alt <= altEnd; alt += step) {
            Point2D next = projectToOverlay(radius, azimuthDeg, alt);

            if (prev != null && next != null) {
                // Check for large jumps (shouldn't happen for spokes, but defensive)
                double dx = next.getX() - prev.getX();
                double dy = next.getY() - prev.getY();
                double dist = Math.sqrt(dx * dx + dy * dy);

                double maxDist = Math.max(orientationCanvas.getWidth(), orientationCanvas.getHeight()) * 0.3;
                if (dist < maxDist) {
                    gc.strokeLine(prev.getX(), prev.getY(), next.getX(), next.getY());
                }
            }

            prev = next;
        }
    }

    private void drawCardinalLabel(GraphicsContext gc, double radius, double azimuthDeg, String label) {
        // Project the label position at horizon (alt=0) plus a small offset above
        Point2D horizonPoint = projectToOverlay(radius, azimuthDeg, 0);
        Point2D abovePoint = projectToOverlay(radius, azimuthDeg, 5);

        if (horizonPoint == null) {
            return;
        }

        // Position label slightly above the horizon point
        double x = horizonPoint.getX();
        double y = horizonPoint.getY();

        // If we have a point above horizon, use it to offset in the right direction
        if (abovePoint != null) {
            double dx = abovePoint.getX() - horizonPoint.getX();
            double dy = abovePoint.getY() - horizonPoint.getY();
            double len = Math.sqrt(dx * dx + dy * dy);
            if (len > 1) {
                // Offset 15 pixels in the "up" direction relative to view
                x += (dx / len) * 15;
                y += (dy / len) * 15;
            }
        } else {
            // Fallback: offset upward in screen coords
            y -= 15;
        }

        // Center the label horizontally on the computed position
        double labelWidth = gc.getFont().getSize() * label.length() * 0.6;
        gc.fillText(label, x - labelWidth / 2, y + 5);
    }

    private Point2D projectToOverlay(double radius, double azimuthDeg, double altitudeDeg) {
        if (altitudeDeg < 0) {
            return null;  // Below horizon - don't draw
        }

        // Get 3D position in world coordinates (same coordinate system as stars)
        double[] pos = skyRenderer.toSkyPoint(radius, azimuthDeg, altitudeDeg);
        Point3D worldPoint = new Point3D(pos[0], pos[1], pos[2]);

        // Project to scene coordinates (localToScene with rootScene=true does perspective projection)
        Point3D scenePoint = world.localToScene(worldPoint, true);

        // Check for invalid projection
        if (Double.isNaN(scenePoint.getX()) || Double.isNaN(scenePoint.getY())) {
            return null;
        }

        // Convert scene coordinates to canvas local coordinates
        Point2D canvasPoint = orientationCanvas.sceneToLocal(scenePoint.getX(), scenePoint.getY());
        if (canvasPoint == null) {
            return null;
        }

        // Get canvas dimensions
        double width = orientationCanvas.getWidth();
        double height = orientationCanvas.getHeight();

        double x = canvasPoint.getX();
        double y = canvasPoint.getY();

        // Check if point is within visible bounds (with margin for labels)
        if (x < -50 || x > width + 50 || y < -50 || y > height + 50) {
            return null;
        }

        return new Point2D(x, y);
    }
}
