package com.teamgannon.trips.dialogs.query;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.DatabaseManagementService;
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
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationError;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showWarningMessage;

@Slf4j
public class AdvancedQueryDialog extends Dialog<AdvResultsSet> {

    private final TextArea wherePart = new TextArea();

    private final CheckBox plotCheckBox = new CheckBox("Plot Results");
    private final CheckBox viewCheckBox = new CheckBox("View Results as table");

    private ListView<String> fieldsView = new ListView<>();
    private ListView<String> operatorsView = new ListView<>();

    private final TextArea queryErrors = new TextArea();

    private final ChoiceBox<String> datasetChoices = new ChoiceBox<>();

    private final DatabaseManagementService service;
    private final @NotNull Map<String, DataSetDescriptor> dataSetDescriptorMap;

    public AdvancedQueryDialog(DatabaseManagementService service, @NotNull Map<String, DataSetDescriptor> dataSetDescriptorMap) {
        this.service = service;
        this.dataSetDescriptorMap = dataSetDescriptorMap;
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
        datasetChoices.getItems().addAll(dataSetDescriptorMap.keySet());
        datasetChoices.getSelectionModel().selectFirst();
        gridPane.add(datasetChoices, 1, 1);

        Label queryLabel = new Label("Query to run::> ");
        queryLabel.setFont(font);
        gridPane.add(queryLabel, 2, 1);

        Label queryPrefix = new Label("SELECT * FROM STARS WHERE ");
        queryPrefix.setFont(font);
        gridPane.add(queryPrefix, 3, 1);

        fieldsView.setMinWidth(50);
        fieldsView.setPlaceholder(new Label("No Content In List"));
        gridPane.add(fieldsView, 0, 2 );

        operatorsView.setMinWidth(50);
        operatorsView.setPlaceholder(new Label("No Content In List"));
        gridPane.add(operatorsView, 1, 2 );

        wherePart.setMinWidth(300);
        wherePart.setPromptText("Enter query expression, leaving it empty mean you want all stars");
        gridPane.add(wherePart, 2, 2, 2, 1);

        // check boxes
        gridPane.add(plotCheckBox, 0, 3);
        viewCheckBox.setSelected(true);
        gridPane.add(viewCheckBox, 1, 3);
        vBox.getChildren().add(gridPane);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        Button doQueryButton = new Button("run query");
        doQueryButton.setOnAction(this::runquery);
        hBox.getChildren().add(doQueryButton);
        Button cancelButton = new Button("dismiss");
        cancelButton.setOnAction(this::cancelReq);
        hBox.getChildren().add(cancelButton);
        vBox.getChildren().add(hBox);

        queryErrors.setDisable(true);
        queryErrors.setPromptText("query syntax errors will appear here");
        vBox.getChildren().add(queryErrors);

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }

    private void close(WindowEvent windowEvent) {
        AdvResultsSet advResultsSet = AdvResultsSet.builder().dismissed(true).build();
        setResult(advResultsSet);
    }

    private void cancelReq(ActionEvent actionEvent) {
        AdvResultsSet advResultsSet = AdvResultsSet.builder().dismissed(true).build();
        setResult(advResultsSet);
    }

    private void runquery(ActionEvent actionEvent) {
        String queryWherePart = wherePart.getText();

        String datasetName = datasetChoices.getValue();
        if (!datasetName.isEmpty()) {
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
                        List<StarObject> starObjectList = service.runNativeQuery(queryToRun);
                        AdvResultsSet advResultsSet = AdvResultsSet
                                .builder()
                                .queryValid(true)
                                .plotStars(plotCheckBox.isSelected())
                                .viewStars(viewCheckBox.isSelected())
                                .dataSetDescriptor(dataSetDescriptorMap.get(datasetName))
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
            showErrorAlert("Run Advanced Query", "you must enter a query");
        }

    }

}
