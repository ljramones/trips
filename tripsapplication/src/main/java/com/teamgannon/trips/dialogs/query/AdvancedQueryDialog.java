package com.teamgannon.trips.dialogs.query;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.StarService;
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
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationError;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showWarningMessage;

@Slf4j
public class AdvancedQueryDialog extends Dialog<AdvResultsSet> {

    private final TextArea wherePart = new TextArea();

    private final CheckBox plotCheckBox = new CheckBox("Plot Results");
    private final CheckBox viewCheckBox = new CheckBox("View Results as table");

    private final TextArea queryErrors = new TextArea();

    private final ChoiceBox<String> datasetChoices = new ChoiceBox<>();
    QueryFields queryFields = new QueryFields();
    private final DatabaseManagementService service;
    private final StarService starService;
    private final TripsContext tripsContext;

    public AdvancedQueryDialog(DatabaseManagementService service,
                               StarService starService,
                               TripsContext tripsContext) {

        this.service = service;
        this.starService = starService;
        this.tripsContext = tripsContext;
        VBox vBox = new VBox();
        this.getDialogPane().setContent(vBox);

        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        gridPane.setPrefWidth(900);

        Label datasetLabel = new Label("Dataset to query: ");
        datasetLabel.setFont(font);
        gridPane.add(datasetLabel, 0, 1);
        gridPane.add(datasetChoices, 1, 1);
        updateDatasetDescriptors();
        Label queryLabel = new Label("Query to run::> ");
        queryLabel.setFont(font);
        gridPane.add(queryLabel, 2, 1);

        Label queryPrefix = new Label("SELECT * FROM STARS WHERE ");
        queryPrefix.setFont(font);
        gridPane.add(queryPrefix, 3, 1);

        Label fieldsLabel = new Label("Fields to query on");
        fieldsLabel.setFont(font);
        Label operatorsLabel = new Label("Operators to use");
        operatorsLabel.setFont(font);
        gridPane.add(fieldsLabel, 0, 2);
        gridPane.add(operatorsLabel, 1, 2);

        ListView<String> fieldsView = new ListView<>();
        fieldsView.setMinWidth(50);

        fieldsView.getItems().addAll(queryFields.getStarObjectFields());
        gridPane.add(fieldsView, 0, 3);

        ListView<String> operatorsView = new ListView<>();
        operatorsView.setMinWidth(50);
        operatorsView.getItems().addAll(queryFields.getOperators());
        gridPane.add(operatorsView, 1, 3);

        wherePart.setMinWidth(300);
        wherePart.setPromptText("Enter query expression, leaving it empty means you want all stars");
        gridPane.add(wherePart, 2, 3, 2, 1);

        // check boxes
        gridPane.add(plotCheckBox, 0, 4);
        viewCheckBox.setSelected(true);
        gridPane.add(viewCheckBox, 1, 4);
        vBox.getChildren().add(gridPane);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        Button examplesButton = new Button("Show Examples");
        examplesButton.setOnAction(this::showExamples);
        hBox.getChildren().add(examplesButton);
        Button doQueryButton = new Button("Run Query");
        doQueryButton.setOnAction(this::runQuery);
        hBox.getChildren().add(doQueryButton);
        Button cancelButton = new Button("Ok");
        cancelButton.setOnAction(this::cancelReq);
        hBox.getChildren().add(cancelButton);
        vBox.getChildren().add(hBox);

        queryErrors.setDisable(true);
        queryErrors.setPromptText("query syntax errors will appear here");
        vBox.getChildren().add(queryErrors);

        // set the dialog as a utility
        DialogUtils.bindCloseHandler(this, this::close);
    }

    public void updateDatasetDescriptors() {
        datasetChoices.getItems().clear();
        datasetChoices.getItems().addAll(tripsContext.getSearchContext().getDatasetMap().keySet());
        if (tripsContext.getDataSetDescriptor() != null) {
            datasetChoices.getSelectionModel().select(tripsContext.getDataSetDescriptor().getDataSetName());
        } else {
            datasetChoices.getSelectionModel().selectFirst();
        }
    }

    private void showExamples(ActionEvent actionEvent) {
        ExamplesDialog dialog = new ExamplesDialog(queryFields.getExamples());
        dialog.show();
    }

    private void close(WindowEvent windowEvent) {
        AdvResultsSet advResultsSet = AdvResultsSet.builder().dismissed(true).build();
        setResult(advResultsSet);
        close();
    }

    private void cancelReq(ActionEvent actionEvent) {
        AdvResultsSet advResultsSet = AdvResultsSet.builder().dismissed(true).build();
        setResult(advResultsSet);
    }

    private void runQuery(ActionEvent actionEvent) {
        String queryWherePart = wherePart.getText();

        String datasetName = datasetChoices.getValue();
        if (datasetName != null && !datasetName.isEmpty()) {
            String queryPrefix = "SELECT * FROM STAR_OBJ WHERE ";
            String datasetQuery = "DATA_SET_NAME='" + datasetName + "' ";
            String queryToRun = queryPrefix + datasetQuery;

            if (queryWherePart.isEmpty()) {
                showWarningMessage("Run Advanced Query", "this will get all stars");
            } else {
                queryToRun += " AND " + queryWherePart;
            }

            log.info("query is ::  {}", queryToRun);
            Validation validation = new Validation(Collections.singletonList(DatabaseType.H2), queryToRun);
            List<ValidationError> errors = validation.validate();
            if (errors.size() > 0) {
                String stringBuilder = errors.stream().map(error -> error.toString() + "\n").collect(Collectors.joining());
                queryErrors.setText(stringBuilder);
            } else {
                if (plotCheckBox.isSelected() || viewCheckBox.isSelected()) {
                    try {
                        List<StarObject> starObjectList = starService.runNativeQuery(queryToRun);
                        AdvResultsSet advResultsSet = AdvResultsSet
                                .builder()
                                .queryValid(true)
                                .plotStars(plotCheckBox.isSelected())
                                .viewStars(viewCheckBox.isSelected())
                                .dataSetDescriptor(tripsContext.getSearchContext().getDatasetMap().get(datasetName))
                                .resultsFound(starObjectList.size() > 0)
                                .starsFound(starObjectList)
                                .build();
                        setResult(advResultsSet);
                    } catch (Exception e) {
                        showErrorAlert("Run Advanced Query", "failed: " + e.getMessage());
                    }
                } else {
                    showErrorAlert("Run Advanced Query", "either plot or view must be selected");
                }
            }
        } else {
            showErrorAlert("Run Advanced Query", "You must enter a query");
        }

    }

}
