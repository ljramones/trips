package com.teamgannon.trips.service.export;

import com.teamgannon.trips.dialogs.dataset.ExportOptions;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.StatusUpdaterListener;
import com.teamgannon.trips.service.DatabaseManagementService;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;
import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

@Slf4j
public class CSVExporter {

    private final StatusUpdaterListener updaterListener;

    public CSVExporter(StatusUpdaterListener updaterListener) {
        this.updaterListener = updaterListener;
    }


    public void exportAsCSV(@NotNull ExportOptions export, @NotNull List<AstrographicObject> astrographicObjects) {

        DataSetDescriptor dataSetDescriptor = export.getDataset();

        try {
            Writer writer = Files.newBufferedWriter(Paths.get(export.getFileName() + ".trips.csv"));

            createDescriptorHeading(writer, dataSetDescriptor);

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
            showInfoMessage("Database Export", export.getDataset().getDataSetName()
                    + " was exported to " + export.getFileName() + ".trips.csv");

        } catch (Exception e) {
            log.error("caught error opening the file:{}", e.getMessage());
            showErrorAlert(
                    "Export Dataset as CSV file",
                    dataSetDescriptor.getDataSetName() +
                            "failed to export:" + e.getMessage()
            );
        }
    }

    private void createDescriptorHeading(@NotNull Writer writer, @NotNull DataSetDescriptor dataSetDescriptor) {
        try {
            String headers = getDescriptorHeaders();
            writer.write(headers);
            String csvRecord = convertToCSV(dataSetDescriptor);
            writer.write(csvRecord);
        } catch (Exception e) {
            log.error("caught error opening the file:{}", e.getMessage());
            showErrorAlert(
                    "Export Dataset as CSV file",
                    dataSetDescriptor.getDataSetName() +
                            "failed to export:" + e.getMessage()
            );
        }
    }

    private @NotNull String convertToCSV(@NotNull DataSetDescriptor dataSetDescriptor) {
        return removeCommas(dataSetDescriptor.getDataSetName()) + "," +
                removeCommas(dataSetDescriptor.getFilePath()) + "," +
                removeCommas(dataSetDescriptor.getFileCreator()) + "," +
                dataSetDescriptor.getFileOriginalDate() + "," +
                removeCommas(dataSetDescriptor.getFileNotes()) + "," +
                removeCommas(dataSetDescriptor.getDatasetType()) + "," +
                dataSetDescriptor.getNumberStars() + "," +
                dataSetDescriptor.getDistanceRange() + "," +
                dataSetDescriptor.getNumberRoutes() + "," +
                removeCommas(dataSetDescriptor.getThemeStr()) + "," +
                dataSetDescriptor.getAstrographicDataList() + "," +
                removeCommas(dataSetDescriptor.getRoutesStr()) + "," +
                removeCommas(dataSetDescriptor.getCustomDataDefsStr()) + "," +
                removeCommas(dataSetDescriptor.getCustomDataValuesStr()) + ","
                + "\n";
    }

    private @NotNull String getDescriptorHeaders() {
        return "dataSetName," +
                "filePath," +
                "fileCreator," +
                "fileOriginalDate," +
                "fileNotes," +
                "datasetType," +
                "numberStars," +
                "distanceRange," +
                "numberRoutes," +
                "themeStr," +
                "astrographicDataList," +
                "routesStr," +
                "customDataDefsStr," +
                "customDataValuesStr" +
                "\n";
    }


    private @NotNull String getHeaders() {
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

    private @NotNull String convertToCSV(@NotNull AstrographicObject astrographicObject) {

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
                astrographicObject.getZ() + ", " +
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

    private @Nullable String removeCommas(@Nullable String origin) {
        String replaced = origin;
        if (origin != null) {
            replaced = origin.replace(",", "~");
        }
        return replaced;
    }


    private void updateStatus(String status) {
        new Thread(() -> Platform.runLater(() -> {
            log.info(status);
            updaterListener.updateStatus(status);
        })).start();
    }

}
