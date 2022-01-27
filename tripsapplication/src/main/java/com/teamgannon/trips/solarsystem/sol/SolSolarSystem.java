package com.teamgannon.trips.solarsystem.sol;

import com.opencsv.bean.CsvToBeanBuilder;
import com.teamgannon.trips.config.application.Localization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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
        try {
            File file = new File(localization.getProgramdata() + "solarSystemPlanets.csv");
            List<SolSolarSystemPlanetModel> planets = new CsvToBeanBuilder(new FileReader(file))
                    .withType(SolSolarSystemPlanetModel.class)
                    .build()
                    .parse();
            for (SolSolarSystemPlanetModel planet : planets) {
                planetMap.put(planet.getName(), planet);
            }
            log.info("\n\nsolar system loaded\n\n");
        } catch (FileNotFoundException e) {
            log.error("file not found due to:" + e.getMessage());
        }
    }

    private void loadPlanets() {
        try {
            File file = new File(localization.getProgramdata() + "cometParameters.csv");
            List<SolSolarSystemCometModel> comets = new CsvToBeanBuilder(new FileReader(file))
                    .withType(SolSolarSystemCometModel.class)
                    .build()
                    .parse();
            for (SolSolarSystemCometModel cometModel : comets) {
                cometMap.put(cometModel.getName(), cometModel);
            }
            log.info("\n\nComet list loaded\n\n");
        } catch (FileNotFoundException e) {
            log.error("file not found due to:" + e.getMessage());
        }
    }

}
