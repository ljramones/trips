package com.teamgannon.trips.service.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamgannon.trips.dialogs.dataset.ExportOptions;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.export.model.JsonExportObj;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

@Slf4j
public class JSONExporter {

    private final StatusUpdaterListener updaterListener;

    public JSONExporter(StatusUpdaterListener updaterListener) {
        this.updaterListener = updaterListener;
    }

    public void exportAsJson(@NotNull ExportOptions export, List<AstrographicObject> astrographicObjects) {

        ObjectMapper Obj = new ObjectMapper();

        try {
            Writer writer = Files.newBufferedWriter(Paths.get(export.getFileName() + ".trips.json"));

            JsonExportObj jsonExportObj = new JsonExportObj();
            jsonExportObj.setDescriptor(export.getDataset());
            jsonExportObj.setAstrographicObjectList(astrographicObjects);

            String jsonStr = Obj.writeValueAsString(jsonExportObj);
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
