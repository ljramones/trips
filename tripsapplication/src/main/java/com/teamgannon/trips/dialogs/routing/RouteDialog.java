package com.teamgannon.trips.dialogs.routing;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.routing.dialogs.components.ColorChoice;
import com.teamgannon.trips.routing.dialogs.components.ColorChoiceDialog;
import com.teamgannon.trips.utility.DialogUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Optional;


@Slf4j
public class RouteDialog extends Dialog<RouteSelector> {

    private final ColorPicker colorPicker = new ColorPicker();

    private final TextArea notes = new TextArea();

    private final TextField routeName = new TextField();

    private final TextField lineWidthTextField = new TextField();

    private final StarDisplayRecord starDisplayRecord;

    private Button colorButton = new Button("Color");

    /**
     * constructor
     *
     * @param starDisplayRecord the start star
     */
    public RouteDialog(@NotNull StarDisplayRecord starDisplayRecord) {

        this.starDisplayRecord = starDisplayRecord;

        // set the dialog as a utility
        DialogUtils.bindCloseHandler(this, this::close);

        this.setTitle("Route Creation Dialog");
        this.setHeaderText("Create an initial Route");

        VBox vBox = new VBox();
        this.getDialogPane().setContent(vBox);

        GridPane grid = new GridPane();
        vBox.getChildren().add(grid);

        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(5);
        grid.setHgap(5);

        Label routeNameLabel = new Label("Route Name: ");
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);
        routeNameLabel.setFont(font);
        grid.add(routeNameLabel, 1, 1);
        grid.add(routeName, 2, 1);

        Label routeColorLabel = new Label("Route Color: ");
        routeColorLabel.setFont(font);
        grid.add(routeColorLabel, 1, 2);

        colorButton.setOnAction(this::setColor);
        grid.add(colorButton, 2, 2);

        Label routeLineWidthLabel = new Label("Route Line Width: ");
        routeColorLabel.setFont(font);
        grid.add(routeLineWidthLabel, 1, 3);
        grid.add(lineWidthTextField, 2, 3);

        Label routeStartLabel = new Label("Route Starts at: ");
        routeColorLabel.setFont(font);
        Label routeStart = new Label(starDisplayRecord.getStarName());
        grid.add(routeStartLabel, 1, 4);
        grid.add(routeStart, 2, 4);

        Label routeStartCoordinatesLabel = new Label("Route Start Coords: ");
        routeStartCoordinatesLabel.setFont(font);
        double x = starDisplayRecord.getX();
        double y = starDisplayRecord.getY();
        double z = starDisplayRecord.getZ();
        Label routeStartCoordinates = new Label(String.format("x(%.2f), y(%.2f), z(%.2f)", x, y, z));
        grid.add(routeStartCoordinatesLabel, 1, 5);
        grid.add(routeStartCoordinates, 2, 5);

        Label notesLabel = new Label("Notes: ");
        grid.add(notesLabel, 1, 6);
        grid.add(notes, 2, 6);

        HBox hBox = new HBox(6);
        hBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox);

        Button okButton = new Button("Ok");
        okButton.setFont(font);
        okButton.setOnAction(this::okAction);
        hBox.getChildren().add(okButton);

        Button cancelButton = new Button("Cancel");
        cancelButton.setFont(font);
        cancelButton.setOnAction(this::cancelClicked);
        hBox.getChildren().add(cancelButton);

    }


    private void setColor(ActionEvent actionEvent) {
        ColorChoiceDialog colorChoiceDialog = new ColorChoiceDialog();
        Optional<ColorChoice> colorChoiceOptional = colorChoiceDialog.showAndWait();
        if (colorChoiceOptional.isPresent()) {
            ColorChoice colorChoice = colorChoiceOptional.get();
            if (colorChoice.isSelected()) {
                colorPicker.setValue(colorChoice.getSwatch());
                colorButton.setTextFill(colorChoice.getSwatch());
            }
        }
    }

    private void cancelClicked(ActionEvent actionEvent) {
        setResult(RouteSelector.builder().selected(false).build());
    }

    private void close(WindowEvent windowEvent) {
        setResult(RouteSelector.builder().selected(false).build());
    }

    private void okAction(ActionEvent actionEvent) {
        try {
            double lineWidth = Double.parseDouble(lineWidthTextField.getText());
            RouteDescriptor routeDescriptor = RouteDescriptor.builder()
                    .name(routeName.getText())
                    .color(colorPicker.getValue())
                    .startStar(starDisplayRecord.getStarName())
                    .routeCoordinates(new ArrayList<>())
                    .lineWidth(lineWidth)
                    .routeNotes(notes.getText())
                    .routeList(new ArrayList<>())
                    .build();
            setResult(RouteSelector
                    .builder()
                    .routeDescriptor(routeDescriptor)
                    .build());
        } catch (NumberFormatException nfe) {
            log.error(
                    "{} is not a valid double so defaulting to 0.5",
                    lineWidthTextField.getText());
        }
    }

    private void pickColor(ActionEvent actionEvent) {
        ColorChoiceDialog dialog = new ColorChoiceDialog();
        Optional<ColorChoice> colorChoiceOptional = dialog.showAndWait();
        if (colorChoiceOptional.isPresent()) {
            ColorChoice colorChoice = colorChoiceOptional.get();
            if (colorChoice.isSelected()) {
                colorPicker.setValue(colorChoice.getSwatch());
            }
        }
    }


}
