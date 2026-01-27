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
            log.error("Failed to read constellation data from {}: {}", file, e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Failed to parse constellation data from {}: {}", file, e.getMessage(), e);
        }
    }


}
