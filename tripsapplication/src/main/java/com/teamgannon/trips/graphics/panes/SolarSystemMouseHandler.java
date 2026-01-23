package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import com.teamgannon.trips.solarsystem.SolarSystemContextMenuFactory;
import com.teamgannon.trips.solarsystem.rendering.SolarSystemRenderer;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

/**
 * Handles mouse events for solar system visualization.
 * <p>
 * Handles:
 * <ul>
 *   <li>Scroll events for zooming</li>
 *   <li>Mouse drag for rotation and panning</li>
 *   <li>Click events for selection and context menus</li>
 * </ul>
 */
@Slf4j
public class SolarSystemMouseHandler {

    private final SubScene subScene;
    private final PerspectiveCamera camera;
    private final Rotate rotateX;
    private final Rotate rotateY;
    private final Rotate rotateZ;
    private final Translate worldTranslate;
    private final SolarSystemRenderer renderer;
    private final SolarSystemContextMenuFactory contextMenuFactory;
    private final Runnable updateLabelsCallback;

    // Mouse position tracking
    private double mousePosX, mousePosY = 0;
    private double mouseOldX, mouseOldY = 0;
    private double mouseDeltaX, mouseDeltaY = 0;

    // Current system reference for context menus
    private SolarSystemDescription currentSystem;

    // Callbacks
    private Consumer<ExoPlanet> addPlanetCallback;
    private javafx.scene.Node contextMenuOwner;

    public SolarSystemMouseHandler(SubScene subScene,
                                   PerspectiveCamera camera,
                                   Rotate rotateX,
                                   Rotate rotateY,
                                   Rotate rotateZ,
                                   Translate worldTranslate,
                                   SolarSystemRenderer renderer,
                                   SolarSystemContextMenuFactory contextMenuFactory,
                                   Runnable updateLabelsCallback) {
        this.subScene = subScene;
        this.camera = camera;
        this.rotateX = rotateX;
        this.rotateY = rotateY;
        this.rotateZ = rotateZ;
        this.worldTranslate = worldTranslate;
        this.renderer = renderer;
        this.contextMenuFactory = contextMenuFactory;
        this.updateLabelsCallback = updateLabelsCallback;
    }

    /**
     * Set the context menu owner node for showing menus.
     */
    public void setContextMenuOwner(javafx.scene.Node owner) {
        this.contextMenuOwner = owner;
    }

    /**
     * Set the current solar system for context menu operations.
     */
    public void setCurrentSystem(SolarSystemDescription system) {
        this.currentSystem = system;
    }

    /**
     * Set the callback for adding a planet.
     */
    public void setAddPlanetCallback(Consumer<ExoPlanet> callback) {
        this.addPlanetCallback = callback;
    }

    /**
     * Initialize all mouse event handlers on the subscene.
     */
    public void initialize() {
        subScene.setOnScroll(this::handleScroll);
        subScene.setOnMousePressed(this::handleMousePressed);
        subScene.setOnMouseDragged(this::handleMouseDragged);
        subScene.setOnMouseClicked(this::handleMouseClicked);
    }

    /**
     * Handle scroll events for zooming.
     */
    private void handleScroll(ScrollEvent event) {
        double deltaY = event.getDeltaY();
        zoomGraph(deltaY * 5);
        updateLabelsCallback.run();
    }

    /**
     * Handle mouse pressed for tracking position.
     */
    private void handleMousePressed(MouseEvent me) {
        mousePosX = me.getSceneX();
        mousePosY = me.getSceneY();
        mouseOldX = me.getSceneX();
        mouseOldY = me.getSceneY();
    }

    /**
     * Handle mouse drag for rotation and panning.
     */
    private void handleMouseDragged(MouseEvent me) {
        mouseOldX = mousePosX;
        mouseOldY = mousePosY;
        mousePosX = me.getSceneX();
        mousePosY = me.getSceneY();
        mouseDeltaX = (mousePosX - mouseOldX);
        mouseDeltaY = (mousePosY - mouseOldY);
        double modifier = 1.0;
        double modifierFactor = 0.1;

        // Middle mouse button OR Shift+Primary = Pan (translate)
        if (me.isMiddleButtonDown() || (me.isPrimaryButtonDown() && me.isShiftDown())) {
            // Pan the view - adjust world translate
            double panSpeed = 2.0;
            worldTranslate.setX(worldTranslate.getX() + mouseDeltaX * panSpeed);
            worldTranslate.setY(worldTranslate.getY() + mouseDeltaY * panSpeed);
        } else if (me.isPrimaryButtonDown()) {
            if (me.isAltDown()) { // roll
                rotateZ.setAngle(((rotateZ.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);
            } else {
                rotateY.setAngle(((rotateY.getAngle() + mouseDeltaX * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180);
                rotateX.setAngle(
                        (((rotateX.getAngle() - mouseDeltaY * modifierFactor * modifier * 2.0) % 360 + 540) % 360 - 180)
                );
            }
        }
        updateLabelsCallback.run();
    }

    /**
     * Handle mouse click for selection and context menus.
     */
    private void handleMouseClicked(MouseEvent me) {
        if (me.getButton() == MouseButton.PRIMARY && !me.isConsumed()) {
            renderer.clearSelection();
            updateLabelsCallback.run();
        }
        if (me.getButton() == MouseButton.SECONDARY && !me.isConsumed()) {
            // Only show if we have a current system loaded
            if (currentSystem != null && currentSystem.getStarDisplayRecord() != null) {
                showEmptySpaceContextMenu(me.getScreenX(), me.getScreenY());
            }
        }
    }

    /**
     * Show context menu when right-clicking on empty space.
     */
    private void showEmptySpaceContextMenu(double screenX, double screenY) {
        if (addPlanetCallback == null || contextMenuOwner == null) {
            return;
        }
        ContextMenu menu = contextMenuFactory.createEmptySpaceContextMenu(
                currentSystem,
                addPlanetCallback
        );
        menu.show(contextMenuOwner, screenX, screenY);
    }

    /**
     * Perform zoom operation.
     *
     * @param zoomAmt the amount to zoom
     */
    private void zoomGraph(double zoomAmt) {
        double z = camera.getTranslateZ();
        double newZ = z - zoomAmt;
        camera.setTranslateZ(newZ);

        // Update moon orbit visibility based on zoom level
        renderer.updateMoonOrbitVisibility(newZ);
    }
}
