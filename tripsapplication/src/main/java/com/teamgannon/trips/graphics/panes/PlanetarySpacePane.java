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
import javafx.scene.*;
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
        sceneRoot.getChildren().add(labelGroup);

        this.setBackground(Background.EMPTY);
        this.getChildren().add(sceneRoot);
        this.setPickOnBounds(false);

        subScene.widthProperty().bind(this.widthProperty());
        subScene.heightProperty().bind(this.heightProperty());

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

    /**
     * Reset the view.
     */
    public void reset() {
        labelGroup.getChildren().clear();
        skyGroup.getChildren().clear();
        skyRenderer.clear();
        currentContext = null;
        nearbyStars.clear();

        // Reset camera position
        rotateX.setAngle(0);
        rotateY.setAngle(0);
        rotateZ.setAngle(0);
        camera.setNearClip(5.0);
        camera.setTranslateZ(-900);
        rotateX.setAngle(-25);
    }

    /**
     * Set initial camera view.
     */
    private void setInitialView() {
        camera.setNearClip(5.0);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-900);

        // Start looking slightly up (25 degrees above horizon)
        rotateX.setAngle(-25);
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
                // Rotate view
                rotateY.setAngle(((rotateY.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);
                rotateX.setAngle(Math.max(-90, Math.min(90,
                        rotateX.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0)));
            }
        });
    }

    /**
     * Zoom the view.
     */
    private void zoomView(double zoomAmt) {
        double z = camera.getTranslateZ();
        double newZ = z - zoomAmt;
        // Clamp zoom
        newZ = Math.max(-1000, Math.min(-100, newZ));
        camera.setTranslateZ(newZ);
    }

    /**
     * Update viewing direction.
     */
    public void setViewingDirection(double azimuth, double altitude) {
        rotateY.setAngle(azimuth);
        rotateX.setAngle(-altitude);  // Negative because looking up is positive altitude
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
}
