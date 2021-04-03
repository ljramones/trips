package com.teamgannon.trips.dialogs.preferences;

import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.listener.PreferencesUpdaterListener;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class CivilizationPane extends Pane {

    private final CivilizationDisplayPreferences preferences;
    private final PreferencesUpdaterListener updater;

    private final ColorPicker humanColorPicker = new ColorPicker();
    private final ColorPicker dornaniColorPicker = new ColorPicker();
    private final ColorPicker ktorColorPicker = new ColorPicker();
    private final ColorPicker aratKurColorPicker = new ColorPicker();
    private final ColorPicker hkhRkhColorPicker = new ColorPicker();
    private final ColorPicker slaasriithiColorPicker = new ColorPicker();
    private final ColorPicker other1ColorPicker = new ColorPicker();
    private final ColorPicker other2ColorPicker = new ColorPicker();
    private final ColorPicker other3ColorPicker = new ColorPicker();
    private final ColorPicker other4ColorPicker = new ColorPicker();

    public CivilizationPane(CivilizationDisplayPreferences preferences, PreferencesUpdaterListener updater) {
        this.preferences = preferences;
        this.updater = updater;

        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

        VBox vBox = new VBox();

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        vBox.getChildren().add(gridPane);

        GridPane gridPane1 = new GridPane();
        gridPane1.setPadding(new Insets(10, 10, 10, 10));
        gridPane1.setVgap(5);
        gridPane1.setHgap(5);

        gridPane.add(gridPane1, 0, 1);

        Label humanLabel = new Label("Human ");
        humanLabel.setFont(font);
        gridPane1.add(humanLabel, 0, 1);
        gridPane1.add(humanColorPicker, 1, 1);

        Label dornaniLabel = new Label("Dornani ");
        dornaniLabel.setFont(font);
        gridPane1.add(dornaniLabel, 0, 2);
        gridPane1.add(dornaniColorPicker, 1, 2);

        Label ktorLabel = new Label("Ktor ");
        ktorLabel.setFont(font);
        gridPane1.add(ktorLabel, 0, 3);
        gridPane1.add(ktorColorPicker, 1, 3);

        Label aratKurLabel = new Label("Arat Kur ");
        aratKurLabel.setFont(font);
        gridPane1.add(aratKurLabel, 0, 4);
        gridPane1.add(aratKurColorPicker, 1, 4);

        Label hkhRkhLabel = new Label("Hkh'Rkh ");
        hkhRkhLabel.setFont(font);
        gridPane1.add(hkhRkhLabel, 0, 5);
        gridPane1.add(hkhRkhColorPicker, 1, 5);

        Label slaasriithiLabel = new Label("Slaasriithi ");
        slaasriithiLabel.setFont(font);
        gridPane1.add(slaasriithiLabel, 0, 6);
        gridPane1.add(slaasriithiColorPicker, 1, 6);

        GridPane gridPane2 = new GridPane();
        gridPane2.setPadding(new Insets(10, 10, 10, 10));
        gridPane2.setVgap(5);
        gridPane2.setHgap(5);

        gridPane.add(gridPane2, 1, 1);

        Label other1Label = new Label("Other 1 ");
        other1Label.setFont(font);
        gridPane2.add(other1Label, 0, 1);
        gridPane2.add(other1ColorPicker, 1, 1);

        Label other2Label = new Label("Other 2 ");
        other2Label.setFont(font);
        gridPane2.add(other2Label, 0, 2);
        gridPane2.add(other2ColorPicker, 1, 2);

        Label other3Label = new Label("Other 3 ");
        other3Label.setFont(font);
        gridPane2.add(other3Label, 0, 3);
        gridPane2.add(other3ColorPicker, 1, 3);

        Label other4Label = new Label("Other 4 ");
        other4Label.setFont(font);
        gridPane2.add(other4Label, 0, 4);
        gridPane2.add(other4ColorPicker, 1, 4);

        setPreferences();

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);

        Button resetBtn = new Button("Reset");
        resetBtn.setOnAction(this::resetPolitiesClicked);
        hBox.getChildren().add(resetBtn);

        Button addBtn = new Button("Change");
        addBtn.setOnAction(this::changePolitiesClicked);
        hBox.getChildren().add(addBtn);

        vBox.getChildren().add(hBox);

        this.getChildren().add(vBox);
    }

    private void changePolitiesClicked(ActionEvent actionEvent) {
        preferences.setHumanPolityColor(humanColorPicker.getValue().toString());
        preferences.setDornaniPolityColor(dornaniColorPicker.getValue().toString());
        preferences.setKtorPolityColor(ktorColorPicker.getValue().toString());
        preferences.setAratKurPolityColor(aratKurColorPicker.getValue().toString());
        preferences.setHkhRkhPolityColor(hkhRkhColorPicker.getValue().toString());
        preferences.setSlaasriithiPolityColor(slaasriithiColorPicker.getValue().toString());
        preferences.setOther1PolityColor(other1ColorPicker.getValue().toString());
        preferences.setOther2PolityColor(other2ColorPicker.getValue().toString());
        preferences.setOther3PolityColor(other3ColorPicker.getValue().toString());
        preferences.setOther4PolityColor(other4ColorPicker.getValue().toString());

        updater.changePolitiesPreferences(preferences);

    }

    private void resetPolitiesClicked(ActionEvent actionEvent) {
        preferences.reset();
        setPreferences();
        updater.changePolitiesPreferences(preferences);
    }

    private void setPreferences() {
        humanColorPicker.setValue(Color.valueOf(preferences.getHumanPolityColor()));
        dornaniColorPicker.setValue(Color.valueOf(preferences.getDornaniPolityColor()));
        ktorColorPicker.setValue(Color.valueOf(preferences.getKtorPolityColor()));
        aratKurColorPicker.setValue(Color.valueOf(preferences.getAratKurPolityColor()));
        hkhRkhColorPicker.setValue(Color.valueOf(preferences.getHkhRkhPolityColor()));
        slaasriithiColorPicker.setValue(Color.valueOf(preferences.getSlaasriithiPolityColor()));

        other1ColorPicker.setValue(Color.valueOf(preferences.getOther1PolityColor()));
        other2ColorPicker.setValue(Color.valueOf(preferences.getOther2PolityColor()));
        other3ColorPicker.setValue(Color.valueOf(preferences.getOther3PolityColor()));
        other4ColorPicker.setValue(Color.valueOf(preferences.getOther4PolityColor()));
    }

    public CivilizationDisplayPreferences getPreferences() {
        return preferences;
    }

}
