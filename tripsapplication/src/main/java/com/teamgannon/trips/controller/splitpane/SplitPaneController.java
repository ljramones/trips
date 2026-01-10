package com.teamgannon.trips.controller.splitpane;

import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Accordion;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SplitPaneController implements SplitPaneView {

    @FXML
    private SplitPane mainSplitPane;

    @FXML
    private BorderPane leftDisplay;

    @FXML
    private BorderPane rightPanel;

    @FXML
    private LeftDisplayController leftDisplayController;

    @FXML
    private RightPanelController rightPanelController;

    public BorderPane getLeftBorderPane() {
        return leftDisplayController.getLeftBorderPane();
    }

    public StackPane getLeftDisplayPane() {
        return leftDisplayController.getLeftDisplayPane();
    }

    public BorderPane getRightBorderPane() {
        return rightPanelController.getRightBorderPane();
    }

    public VBox getSettingsPane() {
        return rightPanelController.getSettingsPane();
    }

    public Accordion getPropertiesAccordion() {
        return rightPanelController.getPropertiesAccordion();
    }

    public RightPanelController getRightPanel() {
        return rightPanelController;
    }

    public LeftDisplayController getLeftDisplay() {
        return leftDisplayController;
    }
}
