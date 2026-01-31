package com.teamgannon.trips.nebula.dialogs.catalog;

import com.teamgannon.trips.nebula.model.NebulaType;
import com.teamgannon.trips.utility.DialogUtils;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.WindowEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Dialog for importing nebulae from built-in astronomical catalogs.
 * <p>
 * Provides browsing, filtering, and selection of catalog entries
 * for import into a dataset.
 */
@Slf4j
public class NebulaCatalogImportDialog extends Dialog<NebulaCatalogImportDialog.ImportResult> {

    private final Font titleFont = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

    private final NebulaCatalogService catalogService;
    private final String datasetName;

    private final TableView<SelectableEntry> tableView = new TableView<>();
    private final ObservableList<SelectableEntry> tableData = FXCollections.observableArrayList();

    private final Label selectionCountLabel = new Label("0 selected");
    private final ComboBox<String> typeFilter = new ComboBox<>();
    private final ComboBox<String> catalogFilter = new ComboBox<>();
    private final TextField searchField = new TextField();

    /**
     * Wrapper class for catalog entries with selection state.
     */
    @Getter
    public static class SelectableEntry {
        private final NebulaCatalogEntry entry;
        private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

        public SelectableEntry(NebulaCatalogEntry entry) {
            this.entry = entry;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        public SimpleBooleanProperty selectedProperty() {
            return selected;
        }
    }

    /**
     * Result of the import operation.
     */
    public record ImportResult(int importedCount, List<String> importedNames) {
    }

    public NebulaCatalogImportDialog(NebulaCatalogService catalogService, String datasetName) {
        this.catalogService = catalogService;
        this.datasetName = datasetName;

        setTitle("Import Nebulae from Catalog");
        setWidth(800);
        setHeight(550);

        VBox mainBox = new VBox(10);
        mainBox.setPadding(new Insets(10));

        createHeader(mainBox);
        createFilterBar(mainBox);
        createTable(mainBox);
        createSelectionBar(mainBox);
        createButtonBar(mainBox);

        getDialogPane().setContent(mainBox);

        // Load initial data
        loadCatalogEntries(null, null);

        DialogUtils.bindCloseHandler(this, this::handleClose);
    }

    private void createHeader(VBox mainBox) {
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Import Nebulae to: " + datasetName);
        titleLabel.setFont(titleFont);
        header.getChildren().add(titleLabel);

        mainBox.getChildren().add(header);
        mainBox.getChildren().add(new Separator());
    }

    private void createFilterBar(VBox mainBox) {
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        // Type filter
        filterBar.getChildren().add(new Label("Type:"));
        typeFilter.getItems().add("All Types");
        for (NebulaType type : NebulaType.values()) {
            typeFilter.getItems().add(type.getDisplayName());
        }
        typeFilter.setValue("All Types");
        typeFilter.setOnAction(e -> applyFilters());
        filterBar.getChildren().add(typeFilter);

        // Catalog filter
        filterBar.getChildren().add(new Label("Catalog:"));
        catalogFilter.getItems().add("All Catalogs");
        catalogFilter.getItems().addAll(catalogService.getSourceCatalogs());
        catalogFilter.setValue("All Catalogs");
        catalogFilter.setOnAction(e -> applyFilters());
        filterBar.getChildren().add(catalogFilter);

        // Search field
        filterBar.getChildren().add(new Label("Search:"));
        searchField.setPromptText("Search by name...");
        searchField.setPrefWidth(150);
        searchField.textProperty().addListener((obs, old, val) -> applyFilters());
        filterBar.getChildren().add(searchField);

        // Clear filters button
        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(e -> {
            typeFilter.setValue("All Types");
            catalogFilter.setValue("All Catalogs");
            searchField.clear();
            applyFilters();
        });
        filterBar.getChildren().add(clearBtn);

        mainBox.getChildren().add(filterBar);
    }

    private void createTable(VBox mainBox) {
        tableView.setPrefHeight(320);
        tableView.setEditable(true);

        // Selection checkbox column
        TableColumn<SelectableEntry, Boolean> selectCol = new TableColumn<>("");
        selectCol.setCellValueFactory(cell -> cell.getValue().selectedProperty());
        selectCol.setCellFactory(CheckBoxTableCell.forTableColumn(selectCol));
        selectCol.setEditable(true);
        selectCol.setPrefWidth(30);
        selectCol.setMaxWidth(30);
        tableView.getColumns().add(selectCol);

        // Catalog ID column
        TableColumn<SelectableEntry, String> idCol = new TableColumn<>("Catalog ID");
        idCol.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEntry().getCatalogId()));
        idCol.setPrefWidth(80);
        tableView.getColumns().add(idCol);

        // Common name column
        TableColumn<SelectableEntry, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(cell -> {
            String name = cell.getValue().getEntry().getCommonName();
            return new SimpleStringProperty(name != null ? name : "");
        });
        nameCol.setPrefWidth(150);
        tableView.getColumns().add(nameCol);

        // Type column
        TableColumn<SelectableEntry, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getEntry().getType().getDisplayName()));
        typeCol.setPrefWidth(120);
        tableView.getColumns().add(typeCol);

        // Distance column
        TableColumn<SelectableEntry, String> distCol = new TableColumn<>("Distance (ly)");
        distCol.setCellValueFactory(cell ->
                new SimpleStringProperty(String.format("%.0f", cell.getValue().getEntry().getDistanceLy())));
        distCol.setPrefWidth(80);
        tableView.getColumns().add(distCol);

        // Size column
        TableColumn<SelectableEntry, String> sizeCol = new TableColumn<>("Radius (ly)");
        sizeCol.setCellValueFactory(cell ->
                new SimpleStringProperty(String.format("%.1f", cell.getValue().getEntry().getRadiusLy())));
        sizeCol.setPrefWidth(70);
        tableView.getColumns().add(sizeCol);

        // Constellation column
        TableColumn<SelectableEntry, String> constCol = new TableColumn<>("Constellation");
        constCol.setCellValueFactory(cell -> {
            String cons = cell.getValue().getEntry().getConstellation();
            return new SimpleStringProperty(cons != null ? cons : "");
        });
        constCol.setPrefWidth(100);
        tableView.getColumns().add(constCol);

        // Source column
        TableColumn<SelectableEntry, String> srcCol = new TableColumn<>("Source");
        srcCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getEntry().getSourceCatalog()));
        srcCol.setPrefWidth(70);
        tableView.getColumns().add(srcCol);

        tableView.setItems(tableData);

        // Update selection count when selections change
        tableData.forEach(entry ->
                entry.selectedProperty().addListener((obs, old, val) -> updateSelectionCount()));

        VBox.setVgrow(tableView, Priority.ALWAYS);
        mainBox.getChildren().add(tableView);
    }

    private void createSelectionBar(VBox mainBox) {
        HBox selectionBar = new HBox(10);
        selectionBar.setAlignment(Pos.CENTER_LEFT);

        Button selectAllBtn = new Button("Select All");
        selectAllBtn.setOnAction(e -> {
            tableData.forEach(entry -> entry.setSelected(true));
            updateSelectionCount();
        });

        Button selectNoneBtn = new Button("Select None");
        selectNoneBtn.setOnAction(e -> {
            tableData.forEach(entry -> entry.setSelected(false));
            updateSelectionCount();
        });

        Button invertBtn = new Button("Invert Selection");
        invertBtn.setOnAction(e -> {
            tableData.forEach(entry -> entry.setSelected(!entry.isSelected()));
            updateSelectionCount();
        });

        selectionCountLabel.setStyle("-fx-font-weight: bold;");

        selectionBar.getChildren().addAll(selectAllBtn, selectNoneBtn, invertBtn, selectionCountLabel);
        mainBox.getChildren().add(selectionBar);
    }

    private void createButtonBar(VBox mainBox) {
        HBox buttonBar = new HBox(10);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(this::handleCancel);

        Button importBtn = new Button("Import Selected");
        importBtn.setDefaultButton(true);
        importBtn.setOnAction(this::handleImport);

        buttonBar.getChildren().addAll(cancelBtn, importBtn);
        mainBox.getChildren().add(buttonBar);
    }

    private void loadCatalogEntries(NebulaType typeFilter, String catalogFilter) {
        tableData.clear();

        List<NebulaCatalogEntry> entries;

        if (typeFilter != null) {
            entries = catalogService.getEntriesByType(typeFilter);
        } else if (catalogFilter != null && !catalogFilter.isEmpty()) {
            entries = catalogService.getEntriesByCatalog(catalogFilter);
        } else {
            entries = catalogService.getAllEntries();
        }

        for (NebulaCatalogEntry entry : entries) {
            SelectableEntry selectable = new SelectableEntry(entry);
            selectable.selectedProperty().addListener((obs, old, val) -> updateSelectionCount());
            tableData.add(selectable);
        }

        updateSelectionCount();
    }

    private void applyFilters() {
        String typeValue = typeFilter.getValue();
        String catalogValue = catalogFilter.getValue();
        String searchText = searchField.getText().toLowerCase().trim();

        // Get all entries
        List<NebulaCatalogEntry> entries = catalogService.getAllEntries();

        // Filter by type
        if (!"All Types".equals(typeValue)) {
            NebulaType selectedType = null;
            for (NebulaType type : NebulaType.values()) {
                if (type.getDisplayName().equals(typeValue)) {
                    selectedType = type;
                    break;
                }
            }
            if (selectedType != null) {
                final NebulaType filterType = selectedType;
                entries = entries.stream()
                        .filter(e -> e.getType() == filterType)
                        .collect(Collectors.toList());
            }
        }

        // Filter by catalog
        if (!"All Catalogs".equals(catalogValue)) {
            entries = entries.stream()
                    .filter(e -> catalogValue.equalsIgnoreCase(e.getSourceCatalog()))
                    .collect(Collectors.toList());
        }

        // Filter by search text
        if (!searchText.isEmpty()) {
            entries = entries.stream()
                    .filter(e -> e.getCatalogId().toLowerCase().contains(searchText) ||
                            (e.getCommonName() != null && e.getCommonName().toLowerCase().contains(searchText)) ||
                            (e.getConstellation() != null && e.getConstellation().toLowerCase().contains(searchText)))
                    .collect(Collectors.toList());
        }

        // Update table
        tableData.clear();
        for (NebulaCatalogEntry entry : entries) {
            SelectableEntry selectable = new SelectableEntry(entry);
            selectable.selectedProperty().addListener((obs, old, val) -> updateSelectionCount());
            tableData.add(selectable);
        }

        updateSelectionCount();
    }

    private void updateSelectionCount() {
        long count = tableData.stream().filter(SelectableEntry::isSelected).count();
        selectionCountLabel.setText(count + " selected");
    }

    private List<NebulaCatalogEntry> getSelectedEntries() {
        return tableData.stream()
                .filter(SelectableEntry::isSelected)
                .map(SelectableEntry::getEntry)
                .collect(Collectors.toList());
    }

    private void handleImport(ActionEvent event) {
        List<NebulaCatalogEntry> selected = getSelectedEntries();

        if (selected.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select at least one nebula to import.");
            alert.showAndWait();
            return;
        }

        int imported = catalogService.importToDataset(selected, datasetName);

        List<String> names = selected.stream()
                .map(e -> e.getCommonName() != null ? e.getCommonName() : e.getCatalogId())
                .collect(Collectors.toList());

        setResult(new ImportResult(imported, names));
        closeDialog();
    }

    private void handleCancel(ActionEvent event) {
        setResult(null);
        closeDialog();
    }

    private void handleClose(WindowEvent event) {
        setResult(null);
    }

    private void closeDialog() {
        if (getDialogPane().getScene() != null && getDialogPane().getScene().getWindow() != null) {
            getDialogPane().getScene().getWindow().hide();
        }
    }
}
