package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.graphics.CurrentPlot;
import com.teamgannon.trips.graphics.GridPlotManager;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.entities.Xform;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.GraphEnablesPersist;
import com.teamgannon.trips.listener.*;
import com.teamgannon.trips.routing.Route;
import com.teamgannon.trips.routing.RouteManager;
import com.teamgannon.trips.starplotting.StarPlotManager;
import com.teamgannon.trips.transits.TransitManager;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
public class InterstellarSpacePane extends Pane {

    //////////////  Star Definitions   //////////////////
    private static final double CAMERA_INITIAL_DISTANCE = -500;
    private static final double CAMERA_INITIAL_X_ANGLE = -25;//   -90

    //////////////// event listeners and updaters
    private static final double CAMERA_INITIAL_Y_ANGLE = -25; // 0
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000.0;

    // animations
    private static final double ROTATE_SECS = 60;
    private final Xform cameraXform = new Xform();

    ////////////////// Camera stuff ////////////////
    private final Xform cameraXform2 = new Xform();
    private final Xform cameraXform3 = new Xform();


    ////////////   Graphics Section of definitions  ////////////////
    private final Group root = new Group();
    private final Xform world = new Xform();

    // camera work
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final double CONTROL_MULTIPLIER = 0.1;
    private final double SHIFT_MULTIPLIER = 0.1;
    private final double ALT_MULTIPLIER = 0.5;

    /**
     * the gird plot manager
     */
    private final GridPlotManager gridPlotManager;

    /**
     * animation rotator
     */
    private final RotateTransition rotator;

    /////////////////

    // screen real estate
    private final int width;
    private final int height;

    /**
     * our current plot
     */
    private final CurrentPlot currentPlot;

    /**
     * the general color palette of the graph
     */
    private ColorPalette colorPalette;

    /**
     * star display specifics
     */
    private StarDisplayPreferences starDisplayPreferences;

    /**
     * animation toggle
     */
    private boolean animationPlay = false;

    /**
     * application context
     */
    private final TripsContext tripsContext;

    /**
     * used to signal an update to the parent list view
     */
    private final ListUpdaterListener listUpdaterListener;

    // mouse positions
    private double mousePosX, mousePosY = 0;
    private double mouseOldX, mouseOldY = 0;
    private double mouseDeltaX, mouseDeltaY = 0;

    private final RouteManager routeManager;

    private final TransitManager transitManager;

    private final StarPlotManager starPlotManager;

    /**
     * constructor for the Graphics Pane
     *
     * @param width  the width
     * @param height the height
     */
    public InterstellarSpacePane(int width,
                                 int height,
                                 int depth,
                                 int spacing,
                                 TripsContext tripsContext,
                                 RouteUpdaterListener routeUpdaterListener,
                                 ListUpdaterListener listUpdaterListener,
                                 StellarPropertiesDisplayerListener displayer,
                                 DatabaseListener databaseListener,
                                 ContextSelectorListener contextSelectorListener,
                                 RedrawListener redrawListener,
                                 ReportGenerator reportGenerator) {
        this.width = width;
        this.height = height;
        this.tripsContext = tripsContext;

        // setup defaults
        this.colorPalette = tripsContext.getAppViewPreferences().getColorPallete();
        this.starDisplayPreferences = tripsContext.getAppViewPreferences().getStarDisplayPreferences();

        this.listUpdaterListener = listUpdaterListener;

        currentPlot = new CurrentPlot();
        currentPlot.setStarDisplayPreferences(starDisplayPreferences);

        this.starPlotManager = new StarPlotManager(
                world,
                listUpdaterListener,
                redrawListener,
                databaseListener,
                displayer,
                contextSelectorListener,
                starDisplayPreferences,
                reportGenerator,
                currentPlot,
                colorPalette
        );

        this.routeManager = new RouteManager(
                world,
                routeUpdaterListener,
                currentPlot
        );

        this.gridPlotManager = new GridPlotManager(
                world,
                spacing, width, depth,
                colorPalette
        );

        this.transitManager = new TransitManager(
                world,
                routeUpdaterListener
        );

        starPlotManager.setRouteManager(routeManager);

        this.setMinHeight(height);
        this.setMinWidth(width);

        // create a rotation animation
        rotator = createRotateAnimation();

        // create all the base display elements
        buildRoot();
        buildScene();
        buildCamera();

        // handle mouse and keyboard events
        handleMouseEvents(this);
        handleKeyboard(this);
    }

    public void setStellarPreferences(StarDisplayPreferences starDisplayPreferences) {
        this.starDisplayPreferences = starDisplayPreferences;
        this.starPlotManager.setStarDisplayPreferences(starDisplayPreferences);
    }

    public void setCivilizationPreferences(CivilizationDisplayPreferences preferences) {
        currentPlot.setCivilizationDisplayPreferences(preferences);
        starPlotManager.setCivilizationDisplayPreferences(preferences);
    }


    /**
     * finds all the transits for stars in view
     *
     * @param distanceRoutes the distance range selected
     */
    public void findTransits(DistanceRoutes distanceRoutes) {
        List<StarDisplayRecord> starsInView = getCurrentStarsInView();
        transitManager.findTransits(distanceRoutes, starsInView);
    }

    /**
     * clear existing transits
     */
    public void clearTransits() {
        transitManager.clearTransits();
    }


    /////////////////// SET DATASET CONTEXT  /////////////////

    public void setDataSetContext(DataSetDescriptor datasetName) {
        routeManager.setDatasetContext(datasetName);
        transitManager.setDatasetContext(datasetName);
    }

    public void setupPlot(
            DataSetDescriptor dataSetDescriptor,
            double[] centerCoordinates,
            StarDisplayPreferences starDisplayPreferences,
            CivilizationDisplayPreferences civilizationDisplayPreferences) {

        clearStars();

        currentPlot.setDataSetDescriptor(dataSetDescriptor);
        currentPlot.setCenterCoordinates(centerCoordinates);
        currentPlot.setCivilizationDisplayPreferences(civilizationDisplayPreferences);
        currentPlot.setPlotActive(true);

        starPlotManager.setStarDisplayPreferences(starDisplayPreferences);

        routeManager.setDatasetContext(dataSetDescriptor);
    }

    //////////////////////

    public GridPlotManager getGridPlotManager() {
        return gridPlotManager;
    }

    public void highlightStar(UUID starId) {
        starPlotManager.highlightStar(starId);
    }

    public void clearPlot() {
        starPlotManager.clearStars();
    }

    public void changeColors(ColorPalette colorPalette) {
        this.colorPalette = colorPalette;
        tripsContext.getAppViewPreferences().setColorPallete(colorPalette);
    }

    public List<StarDisplayRecord> getCurrentStarsInView() {
        return starPlotManager.getCurrentStarsInView();
    }

    //////////////////////////  public methods /////////////////////////////

    /**
     * clear the stars from the display
     */
    public void clearStars() {

        starPlotManager.clearStars();
        clearRoutes();

        // clear the list
        if (listUpdaterListener != null) {
            listUpdaterListener.clearList();
        }

    }

    /**
     * clear the routes
     */
    public void clearRoutes() {
        // clear the routes
        routeManager.clearRoutes();
    }

    public void plotRoutes(List<Route> routeList) {
        routeManager.plotRoutes(routeList);
    }

    ////////////// zoom and move

    public void zoomIn() {
        zoomGraph(-50);
    }

    public void zoomOut() {
        zoomGraph(50);
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

    ////////// toggles


    public void setGraphPresets(GraphEnablesPersist graphEnablesPersist) {
        gridPlotManager.toggleGrid(graphEnablesPersist.isDisplayGrid());
        starPlotManager.toggleExtensions(graphEnablesPersist.isDisplayStems());
        gridPlotManager.toggleScale(graphEnablesPersist.isDisplayLegend());
        starPlotManager.toggleLabels(graphEnablesPersist.isDisplayLabels());
    }

    /**
     * toggle the grid
     *
     * @param gridToggle the status for the grid
     */
    public void toggleGrid(boolean gridToggle) {
        gridPlotManager.toggleGrid(gridToggle);
        starPlotManager.toggleExtensions(gridPlotManager.isVisible());
    }

    public void togglePolities(boolean polities) {
        starPlotManager.togglePolities(polities);
    }

    /**
     * toggle the transit lengths for the transits shown
     *
     * @param transitsLengthsOn flag for transit lengths
     */
    public void toggleTransitLengths(boolean transitsLengthsOn) {
        transitManager.toggleTransitLengths(transitsLengthsOn);
    }

    /**
     * toggle the extensions
     *
     * @param extensionsOn the status of the extensions
     */
    public void toggleExtensions(boolean extensionsOn) {
        starPlotManager.toggleExtensions(extensionsOn);
    }

    /**
     * toggle the stars
     *
     * @param starsOn the status of the stars
     */
    public void toggleStars(boolean starsOn) {
        starPlotManager.toggleStars(starsOn);
        if (gridPlotManager.isVisible()) {
            starPlotManager.toggleExtensions(true);
        }
    }

    /**
     * toggle the scale
     *
     * @param scaleOn the status of the scale
     */
    public void toggleScale(boolean scaleOn) {
        gridPlotManager.toggleScale(scaleOn);
    }

    /**
     * toggle the routes
     *
     * @param routesOn the status of the routes
     */
    public void toggleRoutes(boolean routesOn) {
        routeManager.toggleRoutes(routesOn);
    }

    /**
     * toggle the transit view
     *
     * @param transitsOn true means transits on, false - otherwise
     */
    public void toggleTransits(boolean transitsOn) {
        transitManager.setVisible(transitsOn);
    }

    public void redrawRoutes(List<Route> routes) {
        routeManager.plotRoutes(routes);
    }

    public void plotRouteDesciptors(List<RouteDescriptor> routeDescriptorList) {
        routeManager.plotRouteDescriptors(routeDescriptorList);
    }

    /**
     * toggle the labels
     *
     * @param labelSetting true is labels should be on
     */
    public void toggleLabels(boolean labelSetting) {
        starPlotManager.toggleLabels(labelSetting);
    }


    /**
     * start the rotation of Y-axis animation
     */
    public void toggleAnimation() {
        animationPlay = !animationPlay;
        if (animationPlay) {
            rotator.play();
        } else {
            rotator.pause();
        }
    }

    ///////////////////////////////////

    /**
     * draw a list of stars
     *
     * @param recordList the list of stars
     */
    public void plotStar(List<StarDisplayRecord> recordList, String centerStar, ColorPalette colorPalette) {
        starPlotManager.drawStar(recordList, centerStar, colorPalette);
    }

    /**
     * draw a star
     *
     * @param record     the star record
     * @param centerStar the name of the center star
     */
    public void plotStar(StarDisplayRecord record,
                         String centerStar,
                         ColorPalette colorPalette,
                         StarDisplayPreferences starDisplayPreferences) {


        starPlotManager.drawStar(record, centerStar, colorPalette, starDisplayPreferences);
    }


    ////////////////////////// animation helpers

    /**
     * create an animation player
     *
     * @return the rotation animator
     */
    private RotateTransition createRotateAnimation() {
        RotateTransition rotate = new RotateTransition(
                Duration.seconds(ROTATE_SECS),
                world
        );
        rotate.setAxis(Rotate.Y_AXIS);
        rotate.setFromAngle(360);
        rotate.setToAngle(0);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        return rotate;
    }


    ////////////// graphics helpers  /////////////////////////

    private void buildRoot() {
        // hooks this into the
        this.getChildren().add(root);
    }

    private void buildScene() {
        SubScene subScene = new SubScene(
                world,
                width, height,
                true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(Color.BLACK);

        root.getChildren().add(subScene);
    }

    private void buildCamera() {

        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
//        cameraXform3.setRotateZ(180.0);

        // set camera POV and initial position
        setInitialView();

    }

    public void setInitialView() {
        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);

        // rotate camera along x and y axis
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);

        // push camera back to see the object
        cameraXform3.setTranslate(0, 0, -1000);
    }

    /////////////////  MOUSE AND KEYBOARD EVENT HANDLERS  /////////////////////////

    /**
     * used to handle rotation of the scene
     *
     * @param pane the subscene to manage rotation
     */
    private void handleMouseEvents(Pane pane) {

        pane.setOnScroll((ScrollEvent event) -> {
            double deltaY = event.getDeltaY();
            zoomGraph(deltaY);
        });

        // get initial position of the mouse
        pane.setOnMousePressed((MouseEvent me) -> {
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });

        // rotate the scene based on whether move moves
        pane.setOnMouseDragged((MouseEvent me) -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);

            if (me.isControlDown()) {
                // this drags the graph if the control key is pressed
                double cameraX = cameraXform.getTranslateX() - mouseDeltaX;
                double cameraY = cameraXform.getTranslateY() - mouseDeltaY;

                cameraXform.setTranslateX(cameraX);
                cameraXform.setTranslateY(cameraY);
            } else {

//            updateLabels();  // used to eventually make the labels flat

                double modifier = 1.0;
                double modifierFactor = 0.1;

                if (me.isShiftDown()) {
                    modifier = 5.0;
                }

                if (me.isPrimaryButtonDown()) {
                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX * modifierFactor * modifier * 2.0);  // +
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY * modifierFactor * modifier * 2.0);  // -
                } else if (me.isSecondaryButtonDown()) {
                    log.info("secondary button pushed, x={}, y={}", mousePosX, mousePosY);
                } else if (me.isMiddleButtonDown()) {
                    log.info("middle button pushed, x={}, y={}", mousePosX, mousePosY);
                }

//            me.setDragDetect(true);
            }
        });

    }

    /**
     * setup keyboard events
     *
     * @param pane the pane to manage
     */
    private void handleKeyboard(Pane pane) {
        log.info("Setting up keyboard handling");
        pane.setOnKeyPressed(event -> {
            log.info("Keyboard Event Received: {}", event);
            switch (event.getCode()) {
                case Z:
                    if (event.isShiftDown()) {
                        cameraXform.ry.setAngle(0.0);
                        cameraXform.rx.setAngle(0.0);
                        camera.setTranslateZ(-300.0);
                    }
                    cameraXform2.t.setX(0.0);
                    cameraXform2.t.setY(0.0);
                    break;
                case X:
                    if (event.isControlDown()) {
                        gridPlotManager.toggleVisibility();
                    }
                    break;
                case S:
                    break;
                case SPACE:
                    break;
                case UP:
                    if (event.isControlDown() && event.isShiftDown()) {
                        cameraXform2.t.setY(cameraXform2.t.getY() - 10.0 * CONTROL_MULTIPLIER);
                    } else if (event.isAltDown() && event.isShiftDown()) {
                        cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 10.0 * ALT_MULTIPLIER);
                    } else if (event.isControlDown()) {
                        cameraXform2.t.setY(cameraXform2.t.getY() - 1.0 * CONTROL_MULTIPLIER);
                    } else if (event.isAltDown()) {
                        cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 2.0 * ALT_MULTIPLIER);
                    } else if (event.isShiftDown()) {
                        double z = camera.getTranslateZ();
                        double newZ = z + 5.0 * SHIFT_MULTIPLIER;
                        camera.setTranslateZ(newZ);
                    }
                    break;
                case DOWN:
                    if (event.isControlDown() && event.isShiftDown()) {
                        cameraXform2.t.setY(cameraXform2.t.getY() + 10.0 * CONTROL_MULTIPLIER);
                    } else if (event.isAltDown() && event.isShiftDown()) {
                        cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 10.0 * ALT_MULTIPLIER);
                    } else if (event.isControlDown()) {
                        cameraXform2.t.setY(cameraXform2.t.getY() + 1.0 * CONTROL_MULTIPLIER);
                    } else if (event.isAltDown()) {
                        cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 2.0 * ALT_MULTIPLIER);
                    } else if (event.isShiftDown()) {
                        double z = camera.getTranslateZ();
                        double newZ = z - 5.0 * SHIFT_MULTIPLIER;
                        camera.setTranslateZ(newZ);
                    }
                    break;
                case RIGHT:
                    if (event.isControlDown() && event.isShiftDown()) {
                        cameraXform2.t.setX(cameraXform2.t.getX() + 10.0 * CONTROL_MULTIPLIER);
                    } else if (event.isAltDown() && event.isShiftDown()) {
                        cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 10.0 * ALT_MULTIPLIER);
                    } else if (event.isControlDown()) {
                        cameraXform2.t.setX(cameraXform2.t.getX() + 1.0 * CONTROL_MULTIPLIER);
                    } else if (event.isAltDown()) {
                        cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 2.0 * ALT_MULTIPLIER);
                    }
                    break;
                case LEFT:
                    if (event.isControlDown() && event.isShiftDown()) {
                        cameraXform2.t.setX(cameraXform2.t.getX() - 10.0 * CONTROL_MULTIPLIER);
                    } else if (event.isAltDown() && event.isShiftDown()) {
                        cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 10.0 * ALT_MULTIPLIER);  // -
                    } else if (event.isControlDown()) {
                        cameraXform2.t.setX(cameraXform2.t.getX() - 1.0 * CONTROL_MULTIPLIER);
                    } else if (event.isAltDown()) {
                        cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 2.0 * ALT_MULTIPLIER);  // -
                    }
                    break;
                default:
                    log.info("keyboard Event is {}", event.getCode());
            }
        });
    }

}
