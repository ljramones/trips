package com.teamgannon.trips.dialogs.search;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.dialogs.search.model.ConstellationSelected;
import com.teamgannon.trips.utility.DialogUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.WindowEvent;

public class FindAllByConstellationDialog extends Dialog<ConstellationSelected> {

    /**
     * holds the constellations
     */
    private final ChoiceBox<String> constellationChoice = new ChoiceBox<>();

    /**
     * constructor
     *
     * @param tripsContext holds IAU constellations
     */
    public FindAllByConstellationDialog(TripsContext tripsContext) {

        this.setTitle("Find all stars for a constellation");
        this.setHeight(300);
        this.setWidth(300);

        constellationChoice.getItems().addAll(tripsContext.getConstellationMap().keySet());
        constellationChoice.getSelectionModel().select(0);

        VBox vBox = new VBox();
        GridPane gridPane = new GridPane();
        vBox.getChildren().add(gridPane);
        Label starSearchLabel = new Label("Please select a constellation: ");
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);
        starSearchLabel.setFont(font);
        gridPane.add(starSearchLabel, 0, 0);
        gridPane.add(constellationChoice, 1, 0);

        HBox hBox2 = new HBox();
        hBox2.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox2);

        Button gotToStarButton = new Button("Find Stars");
        gotToStarButton.setOnAction(this::searchConstellationClicked);
        hBox2.getChildren().add(gotToStarButton);

        Button cancelDataSetButton = new Button("Dismiss");
        cancelDataSetButton.setOnAction(this::close);
        hBox2.getChildren().add(cancelDataSetButton);

        this.getDialogPane().setContent(vBox);

        // set the dialog as a utility
        DialogUtils.bindCloseHandler(this, this::close);

    }

    private void close(WindowEvent windowEvent) {
        ConstellationSelected selected = ConstellationSelected.builder().selected(false).build();
        setResult(selected);
    }

    private void close(ActionEvent actionEvent) {
        ConstellationSelected selected = ConstellationSelected.builder().selected(false).build();
        setResult(selected);
    }

    private void searchConstellationClicked(ActionEvent actionEvent) {
        String constellation = constellationChoice.getValue();
        ConstellationSelected selected = ConstellationSelected.builder().selected(true).constellation(constellation).build();
        setResult(selected);
    }

}
