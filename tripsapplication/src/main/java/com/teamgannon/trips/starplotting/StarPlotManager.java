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
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.graphics.panes.StarSelectionModel;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.listener.*;
import com.teamgannon.trips.objects.MeshViewShapeFactory;
import com.teamgannon.trips.routing.RouteManager;
import com.teamgannon.trips.routing.model.RoutingType;
import com.teamgannon.trips.routing.dialogs.ContextAutomatedRoutingDialog;
import com.teamgannon.trips.routing.dialogs.ContextManualRoutingDialog;
import com.teamgannon.trips.screenobjects.StarEditDialog;
import com.teamgannon.trips.screenobjects.StarEditStatus;
import com.teamgannon.trips.solarsystem.PlanetDialog;
import com.teamgannon.trips.solarsystem.SolarSystemGenOptions;
import com.teamgannon.trips.solarsystem.SolarSystemGenerationDialog;
import com.teamgannon.trips.solarsystem.SolarSystemReport;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
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
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Modality;
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

    private ScaleTransition scaleTransition;

    private TransitionState transitionState;


    private final TripsContext tripsContext;
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

    private RotateTransition centralRotator = new RotateTransition();

    private Node highLightStar;


    private StarDisplayPreferences starDisplayPreferences;

    private final static String CENTRAL_STAR = "centralStar";
    private final static String MORAVIAN_STAR = "moravianStar";
    private final static String FOUR_PT_STAR = "4PtStar";
    private final static String FIVE_PT_STAR = "5PtStar";
    private final static String PYRAMID = "pyramid";
    private final static String POLITY_TERRAN = "polity_1";
    private final static String POLITY_DORNANI = "polity_2";
    private final static String POLITY_KTOR = "polity_3";
    private final static String POLITY_ARAT_KUR = "polity_4";
    private final static String POLITY_HKH_RKH = "polity_5";

    private final Map<String, MeshObjectDefinition> specialObjects = new HashMap<>();

    private final MeshViewShapeFactory meshViewShapeFactory = new MeshViewShapeFactory();

    private ContextAutomatedRoutingDialog automatedRoutingDialog;

    private ContextManualRoutingDialog manualRoutingDialog;

    private DataSetDescriptor currentDataSet;

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

        // special graphical objects in MeshView format
        loadSpecialObjects();

    }

    private void loadSpecialObjects() {

        // load central star
        Group centralStar = meshViewShapeFactory.starCentral();
        if (centralStar != null) {
            MeshObjectDefinition objectDefinition = MeshObjectDefinition
                    .builder()
                    .name(CENTRAL_STAR)
                    .id(UUID.randomUUID())
                    .object(centralStar)
                    .xScale(30)
                    .yScale(30)
                    .zScale(30)
                    .axis(Rotate.X_AXIS)
                    .rotateAngle(90)
                    .build();
            specialObjects.put(CENTRAL_STAR, objectDefinition);
        } else {
            log.error("Unable to load the central star object");
        }

        // load 4 pt star star
        Node fourPtStar = meshViewShapeFactory.star4pt();
        if (fourPtStar != null) {
            MeshObjectDefinition objectDefinition = MeshObjectDefinition
                    .builder()
                    .name(FOUR_PT_STAR)
                    .id(UUID.randomUUID())
                    .object(fourPtStar)
                    .xScale(30)
                    .yScale(30)
                    .zScale(30)
                    .axis(Rotate.X_AXIS)
                    .rotateAngle(90)
                    .build();
            specialObjects.put(FOUR_PT_STAR, objectDefinition);
        } else {
            log.error("Unable to load the 4 pt star object");
        }

        // load 5 pt star star
        Group fivePtStar = meshViewShapeFactory.star5pt();
        if (fourPtStar != null) {
            MeshObjectDefinition objectDefinition = MeshObjectDefinition
                    .builder()
                    .name(FIVE_PT_STAR)
                    .id(UUID.randomUUID())
                    .object(fivePtStar)
                    .xScale(30)
                    .yScale(30)
                    .zScale(30)
                    .axis(Rotate.X_AXIS)
                    .rotateAngle(90)
                    .build();
            specialObjects.put(FIVE_PT_STAR, objectDefinition);
        } else {
            log.error("Unable to load the 5 pt star object");
        }


        // load 5 pt star star
        MeshView pyramid = meshViewShapeFactory.pyramid();
        if (pyramid != null) {
            MeshObjectDefinition objectDefinition = MeshObjectDefinition
                    .builder()
                    .name(PYRAMID)
                    .id(UUID.randomUUID())
                    .object(pyramid)
                    .xScale(10)
                    .yScale(10)
                    .zScale(10)
                    .axis(Rotate.X_AXIS)
                    .rotateAngle(-90)
                    .build();
            specialObjects.put(PYRAMID, objectDefinition);
        } else {
            log.error("Unable to load the 5 pt star object");
        }

        MeshView geometric0 = meshViewShapeFactory.geometric0();
        if (geometric0 != null) {
            MeshObjectDefinition objectDefinition = MeshObjectDefinition
                    .builder()
                    .name("geometric0")
                    .id(UUID.randomUUID())
                    .object(geometric0)
                    .xScale(10)
                    .yScale(10)
                    .zScale(10)
                    .axis(Rotate.X_AXIS)
                    .rotateAngle(-90)
                    .build();
            specialObjects.put("geometric0", objectDefinition);
        } else {
            log.error("Unable to load the geometric object");
        }

        log.info("All MeshView objects loaded");

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
        // remove old star
        if (highLightStar != null) {
            stellarDisplayGroup.getChildren().remove(highLightStar);
        }
        // now create new one
        Node starShape = tripsContext.getCurrentPlot().getStar(starId);
        StarDisplayRecord record = (StarDisplayRecord) starShape.getUserData();
        Color color = record.getStarColor();

        // make highLight star same as under lying one, with star record and context menu
        highLightStar = createHighlightStar(color);
        if (highLightStar != null) {
            highLightStar.setUserData(record);
            setContextMenu(record, highLightStar);

            // superimpose this highlight over top of star
            Point3D point3D = record.getCoordinates();
            highLightStar.setTranslateX(point3D.getX());
            highLightStar.setTranslateY(point3D.getY());
            highLightStar.setTranslateZ(point3D.getZ());
            highLightStar.setVisible(true);
            stellarDisplayGroup.getChildren().add(highLightStar);

            // now blink for 100 cycles
            log.info("starting blink");
            blinkStar(highLightStar, 100);

            log.info("mark point");
        }
    }

    private void blinkStar(Node starShape, int cycleCount) {
        if (scaleTransition != null) {

            log.info("stop old fade transition");
            scaleTransition.stop();
            if (transitionState != null) {
                Node node = transitionState.getNode();
                node.setScaleX(transitionState.getXScale());
                node.setScaleY(transitionState.getYScale());
                node.setScaleZ(transitionState.getZScale());
            }
        }
        log.info("create new transition");

        scaleTransition = new ScaleTransition(Duration.seconds(2), starShape);
        double xScale = starShape.getScaleX();

        double yScale = starShape.getScaleY();
        double zScale = starShape.getScaleZ();

        scaleTransition.setFromX(xScale * 2);
        scaleTransition.setFromY(yScale * 2);
        scaleTransition.setFromZ(zScale * 2);
        scaleTransition.setToX(xScale / 2);
        scaleTransition.setToY(yScale / 2);
        scaleTransition.setToZ(zScale / 2);

        scaleTransition.setCycleCount(cycleCount);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setOnFinished(e -> {
            log.info("highlight star expiring and will be removed");
            stellarDisplayGroup.getChildren().remove(starShape);
        });
        scaleTransition.play();
        transitionState = new TransitionState(starShape, xScale, yScale, zScale);

    }


    /**
     * the label to blink
     *
     * @param label      the label to blink
     * @param cycleCount the number of times on a 1 second interval to perform. Null is infinite
     */
    private void blinkStarLabel(Label label, int cycleCount) {
//        if (scaleTransition != null) {
//            scaleTransition.stop();
//        }
//        scaleTransition = new FadeTransition(Duration.seconds(1), label);
//        scaleTransition.setFromValue(1.0);
//        scaleTransition.setToValue(0.0);
//        scaleTransition.setCycleCount(cycleCount);
//        scaleTransition.setAutoReverse(true);
//        scaleTransition.play();
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

        Node starNode = createStar(
                record,
                colorPalette,
                starDisplayPreferences,
                politiesPreferences,
                record.isDisplayLabel(),
                politiesOn);

        // create the extension stem tot he star from the grid
        createExtension(record);

        tripsContext.getCurrentPlot().addStar(record.getRecordId(), starNode);

        // draw the star on the pane
        stellarDisplayGroup.getChildren().add(starNode);
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
                record.isCenter(),
                labelsOn,
                politiesOn,
                starDisplayPreferences,
                politiesPreferences);

        String polity = record.getPolity();
        if (polity.equals("NA")) {
            polity = "Non-Aligned";
        }
        Tooltip tooltip = new Tooltip(record.getStarName() + "::" + polity);
        Tooltip.install(star, tooltip);

        if (listUpdaterListener != null) {
            listUpdaterListener.updateList(record);
        }

        star.setId("regularStar");
        star.setUserData(record);

        return star;
    }

    /**
     * create a stellar object
     *
     * @param record            the star record
     * @param isCenter          flag that indicates that it is in the center
     * @param colorPalette      the color palette to use
     * @param labelsOn          are labels on?
     * @param politiesOn        are polities on?
     * @param polityPreferences the polity prefs
     * @return the created object
     */
    public @NotNull Node drawStellarObject(@NotNull StarDisplayRecord record,
                                           @NotNull ColorPalette colorPalette,
                                           boolean isCenter,
                                           boolean labelsOn,
                                           boolean politiesOn,
                                           StarDisplayPreferences starDisplayPreferences,
                                           @NotNull CivilizationDisplayPreferences polityPreferences) {

        final PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(record.getStarColor());
        material.setSpecularColor(record.getStarColor());
        Node starShape;
        if (isCenter) {
            starShape = createCentralStar();
        } else {
            Sphere sphere = new Sphere(record.getRadius() * GRAPHICS_FUDGE_FACTOR);
            sphere.setMaterial(material);
            starShape = sphere;
        }
        Point3D point3D = record.getCoordinates();
        starShape.setTranslateX(point3D.getX());
        starShape.setTranslateY(point3D.getY());
        starShape.setTranslateZ(point3D.getZ());

        if (labelsOn) {
            Label label = createLabel(record, colorPalette);
            label.setLabelFor(starShape);
            labelDisplayGroup.getChildren().add(label);
            tripsContext.getCurrentPlot().mapLabelToStar(record.getRecordId(), label);
            shapeToLabel.put(starShape, label);
        }

        if (politiesOn) {
            if (!record.getPolity().equals("NA") && !record.getPolity().isEmpty()) {

                MeshView polityObject = getPolityObject(record.getPolity(), polityPreferences);

                // attach polity object
                polityObject.setTranslateX(point3D.getX());
                polityObject.setTranslateY(point3D.getY());
                polityObject.setTranslateZ(point3D.getZ());

                // attach a context menu
                setContextMenu(record, polityObject);

                // set this polity object to the polities display
                politiesDisplayGroup.getChildren().add(polityObject);
                politiesDisplayGroup.setVisible(true);
            } else {
                // set context menu
                setContextMenu(record, starShape);
                log.debug("No polity to plot");
            }

            // set polities to be visible
            politiesDisplayGroup.setVisible(true);
        } else {
            // set context menu
            setContextMenu(record, starShape);
        }
        return starShape;
    }


    private MeshView getPolityObject(String polity, CivilizationDisplayPreferences polityPreferences) {
        MeshObjectDefinition meshObjectDefinition = MeshObjectDefinition.builder().build();
        Color polityColor = polityPreferences.getColorForPolity(polity);
        switch (polity) {
            case CivilizationDisplayPreferences.TERRAN, CivilizationDisplayPreferences.SLAASRIITHI -> {
                meshObjectDefinition = createDornaniPolity();
            }
            case CivilizationDisplayPreferences.DORNANI, CivilizationDisplayPreferences.OTHER1 -> {
                meshObjectDefinition = createDornaniPolity();
            }
            case CivilizationDisplayPreferences.KTOR, CivilizationDisplayPreferences.OTHER3 -> {
                meshObjectDefinition = createDornaniPolity();
            }
            case CivilizationDisplayPreferences.ARAKUR, CivilizationDisplayPreferences.OTHER2 -> {
                meshObjectDefinition = createDornaniPolity();
            }
            case CivilizationDisplayPreferences.HKHRKH, CivilizationDisplayPreferences.OTHER4 -> {
                meshObjectDefinition = createDornaniPolity();
            }
            default -> {
                log.error("unknown polity");
            }
        }
        MeshView polityObject = (MeshView) meshObjectDefinition.getObject();
        if (polityObject != null) {
            // set color
            PhongMaterial material = (PhongMaterial) polityObject.getMaterial();
            material.setDiffuseColor(polityColor);
            material.setSpecularColor(polityColor);

            // set size and orient
            polityObject.setScaleX(meshObjectDefinition.getXScale());
            polityObject.setScaleY(meshObjectDefinition.getYScale());
            polityObject.setScaleZ(meshObjectDefinition.getZScale());
            polityObject.setRotationAxis(meshObjectDefinition.getAxis());
            polityObject.setRotate(meshObjectDefinition.getRotateAngle());
        } else {
            log.error("polity object is null: {}", meshObjectDefinition);
        }
        return polityObject;
    }

    private Node createCentralStar() {
        MeshObjectDefinition meshObjectDefinition = specialObjects.get(CENTRAL_STAR);
        Node centralStar = meshObjectDefinition.getObject();
        centralStar.setScaleX(meshObjectDefinition.getXScale());
        centralStar.setScaleY(meshObjectDefinition.getYScale());
        centralStar.setScaleZ(meshObjectDefinition.getZScale());
        centralStar.setRotationAxis(meshObjectDefinition.getAxis());
        centralStar.setRotate(meshObjectDefinition.getRotateAngle());
        return centralStar;
    }

    private void setContextMenu(@NotNull StarDisplayRecord record, Node star) {
        star.setUserData(record);
        String polity = record.getPolity();
        if (polity.equals("NA")) {
            polity = "Non-aligned";
        }
        ContextMenu starContextMenu = createPopup(record.getStarName() + " (" + polity + ")", star);
        star.addEventHandler(
                MouseEvent.MOUSE_CLICKED,
                e -> starClickEventHandler(star, starContextMenu, e));
        star.setOnMousePressed(event -> {
            Node node = (Node) event.getSource();
            StarDisplayRecord starDescriptor = (StarDisplayRecord) node.getUserData();
            log.info("mouse click detected! " + starDescriptor);
        });
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
        double zZero = tripsContext.getCurrentPlot().getCenterCoordinates()[2];
        Point3D point3DFrom = record.getCoordinates();
        Point3D point3DTo = new Point3D(point3DFrom.getX(), point3DFrom.getY(), zZero);
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
            if (routeManager.isManualRoutingActive()) {
                StarDisplayRecord record = (StarDisplayRecord) star.getUserData();
                if (routeManager.getRoutingType().equals(RoutingType.MANUAL)) {
                    if (manualRoutingDialog != null) {
                        manualRoutingDialog.addStar(record);
                    }
                }
                if (routeManager.getRoutingType().equals(RoutingType.AUTOMATIC)) {
                    if (automatedRoutingDialog != null) {
                        automatedRoutingDialog.setToStar(record.getStarName());
                    }
                }
            } else {
                starContextMenu.show(star, e.getScreenX(), e.getScreenY());
            }
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

        MenuItem removeMenuItem = createRemoveMenuItem(star);
        cm.getItems().add(removeMenuItem);

        cm.getItems().add(new SeparatorMenuItem());
        MenuItem routingHeader = new MenuItem("Routing");
        routingHeader.setStyle(" -fx-font-size:15; -fx-font-weight: bold");
        routingHeader.setDisable(true);
        cm.getItems().add(routingHeader);

        MenuItem automatedRouteMenuItem = createAutomatedRoutingMenuItem(star);
        cm.getItems().add(automatedRouteMenuItem);

        MenuItem manualRouteMenuItem = createManualRoutingMenuItem(star);
        cm.getItems().add(manualRouteMenuItem);

        cm.getItems().add(new SeparatorMenuItem());

        MenuItem distanceToMenuItem = distanceReportMenuItem(star);
        cm.getItems().add(distanceToMenuItem);

        return cm;
    }

    private MenuItem createManualRoutingMenuItem(Node star) {
        MenuItem menuItem = new MenuItem("Build route on screen by clicking stars");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            generateManualRoute(starDescriptor);

        });
        return menuItem;
    }

    private MenuItem createAutomatedRoutingMenuItem(Node star) {
        MenuItem menuItem = new MenuItem("Run route finder/generator");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            generateAutomatedRoute(starDescriptor);

        });
        return menuItem;
    }

    private void generateAutomatedRoute(StarDisplayRecord starDescriptor) {
        log.info("generate automated route");
        automatedRoutingDialog = new ContextAutomatedRoutingDialog(
                this, routeManager, currentDataSet, starDescriptor, getCurrentStarsInView());

        automatedRoutingDialog.initModality(Modality.NONE);
        automatedRoutingDialog.show();
        // set the state for the routing so that clicks on stars don't invoke the context menu
        routeManager.setManualRoutingActive(true);
        routeManager.setRoutingType(RoutingType.AUTOMATIC);
    }

    private void generateManualRoute(StarDisplayRecord starDescriptor) {
        log.info("generate manual route");
        manualRoutingDialog = new ContextManualRoutingDialog(
                routeManager,
                currentDataSet,
                starDescriptor
        );
        manualRoutingDialog.initModality(Modality.NONE);
        manualRoutingDialog.show();
        // set the state for the routing so that clicks on stars don't invoke the context menu
        routeManager.setManualRoutingActive(true);
        routeManager.setRoutingType(RoutingType.MANUAL);

    }

    private @NotNull MenuItem createHighlightStarMenuitem(@NotNull Node star) {
        MenuItem menuItem = new MenuItem("Highlight star");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            highlightStar(starDescriptor.getRecordId());

        });
        return menuItem;
    }

    private @NotNull MenuItem distanceReportMenuItem(@NotNull Node star) {
        MenuItem menuItem = new MenuItem("Generate distance report from this star");
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
        MenuItem removeMenuItem = new MenuItem("Delete star");
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
     * crate a menuitem to edit a targeted item
     *
     * @return the menuitem supporting this action
     */
    private @NotNull MenuItem createEditPropertiesMenuItem(@NotNull Node star) {
        MenuItem editPropertiesMenuItem = new MenuItem("Edit star");
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
                StarDisplayRecord record1 = StarDisplayRecord.fromStarObject(record, starDisplayPreferences);
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

    private void createExtension(double x, double y, double z, Color extensionColor) {
        Point3D point3DFrom = new Point3D(x, y, z);
        Point3D point3DTo = new Point3D(point3DFrom.getX(), point3DFrom.getY(), 0);
        double lineWidth = 0.3;
        Node lineSegment = CustomObjectFactory.createLineSegment(point3DFrom, point3DTo, lineWidth, colorPalette.getExtensionColor(), colorPalette.getLabelFont().toFont());
        extensionsGroup.getChildren().add(lineSegment);
        // add the extensions group to the world model
        extensionsGroup.setVisible(true);
    }


    public void setControlPaneOffset(double controlPaneOffset) {
        this.controlPaneOffset = controlPaneOffset;
    }


    ///////////


    private MeshObjectDefinition createTerranPolity() {
        // load cube as Terran polity
        MeshView polity1 = meshViewShapeFactory.cube();
        if (polity1 != null) {
            return MeshObjectDefinition
                    .builder()
                    .name(POLITY_TERRAN)
                    .id(UUID.randomUUID())
                    .object(polity1)
                    .xScale(1)
                    .yScale(1)
                    .zScale(1)
                    .axis(Rotate.X_AXIS)
                    .rotateAngle(90)
                    .build();
        } else {
            log.error("Unable to load the polity 1 object");
            return MeshObjectDefinition.builder().build();
        }
    }

    public MeshObjectDefinition createDornaniPolity() {
        // load tetrahedron as polity 2
        MeshView polity2 = meshViewShapeFactory.tetrahedron();
        if (polity2 != null) {
            return MeshObjectDefinition
                    .builder()
                    .name(POLITY_DORNANI)
                    .id(UUID.randomUUID())
                    .object(polity2)
                    .xScale(1)
                    .yScale(1)
                    .zScale(1)
                    .axis(Rotate.X_AXIS)
                    .rotateAngle(90)
                    .build();
        } else {
            log.error("Unable to load the polity 2 object");
            return MeshObjectDefinition.builder().build();
        }
    }

    /**
     * create a highlight star
     *
     * @param color the color to display it as (used to match the star)
     * @return the star to display
     */
    private Node createHighlightStar(Color color) {
        // load the moravian star
        // we have to do this each time because it has to unique
        Group highLightStar = meshViewShapeFactory.starMoravian();
        if (highLightStar != null) {

            // extract the various meshviews and set the color to match
            // we need to do this because the moravian object is a group of mesh objects and
            // we need set the material color on each one.
            for (Node node : highLightStar.getChildren()) {
                MeshView meshView = (MeshView) node;
                PhongMaterial material = (PhongMaterial) meshView.getMaterial();
                material.setSpecularColor(color);
                material.setDiffuseColor(color);
            }

            // now scale it and set it to show properly
            highLightStar.setScaleX(30);
            highLightStar.setScaleY(30);
            highLightStar.setScaleZ(30);
            highLightStar.setRotationAxis(Rotate.X_AXIS);
            highLightStar.setRotate(90);
            return highLightStar;
        } else {
            log.error("Unable to load the moravian star object");
            return null;
        }

    }

    /////////////////////////////////////////////////////////////////////

    private @NotNull MenuItem createRoutingMenuItem(@NotNull Node star) {
        MenuItem menuItem = new MenuItem("Start Route");
        menuItem.setOnAction(event -> {
            boolean routingActive = routeManager.isManualRoutingActive();
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


    private MenuItem createGenerateSolarSystemItem(Node star) {
        MenuItem menuItem = new MenuItem("Generate Solar System");
        menuItem.setOnAction(event -> {
            StarDisplayRecord starDescriptor = (StarDisplayRecord) star.getUserData();
            generateSolarSystem(starDescriptor);

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


    private MeshObjectDefinition createKtorPolity() {
        // load icosahedron as polity 3
        MeshView polity3 = meshViewShapeFactory.icosahedron();
        if (polity3 != null) {
            return MeshObjectDefinition
                    .builder()
                    .name(POLITY_KTOR)
                    .id(UUID.randomUUID())
                    .object(polity3)
                    .xScale(1)
                    .yScale(1)
                    .zScale(1)
                    .axis(Rotate.X_AXIS)
                    .rotateAngle(90)
                    .build();
        } else {
            log.error("Unable to load the polity 3 object");
            return MeshObjectDefinition.builder().build();
        }
    }

    private MeshObjectDefinition createAratKurPolity() {
        // load icosahedron as polity 4
        MeshView polity4 = meshViewShapeFactory.octahedron();
        if (polity4 != null) {
            return MeshObjectDefinition
                    .builder()
                    .name(POLITY_ARAT_KUR)
                    .id(UUID.randomUUID())
                    .object(polity4)
                    .xScale(1)
                    .yScale(1)
                    .zScale(1)
                    .axis(Rotate.X_AXIS)
                    .rotateAngle(90)
                    .build();
        } else {
            log.error("Unable to load the polity 4 object");
            return MeshObjectDefinition.builder().build();
        }
    }

    private MeshObjectDefinition createHkhRkhPolity() {
        // load icosahedron as polity 5
        MeshView polity5 = meshViewShapeFactory.dodecahedron();
        if (polity5 != null) {
            return MeshObjectDefinition
                    .builder()
                    .name(POLITY_HKH_RKH)
                    .id(UUID.randomUUID())
                    .object(polity5)
                    .xScale(1)
                    .yScale(1)
                    .zScale(1)
                    .axis(Rotate.X_AXIS)
                    .rotateAngle(90)
                    .build();
        } else {
            log.error("Unable to load the polity 5 object");
            return MeshObjectDefinition.builder().build();
        }
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

    public void clearRoutingFlag() {
        routeManager.setManualRoutingActive(false);
    }

    public void setDataSetContext(DataSetDescriptor descriptor) {
        this.currentDataSet = descriptor;
    }


//    private void setupRotateAnimation(Node node) {
//        centralRotator.setNode(node);
//        centralRotator.setAxis(Rotate.Y_AXIS);
//        centralRotator.setDuration(Duration.INDEFINITE);
//        centralRotator.play();
//    }
//
//
//    private void setupFade(Node node) {
//        FadeTransition fader = new FadeTransition(Duration.seconds(5), node);
//        fader.setFromValue(1.0);
//        fader.setToValue(0.1);
//        fader.setCycleCount(Timeline.INDEFINITE);
//        fader.setAutoReverse(true);
//        fader.play();
//    }

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


    public void setManualRouting(ContextManualRoutingDialog manualRoutingDialog) {
        this.manualRoutingDialog = manualRoutingDialog;
    }

}
