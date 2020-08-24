package com.teamgannon.trips.dialogs.preferencespanes;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.jpa.model.GraphEnablesPersist;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import lombok.extern.slf4j.Slf4j;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class GraphPane extends Pane {

    private final PreferencesUpdater updater;

    private final static String COLOR_PANE_TITLE = "Change Graph Colors";
    private final static String COLOR_PANE_TITLE_MODIFIED = "Change Graph Colors - *modified*";

    private final Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

    private final ColorPalette colorPalette;

    private final GraphEnablesPersist graphEnablesPersist;

    private final TitledPane colorPane = new TitledPane();

    // color change text fields
    private final TextField labelColorTextField = new TextField();
    private final TextField gridColorTextField = new TextField();
    private final TextField extensionColorTextField = new TextField();
    private final TextField legendColorTextField = new TextField();

    private final ColorPicker labelColorPicker = new ColorPicker();
    private final ColorPicker gridColorPicker = new ColorPicker();
    private final ColorPicker extensionColorPicker = new ColorPicker();
    private final ColorPicker legendColorPicker = new ColorPicker();

    private final CheckBox displayGridCheckbox = new CheckBox();
    private final CheckBox displayStemCheckbox = new CheckBox();
    private final CheckBox displayLabelCheckbox = new CheckBox();
    private final CheckBox displayLegendCheckbox = new CheckBox();


    public GraphPane(PreferencesUpdater updater, TripsContext tripsContext) {
        this.updater = updater;

        this.colorPalette = tripsContext.getAppViewPreferences().getColorPallete();
        graphEnablesPersist = tripsContext.getAppViewPreferences().getGraphEnablesPersist();

        VBox vBox = new VBox();

        Pane pane1 = createColorPane();
        TitledPane titledPane1 = new TitledPane("Graph Colors", pane1);
        vBox.getChildren().add(titledPane1);

        GridPane gridPane2 = createEnablePane(graphEnablesPersist);
        TitledPane titledPane2 = new TitledPane("Graph Enables", gridPane2);
        gridPane2.add(titledPane2, 1, 2);
        vBox.getChildren().add(titledPane2);

        this.getChildren().add(vBox);
    }


    private GridPane createGridPane() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        return gridPane;
    }


    ///////////////////////////////////////////////////////////////////////////


    private Pane createColorPane() {

        GridPane colorGridPane = createGridPane();

        //////////////////////////////
        //   setup color of star labels
        Label starNameLabel = new Label("Label color:");
        starNameLabel.setFont(font);
        colorGridPane.add(starNameLabel, 0, 0);
        colorGridPane.add(labelColorTextField, 1, 0);

        // set listener
        labelColorPicker.setOnAction(e -> {
            // color
            Color c = labelColorPicker.getValue();

            // set text of the label to RGB value of color
            labelColorTextField.setText(c.toString());
        });
        colorGridPane.add(labelColorPicker, 2, 0);

        // set values
        labelColorTextField.setText(colorPalette.getLabelColor().toString());
        labelColorPicker.setValue(colorPalette.getLabelColor());

        //////////////////////////////
        //   setup color of grid
        Label gridColorLabel = new Label("Grid color:");
        gridColorLabel.setFont(font);
        colorGridPane.add(gridColorLabel, 0, 1);
        colorGridPane.add(gridColorTextField, 1, 1);
        // set listener
        gridColorPicker.setOnAction(e -> {
            // color
            Color c = gridColorPicker.getValue();

            // set text of the label to RGB value of color
            gridColorTextField.setText(c.toString());
        });
        colorGridPane.add(gridColorPicker, 2, 1);

        // set values
        gridColorTextField.setText(colorPalette.getGridColor().toString());
        gridColorPicker.setValue(colorPalette.getGridColor());

        //////////////////////////////
        //   setup color of extensions
        Label stemLabel = new Label("Stem color:");
        stemLabel.setFont(font);
        colorGridPane.add(stemLabel, 0, 2);
        colorGridPane.add(extensionColorTextField, 1, 2);
        // set listener
        extensionColorPicker.setOnAction(e -> {
            // color
            Color c = extensionColorPicker.getValue();

            // set text of the label to RGB value of color
            extensionColorTextField.setText(c.toString());
        });
        colorGridPane.add(extensionColorPicker, 2, 2);

        // set values
        extensionColorTextField.setText(colorPalette.getExtensionColor().toString());
        extensionColorPicker.setValue(colorPalette.getExtensionColor());

        //////////////////////////////
        //   setup color of legends
        Label legendColor = new Label("Legend color");
        legendColor.setFont(font);
        colorGridPane.add(legendColor, 0, 3);
        colorGridPane.add(legendColorTextField, 1, 3);
        // set listener
        legendColorPicker.setOnAction(e -> {
            // color
            Color c = legendColorPicker.getValue();

            // set text of the label to RGB value of color
            legendColorTextField.setText(c.toString());
        });
        colorGridPane.add(legendColorPicker, 2, 3);

        // set values
        legendColorTextField.setText(colorPalette.getLegendColor().toString());
        legendColorPicker.setValue(colorPalette.getLegendColor());

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);

        Button resetBtn = new Button("Reset");
        resetBtn.setOnAction(this::resetColorsClicked);
        hBox.getChildren().add(resetBtn);

        Button addBtn = new Button("Change");
        addBtn.setOnAction(this::changeColorsClicked);
        hBox.getChildren().add(addBtn);

        colorGridPane.add(hBox, 0, 4, 3, 1);

        // set event listeners
        labelColorTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                labelFieldChanged();
            }
        });

        gridColorTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                gridFieldChanged();
            }
        });

        extensionColorTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                extFieldChanged();
            }
        });

        legendColorTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                legendFieldChanged();
            }
        });

        colorPane.setText(COLOR_PANE_TITLE);
        colorPane.setContent(colorGridPane);

        this.getChildren().add(colorPane);

        colorPane.setCollapsible(false);
        return colorGridPane;
    }


    private void legendFieldChanged() {
        try {
            Color color = Color.valueOf(legendColorTextField.getText());
            legendColorPicker.setValue(color);
            colorPane.setText(COLOR_PANE_TITLE_MODIFIED);
        } catch (Exception e) {
            log.error("{} is an invalid color", legendColorTextField.getText());
            showErrorAlert("Change Legend Color", legendColorTextField.getText() + " is an invalid color");
        }
    }

    private void extFieldChanged() {
        try {
            Color color = Color.valueOf(extensionColorTextField.getText());
            extensionColorPicker.setValue(color);
            colorPane.setText(COLOR_PANE_TITLE_MODIFIED);
        } catch (Exception e) {
            log.error("{} is an invalid color", extensionColorTextField.getText());
            showErrorAlert("Change Extension Color", extensionColorTextField.getText() + " is an invalid color");
        }
    }

    private void gridFieldChanged() {
        try {
            Color color = Color.valueOf(gridColorTextField.getText());
            gridColorPicker.setValue(color);
            colorPane.setText(COLOR_PANE_TITLE_MODIFIED);
        } catch (Exception e) {
            log.error("{} is an invalid color", gridColorTextField.getText());
            showErrorAlert("Change Grid Color", gridColorTextField.getText() + " is an invalid color");
        }
    }

    private void labelFieldChanged() {
        try {
            Color color = Color.valueOf(labelColorTextField.getText());
            labelColorPicker.setValue(color);
            colorPane.setText(COLOR_PANE_TITLE_MODIFIED);
        } catch (Exception e) {
            log.error("{} is an invalid color", labelColorTextField.getText());
            showErrorAlert("Change Label Color", labelColorTextField.getText() + " is an invalid color");
        }
    }


    private void changeColorsClicked(ActionEvent actionEvent) {

        colorPalette.setLabelColor(labelColorPicker.getValue().toString());
        colorPalette.setGridColor(gridColorPicker.getValue().toString());
        colorPalette.setExtensionColor(extensionColorPicker.getValue().toString());
        colorPalette.setLabelColor(legendColorPicker.getValue().toString());

        updater.updateGraphColors(colorPalette);
    }


    private void resetColorsClicked(ActionEvent actionEvent) {
        colorPalette.setDefaults();
        labelColorTextField.setText(colorPalette.getLabelColor().toString());
        labelColorPicker.setValue(colorPalette.getLabelColor());

        gridColorTextField.setText(colorPalette.getGridColor().toString());
        gridColorPicker.setValue(colorPalette.getGridColor());

        extensionColorTextField.setText(colorPalette.getExtensionColor().toString());
        extensionColorPicker.setValue(colorPalette.getExtensionColor());

        legendColorTextField.setText(colorPalette.getLegendColor().toString());
        legendColorPicker.setValue(colorPalette.getLegendColor());

        colorPane.setText(COLOR_PANE_TITLE);

    }


    ///////////////////////////////////////////////////////////////////

    private GridPane createEnablePane(GraphEnablesPersist graphEnablesPersist) {
        GridPane pane = createGridPane();

        Label lb1 = new Label("Display Grid:");
        lb1.setFont(font);
        pane.add(lb1, 1, 1);
        displayGridCheckbox.setSelected(graphEnablesPersist.isDisplayGrid());
        pane.add(displayGridCheckbox, 2, 1);

        Label lb2 = new Label("Display Stems:");
        lb2.setFont(font);
        pane.add(lb2, 1, 2);
        displayStemCheckbox.setSelected(graphEnablesPersist.isDisplayStems());
        pane.add(displayStemCheckbox, 2, 2);

        Label lb3 = new Label("Display Label:");
        lb3.setFont(font);
        pane.add(lb3, 1, 3);
        displayLabelCheckbox.setSelected(graphEnablesPersist.isDisplayLabels());
        pane.add(displayLabelCheckbox, 2, 3);

        Label lb4 = new Label("Display Legend:");
        lb4.setFont(font);
        pane.add(lb4, 1, 4);
        displayLegendCheckbox.setSelected(graphEnablesPersist.isDisplayLegend());
        pane.add(displayLegendCheckbox, 2, 4);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        Button resetBtn = new Button("Reset");
        resetBtn.setOnAction(this::resetEnablesClicked);
        hBox.getChildren().add(resetBtn);
        Button addBtn = new Button("Change");
        addBtn.setOnAction(this::changeEnablesClicked);
        hBox.getChildren().add(addBtn);
        pane.add(hBox, 0, 5, 3, 1);

        return pane;
    }

    private void resetEnablesClicked(ActionEvent actionEvent) {
        graphEnablesPersist.setDefault();
        displayGridCheckbox.setSelected(graphEnablesPersist.isDisplayGrid());
        displayStemCheckbox.setSelected(graphEnablesPersist.isDisplayStems());
        displayLabelCheckbox.setSelected(graphEnablesPersist.isDisplayLabels());
        displayLegendCheckbox.setSelected(graphEnablesPersist.isDisplayLegend());
    }

    private void changeEnablesClicked(ActionEvent actionEvent) {
        graphEnablesPersist.setDisplayGrid(displayGridCheckbox.isSelected());
        graphEnablesPersist.setDisplayStems(displayStemCheckbox.isSelected());
        graphEnablesPersist.setDisplayLabels(displayLabelCheckbox.isSelected());
        graphEnablesPersist.setDisplayLegend(displayLegendCheckbox.isSelected());
        updater.changesGraphEnables(graphEnablesPersist);
    }

}
