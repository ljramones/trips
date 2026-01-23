package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.jpa.model.SolarSystem;
import com.teamgannon.trips.jpa.model.StarObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for ExoPlanetRepository using Testcontainers.
 */
class ExoPlanetRepositoryIntegrationTest extends BaseRepositoryIntegrationTest {

    @Autowired
    private ExoPlanetRepository exoPlanetRepository;

    @Autowired
    private SolarSystemRepository solarSystemRepository;

    @Autowired
    private StarObjectRepository starObjectRepository;

    private SolarSystem testSystem;
    private StarObject testStar;

    @BeforeEach
    void setUp() {
        // Create test star
        testStar = createStar("Test Star", 0, 0, 0, 10);
        starObjectRepository.save(testStar);

        // Create test solar system
        testSystem = createSolarSystem("Test System", testStar.getId());
        solarSystemRepository.save(testSystem);

        // Link star to system
        testStar.setSolarSystemId(testSystem.getId());
        starObjectRepository.save(testStar);

        flushAndClear();
    }

    @AfterEach
    void tearDown() {
        exoPlanetRepository.deleteAll();
        starObjectRepository.deleteAll();
        solarSystemRepository.deleteAll();
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudTests {

        @Test
        @DisplayName("should save and find exoplanet by ID")
        void shouldSaveAndFindById() {
            ExoPlanet planet = createExoPlanet("Kepler-442b", "Kepler-442", testSystem.getId(), testStar.getId());
            exoPlanetRepository.save(planet);
            flushAndClear();

            var found = exoPlanetRepository.findById(planet.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Kepler-442b");
        }

        @Test
        @DisplayName("should find exoplanet by name")
        void shouldFindByName() {
            ExoPlanet planet = createExoPlanet("Proxima b", "Proxima Centauri", testSystem.getId(), testStar.getId());
            exoPlanetRepository.save(planet);
            flushAndClear();

            ExoPlanet found = exoPlanetRepository.findByName("Proxima b");

            assertThat(found).isNotNull();
            assertThat(found.getStarName()).isEqualTo("Proxima Centauri");
        }

        @Test
        @DisplayName("should check if exoplanet exists by name")
        void shouldExistsByName() {
            ExoPlanet planet = createExoPlanet("TRAPPIST-1e", "TRAPPIST-1", testSystem.getId(), testStar.getId());
            exoPlanetRepository.save(planet);
            flushAndClear();

            assertThat(exoPlanetRepository.existsByName("TRAPPIST-1e")).isTrue();
            assertThat(exoPlanetRepository.existsByName("NonExistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("Star Name Queries")
    class StarNameQueryTests {

        @Test
        @DisplayName("should find exoplanets by star name")
        void shouldFindByStarName() {
            exoPlanetRepository.saveAll(List.of(
                    createExoPlanet("TRAPPIST-1b", "TRAPPIST-1", testSystem.getId(), testStar.getId()),
                    createExoPlanet("TRAPPIST-1c", "TRAPPIST-1", testSystem.getId(), testStar.getId()),
                    createExoPlanet("Proxima b", "Proxima Centauri", testSystem.getId(), testStar.getId())
            ));
            flushAndClear();

            List<ExoPlanet> found = exoPlanetRepository.findByStarName("TRAPPIST-1");

            assertThat(found).hasSize(2);
            assertThat(found).extracting(ExoPlanet::getName)
                    .containsExactlyInAnyOrder("TRAPPIST-1b", "TRAPPIST-1c");
        }

        @Test
        @DisplayName("should find distinct star names")
        void shouldFindDistinctStarNames() {
            exoPlanetRepository.saveAll(List.of(
                    createExoPlanet("TRAPPIST-1b", "TRAPPIST-1", testSystem.getId(), testStar.getId()),
                    createExoPlanet("TRAPPIST-1c", "TRAPPIST-1", testSystem.getId(), testStar.getId()),
                    createExoPlanet("Proxima b", "Proxima Centauri", testSystem.getId(), testStar.getId())
            ));
            flushAndClear();

            List<String> starNames = exoPlanetRepository.findDistinctStarNames();

            assertThat(starNames).hasSize(2);
            assertThat(starNames).containsExactlyInAnyOrder("TRAPPIST-1", "Proxima Centauri");
        }

        @Test
        @DisplayName("should count planets by star name")
        void shouldCountPlanetsByStarName() {
            exoPlanetRepository.saveAll(List.of(
                    createExoPlanet("TRAPPIST-1b", "TRAPPIST-1", testSystem.getId(), testStar.getId()),
                    createExoPlanet("TRAPPIST-1c", "TRAPPIST-1", testSystem.getId(), testStar.getId()),
                    createExoPlanet("TRAPPIST-1d", "TRAPPIST-1", testSystem.getId(), testStar.getId())
            ));
            flushAndClear();

            long count = exoPlanetRepository.countPlanetsByStarName("TRAPPIST-1");

            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Solar System Queries")
    class SolarSystemQueryTests {

        @Test
        @DisplayName("should find exoplanets by solar system ID")
        void shouldFindBySolarSystemId() {
            exoPlanetRepository.saveAll(List.of(
                    createExoPlanet("Planet A", "Star A", testSystem.getId(), testStar.getId()),
                    createExoPlanet("Planet B", "Star A", testSystem.getId(), testStar.getId())
            ));
            flushAndClear();

            List<ExoPlanet> found = exoPlanetRepository.findBySolarSystemId(testSystem.getId());

            assertThat(found).hasSize(2);
        }

        @Test
        @DisplayName("should count exoplanets by solar system ID")
        void shouldCountBySolarSystemId() {
            exoPlanetRepository.saveAll(List.of(
                    createExoPlanet("Planet A", "Star A", testSystem.getId(), testStar.getId()),
                    createExoPlanet("Planet B", "Star A", testSystem.getId(), testStar.getId()),
                    createExoPlanet("Planet C", "Star A", testSystem.getId(), testStar.getId())
            ));
            flushAndClear();

            long count = exoPlanetRepository.countBySolarSystemId(testSystem.getId());

            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("should find only planets (not moons) in solar system")
        void shouldFindPlanetsBySolarSystemId() {
            ExoPlanet planet = createExoPlanet("Planet A", "Star A", testSystem.getId(), testStar.getId());
            ExoPlanet moon = createMoon("Moon A", planet.getId(), testSystem.getId());
            exoPlanetRepository.saveAll(List.of(planet, moon));
            flushAndClear();

            List<ExoPlanet> planets = exoPlanetRepository.findPlanetsBySolarSystemId(testSystem.getId());

            assertThat(planets).hasSize(1);
            assertThat(planets.get(0).getName()).isEqualTo("Planet A");
        }
    }

    @Nested
    @DisplayName("Host Star Queries")
    class HostStarQueryTests {

        @Test
        @DisplayName("should find exoplanets by host star ID")
        void shouldFindByHostStarId() {
            exoPlanetRepository.saveAll(List.of(
                    createExoPlanet("Planet A", "Star", testSystem.getId(), testStar.getId()),
                    createExoPlanet("Planet B", "Star", testSystem.getId(), testStar.getId())
            ));
            flushAndClear();

            List<ExoPlanet> found = exoPlanetRepository.findByHostStarId(testStar.getId());

            assertThat(found).hasSize(2);
        }

        @Test
        @DisplayName("should count exoplanets by host star ID")
        void shouldCountByHostStarId() {
            exoPlanetRepository.saveAll(List.of(
                    createExoPlanet("Planet A", "Star", testSystem.getId(), testStar.getId()),
                    createExoPlanet("Planet B", "Star", testSystem.getId(), testStar.getId())
            ));
            flushAndClear();

            long count = exoPlanetRepository.countByHostStarId(testStar.getId());

            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("should count only planets by host star ID")
        void shouldCountPlanetsByHostStarId() {
            ExoPlanet planet = createExoPlanet("Planet", "Star", testSystem.getId(), testStar.getId());
            ExoPlanet moon = createMoon("Moon", planet.getId(), testSystem.getId());
            moon.setHostStarId(testStar.getId()); // Moon also has host star
            exoPlanetRepository.saveAll(List.of(planet, moon));
            flushAndClear();

            long count = exoPlanetRepository.countPlanetsByHostStarId(testStar.getId());

            assertThat(count).isEqualTo(1); // Only planet, not moon
        }

        @Test
        @DisplayName("should find distinct host star IDs")
        void shouldFindDistinctHostStarIds() {
            StarObject star2 = createStar("Star 2", 10, 10, 10, 20);
            starObjectRepository.save(star2);

            exoPlanetRepository.saveAll(List.of(
                    createExoPlanet("Planet A", "Star 1", testSystem.getId(), testStar.getId()),
                    createExoPlanet("Planet B", "Star 1", testSystem.getId(), testStar.getId()),
                    createExoPlanet("Planet C", "Star 2", testSystem.getId(), star2.getId())
            ));
            flushAndClear();

            List<String> hostStarIds = exoPlanetRepository.findDistinctHostStarIds();

            assertThat(hostStarIds).hasSize(2);
            assertThat(hostStarIds).containsExactlyInAnyOrder(testStar.getId(), star2.getId());
        }
    }

    @Nested
    @DisplayName("Moon Queries")
    class MoonQueryTests {

        @Test
        @DisplayName("should find moons by parent planet ID")
        void shouldFindByParentPlanetId() {
            ExoPlanet planet = createExoPlanet("Jupiter-like", "Star", testSystem.getId(), testStar.getId());
            exoPlanetRepository.save(planet);

            ExoPlanet moon1 = createMoon("Moon 1", planet.getId(), testSystem.getId());
            ExoPlanet moon2 = createMoon("Moon 2", planet.getId(), testSystem.getId());
            exoPlanetRepository.saveAll(List.of(moon1, moon2));
            flushAndClear();

            List<ExoPlanet> moons = exoPlanetRepository.findByParentPlanetId(planet.getId());

            assertThat(moons).hasSize(2);
            assertThat(moons).extracting(ExoPlanet::getName)
                    .containsExactlyInAnyOrder("Moon 1", "Moon 2");
        }

        @Test
        @DisplayName("should count moons by parent planet ID")
        void shouldCountByParentPlanetId() {
            ExoPlanet planet = createExoPlanet("Jupiter-like", "Star", testSystem.getId(), testStar.getId());
            exoPlanetRepository.save(planet);

            exoPlanetRepository.saveAll(List.of(
                    createMoon("Moon 1", planet.getId(), testSystem.getId()),
                    createMoon("Moon 2", planet.getId(), testSystem.getId()),
                    createMoon("Moon 3", planet.getId(), testSystem.getId())
            ));
            flushAndClear();

            long count = exoPlanetRepository.countByParentPlanetId(planet.getId());

            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("RA/Dec Queries")
    class RaDecQueryTests {

        @Test
        @DisplayName("should find exoplanets near RA/Dec coordinates")
        void shouldFindByRaDecNear() {
            ExoPlanet planet = createExoPlanet("Target Planet", "Star", testSystem.getId(), testStar.getId());
            planet.setRa(120.5);
            planet.setDec(-30.2);
            exoPlanetRepository.save(planet);
            flushAndClear();

            List<ExoPlanet> found = exoPlanetRepository.findByRaDecNear(120.505, -30.195);

            assertThat(found).hasSize(1);
            assertThat(found.get(0).getName()).isEqualTo("Target Planet");
        }

        @Test
        @DisplayName("should not find exoplanets outside tolerance")
        void shouldNotFindOutsideTolerance() {
            ExoPlanet planet = createExoPlanet("Target Planet", "Star", testSystem.getId(), testStar.getId());
            planet.setRa(120.5);
            planet.setDec(-30.2);
            exoPlanetRepository.save(planet);
            flushAndClear();

            List<ExoPlanet> found = exoPlanetRepository.findByRaDecNear(121.0, -30.0);

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperationTests {

        @Test
        @DisplayName("should delete by solar system ID and planet status")
        void shouldDeleteBySolarSystemIdAndPlanetStatus() {
            ExoPlanet confirmed = createExoPlanet("Confirmed Planet", "Star", testSystem.getId(), testStar.getId());
            confirmed.setPlanetStatus("Confirmed");

            ExoPlanet simulated = createExoPlanet("Simulated Planet", "Star", testSystem.getId(), testStar.getId());
            simulated.setPlanetStatus("Simulated");

            exoPlanetRepository.saveAll(List.of(confirmed, simulated));
            flushAndClear();

            exoPlanetRepository.deleteBySolarSystemIdAndPlanetStatus(testSystem.getId(), "Simulated");
            flushAndClear();

            List<ExoPlanet> remaining = exoPlanetRepository.findBySolarSystemId(testSystem.getId());
            assertThat(remaining).hasSize(1);
            assertThat(remaining.get(0).getPlanetStatus()).isEqualTo("Confirmed");
        }
    }
}
