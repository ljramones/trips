package com.teamgannon.trips.solarsystem;

import com.opencsv.bean.CsvToBeanBuilder;
import com.teamgannon.trips.config.application.Localization;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class SolSolarSystem {

    private final Localization localization;
    /**
     * the list of planets
     */
    @Value("classpath:files/solarSystemPlanets.csv")
    private Resource planetResource;

    /**
     * the list of comets
     */
    @Value("classpath:files/cometParameters.csv")
    private Resource cometResource;

    /**
     * a map of the planets
     */
    Map<String, SolSolarSystemPlanetModel> planetMap = new HashMap<>();

    /**
     * a map of the comets
     */
    Map<String, SolSolarSystemCometModel> cometMap = new HashMap<>();

    public SolSolarSystem(Localization localization) {
        this.localization = localization;
    }

    /**
     * load the story system to look up
     */
    @PostConstruct
    public void loadSolarSystem() {
        loadPlanets();
        loadComets();
        log.info("loaded the solar system");
    }

    private void loadComets() {
        // NB: method-vs-CSV naming has been swapped here since forever — this
        // method loads "solarSystemPlanets.csv" into planetMap. Names of the
        // two methods are preserved to keep call-site behaviour stable.
        File file = java.nio.file.Path.of(localization.getProgramdata()).resolve("solarSystemPlanets.csv").toFile();
        if (!file.exists()) {
            log.warn("Sol planet data file not found at {} — Sol-system planet details will be unavailable. "
                    + "Install the optional data bundle to enable this feature.", file);
            return;
        }
        try {
            List<SolSolarSystemPlanetModel> planets = new CsvToBeanBuilder(new FileReader(file))
                    .withType(SolSolarSystemPlanetModel.class)
                    .build()
                    .parse();
            for (SolSolarSystemPlanetModel planet : planets) {
                planetMap.put(planet.getName(), planet);
            }
            log.info("Sol planet map loaded ({} entries)", planetMap.size());
        } catch (FileNotFoundException e) {
            log.error("Failed to read Sol planet data from {}", file, e);
        }
    }

    private void loadPlanets() {
        File file = java.nio.file.Path.of(localization.getProgramdata()).resolve("cometParameters.csv").toFile();
        if (!file.exists()) {
            log.warn("Sol comet data file not found at {} — Sol-system comet details will be unavailable. "
                    + "Install the optional data bundle to enable this feature.", file);
            return;
        }
        try {
            List<SolSolarSystemCometModel> comets = new CsvToBeanBuilder(new FileReader(file))
                    .withType(SolSolarSystemCometModel.class)
                    .build()
                    .parse();
            for (SolSolarSystemCometModel cometModel : comets) {
                cometMap.put(cometModel.getName(), cometModel);
            }
            log.info("Sol comet map loaded ({} entries)", cometMap.size());
        } catch (FileNotFoundException e) {
            log.error("Failed to read Sol comet data from {}", file, e);
        }
    }

}
