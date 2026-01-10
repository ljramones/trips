package com.teamgannon.trips.controller.splitpane;

import javafx.scene.control.Accordion;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public interface SplitPaneView {
    SplitPane getMainSplitPane();

    BorderPane getLeftBorderPane();

    StackPane getLeftDisplayPane();

    BorderPane getRightBorderPane();

    VBox getSettingsPane();

    Accordion getPropertiesAccordion();

    LeftDisplayController getLeftDisplay();

    RightPanelController getRightPanel();
}
