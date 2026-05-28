package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.model.SolarSystem;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.factories.SolarSystemFactoryRegistry;
import com.teamgannon.trips.service.factories.SolarSystemFactoryResult;
import com.teamgannon.trips.solarsystem.modelling.accrete.Planet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolarSystemGenerationServiceTest {

    @Mock
    private SolarSystemFactoryRegistry factoryRegistry;

    @Mock
    private GeneratedSystemPersister generatedSystemPersister;

    @Mock
    private SolarSystemService solarSystemService;

    private SolarSystemGenerationService service;

    @BeforeEach
    void setUp() {
        service = new SolarSystemGenerationService(factoryRegistry, generatedSystemPersister, solarSystemService);
    }

    @Test
    @DisplayName("default generation delegates to factory registry")
    void defaultGenerationDelegatesToFactoryRegistry() {
        StarObject star = star("Vega");
        SolarSystemFactoryResult expected = new SolarSystemFactoryResult("procedural-accrete", star, 2, 1, 0, false);
        when(factoryRegistry.generate(star)).thenReturn(expected);

        SolarSystemFactoryResult result = service.generateDefault(star);

        assertSame(expected, result);
        verify(factoryRegistry).generate(star);
    }

    @Test
    @DisplayName("previewed planet saves use generated persister with service solar-system lookup")
    void previewedPlanetSavesUseGeneratedPersister() {
        StarObject star = star("Vega");
        List<Planet> planets = List.of(new Planet(null));
        when(generatedSystemPersister.savePlanets(same(star), same(planets), org.mockito.ArgumentMatchers.any()))
                .thenReturn(3);

        int savedCount = service.savePreviewedPlanets(star, planets);

        assertEquals(3, savedCount);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Function<StarObject, SolarSystem>> lookupCaptor =
                ArgumentCaptor.forClass(Function.class);
        verify(generatedSystemPersister).savePlanets(same(star), same(planets), lookupCaptor.capture());

        SolarSystem solarSystem = new SolarSystem();
        when(solarSystemService.findOrCreateSolarSystem(star)).thenReturn(solarSystem);

        assertSame(solarSystem, lookupCaptor.getValue().apply(star));
    }

    private static StarObject star(String displayName) {
        StarObject star = new StarObject();
        star.setId(displayName + "-id");
        star.setDisplayName(displayName);
        return star;
    }
}
