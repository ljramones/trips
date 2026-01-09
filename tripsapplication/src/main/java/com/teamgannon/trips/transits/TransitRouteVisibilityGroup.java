package com.teamgannon.trips.transits;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.SerialFont;
import com.teamgannon.trips.dialogs.routing.RouteDialog;
import com.teamgannon.trips.dialogs.routing.RouteSelector;
import com.teamgannon.trips.events.NewRouteEvent;
import com.teamgannon.trips.events.RoutingStatusEvent;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.entities.StellarEntityFactory;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.service.measure.StarMeasurementService;
import org.springframework.context.ApplicationEventPublisher;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;
import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
@Data
public class TransitRouteVisibilityGroup {

    /**
     * TRIPS application context
     */
    private TripsContext tripsContext;

    /**
     * star measurement service
     */
    private final StarMeasurementService starMeasurementService;

    /**
     * reference to the interstellar pane
     */
    private InterstellarSpacePane interstellarSpacePane;

    /**
     * whether this is currently visible
     * just a marker since it doesn't actually actual set the group visibility
     */
    private boolean visible;

    /**
     * the labels can be visible or not if the transit group is visible
     * but if the visible flag is set to false (no show) then this is also to be not shown
     */
    private boolean labelsVisible;

    /**
     * the containing group for the thransit line segments contained here
     */
    private Group group;

    /**
     * the label group for any labels defined here
     */
    private Group labelGroup;

    /**
     * the name of the group
     */
    private String groupName;

    /**
     * used to act as an index
     */
    private UUID groupId;

    /**
     * lookup for transits
     */
    private final Map<String, TransitRoute> transitRouteMap = new HashMap<>();

    /**
     * transit links
     */
    private List<TransitRoute> transitRouteList = new ArrayList<>();

    /**
     * used to map a given label to a point sphere
     */
    private final Map<Node, Label> shapeToLabel = new HashMap<>();

    /**
     * used to make label appear always flat to the glass pane
     */
    private SubScene subScene;


    /**
     * the transit range ref used to describe any of this
     */
    private TransitRangeDef transitRangeDef;


    /**
     * the route descriptor
     */
    private @Nullable RouteDescriptor routeDescriptor;

    /**
     * used to track an active routing effort
     */
    private boolean routingActive = false;

    /**
     * used to track the current rout list
     */
    private final List<TransitRoute> currentRouteList = new ArrayList<>();

    private final ApplicationEventPublisher eventPublisher;

    /**
     * current dataset
     */
    private DataSetDescriptor dataSetDescriptor;

    private double controlPaneOffset;


    //////////////////////

    /**
     * the constructor
     *
     * @param transitRangeDef the transit group definition
     */
    public TransitRouteVisibilityGroup(SubScene subScene,
                                       InterstellarSpacePane interstellarSpacePane,
                                       StarMeasurementService starMeasurementService,
                                       double controlPaneOffset,
                                       TransitRangeDef transitRangeDef,
                                       ApplicationEventPublisher eventPublisher,
                                       TripsContext tripsContext
    ) {
        this.subScene = subScene;
        this.interstellarSpacePane = interstellarSpacePane;
        this.starMeasurementService = starMeasurementService;
        this.controlPaneOffset = controlPaneOffset;
        this.transitRangeDef = transitRangeDef;
        this.eventPublisher = eventPublisher;
        this.tripsContext = tripsContext;
        visible = false;
        labelsVisible = false;
        group = new Group();
        labelGroup = new Group();
        groupName = transitRangeDef.getBandName();
        groupId = transitRangeDef.getBandId();
    }

    public void toggleTransit(boolean show) {
        group.setVisible(show);
        labelGroup.setVisible(show);
        visible = true;
    }

    public void toggleLabels(boolean show) {
        if (visible) {
            labelGroup.setVisible(show);
        }
    }

    public void clear() {
        labelGroup.getChildren().clear();
        group.getChildren().clear();
        shapeToLabel.clear();
        visible = false;
    }


    /////////////////////////////////////////

    public void plotTransit(TransitRangeDef transitRangeDef, List<StarDisplayRecord> starsInView) {
        List<TransitRoute> transitRoutes = starMeasurementService.calculateDistances(transitRangeDef, starsInView);
        log.info("# of routes found is {}", transitRoutes.size());

        MapUtils.populateMap(transitRouteMap,
                transitRoutes,
                TransitRoute::getName);

        plotTransitRoutes(transitRoutes);
    }

    /**
     * plot the transit routes
     *
     * @param transitRoutes the transit routes
     */
    private void plotTransitRoutes(@NotNull List<TransitRoute> transitRoutes) {
        visible = true;
        labelsVisible = true;

        for (TransitRoute transitRoute : transitRoutes) {
            log.info("transit: {}", transitRoute);
            Label lengthLabel = createLabel(transitRoute);

            Node transitSegment = createLineSegment(
                    transitRoute.getSourceEndpoint(),
                    transitRoute.getTargetEndpoint(),
                    transitRoute.getLineWeight(),
                    transitRoute.getColor(),
                    lengthLabel);
            transitSegment.setUserData(transitRoute);
            Tooltip tooltip = new Tooltip(hoverText(transitRoute));
            Tooltip.install(transitSegment, tooltip);
            createContextMenu(transitSegment);
            group.getChildren().add(transitSegment);
        }
        updateLabels(subScene, controlPaneOffset, interstellarSpacePane.getWidth(),
                interstellarSpacePane.getHeight(), interstellarSpacePane.getBoundsInParent());

        group.setVisible(visible);
        labelGroup.setVisible(visible);
    }


    private @NotNull String hoverText(@NotNull TransitRoute transitRoute) {
        return "transit: "
                + transitRoute.getSource().getStarName() + " <--> "
                + transitRoute.getTarget().getStarName() + "is "
                + String.format("%.2f", transitRoute.getDistance()) + " ";
    }


    /**
     * create a line segment
     *
     * @param origin      the start point
     * @param target      the finish point
     * @param lineWeight  the line width
     * @param color       the line color
     * @param lengthLabel the length label
     * @return the 3-d line
     */
    private @NotNull Node createLineSegment(Point3D origin, @NotNull Point3D target, double lineWeight, Color color, @NotNull Label lengthLabel) {

        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D diff = target.subtract(origin);
        double height = diff.magnitude();

        Point3D mid = target.midpoint(origin);
        Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

        Point3D axisOfRotation = diff.crossProduct(yAxis);
        double angle = Math.acos(diff.normalize().dotProduct(yAxis));
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);

        // create cylinder and color it with phong material
        Cylinder line = StellarEntityFactory.createCylinder(lineWeight, color, height);

        Group lineGroup = new Group();

        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);
        lineGroup.getChildren().add(line);

        if (labelsVisible) {
            // attach label
            Sphere pointSphere = createPointSphere(lengthLabel);
            pointSphere.setTranslateX(mid.getX());
            pointSphere.setTranslateY(mid.getY());
            pointSphere.setTranslateZ(mid.getZ());
            lengthLabel.setTextFill(color);
            lineGroup.getChildren().add(pointSphere);
            if (!shapeToLabel.containsValue(lengthLabel)) {
                shapeToLabel.put(pointSphere, lengthLabel);
                labelGroup.getChildren().add(lengthLabel);
            } else {
                log.warn("what is <{}> present twice", lengthLabel.getText());
            }
        }

        return lineGroup;
    }

    /**
     * create an attachment point for the label
     *
     * @param label the label to add
     * @return the attachment point
     */
    private @NotNull Sphere createPointSphere(@NotNull Label label) {
        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.WHEAT);
        material.setSpecularColor(Color.WHEAT);
        Sphere sphere = new Sphere(1);
        sphere.setMaterial(material);
        label.setLabelFor(sphere);
        labelGroup.getChildren().add(label);
        shapeToLabel.put(sphere, label);
        return sphere;
    }

    /**
     * create a label
     *
     * @param transitRoute the transit route
     * @return the label
     */
    private @NotNull Label createLabel(@NotNull TransitRoute transitRoute) {
        Label label = new Label(String.format(" %.2f ", transitRoute.getDistance()));
        SerialFont serialFont = tripsContext.getCurrentPlot().getColorPalette().getLabelFont();
        label.setFont(serialFont.toFont());
        return label;
    }

    ///////////////

    /**
     * create a popup context menu
     *
     * @param transitSegment the line segment to point and attach the menu to
     */
    private void createContextMenu(@NotNull Node transitSegment) {
        TransitRoute route = (TransitRoute) transitSegment.getUserData();
        ContextMenu transitContextMenu = createPopup(hoverText(route), transitSegment);

        transitSegment.addEventHandler(
                MouseEvent.MOUSE_CLICKED,
                e -> transitClickEventHandler(transitSegment, transitContextMenu, e));

        transitSegment.setOnMousePressed(event -> {
            Node node = (Node) event.getSource();
            TransitRoute transitRoute = (TransitRoute) node.getUserData();
            log.info("mouse click detected! " + transitRoute);
        });

    }


    /**
     * the event handler if you click the line sgement
     *
     * @param transitSegment     the line segment
     * @param transitContextMenu the context menu
     * @param e                  the mouse event
     */
    private void transitClickEventHandler(Node transitSegment, @NotNull ContextMenu transitContextMenu, @NotNull MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            log.info("Primary button pressed");
            transitContextMenu.show(transitSegment, e.getScreenX(), e.getScreenY());
        } else {
            log.info("not primary button pressed");
        }
    }

    private @NotNull ContextMenu createPopup(String hoverText, @NotNull Node transitSegment) {
        final ContextMenu cm = new ContextMenu();

        MenuItem titleItem = new MenuItem(hoverText);
        titleItem.setDisable(true);
        cm.getItems().add(titleItem);

        MenuItem createRouteMenuItem = createNewRoute(transitSegment);
        cm.getItems().add(createRouteMenuItem);

        MenuItem addRouteMenuItem = addToRoute(transitSegment);
        cm.getItems().add(addRouteMenuItem);

        MenuItem completeRouteMenuItem = completeTheRoute(transitSegment);
        cm.getItems().add(completeRouteMenuItem);

        cm.getItems().add(new SeparatorMenuItem());

        MenuItem removeTransitMenuItem = removeTransit(transitSegment);
        cm.getItems().add(removeTransitMenuItem);

        return cm;
    }

    private @NotNull MenuItem removeTransit(@NotNull Node transitSegment) {
        MenuItem menuItem = new MenuItem("Remove");
        menuItem.setOnAction(event -> {
            TransitRoute transitRoute = (TransitRoute) transitSegment.getUserData();
            group.getChildren().remove(transitSegment);
            removeTransit(transitRoute);
            log.info("remove");
        });
        return menuItem;
    }

    ///////////////////////  ROUTING  ////////////////////

    /**
     * remove a transit segment
     *
     * @param transitRoute the transit segment
     */
    private void removeTransit(@NotNull TransitRoute transitRoute) {
        transitRouteMap.remove(transitRoute.getName());
        transitRouteList = new ArrayList<>(transitRouteMap.values());
        MapUtils.populateMap(transitRouteMap,
                transitRouteList,
                TransitRoute::getName);
    }

    /**
     * create a menutitem for create new route
     *
     * @param transitSegment the line segment
     * @return the menu item
     */
    private @NotNull MenuItem createNewRoute(@NotNull Node transitSegment) {
        MenuItem menuItem = new MenuItem("Create New Route");
        menuItem.setOnAction(event -> {
            if (currentRouteList.size() > 0) {
                Optional<ButtonType> buttonType = showConfirmationAlert("Remove Dataset",
                        "Restart Route?",
                        "You have a route in progress, Ok will clear current?");

                if ((buttonType.isPresent()) && (buttonType.get() == ButtonType.OK)) {
                    currentRouteList.clear();
                    createRoute(transitSegment);
                    eventPublisher.publishEvent(new RoutingStatusEvent(this, true));
                }
            } else {
                createRoute(transitSegment);
                eventPublisher.publishEvent(new RoutingStatusEvent(this, true));
            }
        });
        return menuItem;
    }


    private void createRoute(@NotNull Node transitSegment) {
        TransitRoute transitRoute = (TransitRoute) transitSegment.getUserData();
        RouteDialog dialog = new RouteDialog(transitRoute.getSource());
        Optional<RouteSelector> resultOptional = dialog.showAndWait();
        if (resultOptional.isPresent()) {
            RouteSelector routeSelector = resultOptional.get();
            if (routeSelector.isSelected()) {
                currentRouteList.clear();
                routingActive = true;
                routeDescriptor = routeSelector.getRouteDescriptor();
                routeDescriptor.setStartStar(transitRoute.getSource().getStarName());
                routeDescriptor.getRouteList().add(transitRoute.getSource().getRecordId());
                currentRouteList.add(transitRoute);
                log.info("new route");
            }
        }
    }

    private @NotNull MenuItem addToRoute(@NotNull Node transitSegment) {
        MenuItem menuItem = new MenuItem("Add To Route");
        menuItem.setOnAction(event -> {
            if (routingActive) {
                TransitRoute transitRoute = (TransitRoute) transitSegment.getUserData();
                currentRouteList.add(transitRoute);
                log.info("add to route");
            } else {
                showErrorAlert("Link Routing", "start a route first");
            }
        });
        return menuItem;
    }

    private @NotNull MenuItem completeTheRoute(@NotNull Node transitSegment) {
        MenuItem menuItem = new MenuItem("Complete Route");
        menuItem.setOnAction(event -> {
            if (routingActive) {
                TransitRoute transitRoute = (TransitRoute) transitSegment.getUserData();
                currentRouteList.add(transitRoute);
                constructRoute();
                log.info("complete route");
            } else {
                showErrorAlert("Link Routing", "start a route first");
            }
        });
        return menuItem;
    }

    private void constructRoute() {
        log.info("validate the ");
        for (TransitRoute transitRoute : currentRouteList) {
            routeDescriptor.getRouteCoordinates().add(transitRoute.getTargetEndpoint());
            routeDescriptor.getRouteList().add(transitRoute.getTarget().getRecordId());
        }
        eventPublisher.publishEvent(new NewRouteEvent(this, dataSetDescriptor, routeDescriptor));
    }

    /////////////////////////////////////////

    /**
     * update the labels for this transit group
     *
     * @param subScene          the embedded subscene
     * @param controlPaneOffset the control panel offset
     * @param width             the width
     * @param height            the height
     * @param ofParent          the parent bounds
     */
    public void updateLabels(SubScene subScene, double controlPaneOffset, double width, double height, Bounds ofParent) {
        if (visible) {
            for (Map.Entry<Node, Label> entry : shapeToLabel.entrySet()) {
                Node node = entry.getKey();
                Label label = entry.getValue();
                Point3D coordinates = node.localToScene(Point3D.ZERO, true);

                // we need to check if the coordinates work within the displayable area
                if (!clip(coordinates, width, height)) {

                    //Clipping Logic
                    //if coordinates are outside of the scene it could
                    //stretch the screen so don't transform them
                    double xs = coordinates.getX();
                    double ys = coordinates.getY();

                    // configure visibility
                    if (xs < (ofParent.getMinX() + 20) || xs > (ofParent.getMaxX() - 20)) {
                        label.setVisible(false);
                        continue;
                    } else {
                        label.setVisible(true);
                    }
                    if (ys < (controlPaneOffset + 20) || (ys > ofParent.getMaxY() - 20)) {
                        label.setVisible(false);
                        continue;
                    } else {
                        label.setVisible(true);
                    }

                    ///////////////////

                    double x;
                    double y;

                    if (ofParent.getMinX() > 0) {
                        x = xs - ofParent.getMinX();
                    } else {
                        x = xs;
                    }
                    if (ofParent.getMinY() >= 0) {
                        y = ys - ofParent.getMinY() - controlPaneOffset;
                    } else {
                        y = ys < 0 ? ys - controlPaneOffset : ys + controlPaneOffset;
                    }

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
                } else {
                    log.info("label:{} are {},{} is outside area", label, coordinates.getX(), coordinates.getY());
                }
            }
        }
    }

    /**
     * the clipping function
     *
     * @param coordinates the coordinates
     * @param width       the screen width
     * @param height      the sccreen height
     * @return true is clip, false is keep
     */
    private boolean clip(Point3D coordinates, double width, double height) {
//        return abs(coordinates.getX()) < width/2 && abs(coordinates.getY()) < height/2;
        return false;
    }

}
