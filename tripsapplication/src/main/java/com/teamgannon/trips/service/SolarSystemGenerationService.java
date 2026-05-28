package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.model.SolarSystem;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.factories.SolarSystemFactoryRegistry;
import com.teamgannon.trips.service.factories.SolarSystemFactoryResult;
import com.teamgannon.trips.solarsystem.modelling.accrete.Planet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application boundary for creating and saving generated solar systems.
 * Default generation goes through {@link SolarSystemFactoryRegistry}; preview
 * flows that let the user inspect planets before saving persist through the
 * same generated-system persister used by the factory implementations.
 */
@Service
public class SolarSystemGenerationService {

    private final SolarSystemFactoryRegistry factoryRegistry;
    private final GeneratedSystemPersister generatedSystemPersister;
    private final SolarSystemService solarSystemService;

    public SolarSystemGenerationService(SolarSystemFactoryRegistry factoryRegistry,
                                        GeneratedSystemPersister generatedSystemPersister,
                                        SolarSystemService solarSystemService) {
        this.factoryRegistry = factoryRegistry;
        this.generatedSystemPersister = generatedSystemPersister;
        this.solarSystemService = solarSystemService;
    }

    @Transactional
    public SolarSystemFactoryResult generateDefault(StarObject star) {
        return factoryRegistry.generate(star);
    }

    @Transactional
    public int savePreviewedPlanets(StarObject sourceStar, List<Planet> planets) {
        return generatedSystemPersister.savePlanets(
                sourceStar,
                planets,
                this::findOrCreateSolarSystem);
    }

    private SolarSystem findOrCreateSolarSystem(StarObject sourceStar) {
        return solarSystemService.findOrCreateSolarSystem(sourceStar);
    }
}
