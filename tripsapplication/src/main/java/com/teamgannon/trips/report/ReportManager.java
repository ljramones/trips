package com.teamgannon.trips.report;

import com.teamgannon.trips.dialogs.inventory.ComputerInventoryDialog;
import com.teamgannon.trips.dialogs.inventory.InventoryReport;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.report.distance.DistanceReport;
import com.teamgannon.trips.report.distance.DistanceReportDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ReportManager  {

    public void generateDistanceReport(Stage stage, StarDisplayRecord starDescriptor, List<StarDisplayRecord> currentStarsInView) {
        DistanceReport distanceReport = new DistanceReport(starDescriptor);
        distanceReport.findDistance(currentStarsInView);
        distanceReport.generateReport();
        DistanceReportDialog dialog = new DistanceReportDialog(distanceReport);
        Optional<DistanceReport> distanceReportOptional = dialog.showAndWait();
        if (distanceReportOptional.isPresent()) {
            distanceReport = distanceReportOptional.get();
            if (distanceReport.isSaveSelected()) {
                // save the file
               fileSave(stage, distanceReport.getGeneratedReport());
            }
        }
    }

    public void generateComputerInventoryReport(Stage stage, InventoryReport inventoryReport) {
        ComputerInventoryDialog dialog = new ComputerInventoryDialog(inventoryReport);
        Optional<InventoryReport> inventoryReportOptional = dialog.showAndWait();
        if (inventoryReportOptional.isPresent()) {
            inventoryReport = inventoryReportOptional.get();
            if (inventoryReport.isSaveSelected()) {
                // save the file
                fileSave(stage, inventoryReport.getInventoryDescription());
            }
        }
    }

    private void fileSave(Stage stage, String report) {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter for text files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            saveTextToFile(report, file);
        }
    }

    private void saveTextToFile(String generatedReport, File file) {
        try (PrintWriter out = new PrintWriter(file)) {
            out.println(generatedReport);
        } catch (FileNotFoundException e) {
            log.error("Can't create file {} because of {}", file.getAbsolutePath(), e.getMessage());
        }
    }

}
