package com.teamgannon.trips.routing.routemanagement;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.transform.Translate;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Data
public class RouteDisplay {

    /**
     * the subscene for managing the display plane
     */
    private final SubScene subScene;
    private InterstellarSpacePane interstellarSpacePane;

    /**
     * a map between shapes and labels
     */
    private final Map<Node, Label> shapeToLabel = new HashMap<>();

    /**
     * the reverse map for above (label to shape)
     */
    private final Map<Label, Node> reverseLabelLookup = new HashMap<>();

    /**
     * the label group - used to manage all labels at once (turn on and off or delete)
     */
    private final Group labelDisplayGroup = new Group();

    /**
     * use this to map individual routes to their id
     */
    private final Map<UUID, Group> routeLookup = new HashMap<>();

    /**
     * the total set of all routes and is used to manage them as a group
     */
    private final Group routesGroup = new Group();

    /**
     * by default the labels are on
     */
    private boolean routeLabelsOn = true;

    /**
     * reference to the current TRIPS context
     */
    private final TripsContext tripsContext;

    /**
     * to deal with the offset based on the control panel
     */
    private double controlPaneOffset = 0;

    /**
     * this marks whether we are in manual routing mode.
     */
    private boolean manualRoutingActive = false;


    /**
     * constructor
     *
     * @param tripsContext the context
     */
    public RouteDisplay(TripsContext tripsContext,
                        SubScene subScene,
                        InterstellarSpacePane interstellarSpacePane) {
        this.tripsContext = tripsContext;
        this.subScene = subScene;
        this.interstellarSpacePane = interstellarSpacePane;
    }

    public ColorPalette getColorPallete() {
        return tripsContext.getAppViewPreferences().getColorPallete();
    }


    public void setControlPaneOffset(double controlPaneOffset) {
        this.controlPaneOffset = controlPaneOffset;
    }

    /**
     * clear the routes
     */
    public void clear() {
        shapeToLabel.clear();
        reverseLabelLookup.clear();
        labelDisplayGroup.clipProperty();
        routeLabelsOn = false;
        routeLookup.clear();
        routesGroup.getChildren().clear();
    }


    /**
     * change the state of a displayed route
     *
     * @param routeDescriptor the route
     * @param state           the state
     */
    public void changeDisplayStateOfRoute(RouteDescriptor routeDescriptor, boolean state) {
        log.info("Change state of route {} to {}", routeDescriptor.getName(), state);
        Group route = getRoute(routeDescriptor.getId());
        if (route != null) {
            route.setVisible(state);
        } else {
            log.error("requested route is null:{}", routeDescriptor);
        }
    }

    public void toggleRouteVisibility(boolean toggleState) {
        routesGroup.setVisible(toggleState);
    }

    public void toggleRoutes(boolean routesFlag) {
        routesGroup.setVisible(routesFlag);
        labelDisplayGroup.setVisible(routesFlag);
    }

    public void toggleRouteLengths(boolean routesLengthsFlag) {
        labelDisplayGroup.setVisible(routesLengthsFlag);
    }

    public boolean isLabelPresent(Label lengthLabel) {
        return shapeToLabel.containsValue(lengthLabel);
    }

    public void removeObject(Node object) {
        Label label = shapeToLabel.get(object);
        if (label != null) {
            reverseLabelLookup.remove(label);
            labelDisplayGroup.getChildren().remove(label);
            shapeToLabel.remove(object);
        }
    }

    public void linkObjectToLabel(Node object, Label label) {
        labelDisplayGroup.getChildren().add(label);
        shapeToLabel.put(object, label);
        reverseLabelLookup.put(label, object);
    }

    public void removeLabel(Label label) {
        Node node = reverseLabelLookup.get(label);
        shapeToLabel.remove(node);
        reverseLabelLookup.remove(label);
        labelDisplayGroup.getChildren().remove(label);
    }

    ///////////////

    public Group getRoute(UUID id) {
        return routeLookup.get(id);
    }

    public void removeRouteId(UUID id) {
        routeLookup.remove(id);
    }

    public void addRouteToDisplay(RouteDescriptor routeDescriptor, Group routeToAdd) {
        if (routeDescriptor != null) {
            routeLookup.put(routeDescriptor.getId(), routeToAdd);
            routesGroup.getChildren().add(routeToAdd);
        }
    }

    public void removeRouteFromDisplay(Group routeToRemove) {
        routesGroup.getChildren().remove(routeToRemove);
    }


    /////////////////////////////////////

    /**
     * update all labels to match current screen position
     */
    public void updateLabels() {
        final Bounds ofParent = interstellarSpacePane.getBoundsInParent();

        for (Map.Entry<Node, Label> entry : shapeToLabel.entrySet()) {
            Node node = entry.getKey();
            Label label = entry.getValue();
            Point3D coordinates = node.localToScene(Point3D.ZERO, true);

            /*
            Clipping Logic
            if coordinates are outside of the scene it could
            stretch the screen so don't transform them
            */
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
        }

    }

}
