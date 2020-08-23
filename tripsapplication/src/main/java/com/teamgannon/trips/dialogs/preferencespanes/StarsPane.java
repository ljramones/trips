package com.teamgannon.trips.dialogs.preferencespanes;

import com.teamgannon.trips.config.application.StarDescriptionPreference;
import com.teamgannon.trips.config.application.StarDisplayPreferences;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

public class StarsPane extends Pane {

    private StarDisplayPreferences starDisplayPreferences;

    public StarsPane(StarDisplayPreferences starDisplayPreferences, String style) {
        this.starDisplayPreferences = starDisplayPreferences;

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));

        this.setStyle(style);
        gridPane.setStyle(style);

        gridPane.add(new Label("       "), 1, 1);

        Pane pane1 = starsDefinitionPane(starDisplayPreferences);
        gridPane.add(pane1, 1, 2);

        gridPane.add(new Label("      "), 1, 3);

        this.getChildren().add(gridPane);
    }


    private Pane starsDefinitionPane(StarDisplayPreferences starDisplayPreferences) {
        GridPane pane = new GridPane();

        pane.add(new Separator(), 1, 1, 1, starDisplayPreferences.getStarMap().size() + 1);

        int i = 0;
        for (StarDescriptionPreference star : starDisplayPreferences.getStarMap()) {
            createStarLine(pane, 3 + i, starDisplayPreferences.getStarMap().get(i++));
        }

        return pane;
    }

    private void createStarLine(GridPane pane, int row, StarDescriptionPreference starDescriptionPreference) {
        pane.add(
                new Label(
                        String.format("Stellar Class %s:", starDescriptionPreference.getStartClass().getValue())),
                2, row);

        ColorPicker gridColorPicker = new ColorPicker(starDescriptionPreference.getColor());
        pane.add(gridColorPicker, 3, row);
        TextField link1 = new TextField(Float.toString(starDescriptionPreference.getSize()));
        pane.add(link1, 4, row);

    }

}
