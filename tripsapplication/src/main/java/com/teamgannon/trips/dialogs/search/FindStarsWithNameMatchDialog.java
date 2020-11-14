package com.teamgannon.trips.dialogs.search;

import com.teamgannon.trips.dialogs.search.model.StarSearchResults;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.List;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

public class FindStarsWithNameMatchDialog extends Dialog<StarSearchResults> {

    private final TextField starName = new TextField();
    private final ChoiceBox<String> datasets = new ChoiceBox<>();

    public FindStarsWithNameMatchDialog(List<String> datasetNames) {
        this.setTitle("Find a star in database");
        this.setHeight(500);
        this.setWidth(300);

        datasets.getItems().addAll(datasetNames);
        datasets.getSelectionModel().select(0);

        VBox vBox = new VBox();
        HBox hBox = new HBox();
        Label starSearchLabel = new Label("Please enter the partial name to search: ");
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);
        starSearchLabel.setFont(font);
        hBox.getChildren().addAll(starSearchLabel, starName);
        vBox.getChildren().add(hBox);

        HBox hBox1 = new HBox();
        hBox1.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox1);

        Label datasetLabel = new Label("Please enter dataset name: ");
        hBox1.getChildren().addAll(datasetLabel, datasets);

        HBox hBox2 = new HBox();
        hBox2.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox2);

        Button gotToStarButton = new Button("find star");
        gotToStarButton.setOnAction(this::searchStarClicked);
        hBox2.getChildren().add(gotToStarButton);

        Button cancelDataSetButton = new Button("Dismiss");
        cancelDataSetButton.setOnAction(this::close);
        hBox2.getChildren().add(cancelDataSetButton);

        this.getDialogPane().setContent(vBox);

    }

    private void searchStarClicked(ActionEvent actionEvent) {
        String nameToSearch = starName.getText();
        String dataSetName = datasets.getValue();
        if (!nameToSearch.isEmpty()) {
            StarSearchResults findResults = StarSearchResults
                    .builder()
                    .starsFound(true)
                    .nameToSearch(nameToSearch)
                    .dataSetName(dataSetName)
                    .build();
            setResult(findResults);
        } else {
            showErrorAlert("find star", "You must enter a partial name");
        }
    }

    private void close(ActionEvent actionEvent) {
        StarSearchResults findResults = StarSearchResults.builder().starsFound(false).build();
        setResult(findResults);
    }

}
