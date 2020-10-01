package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.dialogs.routing.RouteDialog;
import com.teamgannon.trips.graphics.CurrentPlot;
import com.teamgannon.trips.graphics.StarNotesDialog;
import com.teamgannon.trips.graphics.entities.*;
import com.teamgannon.trips.graphics.panes.StarSelectionModel;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.listener.*;
import com.teamgannon.trips.routing.RouteManager;
import com.teamgannon.trips.screenobjects.StarEditDialog;
import com.teamgannon.trips.screenobjects.StarEditStatus;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;

@Slf4j
public class StarPlotManager {

    /**
     * we do this to make the star size a constant size bigger x1.5
     */
    private final static double GRAPHICS_FUDGE_FACTOR = 1.5;

    /**
     * label state
     */
    private boolean labelsOn = true;

    ///////////////////

    /**
     * toggle state of polities
     */
    private boolean politiesOn = true;

    /**
     * a graphics object group for extensions
     */
    private final Xform extensionsGroup = new Xform();

    /**
     * the stellar group for display
     */
    private final Xform stellarDisplayGroup = new Xform();

    /**
     * used to control label visibility
     */
    private final Xform labelDisplayGroup = new Xform();

    /**
     * to hold all the polities
     */
    private final Xform politiesDisplayGroup = new Xform();


    ///////////////////

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

    /**
     * reference to the Route Manager
     */
    private RouteManager routeManager;

    /**
     * star display specifics
     */
    private StarDisplayPreferences starDisplayPreferences;

    /**
     * the current plot
     */
    private final CurrentPlot currentPlot;

    /**
     * our color palette
     */
    private final ColorPalette colorPalette;

    /**
     * the highlight rotator
     */
    private RotateTransition highlightRotator;

    /**
     * the civilization and
     */
    private CivilizationDisplayPreferences politiesPreferences;

    /**
     * used to implement a selection model for selecting stars
     */
    private final Map<Node, StarSelectionModel> selectionModel = new HashMap<>();

    /**
     * constructor
     *
     * @param world                   the graphics world
     * @param listUpdaterListener     the list updater
     * @param redrawListener          the redraw listener
     * @param databaseListener        the database listener
     * @param displayer               the displayer
     * @param contextSelectorListener the context selector
     * @param starDisplayPreferences  the star display prefs
     * @param reportGenerator         the report generator
     * @param currentPlot             the current plot
     * @param colorPalette            the color palette
     */
    public StarPlotManager(Xform world,
                           ListUpdaterListener listUpdaterListener,
                           RedrawListener redrawListener,
                           DatabaseListener databaseListener,
                           StellarPropertiesDisplayerListener displayer,
                           ContextSelectorListener contextSelectorListener,
                           StarDisplayPreferences starDisplayPreferences,
                           ReportGenerator reportGenerator,
                           CurrentPlot currentPlot,
                           ColorPalette colorPalette) {

        this.listUpdaterListener = listUpdaterListener;
        this.redrawListener = redrawListener;
        this.databaseListener = databaseListener;
        this.displayer = displayer;
        this.contextSelectorListener = contextSelectorListener;
        this.starDisplayPreferences = starDisplayPreferences;
        this.reportGenerator = reportGenerator;
        this.currentPlot = currentPlot;
        this.colorPalette = colorPalette;

        stellarDisplayGroup.setWhatAmI("Stellar Group");
        world.getChildren().add(stellarDisplayGroup);

        extensionsGroup.setWhatAmI("Star Extensions");
        world.getChildren().add(extensionsGroup);

        labelDisplayGroup.setWhatAmI("Labels");
        world.getChildren().add(labelDisplayGroup);

        politiesDisplayGroup.setWhatAmI("Polities");
        world.getChildren().add(politiesDisplayGroup);

    }

    /**
     * get the plotted stars in view
     *
     * @return the list of star display records
     */
    public List<StarDisplayRecord> getCurrentStarsInView() {
        List<StarDisplayRecord> starsInView = new ArrayList<>();
        for (UUID id : currentPlot.getStarIds()) {
            StarDisplayRecord starDisplayRecord = (StarDisplayRecord) currentPlot.getStar(id).getUserData();
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

    public void setStarDisplayPreferences(StarDisplayPreferences displayPreferences) {
        this.starDisplayPreferences = displayPreferences;
    }

    public void setCivilizationDisplayPreferences(CivilizationDisplayPreferences politiesPreferences) {
        this.politiesPreferences = politiesPreferences;
    }


    public Xform getExtensionsGroup() {
        return extensionsGroup;
    }

    /**
     * clear the stars from the display
     */
    public void clearStars() {

        // remove stars
        stellarDisplayGroup.getChildren().clear();
        labelDisplayGroup.getChildren().clear();
        politiesDisplayGroup.getChildren().clear();

        // remove the extension points to the stars
        extensionsGroup.getChildren().clear();
    }


    public void highlightStar(UUID starId) {
        Label starGroup = currentPlot.getLabelForStar(starId);
        highlightRotator = setRotationAnimation(starGroup);
        highlightRotator.play();
        log.info("mark point");
    }

    private static RotateTransition setRotationAnimation(Label group) {
        RotateTransition rotate = new RotateTransition(
                Duration.seconds(10),
                group
        );
        rotate.setAxis(Rotate.Y_AXIS);
        rotate.setFromAngle(360);
        rotate.setToAngle(0);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.setCycleCount(30);
        return rotate;
    }

    public void toggleStars(boolean starsOn) {
        stellarDisplayGroup.setVisible(starsOn);
        labelDisplayGroup.setVisible(starsOn);
    }

    /**
     * draw a list of stars
     *
     * @param recordList the list of stars
     */
    public void drawStar(List<StarDisplayRecord> recordList, String centerStar, ColorPalette colorPalette) {

        for (StarDisplayRecord star : recordList) {
            drawStar(star, centerStar, colorPalette, starDisplayPreferences);
        }
        createExtensionGroup(recordList, colorPalette.getExtensionColor());

        // there is an active plot on screen
        currentPlot.setPlotActive(true);
    }

    /**
     * draw a star
     *
     * @param record     the star record
     * @param centerStar the name of the center star
     */
    public void drawStar(StarDisplayRecord record,
                         String centerStar,
                         ColorPalette colorPalette,
                         StarDisplayPreferences starDisplayPreferences) {

        currentPlot.addRecord(record.copy());
        currentPlot.setCenterStar(centerStar);

        plotStar(record, centerStar, colorPalette, starDisplayPreferences);

    }

    public void clearPlot() {
        currentPlot.clearStars();
    }

    /**
     * toggle the labels
     *
     * @param labelSetting true is labels should be on
     */
    public void toggleLabels(boolean labelSetting) {
        this.labelsOn = labelSetting;

        // we can only do this if there are plot element on screen
        if (currentPlot.isPlotActive()) {
//            redrawPlot();
            labelDisplayGroup.setVisible(labelSetting);
        }
    }

    public void togglePolities(boolean polities) {
        this.politiesOn = polities;
        log.info("toggle polities: {}", polities);

        // we can only do this if there are plot element on screen
        if (currentPlot.isPlotActive()) {
//            redrawPlot();
            politiesDisplayGroup.setVisible(polities);
        }
    }

    private void redrawPlot() {
        clearPlot();
        clearStars();
        log.info("redrawing plot: labels= {}, polities = {}", labelsOn, politiesOn);
        List<StarDisplayRecord> recordList = currentPlot.getStarDisplayRecordList();
        recordList.forEach(
                starDisplayRecord -> plotStar(
                        starDisplayRecord,
                        currentPlot.getCenterStar(),
                        colorPalette,
                        currentPlot.getStarDisplayPreferences()
                )
        );
        routeManager.plotRoutes(currentPlot.getDataSetDescriptor().getRoutes());
        // re-plot routes
    }

    private void plotStar(StarDisplayRecord record,
                          String centerStar,
                          ColorPalette colorPalette,
                          StarDisplayPreferences starDisplayPreferences) {
        Xform starNode;

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
                    labelsOn,
                    politiesOn);

            // create the extension stem tot he star from the grid
            createExtension(record, colorPalette.getExtensionColor());
        }
        currentPlot.addStar(record.getRecordId(), starNode);

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
    private Xform createCentralPoint(StarDisplayRecord record,
                                     ColorPalette colorPalette,
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

        Xform starNode = new Xform();
        starNode.setId("central");
        starNode.setUserData(record);
        starNode.getChildren().add(star);
        return starNode;
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
    private Xform createStar(StarDisplayRecord record,
                             ColorPalette colorPalette,
                             StarDisplayPreferences starDisplayPreferences,
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

        Xform starNode = new Xform();
        starNode.setId("regularStar");
        starNode.setUserData(record);
        starNode.getChildren().add(star);
        return starNode;
    }


    public Node drawStellarObject(StarDisplayRecord record,
                                  ColorPalette colorPalette,
                                  boolean labelsOn,
                                  boolean politiesOn,
                                  StarDisplayPreferences starDisplayPreferences,
                                  CivilizationDisplayPreferences polityPreferences) {

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
    public Group createStellarShape(StarDisplayRecord record,
                                    ColorPalette colorPalette,
                                    boolean labelsOn,
                                    boolean politiesOn,
                                    CivilizationDisplayPreferences polityPreferences) {

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
            currentPlot.mapLabelToStar(record.getRecordId(), label);
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
    public Label createLabel(StarDisplayRecord record,
                             ColorPalette colorPalette) {
        Label label = new Label(record.getStarName());
        label.setFont(new Font("Arial", 6));
        label.setTextFill(colorPalette.getLabelColor());
        Point3D point3D = record.getCoordinates();
        label.setTranslateX(point3D.getX());
        label.setTranslateY(point3D.getY());
        label.setTranslateZ(point3D.getZ());
        return label;
    }


    /**
     * create a set of extensions for a set of stars
     *
     * @param recordList the list of stars
     */
    private void createExtensionGroup(List<StarDisplayRecord> recordList, Color extensionColor) {
        for (StarDisplayRecord record : recordList) {
            createExtension(record, extensionColor);
        }
    }

    /**
     * create an extension for an added star
     *
     * @param record         the star
     * @param extensionColor the color of the extensions from grid to star
     */
    private void createExtension(StarDisplayRecord record, Color extensionColor) {
        Point3D point3DFrom = record.getCoordinates();
        Point3D point3DTo = new Point3D(point3DFrom.getX(), 0, point3DFrom.getZ());
        double lineWidth = colorPalette.getStemLineWidth();
        Node lineSegment = CustomObjectFactory.createLineSegment(point3DFrom, point3DTo, lineWidth, extensionColor);
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
    private void starClickEventHandler(Node star, ContextMenu starContextMenu, MouseEvent e) {
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
    private ContextMenu createPopup(String name, Node star) {
        final ContextMenu cm = new ContextMenu();

        MenuItem titleItem = new MenuItem(name);
        titleItem.setDisable(true);
        cm.getItems().add(titleItem);

        MenuItem setStarMenuItem = createSetStarMenuitem(star);
        cm.getItems().add(setStarMenuItem);

        MenuItem recenterMenuItem = createRecenterMenuitem(star);
        cm.getItems().add(recenterMenuItem);

        MenuItem jumpSystemMenuItem = createEnterSystemItem(star);
        cm.getItems().add(jumpSystemMenuItem);

        cm.getItems().add(new SeparatorMenuItem());
        MenuItem startRouteMenuItem = createRoutingMenuItem(star);
        cm.getItems().add(startRouteMenuItem);

        MenuItem continueRouteMenuItem = continueRoutingMenuItem(star);
        cm.getItems().add(continueRouteMenuItem);

        MenuItem finishRouteMenuItem = finishRoutingMenuItem(star);
        cm.getItems().add(finishRouteMenuItem);

        MenuItem resetRouteMenuItem = resetRoutingMenuItem();
        cm.getItems().add(resetRouteMenuItem);

        cm.getItems().add(new SeparatorMenuItem());

        MenuItem distanceToMenuItem = distanceReportMenuItem(star);
        cm.getItems().add(distanceToMenuItem);

        cm.getItems().add(new SeparatorMenuItem());

        MenuItem propertiesMenuItem = createShowPropertiesMenuItem(star);
        cm.getItems().add(propertiesMenuItem);

        MenuItem enterNotesItem = createNotesMenuItem(star);
        cm.getItems().add(enterNotesItem);
        MenuItem editPropertiesMenuItem = createEditPropertiesMenuItem(star);
        cm.getItems().add(editPropertiesMenuItem);

        cm.getItems().add(new SeparatorMenuItem());

        MenuItem removeMenuItem = createRemoveMenuItem(star);
        cm.getItems().add(removeMenuItem);

        return cm;
    }

    private MenuItem createSetStarMenuitem(Node star) {
        MenuItem menuItem = new MenuItem("Highlight star");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            highlightStar(starDescriptor.getRecordId());

        });
        return menuItem;
    }

    private MenuItem createNotesMenuItem(Node star) {
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

    private MenuItem distanceReportMenuItem(Node star) {
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

    private MenuItem createRecenterMenuitem(Node star) {
        MenuItem menuItem = new MenuItem("Recenter on this star");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            redrawListener.recenter(starDescriptor);
        });
        return menuItem;
    }

    private MenuItem createRoutingMenuItem(Node star) {
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
            result.ifPresent(routeDescriptor -> routeManager.startRoute(routeDescriptor, starDescriptor));
        });
        return menuItem;
    }

    private MenuItem continueRoutingMenuItem(Node star) {
        MenuItem menuItem = new MenuItem("Continue Route");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            routeManager.continueRoute(starDescriptor);
        });
        return menuItem;
    }

    private MenuItem finishRoutingMenuItem(Node star) {
        MenuItem menuItem = new MenuItem("Finish Route");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            routeManager.finishRoute(starDescriptor);
        });
        return menuItem;
    }


    private MenuItem resetRoutingMenuItem() {
        MenuItem menuItem = new MenuItem("Reset Route");
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
    private MenuItem createRemoveMenuItem(Node star) {
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
    private void removeNode(StarDisplayRecord starDisplayRecord) {
        log.info("Removing object for:" + starDisplayRecord.getStarName());
        databaseListener.removeStar(starDisplayRecord.getRecordId());
    }


    /**
     * create an enter system object
     *
     * @param star the star selected
     * @return the menuitem
     */
    private MenuItem createEnterSystemItem(Node star) {
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
    private MenuItem createEditPropertiesMenuItem(Node star) {
        MenuItem editPropertiesMenuItem = new MenuItem("Edit");
        editPropertiesMenuItem.setOnAction(event -> {
            StarDisplayRecord starDisplayRecord = (StarDisplayRecord) star.getUserData();
            StarDisplayRecord editRecord = editProperties(starDisplayRecord);
            star.setUserData(editRecord);

        });
        return editPropertiesMenuItem;
    }

    /**
     * edit a star in the database
     *
     * @param starDisplayRecord the properties to edit
     */
    private StarDisplayRecord editProperties(StarDisplayRecord starDisplayRecord) {
        AstrographicObject starObject = databaseListener.getStar(starDisplayRecord.getRecordId());
        StarEditDialog starEditDialog = new StarEditDialog(starObject);
        Optional<StarEditStatus> optionalStarDisplayRecord = starEditDialog.showAndWait();
        if (optionalStarDisplayRecord.isPresent()) {
            StarEditStatus status = optionalStarDisplayRecord.get();
            if (status.isChanged()) {
                AstrographicObject record = status.getRecord();
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
    private MenuItem createShowPropertiesMenuItem(Node star) {
        MenuItem propertiesMenuItem = new MenuItem("Properties");
        propertiesMenuItem.setOnAction(event -> {
            StarDisplayRecord starDisplayRecord = (StarDisplayRecord) star.getUserData();
            AstrographicObject astrographicObject = databaseListener.getStar(starDisplayRecord.getRecordId());
            displayProperties(astrographicObject);
        });
        return propertiesMenuItem;
    }


    /**
     * display properties for this star
     *
     * @param astrographicObject the properties to display
     */
    private void displayProperties(AstrographicObject astrographicObject) {
        log.info("Showing properties in side panes for:" + astrographicObject.getDisplayName());
        if (displayer != null) {
            displayer.displayStellarProperties(astrographicObject);
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

}
