package com.teamgannon.trips.service.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamgannon.trips.dialogs.dataset.ExportOptions;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

@Slf4j
public class JSONExporter {

    private DatabaseManagementService databaseManagementService;
    private StatusUpdaterListener updaterListener;

    public JSONExporter(DatabaseManagementService databaseManagementService, StatusUpdaterListener updaterListener) {
        this.databaseManagementService = databaseManagementService;
        this.updaterListener = updaterListener;
    }

    public void exportAsJson(ExportOptions export, List<AstrographicObject> astrographicObjects) {

        ObjectMapper Obj = new ObjectMapper();

        try {
            Writer writer = Files.newBufferedWriter(Paths.get(export.getFileName() + ".trips.json"));

            String jsonStr = Obj.writeValueAsString(astrographicObjects);
            writer.write(jsonStr);

            writer.flush();
            writer.close();
            showInfoMessage("Database Export", export.getDataset().getDataSetName()
                    + " was export to " + export.getFileName() + ".trips.json");

        } catch (Exception e) {
            log.error("caught error opening the file:{}", e.getMessage());
            showErrorAlert(
                    "Export Dataset as JSON file",
                    export.getDataset().getDataSetName() +
                            "failed to export:" + e.getMessage());
        }
    }


    private void updateStatus(String status) {
        new Thread(() -> Platform.runLater(() -> {
            log.info(status);
            updaterListener.updateStatus(status);
        })).start();
    }

}
