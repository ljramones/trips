package com.teamgannon.trips.nebula.dialogs;

import com.teamgannon.trips.nebula.model.Nebula;
import com.teamgannon.trips.nebula.model.NebulaType;
import com.teamgannon.trips.nebula.service.NebulaService;
import com.teamgannon.trips.utility.DialogUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.WindowEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;
import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Dialog for managing nebulae in a dataset.
 * <p>
 * Provides a table view of all nebulae with options to:
 * - Add new nebulae
 * - Edit selected nebula
 * - Delete selected nebula
 * - Duplicate selected nebula
 */
@Slf4j
public class NebulaListDialog extends Dialog<NebulaListDialog.Result> {

    private final Font titleFont = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

    private final NebulaService nebulaService;
    private final String datasetName;

    private final TableView<Nebula> tableView = new TableView<>();

    private final Button editButton = new Button("Edit...");
    private final Button deleteButton = new Button("Delete");
    private final Button duplicateButton = new Button("Duplicate");

    @Getter
    private boolean nebulaeModified = false;

    private @Nullable Nebula selectedNebula;

    /**
     * Result returned by the dialog.
     */
    public record Result(boolean modified, int nebulaCount) {
    }

    public NebulaListDialog(NebulaService nebulaService, String datasetName) {
        this.nebulaService = nebulaService;
        this.datasetName = datasetName;

        setTitle("Manage Nebulae - " + datasetName);
        setWidth(750);
        setHeight(450);

        VBox vBox = new VBox(10);
        vBox.setPadding(new Insets(10));
        getDialogPane().setContent(vBox);

        createHeader(vBox);
        createTable(vBox);
        createButtonPanel(vBox);

        updateTable();

        DialogUtils.bindCloseHandler(this, this::handleClose);
    }

    private void createHeader(VBox vBox) {
        HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Nebulae in Dataset: " + datasetName);
        titleLabel.setFont(titleFont);
        hBox.getChildren().add(titleLabel);

        // Add filter by type combo
        ComboBox<String> typeFilter = new ComboBox<>();
        typeFilter.getItems().add("All Types");
        for (NebulaType type : NebulaType.values()) {
            typeFilter.getItems().add(type.getDisplayName());
        }
        typeFilter.setValue("All Types");
        typeFilter.setOnAction(e -> filterByType(typeFilter.getValue()));

        Label filterLabel = new Label("Filter:");
        hBox.getChildren().addAll(filterLabel, typeFilter);

        vBox.getChildren().add(hBox);
        vBox.getChildren().add(new Separator());
    }

    private void createTable(VBox vBox) {
        tableView.setPrefWidth(700);
        tableView.setPrefHeight(280);

        // Name column
        TableColumn<Nebula, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(150);
        tableView.getColumns().add(nameColumn);

        // Type column
        TableColumn<Nebula, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getType().getDisplayName()));
        typeColumn.setPrefWidth(120);
        tableView.getColumns().add(typeColumn);

        // Position column
        TableColumn<Nebula, String> positionColumn = new TableColumn<>("Position (ly)");
        positionColumn.setCellValueFactory(cell -> {
            Nebula n = cell.getValue();
            return new SimpleStringProperty(String.format("(%.1f, %.1f, %.1f)",
                    n.getCenterX(), n.getCenterY(), n.getCenterZ()));
        });
        positionColumn.setPrefWidth(140);
        tableView.getColumns().add(positionColumn);

        // Radius column
        TableColumn<Nebula, String> radiusColumn = new TableColumn<>("Radius (ly)");
        radiusColumn.setCellValueFactory(cell -> {
            Nebula n = cell.getValue();
            if (n.getInnerRadius() > 0) {
                return new SimpleStringProperty(String.format("%.1f - %.1f",
                        n.getInnerRadius(), n.getOuterRadius()));
            } else {
                return new SimpleStringProperty(String.format("%.1f", n.getOuterRadius()));
            }
        });
        radiusColumn.setPrefWidth(90);
        tableView.getColumns().add(radiusColumn);

        // Catalog ID column
        TableColumn<Nebula, String> catalogColumn = new TableColumn<>("Catalog ID");
        catalogColumn.setCellValueFactory(cell -> {
            String id = cell.getValue().getCatalogId();
            return new SimpleStringProperty(id != null ? id : "");
        });
        catalogColumn.setPrefWidth(80);
        tableView.getColumns().add(catalogColumn);

        // Source column
        TableColumn<Nebula, String> sourceColumn = new TableColumn<>("Source");
        sourceColumn.setCellValueFactory(cell -> {
            String src = cell.getValue().getSourceCatalog();
            return new SimpleStringProperty(src != null ? src : "User");
        });
        sourceColumn.setPrefWidth(80);
        tableView.getColumns().add(sourceColumn);

        // Set placeholder
        tableView.setPlaceholder(new Label("No nebulae in this dataset"));

        // Selection model
        TableView.TableViewSelectionModel<Nebula> selectionModel = tableView.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);

        ObservableList<Nebula> selectedItems = selectionModel.getSelectedItems();
        selectedItems.addListener(this::onSelectionChanged);

        // Double-click to edit
        tableView.setRowFactory(tv -> {
            TableRow<Nebula> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    editNebula(null);
                }
            });
            return row;
        });

        vBox.getChildren().add(tableView);
    }

    private void createButtonPanel(VBox vBox) {
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button addButton = new Button("Add New...");
        addButton.setOnAction(this::addNebula);
        buttonBox.getChildren().add(addButton);

        editButton.setOnAction(this::editNebula);
        editButton.setDisable(true);
        buttonBox.getChildren().add(editButton);

        duplicateButton.setOnAction(this::duplicateNebula);
        duplicateButton.setDisable(true);
        buttonBox.getChildren().add(duplicateButton);

        deleteButton.setOnAction(this::deleteNebula);
        deleteButton.setDisable(true);
        buttonBox.getChildren().add(deleteButton);

        // Spacer
        buttonBox.getChildren().add(new Separator());

        Button closeButton = new Button("Close");
        closeButton.setOnAction(this::handleCloseButton);
        buttonBox.getChildren().add(closeButton);

        vBox.getChildren().add(buttonBox);
    }

    private void onSelectionChanged(ListChangeListener.Change<? extends Nebula> change) {
        ObservableList<Nebula> selectedItems = (ObservableList<Nebula>) change.getList();
        if (!selectedItems.isEmpty()) {
            selectedNebula = selectedItems.get(0);
            editButton.setDisable(false);
            deleteButton.setDisable(false);
            duplicateButton.setDisable(false);
        } else {
            selectedNebula = null;
            editButton.setDisable(true);
            deleteButton.setDisable(true);
            duplicateButton.setDisable(true);
        }
    }

    private void updateTable() {
        tableView.getItems().clear();
        List<Nebula> nebulae = nebulaService.findByDataset(datasetName);
        tableView.getItems().addAll(nebulae);
    }

    private void filterByType(String typeFilter) {
        tableView.getItems().clear();

        if ("All Types".equals(typeFilter)) {
            List<Nebula> nebulae = nebulaService.findByDataset(datasetName);
            tableView.getItems().addAll(nebulae);
        } else {
            // Find matching type
            for (NebulaType type : NebulaType.values()) {
                if (type.getDisplayName().equals(typeFilter)) {
                    List<Nebula> nebulae = nebulaService.findByDatasetAndType(datasetName, type);
                    tableView.getItems().addAll(nebulae);
                    break;
                }
            }
        }
    }

    private void addNebula(ActionEvent event) {
        NebulaEditorDialog dialog = new NebulaEditorDialog(datasetName);
        Optional<Nebula> result = dialog.showAndWait();

        if (result.isPresent()) {
            Nebula nebula = result.get();
            nebulaService.save(nebula);
            nebulaeModified = true;
            updateTable();
            log.info("Added nebula: {}", nebula.getName());
        }
    }

    private void editNebula(ActionEvent event) {
        if (selectedNebula == null) {
            showErrorAlert("Edit Nebula", "Please select a nebula first");
            return;
        }

        NebulaEditorDialog dialog = new NebulaEditorDialog(selectedNebula, datasetName);
        Optional<Nebula> result = dialog.showAndWait();

        if (result.isPresent()) {
            Nebula nebula = result.get();
            nebulaService.save(nebula);
            nebulaeModified = true;
            updateTable();
            log.info("Updated nebula: {}", nebula.getName());
        }
    }

    private void duplicateNebula(ActionEvent event) {
        if (selectedNebula == null) {
            showErrorAlert("Duplicate Nebula", "Please select a nebula first");
            return;
        }

        // Create a name for the copy
        String baseName = selectedNebula.getName();
        String newName = baseName + " (Copy)";
        int copyNum = 1;
        while (nebulaService.existsByName(datasetName, newName)) {
            copyNum++;
            newName = baseName + " (Copy " + copyNum + ")";
        }

        Nebula duplicate = nebulaService.duplicate(selectedNebula, newName);
        nebulaService.save(duplicate);
        nebulaeModified = true;
        updateTable();
        log.info("Duplicated nebula '{}' as '{}'", selectedNebula.getName(), newName);
    }

    private void deleteNebula(ActionEvent event) {
        if (selectedNebula == null) {
            showErrorAlert("Delete Nebula", "Please select a nebula first");
            return;
        }

        Optional<ButtonType> result = showConfirmationAlert(
                "Delete Nebula",
                "Delete " + selectedNebula.getName() + "?",
                "This action cannot be undone.");

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String name = selectedNebula.getName();
            nebulaService.delete(selectedNebula);
            nebulaeModified = true;
            selectedNebula = null;
            updateTable();
            log.info("Deleted nebula: {}", name);
        }
    }

    private void handleCloseButton(ActionEvent event) {
        setResult(new Result(nebulaeModified, tableView.getItems().size()));
        close();
    }

    private void handleClose(WindowEvent event) {
        setResult(new Result(nebulaeModified, tableView.getItems().size()));
    }
}
