package com.teamgannon.trips.search.components;

import com.teamgannon.trips.config.application.DataSetContext;
import com.teamgannon.trips.dialogs.dataset.DataSetDescribeDialog;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.search.SearchContext;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class DataSetPanel extends BasePane {

    private final ChoiceBox<String> datasetChoiceBox = new ChoiceBox<>();
    private final Map<String, DataSetDescriptor> datasets;

    private final SearchContext searchContext;


    public DataSetPanel(SearchContext searchContext, DataSetContext dataSetContext) {
        this.searchContext = searchContext;
        this.datasets = searchContext.getDatasetMap();

        for (DataSetDescriptor dataset : datasets.values()) {
            datasetChoiceBox.getItems().add(dataset.getDataSetName());
        }

        Label rowLabel = createLabel("DataSet to use: ");
        if (dataSetContext.isValidDescriptor()) {
            datasetChoiceBox.setValue(dataSetContext.getDescriptor().getDataSetName());
        }


        Separator space = new Separator();
        space.setMinSize(50, 1);

        Button whatButton = new Button("Describe...");
        whatButton.setOnAction(this::describeButtonClicked);

        planGrid.add(rowLabel, 0, 0);
        planGrid.add(datasetChoiceBox, 1, 0);
        planGrid.add(space, 2, 0);
        planGrid.add(whatButton, 3, 0);

        this.getChildren().add(planGrid);
    }

    private void describeButtonClicked(ActionEvent actionEvent) {
        log.info("Describe button clicked");

        String name = datasetChoiceBox.getValue();

        if (datasets.size() == 0) {
            showErrorAlert("Add Dataset", "There are no datasets loaded, please add one!");
        } else {
            if (name != null) {
                DataSetDescriptor descriptor = datasets.get(name);
                DataSetDescribeDialog dialog = new DataSetDescribeDialog(descriptor);
                searchContext.setCurrentDataSet(name);
                searchContext.getAstroSearchQuery().setDescriptor(descriptor);
                dialog.showAndWait();
            } else {
                showErrorAlert("Add Dataset", "Dataset name cannot be empty!");
            }
        }
    }

    public String getSelected() {
        return datasetChoiceBox.getValue();
    }


}
