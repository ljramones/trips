package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.SolarSystem;
import com.teamgannon.trips.jpa.model.StarObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for SolarSystemRepository using Testcontainers.
 */
class SolarSystemRepositoryIntegrationTest extends BaseRepositoryIntegrationTest {

    @Autowired
    private SolarSystemRepository solarSystemRepository;

    @Autowired
    private StarObjectRepository starObjectRepository;

    private StarObject testStar;

    @BeforeEach
    void setUp() {
        testStar = createStar("Test Star", 0, 0, 0, 10);
        starObjectRepository.save(testStar);
        flushAndClear();
    }

    @AfterEach
    void tearDown() {
        solarSystemRepository.deleteAll();
        starObjectRepository.deleteAll();
    }

    @Nested
    @DisplayName("Basic CRUD Operations")
    class BasicCrudTests {

        @Test
        @DisplayName("should save and find solar system by ID")
        void shouldSaveAndFindById() {
            SolarSystem system = createSolarSystem("Alpha Centauri System", testStar.getId());
            system.setStarCount(3);
            system.setPlanetCount(1);
            solarSystemRepository.save(system);
            flushAndClear();

            var found = solarSystemRepository.findById(system.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getSystemName()).isEqualTo("Alpha Centauri System");
            assertThat(found.get().getStarCount()).isEqualTo(3);
            assertThat(found.get().getPlanetCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should find solar system by system name")
        void shouldFindBySystemName() {
            SolarSystem system = createSolarSystem("Sol System", testStar.getId());
            solarSystemRepository.save(system);
            flushAndClear();

            Optional<SolarSystem> found = solarSystemRepository.findBySystemName("Sol System");

            assertThat(found).isPresent();
            assertThat(found.get().getPrimaryStarId()).isEqualTo(testStar.getId());
        }

        @Test
        @DisplayName("should check if solar system exists by name")
        void shouldExistsBySystemName() {
            SolarSystem system = createSolarSystem("Kepler System", testStar.getId());
            solarSystemRepository.save(system);
            flushAndClear();

            assertThat(solarSystemRepository.existsBySystemName("Kepler System")).isTrue();
            assertThat(solarSystemRepository.existsBySystemName("NonExistent")).isFalse();
        }
    }

    @Nested
    @DisplayName("Primary Star Queries")
    class PrimaryStarQueryTests {

        @Test
        @DisplayName("should find solar system by primary star ID")
        void shouldFindByPrimaryStarId() {
            SolarSystem system = createSolarSystem("Test System", testStar.getId());
            solarSystemRepository.save(system);
            flushAndClear();

            Optional<SolarSystem> found = solarSystemRepository.findByPrimaryStarId(testStar.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getSystemName()).isEqualTo("Test System");
        }

        @Test
        @DisplayName("should return empty when no system for star ID")
        void shouldReturnEmptyWhenNoSystemForStarId() {
            Optional<SolarSystem> found = solarSystemRepository.findByPrimaryStarId("nonexistent-star-id");

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Habitable Zone Queries")
    class HabitableZoneQueryTests {

        @Test
        @DisplayName("should find systems with habitable zone planets")
        void shouldFindSystemsWithHabitableZonePlanets() {
            SolarSystem habitable1 = createSolarSystem("Habitable System 1", testStar.getId());
            habitable1.setHasHabitableZonePlanets(true);

            StarObject star2 = createStar("Star 2", 10, 10, 10, 20);
            starObjectRepository.save(star2);
            SolarSystem habitable2 = createSolarSystem("Habitable System 2", star2.getId());
            habitable2.setHasHabitableZonePlanets(true);

            StarObject star3 = createStar("Star 3", 20, 20, 20, 30);
            starObjectRepository.save(star3);
            SolarSystem notHabitable = createSolarSystem("Non-Habitable System", star3.getId());
            notHabitable.setHasHabitableZonePlanets(false);

            solarSystemRepository.saveAll(List.of(habitable1, habitable2, notHabitable));
            flushAndClear();

            List<SolarSystem> found = solarSystemRepository.findByHasHabitableZonePlanetsTrue();

            assertThat(found).hasSize(2);
            assertThat(found).extracting(SolarSystem::getSystemName)
                    .containsExactlyInAnyOrder("Habitable System 1", "Habitable System 2");
        }
    }

    @Nested
    @DisplayName("Aggregate Queries")
    class AggregateQueryTests {

        @Test
        @DisplayName("should count total solar systems")
        void shouldCountTotalSolarSystems() {
            solarSystemRepository.saveAll(List.of(
                    createSolarSystem("System 1", testStar.getId()),
                    createSolarSystem("System 2", testStar.getId()),
                    createSolarSystem("System 3", testStar.getId())
            ));
            flushAndClear();

            long count = solarSystemRepository.count();

            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("World Building Attributes")
    class WorldBuildingTests {

        @Test
        @DisplayName("should save and retrieve polity and colonization info")
        void shouldSaveAndRetrievePolityAndColonization() {
            SolarSystem system = createSolarSystem("Federation System", testStar.getId());
            system.setPolity("United Federation");
            system.setColonized(true);
            system.setTotalPopulation(1000000L);
            solarSystemRepository.save(system);
            flushAndClear();

            Optional<SolarSystem> found = solarSystemRepository.findBySystemName("Federation System");

            assertThat(found).isPresent();
            assertThat(found.get().getPolity()).isEqualTo("United Federation");
            assertThat(found.get().isColonized()).isTrue();
            assertThat(found.get().getTotalPopulation()).isEqualTo(1000000L);
        }
    }

    @Nested
    @DisplayName("Habitable Zone Calculations")
    class HabitableZoneTests {

        @Test
        @DisplayName("should save and retrieve habitable zone boundaries")
        void shouldSaveAndRetrieveHabitableZoneBoundaries() {
            SolarSystem system = createSolarSystem("Sol-like System", testStar.getId());
            system.setHabitableZoneInnerAU(0.95);
            system.setHabitableZoneOuterAU(1.67);
            system.setHasHabitableZonePlanets(true);
            solarSystemRepository.save(system);
            flushAndClear();

            Optional<SolarSystem> found = solarSystemRepository.findBySystemName("Sol-like System");

            assertThat(found).isPresent();
            assertThat(found.get().getHabitableZoneInnerAU()).isEqualTo(0.95);
            assertThat(found.get().getHabitableZoneOuterAU()).isEqualTo(1.67);
        }
    }

    @Nested
    @DisplayName("Multi-Star Systems")
    class MultiStarSystemTests {

        @Test
        @DisplayName("should handle binary star systems")
        void shouldHandleBinaryStarSystems() {
            SolarSystem system = createSolarSystem("Binary System", testStar.getId());
            system.setStarCount(2);
            solarSystemRepository.save(system);
            flushAndClear();

            Optional<SolarSystem> found = solarSystemRepository.findBySystemName("Binary System");

            assertThat(found).isPresent();
            assertThat(found.get().getStarCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("should handle triple star systems")
        void shouldHandleTripleStarSystems() {
            SolarSystem system = createSolarSystem("Alpha Centauri", testStar.getId());
            system.setStarCount(3);
            solarSystemRepository.save(system);
            flushAndClear();

            Optional<SolarSystem> found = solarSystemRepository.findBySystemName("Alpha Centauri");

            assertThat(found).isPresent();
            assertThat(found.get().getStarCount()).isEqualTo(3);
        }
    }
}
