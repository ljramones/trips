package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.dialogs.routing.RouteDialog;
import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.graphics.CurrentPlot;
import com.teamgannon.trips.graphics.GridPlotManager;
import com.teamgannon.trips.graphics.StarNotesDialog;
import com.teamgannon.trips.graphics.entities.*;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.GraphEnablesPersist;
import com.teamgannon.trips.listener.*;
import com.teamgannon.trips.routing.Route;
import com.teamgannon.trips.routing.RouteManager;
import com.teamgannon.trips.screenobjects.StarEditDialog;
import com.teamgannon.trips.screenobjects.StarEditStatus;
import com.teamgannon.trips.transits.TransitManager;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;

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

    /**
     * used to implement a selection model for selecting stars
     */
    private final Map<Node, StarSelectionModel> selectionModel = new HashMap<>();

    ////////////   Graphics Section of definitions  ////////////////
    private final Group root = new Group();
    private final Xform world = new Xform();
    private final Xform extensionsGroup = new Xform();
    private final Xform stellarDisplayGroup = new Xform();


    // used to control label visibility
    private final Xform labelDisplayGroup = new Xform();

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
    private final int depth;
    private final double lineWidth = 0.5;

    /**
     * label state
     */
    private boolean labelsOn = true;
    private boolean politiesOn = true;

    /**
     * is there a plot on screen?
     */
    private boolean plotActive = false;

    /**
     * our current plot
     */
    private CurrentPlot currentPlot;

    /**
     * the general color palette of the graph
     */
    private ColorPalette colorPalette;

    /**
     * star display specifics
     */
    private StarDisplayPreferences starDisplayPreferences;

    /**
     * the civilization and
     */
    private CivilizationDisplayPreferences politiesPreferences;

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
    private final ListUpdater listUpdater;

    /**
     * used to signal an update to the parent property panes
     */
    private final StellarPropertiesDisplayer displayer;

    /**
     * used to an update to the parent controlling which graphics
     * panes is being displayed
     */
    private ContextSelectorListener contextSelectorListener;

    /**
     * the redraw listener
     */
    private RedrawListener redrawListener;

    /**
     * the report generator
     */
    private ReportGenerator reportGenerator;


    // mouse positions
    private double mousePosX, mousePosY = 0;
    private double mouseOldX, mouseOldY = 0;
    private double mouseDeltaX, mouseDeltaY = 0;

    private final DatabaseListener databaseListener;

    private final RouteManager routeManager;

    private final TransitManager transitManager;

    // the lookout for drawn stars
    private final Map<UUID, Xform> starLookup = new HashMap<>();

    private RotateTransition highlightRotator;

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
                                 ListUpdater listUpdater,
                                 StellarPropertiesDisplayer displayer,
                                 DatabaseListener dbUpdater) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.tripsContext = tripsContext;

        // setup defaults
        this.colorPalette = tripsContext.getAppViewPreferences().getColorPallete();
        this.starDisplayPreferences = tripsContext.getAppViewPreferences().getStarDisplayPreferences();
        this.politiesPreferences = tripsContext.getAppViewPreferences().getCivilizationDisplayPreferences();

        this.listUpdater = listUpdater;
        this.displayer = displayer;
        this.databaseListener = dbUpdater;

        this.routeManager = new RouteManager(
                routeUpdaterListener,
                starLookup
        );
        this.gridPlotManager = new GridPlotManager(
                extensionsGroup,
                spacing, width, depth, lineWidth,
                colorPalette
        );

        this.transitManager = new TransitManager(world, routeUpdaterListener);


        this.setMinHeight(height);
        this.setMinWidth(width);

        // setup data structures for each independent element
        world.getChildren().add(gridPlotManager.getGridGroup());
        world.getChildren().add(gridPlotManager.getScaleGroup());

        stellarDisplayGroup.setWhatAmI("Stellar Group");
        world.getChildren().add(stellarDisplayGroup);

        extensionsGroup.setWhatAmI("Star Extensions");
        world.getChildren().add(extensionsGroup);

        world.getChildren().add(routeManager.getRoutesGroup());

        world.getChildren().add(transitManager.getTransitGroup());

        labelDisplayGroup.setWhatAmI("Labels");
        world.getChildren().add(labelDisplayGroup);

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
    }

    public void setCivilizationPreferences(CivilizationDisplayPreferences preferences) {
        this.politiesPreferences = preferences;
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

        currentPlot = new CurrentPlot();
        currentPlot.setDataSetDescriptor(dataSetDescriptor);
        currentPlot.setCenterCoordinates(centerCoordinates);
        currentPlot.setStarDisplayPreferences(starDisplayPreferences);
        currentPlot.setCivilizationDisplayPreferences(civilizationDisplayPreferences);

        routeManager.setDatasetContext(dataSetDescriptor);

        plotActive = true;
    }

    //////////////////////

    public GridPlotManager getGridPlotManager() {
        return gridPlotManager;
    }

    public void highlightStar(UUID starId) {
        Xform starGroup = starLookup.get(starId);
        if (highlightRotator != null) {
            highlightRotator.stop();
        }
        highlightRotator = setRotationAnimation(starGroup);
        highlightRotator.play();
        log.info("mark point");
    }

    private static RotateTransition setRotationAnimation(Group group) {
        RotateTransition rotate = new RotateTransition(
                Duration.seconds(10),
                group
        );
        rotate.setAxis(Rotate.Y_AXIS);
        rotate.setFromAngle(360);
        rotate.setToAngle(0);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        return rotate;
    }

    public void clearPlot() {
        starLookup.clear();
    }

    public double getDepth() {
        return depth;
    }

    public void changeColors(ColorPalette colorPalette) {
        this.colorPalette = colorPalette;
        tripsContext.getAppViewPreferences().setColorPallete(colorPalette);
    }

    public List<StarDisplayRecord> getCurrentStarsInView() {
        List<StarDisplayRecord> starsInView = new ArrayList<>();
        for (UUID id : starLookup.keySet()) {
            StarDisplayRecord starDisplayRecord = (StarDisplayRecord) starLookup.get(id).getUserData();
            starsInView.add(starDisplayRecord);
        }
        starsInView.sort(Comparator.comparing(StarDisplayRecord::getStarName));
        return starsInView;
    }

    //////////////////////////  External event updaters   ////////////////////////

    /**
     * set the context updater
     *
     * @param contextSelectorListener the context selector
     */
    public void setContextUpdater(ContextSelectorListener contextSelectorListener) {
        this.contextSelectorListener = contextSelectorListener;
    }

    /**
     * callback when there is a redraw
     *
     * @param redrawListener the notification that a redraw is happening
     */
    public void setRedrawListener(RedrawListener redrawListener) {
        this.redrawListener = redrawListener;
    }

    /**
     * callback for report generation
     *
     * @param reportGenerator the report generator
     */
    public void setReportGenerator(ReportGenerator reportGenerator) {
        this.reportGenerator = reportGenerator;
    }

    //////////////////////////  public methods /////////////////////////////

    /**
     * clear the stars from the display
     */
    public void clearStars() {

        // remove stars
        stellarDisplayGroup.getChildren().clear();

        // remove the extension points to the stars
        extensionsGroup.getChildren().clear();

        clearRoutes();

        // clear the list
        if (listUpdater != null) {
            listUpdater.clearList();
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
        gridPlotManager.toggleExtensions(graphEnablesPersist.isDisplayStems());
        gridPlotManager.toggleScale(graphEnablesPersist.isDisplayLegend());
        labelsOn = graphEnablesPersist.isDisplayLabels();
    }

    /**
     * toggle the grid
     *
     * @param gridToggle the status for the grid
     */
    public void toggleGrid(boolean gridToggle) {
        gridPlotManager.toggleGrid(gridToggle);
    }

    public void togglePolities(boolean polities) {
        this.politiesOn = polities;
        log.info("toggle polities: {}", polities);

        // we can only do this if there are plot element on screen
        if (plotActive) {
            redrawPlot();
        }
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
        gridPlotManager.toggleExtensions(extensionsOn);
    }

    /**
     * toggle the stars
     *
     * @param starsOn the status of the stars
     */
    public void toggleStars(boolean starsOn) {
        stellarDisplayGroup.setVisible(starsOn);
        if (gridPlotManager.isVisible()) {
            gridPlotManager.extensionsOn(true);
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


    public void toggleTransits(boolean transitsOn) {
        transitManager.setVisible(transitsOn);

    }

    /**
     * toggle the labels
     *
     * @param labelSetting true is labels should be on
     */
    public void toggleLabels(boolean labelSetting) {
        this.labelsOn = labelSetting;

        // we can only do this if there are plot element on screen
        if (plotActive) {
            redrawPlot();
        }
    }

    private void redrawPlot() {
        clearPlot();
        clearRoutes();
        clearStars();
        log.info("redrawing plot: labels= {}, polities = {}", labelsOn, politiesOn);
        List<StarDisplayRecord> recordList = currentPlot.getStarDisplayRecordList();
        recordList.forEach(
                starDisplayRecord -> plotStar(starDisplayRecord,
                        currentPlot.getCenterStar(),
                        colorPalette,
                        currentPlot.getStarDisplayPreferences()
                )
        );
        // re-plot routes
        plotRoutes(currentPlot.getDataSetDescriptor().getRoutes());
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
    public void drawStar(List<StarDisplayRecord> recordList, String centerStar, ColorPalette colorPalette) {

        for (StarDisplayRecord star : recordList) {
            drawStar(star, centerStar, colorPalette, starDisplayPreferences);
        }
        createExtensionGroup(recordList, colorPalette.getExtensionColor());

        // there is an active plot on screen
        plotActive = true;
    }

    /**
     * draw a star
     *
     * @param record     the star record
     * @param centerStar the name of the center star
     */
    public void drawStar(StarDisplayRecord record,
                         String centerStar,
                         ColorPalette colorPalette,
                         StarDisplayPreferences starDisplayPreferences) {

        currentPlot.addRecord(record.copy());
        currentPlot.setCenterStar(centerStar);

        plotStar(record, centerStar, colorPalette, starDisplayPreferences);

    }

    private void plotStar(StarDisplayRecord record,
                          String centerStar,
                          ColorPalette colorPalette,
                          StarDisplayPreferences starDisplayPreferences) {
        Xform starNode;
        // create a star for display
        if (record.getStarName().equals(centerStar)) {
            // we use a special icon for the center of the diagram plot
            starNode = createCentralPoint(record, colorPalette, starDisplayPreferences);
            log.info("sol is at {}", record.getActualCoordinates());
        } else {
            // otherwise draw a regular star
            starNode = createStar(
                    record,
                    colorPalette,
                    starDisplayPreferences,
                    labelsOn,
                    politiesOn);
            createExtension(record, colorPalette.getExtensionColor());
        }
        starLookup.put(record.getRecordId(), starNode);

        // draw the star on the pane
        stellarDisplayGroup.getChildren().add(starNode);
    }


    /**
     * Draw the central star to the plot
     *
     * @param record                 the star record to show
     * @param colorPalette           the color palette to use
     * @param starDisplayPreferences the star display preferences
     * @return the graphical object group representing the star
     */
    private Xform createCentralPoint(StarDisplayRecord record,
                                     ColorPalette colorPalette,
                                     StarDisplayPreferences starDisplayPreferences) {

        Label label = StellarEntityFactory.createLabel(record, colorPalette);
        labelDisplayGroup.getChildren().add(label);

        Node star = StellarEntityFactory.drawCentralIndicator(
                record,
                colorPalette,
                label,
                starDisplayPreferences);

        if (listUpdater != null) {
            listUpdater.updateList(record);
        }

        ContextMenu starContextMenu = createPopup(record.getStarName(), star);
        star.addEventHandler(MouseEvent.MOUSE_CLICKED,
                e -> starClickEventHandler(star, starContextMenu, e));
        star.setOnMousePressed(event -> {
            Node node = (Node) event.getSource();
            StarDisplayRecord starDescriptor = (StarDisplayRecord) node.getUserData();
            log.info("mouse click detected! " + starDescriptor);
        });

        Xform starNode = new Xform();
        starNode.setId("central");
        starNode.setUserData(record);
        starNode.getChildren().add(star);
        return starNode;
    }


    /**
     * create a star named with radius and color located at x,y,z
     *
     * @param record                 the star record
     * @param colorPalette           the color palette to use
     * @param starDisplayPreferences the star preferences
     * @param labelsOn               whether labels are on or off
     * @param politiesOn             whether we polities on or off
     * @return the star to plot
     */
    private Xform createStar(StarDisplayRecord record,
                             ColorPalette colorPalette,
                             StarDisplayPreferences starDisplayPreferences,
                             boolean labelsOn,
                             boolean politiesOn) {

        Node star = StellarEntityFactory.drawStellarObject(
                record,
                colorPalette,
                labelsOn,
                politiesOn,
                starDisplayPreferences,
                politiesPreferences);

        Tooltip tooltip = new Tooltip(record.getStarName());
        Tooltip.install(star, tooltip);

        if (listUpdater != null) {
            listUpdater.updateList(record);
        }

        ContextMenu starContextMenu = createPopup(record.getStarName(), star);
        star.addEventHandler(
                MouseEvent.MOUSE_CLICKED,
                e -> starClickEventHandler(star, starContextMenu, e));
        star.setOnMousePressed(event -> {
            Node node = (Node) event.getSource();
            StarDisplayRecord starDescriptor = (StarDisplayRecord) node.getUserData();
            log.info("mouse click detected! " + starDescriptor);
        });

        Xform starNode = new Xform();
        starNode.setId("regularStar");
        starNode.setUserData(record);
        starNode.getChildren().add(star);
        return starNode;
    }

    ////////////// Star creation helpers  //////////////

    /**
     * create a set of extensions for a set of stars
     *
     * @param recordList the list of stars
     */
    private void createExtensionGroup(List<StarDisplayRecord> recordList, Color extensionColor) {
        for (StarDisplayRecord record : recordList) {
            createExtension(record, extensionColor);
        }
    }

    /**
     * create an extension for an added star
     *
     * @param record         the star
     * @param extensionColor the color of the extensions from grid to star
     */
    private void createExtension(StarDisplayRecord record, Color extensionColor) {
        Point3D point3DFrom = record.getCoordinates();
        Point3D point3DTo = new Point3D(point3DFrom.getX(), 0, point3DFrom.getZ());
        Node lineSegment = CustomObjectFactory.createLineSegment(point3DFrom, point3DTo, lineWidth, extensionColor);
        extensionsGroup.getChildren().add(lineSegment);
        // add the extensions group to the world model
        extensionsGroup.setVisible(true);
    }

    /**
     * setup the event click handler for a star
     *
     * @param star            the star
     * @param starContextMenu the menu
     * @param e               the exception caught
     */
    private void starClickEventHandler(Node star, ContextMenu starContextMenu, MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            log.info("Primary button pressed");
            starContextMenu.show(star, e.getScreenX(), e.getScreenY());
        }
        if (e.getButton() == MouseButton.MIDDLE) {
            log.info("Middle button pressed");
            starContextMenu.show(star, e.getScreenX(), e.getScreenY());
        }

        if (e.getButton() == MouseButton.SECONDARY) {
            log.info("Secondary button pressed");
            if (selectionModel.containsKey(star)) {
                // remove star and selection rectangle
                StarSelectionModel starSelectionModel = selectionModel.get(star);
                Group group = (Group) star;
                group.getChildren().remove(starSelectionModel.getSelectionRectangle());

                // remove the selection model
                selectionModel.remove(star);

            } else {
                // add star selection
                StarSelectionModel starSelectionModel = new StarSelectionModel();
                starSelectionModel.setStarNode(star);
                selectionModel.put(star, starSelectionModel);

            }
        }
    }

    /**
     * remove a star node form the db
     *
     * @param starDisplayRecord the star to remove
     */
    private void removeNode(StarDisplayRecord starDisplayRecord) {
        log.info("Removing object for:" + starDisplayRecord.getStarName());
        databaseListener.removeStar(starDisplayRecord.getRecordId());
    }

    /**
     * edit a star in the database
     *
     * @param starDisplayRecord the properties to edit
     */
    private StarDisplayRecord editProperties(StarDisplayRecord starDisplayRecord) {
        AstrographicObject starObject = databaseListener.getStar(starDisplayRecord.getRecordId());
        StarEditDialog starEditDialog = new StarEditDialog(starObject);
        Optional<StarEditStatus> optionalStarDisplayRecord = starEditDialog.showAndWait();
        if (optionalStarDisplayRecord.isPresent()) {
            StarEditStatus status = optionalStarDisplayRecord.get();
            if (status.isChanged()) {
                AstrographicObject record = status.getRecord();
                StarDisplayRecord record1 = StarDisplayRecord.fromAstrographicObject(record, starDisplayPreferences);
                record1.setCoordinates(starDisplayRecord.getCoordinates());
                log.info("Changed value: {}", record);
                databaseListener.updateStar(record);
                return record1;
            } else {
                log.error("no return");
                return null;
            }
        }
        log.info("Editing properties in side panes for:" + starDisplayRecord.getStarName());
        return null;
    }

    /**
     * display properties for this star
     *
     * @param astrographicObject the properties to display
     */
    private void displayProperties(AstrographicObject astrographicObject) {
        log.info("Showing properties in side panes for:" + astrographicObject.getDisplayName());
        if (displayer != null) {
            displayer.displayStellarProperties(astrographicObject);
        }
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


    ////////////////// Context Menus  ///////////////////

    /**
     * create a context menu for clicking on the stars
     *
     * @param name the name of the star
     * @param star the star
     * @return the menu
     */
    private ContextMenu createPopup(String name, Node star) {
        final ContextMenu cm = new ContextMenu();

        MenuItem titleItem = new MenuItem(name);
        titleItem.setDisable(true);
        cm.getItems().add(titleItem);

        MenuItem setStarMenuItem = createSetStarMenuitem(star);
        cm.getItems().add(setStarMenuItem);

        MenuItem recenterMenuItem = createRecenterMenuitem(star);
        cm.getItems().add(recenterMenuItem);

        MenuItem jumpSystemMenuItem = createEnterSystemItem(star);
        cm.getItems().add(jumpSystemMenuItem);

        cm.getItems().add(new SeparatorMenuItem());
        MenuItem startRouteMenuItem = createRoutingMenuItem(star);
        cm.getItems().add(startRouteMenuItem);

        MenuItem continueRouteMenuItem = continueRoutingMenuItem(star);
        cm.getItems().add(continueRouteMenuItem);

        MenuItem finishRouteMenuItem = finishRoutingMenuItem(star);
        cm.getItems().add(finishRouteMenuItem);

        MenuItem resetRouteMenuItem = resetRoutingMenuItem();
        cm.getItems().add(resetRouteMenuItem);

        cm.getItems().add(new SeparatorMenuItem());

        MenuItem distanceToMenuItem = distanceReportMenuItem(star);
        cm.getItems().add(distanceToMenuItem);

        cm.getItems().add(new SeparatorMenuItem());

        MenuItem propertiesMenuItem = createShowPropertiesMenuItem(star);
        cm.getItems().add(propertiesMenuItem);

        MenuItem enterNotesItem = createNotesMenuItem(star);
        cm.getItems().add(enterNotesItem);
        MenuItem editPropertiesMenuItem = createEditPropertiesMenuItem(star);
        cm.getItems().add(editPropertiesMenuItem);

        cm.getItems().add(new SeparatorMenuItem());

        MenuItem removeMenuItem = createRemoveMenuItem(star);
        cm.getItems().add(removeMenuItem);

        return cm;
    }

    private MenuItem createSetStarMenuitem(Node star) {
        MenuItem menuItem = new MenuItem("Highlight star");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            redrawListener.highlightStar(starDescriptor.getRecordId());

        });
        return menuItem;
    }

    private MenuItem createNotesMenuItem(Node star) {
        MenuItem menuItem = new MenuItem("Edit notes on this star");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            StarNotesDialog notesDialog = new StarNotesDialog(starDescriptor.getNotes());
            notesDialog.setTitle("Edit notes for " + starDescriptor.getStarName());
            Optional<String> notesOptional = notesDialog.showAndWait();
            if (notesOptional.isPresent()) {
                String notes = notesOptional.get();
                if (!notes.isEmpty()) {
                    // save notes in star
                    databaseListener.updateNotesForStar(starDescriptor.getRecordId(), notes);
                    // update the star notes on screen
                    starDescriptor.setNotes(notes);
                    star.setUserData(starDescriptor);
                }
            }

        });
        return menuItem;
    }

    private MenuItem distanceReportMenuItem(Node star) {
        MenuItem menuItem = new MenuItem("Generate Distances from this star");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            reportGenerator.generateDistanceReport(starDescriptor);
        });
        return menuItem;
    }

    private MenuItem createRecenterMenuitem(Node star) {
        MenuItem menuItem = new MenuItem("Recenter on this star");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            redrawListener.recenter(starDescriptor);
        });
        return menuItem;
    }

    private MenuItem createRoutingMenuItem(Node star) {
        MenuItem menuItem = new MenuItem("Start Route");
        menuItem.setOnAction(event -> {
            boolean routingActive = routeManager.isRoutingActive();
            if (routingActive) {
                Optional<ButtonType> buttonType = showConfirmationAlert("Remove Dataset",
                        "Restart Route?",
                        "You have a route in progress, Ok will clear current?");

                if ((buttonType.isEmpty()) || (buttonType.get() != ButtonType.OK)) {
                    return;
                }
            }
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            RouteDialog dialog = new RouteDialog(starDescriptor);
            Optional<RouteDescriptor> result = dialog.showAndWait();
            result.ifPresent(routeDescriptor -> routeManager.startRoute(routeDescriptor, starDescriptor));
        });
        return menuItem;
    }

    private MenuItem continueRoutingMenuItem(Node star) {
        MenuItem menuItem = new MenuItem("Continue Route");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            routeManager.continueRoute(starDescriptor);
        });
        return menuItem;
    }

    private MenuItem finishRoutingMenuItem(Node star) {
        MenuItem menuItem = new MenuItem("Finish Route");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            routeManager.finishRoute(starDescriptor);
        });
        return menuItem;
    }


    private MenuItem resetRoutingMenuItem() {
        MenuItem menuItem = new MenuItem("Reset Route");
        menuItem.setOnAction(this::resetRoute);
        return menuItem;
    }

    public void redrawRoutes(List<Route> routes) {
        routeManager.plotRoutes(routes);
    }


    ///////////////////// Routing

    /**
     * create a menuitem to remove a targeted item
     *
     * @return the menuitem supporting this action
     */
    private MenuItem createRemoveMenuItem(Node star) {
        MenuItem removeMenuItem = new MenuItem("Remove");
        removeMenuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            removeNode(starDescriptor);
        });
        return removeMenuItem;
    }

    /**
     * create an enter system object
     *
     * @param star the star selected
     * @return the menuitem
     */
    private MenuItem createEnterSystemItem(Node star) {
        MenuItem removeMenuItem = new MenuItem("Enter System");
        removeMenuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            jumpToSystem(starDescriptor);
        });
        return removeMenuItem;
    }

    /**
     * crate a menuitem to edit a targeted item
     *
     * @return the menuitem supporting this action
     */
    private MenuItem createEditPropertiesMenuItem(Node star) {
        MenuItem editPropertiesMenuItem = new MenuItem("Edit");
        editPropertiesMenuItem.setOnAction(event -> {
            StarDisplayRecord starDisplayRecord = (StarDisplayRecord) star.getUserData();
            StarDisplayRecord editRecord = editProperties(starDisplayRecord);
            star.setUserData(editRecord);

        });
        return editPropertiesMenuItem;
    }


    /**
     * create a menuitem to show properties
     *
     * @return the menuitem supporting this action
     */
    private MenuItem createShowPropertiesMenuItem(Node star) {
        MenuItem propertiesMenuItem = new MenuItem("Properties");
        propertiesMenuItem.setOnAction(event -> {
            StarDisplayRecord starDisplayRecord = (StarDisplayRecord) star.getUserData();
            AstrographicObject astrographicObject = databaseListener.getStar(starDisplayRecord.getRecordId());
            displayProperties(astrographicObject);
        });
        return propertiesMenuItem;
    }


    /**
     * jump to the solar system selected
     *
     * @param starDisplayRecord the properties of the star selected
     */
    private void jumpToSystem(StarDisplayRecord starDisplayRecord) {
        if (contextSelectorListener != null) {
            contextSelectorListener.selectSolarSystemSpace(starDisplayRecord);
        }
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


    private void resetRoute(ActionEvent event) {
        routeManager.resetRoute();
    }

}
