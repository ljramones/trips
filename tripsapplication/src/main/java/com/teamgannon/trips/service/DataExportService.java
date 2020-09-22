package com.teamgannon.trips.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamgannon.trips.dialogs.dataset.ExportOptions;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

/**
 * Used to import and export data sets
 * <p>
 * Created by larrymitchell on 2017-01-22.
 */
@Slf4j
@Service
public class DataExportService {

    private DatabaseManagementService databaseManagementService;

    public DataExportService(DatabaseManagementService databaseManagementService) {
        this.databaseManagementService = databaseManagementService;
    }

    public void exportDataset(ExportOptions exportOptions) {

        List<AstrographicObject> astrographicObjects = databaseManagementService.getFromDataset(exportOptions.getDataset());
        switch (exportOptions.getExportFormat()) {

            case CSV -> {
                exportAsCSV(exportOptions, astrographicObjects);
            }
            case EXCEL -> {
                exportAsExcel(exportOptions.getFileName(), astrographicObjects);
            }
            case JSON -> {
                exportAsJson(exportOptions, astrographicObjects);
            }
        }

    }

    private void exportAsJson(ExportOptions export, List<AstrographicObject> astrographicObjects) {

        ObjectMapper Obj = new ObjectMapper();

        try {
            Writer writer = Files.newBufferedWriter(Paths.get(export.getFileName() + ".json"));

            String jsonStr = Obj.writeValueAsString(astrographicObjects);
            writer.write(jsonStr);

            writer.flush();
            writer.close();
            showInfoMessage("Database Export", export.getDataset().getDataSetName()
                    + " was export to " + export.getFileName() + ".json");

        } catch (Exception e) {
            log.error("caught error opening the file:{}", e.getMessage());
            showErrorAlert(
                    "Export Dataset as JSON file",
                    export.getDataset().getDataSetName() +
                            "failed to export:" + e.getMessage());
        }
    }

    private void exportAsExcel(String fileName, List<AstrographicObject> astrographicObjects) {

    }

    private void exportAsCSV(ExportOptions export, List<AstrographicObject> astrographicObjects) {
        try {
            Writer writer = Files.newBufferedWriter(Paths.get(export.getFileName() + ".csv"));

            String headers = getHeaders();
            writer.write(headers);
            int i = 0;
            for (AstrographicObject astrographicObject : astrographicObjects) {
                String csvRecord = convertToCSV(astrographicObject);
                writer.write(csvRecord);
                i++;
            }
            writer.flush();
            writer.close();
            showInfoMessage("Database Export", export.getDataset().getDataSetName() + " was export to " + export.getFileName() + ".csv");

        } catch (Exception e) {
            log.error("caught error opening the file:{}", e.getMessage());
            showErrorAlert(
                    "Export Dataset as CSV file",
                    export.getDataset().getDataSetName() +
                            "failed to export:" + e.getMessage()
            );
        }
    }


    private String getHeaders() {

        return "id," +
                "dataSetName," +
                "displayName," +
                "constellationName," +
                "mass," +
                "actualMass," +
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
                "dec_deg," +
                "rs_cdeg," +
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
                "notes" +
                "\n";
    }

    private String convertToCSV(AstrographicObject astrographicObject) {

        return removeCommas(astrographicObject.getId().toString()) + ", " +
                removeCommas(astrographicObject.getDataSetName()) + ", " +
                removeCommas(astrographicObject.getDisplayName()) + ", " +
                removeCommas(astrographicObject.getConstellationName()) + ", " +
                astrographicObject.getMass() + ", " +
                astrographicObject.getActualMass() + ", " +
                removeCommas(astrographicObject.getSource()) + ", " +
                astrographicObject.getCatalogIdList() + ", " +
                astrographicObject.getX() + ", " +
                astrographicObject.getY() + ", " +
                astrographicObject.getY() + ", " +
                astrographicObject.getRadius() + ", " +
                astrographicObject.getRa() + ", " +
                astrographicObject.getPmra() + ", " +
                astrographicObject.getDeclination() + ", " +
                astrographicObject.getPmdec() + ", " +
                astrographicObject.getDec_deg() + ", " +
                astrographicObject.getRs_cdeg() + ", " +
                astrographicObject.getParallax() + ", " +
                astrographicObject.getDistance() + ", " +
                astrographicObject.getRadialVelocity() + ", " +
                astrographicObject.getSpectralClass() + ", " +
                astrographicObject.getOrthoSpectralClass() + ", " +
                astrographicObject.getTemperature() + ", " +
                astrographicObject.isRealStar() + ", " +
                astrographicObject.getBprp() + ", " +
                astrographicObject.getBpg() + ", " +
                astrographicObject.getGrp() + ", " +
                astrographicObject.getLuminosity() + ", " +
                astrographicObject.getMagu() + ", " +
                astrographicObject.getMagb() + ", " +
                astrographicObject.getMagv() + ", " +
                astrographicObject.getMagr() + ", " +
                astrographicObject.getMagi() + ", " +
                astrographicObject.isOther() + ", " +
                astrographicObject.isAnomaly() + ", " +
                astrographicObject.getPolity() + ", " +
                astrographicObject.getWorldType() + ", " +
                astrographicObject.getFuelType() + ", " +
                astrographicObject.getPortType() + ", " +
                astrographicObject.getPopulationType() + ", " +
                astrographicObject.getTechType() + ", " +
                astrographicObject.getProductType() + ", " +
                astrographicObject.getMilSpaceType() + ", " +
                astrographicObject.getMilPlanType() + ", " +
                removeCommas(astrographicObject.getMiscText1()) + ", " +
                removeCommas(astrographicObject.getMiscText2()) + ", " +
                removeCommas(astrographicObject.getMiscText3()) + ", " +
                removeCommas(astrographicObject.getMiscText4()) + ", " +
                removeCommas(astrographicObject.getMiscText5()) + ", " +
                astrographicObject.getMiscNum1() + ", " +
                astrographicObject.getMiscNum2() + ", " +
                astrographicObject.getMiscNum3() + ", " +
                astrographicObject.getMiscNum4() + ", " +
                astrographicObject.getMiscNum5() + ", " +
                removeCommas(astrographicObject.getNotes()) +
                "\n";
    }

    private String removeCommas(String origin) {
        String replaced = origin;
        if (origin != null) {
            replaced= origin.replace(",", " ");
        }
        return replaced;
    }

}
