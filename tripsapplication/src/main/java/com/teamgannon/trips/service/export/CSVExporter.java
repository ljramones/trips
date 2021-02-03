package com.teamgannon.trips.service.export;

import com.teamgannon.trips.dialogs.dataset.ExportOptions;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.listener.StatusUpdaterListener;
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


    public void exportAsCSV(@NotNull ExportOptions export, @NotNull List<StarObject> starObjects) {

        DataSetDescriptor dataSetDescriptor = export.getDataset();

        try {
            Writer writer = Files.newBufferedWriter(Paths.get(export.getFileName() + ".trips.csv"));

            createDescriptorHeading(writer, dataSetDescriptor);

            String headers = getHeaders();
            writer.write(headers);
            int i = 0;
            for (StarObject starObject : starObjects) {
                String csvRecord = convertToCSV(starObject);
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

    private @NotNull String convertToCSV(@NotNull StarObject starObject) {

        return removeCommas(starObject.getId().toString()) + ", " +
                removeCommas(starObject.getDataSetName()) + ", " +
                removeCommas(starObject.getDisplayName()) + ", " +
                removeCommas(starObject.getConstellationName()) + ", " +
                starObject.getMass() + ", " +
                removeCommas(starObject.getSource()) + ", " +
                starObject.getCatalogIdList() + ", " +
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
                removeCommas(starObject.getNotes()) +
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
