package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.config.application.ScreenSize;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.CurrentPlot;
import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import com.teamgannon.trips.config.application.model.UserControls;
import com.teamgannon.trips.controller.MainPane;
import com.teamgannon.trips.controller.RotationController;
import com.teamgannon.trips.events.ClearListEvent;
import com.teamgannon.trips.events.ColorPaletteChangeEvent;
import com.teamgannon.trips.events.UserControlsChangeEvent;
import com.teamgannon.trips.graphics.AstrographicTransformer;
import com.teamgannon.trips.graphics.GridPlotManager;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.GraphEnablesPersist;
import com.teamgannon.trips.routing.RouteManager;
import com.teamgannon.trips.routing.model.Route;
import com.teamgannon.trips.routing.model.RoutingMetric;
import com.teamgannon.trips.starplotting.StarPlotManager;
import com.teamgannon.trips.transits.TransitDefinitions;
import com.teamgannon.trips.transits.TransitManager;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.Math.abs;

@Slf4j
@Component
public class InterstellarSpacePane extends Pane implements RotationController {

    private static final double ROTATE_SECS = 60;

    // define the rest position of the image
    private final Rotate rotateX = new Rotate(105, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(30, Rotate.Z_AXIS);


    private final Group world = new Group();
    private final @NotNull SubScene subScene;

    private boolean sidePanelShiftKludgeFirstTime = true;

    /**
     * animation rotator
     */
    private final @NotNull RotateTransition rotator;

    /**
     * application context
     */
    private final @NotNull TripsContext tripsContext;

    /**
     * the grid plot manager
     */
    private final GridPlotManager gridPlotManager;

    @Getter
    private final RouteManager routeManager;

    private final ApplicationEventPublisher eventPublisher;

    /////////////////
    @Getter
    private final @NotNull TransitManager transitManager;

    @Getter
    private final @NotNull StarPlotManager starPlotManager;

    @NotNull
    PerspectiveCamera camera = new PerspectiveCamera(true);
    private final double baseCameraTranslateX;

    // mouse positions
    private double mousePosX, mousePosY = 0;
    private double mouseOldX, mouseOldY = 0;
    private double mouseDeltaX, mouseDeltaY = 0;

    /**
     * set user sense to engineer mode
     * false is pilot mode
     */
    private UserControls userControls = new UserControls();

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
     * offset to scene coordinates to account for the top UI plane
     */
    private double controlPaneOffset;

    private double deltaX;
    private final PauseTransition labelUpdatePause = new PauseTransition(Duration.millis(75));


    /**
     * constructor for the Graphics Pane
     *
     * @param tripsContext the application context
     */
    public InterstellarSpacePane(TripsContext tripsContext,
                                 ApplicationEventPublisher eventPublisher,
                                 StarPlotManager starPlotManager,
                                 RouteManager routeManager,
                                 GridPlotManager gridPlotManager,
                                 @NotNull TransitManager transitManager) {

        this.tripsContext = tripsContext;
        this.eventPublisher = eventPublisher;
        this.transitManager = transitManager;
        ScreenSize screenSize = tripsContext.getScreenSize();
        this.starPlotManager = starPlotManager;
        this.routeManager = routeManager;
        this.gridPlotManager = gridPlotManager;

        this.colorPalette = tripsContext.getAppViewPreferences().getColorPallete();
        this.starDisplayPreferences = tripsContext.getAppViewPreferences().getStarDisplayPreferences();

        // attach our custom rotation transforms so we can update the labels dynamically
        world.getTransforms().addAll(rotateX, rotateY, rotateZ);

        subScene = new SubScene(world, screenSize.getSceneWidth(), screenSize.getSceneHeight(), true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        setInitialView();
        baseCameraTranslateX = camera.getTranslateX();

        subScene.setCamera(camera);
        Group sceneRoot = new Group(subScene);

        this.setBackground(Background.EMPTY);
        this.getChildren().add(sceneRoot);
        this.setPickOnBounds(false);

        subScene.widthProperty().bind(this.widthProperty());
        subScene.heightProperty().bind(this.heightProperty());
        widthProperty().addListener((obs, oldValue, newValue) -> scheduleLabelUpdate());
        heightProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.doubleValue() > 0) {
                scheduleLabelUpdate();
            }
        });

        // event setup
        handleUserEvents();

        // set up star plot manager
        starPlotManager.setGraphics(sceneRoot, world, subScene);

        // setup route manager
        routeManager.setGraphics(sceneRoot, world, subScene, this);

        // setup grid manager
        gridPlotManager.setGraphics(sceneRoot, world, subScene);

        // setup transit manager
        this.transitManager.setGraphics(sceneRoot, world, subScene, this);

        // create a rotation animation
        rotator = createRotateAnimation();

        log.info("startup complete");

    }

    public void setRotationAngles(double xAngle, double yAngle, double zAngle) {
        rotateX.setAngle(xAngle);
        rotateY.setAngle(yAngle);
        rotateZ.setAngle(zAngle);
        updateLabels();
    }

    public void resetPosition() {
        setRotationAngles(105, 0, 30);
        setPerspectiveCamera();
    }

    private void setPerspectiveCamera() {
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-1600);
    }

    /**
     * set the initial view
     */
    public void setInitialView() {
        setPerspectiveCamera();
    }

    public void resetView() {
        setInitialView();
//        camera.setRotate(25);
    }

    /**
     * set user controls
     *
     * @param userControls the user controls
     */
    public void changeUserControls(UserControls userControls) {
        this.userControls = userControls;
    }

    @EventListener
    public void onUserControlsChangeEvent(UserControlsChangeEvent event) {
        changeUserControls(event.getUserControls());
    }

    /**
     * simulate stars
     *
     * @param numberStars the number to simulate
     */
    public void simulateStars(int numberStars) {
        starPlotManager.generateRandomStars(numberStars);
        Platform.runLater(this::run);
    }

    public void updateLabels() {

        starPlotManager.updateLabels(this);

        gridPlotManager.updateScale();
        gridPlotManager.updateLabels(this);

        routeManager.updateLabels();

        transitManager.updateLabels();
    }


    /**
     * finds all the transits for stars in view
     *
     * @param transitDefinitions the distance range selected
     */
    public void findTransits(TransitDefinitions transitDefinitions) {
        List<StarDisplayRecord> starsInView = getCurrentStarsInView();
        transitManager.findTransits(transitDefinitions, starsInView);
    }

    /**
     * clear existing transits
     */
    public void clearTransits() {
        transitManager.clearTransits();
    }

    //////////////////////

    public void rebuildGrid(double[] centerCoordinates, @NotNull AstrographicTransformer transformer, CurrentPlot colorPalette) {
        gridPlotManager.rebuildGrid(centerCoordinates, transformer, colorPalette);
    }

    public void changeColors(ColorPalette colorPalette) {
        this.colorPalette = colorPalette;
        tripsContext.getAppViewPreferences().setColorPallete(colorPalette);
    }

    @EventListener
    public void onColorPaletteChangeEvent(ColorPaletteChangeEvent event) {
        changeColors(event.getColorPalette());
    }

    public List<StarDisplayRecord> getCurrentStarsInView() {
        return starPlotManager.getCurrentStarsInView();
    }

    //////////////////////////  public methods /////////////////////////////


    public void clearAll() {
        clearStars();
        clearRoutes();
        clearTransits();
    }

    /**
     * clear the stars from the display
     */
    public void clearStars() {

        starPlotManager.clearStars();
        clearRoutes();

        // clear the list
        eventPublisher.publishEvent(new ClearListEvent(this));
    }

    /**
     * clear the routes
     */
    public void clearRoutes() {
        // clear the routes
        routeManager.clearRoutes();
    }

    public void plotRoutes(@NotNull List<Route> routeList) {
        routeManager.clearRoutes();
        routeManager.plotRoutes(routeList);
    }

    ////////////// zoom and move

    public void zoomIn() {
        zoomGraph(-200);
        updateLabels();
    }

    public void zoomIn(int amount) {
        zoomGraph(-amount);
        updateLabels();
    }


    public void zoomOut() {
        zoomGraph(200);
        updateLabels();
    }

    public void zoomOut(int amount) {
        zoomGraph(amount);
        updateLabels();
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


    public void setGraphPresets(@NotNull GraphEnablesPersist graphEnablesPersist) {
        gridPlotManager.toggleGrid(graphEnablesPersist.isDisplayGrid());
        starPlotManager.toggleExtensions(graphEnablesPersist.isDisplayStems());
        gridPlotManager.toggleScale(graphEnablesPersist.isDisplayLegend());
        starPlotManager.toggleLabels(graphEnablesPersist.isDisplayLabels());
    }

    public void shiftDisplayLeft(boolean shift) {
        if (shift) {
            double width = getWidth();
            if (width <= 0) {
                width = subScene.getWidth();
            }
            if (width <= 0) {
                Platform.runLater(() -> shiftDisplayLeft(true));
                return;
            }
            deltaX = MainPane.SIDE_PANEL_SIZE / 2.0;

            log.info("shift display left by {}", deltaX);
            camera.setTranslateX(baseCameraTranslateX + deltaX);
        } else {
            if (!sidePanelShiftKludgeFirstTime) {
                log.info("shift display right!!");
                camera.setTranslateX(baseCameraTranslateX);
            } else {
                sidePanelShiftKludgeFirstTime = false;
            }
        }
        updateLabels();
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
            starPlotManager.toggleExtensions(starsOn);
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

    public void toggleRouteLengths(boolean routesLengthsOn) {
        routeManager.toggleRouteLengths(routesLengthsOn);
    }

    /**
     * toggle the transit view
     *
     * @param transitsOn true means transits on, false - otherwise
     */
    public void toggleTransits(boolean transitsOn) {
        transitManager.setVisible(transitsOn);
    }

    public void redrawRoutes(@NotNull List<Route> routes) {
        routeManager.clearRoutes();
        routeManager.plotRoutes(routes);
        updateLabels();
    }

    public void plotRouteDescriptors(DataSetDescriptor currentDataSet, @NotNull List<RoutingMetric> routeDescriptorList) {
        routeManager.plotRouteDescriptors(currentDataSet, routeDescriptorList);
        updateLabels();
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

    public void plotStars(CurrentPlot currentPlot) {
        boolean showStems = tripsContext.getAppViewPreferences().getGraphEnablesPersist().isDisplayStems() &&
                tripsContext.getAppViewPreferences().getGraphEnablesPersist().isDisplayGrid();
        starPlotManager.drawStars(currentPlot, showStems);
    }


    ////////////////////////// animation helpers

    /**
     * create an animation player
     *
     * @return the rotation animator
     */
    private @NotNull RotateTransition createRotateAnimation() {
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


    public void setControlPaneOffset(double controlPaneOffset) {
        this.controlPaneOffset = controlPaneOffset;
        starPlotManager.setControlPaneOffset(controlPaneOffset);
        gridPlotManager.setControlPaneOffset(controlPaneOffset);
        routeManager.setControlPaneOffset(controlPaneOffset);
        transitManager.setControlPaneOffset(controlPaneOffset);
    }

    private void run() {
        updateLabels();
    }

    private void scheduleLabelUpdate() {
        labelUpdatePause.setOnFinished(event -> updateLabels());
        labelUpdatePause.playFromStart();
    }

    ///////////////////////// event manager

    /**
     * handle the user events
     */
    private void handleUserEvents() {

        // handle keyboard key press events
        subScene.setOnKeyPressed(this::keyEventHandler);

        // handle mouse scroll events
        subScene.setOnScroll(this::mouseScrollEventHandler);

        // handle mouse press events
        subScene.setOnMousePressed(this::mousePressEventHandler);

        // handle mouse drag events
        subScene.setOnMouseDragged(this::mouseDragEventHandler);

    }

    private void mouseScrollEventHandler(ScrollEvent event) {
        double deltaY = event.getDeltaY();
        zoomGraph(deltaY * 5);
        updateLabels();
    }

    private void mousePressEventHandler(MouseEvent me) {
        mousePosX = me.getSceneX();
        mousePosY = me.getSceneY();
        mouseOldX = me.getSceneX();
        mouseOldY = me.getSceneY();
        subScene.requestFocus();
    }

    private void mouseDragEventHandler(MouseEvent me) {
        int direction = userControls.isControlSense() ? +1 : -1;
        mouseOldX = mousePosX;
        mouseOldY = mousePosY;
        mousePosX = me.getSceneX();
        mousePosY = me.getSceneY();
        mouseDeltaX = (mousePosX - mouseOldX);
        mouseDeltaY = (mousePosY - mouseOldY);
        double modifier = UserControls.NORMAL_SPEED;

        if (me.isPrimaryButtonDown() && me.isControlDown()) {
            double width = getWidth();
            double height = getHeight();
            translateXY(width / 2 - mousePosX, height / 2 - mousePosY);
        } else if (me.isPrimaryButtonDown()) {
            if (me.isAltDown()) { //roll
                roll(direction, modifier); // +
            } else {
                rotateXY(direction, modifier, mouseDeltaX, mouseDeltaY);
            }
        }
        updateLabels();
    }

    private void translateXY(double mousePosX, double mousePosY) {
        camera.setTranslateX(mousePosX);
        camera.setTranslateY(mousePosY);
    }

    private void rotateXY(int direction, double modifier, double mouseDeltaX, double mouseDeltaY) {
        rotateZ.setAngle(((rotateZ.getAngle() + direction * mouseDeltaX * modifier) % 360));
        rotateX.setAngle(((rotateX.getAngle() - direction * mouseDeltaY * modifier) % 360));

//        rotateX.setAngle((rotateY.getAngle() - direction * mouseDeltaY * modifier)  % 360 );
//        rotateY.setAngle((rotateX.getAngle() + direction * mouseDeltaX * modifier)  % 360 );
    }

    private void roll(int direction, double modifier) {
        rotateZ.setAngle(((rotateZ.getAngle() + direction * mouseDeltaX * modifier)) % 360);
    }

    private void keyEventHandler(KeyEvent event) {
        switch (event.getCode()) {
            case Z:
                if (event.isShiftDown()) {
                    log.info("shift pressed -> Z");
//                        cameraXform.ry.setAngle(0.0);
//                        cameraXform.rx.setAngle(0.0);
//                        camera.setTranslateZ(-300.0);
                }
//                    cameraXform2.t.setX(0.0);
//                    cameraXform2.t.setY(0.0);
                break;
            case X:
                if (event.isControlDown()) {
                    log.info("control pressed -> X");
//                        gridGroup.setVisible(!gridGroup.isVisible());
                }
                break;
            case S:
                break;
            case SPACE:
                break;
            case UP:
                if (event.isControlDown() && event.isShiftDown()) {
                    log.info("control and shift pressed -> up");
//                        cameraXform2.t.setY(cameraXform2.t.getY() - 10.0 * CONTROL_MULTIPLIER);
                } else if (event.isAltDown() && event.isShiftDown()) {
                    log.info("alt and shift pressed -> up");
//                        cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 10.0 * ALT_MULTIPLIER);
                } else if (event.isControlDown()) {
                    log.info("control pressed -> up");
//                        cameraXform2.t.setY(cameraXform2.t.getY() - 1.0 * CONTROL_MULTIPLIER);
                } else if (event.isAltDown()) {
                    log.info("alt pressed -> up");
//                        cameraXform.rx.setAngle(cameraXform.rx.getAngle() - 2.0 * ALT_MULTIPLIER);
                } else if (event.isShiftDown()) {
                    log.info("shift pressed -> up");
//                        double z = camera.getTranslateZ();
//                        double newZ = z + 5.0 * SHIFT_MULTIPLIER;
//                        camera.setTranslateZ(newZ);
                }
                break;
            case DOWN:
                if (event.isControlDown() && event.isShiftDown()) {
                    log.info("control shift pressed -> down");
//                        cameraXform2.t.setY(cameraXform2.t.getY() + 10.0 * CONTROL_MULTIPLIER);
                } else if (event.isAltDown() && event.isShiftDown()) {
                    log.info("alt and shift pressed -> down");
//                        cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 10.0 * ALT_MULTIPLIER);
                } else if (event.isControlDown()) {
                    log.info("control pressed -> down");
//                        cameraXform2.t.setY(cameraXform2.t.getY() + 1.0 * CONTROL_MULTIPLIER);
                } else if (event.isAltDown()) {
                    log.info("alt pressed -> down");
//                        cameraXform.rx.setAngle(cameraXform.rx.getAngle() + 2.0 * ALT_MULTIPLIER);
                } else if (event.isShiftDown()) {
                    log.info("shift pressed -> down");
//                        double z = camera.getTranslateZ();
//                        double newZ = z - 5.0 * SHIFT_MULTIPLIER;
//                        camera.setTranslateZ(newZ);
                }
                break;
            case RIGHT:
                if (event.isControlDown() && event.isShiftDown()) {
                    log.info("shift and control pressed -> right");
//                        cameraXform2.t.setX(cameraXform2.t.getX() + 10.0 * CONTROL_MULTIPLIER);
                } else if (event.isAltDown() && event.isShiftDown()) {
                    log.info("shift and alt pressed -> right");
//                        cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 10.0 * ALT_MULTIPLIER);
                } else if (event.isControlDown()) {
                    log.info("control pressed -> right");
//                        cameraXform2.t.setX(cameraXform2.t.getX() + 1.0 * CONTROL_MULTIPLIER);
                } else if (event.isAltDown()) {
                    log.info("alt pressed -> right");
//                        cameraXform.ry.setAngle(cameraXform.ry.getAngle() - 2.0 * ALT_MULTIPLIER);
                }
                break;
            case LEFT:
                if (event.isControlDown() && event.isShiftDown()) {
                    log.info("shift and control pressed -> left");
//                        cameraXform2.t.setX(cameraXform2.t.getX() - 10.0 * CONTROL_MULTIPLIER);
                } else if (event.isAltDown() && event.isShiftDown()) {
                    log.info("shift and alt pressed -> right");
//                        cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 10.0 * ALT_MULTIPLIER);  // -
                } else if (event.isControlDown()) {
                    log.info("control pressed -> right");
//                        cameraXform2.t.setX(cameraXform2.t.getX() - 1.0 * CONTROL_MULTIPLIER);
                } else if (event.isAltDown()) {
                    log.info("alt pressed -> right");
//                        cameraXform.ry.setAngle(cameraXform.ry.getAngle() + 2.0 * ALT_MULTIPLIER);  // -
                }
                break;
            default:
                log.info("keyboard Event is {}", event.getCode());
        }
    }

    public void displayRoute(RouteDescriptor routeDescriptor, boolean state) {
        routeManager.changeDisplayStateOfRoute(routeDescriptor, state);
    }

}
