package com.teamgannon.trips.service.export.tasks;

import com.teamgannon.trips.dialogs.dataset.model.ExportOptions;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.service.DatabaseManagementService;
import com.teamgannon.trips.service.export.ExportResults;
import com.teamgannon.trips.service.importservices.tasks.ProgressUpdater;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class CSVQueryDataExportTask extends Task<ExportResults> implements ProgressUpdater {

    private final static int PAGE_SIZE = 1000;

    private final ExportOptions export;
    private final SearchContext searchContext;
    private final DatabaseManagementService databaseManagementService;

    private long lineCount = 0;

    public CSVQueryDataExportTask(ExportOptions export, SearchContext searchContext, DatabaseManagementService databaseManagementService) {
        this.export = export;
        this.searchContext = searchContext;
        this.databaseManagementService = databaseManagementService;
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

    public ExportResults processCSVFile(ExportOptions export) {

        ExportResults exportResults = ExportResults.builder().success(false).build();

        try {
            final Writer writer = Files.newBufferedWriter(Paths.get(export.getFileName() + ".trips.csv"));

            String headers = getHeaders();
            writer.write(headers);
            boolean done = false;
            int pageNumber = 0;
            do {
                Page<StarObject> starObjectList = databaseManagementService.getStarPaged(searchContext.getAstroSearchQuery(), PageRequest.of(pageNumber++, PAGE_SIZE));
                long count = starObjectList.getContent().size();
                if (count > 0) {
                    starObjectList.forEach(starObject -> {
                        try {
                            String csvRecord = convertToCSV(starObject);
                            writer.write(csvRecord);
                            checkCounter();
                        } catch (Exception e) {
                            log.error("bad record:{}", starObject.toString());
                        }
                    });
                } else {
                    done = true;
                }
            } while (!done);

            log.info("data written, closing file");

            writer.flush();
            writer.close();
            String msg = export.getDataset().getDataSetName() + " was exported to " + export.getFileName() + ".trips.csv";

            exportResults.setSuccess(true);
            exportResults.setMessage(msg);

        } catch (Exception e) {
            log.error("caught error opening the file:{}", e.getMessage());
            exportResults.setMessage(export.getDataset() + "failed to export:" + e.getMessage());
        }

        return exportResults;
    }

    private void checkCounter() {
        lineCount++;
        if ((lineCount % 1001) == 0) {
            log.info(lineCount + " records so far ");
            updateTaskInfo(lineCount + " records so far ");
        }
    }


    @Override
    public void updateTaskInfo(String message) {
        updateMessage(message + "  ");
    }


    private @NotNull String getHeaders() {
        return "id," +
                "dataSetName," +
                "displayName," +
                "commonName," +
                "gaiaId," +
                "simbadId," +
                "constellationName," +
                "mass," +
                "age," +
                "metallicity," +
                "source," +
                "catalogIdList," +
                "X," +
                "Y," +
                "Z," +
                "radius," +
                "ra," +
                "pmra," +
                "declination," +
                "pmdec," +
                "parallax," +
                "distance," +
                "radialVelocity," +
                "spectralClass," +
                "orthoSpectralClass," +
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
                "Notes," +
                "L," +
                "B" +
                "\n";
    }

    private @NotNull String convertToCSV(@NotNull StarObject starObject) {

        return removeCommas(starObject.getId().toString()) + ", " +
                removeCommas(starObject.getDataSetName()) + ", " +
                removeCommas(starObject.getDisplayName()) + ", " +
                removeCommas(starObject.getCommonName()) + ", " +
                removeCommas(starObject.getGaiaDR2CatId()) + ", " +
                removeCommas(starObject.getSimbadId()) + ", " +
                removeCommas(starObject.getConstellationName()) + ", " +
                starObject.getMass() + ", " +
                starObject.getAge() + ", " +
                starObject.getMetallicity() + ", " +
                removeCommas(starObject.getSource()) + ", " +
                String.join("~", starObject.getCatalogIdList()) + ", " +
                starObject.getX() + ", " +
                starObject.getY() + ", " +
                starObject.getZ() + ", " +
                starObject.getRadius() + ", " +
                starObject.getRa() + ", " +
                starObject.getPmra() + ", " +
                starObject.getDeclination() + ", " +
                starObject.getPmdec() + ", " +
                starObject.getParallax() + ", " +
                starObject.getDistance() + ", " +
                starObject.getRadialVelocity() + ", " +
                starObject.getSpectralClass() + ", " +
                starObject.getOrthoSpectralClass() + ", " +
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
                removeCommas(starObject.getNotes()) + ", " +
                starObject.getGalacticLat() + ", " +
                starObject.getGalacticLong() +
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
