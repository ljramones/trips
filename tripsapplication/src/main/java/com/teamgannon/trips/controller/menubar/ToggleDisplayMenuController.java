package com.teamgannon.trips.controller.menubar;

import com.teamgannon.trips.controller.shared.SharedUIFunctions;
import com.teamgannon.trips.controller.shared.SharedUIState;
import com.teamgannon.trips.events.UIStateChangeEvent;
import com.teamgannon.trips.javafxsupport.FxThread;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ToggleDisplayMenuController {

    private final ApplicationEventPublisher eventPublisher;
    private final SharedUIState sharedUIState;
    private final SharedUIFunctions sharedUIFunctions;


    @FXML
    public CheckMenuItem toggleGridMenuitem;
    @FXML
    public CheckMenuItem toggleExtensionsMenuitem;
    @FXML
    public CheckMenuItem toggleLabelsMenuitem;
    @FXML
    public CheckMenuItem toggleScaleMenuitem;
    @FXML
    public CheckMenuItem togglePolitiesMenuitem;
    @FXML
    public CheckMenuItem toggleStarMenuitem;
    @FXML
    public CheckMenuItem toggleTransitsMenuitem;
    @FXML
    public CheckMenuItem toggleTransitLengthsMenuitem;
    @FXML
    public CheckMenuItem toggleRoutesMenuitem;
    @FXML
    public CheckMenuItem toggleRouteLengthsMenuitem;
    @FXML
    public CheckMenuItem toggleSidePaneMenuitem;
    @FXML
    public CheckMenuItem toggleToolBarMenuitem;
    @FXML
    public CheckMenuItem toggleStatusBarMenuitem;

    public ToggleDisplayMenuController(ApplicationEventPublisher eventPublisher,
                                       SharedUIState sharedUIState, SharedUIFunctions sharedUIFunctions) {
        this.eventPublisher = eventPublisher;
        this.sharedUIState = sharedUIState;
        this.sharedUIFunctions = sharedUIFunctions;
    }

    public void toggleGrid(ActionEvent actionEvent) {
        sharedUIFunctions.toggleGrid();
    }

    public void toggleGridExtensions(ActionEvent actionEvent) {
        sharedUIFunctions.toggleGridExtensions();
    }

    public void toggleLabels(ActionEvent actionEvent) {
        sharedUIFunctions.toggleLabels();
    }

    public void toggleScale(ActionEvent actionEvent) {
        sharedUIFunctions.toggleScale();
    }

    public void togglePolities(ActionEvent actionEvent) {
        sharedUIFunctions.togglePolities();
    }

    public void toggleStars(ActionEvent actionEvent) {
        sharedUIFunctions.toggleStars();
    }

    public void toggleTransitAction(ActionEvent actionEvent) {
        sharedUIFunctions.toggleTransit();
    }

    public void toggleTransitLengths(ActionEvent actionEvent) {

    }

    public void toggleRoutes(ActionEvent actionEvent) {
        sharedUIFunctions.toggleRoutes();
    }

    public void toggleRouteLengths(ActionEvent actionEvent) {

    }

    public void toggleSidePane(ActionEvent actionEvent) {
        sharedUIFunctions.toggleSidePane();
    }

    public void toggleToolbar(ActionEvent actionEvent) {

    }

    public void toggleStatusBar(ActionEvent actionEvent) {

    }

    @EventListener
    public void handleUIStateChangeEvent(UIStateChangeEvent event) {
        FxThread.runOnFxThread(() -> {
            switch (event.getElement()) {
                case GRID -> toggleGridMenuitem.setSelected(event.isState());
                case LABELS -> toggleLabelsMenuitem.setSelected(event.isState());
                case POLITIES -> togglePolitiesMenuitem.setSelected(event.isState());
                case STARS -> toggleStarMenuitem.setSelected(event.isState());
                case ROUTES -> toggleRouteLengthsMenuitem.setSelected(event.isState());
                case TRANSITS -> toggleTransitLengthsMenuitem.setSelected(event.isState());
                case TRANSIT_LENGTHS -> toggleTransitLengthsMenuitem.setSelected(event.isState());
                case ROUTE_LENGTHS -> toggleRouteLengthsMenuitem.setSelected(event.isState());
                case SIDE_PANE -> toggleSidePaneMenuitem.setSelected(event.isState());
                case TOOLBAR -> toggleToolBarMenuitem.setSelected(event.isState());
                case STATUS_BAR -> toggleStatusBarMenuitem.setSelected(event.isState());
                case SCALE -> toggleScaleMenuitem.setSelected(event.isState());
                case EXTENSIONS -> toggleExtensionsMenuitem.setSelected(event.isState());
            }
        });
    }
}
