package com.teamgannon.trips.dialogs.dataset;


import com.teamgannon.trips.config.application.DataSetContext;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.DataSetChangeListener;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Slf4j
public class SelectActiveDatasetDialog extends Dialog<Integer> {

    private final Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

    private final DataSetChangeListener dataSetChangeListener;
    private final DataSetContext dataSetContext;
    private final ComboBox<DataSetDescriptor> descriptorComboBox = new ComboBox<>();
    private final DatabaseManagementService databaseManagementService;


    public SelectActiveDatasetDialog(DataSetChangeListener dataSetChangeListener,
                                     DataSetContext dataSetContext,
                                     DatabaseManagementService databaseManagementService) {

        this.dataSetChangeListener = dataSetChangeListener;
        this.dataSetContext = dataSetContext;
        this.databaseManagementService = databaseManagementService;

        this.setTitle("Dataset Management Dialog");
        this.setWidth(700);
        this.setHeight(500);

        VBox vBox = new VBox();
        this.getDialogPane().setContent(vBox);

        createSelectedDatasetContext(vBox);

        updateTable();

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }

    private void updateTable() {

        // get descriptors
        List<DataSetDescriptor> descriptors = databaseManagementService.getDataSets();

        // fill in table
        descriptors.forEach(descriptor -> {
            descriptorComboBox.getItems().add(descriptor);
        });

    }


    private void createSelectedDatasetContext(@NotNull VBox vBox) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        Label contextSettingLabel = new Label("Active Dataset: ");
        contextSettingLabel.setFont(font);
        hBox.getChildren().addAll(contextSettingLabel, new Separator(), descriptorComboBox);
        vBox.getChildren().add(hBox);
        vBox.getChildren().addAll(new Separator());
        if (dataSetContext.isValidDescriptor()) {
            descriptorComboBox.setValue(dataSetContext.getDescriptor());
        }
        descriptorComboBox.setCellFactory(new ComboBoxDatasetCellFactory());

        // THIS IS NEEDED because providing a custom cell factory is NOT enough. You also need a specific set button cell
        descriptorComboBox.setButtonCell(new ListCell<>() {

            @Override
            protected void updateItem(@Nullable DataSetDescriptor item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item.getDataSetName());
                } else {
                    setText(null);
                }
            }
        });

        descriptorComboBox.setOnAction(e -> dataSetChangeListener.setContextDataSet(descriptorComboBox.getValue()));

        hBox.getChildren().add(new Separator());
        Button okButton = new Button("Ok");
        okButton.setOnAction(this::close);
        hBox.getChildren().add(okButton);

    }

    /**
     * close the dialog from stage x button
     *
     * @param we the windows event
     */
    private void close(WindowEvent we) {
        setResult(1);
    }


    /**
     * close triggered by button event
     *
     * @param actionEvent the action event
     */
    private void close(ActionEvent actionEvent) {
        setResult(1);
    }

}
