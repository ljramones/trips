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

    @Nested
    @DisplayName("Composite Index Query Patterns")
    class CompositeIndexQueryTests {

        @Test
        @DisplayName("should efficiently query planets vs moons by solar system (uses idx_exoplanet_system_moon)")
        void shouldQueryPlanetsVsMoonsBySolarSystem() {
            // Create a complex system with planets and moons
            ExoPlanet planet1 = createExoPlanet("Planet 1", "Star", testSystem.getId(), testStar.getId());
            ExoPlanet planet2 = createExoPlanet("Planet 2", "Star", testSystem.getId(), testStar.getId());
            ExoPlanet planet3 = createExoPlanet("Planet 3", "Star", testSystem.getId(), testStar.getId());
            exoPlanetRepository.saveAll(List.of(planet1, planet2, planet3));
            flushAndClear();

            // Reload to get IDs
            planet1 = exoPlanetRepository.findByName("Planet 1");
            planet2 = exoPlanetRepository.findByName("Planet 2");

            // Add moons to planets
            ExoPlanet moon1a = createMoon("Moon 1a", planet1.getId(), testSystem.getId());
            ExoPlanet moon1b = createMoon("Moon 1b", planet1.getId(), testSystem.getId());
            ExoPlanet moon2a = createMoon("Moon 2a", planet2.getId(), testSystem.getId());
            exoPlanetRepository.saveAll(List.of(moon1a, moon1b, moon2a));
            flushAndClear();

            // Query should use composite index (solarSystemId, isMoon)
            List<ExoPlanet> planetsOnly = exoPlanetRepository.findPlanetsBySolarSystemId(testSystem.getId());
            List<ExoPlanet> allBodies = exoPlanetRepository.findBySolarSystemId(testSystem.getId());

            assertThat(planetsOnly).hasSize(3);
            assertThat(allBodies).hasSize(6); // 3 planets + 3 moons
            assertThat(planetsOnly).extracting(ExoPlanet::getIsMoon)
                    .allMatch(isMoon -> isMoon == null || !isMoon);
        }

        @Test
        @DisplayName("should efficiently query by solar system and planet status (uses idx_exoplanet_system_status)")
        void shouldQueryBySolarSystemAndStatus() {
            // Create planets with different statuses
            ExoPlanet confirmed1 = createExoPlanet("Confirmed 1", "Star", testSystem.getId(), testStar.getId());
            confirmed1.setPlanetStatus("Confirmed");

            ExoPlanet confirmed2 = createExoPlanet("Confirmed 2", "Star", testSystem.getId(), testStar.getId());
            confirmed2.setPlanetStatus("Confirmed");

            ExoPlanet candidate1 = createExoPlanet("Candidate 1", "Star", testSystem.getId(), testStar.getId());
            candidate1.setPlanetStatus("Candidate");

            ExoPlanet simulated1 = createExoPlanet("Simulated 1", "Star", testSystem.getId(), testStar.getId());
            simulated1.setPlanetStatus("Simulated");

            exoPlanetRepository.saveAll(List.of(confirmed1, confirmed2, candidate1, simulated1));
            flushAndClear();

            // Delete operation uses composite index (solarSystemId, planetStatus)
            exoPlanetRepository.deleteBySolarSystemIdAndPlanetStatus(testSystem.getId(), "Simulated");
            flushAndClear();

            List<ExoPlanet> remaining = exoPlanetRepository.findBySolarSystemId(testSystem.getId());
            assertThat(remaining).hasSize(3);
            assertThat(remaining).extracting(ExoPlanet::getPlanetStatus)
                    .containsOnly("Confirmed", "Candidate");
        }

        @Test
        @DisplayName("should efficiently query planets by host star (uses idx_exoplanet_host_moon)")
        void shouldQueryPlanetsByHostStar() {
            // Create a second star for binary system
            StarObject star2 = createStar("Star B", 0.5, 0.5, 0.5, 10);
            starObjectRepository.save(star2);
            flushAndClear();

            // Create planets orbiting different stars
            ExoPlanet planetA1 = createExoPlanet("Planet A1", "Star A", testSystem.getId(), testStar.getId());
            ExoPlanet planetA2 = createExoPlanet("Planet A2", "Star A", testSystem.getId(), testStar.getId());
            ExoPlanet planetB1 = createExoPlanet("Planet B1", "Star B", testSystem.getId(), star2.getId());

            exoPlanetRepository.saveAll(List.of(planetA1, planetA2, planetB1));
            flushAndClear();

            // Reload to get IDs
            planetA1 = exoPlanetRepository.findByName("Planet A1");

            // Add moon to planet A1
            ExoPlanet moonA1 = createMoon("Moon A1", planetA1.getId(), testSystem.getId());
            moonA1.setHostStarId(testStar.getId());
            exoPlanetRepository.save(moonA1);
            flushAndClear();

            // Query should use composite index (hostStarId, isMoon)
            long planetCountStarA = exoPlanetRepository.countPlanetsByHostStarId(testStar.getId());
            long totalCountStarA = exoPlanetRepository.countByHostStarId(testStar.getId());
            long planetCountStarB = exoPlanetRepository.countPlanetsByHostStarId(star2.getId());

            assertThat(planetCountStarA).isEqualTo(2); // Only planets, not moons
            assertThat(totalCountStarA).isEqualTo(3); // Planets + moon
            assertThat(planetCountStarB).isEqualTo(1);
        }

        @Test
        @DisplayName("should handle mixed queries across multiple systems")
        void shouldHandleMixedQueriesAcrossMultipleSystems() {
            // Create a second system
            StarObject star2 = createStar("Star 2", 10, 10, 10, 20);
            starObjectRepository.save(star2);
            SolarSystem system2 = createSolarSystem("System 2", star2.getId());
            solarSystemRepository.save(system2);
            flushAndClear();

            // Populate first system
            ExoPlanet s1p1 = createExoPlanet("S1 Planet 1", "Star 1", testSystem.getId(), testStar.getId());
            s1p1.setPlanetStatus("Confirmed");
            ExoPlanet s1p2 = createExoPlanet("S1 Planet 2", "Star 1", testSystem.getId(), testStar.getId());
            s1p2.setPlanetStatus("Candidate");
            exoPlanetRepository.saveAll(List.of(s1p1, s1p2));
            flushAndClear();

            s1p1 = exoPlanetRepository.findByName("S1 Planet 1");
            ExoPlanet s1m1 = createMoon("S1 Moon 1", s1p1.getId(), testSystem.getId());
            exoPlanetRepository.save(s1m1);

            // Populate second system
            ExoPlanet s2p1 = createExoPlanet("S2 Planet 1", "Star 2", system2.getId(), star2.getId());
            s2p1.setPlanetStatus("Confirmed");
            exoPlanetRepository.save(s2p1);
            flushAndClear();

            // Query patterns that benefit from composite indexes
            List<ExoPlanet> s1Planets = exoPlanetRepository.findPlanetsBySolarSystemId(testSystem.getId());
            List<ExoPlanet> s2Planets = exoPlanetRepository.findPlanetsBySolarSystemId(system2.getId());
            long s1Total = exoPlanetRepository.countBySolarSystemId(testSystem.getId());
            long s2Total = exoPlanetRepository.countBySolarSystemId(system2.getId());

            assertThat(s1Planets).hasSize(2); // 2 planets, excluding moon
            assertThat(s2Planets).hasSize(1);
            assertThat(s1Total).isEqualTo(3); // 2 planets + 1 moon
            assertThat(s2Total).isEqualTo(1);
        }

        @Test
        @DisplayName("should efficiently filter large system with many bodies")
        void shouldFilterLargeSystemWithManyBodies() {
            // Create a system like Jupiter with many moons
            ExoPlanet gasGiant = createExoPlanet("Gas Giant", "Star", testSystem.getId(), testStar.getId());
            gasGiant.setPlanetStatus("Confirmed");
            exoPlanetRepository.save(gasGiant);
            flushAndClear();

            gasGiant = exoPlanetRepository.findByName("Gas Giant");

            // Add many moons
            for (int i = 1; i <= 20; i++) {
                ExoPlanet moon = createMoon("Moon " + i, gasGiant.getId(), testSystem.getId());
                moon.setPlanetStatus("Confirmed");
                exoPlanetRepository.save(moon);
            }

            // Add some additional planets
            for (int i = 1; i <= 5; i++) {
                ExoPlanet planet = createExoPlanet("Planet " + i, "Star", testSystem.getId(), testStar.getId());
                planet.setPlanetStatus(i <= 3 ? "Confirmed" : "Candidate");
                exoPlanetRepository.save(planet);
            }
            flushAndClear();

            // These queries benefit from composite indexes
            List<ExoPlanet> planetsOnly = exoPlanetRepository.findPlanetsBySolarSystemId(testSystem.getId());
            long totalBodies = exoPlanetRepository.countBySolarSystemId(testSystem.getId());

            assertThat(planetsOnly).hasSize(6); // Gas Giant + 5 other planets
            assertThat(totalBodies).isEqualTo(26); // 6 planets + 20 moons
        }
    }
}
