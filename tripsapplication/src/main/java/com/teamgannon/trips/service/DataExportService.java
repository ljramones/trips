package com.teamgannon.trips.service;

import com.teamgannon.trips.dialogs.dataset.ExportOptions;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.service.export.CSVExporter;
import com.teamgannon.trips.service.export.ExcelExporter;
import com.teamgannon.trips.service.export.JSONExporter;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.showConfirmationAlert;
import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

/**
 * Used to import and export data sets
 * <p>
 * Created by larrymitchell on 2017-01-22.
 */
@Slf4j
public class DataExportService {

    private final DatabaseManagementService databaseManagementService;
    private final StatusUpdaterListener updaterListener;

    private final @NotNull ExcelExporter excelExporter;

    private final @NotNull CSVExporter csvExporter;

    private final @NotNull JSONExporter jsonExporter;

    public DataExportService(DatabaseManagementService databaseManagementService,
                             StatusUpdaterListener updaterListener) {

        this.databaseManagementService = databaseManagementService;
        this.updaterListener = updaterListener;

        excelExporter = new ExcelExporter(databaseManagementService, updaterListener);
        csvExporter = new CSVExporter(updaterListener);
        jsonExporter = new JSONExporter(updaterListener);
    }

    public void exportDB() {
        Optional<ButtonType> result = showConfirmationAlert(
                "Data Export Service", "Entire Database",
                "Do you want to export this database? It will take a while.");
        if ((result.isPresent()) && (result.get() == ButtonType.OK)) {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export entire database to export as a Excel file");
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                excelExporter.exportEntireDB(file.getAbsolutePath());
            } else {
                log.warn("file export cancelled");
                showInfoMessage("Database export", "Export cancelled");
            }
        }
    }

    public void exportDataset(@NotNull ExportOptions exportOptions) {

        List<AstrographicObject> astrographicObjects = databaseManagementService.getFromDataset(exportOptions.getDataset());

        exportExec(exportOptions, astrographicObjects);

    }

    /**
     * do the actual export witht he defined objects
     *
     * @param exportOptions       the options
     * @param astrographicObjects the objects to export
     */
    private void exportExec(@NotNull ExportOptions exportOptions, List<AstrographicObject> astrographicObjects) {
        switch (exportOptions.getExportFormat()) {

            case CSV -> csvExporter.exportAsCSV(exportOptions, astrographicObjects);
            case EXCEL -> excelExporter.exportAsExcel(exportOptions, astrographicObjects);
            case JSON -> jsonExporter.exportAsJson(exportOptions, astrographicObjects);
        }
    }

    /**
     * export a queried dataset based on options
     *
     * @param exportOptions       the options
     * @param astrographicObjects the objects to export
     */
    public void exportDatasetOnQuery(ExportOptions exportOptions, List<AstrographicObject> astrographicObjects) {
        log.info("exporting {} with {} stars ", exportOptions.getDataset(), astrographicObjects.size());
        exportExec(exportOptions, astrographicObjects);
    }

}
