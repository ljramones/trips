package com.teamgannon.trips.search.components;

import com.teamgannon.trips.dialogs.dataset.DataSetDescribeDialog;
import com.teamgannon.trips.events.SetContextDataSetEvent;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.search.SearchContext;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Map;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class DataSetPanel extends BasePane {

    private final ChoiceBox<String> datasetChoiceBox = new ChoiceBox<>();
    private final Map<String, DataSetDescriptor> datasets;
    private final ApplicationEventPublisher eventPublisher;

    private final @NotNull SearchContext searchContext;

    /**
     * constructor
     *
     * @param searchContext  the search context
     * @param eventPublisher the event publisher
     */
    public DataSetPanel(@NotNull SearchContext searchContext,
                        ApplicationEventPublisher eventPublisher) {

        this.searchContext = searchContext;
        this.datasets = searchContext.getDatasetMap();
        this.eventPublisher = eventPublisher;

        for (DataSetDescriptor dataset : datasets.values()) {
            datasetChoiceBox.getItems().add(dataset.getDataSetName());
        }

        Label rowLabel = createLabel("DataSet to use: ");
        if (searchContext.getDataSetContext().isValidDescriptor()) {
            datasetChoiceBox.setValue(searchContext.getDataSetDescriptor().getDataSetName());
            searchContext.getAstroSearchQuery().setDescriptor(searchContext.getDataSetDescriptor());
        }
        datasetChoiceBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> setNewDataSet(newValue));

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

    private void setNewDataSet(String newValue) {
        DataSetDescriptor descriptor = datasets.get(newValue);
        eventPublisher.publishEvent(new SetContextDataSetEvent(this, descriptor));
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

    public DataSetDescriptor getSelected() {
        return datasets.get(datasetChoiceBox.getValue());
    }

    public void setDataSetContext(@NotNull DataSetDescriptor descriptor) {
        datasetChoiceBox.setValue(descriptor.getDataSetName());
    }

    public void updateDataContext(@NotNull DataSetDescriptor dataSetDescriptor) {
        datasets.put(dataSetDescriptor.getDataSetName(), dataSetDescriptor);
        datasetChoiceBox.getItems().add(dataSetDescriptor.getDataSetName());
    }

    public void removeDataset(DataSetDescriptor dataSetDescriptor) {
        datasetChoiceBox.getItems().remove(dataSetDescriptor.getDataSetName());
        String name= dataSetDescriptor.getDataSetName();
        if (datasetChoiceBox.getValue().equals(name)) {
            datasetChoiceBox.getSelectionModel().select(0);
        }
    }
}
