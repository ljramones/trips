package com.teamgannon.trips.floating;

import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.graphics.CurrentPlot;
import com.teamgannon.trips.graphics.GridPlotManager;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.GraphEnablesPersist;
import com.teamgannon.trips.listener.*;
import com.teamgannon.trips.routing.Route;
import com.teamgannon.trips.routing.RoutingMetric;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.scene.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

import static org.fxyz3d.geometry.MathUtils.clamp;

@Slf4j
public class InterstellarPaneExample extends Pane {

    private final ListUpdaterListener listUpdaterListener;

    private  ColorPalette colorPalette;

    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;

    PerspectiveCamera camera = new PerspectiveCamera(true);

    // We'll use custom Rotate transforms to manage the coordinate conversions
    private final Rotate rotateX = new Rotate(25, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(25, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    private final Group root = new Group();
    private final Group world = new Group();  //all 3D nodes in scene

    private final SubScene subScene;

    private final StarPlotterManagerExample starPlotterManagerExample;

    private final RouteManagerExample routeManagerExample;

    private final TransitManagerExample transitManagerExample;

    private final GridPlotManagerExample gridPlotManagerExample;

    private TripsContext tripsContext;

    private StarDisplayPreferences starDisplayPreferences;
    private CurrentPlot currentPlot;


    /**
     * animation toggle
     */
    private boolean animationPlay = false;

    /**
     * animation rotator
     */
    private  RotateTransition rotator;

    private static final double ROTATE_SECS = 60;


    public InterstellarPaneExample(double sceneWidth,
                                   double sceneHeight,
                                   double depth,
                                   double spacing,
                                   TripsContext tripsContext,
                                   RouteUpdaterListener routeUpdaterListener,
                                   ListUpdaterListener listUpdaterListener,
                                   StellarPropertiesDisplayerListener displayer,
                                   DatabaseListener databaseListener,
                                   ContextSelectorListener contextSelectorListener,
                                   RedrawListener redrawListener,
                                   ReportGenerator reportGenerator) {

        this.tripsContext = tripsContext;
        this.listUpdaterListener = listUpdaterListener;

        currentPlot = new CurrentPlot();
        currentPlot.setStarDisplayPreferences(starDisplayPreferences);

        this.colorPalette = tripsContext.getAppViewPreferences().getColorPallete();
        this.starDisplayPreferences = tripsContext.getAppViewPreferences().getStarDisplayPreferences();

        // attach our custom rotation transforms so we can update the labels dynamically
        world.getTransforms().addAll(rotateX, rotateY, rotateZ);

        subScene = new SubScene(world, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        setPerspectiveCamera();

        subScene.setCamera(camera);
        Group sceneRoot = new Group(subScene);

        handleMouseEvents();

        // add to the 2D portion of this component
        Pane pane = new Pane();
        pane.setPrefSize(sceneWidth, sceneHeight);
        pane.setMaxSize(Pane.USE_COMPUTED_SIZE, Pane.USE_COMPUTED_SIZE);
        pane.setMinSize(Pane.USE_COMPUTED_SIZE, Pane.USE_COMPUTED_SIZE);
        pane.setBackground(Background.EMPTY);
        pane.getChildren().add(sceneRoot);
        pane.setPickOnBounds(false);

        subScene.widthProperty().bind(pane.widthProperty());
        subScene.heightProperty().bind(pane.heightProperty());
        Platform.runLater(this::updateLabels);
        root.getChildren().add(pane);

        this.starPlotterManagerExample = new StarPlotterManagerExample(
                sceneRoot,
                world,
                subScene,
                listUpdaterListener,
                redrawListener,
                databaseListener,
                displayer,
                contextSelectorListener,
                new StarDisplayPreferences(),
                reportGenerator,
                new CurrentPlot(),
                colorPalette
        );

        this.gridPlotManagerExample = new GridPlotManagerExample(
                world,
                sceneRoot,
                subScene,
                spacing, sceneWidth, depth,
                colorPalette);

        this.routeManagerExample = new RouteManagerExample(
                world,
                sceneRoot,
                subScene,
                routeUpdaterListener,
                currentPlot
        );

        this.transitManagerExample = new TransitManagerExample(
                world,
                sceneRoot,
                subScene,
                routeUpdaterListener
        );

        // create a rotation animation
        rotator = createRotateAnimation();

        // just for simulation
        starPlotterManagerExample.generateRandomStars(50);

    }

    private void setPerspectiveCamera() {
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-1500);
    }

    public Group getRoot() {
        return root;
    }

    public void setStellarPreferences(StarDisplayPreferences starDisplayPreferences) {
        this.starDisplayPreferences = starDisplayPreferences;
        this.starPlotterManagerExample.setStarDisplayPreferences(starDisplayPreferences);
    }

    public void setCivilizationPreferences(CivilizationDisplayPreferences preferences) {
        currentPlot.setCivilizationDisplayPreferences(preferences);
        starPlotterManagerExample.setCivilizationDisplayPreferences(preferences);
    }


    /**
     * finds all the transits for stars in view
     *
     * @param distanceRoutes the distance range selected
     */
    public void findTransits(DistanceRoutes distanceRoutes) {
        List<StarDisplayRecord> starsInView = getCurrentStarsInView();
        transitManagerExample.findTransits(distanceRoutes, starsInView);
    }

    /**
     * clear existing transits
     */
    public void clearTransits() {
        transitManagerExample.clearTransits();
    }


    /////////////////// SET DATASET CONTEXT  /////////////////

    public void setDataSetContext(DataSetDescriptor datasetName) {
        routeManagerExample.setDatasetContext(datasetName);
        transitManagerExample.setDatasetContext(datasetName);
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

        starPlotterManagerExample.setStarDisplayPreferences(starDisplayPreferences);

        routeManagerExample.setDatasetContext(dataSetDescriptor);
    }

    //////////////////////

    public GridPlotManagerExample getGridPlotManager() {
        return gridPlotManagerExample;
    }

    public void highlightStar(UUID starId) {
        starPlotterManagerExample.highlightStar(starId);
    }

    public void clearPlot() {
        starPlotterManagerExample.clearStars();
    }

    public void changeColors(ColorPalette colorPalette) {
        this.colorPalette = colorPalette;
        tripsContext.getAppViewPreferences().setColorPallete(colorPalette);
    }

    public List<StarDisplayRecord> getCurrentStarsInView() {
        return starPlotterManagerExample.getCurrentStarsInView();
    }



    //////////////////////////  public methods /////////////////////////////

    /**
     * clear the stars from the display
     */
    public void clearStars() {

        starPlotterManagerExample.clearStars();
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
        routeManagerExample.clearRoutes();
    }

    public void plotRoutes(List<Route> routeList) {
        routeManagerExample.plotRoutes(routeList);
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
        gridPlotManagerExample.toggleGrid(graphEnablesPersist.isDisplayGrid());
        starPlotterManagerExample.toggleExtensions(graphEnablesPersist.isDisplayStems());
        gridPlotManagerExample.toggleScale(graphEnablesPersist.isDisplayLegend());
        starPlotterManagerExample.toggleLabels(graphEnablesPersist.isDisplayLabels());

    }

    /**
     * toggle the grid
     *
     * @param gridToggle the status for the grid
     */
    public void toggleGrid(boolean gridToggle) {
        gridPlotManagerExample.toggleGrid(gridToggle);
        starPlotterManagerExample.toggleExtensions(gridPlotManagerExample.isVisible());
    }

    public void togglePolities(boolean polities) {
        starPlotterManagerExample.togglePolities(polities);
    }

    /**
     * toggle the transit lengths for the transits shown
     *
     * @param transitsLengthsOn flag for transit lengths
     */
    public void toggleTransitLengths(boolean transitsLengthsOn) {
        transitManagerExample.toggleTransitLengths(transitsLengthsOn);
    }

    /**
     * toggle the extensions
     *
     * @param extensionsOn the status of the extensions
     */
    public void toggleExtensions(boolean extensionsOn) {
        starPlotterManagerExample.toggleExtensions(extensionsOn);
    }

    /**
     * toggle the stars
     *
     * @param starsOn the status of the stars
     */
    public void toggleStars(boolean starsOn) {
        starPlotterManagerExample.toggleStars(starsOn);
        if (gridPlotManagerExample.isVisible()) {
            starPlotterManagerExample.toggleExtensions(starsOn);
        }
    }

    /**
     * toggle the scale
     *
     * @param scaleOn the status of the scale
     */
    public void toggleScale(boolean scaleOn) {
        gridPlotManagerExample.toggleScale(scaleOn);
    }

    /**
     * toggle the routes
     *
     * @param routesOn the status of the routes
     */
    public void toggleRoutes(boolean routesOn) {
        routeManagerExample.toggleRoutes(routesOn);
    }

    /**
     * toggle the transit view
     *
     * @param transitsOn true means transits on, false - otherwise
     */
    public void toggleTransits(boolean transitsOn) {
        transitManagerExample.setVisible(transitsOn);
    }

    public void redrawRoutes(List<Route> routes) {
        routeManagerExample.plotRoutes(routes);
    }

    public void plotRouteDescriptors(List<RoutingMetric> routeDescriptorList) {
        routeManagerExample.plotRouteDescriptors(routeDescriptorList);
    }

    /**
     * toggle the labels
     *
     * @param labelSetting true is labels should be on
     */
    public void toggleLabels(boolean labelSetting) {
        starPlotterManagerExample.toggleLabels(labelSetting);
    }

    public boolean isPlotActive() {
        return starPlotterManagerExample.isPlotActive();
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
        starPlotterManagerExample.drawStar(recordList, centerStar, colorPalette);
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


        starPlotterManagerExample.drawStar(record, centerStar, colorPalette, starDisplayPreferences);
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


    /**
     * set a fade transition on a node
     *
     * @param node the node to set
     */
    public void setFade(Node node, int cycleCount) {
        FadeTransition fader = new FadeTransition(Duration.seconds(5), node);
        fader.setFromValue(1.0);
        fader.setToValue(0.1);
        fader.setCycleCount(cycleCount);
        fader.setAutoReverse(true);
        fader.play();
    }








    /////////////
    private void handleMouseEvents() {
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
                    double modifier = 5.0;
                    double modifierFactor = 0.1;

                    if (me.isPrimaryButtonDown()) {
                        if (me.isAltDown()) { //roll
                            rotateZ.setAngle(((rotateZ.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180); // +
                        } else {
                            rotateY.setAngle(((rotateY.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180); // +
                            rotateX.setAngle(
                                    clamp(
                                            (((rotateX.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180),
                                            -60,
                                            60
                                    )
                            ); // -
                        }
                    }
                    updateLabels();
                }
        );
    }

    private void updateLabels() {
        starPlotterManagerExample.updateLabels();

        gridPlotManagerExample.updateScale();
        gridPlotManagerExample.updateLabels();

        routeManagerExample.updateLabels();

        transitManagerExample.updateLabels();
    }

}
