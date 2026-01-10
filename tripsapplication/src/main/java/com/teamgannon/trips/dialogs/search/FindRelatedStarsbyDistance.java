package com.teamgannon.trips.dialogs.search;

import com.teamgannon.trips.dialogs.search.model.MultipleStarSearchResults;
import com.teamgannon.trips.dialogs.search.model.SingleStarSelection;
import com.teamgannon.trips.dialogs.search.model.StarDistances;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
import com.teamgannon.trips.utility.DialogUtils;
import javafx.event.ActionEvent;
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

import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static java.lang.Double.parseDouble;

@Slf4j
public class FindRelatedStarsbyDistance extends Dialog<MultipleStarSearchResults> {

    // the star we want to search with
    private final TextField starName = new TextField();

    // the distance from the central star
    private final TextField distance = new TextField();

    // the dataset we want to search with
    private final ChoiceBox<String> datasets = new ChoiceBox<>();
    private final DatabaseManagementService databaseManagementService;
    private final StarService starService;

    public FindRelatedStarsbyDistance(DatabaseManagementService databaseManagementService,
                                      StarService starService,
                                      @NotNull List<String> datasetNames,
                                      DataSetDescriptor dataSetDescriptor) {
        this.databaseManagementService = databaseManagementService;
        this.starService = starService;
        this.setTitle("Find Related Stars by Distance");
        this.setHeight(500);
        this.setWidth(500);

        datasets.getItems().addAll(datasetNames);
        datasets.getSelectionModel().select(dataSetDescriptor.getDataSetName());

        VBox vBox = new VBox();
        GridPane gridPane = new GridPane();
        vBox.getChildren().add(gridPane);
        Label starSearchLabel = new Label("Please enter the partial name to search: ");
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);
        starSearchLabel.setFont(font);
        gridPane.add(starSearchLabel, 0, 1);
        gridPane.add(starName, 1, 1);

        Label datasetLabel = new Label("Please enter dataset name: ");
        datasetLabel.setFont(font);
        gridPane.add(datasetLabel, 0, 2);
        gridPane.add(datasets, 1, 2);

        Label distanceLabel = new Label("Please distance from star (in ly, must be > 3 ly): ");
        distanceLabel.setFont(font);
        gridPane.add(distanceLabel, 0, 3);
        gridPane.add(distance, 1, 3);

        HBox hBox2 = new HBox();
        hBox2.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox2);

        Button gotToStarButton = new Button("find stars");
        gotToStarButton.setOnAction(this::searchStarClicked);
        hBox2.getChildren().add(gotToStarButton);

        Button cancelDataSetButton = new Button("Dismiss");
        cancelDataSetButton.setOnAction(this::close);
        hBox2.getChildren().add(cancelDataSetButton);

        this.getDialogPane().setContent(vBox);

        // set the dialog as a utility
        DialogUtils.bindCloseHandler(this, this::close);

    }

    private void close(WindowEvent windowEvent) {
        MultipleStarSearchResults findResults = MultipleStarSearchResults.builder().starsFound(false).build();
        setResult(findResults);
    }

    private void searchStarClicked(ActionEvent actionEvent) {
        String nameToSearch = starName.getText();
        String dataSetName = datasets.getValue();

        if (!nameToSearch.isEmpty()) {
            double distanceToSearch = 0.0;
            try {
                distanceToSearch = parseDouble(distance.getText());
                if (distanceToSearch < 3.0) {
                    showErrorAlert("find star", "You must enter a valid distance > 3 light years");
                    return;
                }
            } catch (NumberFormatException nfe) {
                showErrorAlert("find star", "You must enter a valid distance > 3 ly");
                return;
            }

            List<StarObject> starObjects = starService.findStarsWithName(dataSetName, nameToSearch);
            log.info("number of stars found ={}", starObjects.size());
            ShowStarMatchesDialog showStarMatchesDialog = new ShowStarMatchesDialog(databaseManagementService, starService, starObjects);
            Optional<SingleStarSelection> starSelection = showStarMatchesDialog.showAndWait();
            if (starSelection.isPresent()) {
                if (starSelection.get().isSelected()) {

                    StarObject starObject = starSelection.get().getStarObject();
                    log.info("star selection ={}", starObject.getDisplayName());

                    List<StarDistances> relatedStars = starService.findStarsWithinDistance(dataSetName, starObject, distanceToSearch);

                    MultipleStarSearchResults findResults = MultipleStarSearchResults
                            .builder()
                            .starsFound(true)
                            .nameToSearch(nameToSearch)
                            .dataSetName(dataSetName)
                            .starObjects(relatedStars)
                            .build();
                    setResult(findResults);
                }
            }
        } else {
            showErrorAlert("find star", "You must enter a full or partial name");
        }
    }

    private void close(ActionEvent actionEvent) {
        MultipleStarSearchResults findResults = MultipleStarSearchResults.builder().starsFound(false).build();
        setResult(findResults);
    }
}
