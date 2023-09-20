package com.teamgannon.trips.service.export.tasks;

import com.teamgannon.trips.dialogs.dataset.model.ExportOptions;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.export.ExportResults;
import com.teamgannon.trips.service.importservices.tasks.ProgressUpdater;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class CSVDataSetDataExportTask extends Task<ExportResults> implements ProgressUpdater {

    private final int PAGE_SIZE = 1000;

    private final ExportOptions export;
    private final DatabaseManagementService databaseManagementService;

    public CSVDataSetDataExportTask(ExportOptions export, DatabaseManagementService databaseManagementService) {
        this.export = export;
        this.databaseManagementService = databaseManagementService;
    }


    @Override
    public void updateTaskInfo(String message) {
        updateMessage(message + "  ");
    }

    @Override
    protected ExportResults call() throws Exception {
        ExportResults result = processCSVFile(export);
        if (result.isSuccess()) {
            log.info("New dataset {} added", export.getFileName());
        } else {
            log.error("load csv: " + result.getMessage());
        }

        return result;
    }

    private ExportResults processCSVFile(ExportOptions export) {

        ExportResults exportResults = ExportResults.builder().success(false).build();

        log.info("about to process file");
        try {
            Writer writer = Files.newBufferedWriter(Paths.get(export.getFileName() + ".trips.csv"));

//            createDescriptorHeading(writer, dataSetDescriptor);

            String headers = getHeaders();
            writer.write(headers);

            int total = 0;
            int pageNumber = 0;
            Page<StarObject> starObjectPage = databaseManagementService.getFromDatasetByPage(export.getDataset(), pageNumber, PAGE_SIZE);
            int totalPages = starObjectPage.getTotalPages();

            for (int i = 0; i < totalPages; i++) {
                starObjectPage = databaseManagementService.getFromDatasetByPage(export.getDataset(), i, PAGE_SIZE);
                List<StarObject> starObjects = starObjectPage.getContent();
                for (StarObject starObject : starObjects) {
                    String csvRecord = convertToCSV(starObject);
                    writer.write(csvRecord);
                    total++;
                }
                updateTaskInfo(total + " elements written so far");
            }
            writer.flush();
            writer.close();
            log.info("finished exporting file");
            exportResults.setSuccess(true);

        } catch (Exception e) {
            exportResults.setMessage("caught error opening the file:{}" + e.getMessage());
            log.error("caught error opening the file:{}", e.getMessage());
        }
        return exportResults;
    }


    private @NotNull String getHeaders() {
        return "id," +
                "dataSetName," +
                "displayName," +
                "commonName," +
                "System Name," +
                "Epoch," +
                "constellationName," +
                "mass," +
                "notes," +
                "source," +
                "catalogIdList," +
                "simbadId," +
                "radius," +
                "ra," +
                "declination," +
                "pmra," +
                "pmdec," +
                "distance," +
                "radialVelocity," +
                "spectralClass," +
                "temperature," +
                "realStar," +
                "bprp," +
                "bpg," +
                "grp," +
                "luminosity," +
                "magu," +
                "magb," +
                "magv," +
                "magr," +
                "magi," +
                "other," +
                "anomaly," +
                "polity," +
                "worldType," +
                "fuelType," +
                "portType," +
                "populationType," +
                "techType," +
                "productType," +
                "milSpaceType," +
                "milPlanType," +
                "age," +
                "metallicity," +
                "miscText1," +
                "miscText2," +
                "miscText3," +
                "miscText4," +
                "miscText5," +
                "miscNum1," +
                "miscNum2," +
                "miscNum3," +
                "miscNum4," +
                "miscNum5," +
                "numExoplanets" +
                "\n";
    }

    private @NotNull String convertToCSV(@NotNull StarObject starObject) {

        return removeCommas(starObject.getId()) + ", " +
                removeCommas(starObject.getDataSetName()) + ", " +
                removeCommas(starObject.getDisplayName()) + ", " +
                removeCommas(starObject.getCommonName()) + ", " +
                removeCommas(starObject.getSystemName()) + ", " +
                removeCommas(starObject.getEpoch()) + ", " +
                removeCommas(starObject.getConstellationName()) + ", " +
                starObject.getMass() + ", " +
                removeCommas(starObject.getNotes()) + ", " +
                removeCommas(starObject.getSource()) + ", " +
                String.join("~", starObject.getCatalogIdList()) + ", " +
                removeCommas(starObject.getSimbadId()) + ", " +
                starObject.getRadius() + ", " +
                starObject.getRa() + ", " +
                starObject.getDeclination() + ", " +
                starObject.getPmra() + ", " +
                starObject.getPmdec() + ", " +
                starObject.getDistance() + ", " +
                starObject.getRadialVelocity() + ", " +
                starObject.getSpectralClass() + ", " +
                starObject.getTemperature() + ", " +
                starObject.isRealStar() + ", " +
                starObject.getBprp() + ", " +
                starObject.getBpg() + ", " +
                starObject.getGrp() + ", " +
                starObject.getLuminosity() + ", " +
                starObject.getMagu() + ", " +
                starObject.getMagb() + ", " +
                starObject.getMagv() + ", " +
                starObject.getMagr() + ", " +
                starObject.getMagi() + ", " +
                starObject.isOther() + ", " +
                starObject.isAnomaly() + ", " +
                starObject.getPolity() + ", " +
                starObject.getWorldType() + ", " +
                starObject.getFuelType() + ", " +
                starObject.getPortType() + ", " +
                starObject.getPopulationType() + ", " +
                starObject.getTechType() + ", " +
                starObject.getProductType() + ", " +
                starObject.getMilSpaceType() + ", " +
                starObject.getMilPlanType() + ", " +
                starObject.getAge() + ", " +
                starObject.getMetallicity() + ", " +
                removeCommas(starObject.getMiscText1()) + ", " +
                removeCommas(starObject.getMiscText2()) + ", " +
                removeCommas(starObject.getMiscText3()) + ", " +
                removeCommas(starObject.getMiscText4()) + ", " +
                removeCommas(starObject.getMiscText5()) + ", " +
                starObject.getMiscNum1() + ", " +
                starObject.getMiscNum2() + ", " +
                starObject.getMiscNum3() + ", " +
                starObject.getMiscNum4() + ", " +
                starObject.getMiscNum5() + ", " +
                starObject.getNumExoplanets() +
                "\n";
    }

    private @Nullable String removeCommas(@Nullable String origin) {
        String replaced = origin;
        if (origin != null) {
            replaced = origin.replace(",", "~");
        }
        return replaced;
    }


}
