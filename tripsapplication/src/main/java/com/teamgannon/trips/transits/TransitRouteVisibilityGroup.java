package com.teamgannon.trips.transits;

import com.teamgannon.trips.config.application.model.SerialFont;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.entities.StellarEntityFactory;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.List;

/**
 * Manages the visualization of transit routes for a specific transit band.
 * Handles 3D line rendering, labels, and user interaction via context menus.
 */
@Slf4j
public class TransitRouteVisibilityGroup {

    private final TransitGraphicsContext context;
    private final TransitRangeDef transitRangeDef;

    @Getter
    private final Group group;

    @Getter
    private final Group labelGroup;

    @Getter
    private final String groupName;

    @Getter
    private final UUID groupId;

    private final Map<String, TransitRoute> transitRouteMap = new HashMap<>();
    private final Map<Node, Label> shapeToLabel = new HashMap<>();

    /**
     * Gets all transit routes in this visibility group.
     *
     * @return collection of transit routes
     */
    public java.util.Collection<TransitRoute> getTransitRoutes() {
        return transitRouteMap.values();
    }

    /**
     * Gets the number of transit routes in this group.
     *
     * @return route count
     */
    public int getTransitCount() {
        return transitRouteMap.size();
    }

    private static final double LABEL_PADDING = TransitConstants.LABEL_PADDING;
    private static final double LABEL_EDGE_MARGIN = TransitConstants.LABEL_EDGE_MARGIN;
    private static final double LABEL_COLLISION_PADDING = 4.0;

    /**
     * Create a new transit visibility group.
     *
     * @param context         shared graphics context and services
     * @param transitRangeDef the transit band definition
     */
    public TransitRouteVisibilityGroup(TransitGraphicsContext context, TransitRangeDef transitRangeDef) {
        this.context = context;
        this.transitRangeDef = transitRangeDef;

        this.group = new Group();
        this.labelGroup = new Group();
        this.groupName = transitRangeDef.getBandName();
        this.groupId = transitRangeDef.getBandId();

        group.setVisible(false);
        labelGroup.setVisible(false);
    }

    /**
     * Toggle visibility of transit lines and labels.
     */
    public void toggleTransit(boolean show) {
        group.setVisible(show);
        labelGroup.setVisible(show);
    }

    /**
     * Toggle visibility of labels only (if transits are visible).
     */
    public void toggleLabels(boolean show) {
        if (group.isVisible()) {
            labelGroup.setVisible(show);
        }
    }

    /**
     * Clear all transit visualizations.
     */
    public void clear() {
        labelGroup.getChildren().clear();
        group.getChildren().clear();
        shapeToLabel.clear();
        transitRouteMap.clear();
    }

    /**
     * Plot transits for the given stars within the transit range.
     * Calculates routes using the distance calculator.
     */
    public void plotTransit(TransitRangeDef transitRangeDef, List<StarDisplayRecord> starsInView) {
        List<TransitRoute> transitRoutes = context.getDistanceCalculator()
                .calculateDistances(transitRangeDef, starsInView);
        log.debug("# of routes found is {}", transitRoutes.size());

        MapUtils.populateMap(transitRouteMap, transitRoutes, TransitRoute::getName);
        plotTransitRoutes(transitRoutes);
    }

    /**
     * Plot pre-calculated transit routes.
     * Use this when routes have already been calculated (e.g., from async calculation).
     *
     * @param transitRoutes the pre-calculated routes to display
     */
    public void plotPreCalculatedRoutes(@NotNull List<TransitRoute> transitRoutes) {
        log.debug("Plotting {} pre-calculated routes for band {}", transitRoutes.size(), groupName);
        MapUtils.populateMap(transitRouteMap, transitRoutes, TransitRoute::getName);
        plotTransitRoutes(transitRoutes);
    }

    private void plotTransitRoutes(@NotNull List<TransitRoute> transitRoutes) {
        for (TransitRoute transitRoute : transitRoutes) {
            log.debug("transit: {}", transitRoute);
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

        group.setVisible(true);
        labelGroup.setVisible(true);
    }

    private @NotNull String hoverText(@NotNull TransitRoute transitRoute) {
        return "transit: "
                + transitRoute.getSource().getStarName() + " <--> "
                + transitRoute.getTarget().getStarName() + " is "
                + String.format("%.2f", transitRoute.getDistance()) + " ly";
    }

    private @NotNull Node createLineSegment(Point3D origin, @NotNull Point3D target,
                                            double lineWeight, Color color, @NotNull Label lengthLabel) {
        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D diff = target.subtract(origin);
        double height = diff.magnitude();

        Point3D mid = target.midpoint(origin);
        Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

        Point3D axisOfRotation = diff.crossProduct(yAxis);
        double angle = Math.acos(diff.normalize().dotProduct(yAxis));
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);

        var line = StellarEntityFactory.createCylinder(lineWeight, color, height);

        Group lineGroup = new Group();
        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);
        lineGroup.getChildren().add(line);

        // Always create label attachment point
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
            log.warn("Label <{}> already present", lengthLabel.getText());
        }

        return lineGroup;
    }

    private @NotNull Sphere createPointSphere(@NotNull Label label) {
        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.WHEAT);
        material.setSpecularColor(Color.WHEAT);
        Sphere sphere = new Sphere(TransitConstants.LABEL_ANCHOR_SPHERE_RADIUS);
        sphere.setMaterial(material);
        label.setLabelFor(sphere);
        return sphere;
    }

    private @NotNull Label createLabel(@NotNull TransitRoute transitRoute) {
        Label label = new Label(String.format(" %.2f ", transitRoute.getDistance()));
        SerialFont serialFont = context.getTripsContext().getCurrentPlot().getColorPalette().getLabelFont();
        label.setFont(serialFont.toFont());
        return label;
    }

    private void createContextMenu(@NotNull Node transitSegment) {
        TransitRoute route = (TransitRoute) transitSegment.getUserData();
        ContextMenu transitContextMenu = createPopup(hoverText(route), transitSegment);

        transitSegment.addEventHandler(
                MouseEvent.MOUSE_CLICKED,
                e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        transitContextMenu.show(transitSegment, e.getScreenX(), e.getScreenY());
                    }
                });
    }

    private @NotNull ContextMenu createPopup(String hoverText, @NotNull Node transitSegment) {
        final ContextMenu cm = new ContextMenu();

        MenuItem titleItem = new MenuItem(hoverText);
        titleItem.setDisable(true);
        cm.getItems().add(titleItem);

        cm.getItems().add(createNewRouteMenuItem(transitSegment));
        cm.getItems().add(createAddToRouteMenuItem(transitSegment));
        cm.getItems().add(createCompleteRouteMenuItem(transitSegment));
        cm.getItems().add(new SeparatorMenuItem());
        cm.getItems().add(createRemoveTransitMenuItem(transitSegment));

        return cm;
    }

    private @NotNull MenuItem createRemoveTransitMenuItem(@NotNull Node transitSegment) {
        MenuItem menuItem = new MenuItem("Remove");
        menuItem.setOnAction(event -> {
            TransitRoute transitRoute = (TransitRoute) transitSegment.getUserData();
            group.getChildren().remove(transitSegment);
            transitRouteMap.remove(transitRoute.getName());
            log.debug("Removed transit: {}", transitRoute.getName());
        });
        return menuItem;
    }

    private @NotNull MenuItem createNewRouteMenuItem(@NotNull Node transitSegment) {
        MenuItem menuItem = new MenuItem("Create New Route");
        menuItem.setOnAction(event -> {
            TransitRoute transitRoute = (TransitRoute) transitSegment.getUserData();
            context.getRouteBuilderService().startNewRoute(transitRoute);
        });
        return menuItem;
    }

    private @NotNull MenuItem createAddToRouteMenuItem(@NotNull Node transitSegment) {
        MenuItem menuItem = new MenuItem("Add To Route");
        menuItem.setOnAction(event -> {
            TransitRoute transitRoute = (TransitRoute) transitSegment.getUserData();
            context.getRouteBuilderService().addToRoute(transitRoute);
        });
        return menuItem;
    }

    private @NotNull MenuItem createCompleteRouteMenuItem(@NotNull Node transitSegment) {
        MenuItem menuItem = new MenuItem("Complete Route");
        menuItem.setOnAction(event -> {
            TransitRoute transitRoute = (TransitRoute) transitSegment.getUserData();
            context.getRouteBuilderService().completeRoute(transitRoute);
        });
        return menuItem;
    }

    /**
     * Update label positions after view rotation/zoom.
     * Uses two-step coordinate transformation for proper tracking.
     * Includes collision detection to prevent overlapping labels.
     */
    public void updateLabels() {
        if (!group.isVisible() || shapeToLabel.isEmpty()) {
            return;
        }

        var subScene = context.getSubScene();
        var pane = context.getInterstellarSpacePane();
        double controlPaneOffset = context.getControlPaneOffset();
        Bounds ofParent = pane.getBoundsInParent();

        // Phase 1: Build candidate list with screen positions and depths
        List<LabelCandidate> candidates = new ArrayList<>(shapeToLabel.size());

        for (Map.Entry<Node, Label> entry : shapeToLabel.entrySet()) {
            Node node = entry.getKey();
            Label label = entry.getValue();

            if (Double.isNaN(node.getTranslateX()) || Double.isNaN(node.getTranslateY()) || Double.isNaN(node.getTranslateZ())) {
                label.setVisible(false);
                continue;
            }

            Point3D sceneCoords = node.localToScene(Point3D.ZERO, true);

            if (Double.isNaN(sceneCoords.getX()) || Double.isNaN(sceneCoords.getY())) {
                label.setVisible(false);
                continue;
            }

            double xs = sceneCoords.getX();
            double ys = sceneCoords.getY();

            if (xs < (ofParent.getMinX() + LABEL_PADDING) || xs > (ofParent.getMaxX() - LABEL_PADDING)) {
                label.setVisible(false);
                continue;
            }
            if (ys < (controlPaneOffset + LABEL_PADDING) || ys > (ofParent.getMaxY() - LABEL_PADDING)) {
                label.setVisible(false);
                continue;
            }

            double distanceToCamera = sceneCoords.getZ();
            candidates.add(new LabelCandidate(node, label, sceneCoords, distanceToCamera));
        }

        // Phase 2: Sort by distance (closest to camera first = highest priority)
        candidates.sort((a, b) -> Double.compare(a.distanceToCamera(), b.distanceToCamera()));

        // Phase 3: Process with collision detection
        List<Rectangle2D> occupied = new ArrayList<>();

        for (LabelCandidate candidate : candidates) {
            Label label = candidate.label();
            Point3D sceneCoords = candidate.scenePoint();

            double xs = sceneCoords.getX();
            double ys = sceneCoords.getY();

            Point2D localPoint = labelGroup.sceneToLocal(xs, ys);
            double x = localPoint.getX();
            double y = localPoint.getY() - controlPaneOffset;

            x = Math.max(0, x);
            y = Math.max(0, y);

            // Measure label dimensions
            label.applyCss();
            label.autosize();
            double labelWidth = label.getWidth();
            double labelHeight = label.getHeight();

            if ((x + labelWidth + LABEL_EDGE_MARGIN) > subScene.getWidth()) {
                x = subScene.getWidth() - (labelWidth + LABEL_EDGE_MARGIN);
            }

            if ((y + labelHeight) > subScene.getHeight()) {
                y = subScene.getHeight() - (labelHeight + LABEL_EDGE_MARGIN);
            }

            // Create bounds rectangle with padding for collision detection
            Rectangle2D bounds = new Rectangle2D(
                    x - LABEL_COLLISION_PADDING,
                    y - LABEL_COLLISION_PADDING,
                    labelWidth + (LABEL_COLLISION_PADDING * 2),
                    labelHeight + (LABEL_COLLISION_PADDING * 2));

            // Check for collision with already-placed labels
            boolean collides = false;
            for (Rectangle2D occupiedBounds : occupied) {
                if (occupiedBounds.intersects(bounds)) {
                    collides = true;
                    break;
                }
            }

            if (collides) {
                label.setVisible(false);
                continue;
            }

            // No collision - show label and track its bounds
            label.setVisible(true);
            occupied.add(bounds);

            label.getTransforms().setAll(new Translate(x, y));
        }
    }

    /**
     * Internal record for sorting labels by distance to camera.
     */
    private record LabelCandidate(
            Node node,
            Label label,
            Point3D scenePoint,
            double distanceToCamera
    ) {}
}
