package com.teamgannon.trips.search.components;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Created by larrymitchell on 2017-06-24.
 */
public class BaseSearchPane extends Pane {

    protected final static float LABEL_PREF_WIDTH = 150;

    protected GridPane planGrid = new GridPane();

    public GridPane getPane() {
        return planGrid;
    }

    protected Label createLabel(String textName) {
        Label label = new Label(textName);
        label.setPrefWidth(LABEL_PREF_WIDTH);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        return label;
    }

}
