package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.dialog.RouteDialog;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.entities.StellarEntityFactory;
import com.teamgannon.trips.graphics.entities.Xform;
import com.teamgannon.trips.graphics.operators.*;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class InterstellarSpacePane extends Pane {

    //////////////  Star Definitions   //////////////////

    /**
     * list of star display records
     */
    private Set<StarDisplayRecord> starDisplayRecords = new HashSet<>();

    /**
     * used to implement a selection model for selecting stars
     */
    private Map<Node, StarSelectionModel> selectionModel = new HashMap<>();


    //////////////// event listeners and updaters
    /**
     * used to signal an update to the parent list view
     */
    private ListUpdater listUpdater;

    /**
     * used to signal an update to the parent property panes
     */
    private StellarPropertiesDisplayer displayer;

    /**
     * used to an update to the parent controlling which graphics
     * panes is being displayed
     */
    private ContextSelector contextSelector;

    /**
     * the route updater listener
     */
    private RouteUpdater routeUpdater;

    /**
     * the redraw listener
     */
    private RedrawListener redrawListener;

    /**
     * the report generator
     */
    private ReportGenerator reportGenerator;

    ////////////////// Routing ////////////////

    /**
     * this is the descriptor of the current route
     */
    private RouteDescriptor currentRoute;

    /**
     * the graphic portion of the current route
     */
    private Xform currentRouteDisplay = new Xform();

    /**
     * whether there is a route being traced, true is yes
     */
    private boolean routingActive = false;

    ////////////   Graphics Section of definitions  ////////////////
    private Group root = new Group();

    private Xform world = new Xform();
    private Xform gridGroup = new Xform();
    private Xform extensionsGroup = new Xform();
    private Xform stellarDisplayGroup = new Xform();
    private Xform scaleGroup = new Xform();
    private Xform routesGroup = new Xform();

    HashMap<Shape3D, Label> shape3DToLabel = new HashMap<>();

    // camera work
    private PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Xform cameraXform = new Xform();
    private final Xform cameraXform2 = new Xform();
    private final Xform cameraXform3 = new Xform();
    private static final double CAMERA_INITIAL_DISTANCE = -500;
    private static final double CAMERA_INITIAL_X_ANGLE = -25;//   -90
    private static final double CAMERA_INITIAL_Y_ANGLE = -25; // 0
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000.0;

    private double CONTROL_MULTIPLIER = 0.1;
    private double SHIFT_MULTIPLIER = 0.1;
    private double ALT_MULTIPLIER = 0.5;

    // mose positions
    private double mousePosX, mousePosY = 0;
    private double mouseOldX, mouseOldY = 0;
    private double mouseDeltaX, mouseDeltaY = 0;

    // animations
    private static final double ROTATE_SECS = 60;
    private RotateTransition rotator;

    /////////////////
    // screen real estate
    private int width;
    private int height;

    private int depth;
    private int spacing;

    private double lineWidth = 0.5;

    double modifier = 1.0;
    double modifierFactor = 0.1;

    private static String scaleString = "Scale: 1 grid is %d ly square";


    /**
     * constructor for the Graphics Pane
     *
     * @param width  the width
     * @param height the height
     */
    public InterstellarSpacePane(int width, int height, int depth, int spacing) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.spacing = spacing;

        this.setMinHeight(height);
        this.setMinWidth(width);

        // setup data structures for each independent element
        gridGroup.setWhatAmI("Planar Grid");
        world.getChildren().add(gridGroup);
        scaleGroup.setWhatAmI("Reference Scale");
        world.getChildren().add(scaleGroup);
        stellarDisplayGroup.setWhatAmI("Stellar Group");
        world.getChildren().add(stellarDisplayGroup);
        extensionsGroup.setWhatAmI("Star Extensions");
        world.getChildren().add(extensionsGroup);
        routesGroup.setWhatAmI("Star Routes");
        world.getChildren().add(routesGroup);

        // we don't connect this into the world yet
        createCurrentRouteDisplay();

        // create a rotation animation
        rotator = createRotateAnimation();

        // create all the base display elements
        buildRoot();
        buildScene();
        buildCamera();

        buildGrid();
        buildInitialScaleLegend();

        // handle mouse and keyboard events
        handleMouseEvents(this);
        handleKeyboard(this);
    }

    public double getDepth() {
        return depth;
    }


    //////////////////////////  External event updaters   ////////////////////////

    /**
     * set the list updater
     *
     * @param listUpdater the updater
     */
    public void setListUpdater(ListUpdater listUpdater) {
        this.listUpdater = listUpdater;
    }

    /**
     * set the stellar properties displayer
     *
     * @param displayer the displayer
     */
    public void setStellarObjectDisplayer(StellarPropertiesDisplayer displayer) {
        this.displayer = displayer;
    }

    /**
     * set the context updater
     *
     * @param contextSelector the context selector
     */
    public void setContextUpdater(ContextSelector contextSelector) {
        this.contextSelector = contextSelector;
    }

    /**
     * set the route updater
     *
     * @param routeUpdater the route updater
     */
    public void setRouteUpdater(RouteUpdater routeUpdater) {
        this.routeUpdater = routeUpdater;
    }

    /**
     * callback when there is a redraw
     *
     * @param redrawListener
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
        starDisplayRecords.clear();

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
        routesGroup.getChildren().clear();
    }

    /**
     * plot a number of stars
     *
     * @param starDisplayRecordList the list of stars
     */
    public void plotStars(List<StarDisplayRecord> starDisplayRecordList) {
        // clear stars, extensions and routes
        clearStars();

        // clear the list
        if (listUpdater != null) {
            listUpdater.clearList();
        }
    }

    /**
     * plot the routes
     *
     * @param routeList the list of routes
     */
    public void plotRoutes(List<RouteDescriptor> routeList) {
        // clear existing routes
        routesGroup.getChildren().clear();
        routeList.forEach(this::plotRoute);
    }

    public void plotRoute(RouteDescriptor routeDescriptor) {
        Xform route = StellarEntityFactory.createRoute(routeDescriptor);
        routesGroup.getChildren().add(route);
        routesGroup.setVisible(true);
    }


    public void createRoute(RouteDescriptor currentRoute) {
        this.currentRoute = currentRoute;
    }


    public void completeRoute() {

        // trigger that a new route has been created
        if (routeUpdater != null) {
            routeUpdater.newRoute(currentRoute);
        }
    }


    public void zoomIn() {
        zoomGraph(-50);
    }

    public void zoomOut() {
        zoomGraph(50);
    }

    /**
     * do actual zoom
     *
     * @param zoomAmt the amoutn to zoom
     */
    private void zoomGraph(double zoomAmt) {
        double z = camera.getTranslateZ();
        double newZ = z - zoomAmt;
        camera.setTranslateZ(newZ);
    }

    /**
     * toggle the grid
     *
     * @param gridToggle the status for the grid
     */
    public void toggleGrid(boolean gridToggle) {
        gridGroup.setVisible(gridToggle);
        extensionsGroup.setVisible(gridToggle);
    }

    /**
     * toggle the extensions
     *
     * @param extensionsOn the status of the extensions
     */
    public void toggleExtensions(boolean extensionsOn) {
        if (gridGroup.isVisible()) {
            extensionsGroup.setVisible(extensionsOn);
        }
    }

    /**
     * toggle the stars
     *
     * @param starsOn the status of the stars
     */
    public void toggleStars(boolean starsOn) {
        stellarDisplayGroup.setVisible(starsOn);
        if (gridGroup.isVisible()) {
            extensionsGroup.setVisible(starsOn);
        }
    }

    /**
     * toggle the scale
     *
     * @param scaleOn the status of the scale
     */
    public void toggleScale(boolean scaleOn) {
        scaleGroup.setVisible(scaleOn);
    }

    /**
     * toggle the routes
     *
     * @param routesOn the status of the routes
     */
    public void toggleRoutes(boolean routesOn) {
        routesGroup.setVisible(routesOn);
    }

    /**
     * start the rotation of Y-axis animation
     */
    public void startYrotate() {
        rotator.play();
    }

    /**
     * stop the rotation animation
     */
    public void stopYrotate() {
        rotator.pause();
    }

    /**
     * draw a star
     *
     * @param record     the star record
     * @param centerStar
     */
    public void drawStar(StarDisplayRecord record, String centerStar) {

        starDisplayRecords.add(record);

        Node starNode;
        // create a star for display
        if (record.getStarName().equals(centerStar)) {
            // we use a special icon for the center of the diagram plot
            starNode = createCentralPoint(record);
        } else {
            // otherwise draw a regular star
            starNode = createStar(record);
            createExtension(record);
        }

        // draw the star on the pane
        stellarDisplayGroup.getChildren().add(starNode);
    }

    private Xform createCentralPoint(StarDisplayRecord record) {
        Map<String, String> customProperties = record.toProperties();

        Node star = StellarEntityFactory.drawCentralIndicator(customProperties, record);

        starDisplayRecords.add(record);

        if (listUpdater != null) {
            listUpdater.updateList(customProperties);
        }

        ContextMenu starContextMenu = createPopup(record.getStarName(), star);
        star.addEventHandler(MouseEvent.MOUSE_CLICKED,
                e -> starClickEventHandler(star, starContextMenu, e));
        star.setOnMousePressed(event -> {
            Node node = (Node) event.getSource();
            Map<String, String> properties = (Map<String, String>) node.getUserData();
            log.info("mouse click detected! " + properties);
        });

        Xform starNode = new Xform();
        starNode.getChildren().add(star);
        return starNode;
    }

    /**
     * draw a list of stars
     *
     * @param recordList the list of stars
     */
    public void drawStar(List<StarDisplayRecord> recordList, String centerStar) {
        starDisplayRecords.addAll(recordList);
        for (StarDisplayRecord star : recordList) {
            drawStar(star, centerStar);
        }
        createExtensionGroup(recordList);
    }

    ////////////// Star creation helpers  //////////////


    private void createCurrentRouteDisplay() {
        currentRouteDisplay = new Xform();
        currentRouteDisplay.setWhatAmI("Current Route");
    }

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

    /**
     * create a set of extensions for a set of stars
     *
     * @param recordList the list of stars
     */
    private void createExtensionGroup(List<StarDisplayRecord> recordList) {
        for (StarDisplayRecord record : recordList) {
            createExtension(record);
        }
    }

    /**
     * create an extension for an added star
     *
     * @param record the star
     */
    private void createExtension(StarDisplayRecord record) {
        Point3D point3DFrom = record.getCoordinates();
        Point3D point3DTo = new Point3D(point3DFrom.getX(), 0, point3DFrom.getZ());
        Cylinder lineSegment
                = StellarEntityFactory.createLineSegment(
                point3DFrom, point3DTo, lineWidth, Color.BLUEVIOLET
        );
        extensionsGroup.getChildren().add(lineSegment);
        // add the extensions group to the world model
        extensionsGroup.setVisible(true);
    }

    private void rebuildScaleLegend(int newScaleLegend) {
        scaleGroup.getChildren().clear();
        createScaleLegend(newScaleLegend);
    }

    /**
     * build the scale for the display
     */
    private void buildInitialScaleLegend() {
        createScaleLegend(spacing);
    }

    private void createScaleLegend(int scaleValue) {
        Text scaleText = new Text(String.format(scaleString, scaleValue));
        scaleText.setFont(Font.font("Verdana", 20));
        scaleText.setFill(Color.WHITE);
        scaleGroup.getChildren().add(scaleText);
        scaleGroup.setTranslate(50, 350, 0);
    }

    /**
     * create a star named with radius and color located at x,y,z
     *
     * @param record the star record
     * @return the star to plot
     */
    private Xform createStar(StarDisplayRecord record) {

        Map<String, String> customProperties = record.toProperties();
        Node star = StellarEntityFactory.drawStellarObject(customProperties, record);

        Tooltip tooltip = new Tooltip(record.getStarName());
        Tooltip.install(star, tooltip);

        starDisplayRecords.add(record);

        if (listUpdater != null) {
            listUpdater.updateList(customProperties);
        }

        ContextMenu starContextMenu = createPopup(record.getStarName(), star);
        star.addEventHandler(MouseEvent.MOUSE_CLICKED,
                e -> starClickEventHandler(star, starContextMenu, e));
        star.setOnMousePressed(event -> {
            Node node = (Node) event.getSource();
            Map<String, String> properties = (Map<String, String>) node.getUserData();
            log.info("mouse click detected! " + properties);
        });

        Xform starNode = new Xform();
        starNode.getChildren().add(star);
        return starNode;
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
//        titleItem.getStyleClass().add("context-menu-title");
        cm.getItems().add(titleItem);

        MenuItem recenterMenuItem = createRecenterMenuitem(star);
        cm.getItems().add(recenterMenuItem);

        cm.getItems().add(new SeparatorMenuItem());

        MenuItem jumpSystemMenuItem = createEnterSystemItem(star);
        cm.getItems().add(jumpSystemMenuItem);

        cm.getItems().add(new SeparatorMenuItem());
        MenuItem startRouteMenuItem = createRoutingMenuItem(star);
        cm.getItems().add(startRouteMenuItem);

        MenuItem continueRouteMenuItem = continueRoutingMenuItem(star);
        cm.getItems().add(continueRouteMenuItem);

        MenuItem finishRouteMenuItem = finishRoutingMenuItem(star);
        cm.getItems().add(finishRouteMenuItem);

        MenuItem resetRouteMenuItem = resetRoutingMenuItem(star);
        cm.getItems().add(resetRouteMenuItem);

        cm.getItems().add(new SeparatorMenuItem());

        MenuItem distanceToMenuItem = distanceReportMenuItem(star);
        cm.getItems().add(distanceToMenuItem);

        cm.getItems().add(new SeparatorMenuItem());

        MenuItem propertiesMenuItem = createShowPropertiesMenuItem(star);
        cm.getItems().add(propertiesMenuItem);

        MenuItem editPropertiesMenuItem = createEditPropertiesMenuItem(star);
        cm.getItems().add(editPropertiesMenuItem);

        MenuItem removeMenuItem = createRemoveMenuItem(star);
        cm.getItems().add(removeMenuItem);

        return cm;
    }

    private MenuItem distanceReportMenuItem(Node star) {
        MenuItem menuItem = new MenuItem("Generate Distances from this star");
        menuItem.setOnAction(event -> {
            Map<String, String> properties = (Map<String, String>) star.getUserData();
            StarDisplayRecord starDescriptor = StarDisplayRecord.fromProperties(properties);
            reportGenerator.generateDistanceReport(starDescriptor);
        });
        return menuItem;
    }

    private MenuItem createRecenterMenuitem(Node star) {
        MenuItem menuItem = new MenuItem("Recenter on this star");
        menuItem.setOnAction(event -> {
            Map<String, String> properties = (Map<String, String>) star.getUserData();
            StarDisplayRecord starDescriptor = StarDisplayRecord.fromProperties(properties);
            redrawListener.recenter(starDescriptor);
        });
        return menuItem;
    }

    private MenuItem createRoutingMenuItem(Node star) {
        MenuItem menuItem = new MenuItem("Start Route");
        menuItem.setOnAction(event -> {
            Map<String, String> properties = (Map<String, String>) star.getUserData();
            RouteDialog dialog = new RouteDialog(properties);
            Optional<RouteDescriptor> result = dialog.showAndWait();
            result.ifPresent(routeDescriptor -> {
                startRoute(routeDescriptor, properties);
            });
        });
        return menuItem;
    }

    private MenuItem continueRoutingMenuItem(Node star) {
        MenuItem menuItem = new MenuItem("Continue Route");
        menuItem.setOnAction(event -> {
            Map<String, String> properties = (Map<String, String>) star.getUserData();
            continueRoute(properties);
        });
        return menuItem;
    }

    private MenuItem finishRoutingMenuItem(Node star) {
        MenuItem menuItem = new MenuItem("Finish Route");
        menuItem.setOnAction(event -> {
            Map<String, String> properties = (Map<String, String>) star.getUserData();
            finishRoute(properties);
        });
        return menuItem;
    }


    private MenuItem resetRoutingMenuItem(Node star) {
        MenuItem menuItem = new MenuItem("Reset Route");
        menuItem.setOnAction(event -> {
            Map<String, String> properties = (Map<String, String>) star.getUserData();
            resetRoute(properties);
        });
        return menuItem;
    }

    private void finishRoute(Map<String, String> properties) {
        createRouteSegment(properties);
        routingActive = false;
        makeRoutePermanent(currentRoute);
        routeUpdater.newRoute(currentRoute);
    }

    private void makeRoutePermanent(RouteDescriptor currentRoute) {
        // remove our hand drawn route
        routesGroup.getChildren().remove(currentRouteDisplay);

        // create a new one based on descriptor
        Xform displayRoute = createDisplayRoute(currentRoute);

        // add this created one to the routes group
        routesGroup.getChildren().add(displayRoute);
    }

    private Xform createDisplayRoute(RouteDescriptor currentRoute) {
        Xform route = new Xform();
        route.setWhatAmI(currentRoute.getName());
        Point3D previousPoint = new Point3D(0, 0, 0);
        boolean firstPoint = true;
        for (Point3D point3D : currentRoute.getLineSegments()) {
            if (firstPoint) {
                firstPoint = false;
            } else {
                Cylinder lineSegment = StellarEntityFactory.createLineSegment(previousPoint, point3D, lineWidth, currentRoute.getColor());
                route.getChildren().add(lineSegment);
            }
            previousPoint = point3D;
        }
        return route;
    }

    private void startRoute(RouteDescriptor routeDescriptor, Map<String, String> properties) {
        routingActive = true;
        currentRoute = routeDescriptor;
        log.info("Start charting the route:" + routeDescriptor);
        double x = Double.parseDouble(properties.get("x"));
        double y = Double.parseDouble(properties.get("y"));
        double z = Double.parseDouble(properties.get("z"));
        UUID id = UUID.fromString(properties.get("recordNumber"));
        if (currentRoute != null) {
            Point3D toPoint3D = new Point3D(x, y, z);
            currentRoute.getLineSegments().add(toPoint3D);
            currentRoute.getRouteList().add(id);
            routesGroup.getChildren().add(currentRouteDisplay);
            routesGroup.setVisible(true);
        }
    }

    private void continueRoute(Map<String, String> properties) {
        if (routingActive) {
            createRouteSegment(properties);
            log.info("Next Routing step:", currentRoute);
        }
    }

    private void createRouteSegment(Map<String, String> properties) {
        double x = Double.parseDouble(properties.get("x"));
        double y = Double.parseDouble(properties.get("y"));
        double z = Double.parseDouble(properties.get("z"));
        UUID id = UUID.fromString(properties.get("recordNumber"));

        if (currentRoute != null) {
            int size = currentRoute.getLineSegments().size();
            Point3D fromPoint = currentRoute.getLineSegments().get(size - 1);
            Point3D toPoint3D = new Point3D(x, y, z);
            Cylinder lineSegment = StellarEntityFactory.createLineSegment(
                    fromPoint, toPoint3D, 0.5, currentRoute.getColor()
            );
            currentRouteDisplay.getChildren().add(lineSegment);
            currentRoute.getLineSegments().add(toPoint3D);
            currentRoute.getRouteList().add(id);
            currentRouteDisplay.setVisible(true);
        }
    }

    /**
     * reset the route and remove the parts that were partially drawn
     *
     * @param properties the properties
     */
    private void resetRoute(Map<String, String> properties) {
        if (currentRoute != null) {
            currentRoute.clear();
        }
        routesGroup.getChildren().remove(currentRouteDisplay);
        routingActive = false;
        createCurrentRouteDisplay();
        resetCurrentRoute();
        log.info("Resetting the route");
    }

    /**
     * reset the current route
     */
    private void resetCurrentRoute() {
        currentRoute.clear();
        currentRouteDisplay = new Xform();
        currentRouteDisplay.setWhatAmI("Current Route");
    }

    /**
     * create a menuitem to remove a targeted item
     *
     * @return the menuitem supporting this action
     */
    private MenuItem createRemoveMenuItem(Node star) {
        MenuItem removeMenuItem = new MenuItem("Remove");
        removeMenuItem.setOnAction(event -> {
            Map<String, String> properties = (Map<String, String>) star.getUserData();
            removeNode(properties);
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
            Map<String, String> properties = (Map<String, String>) star.getUserData();
            jumpToSystem(properties);
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
            Map<String, String> properties = (Map<String, String>) star.getUserData();
            editProperties(properties);
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
            Map<String, String> properties = (Map<String, String>) star.getUserData();
            displayProperties(properties);
        });
        return propertiesMenuItem;
    }


    /**
     * jump to the solar system selected
     *
     * @param properties the properties of the star selected
     */
    private void jumpToSystem(Map<String, String> properties) {
        if (contextSelector != null) {
            contextSelector.selectSolarSystemSpace(properties);
        }
    }

    /**
     * remove a star node form the db
     *
     * @param properties the properties to remove
     */
    private void removeNode(Map<String, String> properties) {
        log.info("Removing object for:" + properties.get("name"));
    }

    /**
     * edit a star in the database
     *
     * @param properties the properties to edit
     */
    private void editProperties(Map<String, String> properties) {
        log.info("Editing properties in side panes for:" + properties.get("name"));
    }

    /**
     * display properties for this star
     *
     * @param properties the properties to display
     */
    private void displayProperties(Map<String, String> properties) {
        log.info("Showing properties in side panes for:" + properties.get("name"));
        if (displayer != null) {
            displayer.displayStellarProperties(properties);
        }
    }

    ////////////////////////

    private GridPane createPopupGridPaneForNode(Map<String, String> properties) {
        GridPane pane = new GridPane();
        pane.getChildren().clear();
        int i = 0;
        for (String key : properties.keySet()) {
            pane.addRow(i++, new Label(key + ":"), new Label(properties.get(key)));
        }
        return pane;
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


    public void rebuildGrid(double scaleIncrement, double gridScale) {
        // clear old grid
        gridGroup.getChildren().clear();

        // rebuild grid
        createGrid(gridGroup, (int) gridScale);

        // now rebuild scale legend
        rebuildScaleLegend((int) scaleIncrement);
    }

    private void buildGrid() {
        createGrid(gridGroup, spacing);
    }

    private void createGrid(Group grid, int gridIncrement) {

        gridGroup.setTranslate(-width / 2.0, 0, -depth / 2.0);

        // iterate over z dimension
        int zDivisions = width / gridIncrement;
        double x = 0.0;
        for (int i = 0; i <= zDivisions; i++) {
            Point3D from = new Point3D(x, 0, 0);
            Point3D to = new Point3D(x, 0, depth);
            Cylinder lineSegment = StellarEntityFactory.createLineSegment(from, to, lineWidth, Color.BLUE);
            grid.getChildren().add(lineSegment);
            x += gridIncrement;
        }

        // iterate over x dimension
        int xDivisions = depth / gridIncrement;
        double z = 0.0;
        for (int i = 0; i <= xDivisions; i++) {
            Point3D from = new Point3D(0, 0, z);
            Point3D to = new Point3D(width, 0, z);
            Cylinder lineSegment = StellarEntityFactory.createLineSegment(from, to, lineWidth, Color.BLUE);
            grid.getChildren().add(lineSegment);
            z += gridIncrement;
        }

        grid.setVisible(true);
    }

    /**
     * used to handle rotation of the scene
     *
     * @param pane the subscene to manage rotation
     */
    private void handleMouseEvents(Pane pane) {

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
//            updateLabels();  // used to eventually make the labels flat

            double modifier = 1.0;
            double modifierFactor = 0.1;

            if (me.isControlDown()) {
                modifier = 0.1;
            }
            if (me.isShiftDown()) {
                modifier = 10.0;
            }

            if (me.isPrimaryButtonDown()) {
                cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX * modifierFactor * modifier * 2.0);  // +
                cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY * modifierFactor * modifier * 2.0);  // -
            } else if (me.isSecondaryButtonDown()) {
                log.info("secondary button pushed, x={}, y={}", mousePosX, mousePosY);
            } else if (me.isMiddleButtonDown()) {
                log.info("middle button pushed, x={}, y={}", mousePosX, mousePosY);
            }
        });

    }

    private void updateLabels() {
        List<Node> starGroup = stellarDisplayGroup.getChildren();

        for (Node star : starGroup) {
            Xform xform = (Xform) star;
            List<Node> nodes = xform.getChildren();
            for (Node node : nodes) {
                Group group = (Group) node;
                List<Node> things = group.getChildren();
                Label label;
                Sphere sphere;
                // one or another
                if (things.get(0) instanceof Label) {
                    label = (Label) things.get(0);
                    // leave center star alone
                    if (things.get(1) instanceof Box) {
                        continue;
                    }
                    sphere = (Sphere) things.get(1);
                } else {
                    label = (Label) things.get(1);
                    // leave center star alone
                    if (things.get(0) instanceof Box) {
                        continue;
                    }
                    sphere = (Sphere) things.get(0);
                }
                redrawLabel(group, sphere, label);
            }
//            log.info(star.toString());
        }

    }

    private void redrawLabel(Group group, Sphere sphere, Label label) {
        Point3D point3d = new Point3D(label.getTranslateX(), label.getTranslateY(), label.getTranslateZ());
        Point3D localToScenePoint3d = label.localToScene(point3d, true);

        label.getTransforms().setAll(new Translate(localToScenePoint3d.getX(), localToScenePoint3d.getY()));

        boolean elementsRemoved = group.getChildren().removeIf(Label.class::isInstance);
        group.getChildren().add(label);
    }

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
                        if (gridGroup.isVisible()) {
                            gridGroup.setVisible(false);
                        } else {
                            gridGroup.setVisible(true);
                        }
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
