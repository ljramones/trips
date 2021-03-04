package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.config.application.CurrentPlot;
import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.SerialFont;
import com.teamgannon.trips.dialogs.routing.RouteDialog;
import com.teamgannon.trips.graphics.StarNotesDialog;
import com.teamgannon.trips.graphics.entities.CustomObjectFactory;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.entities.StellarEntityFactory;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.graphics.panes.StarSelectionModel;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.listener.*;
import com.teamgannon.trips.routing.RouteManager;
import com.teamgannon.trips.screenobjects.StarEditDialog;
import com.teamgannon.trips.screenobjects.StarEditStatus;
import com.teamgannon.trips.solarsystem.PlanetDialog;
import com.teamgannon.trips.solarsystem.SolarSystemGenOptions;
import com.teamgannon.trips.solarsystem.SolarSystemGenerationDialog;
import com.teamgannon.trips.solarsystem.SolarSystemReport;
import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
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
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.transform.Translate;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;

@Slf4j
public class StarPlotManager {

    /**
     * we do this to make the star size a constant size bigger x1.5
     */
    private final static double GRAPHICS_FUDGE_FACTOR = 1.5;
    private final static double RADIUS_MAX = 7;

    ///////////////////
    private final static double X_MAX = 300;
    private final static double Y_MAX = 300;
    private final static double Z_MAX = 300;
    /**
     * a graphics object group for extensions
     */
    private final Group extensionsGroup = new Group();
    /**
     * the stellar group for display
     */
    private final Group stellarDisplayGroup = new Group();
    /**
     * used to control label visibility
     */
    private final Group labelDisplayGroup = new Group();
    /**
     * to hold all the polities
     */
    private final Group politiesDisplayGroup = new Group();
    private final Group world;
    private final SubScene subScene;
    /**
     * used to signal an update to the parent list view
     */
    private final ListUpdaterListener listUpdaterListener;
    /**
     * the redraw listener
     */
    private final RedrawListener redrawListener;
    /**
     * to make database changes
     */
    private final DatabaseListener databaseListener;
    /**
     * used to an update to the parent controlling which graphics
     * panes is being displayed
     */
    private final ContextSelectorListener contextSelectorListener;
    /**
     * used to signal an update to the parent property panes
     */
    private final StellarPropertiesDisplayerListener displayer;
    /**
     * the report generator
     */
    private final ReportGenerator reportGenerator;

    private FadeTransition fadeTransition;


    private TripsContext tripsContext;
    /**
     * our color palette
     */
    private ColorPalette colorPalette;

    /**
     * used to implement a selection model for selecting stars
     */
    private final Map<Node, StarSelectionModel> selectionModel = new HashMap<>();
    private final Map<Node, Label> shapeToLabel = new HashMap<>();
    private final Random random = new Random();

    /**
     * label state
     */
    private boolean labelsOn = true;

    /**
     * toggle state of polities
     */
    private boolean politiesOn = true;

    /**
     * reference to the Route Manager
     */
    private RouteManager routeManager;


    private double controlPaneOffset;

    private StarDisplayPreferences starDisplayPreferences;

    /**
     * constructor
     *
     * @param world                   the graphics world
     * @param listUpdaterListener     the list updater
     * @param redrawListener          the redraw listener
     * @param databaseListener        the database listener
     * @param displayer               the displayer
     * @param contextSelectorListener the context selector
     * @param reportGenerator         the report generator
     * @param tripsContext            the trips context
     * @param colorPalette            the color palette
     */
    public StarPlotManager(@NotNull Group sceneRoot,
                           @NotNull Group world,
                           SubScene subScene,
                           ListUpdaterListener listUpdaterListener,
                           RedrawListener redrawListener,
                           DatabaseListener databaseListener,
                           StellarPropertiesDisplayerListener displayer,
                           ContextSelectorListener contextSelectorListener,
                           ReportGenerator reportGenerator,
                           TripsContext tripsContext,
                           ColorPalette colorPalette) {

        this.world = world;
        this.subScene = subScene;

        this.listUpdaterListener = listUpdaterListener;
        this.redrawListener = redrawListener;
        this.databaseListener = databaseListener;
        this.displayer = displayer;
        this.contextSelectorListener = contextSelectorListener;
        this.reportGenerator = reportGenerator;
        this.tripsContext = tripsContext;
        this.colorPalette = colorPalette;

        world.getChildren().add(stellarDisplayGroup);

        sceneRoot.getChildren().add(labelDisplayGroup);

        world.getChildren().add(extensionsGroup);

        world.getChildren().add(politiesDisplayGroup);

    }

    /**
     * get the plotted stars in view
     *
     * @return the list of star display records
     */
    public @NotNull List<StarDisplayRecord> getCurrentStarsInView() {
        List<StarDisplayRecord> starsInView = new ArrayList<>();
        for (UUID id : tripsContext.getCurrentPlot().getStarIds()) {
            StarDisplayRecord starDisplayRecord = (StarDisplayRecord) tripsContext.getCurrentPlot().getStar(id).getUserData();
            starsInView.add(starDisplayRecord);
        }
        starsInView.sort(Comparator.comparing(StarDisplayRecord::getStarName));
        return starsInView;
    }

    /**
     * toggle the extensions
     *
     * @param extensionsOn the extensions flag
     */
    public void toggleExtensions(boolean extensionsOn) {
        extensionsGroup.setVisible(extensionsOn);
    }

    public void setRouteManager(RouteManager routeManager) {
        this.routeManager = routeManager;
    }


    /**
     * clear the stars from the display
     */
    public void clearStars() {

        // remove stars
        stellarDisplayGroup.getChildren().clear();
        labelDisplayGroup.getChildren().clear();
        politiesDisplayGroup.getChildren().clear();
        shapeToLabel.clear();

        // remove the extension points to the stars
        extensionsGroup.getChildren().clear();
    }

    public void highlightStar(UUID starId) {
        Label starGroup = tripsContext.getCurrentPlot().getLabelForStar(starId);
        blinkStarLabel(starGroup, 60);
        log.info("mark point");
    }

    /**
     * the label to blink
     *
     * @param label      the label to blink
     * @param cycleCount the number of times on a 1 second interval to perform. Null is infinite
     */
    private void blinkStarLabel(Label label, int cycleCount) {
        if (fadeTransition!=null) {
            fadeTransition.stop();
        }
        fadeTransition = new FadeTransition(Duration.seconds(1), label);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        fadeTransition.setCycleCount(cycleCount);
        fadeTransition.setAutoReverse(true);
        fadeTransition.play();
    }

    public void toggleStars(boolean starsOn) {
        stellarDisplayGroup.setVisible(starsOn);
        labelDisplayGroup.setVisible(starsOn);
    }


    public void drawStars(CurrentPlot currentPlot) {
        this.colorPalette = currentPlot.getColorPalette();
        this.starDisplayPreferences = currentPlot.getStarDisplayPreferences();

        for (StarDisplayRecord starDisplayRecord : currentPlot.getStarDisplayRecordList()) {
            plotStar(starDisplayRecord, currentPlot.getCenterStar(),
                    currentPlot.getColorPalette(), currentPlot.getStarDisplayPreferences(),
                    currentPlot.getCivilizationDisplayPreferences());
        }
    }

    public void clearPlot() {
        tripsContext.getCurrentPlot().clearPlot();
    }

    /**
     * toggle the labels
     *
     * @param labelSetting true is labels should be on
     */
    public void toggleLabels(boolean labelSetting) {
        this.labelsOn = labelSetting;

        // we can only do this if there are plot element on screen
        if (tripsContext.getCurrentPlot().isPlotActive()) {
            labelDisplayGroup.setVisible(labelSetting);
        }
    }

    public void togglePolities(boolean polities) {
        this.politiesOn = polities;
        log.info("toggle polities: {}", polities);

        // we can only do this if there are plot element on screen
        if (tripsContext.getCurrentPlot().isPlotActive()) {
            politiesDisplayGroup.setVisible(polities);
        }
    }

    private void redrawPlot() {
        clearPlot();
        clearStars();
        log.info("redrawing plot: labels= {}, polities = {}", labelsOn, politiesOn);
        List<StarDisplayRecord> recordList = tripsContext.getCurrentPlot().getStarDisplayRecordList();
        recordList.forEach(
                starDisplayRecord -> plotStar(
                        starDisplayRecord,
                        tripsContext.getCurrentPlot().getCenterStar(),
                        colorPalette,
                        tripsContext.getCurrentPlot().getStarDisplayPreferences(),
                        tripsContext.getCurrentPlot().getCivilizationDisplayPreferences()
                )
        );

        // re-plot routes
        routeManager.plotRoutes(tripsContext.getCurrentPlot().getDataSetDescriptor().getRoutes());

    }

    private void plotStar(@NotNull StarDisplayRecord record,
                          String centerStar,
                          @NotNull ColorPalette colorPalette,
                          StarDisplayPreferences starDisplayPreferences,
                          CivilizationDisplayPreferences politiesPreferences) {

        Node starNode;
        // create a star for display
        if (record.getStarName().equals(centerStar)) {
            // we use a special icon for the center of the diagram plot
            starNode = createCentralPoint(record, colorPalette, starDisplayPreferences);
            log.info("sol is at {}", record.getActualCoordinates());
        } else {
            // otherwise draw a regular star
            starNode = createStar(
                    record,
                    colorPalette,
                    starDisplayPreferences,
                    politiesPreferences,
                    labelsOn,
                    politiesOn);

            // create the extension stem tot he star from the grid
            createExtension(record);
        }
        tripsContext.getCurrentPlot().addStar(record.getRecordId(), starNode);

        // draw the star on the pane
        stellarDisplayGroup.getChildren().add(starNode);
    }

    /**
     * Draw the central star to the plot
     *
     * @param record                 the star record to show
     * @param colorPalette           the color palette to use
     * @param starDisplayPreferences the star display preferences
     * @return the graphical object group representing the star
     */
    private @NotNull Node createCentralPoint(@NotNull StarDisplayRecord record,
                                             @NotNull ColorPalette colorPalette,
                                             StarDisplayPreferences starDisplayPreferences) {

        Label label = StellarEntityFactory.createLabel(record, colorPalette);
        labelDisplayGroup.getChildren().add(label);

        Node star = StellarEntityFactory.drawCentralIndicator(
                record,
                colorPalette,
                label,
                starDisplayPreferences);

        if (listUpdaterListener != null) {
            listUpdaterListener.updateList(record);
        }

        ContextMenu starContextMenu = createPopup(record.getStarName(), star);
        star.addEventHandler(MouseEvent.MOUSE_CLICKED,
                e -> starClickEventHandler(star, starContextMenu, e));
        star.setOnMousePressed(event -> {
            Node node = (Node) event.getSource();
            StarDisplayRecord starDescriptor = (StarDisplayRecord) node.getUserData();
            log.info("mouse click detected! " + starDescriptor);
        });

        star.setId("central");
        star.setUserData(record);
        return star;
    }

    /**
     * create a star named with radius and color located at x,y,z
     *
     * @param record                 the star record
     * @param colorPalette           the color palette to use
     * @param starDisplayPreferences the star preferences
     * @param labelsOn               whether labels are on or off
     * @param politiesOn             whether we polities on or off
     * @return the star to plot
     */
    private @NotNull Node createStar(@NotNull StarDisplayRecord record,
                                     @NotNull ColorPalette colorPalette,
                                     StarDisplayPreferences starDisplayPreferences,
                                     CivilizationDisplayPreferences politiesPreferences,
                                     boolean labelsOn,
                                     boolean politiesOn) {

        Node star = drawStellarObject(
                record,
                colorPalette,
                labelsOn,
                politiesOn,
                starDisplayPreferences,
                politiesPreferences);

        Tooltip tooltip = new Tooltip(record.getStarName());
        Tooltip.install(star, tooltip);

        if (listUpdaterListener != null) {
            listUpdaterListener.updateList(record);
        }

        ContextMenu starContextMenu = createPopup(record.getStarName(), star);
        star.addEventHandler(
                MouseEvent.MOUSE_CLICKED,
                e -> starClickEventHandler(star, starContextMenu, e));
        star.setOnMousePressed(event -> {
            Node node = (Node) event.getSource();
            StarDisplayRecord starDescriptor = (StarDisplayRecord) node.getUserData();
            log.info("mouse click detected! " + starDescriptor);
        });

        star.setId("regularStar");
        star.setUserData(record);

        return star;
    }

    public @NotNull Node drawStellarObject(@NotNull StarDisplayRecord record,
                                           @NotNull ColorPalette colorPalette,
                                           boolean labelsOn,
                                           boolean politiesOn,
                                           StarDisplayPreferences starDisplayPreferences,
                                           @NotNull CivilizationDisplayPreferences polityPreferences) {

        Group group = createStellarShape(record, colorPalette, labelsOn, politiesOn, polityPreferences);
        group.setUserData(record);
        return group;
    }

    /**
     * create a stellar object
     *
     * @param record            the star record
     * @param colorPalette      the color palette to use
     * @param labelsOn          are labels on?
     * @param politiesOn        are polities on?
     * @param polityPreferences the plo
     * @return the created object
     */
    public @NotNull Group createStellarShape(@NotNull StarDisplayRecord record,
                                             @NotNull ColorPalette colorPalette,
                                             boolean labelsOn,
                                             boolean politiesOn,
                                             @NotNull CivilizationDisplayPreferences polityPreferences) {

        Group group = new Group();

        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(record.getStarColor());
        material.setSpecularColor(record.getStarColor());
        Sphere sphere = new Sphere(record.getRadius() * GRAPHICS_FUDGE_FACTOR);
        sphere.setMaterial(material);
        Point3D point3D = record.getCoordinates();
        sphere.setTranslateX(point3D.getX());
        sphere.setTranslateY(point3D.getY());
        sphere.setTranslateZ(point3D.getZ());
        group.getChildren().add(sphere);

        if (labelsOn) {
            Label label = createLabel(record, colorPalette);
            label.setLabelFor(sphere);
            labelDisplayGroup.getChildren().add(label);
            tripsContext.getCurrentPlot().mapLabelToStar(record.getRecordId(), label);
            shapeToLabel.put(sphere, label);
        }

        if (politiesOn) {
            if (!record.getPolity().equals("NA")) {
                Color polityColor = polityPreferences.getColorForPolity(record.getPolity());
                // add a polity indicator
                double polityShellRadius = record.getRadius() * GRAPHICS_FUDGE_FACTOR * 1.5;
                // group.getChildren().add(politySphere);
                PhongMaterial polityMaterial = new PhongMaterial();
//            polityMaterial.setDiffuseMap(earthImage);
                polityMaterial.setDiffuseColor(new Color(polityColor.getRed(), polityColor.getGreen(), polityColor.getBlue(), 0.2));  // Note alpha of 0.6
                polityMaterial.diffuseMapProperty();
                Sphere politySphere = new Sphere(polityShellRadius);
                politySphere.setMaterial(polityMaterial);
                politySphere.setTranslateX(point3D.getX());
                politySphere.setTranslateY(point3D.getY());
                politySphere.setTranslateZ(point3D.getZ());
                politiesDisplayGroup.getChildren().add(politySphere);
                politiesDisplayGroup.setVisible(true);
            } else {
                log.debug("No polity to plot");
            }
        }
        return group;
    }

    /**
     * create a label for a shape
     *
     * @param record       the star record
     * @param colorPalette the color palette to use
     * @return the created object
     */
    public @NotNull Label createLabel(@NotNull StarDisplayRecord record,
                                      @NotNull ColorPalette colorPalette) {
        Label label = new Label(record.getStarName());
        SerialFont serialFont = colorPalette.getLabelFont();
        label.setFont(serialFont.toFont());
        label.setTextFill(colorPalette.getLabelColor());
        return label;
    }

    /**
     * create an extension for an added star
     *
     * @param record the star
     */
    private void createExtension(@NotNull StarDisplayRecord record) {
        double yZero = tripsContext.getCurrentPlot().getCenterCoordinates()[1];
        Point3D point3DFrom = record.getCoordinates();
        Point3D point3DTo = new Point3D(point3DFrom.getX(), yZero, point3DFrom.getZ());
        double lineWidth = colorPalette.getStemLineWidth();
        Node lineSegment = CustomObjectFactory.createLineSegment(point3DFrom, point3DTo,
                lineWidth, colorPalette.getExtensionColor(), colorPalette.getLabelFont().toFont());
        extensionsGroup.getChildren().add(lineSegment);
        // add the extensions group to the world model
        extensionsGroup.setVisible(true);
    }

    /**
     * setup the event click handler for a star
     *
     * @param star            the star
     * @param starContextMenu the menu
     * @param e               the exception caught
     */
    private void starClickEventHandler(Node star, @NotNull ContextMenu starContextMenu, @NotNull MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            log.info("Primary button pressed");
            starContextMenu.show(star, e.getScreenX(), e.getScreenY());
        }
        if (e.getButton() == MouseButton.MIDDLE) {
            log.info("Middle button pressed");
            starContextMenu.show(star, e.getScreenX(), e.getScreenY());
        }

        if (e.getButton() == MouseButton.SECONDARY) {
            log.info("Secondary button pressed");
            if (selectionModel.containsKey(star)) {
                // remove star and selection rectangle
                StarSelectionModel starSelectionModel = selectionModel.get(star);
                Group group = (Group) star;
                group.getChildren().remove(starSelectionModel.getSelectionRectangle());

                // remove the selection model
                selectionModel.remove(star);

            } else {
                // add star selection
                StarSelectionModel starSelectionModel = new StarSelectionModel();
                starSelectionModel.setStarNode(star);
                selectionModel.put(star, starSelectionModel);

            }
        }
    }

    /**
     * create a context menu for clicking on the stars
     *
     * @param name the name of the star
     * @param star the star
     * @return the menu
     */
    private @NotNull ContextMenu createPopup(String name, @NotNull Node star) {
        final ContextMenu cm = new ContextMenu();

        MenuItem titleItem = new MenuItem(name);
        titleItem.setStyle("-fx-text-fill: darkblue; -fx-font-size:20; -fx-font-weight: bold");
        titleItem.setDisable(true);
        cm.getItems().add(titleItem);
        cm.getItems().add(new SeparatorMenuItem());

        MenuItem highlightStarMenuItem = createHighlightStarMenuitem(star);
        cm.getItems().add(highlightStarMenuItem);

        MenuItem propertiesMenuItem = createShowPropertiesMenuItem(star);
        cm.getItems().add(propertiesMenuItem);

        MenuItem recenterMenuItem = createRecenterMenuitem(star);
        cm.getItems().add(recenterMenuItem);

        MenuItem editPropertiesMenuItem = createEditPropertiesMenuItem(star);
        cm.getItems().add(editPropertiesMenuItem);

        MenuItem enterNotesItem = createNotesMenuItem(star);
        cm.getItems().add(enterNotesItem);

        MenuItem removeMenuItem = createRemoveMenuItem(star);
        cm.getItems().add(removeMenuItem);

        cm.getItems().add(new SeparatorMenuItem());

        MenuItem startRouteMenuItem = createRoutingMenuItem(star);
        cm.getItems().add(startRouteMenuItem);

        MenuItem continueRouteMenuItem = continueRoutingMenuItem(star);
        cm.getItems().add(continueRouteMenuItem);

        MenuItem removeRouteMenuItem = removeRouteMenuItem(star);
        cm.getItems().add(removeRouteMenuItem);

        MenuItem finishRouteMenuItem = finishRoutingMenuItem(star);
        cm.getItems().add(finishRouteMenuItem);

        MenuItem resetRouteMenuItem = resetRoutingMenuItem();
        cm.getItems().add(resetRouteMenuItem);

        cm.getItems().add(new SeparatorMenuItem());

        MenuItem distanceToMenuItem = distanceReportMenuItem(star);
        cm.getItems().add(distanceToMenuItem);

        cm.getItems().add(new SeparatorMenuItem());

        MenuItem jumpSystemMenuItem = createEnterSystemItem(star);
        cm.getItems().add(jumpSystemMenuItem);

        MenuItem generateSolarSystemMenuItem = createGenerateSolarSystemItem(star);
        cm.getItems().add(generateSolarSystemMenuItem);

        return cm;
    }

    private MenuItem createGenerateSolarSystemItem(Node star) {
        MenuItem menuItem = new MenuItem("Generate Solar System");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            generateSolarSystem(starDescriptor);

        });
        return menuItem;
    }

    private void generateSolarSystem(StarDisplayRecord starDescriptor) {
        StarObject starObject = databaseListener.getStar(starDescriptor.getRecordId());
        SolarSystemGenerationDialog dialog = new SolarSystemGenerationDialog(starObject);
        Optional<SolarSystemGenOptions> solarSystemGenOptional = dialog.showAndWait();
        if (solarSystemGenOptional.isPresent()) {
            SolarSystemGenOptions solarSystemGenOptions = solarSystemGenOptional.get();
            SolarSystemReport report = new SolarSystemReport(starObject, solarSystemGenOptions);
            report.generateReport();

            PlanetDialog planetDialog = new PlanetDialog(report);
            planetDialog.showAndWait();

        }
    }

    private @NotNull MenuItem createHighlightStarMenuitem(@NotNull Node star) {
        MenuItem menuItem = new MenuItem("Highlight star");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            highlightStar(starDescriptor.getRecordId());

        });
        return menuItem;
    }

    private @NotNull MenuItem createNotesMenuItem(@NotNull Node star) {
        MenuItem menuItem = new MenuItem("Edit notes on this star");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            StarNotesDialog notesDialog = new StarNotesDialog(starDescriptor.getNotes());
            notesDialog.setTitle("Edit notes for " + starDescriptor.getStarName());
            Optional<String> notesOptional = notesDialog.showAndWait();
            if (notesOptional.isPresent()) {
                String notes = notesOptional.get();
                if (!notes.isEmpty()) {
                    // save notes in star
                    databaseListener.updateNotesForStar(starDescriptor.getRecordId(), notes);
                    // update the star notes on screen
                    starDescriptor.setNotes(notes);
                    star.setUserData(starDescriptor);
                }
            }

        });
        return menuItem;
    }

    private @NotNull MenuItem distanceReportMenuItem(@NotNull Node star) {
        MenuItem menuItem = new MenuItem("Generate Distances from this star");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            if (reportGenerator != null) {
                reportGenerator.generateDistanceReport(starDescriptor);
            } else {
                log.error("report generator should not be null --> bug");
            }
        });
        return menuItem;
    }

    private @NotNull MenuItem createRecenterMenuitem(@NotNull Node star) {
        MenuItem menuItem = new MenuItem("Recenter on this star");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            redrawListener.recenter(starDescriptor);
        });
        return menuItem;
    }

    private @NotNull MenuItem createRoutingMenuItem(@NotNull Node star) {
        MenuItem menuItem = new MenuItem("Start Route");
        menuItem.setOnAction(event -> {
            boolean routingActive = routeManager.isRoutingActive();
            if (routingActive) {
                Optional<ButtonType> buttonType = showConfirmationAlert("Remove Dataset",
                        "Restart Route?",
                        "You have a route in progress, Ok will clear current?");

                if ((buttonType.isEmpty()) || (buttonType.get() != ButtonType.OK)) {
                    return;
                }
            }
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            RouteDialog dialog = new RouteDialog(starDescriptor);
            Optional<RouteDescriptor> result = dialog.showAndWait();

            result.ifPresent(routeDescriptor -> routeManager.startRoute(
                    tripsContext.getCurrentPlot().getDataSetDescriptor(),
                    routeDescriptor, starDescriptor)
            );
        });
        return menuItem;
    }

    private @NotNull MenuItem continueRoutingMenuItem(@NotNull Node star) {
        MenuItem menuItem = new MenuItem("Continue Route");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            routeManager.continueRoute(starDescriptor);
        });
        return menuItem;
    }


    private MenuItem removeRouteMenuItem(Node star) {
        MenuItem menuItem = new MenuItem("Remove last link from route");
        menuItem.setOnAction(event -> {
            routeManager.removeRoute();
        });
        return menuItem;
    }


    private @NotNull MenuItem finishRoutingMenuItem(@NotNull Node star) {
        MenuItem menuItem = new MenuItem("Finish Route");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            routeManager.finishRoute(starDescriptor);
        });
        return menuItem;
    }

    private @NotNull MenuItem resetRoutingMenuItem() {
        MenuItem menuItem = new MenuItem("Route: Start over");
        menuItem.setOnAction(this::resetRoute);
        return menuItem;
    }

    private void resetRoute(ActionEvent event) {
        routeManager.resetRoute();
    }

    /**
     * create a menuitem to remove a targeted item
     *
     * @return the menuitem supporting this action
     */
    private @NotNull MenuItem createRemoveMenuItem(@NotNull Node star) {
        MenuItem removeMenuItem = new MenuItem("Remove");
        removeMenuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            removeNode(starDescriptor);
        });
        return removeMenuItem;
    }

    /**
     * remove a star node form the db
     *
     * @param starDisplayRecord the star to remove
     */
    private void removeNode(@NotNull StarDisplayRecord starDisplayRecord) {
        log.info("Removing object for:" + starDisplayRecord.getStarName());
        databaseListener.removeStar(starDisplayRecord.getRecordId());
    }

    /**
     * create an enter system object
     *
     * @param star the star selected
     * @return the menuitem
     */
    private @NotNull MenuItem createEnterSystemItem(@NotNull Node star) {
        MenuItem removeMenuItem = new MenuItem("Enter System");
        removeMenuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            jumpToSystem(starDescriptor);
        });
        return removeMenuItem;
    }

    /**
     * crate a menuitem to edit a targeted item
     *
     * @return the menuitem supporting this action
     */
    private @NotNull MenuItem createEditPropertiesMenuItem(@NotNull Node star) {
        MenuItem editPropertiesMenuItem = new MenuItem("Edit");
        editPropertiesMenuItem.setOnAction(event -> {
            StarDisplayRecord starDisplayRecord = (StarDisplayRecord) star.getUserData();
            StarDisplayRecord editRecord = editProperties(starDisplayRecord);
            star.setUserData(editRecord);

        });
        return editPropertiesMenuItem;
    }


    ///////////////////////// Simulate  /////////

    /**
     * edit a star in the database
     *
     * @param starDisplayRecord the properties to edit
     */
    private @Nullable StarDisplayRecord editProperties(@NotNull StarDisplayRecord starDisplayRecord) {
        StarObject starObject = databaseListener.getStar(starDisplayRecord.getRecordId());
        StarEditDialog starEditDialog = new StarEditDialog(starObject);
        Optional<StarEditStatus> optionalStarDisplayRecord = starEditDialog.showAndWait();
        if (optionalStarDisplayRecord.isPresent()) {
            StarEditStatus status = optionalStarDisplayRecord.get();
            if (status.isChanged()) {
                StarObject record = status.getRecord();
                StarDisplayRecord record1 = StarDisplayRecord.fromAstrographicObject(record, starDisplayPreferences);
                if (record1 != null) {
                    record1.setCoordinates(starDisplayRecord.getCoordinates());
                    log.info("Changed value: {}", record);
                    databaseListener.updateStar(record);
                } else {
                    log.error("Conversion of {} to star display record, returned a null-->bug!!", record);
                }
                return record1;
            } else {
                log.error("no return");
                return null;
            }
        }
        log.info("Editing properties in side panes for:" + starDisplayRecord.getStarName());
        return null;
    }

    /**
     * create a menuitem to show properties
     *
     * @return the menuitem supporting this action
     */
    private @NotNull MenuItem createShowPropertiesMenuItem(@NotNull Node star) {
        MenuItem propertiesMenuItem = new MenuItem("Properties");
        propertiesMenuItem.setOnAction(event -> {
            StarDisplayRecord starDisplayRecord = (StarDisplayRecord) star.getUserData();
            StarObject starObject = databaseListener.getStar(starDisplayRecord.getRecordId());
            displayProperties(starObject);
        });
        return propertiesMenuItem;
    }

    /**
     * display properties for this star
     *
     * @param starObject the properties to display
     */
    private void displayProperties(@NotNull StarObject starObject) {
        log.info("Showing properties in side panes for:" + starObject.getDisplayName());
        if (displayer != null) {
            displayer.displayStellarProperties(starObject);
        }
    }

    /**
     * jump to the solar system selected
     *
     * @param starDisplayRecord the properties of the star selected
     */
    private void jumpToSystem(StarDisplayRecord starDisplayRecord) {
        if (contextSelectorListener != null) {
            contextSelectorListener.selectSolarSystemSpace(starDisplayRecord);
        }
    }

    public void updateLabels(@NotNull InterstellarSpacePane interstellarSpacePane) {
        Bounds ofParent = interstellarSpacePane.getBoundsInParent();
        for (Map.Entry<Node, Label> entry : shapeToLabel.entrySet()) {
            Node node = entry.getKey();
            Label label = entry.getValue();
            Point3D coordinates = node.localToScene(Point3D.ZERO, true);

            // Clipping Logic
            // if coordinates are outside of the scene it could
            // stretch the screen so don't transform them
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
        }

    }

    /**
     * generate random stars
     *
     * @param numberStars number of stars
     */
    public void generateRandomStars(int numberStars) {
        for (int i = 0; i < numberStars; i++) {
            double radius = random.nextDouble() * RADIUS_MAX;
            Color color = randomColor();
            double x = random.nextDouble() * X_MAX * 2 / 3 * (random.nextBoolean() ? 1 : -1);
            double y = random.nextDouble() * Y_MAX * 2 / 3 * (random.nextBoolean() ? 1 : -1);
            double z = random.nextDouble() * Z_MAX * 2 / 3 * (random.nextBoolean() ? 1 : -1);

            String labelText = "Star " + i;
            createSphereAndLabel(radius, x, y, z, color, colorPalette.getLabelFont().toFont(), labelText);
            createExtension(x, y, z, Color.VIOLET);
        }

        log.info("shapes:{}", shapeToLabel.size());
    }

    private @NotNull Color randomColor() {
        int r = random.nextInt(255);
        int g = random.nextInt(255);
        int b = random.nextInt(255);
        return Color.rgb(r, g, b);
    }

    private void createSphereAndLabel(double radius, double x, double y, double z, Color color, Font font, String labelText) {
        Sphere sphere = new Sphere(radius);
        sphere.setTranslateX(x);
        sphere.setTranslateY(y);
        sphere.setTranslateZ(z);
        sphere.setMaterial(new PhongMaterial(color));
        //add our nodes to the group that will later be added to the 3D scene
        world.getChildren().add(sphere);

        Label label = new Label(labelText);
        label.setTextFill(color);
        label.setFont(font);
        ObjectDescriptor descriptor = ObjectDescriptor
                .builder()
                .name(labelText)
                .color(color)
                .x(x)
                .y(y)
                .z(z)
                .build();
        sphere.setUserData(descriptor);
        Tooltip tooltip = new Tooltip(descriptor.toString());
        Tooltip.install(sphere, tooltip);
        labelDisplayGroup.getChildren().add(label);

        //Add to hashmap so updateLabels() can manage the label position
        shapeToLabel.put(sphere, label);

    }

    private void setupFade(Node node) {
        FadeTransition fader = new FadeTransition(Duration.seconds(5), node);
        fader.setFromValue(1.0);
        fader.setToValue(0.1);
        fader.setCycleCount(Timeline.INDEFINITE);
        fader.setAutoReverse(true);
        fader.play();
    }


    private void createExtension(double x, double y, double z, Color extensionColor) {
        Point3D point3DFrom = new Point3D(x, y, z);
        Point3D point3DTo = new Point3D(point3DFrom.getX(), 0, point3DFrom.getZ());
        double lineWidth = 0.3;
        Node lineSegment = CustomObjectFactory.createLineSegment(point3DFrom, point3DTo, lineWidth, colorPalette.getExtensionColor(), colorPalette.getLabelFont().toFont());
        extensionsGroup.getChildren().add(lineSegment);
        // add the extensions group to the world model
        extensionsGroup.setVisible(true);
    }


    public void setControlPaneOffset(double controlPaneOffset) {
        this.controlPaneOffset = controlPaneOffset;
    }


}
