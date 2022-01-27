package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.config.application.ScreenSize;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.listener.ContextSelectorListener;
import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.animation.RotateTransition;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.transform.Rotate;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;


/**
 * This is used to display a solar system
 * <p>
 * Created by larrymitchell on 2017-02-05.
 */
@Slf4j
@Component
public class SolarSystemSpacePane extends Pane {

    /**
     * rotation angle controls
     */
    private static final double ROTATE_SECS = 60;
    private final Rotate rotateX = new Rotate(25, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(25, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);
    private final TripsContext tripsContext;
    private final DatabaseManagementService databaseManagementService;

    // mouse positions
    private double mousePosX, mousePosY = 0;
    private double mouseOldX, mouseOldY = 0;
    private double mouseDeltaX, mouseDeltaY = 0;

    /**
     * graphical groups
     */
    private final Group world = new Group();
    private final Group root = new Group();
    private final Group starNameGroup = new Group();

    /**
     * contains all the entities in the solar system
     */
    private final Group systemEntityGroup = new Group();

    /**
     * the subscene which is used for a glass pane flat screen
     */
    private final SubScene subScene;

    /**
     * the perspective camera for selecting views on the scene
     */
    private final PerspectiveCamera camera = new PerspectiveCamera(true);

    /**
     * the depth of the screen in pixels
     */
    private final double depth;


    /**
     * animation rotator
     */
    private RotateTransition rotator;

    /**
     * animation toggle
     */
    private final boolean animationPlay = false;

    /**
     * signals a switch of context from solarsystem space to interstellarspace
     */
    private ContextSelectorListener contextSelectorListener;


    /**
     * constructor
     *
     * @param tripsContext              the trips context
     * @param databaseManagementService the database management service
     */
    public SolarSystemSpacePane(TripsContext tripsContext,
                                DatabaseManagementService databaseManagementService) {

        this.tripsContext = tripsContext;
        ScreenSize screenSize = tripsContext.getScreenSize();
        this.databaseManagementService = databaseManagementService;

        this.depth = screenSize.getDepth();

        // attach our custom rotation transforms so we can update the labels dynamically
        world.getTransforms().addAll(rotateX, rotateY, rotateZ);

        subScene = new SubScene(world, screenSize.getSceneWidth(), screenSize.getSceneHeight(), true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        setInitialView();

        subScene.setCamera(camera);
        Group sceneRoot = new Group(subScene);

        sceneRoot.getChildren().add(starNameGroup);

        this.setBackground(Background.EMPTY);
        this.getChildren().add(sceneRoot);
        this.setPickOnBounds(false);

        subScene.widthProperty().bind(this.widthProperty());
        subScene.heightProperty().bind(this.heightProperty());

        root.getChildren().add(this);

        handleMouseEvents();
    }

    /**
     * call to update the labels
     */
    public void updateLabels() {

    }


    /**
     * set the system to show
     *
     * @param starDisplayRecord object properties of this system
     */
    public void setSystemToDisplay(@NotNull StarDisplayRecord starDisplayRecord) {
        String systemName = starDisplayRecord.getStarName();
        createScaleLegend(systemName);

        // get the solar system description
        SolarSystemDescription solarSystemDescription = databaseManagementService.getSolarSystem(starDisplayRecord);

        // render the solar system
        render(solarSystemDescription);
    }


    /**
     * used to draw the target System
     */
    private void render(SolarSystemDescription solarSystemDescription) {
        // figure out size of solar system to get scaling factors

        // plot central star

        // iterate through all the planets

        // iterate through all the other objects

        log.info("system rendered");
    }

    private Node createStar(StarDisplayRecord starDisplayRecord) {
        return null;
    }

    private Node createPlanet(PlanetDescription planetDescription) {
        return null;
    }


    /**
     * reset the system
     */
    public void reset() {
        // clear group to redraw
        starNameGroup.getChildren().clear();
    }

    // ---------------------- helpers -------------------------- //

    /**
     * setup the context selector listener
     *
     * @param contextSelectorListener the context selector listener
     */
    public void setContextUpdater(ContextSelectorListener contextSelectorListener) {
        this.contextSelectorListener = contextSelectorListener;
    }

    /////////////////////////////////////

    /**
     * set the initial view
     */
    private void setInitialView() {
        setPerspectiveCamera();
    }

    /**
     * create the scale legend
     *
     * @param starName the star name
     */
    private void createScaleLegend(String starName) {
        // clear group to redraw
        starNameGroup.getChildren().clear();

        GridPane titlePane = new GridPane();
        titlePane.setPrefWidth(450);
        starNameGroup.getChildren().add(titlePane);

        Label starNameLabel = new Label(starName);
        starNameLabel.setFont(Font.font("Verdana", FontPosture.ITALIC, 20));
        starNameLabel.setTextFill(Color.WHEAT);

        titlePane.add(starNameLabel, 0, 0);

        Separator separator1 = new Separator();
        separator1.setMinWidth(40.0);
        titlePane.add(separator1, 1, 0);

        // setup return button to jump back to interstellar space
        Button returnButton = new Button("Jump Back");
        returnButton.setOnAction(e -> jumpBackToInterstellarSpace());
        titlePane.add(returnButton, 2, 0);

        titlePane.setTranslateX(subScene.getWidth() - 430);
        titlePane.setTranslateY(subScene.getHeight() - 30);
        titlePane.setTranslateZ(0);
    }

    /**
     * jump back to the interstellar space
     */
    private void jumpBackToInterstellarSpace() {
        // there is no specific context at the moment.  We assume the same interstellar space we came form
        contextSelectorListener.selectInterstellarSpace(new HashMap<>());
    }

    /**
     * set the perspective camera parameters
     */
    private void setPerspectiveCamera() {
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-1600);
    }

    /**
     * handle the mouse events
     */
    private void handleMouseEvents() {

        subScene.setOnScroll((ScrollEvent event) -> {
            double deltaY = event.getDeltaY();
            zoomGraph(deltaY * 5);
            updateLabels();
        });

        subScene.setOnMousePressed((MouseEvent me) -> {
                    mousePosX = me.getSceneX();
                    mousePosY = me.getSceneY();
                    mouseOldX = me.getSceneX();
                    mouseOldY = me.getSceneY();
                }
        );

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
                        if (me.isAltDown()) { //roll
                            rotateZ.setAngle(((rotateZ.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180); // +
                        } else {
                            rotateY.setAngle(((rotateY.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180); // +
                            rotateX.setAngle(
                                    (((rotateX.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180)
                            ); // -
                        }
                    }
                    updateLabels();
                }
        );
    }

    /**
     * do actual zoom
     *
     * @param zoomAmt the amount to zoom
     */
    private void zoomGraph(double zoomAmt) {
        double z = camera.getTranslateZ();
        double newZ = z - zoomAmt;
        camera.setTranslateZ(newZ);
    }

}
