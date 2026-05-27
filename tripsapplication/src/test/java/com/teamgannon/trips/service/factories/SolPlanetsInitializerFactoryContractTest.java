package com.teamgannon.trips.service.factories;

import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.SolPlanetsInitializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Pins {@link SolPlanetsInitializer}'s {@link SolarSystemFactory#appliesTo}
 * + {@link SolarSystemFactory#name} contract — these are pure-function
 * predicates we can exercise without spinning up Spring or the database.
 * Issue 18.
 */
class SolPlanetsInitializerFactoryContractTest {

    private SolPlanetsInitializer factory() {
        // The factory contract methods don't touch the repositories, so
        // null mocks are sufficient.
        return new SolPlanetsInitializer(
                mock(com.teamgannon.trips.jpa.repository.StarObjectRepository.class),
                mock(com.teamgannon.trips.jpa.repository.SolarSystemRepository.class),
                mock(com.teamgannon.trips.jpa.repository.ExoPlanetRepository.class),
                mock(com.teamgannon.trips.jpa.repository.SolarSystemFeatureRepository.class));
    }

    @Test
    @DisplayName("name() is stable")
    void nameStable() {
        assertEquals(SolPlanetsInitializer.FACTORY_NAME, factory().name());
    }

    @Test
    @DisplayName("appliesTo accepts 'Sol' (case-insensitive, trimmed)")
    void appliesToSol() {
        SolPlanetsInitializer f = factory();
        assertTrue(f.appliesTo(star("Sol")));
        assertTrue(f.appliesTo(star("sol")));
        assertTrue(f.appliesTo(star("SOL")));
        assertTrue(f.appliesTo(star("  Sol  ")));
    }

    @Test
    @DisplayName("appliesTo rejects other stars")
    void rejectsOtherStars() {
        SolPlanetsInitializer f = factory();
        assertFalse(f.appliesTo(star("Vega")));
        assertFalse(f.appliesTo(star("Alpha Centauri")));
        assertFalse(f.appliesTo(star("Solar System"))); // word containing "Sol" but not equal
        assertFalse(f.appliesTo(star("")));
    }

    @Test
    @DisplayName("appliesTo handles null inputs gracefully")
    void nullSafe() {
        SolPlanetsInitializer f = factory();
        assertFalse(f.appliesTo(null));
        assertFalse(f.appliesTo(star(null)));
    }

    private static StarObject star(String displayName) {
        StarObject s = new StarObject();
        s.setId("test-id");
        s.setDisplayName(displayName);
        return s;
    }
}
