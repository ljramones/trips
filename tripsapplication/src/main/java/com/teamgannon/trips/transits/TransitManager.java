package com.teamgannon.trips.transits;

import com.teamgannon.trips.dialogs.routing.RouteDialog;
import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.entities.StellarEntityFactory;
import com.teamgannon.trips.graphics.entities.Xform;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.RouteUpdaterListener;
import com.teamgannon.trips.service.StarMeasurementService;
import com.teamgannon.trips.service.TransitRoute;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Cylinder;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

import java.util.*;

import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;
import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class TransitManager {

    /**
     * whether the transits are visible or not
     */
    private boolean transitsOn;

    /**
     * lookup for transits
     */
    private final Map<String, TransitRoute> transitRouteMap = new HashMap<>();

    /**
     * list of computed transits
     */
    private List<TransitRoute> transitRoutes;

    /**
     * used to track the current rout list
     */
    private final List<TransitRoute> currentRouteList = new ArrayList<>();

    /**
     * the route descriptor
     */
    private RouteDescriptor routeDescriptor;

    /**
     * used to track an active routing effort
     */
    private boolean routingActive = false;

    /**
     * our graphics world
     */
    private final Xform world;

    /**
     * the graphical element controlling transits
     */
    private final Xform transitGroup;

    /**
     * to track the labels for the transits
     */
    private final Xform transitLabelsGroup = new Xform();

    /**
     * the listener to create routes on demand
     */
    private final RouteUpdaterListener routeUpdaterListener;

    /**
     * current dataset
     */
    private DataSetDescriptor dataSetDescriptor;

    private boolean transitsLengthsOn = true;


    ////////////////

    /**
     * constructor
     */
    public TransitManager(Xform world, RouteUpdaterListener routeUpdaterListener) {
        this.world = world;
        this.routeUpdaterListener = routeUpdaterListener;
        transitGroup = new Xform();
        transitGroup.setWhatAmI("Transit Plot Group");

        transitLabelsGroup.setWhatAmI("Labels for Transit Plots");
        world.getChildren().add(transitLabelsGroup);
    }


    public boolean isVisible() {
        return transitsOn;
    }

    public Xform getTransitGroup() {
        return transitGroup;
    }

    public void setDatasetContext(DataSetDescriptor dataSetDescriptor) {
        this.dataSetDescriptor = dataSetDescriptor;
    }

    public void clearTransits() {
        transitGroup.getChildren().clear();
        transitRouteMap.clear();
        if (transitRoutes != null) {
            transitRoutes.clear();
        }
        routeDescriptor = null;
        currentRouteList.clear();
        routingActive = false;
    }

    /**
     * toggle transit lengths
     *
     * @param transitsLengthsOn toggle the transit lengths
     */
    public void toggleTransitLengths(boolean transitsLengthsOn) {
        this.transitsLengthsOn = transitsLengthsOn;
        log.info("transit labels visibility:{}", transitsLengthsOn);
        transitLabelsGroup.setVisible(transitsLengthsOn);
    }

    /**
     * finds all the transits for stars in view
     *
     * @param distanceRoutes the distance range selected
     * @param starsInView    the stars in the current plot
     */
    public void findTransits(DistanceRoutes distanceRoutes, List<StarDisplayRecord> starsInView) {
        // clear existing
        clearTransits();

        // now draw new
        log.info("Distance between stars is:" + distanceRoutes.getUpperDistance());
        StarMeasurementService starMeasurementService = new StarMeasurementService();
        transitRoutes = starMeasurementService.calculateDistances(distanceRoutes, starsInView);
        MapUtils.populateMap(transitRouteMap,
                transitRoutes,
                TransitRoute::getName);
        plotTransitRoutes(transitRoutes);

        log.info("done calcs");
    }

    private void plotTransitRoutes(List<TransitRoute> transitRoutes) {
        for (TransitRoute transitRoute : transitRoutes) {
            log.info("transit: {}", transitRoute);
            Label lengthLabel = createLabel(transitRoute);

            Node transitSegment = createLineSegment(
                    transitRoute.getSourceEndpoint(),
                    transitRoute.getTargetEndpoint(),
                    transitRoute.getLineWeight(),
                    transitRoute.getColor(),
                    transitsLengthsOn,
                    lengthLabel);
            transitSegment.setUserData(transitRoute);
            Tooltip tooltip = new Tooltip(hoverText(transitRoute));
            Tooltip.install(transitSegment, tooltip);
            createContextMenu(transitSegment);
            transitGroup.getChildren().add(transitSegment);
        }
        transitGroup.setVisible(true);
    }

    private Node createLineSegment(Point3D origin, Point3D target, double lineWeight, Color color, boolean labelsOn, Label lengthLabel) {
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

        Xform lineGroup = new Xform();

        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);
        lineGroup.getChildren().add(line);

        if (labelsOn) {
            // attach label
            lengthLabel.setTranslateX(mid.getX());
            lengthLabel.setTranslateY(mid.getY());
            lengthLabel.setTranslateZ(mid.getZ());
            lengthLabel.setTextFill(color);
            transitLabelsGroup.getChildren().add(lengthLabel);
        }

        return lineGroup;
    }

    private Label createLabel(TransitRoute transitRoute) {
        Label label = new Label(String.format("%.2fly", transitRoute.getDistance()));
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 6);
        label.setFont(font);
        return label;
    }

    private void createContextMenu(Node transitSegment) {
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

    private void transitClickEventHandler(Node transitSegment, ContextMenu transitContextMenu, MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            log.info("Primary button pressed");
            transitContextMenu.show(transitSegment, e.getScreenX(), e.getScreenY());
        } else {
            log.info("not primary button pressed");
        }
    }

    private ContextMenu createPopup(String hoverText, Node transitSegment) {
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

    private MenuItem removeTransit(Node transitSegment) {
        MenuItem menuItem = new MenuItem("Remove");
        menuItem.setOnAction(event -> {
            TransitRoute transitRoute = (TransitRoute) transitSegment.getUserData();
            transitGroup.getChildren().remove(transitSegment);
            removeTransit(transitRoute);
            log.info("remove");
        });
        return menuItem;
    }

    private void removeTransit(TransitRoute transitRoute) {
        transitRouteMap.remove(transitRoute.getName());
        transitRoutes = new ArrayList<>(transitRouteMap.values());
        MapUtils.populateMap(transitRouteMap,
                transitRoutes,
                TransitRoute::getName);
    }


    ///////////////////////  ROUTING  ////////////////////

    private MenuItem createNewRoute(Node transitSegment) {
        MenuItem menuItem = new MenuItem("Create New Route");
        menuItem.setOnAction(event -> {
            if (currentRouteList.size() > 0) {
                Optional<ButtonType> buttonType = showConfirmationAlert("Remove Dataset",
                        "Restart Route?",
                        "You have a route in progress, Ok will clear current?");

                if ((buttonType.isPresent()) && (buttonType.get() == ButtonType.OK)) {
                    currentRouteList.clear();
                    createRoute(transitSegment);
                    routeUpdaterListener.routingStatus(true);
                }
            } else {
                createRoute(transitSegment);
                routeUpdaterListener.routingStatus(true);
            }
        });
        return menuItem;
    }


    private void createRoute(Node transitSegment) {
        TransitRoute transitRoute = (TransitRoute) transitSegment.getUserData();
        RouteDialog dialog = new RouteDialog(transitRoute.getSource());
        Optional<RouteDescriptor> result = dialog.showAndWait();
        if (result.isPresent()) {
            currentRouteList.clear();
            routingActive = true;
            routeDescriptor = result.get();
            routeDescriptor.setStartStar(transitRoute.getSource().getStarName());
            routeDescriptor.getRouteList().add(transitRoute.getSource().getRecordId());
            currentRouteList.add(transitRoute);
            log.info("new route");
        }
    }

    private MenuItem addToRoute(Node transitSegment) {
        MenuItem menuItem = new MenuItem("Add To Route");
        menuItem.setOnAction(event -> {
            if (routingActive) {
                TransitRoute transitRoute = (TransitRoute) transitSegment.getUserData();
                currentRouteList.add(transitRoute);
                log.info("add to route");
            } else {
                showErrorAlert("Transit Routing", "start a route first");
            }
        });
        return menuItem;
    }


    private MenuItem completeTheRoute(Node transitSegment) {
        MenuItem menuItem = new MenuItem("Complete Route");
        menuItem.setOnAction(event -> {
            if (routingActive) {
                TransitRoute transitRoute = (TransitRoute) transitSegment.getUserData();
                currentRouteList.add(transitRoute);
                constructRoute();
                log.info("complete route");
            } else {
                showErrorAlert("Transit Routing", "start a route first");
            }
        });
        return menuItem;
    }


    private void constructRoute() {
        log.info("validate the ");
        for (TransitRoute transitRoute : currentRouteList) {
            routeDescriptor.getLineSegments().add(transitRoute.getTargetEndpoint());
            routeDescriptor.getRouteList().add(transitRoute.getTarget().getRecordId());
            routeUpdaterListener.newRoute(dataSetDescriptor, routeDescriptor);
        }
    }


    public void setVisible(boolean transitsOn) {
        this.transitsOn = transitsOn;
        this.transitsLengthsOn = transitsOn;
        transitGroup.setVisible(transitsOn);
        transitLabelsGroup.setVisible(transitsLengthsOn);
    }

    private String hoverText(TransitRoute transitRoute) {
        return "transit: "
                + transitRoute.getSource().getStarName() + " <--> "
                + transitRoute.getTarget().getStarName() + "is "
                + String.format("%.2f", transitRoute.getDistance()) + "ly";
    }

}
