package com.teamgannon.trips.experiments.elements;

import com.teamgannon.trips.dead.StatusReporter;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import javafx.geometry.Point3D;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
public class CombinedStarPane extends Pane {

    private InterstellarSpacePane starPane;

    private boolean gridOn = true;
    private boolean extensionsOn = true;
    private boolean starsOn = true;
    private boolean scaleOn = true;
    private boolean routesOn = true;

    private int width;
    private int height;
    private int depth;

    private double routeLength = 10;

    private StatusReporter reporter = new StatusReporter();

    /**
     * the set of colors that we use to randomize
     */
    private Color[] colors = new Color[10];

    /**
     * the lsit of star display recored that are available for this graph
     */
    private List<StarDisplayRecord> recordList = new ArrayList<>();

    /**
     * list of routes
     */
    private List<RouteDescriptor> routeList = new ArrayList<>();

    /**
     * the current route
     */
    private RouteDescriptor currentRoute;

    /**
     * utility to generate random numbers
     */
    private static final Random random = new Random();

    /**
     * constructor for the star pane
     *
     * @param width   width
     * @param height  height
     * @param depth   depth
     * @param spacing spacing of grid
     */
    public CombinedStarPane(int width, int height, int depth, int spacing) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.setMinHeight(height);
        this.setMinWidth(width);

        // create main graphics display pane
        starPane = new InterstellarSpacePane(width, height, depth, spacing);

        // setup event listeners
        starPane.setListUpdater(reporter);
        starPane.setContextUpdater(reporter);
        starPane.setStellarObjectDisplayer(reporter);
        starPane.setRouteUpdater(reporter);
        starPane.setRedrawListener(reporter);

        VBox vBox = new VBox();
        this.getChildren().add(vBox);

        createMenu(vBox);
        createToolBar(vBox);

        vBox.getChildren().add(starPane);
    }

    ////////////// toolbar ///////////////////////////////

    private void createToolBar(VBox vBox) {
        ToolBar toolBar = new ToolBar();

        Button starButton = new Button("Stars");
        starButton.setOnAction(e -> {
            toggleStars();
        });
        toolBar.getItems().add(starButton);

        Button gridButton = new Button("Grid");
        gridButton.setOnAction(e -> {
            toggleGrid();
        });
        toolBar.getItems().add(gridButton);

        Button extensionsButton = new Button("Extensions");
        extensionsButton.setOnAction(e -> {
            toggleExtensions();
        });
        toolBar.getItems().add(extensionsButton);

        Button scaleButton = new Button("Scale");
        scaleButton.setOnAction(e -> {
            toggleScale();
        });
        toolBar.getItems().add(scaleButton);

        Button zoomInButton = new Button("+");
        zoomInButton.setOnAction(e -> {
            starPane.zoomIn();
        });
        toolBar.getItems().add(zoomInButton);

        Button zoomOutButton = new Button("-");
        zoomOutButton.setOnAction(e -> {
            starPane.zoomOut();
        });
        toolBar.getItems().add(zoomOutButton);

        vBox.getChildren().add(toolBar);
    }


    /////////////////  Menu Construction  ////////

    private void createMenu(VBox vBox) {
        MenuBar menuBar = new MenuBar();
        vBox.getChildren().add(menuBar);

        menuBar.getMenus().add(createDisplayMenu());
        menuBar.getMenus().add(createPlotMenu());
        menuBar.getMenus().add(createAnimationsMenu());
        menuBar.getMenus().add(createRouteMenu());
    }

    private Menu createRouteMenu() {
        Menu routeMenu = new Menu("Route");

        MenuItem createRoute = new MenuItem("Create route");
        createRoute.setOnAction(e -> {
            starPane.createRoute(currentRoute);
        });
        routeMenu.getItems().add(createRoute);

        MenuItem finishRoute = new MenuItem("Complete route");
        finishRoute.setOnAction(e -> {
            starPane.completeRoute();
        });
        routeMenu.getItems().add(finishRoute);

        MenuItem plotRoute = new MenuItem("Plot routes");
        plotRoute.setOnAction(e -> {
//            starPane.plotRoutes(routeList);
            starPane.plotRoute(createRoute());
        });
        routeMenu.getItems().add(plotRoute);

        return routeMenu;
    }

    private Menu createAnimationsMenu() {
        Menu animationsMenu = new Menu("Animate");

        MenuItem startYaxisRotation = new MenuItem("Start Y-axis rotation");
        startYaxisRotation.setOnAction(e -> {
            starPane.startYrotate();
        });
        animationsMenu.getItems().add(startYaxisRotation);

        MenuItem stopYaxisRotation = new MenuItem("Stop Y-axis rotation");
        stopYaxisRotation.setOnAction(e -> {
            starPane.stopYrotate();
        });
        animationsMenu.getItems().add(stopYaxisRotation);

        return animationsMenu;
    }

    private Menu createPlotMenu() {
        Menu plotMenu = new Menu("Plot");

        MenuItem simulate = new MenuItem("simulate");
        simulate.setOnAction(e -> {
            simulate();
        });
        plotMenu.getItems().add(simulate);

        MenuItem plotStars = new MenuItem("Plot Stars");
        plotStars.setOnAction(e -> {
            starPane.plotStars(recordList);
        });
        plotMenu.getItems().add(plotStars);

        MenuItem plotRoutes = new MenuItem("Plot Routes");
        plotStars.setOnAction(e -> {
            starPane.plotRoutes(routeList);
        });
        plotMenu.getItems().add(plotRoutes);

        MenuItem clearStars = new MenuItem("Clear Stars");
        clearStars.setOnAction(e -> {
            starPane.clearStars();
        });
        plotMenu.getItems().add(clearStars);

        MenuItem clearRoutes = new MenuItem("Clear Routes");
        clearRoutes.setOnAction(e -> {
            starPane.clearRoutes();
        });
        plotMenu.getItems().add(clearRoutes);

        return plotMenu;
    }

    private Menu createDisplayMenu() {
        Menu displayMenu = new Menu("Display");

        MenuItem toggleGrid = new MenuItem("Toggle Grid");
        toggleGrid.setOnAction(e -> {
            toggleGrid();
        });
        displayMenu.getItems().add(toggleGrid);

        MenuItem toggleExtensions = new MenuItem("Toggle Grid Extensions");
        toggleExtensions.setOnAction(e -> {
            toggleExtensions();
        });
        displayMenu.getItems().add(toggleExtensions);

        MenuItem stars = new MenuItem("Toggle Stars");
        stars.setOnAction(e -> {
            toggleStars();
        });
        displayMenu.getItems().add(stars);

        MenuItem scale = new MenuItem("Toggle Scale");
        scale.setOnAction(e -> {
            toggleScale();
        });
        displayMenu.getItems().add(scale);

        MenuItem routes = new MenuItem("Toggle Routes");
        routes.setOnAction(e -> {
            toggleRoutes();
        });
        displayMenu.getItems().add(routes);
        return displayMenu;
    }

    ////////////////// event actions /////////////////

    private void toggleGrid() {
        gridOn = !gridOn;
        starPane.toggleGrid(gridOn);
    }

    private void toggleExtensions() {
        extensionsOn = !extensionsOn;
        starPane.toggleExtensions(extensionsOn);
    }

    private void toggleStars() {
        starsOn = !starsOn;
        starPane.toggleStars(starsOn);
    }

    private void toggleScale() {
        scaleOn = !scaleOn;
        starPane.toggleScale(scaleOn);
    }

    private void toggleRoutes() {
        routesOn = !routesOn;
        starPane.toggleRoutes(routesOn);
    }

    ///////////////// Simulation  //////////////////////////////


    /**
     * simulate stars
     */
    private void simulate() {

        initializeColors();

        log.info("clearing stars");
        recordList.clear();

        log.info("drawing 25 random stars, plus earth");
        recordList.add(createSolNode());
        IntStream.range(0, 25).forEach(i -> recordList.add(createStarNode()));

        starPane.drawStar(recordList, "Sol");
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