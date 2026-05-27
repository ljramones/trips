package com.teamgannon.trips.constellation;

import com.opencsv.bean.CsvToBeanBuilder;
import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.config.application.TripsContext;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Component
public class ConstellationLoader {

    private final TripsContext tripsContext;

    private final Localization localization;

    public ConstellationLoader(TripsContext tripsContext,
                               Localization localization) {
        this.tripsContext = tripsContext;
        this.localization = localization;
    }

    @PostConstruct
    public void initialize() {
        Path file = Paths.get(localization.getProgramdata(), "constellation.csv");
        if (!Files.exists(file)) {
            // Optional data file — constellation names just won't be available
            // in tooltips. Don't flood the log with a stack trace for the
            // expected first-run case where the user hasn't installed the
            // optional data bundle.
            log.warn("Constellation data file not found at {} — constellation names will be unavailable. "
                    + "Install the optional data bundle to enable this feature.", file);
            return;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            List<Constellation> constellationList = new CsvToBeanBuilder(reader)
                    .withType(Constellation.class)
                    .build()
                    .parse();
            for (Constellation constellation : constellationList) {
                tripsContext.getConstellationMap().put(constellation.getName(), constellation);
            }
            log.info("Constellation map loaded from {}", file);
        } catch (IOException e) {
            // File existed at the exists() check but failed to read — that's
            // a real I/O problem worth a full stack trace.
            log.error("Failed to read constellation data from {}: {}", file, e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Failed to parse constellation data from {}: {}", file, e.getMessage(), e);
        }
    }


}
