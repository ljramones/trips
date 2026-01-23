package com.teamgannon.trips.jpa.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ExoPlanet constructors and ID generation.
 */
@DisplayName("ExoPlanet Constructor Tests")
class ExoPlanetConstructorTest {

    @Nested
    @DisplayName("Default Constructor")
    class DefaultConstructorTests {

        @Test
        @DisplayName("should create instance with no-args constructor")
        void shouldCreateWithNoArgs() {
            ExoPlanet planet = new ExoPlanet();

            assertNotNull(planet);
        }

        @Test
        @DisplayName("should have null id initially")
        void shouldHaveNullIdInitially() {
            ExoPlanet planet = new ExoPlanet();

            assertNull(planet.getId());
        }

        @Test
        @DisplayName("should have null name initially")
        void shouldHaveNullNameInitially() {
            ExoPlanet planet = new ExoPlanet();

            assertNull(planet.getName());
        }
    }

    @Nested
    @DisplayName("Name Constructor")
    class NameConstructorTests {

        @Test
        @DisplayName("should create instance with name")
        void shouldCreateWithName() {
            ExoPlanet planet = new ExoPlanet("Kepler-442b");

            assertNotNull(planet);
            assertEquals("Kepler-442b", planet.getName());
        }

        @Test
        @DisplayName("should auto-generate UUID for id")
        void shouldAutoGenerateId() {
            ExoPlanet planet = new ExoPlanet("Test Planet");

            assertNotNull(planet.getId());
            assertFalse(planet.getId().isEmpty());
        }

        @Test
        @DisplayName("should generate unique IDs for different instances")
        void shouldGenerateUniqueIds() {
            ExoPlanet planet1 = new ExoPlanet("Planet 1");
            ExoPlanet planet2 = new ExoPlanet("Planet 2");

            assertNotEquals(planet1.getId(), planet2.getId());
        }

        @Test
        @DisplayName("should generate valid UUID format")
        void shouldGenerateValidUuidFormat() {
            ExoPlanet planet = new ExoPlanet("Test Planet");

            String id = planet.getId();
            // UUID format: 8-4-4-4-12
            assertTrue(id.matches("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"));
        }
    }

    @Nested
    @DisplayName("Setter Methods")
    class SetterMethodTests {

        @Test
        @DisplayName("should allow setting all basic fields")
        void shouldAllowSettingBasicFields() {
            ExoPlanet planet = new ExoPlanet();
            planet.setId("custom-id");
            planet.setName("Custom Planet");
            planet.setSolarSystemId("system-123");
            planet.setHostStarId("star-456");
            planet.setMass(1.5);
            planet.setRadius(1.2);
            planet.setSemiMajorAxis(1.0);
            planet.setEccentricity(0.05);

            assertEquals("custom-id", planet.getId());
            assertEquals("Custom Planet", planet.getName());
            assertEquals("system-123", planet.getSolarSystemId());
            assertEquals("star-456", planet.getHostStarId());
            assertEquals(1.5, planet.getMass());
            assertEquals(1.2, planet.getRadius());
            assertEquals(1.0, planet.getSemiMajorAxis());
            assertEquals(0.05, planet.getEccentricity());
        }

        @Test
        @DisplayName("should allow setting boolean fields")
        void shouldAllowSettingBooleanFields() {
            ExoPlanet planet = new ExoPlanet();
            planet.setIsMoon(true);
            planet.setHabitable(true);
            planet.setEarthlike(false);
            planet.setGasGiant(false);
            planet.setColonized(true);

            assertTrue(planet.getIsMoon());
            assertTrue(planet.getHabitable());
            assertFalse(planet.getEarthlike());
            assertFalse(planet.getGasGiant());
            assertTrue(planet.getColonized());
        }

        @Test
        @DisplayName("should allow setting extended properties")
        void shouldAllowSettingExtendedProperties() {
            ExoPlanet planet = new ExoPlanet();
            planet.setPlanetType("Terrestrial");
            planet.setAtmosphereType("Breathable");
            planet.setSurfaceTemperature(288.0);
            planet.setSurfaceGravity(1.0);
            planet.setHydrosphere(0.71);
            planet.setPopulation(8000000000L);
            planet.setPolity("Earth Federation");

            assertEquals("Terrestrial", planet.getPlanetType());
            assertEquals("Breathable", planet.getAtmosphereType());
            assertEquals(288.0, planet.getSurfaceTemperature());
            assertEquals(1.0, planet.getSurfaceGravity());
            assertEquals(0.71, planet.getHydrosphere());
            assertEquals(8000000000L, planet.getPopulation());
            assertEquals("Earth Federation", planet.getPolity());
        }
    }

    @Nested
    @DisplayName("Orbital Element Fields")
    class OrbitalElementTests {

        @Test
        @DisplayName("should store all Keplerian orbital elements")
        void shouldStoreKeplerianElements() {
            ExoPlanet planet = new ExoPlanet("Test Planet");
            planet.setSemiMajorAxis(1.0);
            planet.setEccentricity(0.017);
            planet.setInclination(7.155);
            planet.setLongitudeOfAscendingNode(48.331);
            planet.setOmega(114.208); // Argument of periapsis

            assertEquals(1.0, planet.getSemiMajorAxis());
            assertEquals(0.017, planet.getEccentricity());
            assertEquals(7.155, planet.getInclination());
            assertEquals(48.331, planet.getLongitudeOfAscendingNode());
            assertEquals(114.208, planet.getOmega());
        }

        @Test
        @DisplayName("should allow null orbital elements")
        void shouldAllowNullOrbitalElements() {
            ExoPlanet planet = new ExoPlanet("Test Planet");

            assertNull(planet.getSemiMajorAxis());
            assertNull(planet.getEccentricity());
            assertNull(planet.getInclination());
            assertNull(planet.getLongitudeOfAscendingNode());
            assertNull(planet.getOmega());
        }
    }

    @Nested
    @DisplayName("Procedural Generation Fields")
    class ProceduralFieldTests {

        @Test
        @DisplayName("should store procedural generation metadata")
        void shouldStoreProceduralMetadata() {
            ExoPlanet planet = new ExoPlanet("Generated Planet");
            planet.setProceduralSeed(12345L);
            planet.setProceduralGeneratorVersion("1.0.0");
            planet.setProceduralSource("ACCRETE");
            planet.setProceduralGeneratedAt("2024-01-15T10:30:00Z");

            assertEquals(12345L, planet.getProceduralSeed());
            assertEquals("1.0.0", planet.getProceduralGeneratorVersion());
            assertEquals("ACCRETE", planet.getProceduralSource());
            assertEquals("2024-01-15T10:30:00Z", planet.getProceduralGeneratedAt());
        }

        @Test
        @DisplayName("should store procedural preview image")
        void shouldStoreProceduralPreview() {
            ExoPlanet planet = new ExoPlanet("Preview Planet");
            byte[] previewData = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}; // PNG magic bytes
            planet.setProceduralPreview(previewData);

            assertArrayEquals(previewData, planet.getProceduralPreview());
        }
    }
}
