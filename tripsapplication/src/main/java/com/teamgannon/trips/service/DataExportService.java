package com.teamgannon.trips.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamgannon.trips.dialogs.dataset.ExportOptions;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.service.export.CSVExporter;
import com.teamgannon.trips.service.export.ExcelExporter;
import com.teamgannon.trips.service.export.JSONExporter;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.*;

/**
 * Used to import and export data sets
 * <p>
 * Created by larrymitchell on 2017-01-22.
 */
@Slf4j
public class DataExportService {

    private final DatabaseManagementService databaseManagementService;
    private final StatusUpdaterListener updaterListener;

    private final ExcelExporter excelExporter;

    private final CSVExporter csvExporter;

    private final JSONExporter jsonExporter;

    public DataExportService(DatabaseManagementService databaseManagementService,
                             StatusUpdaterListener updaterListener) {
        this.databaseManagementService = databaseManagementService;
        this.updaterListener = updaterListener;

        excelExporter = new ExcelExporter(databaseManagementService, updaterListener);
        csvExporter = new CSVExporter(databaseManagementService, updaterListener);
        jsonExporter = new JSONExporter(databaseManagementService, updaterListener);
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

    public void exportDataset(ExportOptions exportOptions) {

        List<AstrographicObject> astrographicObjects = databaseManagementService.getFromDataset(exportOptions.getDataset());
        switch (exportOptions.getExportFormat()) {

            case CSV -> {
                csvExporter.exportAsCSV(exportOptions, astrographicObjects);
            }
            case EXCEL -> {
                excelExporter.exportAsExcel(exportOptions, astrographicObjects);
            }
            case JSON -> {
                jsonExporter.exportAsJson(exportOptions, astrographicObjects);
            }
        }

    }

}
