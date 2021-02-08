package com.teamgannon.trips.transits;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.SerialFont;
import com.teamgannon.trips.dialogs.routing.RouteDialog;
import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.entities.StellarEntityFactory;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.RouteUpdaterListener;
import com.teamgannon.trips.service.StarMeasurementService;
import com.teamgannon.trips.service.model.TransitRoute;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;
import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class TransitManager {


    /**
     * lookup for transits
     */
    private final Map<String, TransitRoute> transitRouteMap = new HashMap<>();

    /**
     * used to track the current rout list
     */
    private final List<TransitRoute> currentRouteList = new ArrayList<>();

    /**
     * the graphical element controlling transits
     */
    private final @NotNull Group transitGroup;

    /**
     * the label display
     */
    private final Group labelDisplayGroup = new Group();

    private final Map<Node, Label> shapeToLabel = new HashMap<>();

    private final SubScene subScene;

    /**
     * the listener to create routes on demand
     */
    private final RouteUpdaterListener routeUpdaterListener;
    private final TripsContext tripsContext;
    private final InterstellarSpacePane interstellarSpacePane;

    /**
     * whether the transits are visible or not
     */
    private boolean transitsOn;

    /**
     * list of computed transits
     */
    private List<TransitRoute> transitRoutes;

    /**
     * the route descriptor
     */
    private @Nullable RouteDescriptor routeDescriptor;

    /**
     * used to track an active routing effort
     */
    private boolean routingActive = false;

    /**
     * current dataset
     */
    private DataSetDescriptor dataSetDescriptor;

    private boolean transitsLengthsOn = true;
    private double controlPaneOffset;


    ////////////////

    /**
     * constructor
     */
    public TransitManager(@NotNull Group world,
                          @NotNull Group sceneRoot,
                          SubScene subScene,
                          InterstellarSpacePane interstellarSpacePane,
                          RouteUpdaterListener routeUpdaterListener,
                          TripsContext tripsContext) {
        this.subScene = subScene;
        this.interstellarSpacePane = interstellarSpacePane;

        // our graphics world
        this.routeUpdaterListener = routeUpdaterListener;
        this.tripsContext = tripsContext;
        transitGroup = new Group();
        world.getChildren().add(transitGroup);
        sceneRoot.getChildren().add(labelDisplayGroup);

    }


    public boolean isVisible() {
        return transitsOn;
    }

    public void setVisible(boolean transitsOn) {
        this.transitsOn = transitsOn;
        this.transitsLengthsOn = transitsOn;
        transitGroup.setVisible(transitsOn);
        labelDisplayGroup.setVisible(transitsLengthsOn);
    }

    public void setDatasetContext(DataSetDescriptor dataSetDescriptor) {
        this.dataSetDescriptor = dataSetDescriptor;
    }

    public void clearTransits() {
        transitGroup.getChildren().clear();
        transitRouteMap.clear();
        if (transitRoutes != null) {
            transitRoutes.clear();
            transitsOn = false;
        }
        shapeToLabel.clear();

        labelDisplayGroup.getChildren().clear();
        transitsLengthsOn = false;

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
        labelDisplayGroup.setVisible(transitsLengthsOn);
    }

    /**
     * finds all the transits for stars in view
     *
     * @param distanceRoutes the distance range selected
     * @param starsInView    the stars in the current plot
     */
    public void findTransits(@NotNull DistanceRoutes distanceRoutes, @NotNull List<StarDisplayRecord> starsInView) {
        // clear existing
        clearTransits();

        // set
        transitsLengthsOn = true;
        transitsOn = true;

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

    private void plotTransitRoutes(@NotNull List<TransitRoute> transitRoutes) {
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
            transitGroup.getChildren().add(transitSegment);
        }
        updateLabels(interstellarSpacePane);
        transitGroup.setVisible(true);
    }

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

        if (transitsLengthsOn) {
            // attach label
            Sphere pointSphere = createPointSphere(lengthLabel);
            pointSphere.setTranslateX(mid.getX());
            pointSphere.setTranslateY(mid.getY());
            pointSphere.setTranslateZ(mid.getZ());
            lengthLabel.setTextFill(color);
            lineGroup.getChildren().add(pointSphere);
            if (!shapeToLabel.containsValue(lengthLabel)) {
                shapeToLabel.put(pointSphere, lengthLabel);
                labelDisplayGroup.getChildren().add(lengthLabel);
            } else {
                log.warn("what is <{}> present twice", lengthLabel.getText());
            }
        }

        return lineGroup;
    }

    private @NotNull Sphere createPointSphere(@NotNull Label label) {
        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.WHEAT);
        material.setSpecularColor(Color.WHEAT);
        Sphere sphere = new Sphere(1);
        sphere.setMaterial(material);
        label.setLabelFor(sphere);
        labelDisplayGroup.getChildren().add(label);
        shapeToLabel.put(sphere, label);
        return sphere;
    }

    private @NotNull Label createLabel(@NotNull TransitRoute transitRoute) {
        Label label = new Label(String.format("%.2fly", transitRoute.getDistance()));
        SerialFont serialFont = tripsContext.getCurrentPlot().getColorPalette().getLabelFont();
        label.setFont(serialFont.toFont());
        return label;
    }

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
            transitGroup.getChildren().remove(transitSegment);
            removeTransit(transitRoute);
            log.info("remove");
        });
        return menuItem;
    }


    ///////////////////////  ROUTING  ////////////////////

    private void removeTransit(@NotNull TransitRoute transitRoute) {
        transitRouteMap.remove(transitRoute.getName());
        transitRoutes = new ArrayList<>(transitRouteMap.values());
        MapUtils.populateMap(transitRouteMap,
                transitRoutes,
                TransitRoute::getName);
    }

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
                    routeUpdaterListener.routingStatus(true);
                }
            } else {
                createRoute(transitSegment);
                routeUpdaterListener.routingStatus(true);
            }
        });
        return menuItem;
    }

    private void createRoute(@NotNull Node transitSegment) {
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

    private @NotNull MenuItem addToRoute(@NotNull Node transitSegment) {
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

    private @NotNull MenuItem completeTheRoute(@NotNull Node transitSegment) {
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
        }
        routeUpdaterListener.newRoute(dataSetDescriptor, routeDescriptor);
    }

    private @NotNull String hoverText(@NotNull TransitRoute transitRoute) {
        return "transit: "
                + transitRoute.getSource().getStarName() + " <--> "
                + transitRoute.getTarget().getStarName() + "is "
                + String.format("%.2f", transitRoute.getDistance()) + "ly";
    }

    public void updateLabels(@NotNull InterstellarSpacePane interstellarSpacePane) {

        double width = interstellarSpacePane.getWidth();
        double height = interstellarSpacePane.getHeight();
        Bounds ofParent = interstellarSpacePane.getBoundsInParent();

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

    private boolean clip(Point3D coordinates, double width, double height) {
//        return abs(coordinates.getX()) < width/2 && abs(coordinates.getY()) < height/2;
        return false;
    }

    public void setControlPaneOffset(double controlPaneOffset) {
        this.controlPaneOffset = controlPaneOffset;
    }
}
