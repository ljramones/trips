package com.teamgannon.trips.search.components;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.jetbrains.annotations.NotNull;

/**
 * Created by larrymitchell on 2017-06-24.
 */
public class BasePane extends GridPane {

    protected final static float LABEL_PREF_WIDTH = 150;

    public GridPane getPane() {
        return this;
    }

    protected @NotNull Label createLabel(String textName) {
        Label label = new Label(textName);
        label.setPrefWidth(LABEL_PREF_WIDTH);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        return label;
    }

    protected void applyLabelStyle(@NotNull Label label) {
        label.setPrefWidth(LABEL_PREF_WIDTH);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 13));
    }

}
