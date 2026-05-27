package com.teamgannon.trips.dataset;

import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.dialogs.support.DataFileFormat;
import com.teamgannon.trips.dialogs.support.DataFormatEnum;
import com.teamgannon.trips.javafxsupport.InlineFieldValidation;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.DatasetService;
import com.teamgannon.trips.utility.DialogUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AddDataSetDialog extends Dialog<Dataset> {

    private final TextField dataSetName = new TextField();
    private final ChoiceBox<String> dataSetType = new ChoiceBox<>();
    private final TextField dataSetAuthor = new TextField();
    private final TextArea notes = new TextArea();
    private final TextField fileSelected = new TextField();

    /**
     * Shared inline-validation error label (Issue 29 / Bucket A) — replaces
     * the 6 modal Alert popups that fired one-per-required-field. The label
     * sits below the grid; each validation block calls
     * {@link InlineFieldValidation#attachError} to point the user at the
     * offending field with a red border and put the message in this label.
     */
    private final Label validationError = new Label();
    private final Dataset dataSet = new Dataset();
    private final Map<DataFormatEnum, DataFileFormat> dataFileFormats = new HashMap<>();
    private final Localization localization;
    private final DatabaseManagementService databaseManagementService;
    private final DatasetService datasetService;
    public @NotNull Button addDataSetButton = new Button("Add Dataset");


    public AddDataSetDialog(Localization localization,
                            DatabaseManagementService databaseManagementService,
                            DatasetService datasetService) {

        this.localization = localization;
        this.databaseManagementService = databaseManagementService;
        this.datasetService = datasetService;

        this.setHeight(450);
        this.setWidth(550);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10.0);
        this.getDialogPane().setContent(vBox);
        this.setTitle("Add a Dataset");

        GridPane gridPane = new GridPane();
        gridPane.setVgap(5);
        gridPane.setHgap(10);

        // Set column constraints so labels don't get truncated
        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setMinWidth(100);
        labelColumn.setPrefWidth(100);

        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setHgrow(Priority.ALWAYS);
        fieldColumn.setMinWidth(350);

        gridPane.getColumnConstraints().addAll(labelColumn, fieldColumn);
        vBox.getChildren().add(gridPane);

        Label dataSetTypeLabel = new Label("Dataset Type:");
        gridPane.add(dataSetTypeLabel, 0, 0);
        addChoices();

        HBox hBox = new HBox();
        dataSetType.setValue(DataFormatEnum.CSV.getValue());
        hBox.getChildren().add(dataSetType);
        hBox.getChildren().add(new Label("   "));

        Button loadDataSetButton = new Button("Select File");
        loadDataSetButton.setOnAction(this::loadDataSetClicked);
        hBox.getChildren().add(loadDataSetButton);
        gridPane.add(hBox, 1, 0);

        Label dataSetFileSelectedLabel = new Label("Dataset File:");
        gridPane.add(dataSetFileSelectedLabel, 0, 1);
        fileSelected.setPromptText("use button to navigate to file, or enter it");
        gridPane.add(fileSelected, 1, 1);

        Label dataSetNameLabel = new Label("Dataset Name:");
        gridPane.add(dataSetNameLabel, 0, 2);
        dataSetName.setText("newDataset1");
        gridPane.add(dataSetName, 1, 2);
        dataSetName.setPromptText("Use \"Select File\" below of enter full path to file");
        Tooltip tooltipDataSetName = new Tooltip("Use \"Select File\" below of enter full path to file");
        dataSetName.setTooltip(tooltipDataSetName);


        Label dataSetAuthorLabel = new Label("Author:");
        gridPane.add(dataSetAuthorLabel, 0, 3);
        dataSetAuthor.setText("Anonymous");
        gridPane.add(dataSetAuthor, 1, 3);

        Label dataSetNotesLabel = new Label("Notes:");
        gridPane.add(dataSetNotesLabel, 0, 4);
        notes.setMinSize(200, 60);
        notes.setPromptText("add descriptive information for this entry");
        gridPane.add(notes, 1, 4);

        // Inline validation status (Issue 29 / Bucket A). Hidden by default;
        // shown by InlineFieldValidation.attachError when a required field
        // fails validation on submit.
        validationError.setVisible(false);
        validationError.setManaged(false);
        vBox.getChildren().add(validationError);

        HBox hBox5 = new HBox();
        hBox5.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox5);

        addDataSetButton.setDisable(true);
        addDataSetButton.setOnAction(this::addDataSetClicked);
        hBox5.getChildren().add(addDataSetButton);

        Button cancelDataSetButton = new Button("Cancel");
        cancelDataSetButton.setOnAction(this::close);
        hBox5.getChildren().add(cancelDataSetButton);

        DialogUtils.bindCloseHandler(this, this::close);
    }

    /**
     * close the dialog from the close button
     *
     * @param actionEvent the action event
     */
    private void close(ActionEvent actionEvent) {
        setResult(new Dataset());
        close();
    }

    /**
     * close the dialog from stage x button
     *
     * @param we the windows event
     */
    private void close(WindowEvent we) {
        setResult(new Dataset());
        close();
    }

    private void addChoices() {
        addFormat(DataFormatEnum.CH_VIEW, "chv");
        addFormat(DataFormatEnum.CSV, "trips.csv");
    }

    private void addFormat(DataFormatEnum fileType, String suffix) {
        DataFileFormat dataFileFormat = new DataFileFormat();
        dataFileFormat.setDataFormatEnum(fileType);
        dataFileFormat.setSuffix(suffix);
        dataFileFormats.put(dataFileFormat.getDataFormatEnum(), dataFileFormat);
        dataSetType.getItems().add(dataFileFormat.getDataFormatEnum().getValue());
    }

    private void addDataSetClicked(ActionEvent actionEvent) {
        // pull the data from the controls
        getData();

        // Inline validation (Issue 29 / Bucket A): each branch points the
        // user at the offending field with a red border + an inline message
        // instead of stacking modal Alert popups.
        InlineFieldValidation.clearError(dataSetName, validationError);
        InlineFieldValidation.clearError(dataSetType, validationError);
        InlineFieldValidation.clearError(dataSetAuthor, validationError);
        InlineFieldValidation.clearError(fileSelected, validationError);

        if (dataSet.getName().isEmpty()) {
            InlineFieldValidation.attachError(dataSetName, validationError, "Dataset name is required.");
            return;
        }
        if (datasetService.hasDataSet(dataSet.getName())) {
            InlineFieldValidation.attachError(dataSetName, validationError, "A dataset with this name already exists.");
            return;
        }
        if (dataSet.getDataType() == null) {
            InlineFieldValidation.attachError(dataSetType, validationError, "Dataset type is required.");
            return;
        }
        if (dataSet.getAuthor().isEmpty()) {
            InlineFieldValidation.attachError(dataSetAuthor, validationError, "Dataset author is required.");
            return;
        }
        if (dataSet.getFileSelected().isEmpty()) {
            InlineFieldValidation.attachError(fileSelected, validationError, "Dataset file is required.");
            return;
        }
        if (checkFileDNExists(dataSet.getFileSelected())) {
            InlineFieldValidation.attachError(fileSelected, validationError, "Selected file does not exist on disk.");
            return;
        }

        // the result for return
        setResult(dataSet);
    }

    private void loadDataSetClicked(ActionEvent actionEvent) {
        String selectFileType = dataSetType.getValue();
        if (selectFileType == null) {
            // Inline (Issue 29 / Bucket A): point the user at the dataSetType
            // ChoiceBox rather than firing a modal Alert.
            InlineFieldValidation.attachError(dataSetType, validationError,
                    "Pick a dataset type before selecting a file.");
            return;
        }
        String fileName = fileSelected.getText();
        if (!fileName.isEmpty()) {
            if (checkFileDNExists(fileName)) {
                showErrorAlert("Add Dataset", "This file does not exist!");
                return;
            }
        } else {
            // show file selection dialog
            if (!showFileDialog(selectFileType)) {
                return;
            }
        }

        addDataSetButton.setDisable(false);
    }

    private boolean showFileDialog(String selectFileType) {
        DataFormatEnum formatEnum = DataFormatEnum.fromString(selectFileType);
        return chooseFile(dataFileFormats.get(formatEnum));
    }

    public boolean chooseFile(@NotNull DataFileFormat dataFileFormat) {
        log.debug("Import a {} format file", dataFileFormat.getDataFormatEnum().getValue());
        final FileChooser fileChooser = new FileChooser();
        String title = String.format("Select %s file to import", dataFileFormat.getDataFormatEnum().getValue());
        fileChooser.setTitle(title);
        File filesFolder = new File(localization.getFileDirectory());
        if (!filesFolder.exists()) {
            boolean created = filesFolder.mkdirs();
            if (!created) {
                log.error("data files folder did not exist, but attempt to create directories failed");
                showErrorAlert("Add Dataset ", "files folder did not exist, but attempt to create directories failed");
            }
        }
        if (filesFolder.exists()) {
            fileChooser.setInitialDirectory(filesFolder);
        } else {
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home", ".")));
        }
        FileChooser.ExtensionFilter filter = selectExtensionFilter(dataFileFormat.getDataFormatEnum());
        fileChooser.getExtensionFilters().add(filter);
        File file = fileChooser.showOpenDialog(getDialogPane().getScene() != null
                ? getDialogPane().getScene().getWindow()
                : null);
        if (file != null) {
            fileSelected.setText(file.getAbsolutePath());
            dataSet.setDataType(dataFileFormat);
            return true;
        } else {
            log.warn("file selection cancelled");
            return false;
        }
    }

    @NotNull
    private FileChooser.ExtensionFilter selectExtensionFilter(DataFormatEnum dataFormatEnum) {
        switch (dataFormatEnum) {

            case CH_VIEW -> {
                return new FileChooser.ExtensionFilter("CH View files (*.chv)", "*.chv");
            }
            case CSV -> {
                return new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
            }
            default -> {
                return new FileChooser.ExtensionFilter("All files (*.*)", "*.*");
            }
        }
    }

    /**
     * show an error alert
     *
     * @param title the title
     * @param error the error
     */
    private void showErrorAlert(String title,
                                String error) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(error);
        alert.showAndWait();
        log.error(error);
    }

    private void getData() {
        dataSet.setName(dataSetName.getText());
        dataSet.setFileSelected(fileSelected.getText());
        dataSet.setNotes(notes.getText());
        dataSet.setAuthor(dataSetAuthor.getText());
        String selectedFormat = dataSetType.getValue();
        if (selectedFormat != null) {
            DataFormatEnum formatEnum = DataFormatEnum.fromString(selectedFormat);
            if (formatEnum != null) {
                dataSet.setDataType(dataFileFormats.get(formatEnum));
            }
        }
    }

    private boolean checkFileDNExists(@NotNull String filePath) {
        return !new File(filePath).exists();
    }

}
