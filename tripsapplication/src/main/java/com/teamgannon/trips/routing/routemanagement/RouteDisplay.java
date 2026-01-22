package com.teamgannon.trips.routing.routemanagement;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.routing.RoutingConstants;
import com.teamgannon.trips.routing.model.RouteSegment;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.transform.Translate;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;


@Slf4j
@Data
public class RouteDisplay {

    /**
     * the subscene for managing the display plane
     */
    private final SubScene subScene;

    /**
     * reference to the drawing screen
     */
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
     * a map of the route segments for separating the route segments
     */
    private Set<RouteSegment> routeSegments = new HashSet<>();


    /**
     * constructor
     *
     * @param tripsContext the context
     */
    public RouteDisplay(TripsContext tripsContext,
                        SubScene subScene,
                        InterstellarSpacePane interstellarSpacePane) {
        log.info("\n\n\nInitializing the Route Display\n\n\n");
        this.tripsContext = tripsContext;
        this.subScene = subScene;
        this.interstellarSpacePane = interstellarSpacePane;
    }

    public boolean isManualRoutingActive() {
        return manualRoutingActive;
    }

    public void setManualRoutingActive(boolean flag) {
        manualRoutingActive = flag;
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
        routeSegments.clear();
    }

    /**
     * check if a route segment is present
     *
     * @param routeSegment the route segment
     * @return true if there
     */
    public boolean contains(RouteSegment routeSegment) {
        return routeSegments.contains(routeSegment);
    }

    public void addSegment(RouteSegment routeSegment) {
        routeSegments.add(routeSegment);
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
        log.info("\n\n\n\nAdd new route to display\n\n\n\n");
        if (routeDescriptor != null) {
            if (!routesGroup.getChildren().contains(routeToAdd)) {
                routeLookup.put(routeDescriptor.getId(), routeToAdd);
                routesGroup.getChildren().add(routeToAdd);
            } else {
                log.error("Already contains route={}", routeDescriptor);
            }
        }
    }

    public void removeRouteFromDisplay(Group routeToRemove) {
        routesGroup.getChildren().remove(routeToRemove);
    }


    /////////////////////////////////////

    /**
     * Update all route labels to match current screen positions after view rotation/zoom.
     * <p>
     * This method implements the <b>billboard labels pattern</b> where 2D labels are
     * positioned to track 3D anchor points while remaining flat on screen.
     * <p>
     * <b>Coordinate Transformation (Two-Step Process):</b>
     * <ol>
     *   <li><b>3D to Scene:</b> {@code node.localToScene(Point3D.ZERO, true)} transforms
     *       the 3D anchor point to scene coordinates. The {@code true} parameter ensures
     *       the entire transform chain is applied.</li>
     *   <li><b>Scene to Local:</b> {@code labelDisplayGroup.sceneToLocal(x, y)} converts
     *       scene coordinates to the label group's local coordinate space.</li>
     * </ol>
     * <p>
     * <b>Why Two Steps Are Required:</b>
     * Using only {@code localToScene()} would give coordinates in the Scene's space,
     * but the labelDisplayGroup may have its own transforms. Without the second step,
     * labels would drift as the 3D world rotates because coordinate spaces don't align.
     * <p>
     * <b>Visibility Logic:</b>
     * <ul>
     *   <li>Labels within {@link RoutingConstants#LABEL_CLIPPING_PADDING} of viewport edges are hidden</li>
     *   <li>Labels are clamped to prevent going off-screen using {@link RoutingConstants#LABEL_EDGE_MARGIN}</li>
     *   <li>The {@code controlPaneOffset} accounts for any control panes at the top of the view</li>
     * </ul>
     * <p>
     * <b>When to Call:</b> After mouse drag (rotation), scroll (zoom), or initial render.
     *
     * @see RoutingConstants#LABEL_CLIPPING_PADDING
     * @see RoutingConstants#LABEL_EDGE_MARGIN
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

            // configure visibility - hide labels too close to viewport edges
            double padding = RoutingConstants.LABEL_CLIPPING_PADDING;
            if (xs < (ofParent.getMinX() + padding) || xs > (ofParent.getMaxX() - padding)) {
                label.setVisible(false);
                continue;
            } else {
                label.setVisible(true);
            }
            if (ys < (controlPaneOffset + padding) || (ys > ofParent.getMaxY() - padding)) {
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

            // is it right of the view? Clamp to viewport with margin
            double margin = RoutingConstants.LABEL_EDGE_MARGIN;
            if ((x + label.getWidth() + margin) > subScene.getWidth()) {
                x = subScene.getWidth() - (label.getWidth() + margin);
            }

            // is it above the view?
            if (y < 0) {
                y = 0;
            }

            // is it below the view? Clamp to viewport with margin
            if ((y + label.getHeight()) > subScene.getHeight()) {
                y = subScene.getHeight() - (label.getHeight() + margin);
            }

            //update the local transform of the label.
            label.getTransforms().setAll(new Translate(x, y));
        }

    }

}
