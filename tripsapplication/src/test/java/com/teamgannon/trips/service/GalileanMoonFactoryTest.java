package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GalileanMoonFactory.
 */
class GalileanMoonFactoryTest {

    @Nested
    @DisplayName("Moon Creation")
    class MoonCreationTests {

        @Test
        @DisplayName("should create all four Galilean moons when none exist")
        void shouldCreateAllFourMoonsWhenNoneExist() {
            ExoPlanet jupiter = createJupiter();
            List<String> existingNames = new ArrayList<>();

            List<ExoPlanet> moons = GalileanMoonFactory.createMoons(jupiter, existingNames);

            assertEquals(4, moons.size());
        }

        @Test
        @DisplayName("should create moons with correct names")
        void shouldCreateMoonsWithCorrectNames() {
            ExoPlanet jupiter = createJupiter();
            List<String> existingNames = new ArrayList<>();

            List<ExoPlanet> moons = GalileanMoonFactory.createMoons(jupiter, existingNames);

            List<String> moonNames = moons.stream().map(ExoPlanet::getName).toList();
            assertTrue(moonNames.contains("Io"));
            assertTrue(moonNames.contains("Europa"));
            assertTrue(moonNames.contains("Ganymede"));
            assertTrue(moonNames.contains("Callisto"));
        }

        @Test
        @DisplayName("should skip existing moons")
        void shouldSkipExistingMoons() {
            ExoPlanet jupiter = createJupiter();
            List<String> existingNames = List.of("io", "europa");

            List<ExoPlanet> moons = GalileanMoonFactory.createMoons(jupiter, existingNames);

            assertEquals(2, moons.size());
            List<String> moonNames = moons.stream().map(ExoPlanet::getName).toList();
            assertFalse(moonNames.contains("Io"));
            assertFalse(moonNames.contains("Europa"));
            assertTrue(moonNames.contains("Ganymede"));
            assertTrue(moonNames.contains("Callisto"));
        }

        @Test
        @DisplayName("should skip all moons if all exist")
        void shouldSkipAllMoonsIfAllExist() {
            ExoPlanet jupiter = createJupiter();
            List<String> existingNames = List.of("io", "europa", "ganymede", "callisto");

            List<ExoPlanet> moons = GalileanMoonFactory.createMoons(jupiter, existingNames);

            assertTrue(moons.isEmpty());
        }

        @Test
        @DisplayName("should return empty list for null jupiter")
        void shouldReturnEmptyListForNullJupiter() {
            List<String> existingNames = new ArrayList<>();

            List<ExoPlanet> moons = GalileanMoonFactory.createMoons(null, existingNames);

            assertTrue(moons.isEmpty());
        }

        @Test
        @DisplayName("should set moons with parent planet ID")
        void shouldSetMoonsWithParentPlanetId() {
            ExoPlanet jupiter = createJupiter();
            List<String> existingNames = new ArrayList<>();

            List<ExoPlanet> moons = GalileanMoonFactory.createMoons(jupiter, existingNames);

            for (ExoPlanet moon : moons) {
                assertEquals("jupiter-id", moon.getParentPlanetId());
            }
        }

        @Test
        @DisplayName("should set moons with solar system ID")
        void shouldSetMoonsWithSolarSystemId() {
            ExoPlanet jupiter = createJupiter();
            List<String> existingNames = new ArrayList<>();

            List<ExoPlanet> moons = GalileanMoonFactory.createMoons(jupiter, existingNames);

            for (ExoPlanet moon : moons) {
                assertEquals("solar-system-id", moon.getSolarSystemId());
            }
        }

        @Test
        @DisplayName("should mark all as moons")
        void shouldMarkAllAsMoons() {
            ExoPlanet jupiter = createJupiter();
            List<String> existingNames = new ArrayList<>();

            List<ExoPlanet> moons = GalileanMoonFactory.createMoons(jupiter, existingNames);

            for (ExoPlanet moon : moons) {
                assertTrue(moon.getIsMoon());
            }
        }

        @Test
        @DisplayName("should set unique IDs for each moon")
        void shouldSetUniqueIdsForEachMoon() {
            ExoPlanet jupiter = createJupiter();
            List<String> existingNames = new ArrayList<>();

            List<ExoPlanet> moons = GalileanMoonFactory.createMoons(jupiter, existingNames);

            List<String> ids = moons.stream().map(ExoPlanet::getId).toList();
            assertEquals(4, ids.stream().distinct().count());
            for (String id : ids) {
                assertNotNull(id);
                assertFalse(id.isBlank());
            }
        }

        @Test
        @DisplayName("should set host star ID from Jupiter")
        void shouldSetHostStarIdFromJupiter() {
            ExoPlanet jupiter = createJupiter();
            List<String> existingNames = new ArrayList<>();

            List<ExoPlanet> moons = GalileanMoonFactory.createMoons(jupiter, existingNames);

            for (ExoPlanet moon : moons) {
                assertEquals("host-star-id", moon.getHostStarId());
            }
        }

        @Test
        @DisplayName("should set star name from Jupiter")
        void shouldSetStarNameFromJupiter() {
            ExoPlanet jupiter = createJupiter();
            List<String> existingNames = new ArrayList<>();

            List<ExoPlanet> moons = GalileanMoonFactory.createMoons(jupiter, existingNames);

            for (ExoPlanet moon : moons) {
                assertEquals("Sol", moon.getStarName());
            }
        }
    }

    @Nested
    @DisplayName("Moon Physical Data")
    class MoonPhysicalDataTests {

        @Test
        @DisplayName("Io should have correct physical properties")
        void ioShouldHaveCorrectProperties() {
            ExoPlanet io = findMoonByName("Io");

            assertNotNull(io);
            assertEquals(0.002819, io.getSemiMajorAxis(), 0.000001);
            assertEquals(0.286, io.getRadius(), 0.001);
            assertEquals(0.015, io.getMass(), 0.001);
            assertEquals(1.769, io.getOrbitalPeriod(), 0.001);
            assertEquals(0.0041, io.getEccentricity(), 0.0001);
        }

        @Test
        @DisplayName("Europa should have correct physical properties")
        void europaShouldHaveCorrectProperties() {
            ExoPlanet europa = findMoonByName("Europa");

            assertNotNull(europa);
            assertEquals(0.004485, europa.getSemiMajorAxis(), 0.000001);
            assertEquals(0.245, europa.getRadius(), 0.001);
            assertEquals(0.008, europa.getMass(), 0.001);
            assertEquals(3.551, europa.getOrbitalPeriod(), 0.001);
            assertEquals(0.0094, europa.getEccentricity(), 0.0001);
        }

        @Test
        @DisplayName("Ganymede should have correct physical properties")
        void ganymedeShouldHaveCorrectProperties() {
            ExoPlanet ganymede = findMoonByName("Ganymede");

            assertNotNull(ganymede);
            assertEquals(0.007155, ganymede.getSemiMajorAxis(), 0.000001);
            assertEquals(0.413, ganymede.getRadius(), 0.001);
            assertEquals(0.025, ganymede.getMass(), 0.001);
            assertEquals(7.155, ganymede.getOrbitalPeriod(), 0.001);
            assertEquals(0.0011, ganymede.getEccentricity(), 0.0001);
        }

        @Test
        @DisplayName("Callisto should have correct physical properties")
        void callistoShouldHaveCorrectProperties() {
            ExoPlanet callisto = findMoonByName("Callisto");

            assertNotNull(callisto);
            assertEquals(0.012585, callisto.getSemiMajorAxis(), 0.000001);
            assertEquals(0.378, callisto.getRadius(), 0.001);
            assertEquals(0.018, callisto.getMass(), 0.001);
            assertEquals(16.689, callisto.getOrbitalPeriod(), 0.001);
            assertEquals(0.0074, callisto.getEccentricity(), 0.0001);
        }

        @Test
        @DisplayName("moons should have zero inclination")
        void moonsShouldHaveZeroInclination() {
            ExoPlanet jupiter = createJupiter();
            List<ExoPlanet> moons = GalileanMoonFactory.createMoons(jupiter, new ArrayList<>());

            for (ExoPlanet moon : moons) {
                assertEquals(0.0, moon.getInclination(), 0.001);
            }
        }

        @Test
        @DisplayName("moons should have confirmed status")
        void moonsShouldHaveConfirmedStatus() {
            ExoPlanet jupiter = createJupiter();
            List<ExoPlanet> moons = GalileanMoonFactory.createMoons(jupiter, new ArrayList<>());

            for (ExoPlanet moon : moons) {
                assertEquals("Confirmed", moon.getPlanetStatus());
            }
        }

        @Test
        @DisplayName("moons should have Known detection type")
        void moonsShouldHaveKnownDetectionType() {
            ExoPlanet jupiter = createJupiter();
            List<ExoPlanet> moons = GalileanMoonFactory.createMoons(jupiter, new ArrayList<>());

            for (ExoPlanet moon : moons) {
                assertEquals("Known", moon.getDetectionType());
            }
        }

        private ExoPlanet findMoonByName(String name) {
            ExoPlanet jupiter = createJupiter();
            List<ExoPlanet> moons = GalileanMoonFactory.createMoons(jupiter, new ArrayList<>());
            return moons.stream()
                    .filter(m -> m.getName().equals(name))
                    .findFirst()
                    .orElse(null);
        }
    }

    @Nested
    @DisplayName("Get Moon Names")
    class GetMoonNamesTests {

        @Test
        @DisplayName("should return all four moon names")
        void shouldReturnAllFourMoonNames() {
            String[] names = GalileanMoonFactory.getMoonNames();

            assertEquals(4, names.length);
        }

        @Test
        @DisplayName("should return correct moon names")
        void shouldReturnCorrectMoonNames() {
            String[] names = GalileanMoonFactory.getMoonNames();

            List<String> nameList = List.of(names);
            assertTrue(nameList.contains("Io"));
            assertTrue(nameList.contains("Europa"));
            assertTrue(nameList.contains("Ganymede"));
            assertTrue(nameList.contains("Callisto"));
        }
    }

    // Helper methods

    private ExoPlanet createJupiter() {
        ExoPlanet jupiter = new ExoPlanet();
        jupiter.setId("jupiter-id");
        jupiter.setName("Jupiter");
        jupiter.setSolarSystemId("solar-system-id");
        jupiter.setHostStarId("host-star-id");
        jupiter.setStarName("Sol");
        return jupiter;
    }
}
