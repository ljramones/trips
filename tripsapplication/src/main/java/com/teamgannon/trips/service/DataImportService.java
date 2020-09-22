package com.teamgannon.trips.service;

import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;

import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

@Slf4j
@Service
public class DataImportService {

    private DatabaseManagementService databaseManagementService;

    public DataImportService(DatabaseManagementService databaseManagementService) {
        this.databaseManagementService = databaseManagementService;
    }

    public void loadMultipleDatasets() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select excel file comtaining datasets");
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")

        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            loadDBFile(file);
        } else {
            log.warn("file import cancelled");
            showInfoMessage("Import databse", "Cancelled request to import");
        }
    }

    private void loadDBFile(File file) {

    }


}
