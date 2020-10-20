package com.teamgannon.trips.floating;

import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.graphics.CurrentPlot;
import com.teamgannon.trips.listener.*;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import static org.fxyz3d.geometry.MathUtils.clamp;

@Slf4j
public class InterstellarExample extends Pane {

    private final ColorPalette colorPalette;
    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;

    private final Font font = new Font("arial", 10);

    // We'll use custom Rotate transforms to manage the coordinate conversions
    private final Rotate rotateX = new Rotate(25, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(25, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    private final Group root = new Group();
    private final Group world = new Group();  //all 3D nodes in scene

    private final SubScene subScene;

    private final StarPlotterManagerExample starPlotterManagerExample;

    private final RouteManagerExample routeManagerExample;

    private final TransitManagerExample transitManagerExample;

    private final GridPlotManagerExample gridPlotManagerExample;
    private ListUpdaterListener listUpdaterListener;
    private RedrawListener redrawListener;
    private DatabaseListener databaseListener;
    private StellarPropertiesDisplayerListener displayer;
    private ContextSelectorListener contextSelectorListener;
    private StarDisplayPreferences starDisplayPreferences;
    private ReportGenerator reportGenerator;
    private CurrentPlot currentPlot;


    public InterstellarExample(double sceneWidth,
                               double sceneHeight,
                               double depth,
                               double spacing,
                               TripsContext tripsContext,
                               RouteUpdaterListener routeUpdaterListener,
                               ListUpdaterListener listUpdaterListener,
                               StellarPropertiesDisplayerListener displayer,
                               DatabaseListener databaseListener,
                               ContextSelectorListener contextSelectorListener,
                               RedrawListener redrawListener,
                               ReportGenerator reportGenerator) {

        this.listUpdaterListener = listUpdaterListener;
        this.redrawListener = redrawListener;
        this.databaseListener = databaseListener;
        this.displayer = displayer;
        this.contextSelectorListener = contextSelectorListener;
        this.reportGenerator = reportGenerator;

        currentPlot = new CurrentPlot();
        currentPlot.setStarDisplayPreferences(starDisplayPreferences);

        this.colorPalette = tripsContext.getAppViewPreferences().getColorPallete();
        this.starDisplayPreferences = tripsContext.getAppViewPreferences().getStarDisplayPreferences();

        // attach our custom rotation transforms so we can update the labels dynamically
        world.getTransforms().addAll(rotateX, rotateY, rotateZ);

        subScene = new SubScene(world, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        PerspectiveCamera camera = setPerspectiveCamera();

        subScene.setCamera(camera);
        Group sceneRoot = new Group(subScene);

        handleMouseEvents();

        // add to the 2D portion of this component
        Pane pane = new Pane();
        pane.setPrefSize(sceneWidth, sceneHeight);
        pane.setMaxSize(Pane.USE_COMPUTED_SIZE, Pane.USE_COMPUTED_SIZE);
        pane.setMinSize(Pane.USE_COMPUTED_SIZE, Pane.USE_COMPUTED_SIZE);
        pane.setBackground(Background.EMPTY);
        pane.getChildren().add(sceneRoot);
        pane.setPickOnBounds(false);

        subScene.widthProperty().bind(pane.widthProperty());
        subScene.heightProperty().bind(pane.heightProperty());
        Platform.runLater(this::updateLabels);
        root.getChildren().add(pane);

        this.starPlotterManagerExample = new StarPlotterManagerExample(
                sceneRoot,
                world,
                subScene,
                listUpdaterListener,
                redrawListener,
                databaseListener,
                displayer,
                contextSelectorListener,
                new StarDisplayPreferences(),
                reportGenerator,
                new CurrentPlot(),
                colorPalette
        );

        this.gridPlotManagerExample = new GridPlotManagerExample(
                world,
                sceneRoot,
                subScene,
                spacing, sceneWidth, depth,
                colorPalette);

        this.routeManagerExample = new RouteManagerExample(
                world,
                sceneRoot,
                subScene,
                routeUpdaterListener,
                currentPlot
        );

        this.transitManagerExample = new TransitManagerExample(
                world,
                sceneRoot,
                subScene,
                routeUpdaterListener
        );


        // just for simulation
        starPlotterManagerExample.generateRandomStars(50);

    }

    @NotNull
    private PerspectiveCamera setPerspectiveCamera() {

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-1500);
        return camera;
    }

    public Group getRoot() {
        return root;
    }

    private void handleMouseEvents() {
        subScene.setOnMousePressed((MouseEvent me) -> {
                    mousePosX = me.getSceneX();
                    mousePosY = me.getSceneY();
                    mouseOldX = me.getSceneX();
                    mouseOldY = me.getSceneY();
                }
        );

        subScene.setOnMouseDragged((MouseEvent me) -> {
                    mouseOldX = mousePosX;
                    mouseOldY = mousePosY;
                    mousePosX = me.getSceneX();
                    mousePosY = me.getSceneY();
                    mouseDeltaX = (mousePosX - mouseOldX);
                    mouseDeltaY = (mousePosY - mouseOldY);
                    double modifier = 5.0;
                    double modifierFactor = 0.1;

                    if (me.isPrimaryButtonDown()) {
                        if (me.isAltDown()) { //roll
                            rotateZ.setAngle(((rotateZ.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180); // +
                        } else {
                            rotateY.setAngle(((rotateY.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180); // +
                            rotateX.setAngle(
                                    clamp(
                                            (((rotateX.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180),
                                            -60,
                                            60
                                    )
                            ); // -
                        }
                    }
                    updateLabels();
                }
        );
    }

    private void updateLabels() {
        starPlotterManagerExample.updateLabels();

        gridPlotManagerExample.updateScale();
        gridPlotManagerExample.updateLabels();

        routeManagerExample.updateLabels();

        transitManagerExample.updateLabels();
    }

}
