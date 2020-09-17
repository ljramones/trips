package com.teamgannon.trips.transits;

import com.teamgannon.trips.dialogs.search.model.DistanceRoutes;
import com.teamgannon.trips.graphics.entities.CustomObjectFactory;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.entities.Xform;
import com.teamgannon.trips.service.StarMeasurementService;
import com.teamgannon.trips.service.TransitRoute;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;

import java.util.*;

import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;

@Slf4j
public class TransitManager {

    /**
     * the graphical element controlling transits
     */
    private final Xform transitGroup;

    /**
     * whether the transits are visible or not
     */
    private boolean transitsOn;

    /**
     * lookup for transits
     */
    private Map<String, TransitRoute> transitRouteMap = new HashMap<>();

    /**
     * list of computed transits
     */
    private List<TransitRoute> transitRoutes;

    /**
     * used to track the current rout list
     */
    private List<TransitRoute> currentRouteList = new ArrayList<>();

    ////////////////

    /**
     * constructor
     */
    public TransitManager() {
        transitGroup = new Xform();
        transitGroup.setWhatAmI("Transit Plot Group");
    }

    public Xform getTransitGroup() {
        return transitGroup;
    }

    /**
     * finds all the transits for stars in view
     *
     * @param distanceRoutes the distance range selected
     * @param starsInView    the stars in the current plot
     */
    public void findTransits(DistanceRoutes distanceRoutes, List<StarDisplayRecord> starsInView) {
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
            Node transitSegment = CustomObjectFactory.createLineSegment(transitRoute.getSourceEndpoint(), transitRoute.getTargetEndpoint(), 1, Color.YELLOW);
            transitSegment.setUserData(transitRoute);
            Tooltip tooltip = new Tooltip(hoverText(transitRoute));
            Tooltip.install(transitSegment, tooltip);
            createContextMenu(transitSegment);
            transitGroup.getChildren().add(transitSegment);
        }
        transitGroup.setVisible(true);
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

        MenuItem completeRouteMenuItem = completeToRoute(transitSegment);
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

    private MenuItem completeToRoute(Node transitSegment) {
        MenuItem menuItem = new MenuItem("Complete Route");
        menuItem.setOnAction(event -> {
            TransitRoute transitRoute = (TransitRoute) transitSegment.getUserData();
            currentRouteList.add(transitRoute);
            constructRoute();
            log.info("complete route");
        });
        return menuItem;
    }

    private void constructRoute() {
        log.info("validate the ");
    }

    private MenuItem addToRoute(Node transitSegment) {
        MenuItem menuItem = new MenuItem("Add To Route");
        menuItem.setOnAction(event -> {
            TransitRoute transitRoute = (TransitRoute) transitSegment.getUserData();
            currentRouteList.add(transitRoute);
            log.info("add to route");
        });
        return menuItem;
    }

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
                }
            } else {
                createRoute(transitSegment);
            }
        });
        return menuItem;
    }

    private void createRoute(Node transitSegment) {
        TransitRoute transitRoute = (TransitRoute) transitSegment.getUserData();
        currentRouteList.clear();
        currentRouteList.add(transitRoute);
        log.info("new route");
    }

    public void setVisible(boolean transitsOn) {
        this.transitsOn = transitsOn;
        transitGroup.setVisible(transitsOn);
    }

    public boolean isVisible() {
        return transitsOn;
    }

    private String hoverText(TransitRoute transitRoute) {
        return "transit: "
                + transitRoute.getSource().getStarName() + " <--> "
                + transitRoute.getTarget().getStarName() + "is "
                + String.format("%.2f", transitRoute.getDistance()) + "ly";
    }

}
