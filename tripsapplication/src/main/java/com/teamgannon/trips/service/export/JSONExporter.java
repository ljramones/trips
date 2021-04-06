package com.teamgannon.trips.service.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamgannon.trips.dialogs.dataset.ExportOptions;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.service.export.model.DataSetDescriptorDTO;
import com.teamgannon.trips.service.export.model.JsonExportObj;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

@Slf4j
public class JSONExporter implements ExportTaskControl {

    private final StatusUpdaterListener updaterListener;

    public JSONExporter(StatusUpdaterListener updaterListener) {
        this.updaterListener = updaterListener;
    }

    public static void main(String[] args) {
        try {

            DataSetDescriptor descriptor = new DataSetDescriptor();
            descriptor.setThemeStr("");
            descriptor.setCustomDataValuesStr("");
            descriptor.setCustomDataDefsStr("");
            descriptor.setDataSetName("test");
            descriptor.setFileCreator("anon");
            descriptor.setFileNotes("notes");
            descriptor.setFilePath("filepath");
            descriptor.setDatasetType("json");
            descriptor.setRoutesStr("");

            DataSetDescriptorDTO dto = descriptor.toDataSetDescriptorDTO();

            ObjectMapper mapper = new ObjectMapper();
            String jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto);

            DataSetDescriptorDTO dto1 = mapper.readValue(jsonStr, DataSetDescriptorDTO.class);
            DataSetDescriptor descriptor1 = dto1.toDataSetDescriptor();

            log.info("converted");
        } catch (Exception e) {
            log.error("failed to export as Json" + e.getMessage());
        }
    }

    public void exportAsJson(@NotNull ExportOptions export, List<StarObject> starObjects) {

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            Writer writer = Files.newBufferedWriter(Paths.get(export.getFileName() + ".trips.json"));

            JsonExportObj jsonExportObj = new JsonExportObj();
            jsonExportObj.setDescriptor(export.getDataset().toDataSetDescriptorDTO());
            jsonExportObj.setStarObjectList(starObjects);

            String jsonStr = objectMapper.writeValueAsString(jsonExportObj);
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

    @Override
    public boolean cancelExport() {
        return false;
    }

    @Override
    public String whoAmI() {
        return "JSON exporter";
    }
}
