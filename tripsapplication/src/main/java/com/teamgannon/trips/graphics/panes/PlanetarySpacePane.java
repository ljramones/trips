package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.config.application.ScreenSize;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.events.ContextSelectionType;
import com.teamgannon.trips.events.ContextSelectorEvent;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.planetary.PlanetaryContext;
import com.teamgannon.trips.planetary.rendering.PlanetarySkyRenderer;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
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

    /**
     * Constructor
     */
    public PlanetarySpacePane(TripsContext tripsContext,
                              ApplicationEventPublisher eventPublisher) {
        this.tripsContext = tripsContext;
        this.eventPublisher = eventPublisher;

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

        // Render the sky dome
        renderSky();
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
        camera.setTranslateZ(-500);
    }

    /**
     * Set initial camera view.
     */
    private void setInitialView() {
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-500);

        // Start looking slightly up (30 degrees above horizon)
        rotateX.setAngle(-30);
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
        return skyRenderer.getBrightestStars();
    }
}
