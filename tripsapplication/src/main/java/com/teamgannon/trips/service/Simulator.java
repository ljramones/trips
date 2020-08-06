package com.teamgannon.trips.service;

import com.teamgannon.trips.config.application.ColorPalette;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
public class Simulator {


    /**
     * the set of colors that we use to randomize
     */
    private final Color[] colors = new Color[10];


    /**
     * the lsit of star display recored that are available for this graph
     */
    private final List<StarDisplayRecord> recordList = new ArrayList<>();

    /**
     * list of routes
     */
    private final List<RouteDescriptor> routeList = new ArrayList<>();


    private final InterstellarSpacePane starPane;

    private final Random random = new Random();

    private final int width;
    private final int height;
    private final int depth;

    private final ColorPalette colorPalette;

    public Simulator(InterstellarSpacePane starPane, int width, int height, int depth, ColorPalette colorPalette) {
        this.starPane = starPane;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.colorPalette = colorPalette;
    }


    /**
     * simulate stars
     */
    public void simulate() {

        initializeColors();

        log.info("clearing stars");
        recordList.clear();

        log.info("drawing 25 random stars, plus earth");
        recordList.add(createSolNode());
        IntStream.range(0, 25).forEach(i -> recordList.add(createStarNode()));



        starPane.drawStar(recordList, "Sol" , colorPalette);
    }


    /**
     * create an earth node
     *
     * @return the earth node
     */
    private StarDisplayRecord createSolNode() {
        return StarDisplayRecord.builder()
                .starName("Sol")
                .starColor(Color.YELLOW)
                .radius(2)
                .recordId(UUID.randomUUID())
                .coordinates(new Point3D(0, 0, 0))
                .build();

    }

    private StarDisplayRecord createStarNode() {
        double starSize = 10 * Math.random() + 1;

        double x = width / 2.0 * random.nextDouble()
                * (random.nextBoolean() ? 1 : -1);
        // we flip the y axis since it points down
        double y = -height / 2.0 * random.nextDouble()
                * (random.nextBoolean() ? 1 : -1);
        double z = depth / 2.0 * random.nextDouble()
                * (random.nextBoolean() ? 1 : -1);

        Color color = chooseRandomColor();
        String name = generateRandomLabel();

        return StarDisplayRecord.builder()
                .starName(name)
                .starColor(color)
                .radius(starSize)
                .recordId(UUID.randomUUID())
                .coordinates(new Point3D(x, y, z))
                .build();
    }


    /**
     * generate a random color
     *
     * @return the color
     */
    private Color chooseRandomColor() {
        int index = (int) (10 * Math.random());
        return colors[index];
    }


    /**
     * generate a random label
     *
     * @return the label
     */
    private String generateRandomLabel() {
        int i = (int) (100 * Math.random());
        return "star-" + i;
    }


    /**
     * initialize the colors that we will select form
     */
    private void initializeColors() {
        colors[0] = Color.ALICEBLUE;
        colors[1] = Color.CHARTREUSE;
        colors[2] = Color.RED;
        colors[3] = Color.YELLOW;
        colors[4] = Color.YELLOWGREEN;
        colors[5] = Color.GREEN;
        colors[6] = Color.PEACHPUFF;
        colors[7] = Color.GOLD;
        colors[8] = Color.DARKMAGENTA;
        colors[9] = Color.OLIVE;
    }

    private RouteDescriptor createRoute() {
        RouteDescriptor route
                = RouteDescriptor.builder()
                .name("TestRoute")
                .color(Color.LIGHTCORAL)
                .maxLength(10)
                .startStar("Sol")
                .build();

        List<Point3D> lineSegments = new ArrayList<>();
        lineSegments.add(new Point3D(0, 0, 0));
        lineSegments.add(new Point3D(10, 10, 10));
        lineSegments.add(new Point3D(5, -2, 10));
        lineSegments.add(new Point3D(15, 2, 15));
        lineSegments.add(new Point3D(20, 8, 15));
        lineSegments.add(new Point3D(25, 10, 20));
        route.setLineSegments(lineSegments);

        return route;
    }

}
