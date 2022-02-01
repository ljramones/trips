package com.teamgannon.trips.graphics.entities;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.model.Route;
import com.teamgannon.trips.routing.model.RouteSegment;
import com.teamgannon.trips.routing.tree.treemodel.RouteTree;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import lombok.Builder;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class RouteDescriptor {

    /**
     * unique id for distinction purposes
     */
    @Builder.Default
    private UUID id = UUID.randomUUID();

    /**
     * name of the route
     */
    private String name;

    /**
     * max length of each segment
     */
    private double maxLength;

    /**
     * total length of the route
     */
    private double totalLength;

    /**
     * width of the routing line
     */
    private double lineWidth;

    /**
     * whether the entire route is fully or partially visible or not at all
     * <p>
     * FULL is that entire route is visible
     * PARTIAL is that only some stars are visible
     * INVISIBLE means none of the route is visible
     */
    private RouteVisibility visibility;

    /**
     * defined color of route
     */
    @Builder.Default
    private @NotNull Color color = Color.WHITE;

    /**
     * list of ordered routes, min length is 2
     */
    @Builder.Default
    private @NotNull List<UUID> routeList = new ArrayList<>();

    /**
     * the list of stars in the route
     */
    @Builder.Default
    @Transient
    private List<StarDisplayRecord> starDisplayRecords = new ArrayList<>();

    @Transient
    private DataSetDescriptor descriptor;

    /**
     * list of star names
     */
    @Builder.Default
    private @NotNull List<String> nameList = new ArrayList<>();

    /**
     * list of ordered lengths, min length is 2
     */
    @Builder.Default
    private @NotNull List<Double> lengthList = new ArrayList<>();

    /**
     * only during plotting, leave empty otherwise
     */
    @Transient
    @Builder.Default
    private @NotNull List<Node> lineSegmentList = new ArrayList<>();

    /**
     * only during plotting, leave empty otherwise
     */
    @Transient
    @Builder.Default
    private @NotNull List<Label> labelList = new ArrayList<>();

    /**
     * we keep track of the from star for calculations
     */
    @Transient
    private StarDisplayRecord lastStar;

    /**
     * start star name
     */
    private String startStar;

    /**
     * the notes for the route
     */
    private String routeNotes;

    /**
     * the list of line segments joined by 3D points
     * only during plotting, leave empty otherwise
     */
    @Builder.Default
    private List<Point3D> routeCoordinates = new ArrayList<>();


    public void mutateCoordinates(RouteSegment routeSegment) {
        Point3D from = routeCoordinates.get(routeSegment.getIndexA());
        double xFrom = from.getX() + 2;
        double yFrom = from.getY() + 2;
        double zFrom = from.getZ() + 2;
        from = new Point3D(xFrom, yFrom, zFrom);

        Point3D to = routeCoordinates.get(routeSegment.getIndexB());
        double xTo = to.getX() + 2;
        double yTo = to.getY() + 2;
        double zTo = to.getZ() + 2;
        to = new Point3D(xTo, yTo, zTo);

        routeCoordinates.set(routeSegment.getIndexA(), from);
        routeCoordinates.set(routeSegment.getIndexB(), to);
    }

    public List<RouteSegment> getRouteSegments() {
        List<RouteSegment> segments = new ArrayList<>();
        boolean first = true;
        Point3D fromPoint = null;
        int i = 0;
        for (Point3D point3D : routeCoordinates) {
            if (first) {
                fromPoint = point3D;
                first = false;
                i++;
            } else {
                if (fromPoint != null) {
                    double fromPointX = fromPoint.getX();
                    double fromPointY = fromPoint.getY();
                    double fromPointZ = fromPoint.getZ();
                    double[] fromPoint3d = new double[]{fromPointX, fromPointY, fromPointZ};

                    double toPointX = point3D.getX();
                    double toPointY = point3D.getY();
                    double toPointZ = point3D.getZ();
                    double[] toPoint3d = new double[]{toPointX, toPointY, toPointZ};

                    RouteSegment routeSegment = new RouteSegment();
                    routeSegment.setPointA(fromPoint3d);
                    routeSegment.setIndexA(i - 1);
                    routeSegment.setPointB(toPoint3d);
                    routeSegment.setIndexB(i);
                    segments.add(routeSegment);
                    i++;
                }
            }
        }
        return segments;
    }

    public static RouteDescriptor toRouteDescriptor(@NotNull Route route) {
        return RouteDescriptor
                .builder()
                .id(route.getUuid())
                .name(route.getRouteName())
                .maxLength(route.getRouteStars().size())
                .color(Color.valueOf(route.getRouteColor()))
                .lineWidth(route.getLineWidth())
                .routeNotes(route.getRouteNotes())
                .startStar(route.getStartingStar())
                .build();
    }

    public static RouteDescriptor toRouteDescriptor(@NotNull RouteTree routeTree) {
        return RouteDescriptor
                .builder()
                .descriptor(routeTree.getDescriptor())
                .id(routeTree.getUuid())
                .name(routeTree.getRouteName())
                .maxLength(routeTree.getRouteSegmentList().size())
                .color(routeTree.getRouteColor())
                .lineWidth(routeTree.getLineWidth())
                .routeNotes(routeTree.getRouteNotes())
                .startStar(routeTree.getStartingStar())
                .build();
    }

    public void addLineSegment(Point3D point3D) {
        routeCoordinates.add(point3D);
    }

    public void addLengthSegment(Double length) {
        lengthList.add(length);
    }

    public void addLink(StarDisplayRecord record, Point3D starPosition,
                        double routeLength, Node lineSegment, Label lengthLabel) {
        if (starDisplayRecords.size() == 0) {
            startStar = record.getStarName();
            lastStar = record;
            nameList.add(startStar);
            routeCoordinates.add(starPosition);
            routeList.add(record.getRecordId());
            starDisplayRecords.add(record);
        } else {
            starDisplayRecords.add(record);

            routeCoordinates.add(starPosition);
            lineSegmentList.add(lineSegment);

            lengthList.add(routeLength);
            totalLength += routeLength;

            routeList.add(record.getRecordId());

            labelList.add(lengthLabel);

            nameList.add(record.getStarName());
            lastStar = record;
        }

    }

    public double findTotalLength() {
        lengthList.forEach(length -> totalLength += length);
        return totalLength;
    }

    public boolean removeLast() {
        int lastIndex = routeList.size() - 1;
        if (lastIndex > 1) {
            routeList.remove(lastIndex);
            lengthList.remove(lastIndex - 2);
            lastStar = starDisplayRecords.get(lastIndex - 1);
            lineSegmentList.remove(lastIndex - 1);
            nameList.remove(lastIndex);
            starDisplayRecords.remove(lastIndex);
            labelList.remove(lastIndex - 1);
            routeCoordinates.remove(lastIndex);
            return false;
        } else {
            routeList.clear();
            lengthList.clear();
            labelList.clear();
            startStar = starDisplayRecords.get(0).getStarName();
            lastStar = starDisplayRecords.get(0);
            lineSegmentList.clear();
            routeCoordinates.clear();
            nameList.clear();
            starDisplayRecords.clear();
            return true;
        }
    }

    public Label getLastLabel() {
        int lastIndex = labelList.size() - 1;
        if (lastIndex >= 0) {
            return labelList.get(lastIndex);
        } else {
            return null;
        }
    }

    public void clear() {
        name = "";
        routeList.clear();
        color = Color.LIGHTCORAL;
        routeCoordinates.clear();
    }

    public @NotNull Route toRoute() {
        Route route = new Route();

        route.setUuid(id);
        route.setRouteName(this.name);
        route.getRouteStars().addAll(routeList);
        route.getRouteLengths().addAll(lengthList);
        route.setRouteNotes(this.routeNotes);
        route.setStartingStar(this.startStar);
        route.getRouteStarNames().addAll(this.nameList);
        route.setLineWidth(this.lineWidth);
        route.setRouteColor(this.color.toString());
        route.setTotalLength(getTotalLength());

        return route;
    }

}
