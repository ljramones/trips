package com.teamgannon.trips.tableviews;

import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.service.StarService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;

/**
 * Background service for streaming export of all stars matching a query to CSV.
 * Uses paged iteration to avoid loading all data into memory at once.
 */
@Slf4j
public class StarTableExportService extends Service<Long> {

    private static final int EXPORT_PAGE_SIZE = 500;

    private final StarService starService;
    private final AstroSearchQuery query;
    private final File outputFile;

    /**
     * Create a new export service.
     *
     * @param starService the star service for database access
     * @param query       the search query defining which stars to export
     * @param outputFile  the file to write to
     */
    public StarTableExportService(StarService starService, AstroSearchQuery query, File outputFile) {
        this.starService = starService;
        this.query = query;
        this.outputFile = outputFile;
    }

    @Override
    protected Task<Long> createTask() {
        return new Task<>() {
            @Override
            protected Long call() throws Exception {
                long total = starService.countBySearchQuery(query);
                long exported = 0;
                int pageNum = 0;

                updateMessage("Counting records...");
                updateProgress(0, total);

                try (BufferedWriter writer = Files.newBufferedWriter(outputFile.toPath())) {
                    // Write header
                    writeHeader(writer);

                    Page<StarObject> page;
                    do {
                        if (isCancelled()) {
                            updateMessage("Export cancelled");
                            return exported;
                        }

                        page = starService.getStarPaged(query,
                                PageRequest.of(pageNum++, EXPORT_PAGE_SIZE, Sort.by("displayName")));

                        for (StarObject star : page.getContent()) {
                            if (isCancelled()) {
                                updateMessage("Export cancelled");
                                return exported;
                            }

                            // Skip invalid records
                            if (star.getDisplayName() == null || star.getDisplayName().equalsIgnoreCase("name")) {
                                continue;
                            }

                            writeLine(writer, star);
                            exported++;
                            updateProgress(exported, total);
                            updateMessage(String.format("Exported %d of %d records...", exported, total));
                        }
                    } while (page.hasNext());

                    updateMessage(String.format("Export complete: %d records", exported));
                }

                return exported;
            }

            private void writeHeader(BufferedWriter writer) throws Exception {
                writer.write(String.join(",",
                        "Display Name",
                        "Common Name",
                        "Distance (LY)",
                        "Spectra",
                        "Radius",
                        "Mass",
                        "Luminosity",
                        "Temperature",
                        "RA",
                        "Declination",
                        "Parallax",
                        "X",
                        "Y",
                        "Z",
                        "Real",
                        "Polity",
                        "Constellation",
                        "Notes"));
                writer.newLine();
            }

            private void writeLine(BufferedWriter writer, StarObject star) throws Exception {
                writer.write(String.join(",",
                        csvCell(star.getDisplayName()),
                        csvCell(star.getCommonName()),
                        csvCell(star.getDistance()),
                        csvCell(star.getSpectralClass()),
                        csvCell(star.getRadius()),
                        csvCell(star.getMass()),
                        csvCell(star.getLuminosity()),
                        csvCell(star.getTemperature()),
                        csvCell(star.getRa()),
                        csvCell(star.getDeclination()),
                        csvCell(star.getParallax()),
                        csvCell(star.getX()),
                        csvCell(star.getY()),
                        csvCell(star.getZ()),
                        csvCell(star.isRealStar()),
                        csvCell(star.getPolity()),
                        csvCell(star.getConstellationName()),
                        csvCell(star.getNotes())));
                writer.newLine();
            }

            private String csvCell(String value) {
                if (value == null) {
                    return "";
                }
                String escaped = value.replace("\"", "\"\"");
                if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
                    return "\"" + escaped + "\"";
                }
                return escaped;
            }

            private String csvCell(Double value) {
                return value == null ? "" : value.toString();
            }

            private String csvCell(Boolean value) {
                return value == null ? "" : value.toString();
            }
        };
    }
}
