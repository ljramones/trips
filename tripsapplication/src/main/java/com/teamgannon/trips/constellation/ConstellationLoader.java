package com.teamgannon.trips.constellation;

import com.opencsv.bean.CsvToBeanBuilder;
import com.teamgannon.trips.config.application.Localization;
import com.teamgannon.trips.config.application.TripsContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
        try {
            File file = new File(localization.getProgramdata()+"constellation.csv");
            List<Constellation> constellationList = new CsvToBeanBuilder(new FileReader(file))
                    .withType(Constellation.class)
                    .build()
                    .parse();
            for (Constellation constellation : constellationList) {
                tripsContext.getConstellationMap().put(constellation.getName(), constellation);
            }
            log.info("\n\nConstellation map loaded");
        } catch (FileNotFoundException e) {
            log.error("file not found due to:" + e.getMessage());
        }
    }


}
