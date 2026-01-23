package com.teamgannon.trips.starplotting;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.entities.StarSelectionModel;
import com.teamgannon.trips.routing.dialogs.ContextManualRoutingDialog;
import com.teamgannon.trips.routing.model.RoutingType;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Handles mouse click events on star nodes in the interstellar view.
 * <p>
 * This class manages:
 * <ul>
 *   <li>Primary button (left-click) - context menu or routing</li>
 *   <li>Middle button - always shows context menu</li>
 *   <li>Secondary button (right-click) - star selection toggle</li>
 * </ul>
 * <p>
 * It also maintains the star selection model for multi-star selection.
 */
@Slf4j
public class StarClickHandler {

    /**
     * Selection model for tracking selected stars.
     */
    @Getter
    private final Map<Node, StarSelectionModel> selectionModel = new HashMap<>();

    /**
     * Context menu handler for routing state queries.
     */
    private final StarContextMenuHandler contextMenuHandler;

    /**
     * Function to create context menu for a star.
     */
    private final BiFunction<StarDisplayRecord, Node, ContextMenu> contextMenuCreator;

    /**
     * Constructor.
     *
     * @param contextMenuHandler  the context menu handler for routing state
     * @param contextMenuCreator  function to create context menus
     */
    public StarClickHandler(StarContextMenuHandler contextMenuHandler,
                            BiFunction<StarDisplayRecord, Node, ContextMenu> contextMenuCreator) {
        this.contextMenuHandler = contextMenuHandler;
        this.contextMenuCreator = contextMenuCreator;
    }

    /**
     * Sets up a context menu on a star node (eager creation).
     * <p>
     * The context menu is created immediately and attached to the node.
     * Use {@link #setupLazyContextMenu} for better memory efficiency with large star plots.
     *
     * @param record the star record
     * @param star   the star node
     */
    public void setupContextMenu(@NotNull StarDisplayRecord record, Node star) {
        star.setUserData(record);
        String polity = formatPolity(record.getPolity());
        ContextMenu starContextMenu = contextMenuCreator.apply(record, star);

        star.addEventHandler(MouseEvent.MOUSE_CLICKED,
                e -> handleStarClick(star, starContextMenu, e));

        star.setOnMousePressed(event -> {
            Node node = (Node) event.getSource();
            StarDisplayRecord starDescriptor = (StarDisplayRecord) node.getUserData();
            log.info("mouse click detected! {}", starDescriptor);
        });
    }

    /**
     * Sets up lazy-loaded context menu for a star node.
     * <p>
     * The context menu is created on-demand when the user clicks the node.
     * This significantly reduces memory usage and initialization time for large star plots.
     *
     * @param record the star record
     * @param star   the star node
     */
    public void setupLazyContextMenu(@NotNull StarDisplayRecord record, Node star) {
        star.setUserData(record);

        star.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                handlePrimaryClick(record, star, e);
            } else if (e.getButton() == MouseButton.MIDDLE) {
                handleMiddleClick(record, star, e);
            } else if (e.getButton() == MouseButton.SECONDARY) {
                handleSecondaryClick(star);
            }
        });

        star.setOnMousePressed(event -> {
            Node node = (Node) event.getSource();
            StarDisplayRecord starDescriptor = (StarDisplayRecord) node.getUserData();
            log.debug("mouse click detected! {}", starDescriptor);
        });
    }

    /**
     * Handles star click events.
     *
     * @param star            the star node
     * @param starContextMenu the pre-created context menu
     * @param e               the mouse event
     */
    private void handleStarClick(Node star, @NotNull ContextMenu starContextMenu, @NotNull MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            handlePrimaryClickWithMenu(star, starContextMenu, e);
        } else if (e.getButton() == MouseButton.MIDDLE) {
            log.info("Middle button pressed");
            starContextMenu.show(star, e.getScreenX(), e.getScreenY());
        } else if (e.getButton() == MouseButton.SECONDARY) {
            handleSecondaryClick(star);
        }
    }

    /**
     * Handles primary (left) click with a pre-created context menu.
     */
    private void handlePrimaryClickWithMenu(Node star, ContextMenu starContextMenu, MouseEvent e) {
        log.info("Primary button pressed");
        if (contextMenuHandler.isManualRoutingActive()) {
            log.info("Manual Routing is active");
            StarDisplayRecord record = (StarDisplayRecord) star.getUserData();
            handleRoutingClick(record);
        } else {
            log.info("Manual routing is not active");
            starContextMenu.show(star, e.getScreenX(), e.getScreenY());
        }
    }

    /**
     * Handles primary (left) click with lazy context menu creation.
     */
    private void handlePrimaryClick(StarDisplayRecord record, Node star, MouseEvent e) {
        log.info("Primary button pressed");
        if (contextMenuHandler.isManualRoutingActive()) {
            log.info("Manual Routing is active");
            handleRoutingClick(record);
        } else {
            log.info("Manual routing is not active - showing context menu");
            ContextMenu starContextMenu = contextMenuCreator.apply(record, star);
            starContextMenu.show(star, e.getScreenX(), e.getScreenY());
        }
    }

    /**
     * Handles middle button click - always shows context menu.
     */
    private void handleMiddleClick(StarDisplayRecord record, Node star, MouseEvent e) {
        log.info("Middle button pressed");
        ContextMenu starContextMenu = contextMenuCreator.apply(record, star);
        starContextMenu.show(star, e.getScreenX(), e.getScreenY());
    }

    /**
     * Handles secondary (right) click - toggles star selection.
     */
    private void handleSecondaryClick(Node star) {
        log.info("Secondary button pressed");
        if (selectionModel.containsKey(star)) {
            removeSelection(star);
        } else {
            addSelection(star);
        }
    }

    /**
     * Handles click during active routing mode.
     */
    private void handleRoutingClick(StarDisplayRecord record) {
        if (contextMenuHandler.getRoutingType().equals(RoutingType.MANUAL)) {
            ContextManualRoutingDialog manualDialog = contextMenuHandler.getManualRoutingDialog();
            if (manualDialog != null) {
                manualDialog.addStar(record);
            }
        }
        if (contextMenuHandler.getRoutingType().equals(RoutingType.AUTOMATIC)) {
            var automatedDialog = contextMenuHandler.getAutomatedRoutingDialog();
            if (automatedDialog != null) {
                automatedDialog.setToStar(record.getStarName());
            }
        }
    }

    /**
     * Adds a star to the selection model.
     *
     * @param star the star to select
     */
    public void addSelection(Node star) {
        StarSelectionModel starSelectionModel = new StarSelectionModel();
        starSelectionModel.setStarNode(star);
        selectionModel.put(star, starSelectionModel);
    }

    /**
     * Removes a star from the selection model.
     *
     * @param star the star to deselect
     */
    public void removeSelection(Node star) {
        StarSelectionModel starSelectionModel = selectionModel.get(star);
        if (starSelectionModel != null) {
            Node selectionRectangle = starSelectionModel.getSelectionRectangle();
            if (selectionRectangle != null && selectionRectangle.getParent() instanceof Group group) {
                group.getChildren().remove(selectionRectangle);
            }
        }
        selectionModel.remove(star);
    }

    /**
     * Clears all selections.
     */
    public void clearSelections() {
        for (Node star : selectionModel.keySet()) {
            StarSelectionModel model = selectionModel.get(star);
            Node selectionRectangle = model.getSelectionRectangle();
            if (selectionRectangle != null && selectionRectangle.getParent() instanceof Group group) {
                group.getChildren().remove(selectionRectangle);
            }
        }
        selectionModel.clear();
    }

    /**
     * Checks if a star is currently selected.
     *
     * @param star the star to check
     * @return true if selected
     */
    public boolean isSelected(Node star) {
        return selectionModel.containsKey(star);
    }

    /**
     * Gets the number of selected stars.
     *
     * @return the selection count
     */
    public int getSelectionCount() {
        return selectionModel.size();
    }

    /**
     * Formats polity string for display.
     */
    private String formatPolity(String polity) {
        return polity.equals("NA") ? "Non-aligned" : polity;
    }
}
