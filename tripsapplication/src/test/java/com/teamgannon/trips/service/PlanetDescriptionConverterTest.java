package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PlanetDescriptionConverter.
 */
class PlanetDescriptionConverterTest {

    @Nested
    @DisplayName("Single ExoPlanet Conversion")
    class SingleConversionTests {

        @Test
        @DisplayName("should convert basic planet properties")
        void shouldConvertBasicProperties() {
            ExoPlanet exoPlanet = createBasicExoPlanet();

            PlanetDescription result = PlanetDescriptionConverter.convert(exoPlanet);

            assertEquals("planet-123", result.getId());
            assertEquals("Kepler-442b", result.getName());
            assertEquals("Kepler-442", result.getBelongstoStar());
        }

        @Test
        @DisplayName("should convert mass when present")
        void shouldConvertMass() {
            ExoPlanet exoPlanet = createBasicExoPlanet();
            exoPlanet.setMass(2.34);

            PlanetDescription result = PlanetDescriptionConverter.convert(exoPlanet);

            assertEquals(2.34, result.getMass(), 0.001);
        }

        @Test
        @DisplayName("should handle null mass")
        void shouldHandleNullMass() {
            ExoPlanet exoPlanet = createBasicExoPlanet();
            exoPlanet.setMass(null);

            PlanetDescription result = PlanetDescriptionConverter.convert(exoPlanet);

            assertEquals(0.0, result.getMass(), 0.001);
        }

        @Test
        @DisplayName("should convert radius when present")
        void shouldConvertRadius() {
            ExoPlanet exoPlanet = createBasicExoPlanet();
            exoPlanet.setRadius(1.34);

            PlanetDescription result = PlanetDescriptionConverter.convert(exoPlanet);

            assertEquals(1.34, result.getRadius(), 0.001);
        }

        @Test
        @DisplayName("should handle null radius")
        void shouldHandleNullRadius() {
            ExoPlanet exoPlanet = createBasicExoPlanet();
            exoPlanet.setRadius(null);

            PlanetDescription result = PlanetDescriptionConverter.convert(exoPlanet);

            assertEquals(0.0, result.getRadius(), 0.001);
        }

        @Test
        @DisplayName("should convert orbital parameters")
        void shouldConvertOrbitalParameters() {
            ExoPlanet exoPlanet = createBasicExoPlanet();
            exoPlanet.setSemiMajorAxis(1.12);
            exoPlanet.setEccentricity(0.04);
            exoPlanet.setInclination(89.5);
            exoPlanet.setOrbitalPeriod(112.3);

            PlanetDescription result = PlanetDescriptionConverter.convert(exoPlanet);

            assertEquals(1.12, result.getSemiMajorAxis(), 0.001);
            assertEquals(0.04, result.getEccentricity(), 0.001);
            assertEquals(89.5, result.getInclination(), 0.001);
            assertEquals(112.3, result.getOrbitalPeriod(), 0.001);
        }

        @Test
        @DisplayName("should handle null orbital parameters")
        void shouldHandleNullOrbitalParameters() {
            ExoPlanet exoPlanet = createBasicExoPlanet();
            exoPlanet.setSemiMajorAxis(null);
            exoPlanet.setEccentricity(null);
            exoPlanet.setInclination(null);
            exoPlanet.setOrbitalPeriod(null);

            PlanetDescription result = PlanetDescriptionConverter.convert(exoPlanet);

            assertEquals(0.0, result.getSemiMajorAxis(), 0.001);
            assertEquals(0.0, result.getEccentricity(), 0.001);
            assertEquals(0.0, result.getInclination(), 0.001);
            assertEquals(0.0, result.getOrbitalPeriod(), 0.001);
        }

        @Test
        @DisplayName("should convert omega (argument of periapsis)")
        void shouldConvertOmega() {
            ExoPlanet exoPlanet = createBasicExoPlanet();
            exoPlanet.setOmega(45.0);

            PlanetDescription result = PlanetDescriptionConverter.convert(exoPlanet);

            assertEquals(45.0, result.getArgumentOfPeriapsis(), 0.001);
        }

        @Test
        @DisplayName("should convert time of periapsis passage")
        void shouldConvertTperi() {
            ExoPlanet exoPlanet = createBasicExoPlanet();
            exoPlanet.setTperi(2451545.0);

            PlanetDescription result = PlanetDescriptionConverter.convert(exoPlanet);

            assertEquals(2451545.0, result.getTimeOfPeriapsisPassage(), 0.001);
        }

        @Test
        @DisplayName("should convert calculated temperature")
        void shouldConvertCalculatedTemperature() {
            ExoPlanet exoPlanet = createBasicExoPlanet();
            exoPlanet.setTempCalculated(288.0);

            PlanetDescription result = PlanetDescriptionConverter.convert(exoPlanet);

            assertEquals(288.0, result.getEquilibriumTemperature(), 0.001);
        }

        @Test
        @DisplayName("should use measured temperature when calculated is null")
        void shouldUseMeasuredTemperatureWhenCalculatedIsNull() {
            ExoPlanet exoPlanet = createBasicExoPlanet();
            exoPlanet.setTempCalculated(null);
            exoPlanet.setTempMeasured(295.0);

            PlanetDescription result = PlanetDescriptionConverter.convert(exoPlanet);

            assertEquals(295.0, result.getEquilibriumTemperature(), 0.001);
        }

        @Test
        @DisplayName("should prefer calculated temperature over measured")
        void shouldPreferCalculatedOverMeasured() {
            ExoPlanet exoPlanet = createBasicExoPlanet();
            exoPlanet.setTempCalculated(288.0);
            exoPlanet.setTempMeasured(295.0);

            PlanetDescription result = PlanetDescriptionConverter.convert(exoPlanet);

            assertEquals(288.0, result.getEquilibriumTemperature(), 0.001);
        }

        @Test
        @DisplayName("should convert surface gravity (log g)")
        void shouldConvertLogG() {
            ExoPlanet exoPlanet = createBasicExoPlanet();
            exoPlanet.setLogG(3.5);

            PlanetDescription result = PlanetDescriptionConverter.convert(exoPlanet);

            assertEquals(3.5, result.getSurfaceGravity(), 0.001);
        }

        @Test
        @DisplayName("should convert moon flag when true")
        void shouldConvertMoonFlagTrue() {
            ExoPlanet exoPlanet = createBasicExoPlanet();
            exoPlanet.setIsMoon(true);
            exoPlanet.setParentPlanetId("parent-planet-id");

            PlanetDescription result = PlanetDescriptionConverter.convert(exoPlanet);

            assertTrue(result.isMoon());
            assertEquals("parent-planet-id", result.getParentPlanetId());
        }

        @Test
        @DisplayName("should convert moon flag when false")
        void shouldConvertMoonFlagFalse() {
            ExoPlanet exoPlanet = createBasicExoPlanet();
            exoPlanet.setIsMoon(false);
            exoPlanet.setParentPlanetId(null);

            PlanetDescription result = PlanetDescriptionConverter.convert(exoPlanet);

            assertFalse(result.isMoon());
            assertNull(result.getParentPlanetId());
        }

        @Test
        @DisplayName("should handle null moon flag")
        void shouldHandleNullMoonFlag() {
            ExoPlanet exoPlanet = createBasicExoPlanet();
            exoPlanet.setIsMoon(null);

            PlanetDescription result = PlanetDescriptionConverter.convert(exoPlanet);

            assertFalse(result.isMoon());
        }
    }

    @Nested
    @DisplayName("List Conversion")
    class ListConversionTests {

        @Test
        @DisplayName("should convert list of planets")
        void shouldConvertListOfPlanets() {
            List<ExoPlanet> exoPlanets = new ArrayList<>();
            exoPlanets.add(createExoPlanet("planet-1", "Planet b", "Star A"));
            exoPlanets.add(createExoPlanet("planet-2", "Planet c", "Star A"));
            exoPlanets.add(createExoPlanet("planet-3", "Planet d", "Star A"));

            List<PlanetDescription> results = PlanetDescriptionConverter.convert(exoPlanets);

            assertEquals(3, results.size());
            assertEquals("planet-1", results.get(0).getId());
            assertEquals("planet-2", results.get(1).getId());
            assertEquals("planet-3", results.get(2).getId());
        }

        @Test
        @DisplayName("should return empty list for empty input")
        void shouldReturnEmptyListForEmptyInput() {
            List<ExoPlanet> exoPlanets = new ArrayList<>();

            List<PlanetDescription> results = PlanetDescriptionConverter.convert(exoPlanets);

            assertNotNull(results);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("should handle single element list")
        void shouldHandleSingleElementList() {
            List<ExoPlanet> exoPlanets = new ArrayList<>();
            exoPlanets.add(createBasicExoPlanet());

            List<PlanetDescription> results = PlanetDescriptionConverter.convert(exoPlanets);

            assertEquals(1, results.size());
            assertEquals("planet-123", results.get(0).getId());
        }
    }

    // Helper methods

    private ExoPlanet createBasicExoPlanet() {
        return createExoPlanet("planet-123", "Kepler-442b", "Kepler-442");
    }

    private ExoPlanet createExoPlanet(String id, String name, String starName) {
        ExoPlanet exoPlanet = new ExoPlanet();
        exoPlanet.setId(id);
        exoPlanet.setName(name);
        exoPlanet.setStarName(starName);
        return exoPlanet;
    }
}
