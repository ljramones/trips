package com.teamgannon.trips.dialogs.dataset;


import com.teamgannon.trips.config.application.model.DataSetContext;
import com.teamgannon.trips.events.ShowStellarDataEvent;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.DatasetService;
import com.teamgannon.trips.utility.DialogUtils;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;

@Slf4j
public class SelectActiveDatasetDialog extends Dialog<Boolean> {

    private final Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

    private final ApplicationEventPublisher eventPublisher;
    private final DataSetContext dataSetContext;
    private final ListView<DataSetDescriptor> descriptorListView = new ListView<>();
    private final DatabaseManagementService databaseManagementService;
    private final DatasetService datasetService;

    public SelectActiveDatasetDialog(ApplicationEventPublisher eventPublisher,
                                     DataSetContext dataSetContext,
                                     DatabaseManagementService databaseManagementService,
                                     DatasetService datasetService) {

        this.eventPublisher = eventPublisher;
        this.dataSetContext = dataSetContext;
        this.databaseManagementService = databaseManagementService;
        this.datasetService = datasetService;

        this.setTitle("Dataset Management Dialog");
        this.setWidth(700);
        this.setHeight(200);

        VBox vBox = new VBox();
        this.getDialogPane().setContent(vBox);

        createSelectedDatasetContext(vBox);

        updateTable();

        // set the dialog as a utility
        DialogUtils.bindCloseHandler(this, this::close);
    }

    private void updateTable() {
        // get descriptors
        List<DataSetDescriptor> descriptors = datasetService.getDataSets();
        // fill in table
        descriptorListView.setItems(FXCollections.observableArrayList(descriptors));
    }

    private void createSelectedDatasetContext(@NotNull VBox vBox) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        Label contextSettingLabel = new Label("Active Dataset: ");
        contextSettingLabel.setFont(font);
        hBox.getChildren().addAll(contextSettingLabel, new Separator(), descriptorListView);
        vBox.getChildren().add(hBox);
        vBox.getChildren().addAll(new Separator());

        // Set the maximum height of the ListView.
        descriptorListView.setMaxHeight(500); // You can adjust this value as needed.

        // Set the cell factory and the mouse click handler as before.
        descriptorListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<DataSetDescriptor> call(ListView<DataSetDescriptor> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(DataSetDescriptor item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null && !empty) {
                            setText(item.getDataSetName());
                        } else {
                            setText(null);
                        }
                    }
                };
            }
        });

        descriptorListView.setOnMouseClicked(mouseEvent -> {
            DataSetDescriptor selected = descriptorListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                log.info("selected dataset: {}", selected.getDataSetName());
                eventPublisher.publishEvent(new ShowStellarDataEvent(this, selected, true, false));
                setResult(true);
            }
        });

        descriptorListView.itemsProperty().addListener((observable, oldValue, newValue) -> {
            // Calculate and set the preferred height of the ListView.
            int itemsCount = newValue.size();
            double cellHeight = 25; // Adjust this value based on your cell's actual height.
            double newHeight = itemsCount * cellHeight;

            // Set a maximum limit to the height.
            double maxHeight = 500; // Set this to the maximum height you want for the ListView.
            if (newHeight > maxHeight) {
                newHeight = maxHeight;
            }

            // Update the height of the ListView.
            descriptorListView.setPrefHeight(newHeight);

            // Optionally: Adjust the dialog's height if needed.
            // You might need to consider other UI elements' heights and margins in your calculation.
            SelectActiveDatasetDialog.this.getDialogPane().setPrefHeight(newHeight + 100); // Adjust the additional height as needed.
        });
    }



    private void close(WindowEvent we) {
        setResult(true);
    }
}
