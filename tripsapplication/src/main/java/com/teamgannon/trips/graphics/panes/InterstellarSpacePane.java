package com.teamgannon.trips.graphics.panes;

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
import com.teamgannon.trips.routing.RouteManager;
import com.teamgannon.trips.routing.RoutingMetric;
import com.teamgannon.trips.starplotting.StarPlotManager;
import com.teamgannon.trips.transits.TransitManager;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.fxyz3d.geometry.MathUtils.clamp;

@Slf4j
public class InterstellarSpacePane extends Pane {
    private static final int CYCLE_COUNT = 30;

    // mouse positions
    private double mousePosX, mousePosY = 0;
    private double mouseOldX, mouseOldY = 0;
    private double mouseDeltaX, mouseDeltaY = 0;

    private final Rotate rotateX = new Rotate(25, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(25, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    private final Group root = new Group();
    private final Group world = new Group();

    private final SubScene subScene;

    PerspectiveCamera camera = new PerspectiveCamera(true);

    private static final double ROTATE_SECS = 60;

    /**
     * animation rotator
     */
    private final RotateTransition rotator;

    /////////////////

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

    /**
     * the grid plot manager
     */
    private final GridPlotManager gridPlotManager;

    private final RouteManager routeManager;

    private final TransitManager transitManager;

    private final StarPlotManager starPlotManager;

    private final Map<Node, Label> shapeToLabel = new HashMap<>();

    /**
     * constructor for the Graphics Pane
     *
     * @param sceneWidth  the width
     * @param sceneHeight the height
     */
    public InterstellarSpacePane(double sceneWidth,
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





        subScene = new SubScene(world, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        root.getChildren().add(subScene);

        subScene.setCamera(camera);

        this.starPlotManager = new StarPlotManager(
                world,
                shapeToLabel,
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
                shapeToLabel,
                routeUpdaterListener,
                currentPlot
        );

        this.gridPlotManager = new GridPlotManager(
                world,
                shapeToLabel,
                spacing, sceneWidth, depth,
                colorPalette
        );

        this.transitManager = new TransitManager(
                world,
                shapeToLabel,
                routeUpdaterListener
        );

        starPlotManager.setRouteManager(routeManager);

        this.setMinHeight(sceneHeight);
        this.setMinWidth(sceneWidth);

        // create a rotation animation
        rotator = createRotateAnimation();

        // create all the base display elements
        this.getChildren().add(root);
        setInitialView();
        handleMouseEvents();
    }

    /**
     * handle the mouse events
     */
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

        log.info("updateLabels: {}", shapeToLabel.size());
        shapeToLabel.forEach((node, label) -> {
            Point3D coordinates = node.localToScene(Point3D.ZERO, true);

            //Clipping Logic
            //if coordinates are outside of the scene it could
            //stretch the screen so don't transform them
            double x = coordinates.getX();
            double y = coordinates.getY();

            // is it left of the view?
            if (x < 0) {
                x = 0;
            }

            // is it right of the view?
            if ((x + label.getWidth() + 5) > subScene.getWidth()) {
                x = subScene.getWidth() - (label.getWidth() + 5);
            }

            // is it above the view?
            if (y < 0) {
                y = 0;
            }

            // is it below the view
            if ((y + label.getHeight()) > subScene.getHeight()) {
                y = subScene.getHeight() - (label.getHeight() + 5);
            }

            //update the local transform of the label.
            label.getTransforms().setAll(new Translate(x, y));
        });

        gridPlotManager.updateLabels();
    }

    /**
     * set the initial view
     */
    public void setInitialView() {
        setInitialCamera();
        world.getTransforms().addAll(rotateX, rotateY, rotateZ);
        world.getRotate();
    }

    /**
     * set the initial camera view
     */
    public void setInitialCamera() {
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-1500);
    }

    public void resetView() {
        setInitialView();
        camera.setRotate(25);
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

    public void plotRouteDescriptors(List<RoutingMetric> routeDescriptorList) {
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

    public boolean isPlotActive() {
        return starPlotManager.isPlotActive();
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

}
