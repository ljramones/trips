package com.teamgannon.trips.tableviews;

import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.service.StarService;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Handles CSV export functionality for star table data.
 * Supports both current page export and full dataset export with progress tracking.
 */
@Slf4j
public class StarTableCsvExporter {

    private static final String[] CSV_HEADERS = {
            "Display Name",
            "Distance (LY)",
            "Spectra",
            "Radius",
            "Mass",
            "Luminosity",
            "RA",
            "Declination",
            "Parallax",
            "X",
            "Y",
            "Z",
            "Real",
            "Comment"
    };

    private final StarService starService;
    private final AstroSearchQuery query;
    private final String dataSetName;

    public StarTableCsvExporter(StarService starService, AstroSearchQuery query, String dataSetName) {
        this.starService = starService;
        this.query = query;
        this.dataSetName = dataSetName;
    }

    /**
     * Export the current page of star records to CSV.
     *
     * @param records         the records to export
     * @param currentPage     current page index (0-based)
     * @param window          parent window for file chooser
     * @param onStatusUpdate  callback for status updates
     */
    public void exportCurrentPage(List<StarEditRecord> records, int currentPage,
                                  Window window, Consumer<String> onStatusUpdate) {
        if (records.isEmpty()) {
            showErrorAlert("Export CSV", "No rows on this page to export.");
            return;
        }

        FileChooser fileChooser = createFileChooser(
                "Export Page to CSV",
                dataSetName + "-page-" + (currentPage + 1) + ".csv"
        );

        File file = fileChooser.showSaveDialog(window);
        if (file == null) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writeHeader(writer);
            for (StarEditRecord record : records) {
                writeLine(writer, record);
            }
            onStatusUpdate.accept("Exported " + records.size() + " records to " + file.getName());
        } catch (IOException e) {
            log.error("Failed to export CSV", e);
            showErrorAlert("Export CSV", "Failed to export: " + e.getMessage());
        }
    }

    /**
     * Export all stars matching the query to CSV with progress tracking.
     *
     * @param totalCount     total number of stars to export
     * @param window         parent window for file chooser
     * @param onStatusUpdate callback for status updates
     */
    public void exportAll(long totalCount, Window window, Consumer<String> onStatusUpdate) {
        if (totalCount == 0) {
            showErrorAlert("Export CSV", "No stars to export.");
            return;
        }

        FileChooser fileChooser = createFileChooser(
                "Export All Stars to CSV",
                dataSetName + "-all.csv"
        );

        File file = fileChooser.showSaveDialog(window);
        if (file == null) {
            return;
        }

        // Create and start export service
        StarTableExportService exportService = new StarTableExportService(starService, query, file);

        // Show progress dialog
        showExportProgressDialog(exportService, file, onStatusUpdate);

        exportService.start();
    }

    private void showExportProgressDialog(StarTableExportService exportService, File file,
                                          Consumer<String> onStatusUpdate) {
        Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
        progressAlert.setTitle("Exporting...");
        progressAlert.setHeaderText("Exporting all stars to CSV");
        progressAlert.getButtonTypes().setAll(ButtonType.CANCEL);

        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        Label progressLabel = new Label("Starting export...");

        VBox content = new VBox(10);
        content.getChildren().addAll(progressLabel, progressBar);
        progressAlert.getDialogPane().setContent(content);

        progressBar.progressProperty().bind(exportService.progressProperty());
        exportService.messageProperty().addListener((obs, old, msg) ->
                Platform.runLater(() -> progressLabel.setText(msg)));

        exportService.setOnSucceeded(e -> {
            progressAlert.close();
            Long count = exportService.getValue();
            onStatusUpdate.accept("Exported " + count + " records to " + file.getName());
        });

        exportService.setOnFailed(e -> {
            progressAlert.close();
            Throwable ex = exportService.getException();
            log.error("Export failed", ex);
            showErrorAlert("Export Failed", "Export failed: " + (ex != null ? ex.getMessage() : "Unknown error"));
        });

        exportService.setOnCancelled(e -> {
            progressAlert.close();
            onStatusUpdate.accept("Export cancelled.");
        });

        progressAlert.setOnCloseRequest(e -> {
            if (exportService.isRunning()) {
                exportService.cancel();
            }
        });

        progressAlert.show();
    }

    private FileChooser createFileChooser(String title, String initialFileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName(initialFileName);
        return fileChooser;
    }

    /**
     * Write CSV header line.
     */
    public void writeHeader(BufferedWriter writer) throws IOException {
        writer.write(String.join(",", CSV_HEADERS));
        writer.newLine();
    }

    /**
     * Write a single record as CSV line.
     */
    public void writeLine(BufferedWriter writer, StarEditRecord record) throws IOException {
        writer.write(String.join(",",
                csvCell(record.getDisplayName()),
                csvCell(record.getDistanceToEarth()),
                csvCell(record.getSpectra()),
                csvCell(record.getRadius()),
                csvCell(record.getMass()),
                csvCell(record.getLuminosity()),
                csvCell(record.getRa()),
                csvCell(record.getDeclination()),
                csvCell(record.getParallax()),
                csvCell(record.getXCoord()),
                csvCell(record.getYCoord()),
                csvCell(record.getZCoord()),
                csvCell(record.isReal()),
                csvCell(record.getComment())));
        writer.newLine();
    }

    /**
     * Format a string value for CSV output.
     */
    public static String csvCell(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    /**
     * Format a Double value for CSV output.
     */
    public static String csvCell(Double value) {
        return value == null ? "" : value.toString();
    }

    /**
     * Format a Boolean value for CSV output.
     */
    public static String csvCell(Boolean value) {
        return value == null ? "" : value.toString();
    }

    /**
     * Get the CSV headers.
     */
    public static String[] getHeaders() {
        return CSV_HEADERS.clone();
    }
}
