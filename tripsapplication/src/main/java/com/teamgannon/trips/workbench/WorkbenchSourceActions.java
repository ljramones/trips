package com.teamgannon.trips.workbench;

import com.teamgannon.trips.workbench.service.WorkbenchTapDefaults;
import com.teamgannon.trips.workbench.service.WorkbenchTapService;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.control.TabPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class WorkbenchSourceActions {

    private final ListView<WorkbenchSource> sourceListView;
    private final Label sourceStatusLabel;
    private final TabPane workbenchTabs;
    private final CheckBox cacheDefaultCheckbox;
    private final Supplier<Path> cacheDirSupplier;
    private final WorkbenchTapService tapService;
    private final Consumer<String> updateStatus;
    private final BiConsumer<String, String> showError;
    private final List<WorkbenchSource> sources;

    public WorkbenchSourceActions(List<WorkbenchSource> sources,
                                  ListView<WorkbenchSource> sourceListView,
                                  Label sourceStatusLabel,
                                  TabPane workbenchTabs,
                                  CheckBox cacheDefaultCheckbox,
                                  Supplier<Path> cacheDirSupplier,
                                  WorkbenchTapService tapService,
                                  Consumer<String> updateStatus,
                                  BiConsumer<String, String> showError) {
        this.sources = sources;
        this.sourceListView = sourceListView;
        this.sourceStatusLabel = sourceStatusLabel;
        this.workbenchTabs = workbenchTabs;
        this.cacheDefaultCheckbox = cacheDefaultCheckbox;
        this.cacheDirSupplier = cacheDirSupplier;
        this.tapService = tapService;
        this.updateStatus = updateStatus;
        this.showError = showError;
    }

    public void onAddSource() {
        List<String> choices = List.of(
                WorkbenchSourceType.LOCAL_CSV.getLabel(),
                WorkbenchSourceType.URL_CSV.getLabel(),
                WorkbenchSourceType.GAIA_TAP.getLabel(),
                "Gaia TAP (TOP 200)",
                "Gaia TAP (TOP 1000)",
                "Gaia TAP (TOP 5000)",
                WorkbenchSourceType.SIMBAD_TAP.getLabel(),
                "SIMBAD TAP (TOP 200)",
                "SIMBAD TAP (TOP 1000)",
                WorkbenchSourceType.VIZIER_TAP.getLabel(),
                "VizieR TAP (Hipparcos)",
                "VizieR TAP (Tycho-2)",
                "VizieR TAP (RAVE DR5)",
                "VizieR TAP (LAMOST DR5)",
                "VizieR TAP (Gaia RUWE subset)",
                "VizieR TAP (Gliese/CNS)",
                "VizieR TAP (RECONS)",
                "VizieR TAP (Lookup tables)"
        );
        ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle("Add Source");
        dialog.setHeaderText("Select a source type");
        dialog.setContentText("Source type:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        String selection = result.get();
        if ("Gaia TAP (TOP 200)".equals(selection)) {
            addGaiaTapSource(WorkbenchTapDefaults.defaultGaiaQuery(200), "Gaia_DR3_TOP_200.csv");
            return;
        }
        if ("Gaia TAP (TOP 1000)".equals(selection)) {
            addGaiaTapSource(WorkbenchTapDefaults.defaultGaiaQuery(1000), "Gaia_DR3_TOP_1000.csv");
            return;
        }
        if ("Gaia TAP (TOP 5000)".equals(selection)) {
            addGaiaTapSource(WorkbenchTapDefaults.defaultGaiaQuery(5000), "Gaia_DR3_TOP_5000.csv");
            return;
        }
        if ("SIMBAD TAP (TOP 200)".equals(selection)) {
            addSimbadTapSource(WorkbenchTapDefaults.defaultSimbadQuery(200), "SIMBAD_TOP_200.csv");
            return;
        }
        if ("SIMBAD TAP (TOP 1000)".equals(selection)) {
            addSimbadTapSource(WorkbenchTapDefaults.defaultSimbadQuery(1000), "SIMBAD_TOP_1000.csv");
            return;
        }
        if ("VizieR TAP (Hipparcos)".equals(selection)) {
            addVizierTapSource(WorkbenchTapDefaults.defaultVizierHipparcosQuery(1000), "VIZIER_HIP2_TOP_1000.csv");
            return;
        }
        if ("VizieR TAP (Tycho-2)".equals(selection)) {
            addVizierTapSource(WorkbenchTapDefaults.defaultVizierTycho2Query(1000), "VIZIER_TYC2_TOP_1000.csv");
            return;
        }
        if ("VizieR TAP (RAVE DR5)".equals(selection)) {
            addVizierTapSource(WorkbenchTapDefaults.defaultVizierRaveQuery(1000), "VIZIER_RAVE_DR5_TOP_1000.csv");
            return;
        }
        if ("VizieR TAP (LAMOST DR5)".equals(selection)) {
            addVizierTapSource(WorkbenchTapDefaults.defaultVizierLamostQuery(1000), "VIZIER_LAMOST_DR5_TOP_1000.csv");
            return;
        }
        if ("VizieR TAP (Gaia RUWE subset)".equals(selection)) {
            addVizierPresetWithPrompt("Gaia RUWE subset", "VIZIER_GAIA_RUWE_TOP_1000.csv");
            return;
        }
        if ("VizieR TAP (Gliese/CNS)".equals(selection)) {
            addVizierPresetWithPrompt("Gliese/CNS", "VIZIER_GLIESE_CNS_TOP_1000.csv");
            return;
        }
        if ("VizieR TAP (RECONS)".equals(selection)) {
            addVizierTapSource(WorkbenchTapDefaults.defaultVizierReconsQuery(1000), "VIZIER_RECONS_TOP_1000.csv");
            return;
        }
        if ("VizieR TAP (Lookup tables)".equals(selection)) {
            addVizierTableLookupSource();
            return;
        }
        WorkbenchSourceType type = WorkbenchSourceType.fromLabel(selection);
        if (type == WorkbenchSourceType.LOCAL_CSV) {
            addLocalCsvSource();
        } else if (type == WorkbenchSourceType.URL_CSV) {
            addUrlCsvSource();
        } else if (type == WorkbenchSourceType.GAIA_TAP) {
            addGaiaTapSource();
        } else if (type == WorkbenchSourceType.SIMBAD_TAP) {
            addSimbadTapSource();
        } else if (type == WorkbenchSourceType.VIZIER_TAP) {
            addVizierTapSource();
        }
    }

    public void onRefreshSources() {
        sourceStatusLabel.setText("Sources: " + sources.size());
    }

    public void onRemoveSource() {
        WorkbenchSource selected = sourceListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            sources.remove(selected);
            sourceStatusLabel.setText("Removed source: " + selected.getName());
        }
    }

    public void onDownloadSource() {
        updateStatus.accept("Download clicked.");
        WorkbenchSource selected = sourceListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError.accept("Download", "Select a source to download.");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save downloaded CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        applyInitialDirectory(fileChooser);
        String fileName = ensureCsvFileName(selected.getName());
        fileChooser.setInitialFileName(fileName);
        File file;
        Path cacheDir = cacheDirSupplier.get();
        if (cacheDefaultCheckbox != null && cacheDefaultCheckbox.isSelected() && cacheDir != null) {
            file = cacheDir.resolve(fileName).toFile();
        } else {
            file = fileChooser.showSaveDialog(getWindow());
        }
        if (file == null) {
            return;
        }
        if (selected.getType() == WorkbenchSourceType.URL_CSV) {
            updateStatus.accept("Downloading " + selected.getName() + "...");
            tapService.downloadHttpFile(selected.getLocation(),
                    file.toPath(),
                    () -> {
                        updateStatus.accept("Downloaded: " + file.getName());
                        addLocalSourceIfMissing(file.toPath());
                    },
                    updateStatus,
                    message -> showError.accept("Download failed", message));
        } else if (selected.getType() == WorkbenchSourceType.GAIA_TAP) {
            updateStatus.accept("Submitting Gaia TAP job...");
            tapService.downloadGaiaTapToFile(selected.getLocation(),
                    file.toPath(),
                    () -> {
                        updateStatus.accept("Downloaded: " + file.getName());
                        addLocalSourceIfMissing(file.toPath());
                    },
                    updateStatus,
                    message -> showError.accept("Gaia TAP failed", message));
        } else if (selected.getType() == WorkbenchSourceType.SIMBAD_TAP) {
            updateStatus.accept("Submitting SIMBAD TAP job...");
            tapService.downloadSimbadTapToFile(selected.getLocation(),
                    file.toPath(),
                    () -> {
                        updateStatus.accept("Downloaded: " + file.getName());
                        addLocalSourceIfMissing(file.toPath());
                    },
                    updateStatus,
                    message -> showError.accept("SIMBAD TAP failed", message));
        } else if (selected.getType() == WorkbenchSourceType.VIZIER_TAP) {
            updateStatus.accept("Submitting VizieR TAP job...");
            tapService.downloadVizierTapToFile(selected.getLocation(),
                    file.toPath(),
                    () -> {
                        updateStatus.accept("Downloaded: " + file.getName());
                        addLocalSourceIfMissing(file.toPath());
                    },
                    updateStatus,
                    message -> showError.accept("VizieR TAP failed", message));
        } else {
            showError.accept("Download", "Only URL or TAP sources can be downloaded.");
        }
    }

    public void ensureLocalCsvSource(WorkbenchSource source, Consumer<Path> onReady) {
        if (source == null) {
            return;
        }
        if (source.getType() == WorkbenchSourceType.LOCAL_CSV) {
            onReady.accept(Path.of(source.getLocation()));
            return;
        }
        Path cacheDir = cacheDirSupplier.get();
        if (cacheDir == null) {
            showError.accept("Source", "Set a cache directory first.");
            return;
        }
        String fileName = ensureCsvFileName(source.getName());
        Path outputPath = cacheDir.resolve(fileName);
        if (Files.exists(outputPath)) {
            addLocalSourceIfMissing(outputPath);
            onReady.accept(outputPath);
            return;
        }
        if (source.getType() == WorkbenchSourceType.URL_CSV) {
            updateStatus.accept("Downloading " + source.getName() + "...");
            tapService.downloadHttpFile(source.getLocation(),
                    outputPath,
                    () -> {
                        addLocalSourceIfMissing(outputPath);
                        onReady.accept(outputPath);
                    },
                    updateStatus,
                    message -> showError.accept("Download failed", message));
        } else if (source.getType() == WorkbenchSourceType.GAIA_TAP) {
            updateStatus.accept("Submitting Gaia TAP job...");
            tapService.downloadGaiaTapToFile(source.getLocation(),
                    outputPath,
                    () -> {
                        addLocalSourceIfMissing(outputPath);
                        onReady.accept(outputPath);
                    },
                    updateStatus,
                    message -> showError.accept("Gaia TAP failed", message));
        } else if (source.getType() == WorkbenchSourceType.SIMBAD_TAP) {
            updateStatus.accept("Submitting SIMBAD TAP job...");
            tapService.downloadSimbadTapToFile(source.getLocation(),
                    outputPath,
                    () -> {
                        addLocalSourceIfMissing(outputPath);
                        onReady.accept(outputPath);
                    },
                    updateStatus,
                    message -> showError.accept("SIMBAD TAP failed", message));
        } else if (source.getType() == WorkbenchSourceType.VIZIER_TAP) {
            updateStatus.accept("Submitting VizieR TAP job...");
            tapService.downloadVizierTapToFile(source.getLocation(),
                    outputPath,
                    () -> {
                        addLocalSourceIfMissing(outputPath);
                        onReady.accept(outputPath);
                    },
                    updateStatus,
                    message -> showError.accept("VizieR TAP failed", message));
        } else {
            showError.accept("Source", "Unable to prepare a local CSV for this source.");
        }
    }

    public void addLocalSourceIfMissing(Path outputPath) {
        Optional<WorkbenchSource> existing = sources.stream()
                .filter(source -> source.getType() == WorkbenchSourceType.LOCAL_CSV)
                .filter(source -> outputPath.toString().equals(source.getLocation()))
                .findFirst();
        WorkbenchSource source = existing.orElseGet(() -> {
            WorkbenchSource created = WorkbenchSource.localCsv(outputPath.getFileName().toString(), outputPath.toString());
            sources.add(created);
            return created;
        });
        sourceListView.getSelectionModel().select(source);
        sourceStatusLabel.setText("Added source: " + source.getName());
    }

    public void applyInitialDirectory(FileChooser fileChooser) {
        Path cacheDir = cacheDirSupplier.get();
        if (cacheDir != null && Files.exists(cacheDir)) {
            fileChooser.setInitialDirectory(cacheDir.toFile());
        }
    }

    public String ensureCsvFileName(String name) {
        String safeName = name == null || name.isBlank() ? "gaia-query.csv" : name.trim();
        safeName = safeName.replaceAll("[\\\\/]+", "_");
        safeName = safeName.replaceAll("\\s+", "_");
        if (!safeName.toLowerCase().endsWith(".csv")) {
            safeName = safeName + ".csv";
        }
        return safeName;
    }

    private void addLocalCsvSource() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv"));
        applyInitialDirectory(fileChooser);
        File file = fileChooser.showOpenDialog(getWindow());
        if (file == null) {
            return;
        }
        WorkbenchSource source = WorkbenchSource.localCsv(file.getName(), file.getAbsolutePath());
        sources.add(source);
        sourceStatusLabel.setText("Added source: " + source.getName());
    }

    private void addUrlCsvSource() {
        TextInputDialog dialog = new TextInputDialog("https://");
        dialog.setTitle("Add CSV URL");
        dialog.setHeaderText("Enter a URL to a CSV source");
        dialog.setContentText("URL:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        String url = result.get().trim();
        if (!url.startsWith("http")) {
            showError.accept("Add Source", "URL must start with http or https.");
            return;
        }
        WorkbenchSource source = WorkbenchSource.urlCsv(url);
        sources.add(source);
        sourceStatusLabel.setText("Added source: " + source.getName());
    }

    private void addGaiaTapSource() {
        Dialog<TapQuerySpec> dialog = new Dialog<>();
        dialog.setTitle("Add Gaia TAP Source");
        dialog.setHeaderText("Define a Gaia TAP query");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField("Gaia DR3 query");
        TextArea queryArea = new TextArea(WorkbenchTapDefaults.defaultGaiaQuery(2000));
        queryArea.setPrefColumnCount(60);
        queryArea.setPrefRowCount(8);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(8);
        gridPane.add(new Label("Name"), 0, 0);
        gridPane.add(nameField, 1, 0);
        gridPane.add(new Label("ADQL"), 0, 1);
        gridPane.add(queryArea, 1, 1);

        dialog.getDialogPane().setContent(gridPane);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new TapQuerySpec(nameField.getText().trim(), queryArea.getText().trim());
            }
            return null;
        });

        Optional<TapQuerySpec> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        TapQuerySpec spec = result.get();
        if (spec.adql.isEmpty()) {
            showError.accept("Add Gaia TAP Source", "ADQL query cannot be empty.");
            return;
        }
        String name = spec.name.isEmpty() ? "Gaia DR3 query" : spec.name;
        WorkbenchSource source = WorkbenchSource.gaiaTap(name, spec.adql);
        sources.add(source);
        sourceStatusLabel.setText("Added source: " + source.getName());
    }

    private void addGaiaTapSource(String adql, String fileName) {
        WorkbenchSource source = WorkbenchSource.gaiaTap(fileName, adql);
        sources.add(source);
        sourceStatusLabel.setText("Added source: " + source.getName());
    }

    private void addSimbadTapSource() {
        Dialog<TapQuerySpec> dialog = new Dialog<>();
        dialog.setTitle("Add SIMBAD TAP Source");
        dialog.setHeaderText("Define a SIMBAD TAP query");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField("SIMBAD query");
        TextArea queryArea = new TextArea(WorkbenchTapDefaults.defaultSimbadQuery(1000));
        queryArea.setPrefColumnCount(60);
        queryArea.setPrefRowCount(8);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(8);
        gridPane.add(new Label("Name"), 0, 0);
        gridPane.add(nameField, 1, 0);
        gridPane.add(new Label("ADQL"), 0, 1);
        gridPane.add(queryArea, 1, 1);

        dialog.getDialogPane().setContent(gridPane);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new TapQuerySpec(nameField.getText().trim(), queryArea.getText().trim());
            }
            return null;
        });

        Optional<TapQuerySpec> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        TapQuerySpec spec = result.get();
        if (spec.adql.isEmpty()) {
            showError.accept("Add SIMBAD TAP Source", "ADQL query cannot be empty.");
            return;
        }
        String name = spec.name.isEmpty() ? "SIMBAD query" : spec.name;
        WorkbenchSource source = WorkbenchSource.simbadTap(name, spec.adql);
        sources.add(source);
        sourceStatusLabel.setText("Added source: " + source.getName());
    }

    private void addSimbadTapSource(String adql, String fileName) {
        WorkbenchSource source = WorkbenchSource.simbadTap(fileName, adql);
        sources.add(source);
        sourceStatusLabel.setText("Added source: " + source.getName());
    }

    private void addVizierTapSource() {
        Dialog<TapQuerySpec> dialog = new Dialog<>();
        dialog.setTitle("Add VizieR TAP Source");
        dialog.setHeaderText("Define a VizieR TAP query");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField("VizieR query");
        TextArea queryArea = new TextArea(WorkbenchTapDefaults.defaultVizierHipparcosQuery(1000));
        queryArea.setPrefColumnCount(60);
        queryArea.setPrefRowCount(8);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(8);
        gridPane.setVgap(8);
        gridPane.add(new Label("Name"), 0, 0);
        gridPane.add(nameField, 1, 0);
        gridPane.add(new Label("ADQL"), 0, 1);
        gridPane.add(queryArea, 1, 1);

        dialog.getDialogPane().setContent(gridPane);
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new TapQuerySpec(nameField.getText().trim(), queryArea.getText().trim());
            }
            return null;
        });

        Optional<TapQuerySpec> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        TapQuerySpec spec = result.get();
        if (spec.adql.isEmpty()) {
            showError.accept("Add VizieR TAP Source", "ADQL query cannot be empty.");
            return;
        }
        String name = spec.name.isEmpty() ? "VizieR query" : spec.name;
        WorkbenchSource source = WorkbenchSource.vizierTap(name, spec.adql);
        sources.add(source);
        sourceStatusLabel.setText("Added source: " + source.getName());
    }

    private void addVizierTapSource(String adql, String fileName) {
        WorkbenchSource source = WorkbenchSource.vizierTap(fileName, adql);
        sources.add(source);
        sourceStatusLabel.setText("Added source: " + source.getName());
    }

    private void addVizierTableLookupSource() {
        TextInputDialog dialog = new TextInputDialog("lamost");
        dialog.setTitle("VizieR Table Lookup");
        dialog.setHeaderText("Search TAP_SCHEMA.tables for a catalog");
        dialog.setContentText("Keyword:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        String keyword = result.get().trim();
        if (keyword.isEmpty()) {
            showError.accept("VizieR Table Lookup", "Keyword cannot be empty.");
            return;
        }
        String adql = WorkbenchTapDefaults.vizierTableLookupQuery(keyword, 200);
        String fileName = "VIZIER_TABLES_" + keyword.replaceAll("\\s+", "_") + ".csv";
        addVizierTapSource(adql, fileName);
    }

    private void addVizierPresetWithPrompt(String label, String fileName) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("VizieR Catalog");
        dialog.setHeaderText("Enter the VizieR table ID for " + label);
        dialog.setContentText("Table ID (example: V/164/dr5):");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        String tableId = WorkbenchTapDefaults.sanitizeVizierTableId(result.get());
        if (tableId.isEmpty()) {
            showError.accept("VizieR Catalog", "Table ID cannot be empty.");
            return;
        }
        String adql = "SELECT TOP 1000 * FROM \"" + tableId + "\"";
        addVizierTapSource(adql, fileName);
    }

    private Window getWindow() {
        return workbenchTabs.getScene() != null ? workbenchTabs.getScene().getWindow() : null;
    }

    private static class TapQuerySpec {
        private final String name;
        private final String adql;

        private TapQuerySpec(String name, String adql) {
            this.name = name;
            this.adql = adql;
        }
    }
}
