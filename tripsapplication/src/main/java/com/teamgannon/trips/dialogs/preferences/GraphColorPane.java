package com.teamgannon.trips.dialogs.preferences;

import com.teamgannon.trips.config.application.model.ColorPalette;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class GraphColorPane extends Pane {


    private final static String COLOR_PANE_TITLE = "Change Graph Colors";
    private final static String COLOR_PANE_TITLE_MODIFIED = "Change Graph Colors - *modified*";

    private final ColorPalette colorPalette;

    private boolean colorChangeDetected = false;

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


    public GraphColorPane(ColorPalette colorPalette) {
        this.colorPalette = colorPalette;

        GridPane gridPane = new GridPane();
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        //////////////////////////////
        //   setup color of star labels
        Label starNameLabel = new Label("Label color:");
        gridPane.add(starNameLabel, 0, 0);
        gridPane.add(labelColorTextField, 1, 0);

        // set listener
        labelColorPicker.setOnAction(e -> {
            // color
            Color c = labelColorPicker.getValue();

            // set text of the label to RGB value of color
            labelColorTextField.setText(c.toString());
        });
        gridPane.add(labelColorPicker, 2, 0);

        // set values
        labelColorTextField.setText(colorPalette.getLabelColor().toString());
        labelColorPicker.setValue(colorPalette.getLabelColor());

        //////////////////////////////
        //   setup color of grid
        Label gridColorLabel = new Label("Grid color:");
        gridPane.add(gridColorLabel, 0, 1);
        gridPane.add(gridColorTextField, 1, 1);
        // set listener
        gridColorPicker.setOnAction(e -> {
            // color
            Color c = gridColorPicker.getValue();

            // set text of the label to RGB value of color
            gridColorTextField.setText(c.toString());
        });
        gridPane.add(gridColorPicker, 2, 1);

        // set values
        gridColorTextField.setText(colorPalette.getGridColor().toString());
        gridColorPicker.setValue(colorPalette.getGridColor());

        //////////////////////////////
        //   setup color of extensions
        Label spectraLabel = new Label("Extension color:");
        gridPane.add(spectraLabel, 0, 2);
        gridPane.add(extensionColorTextField, 1, 2);
        // set listener
        extensionColorPicker.setOnAction(e -> {
            // color
            Color c = extensionColorPicker.getValue();

            // set text of the label to RGB value of color
            extensionColorTextField.setText(c.toString());
        });
        gridPane.add(extensionColorPicker, 2, 2);

        // set values
        extensionColorTextField.setText(colorPalette.getExtensionColor().toString());
        extensionColorPicker.setValue(colorPalette.getExtensionColor());

        //////////////////////////////
        //   setup color of legends
        Label radiusLabel = new Label("Legend color");
        gridPane.add(radiusLabel, 0, 3);
        gridPane.add(legendColorTextField, 1, 3);
        // set listener
        legendColorPicker.setOnAction(e -> {
            // color
            Color c = legendColorPicker.getValue();

            // set text of the label to RGB value of color
            legendColorTextField.setText(c.toString());
        });
        gridPane.add(legendColorPicker, 2, 3);

        // set values
        legendColorTextField.setText(colorPalette.getLegendColor().toString());
        legendColorPicker.setValue(colorPalette.getLegendColor());

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        Button resetBtn = new Button("Reset");
        resetBtn.setOnAction(this::resetColorsClicked);
        hBox.getChildren().add(resetBtn);
        gridPane.add(hBox, 0, 4, 3, 1);

        // set event listeners
        labelColorTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                labelFieldChanged(ke);
            }
        });

        gridColorTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                gridFieldChanged(ke);
            }
        });

        extensionColorTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                extFieldChanged(ke);
            }
        });

        legendColorTextField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                legendFieldChanged(ke);
            }
        });

        colorPane.setText(COLOR_PANE_TITLE);
        colorPane.setContent(gridPane);

        this.getChildren().add(colorPane);

        colorPane.setCollapsible(false);

    }

    private void resetColorsClicked(ActionEvent actionEvent) {
        resetColors();
    }

    private void legendFieldChanged(KeyEvent ke) {
        try {
            Color color = Color.valueOf(legendColorTextField.getText());
            legendColorPicker.setValue(color);
            colorPane.setText(COLOR_PANE_TITLE_MODIFIED);
            colorChangeDetected = true;
        } catch (Exception e) {
            log.error("{} is an invalid color", legendColorTextField.getText());
            showErrorAlert("Change Legend Color", legendColorTextField.getText() + " is an invalid color");
        }
    }

    private void extFieldChanged(KeyEvent ke) {
        try {
            Color color = Color.valueOf(extensionColorTextField.getText());
            extensionColorPicker.setValue(color);
            colorPane.setText(COLOR_PANE_TITLE_MODIFIED);
            colorChangeDetected = true;
        } catch (Exception e) {
            log.error("{} is an invalid color", extensionColorTextField.getText());
            showErrorAlert("Change Extension Color", extensionColorTextField.getText() + " is an invalid color");
        }
    }

    private void gridFieldChanged(KeyEvent ke) {
        try {
            Color color = Color.valueOf(gridColorTextField.getText());
            gridColorPicker.setValue(color);
            colorPane.setText(COLOR_PANE_TITLE_MODIFIED);
            colorChangeDetected = true;
        } catch (Exception e) {
            log.error("{} is an invalid color", gridColorTextField.getText());
            showErrorAlert("Change Grid Color", gridColorTextField.getText() + " is an invalid color");
        }
    }

    private void labelFieldChanged(KeyEvent ke) {
        try {
            Color color = Color.valueOf(labelColorTextField.getText());
            labelColorPicker.setValue(color);
            colorPane.setText(COLOR_PANE_TITLE_MODIFIED);
            colorChangeDetected = true;
        } catch (Exception e) {
            log.error("{} is an invalid color", labelColorTextField.getText());
            showErrorAlert("Change Label Color", labelColorTextField.getText() + " is an invalid color");
        }
    }


    public boolean isChanged() {
        return colorChangeDetected;
    }


    public ColorPalette getColorData() {

        ColorPalette palette = new ColorPalette();

        palette.setLabelColor(labelColorPicker.getValue().toString());
        palette.setGridColor(gridColorPicker.getValue().toString());
        palette.setExtensionColor(extensionColorPicker.getValue().toString());
        palette.setLabelColor(legendColorPicker.getValue().toString());

        return colorPalette;
    }

    public void resetColors() {
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

}
