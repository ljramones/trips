package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.listener.ContextSelectorListener;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.transform.Rotate;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static org.fxyz3d.geometry.MathUtils.clamp;

/**
 * This is used to display a solar system
 * <p>
 * Created by larrymitchell on 2017-02-05.
 */
public class SolarSystemSpacePane extends Pane {

    // mouse positions
    private double mousePosX, mousePosY = 0;
    private double mouseOldX, mouseOldY = 0;
    private double mouseDeltaX, mouseDeltaY = 0;

    private final Rotate rotateX = new Rotate(25, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(25, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    private final Group world = new Group();

    private Group root = new Group();

    private SubScene subScene;

    PerspectiveCamera camera = new PerspectiveCamera(true);

    private static final double ROTATE_SECS = 60;

    /**
     * animation rotator
     */
    private RotateTransition rotator;

    /**
     * animation toggle
     */
    private boolean animationPlay = false;

    private Label starNameLabel;

    private final Group starNameGroup = new Group();

    /**
     * the universe model which holds detail about our pocket universe
     */
    private final Universe universe = new Universe();

    private String systemName = "No System Selected";
    private Button returnButton;
    private StarDisplayRecord starDisplayRecord;
    private ContextSelectorListener contextSelectorListener;
    private double depth;


    public SolarSystemSpacePane(double sceneWidth,
                                double sceneHeight,
                                double depth) {

        this.depth = depth;

        // attach our custom rotation transforms so we can update the labels dynamically
        world.getTransforms().addAll(rotateX, rotateY, rotateZ);

        subScene = new SubScene(world, sceneWidth, sceneHeight, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        setInitialView();

        subScene.setCamera(camera);
        Group sceneRoot = new Group(subScene);

        sceneRoot.getChildren().add(starNameGroup);

        this.setBackground(Background.EMPTY);
        this.getChildren().add(sceneRoot);
        this.setPickOnBounds(false);

        subScene.widthProperty().bind(this.widthProperty());
        subScene.heightProperty().bind(this.heightProperty());

        root.getChildren().add(this);

        handleMouseEvents();
    }

    /**
     * set the initial view
     */
    public void setInitialView() {
        setPerspectiveCamera();
    }


    private void setPerspectiveCamera() {
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);
        camera.setTranslateZ(-1600);
    }

    /**
     * handle the mouse events
     */
    private void handleMouseEvents() {

        subScene.setOnScroll((ScrollEvent event) -> {
            double deltaY = event.getDeltaY();
            zoomGraph(deltaY * 5);
            updateLabels();
        });

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
                    double modifier = 1.0;
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

    /**
     * do actual zoom
     *
     * @param zoomAmt the amount to zoom
     */
    private void zoomGraph(double zoomAmt) {
        double z = camera.getTranslateZ();
        double newZ = z - zoomAmt;
        camera.setTranslateZ(newZ);
    }


    public void updateLabels() {

    }


    /**
     * set the system to show
     *
     * @param starDisplayRecord object properties of this system
     */
    public void setSystemToDisplay(@NotNull StarDisplayRecord starDisplayRecord) {
        this.starDisplayRecord = starDisplayRecord;
        systemName = starDisplayRecord.getStarName();
        createScaleLegend(systemName);
    }

    /**
     * used to draw the target System
     */
    public void render() {

    }

    // ---------------------- helpers -------------------------- //


    private void createScaleLegend(String starName) {

        GridPane titlePane = new GridPane();
        titlePane.setPrefWidth(450);
        starNameGroup.getChildren().add(titlePane);

        starNameLabel = new Label(starName);
        starNameLabel.setFont(Font.font("Verdana", FontPosture.ITALIC, 20));
        starNameLabel.setTextFill(Color.WHEAT);

        titlePane.add(starNameLabel, 0, 0);

        Separator separator1 = new Separator();
        separator1.setMinWidth(40.);
        titlePane.add(separator1, 1, 0);

        // setup return button to jump back to interstellar space
        returnButton = new Button("Jump Back");
        returnButton.setOnAction(e -> jumpBackToInterstellarSpace());
        titlePane.add(returnButton, 2, 0);

        titlePane.setTranslateX(subScene.getWidth() - 430);
        titlePane.setTranslateY(subScene.getHeight() - 30);
        titlePane.setTranslateZ(0);

    }

    private void jumpBackToInterstellarSpace() {
        // there is no specific context at the moment.  We assume the same interstellar space we came form
        contextSelectorListener.selectInterstellarSpace(new HashMap<>());
    }

    public void setContextUpdater(ContextSelectorListener contextSelectorListener) {
        this.contextSelectorListener = contextSelectorListener;
    }
}
