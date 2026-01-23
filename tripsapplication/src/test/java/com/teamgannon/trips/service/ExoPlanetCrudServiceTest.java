package com.teamgannon.trips.service;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.jpa.model.SolarSystem;
import com.teamgannon.trips.jpa.repository.ExoPlanetRepository;
import com.teamgannon.trips.jpa.repository.SolarSystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExoPlanetCrudService.
 */
@ExtendWith(MockitoExtension.class)
class ExoPlanetCrudServiceTest {

    @Mock
    private ExoPlanetRepository exoPlanetRepository;

    @Mock
    private SolarSystemRepository solarSystemRepository;

    private ExoPlanetCrudService service;

    @BeforeEach
    void setUp() {
        service = new ExoPlanetCrudService(exoPlanetRepository, solarSystemRepository);
    }

    @Nested
    @DisplayName("Find by Name")
    class FindByNameTests {

        @Test
        @DisplayName("should return planet when found")
        void shouldReturnPlanetWhenFound() {
            ExoPlanet expected = createExoPlanet("planet-1", "Kepler-442b");
            when(exoPlanetRepository.findByName("Kepler-442b")).thenReturn(expected);

            ExoPlanet result = service.findByName("Kepler-442b");

            assertEquals(expected, result);
            verify(exoPlanetRepository).findByName("Kepler-442b");
        }

        @Test
        @DisplayName("should return null when not found")
        void shouldReturnNullWhenNotFound() {
            when(exoPlanetRepository.findByName("NonExistent")).thenReturn(null);

            ExoPlanet result = service.findByName("NonExistent");

            assertNull(result);
        }

        @Test
        @DisplayName("should return null for null name")
        void shouldReturnNullForNullName() {
            ExoPlanet result = service.findByName(null);

            assertNull(result);
            verify(exoPlanetRepository, never()).findByName(any());
        }

        @Test
        @DisplayName("should return null for blank name")
        void shouldReturnNullForBlankName() {
            ExoPlanet result = service.findByName("  ");

            assertNull(result);
            verify(exoPlanetRepository, never()).findByName(any());
        }
    }

    @Nested
    @DisplayName("Find by ID")
    class FindByIdTests {

        @Test
        @DisplayName("should return planet when found")
        void shouldReturnPlanetWhenFound() {
            ExoPlanet expected = createExoPlanet("planet-1", "Kepler-442b");
            when(exoPlanetRepository.findById("planet-1")).thenReturn(Optional.of(expected));

            ExoPlanet result = service.findById("planet-1");

            assertEquals(expected, result);
        }

        @Test
        @DisplayName("should return null when not found")
        void shouldReturnNullWhenNotFound() {
            when(exoPlanetRepository.findById("nonexistent")).thenReturn(Optional.empty());

            ExoPlanet result = service.findById("nonexistent");

            assertNull(result);
        }

        @Test
        @DisplayName("should return null for null ID")
        void shouldReturnNullForNullId() {
            ExoPlanet result = service.findById(null);

            assertNull(result);
            verify(exoPlanetRepository, never()).findById(any());
        }

        @Test
        @DisplayName("should return null for blank ID")
        void shouldReturnNullForBlankId() {
            ExoPlanet result = service.findById("  ");

            assertNull(result);
            verify(exoPlanetRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("Find by Solar System ID")
    class FindBySolarSystemIdTests {

        @Test
        @DisplayName("should return list of planets")
        void shouldReturnListOfPlanets() {
            List<ExoPlanet> expected = List.of(
                    createExoPlanet("planet-1", "Planet b"),
                    createExoPlanet("planet-2", "Planet c")
            );
            when(exoPlanetRepository.findBySolarSystemId("solar-123")).thenReturn(expected);

            List<ExoPlanet> result = service.findBySolarSystemId("solar-123");

            assertEquals(2, result.size());
            verify(exoPlanetRepository).findBySolarSystemId("solar-123");
        }

        @Test
        @DisplayName("should return empty list when none found")
        void shouldReturnEmptyListWhenNoneFound() {
            when(exoPlanetRepository.findBySolarSystemId("solar-123")).thenReturn(new ArrayList<>());

            List<ExoPlanet> result = service.findBySolarSystemId("solar-123");

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Find Planets by Solar System ID")
    class FindPlanetsBySolarSystemIdTests {

        @Test
        @DisplayName("should return only planets, not moons")
        void shouldReturnOnlyPlanetsNotMoons() {
            List<ExoPlanet> expected = List.of(
                    createExoPlanet("planet-1", "Planet b")
            );
            when(exoPlanetRepository.findPlanetsBySolarSystemId("solar-123")).thenReturn(expected);

            List<ExoPlanet> result = service.findPlanetsBySolarSystemId("solar-123");

            assertEquals(1, result.size());
            verify(exoPlanetRepository).findPlanetsBySolarSystemId("solar-123");
        }
    }

    @Nested
    @DisplayName("Find Moons by Parent Planet ID")
    class FindMoonsByParentPlanetIdTests {

        @Test
        @DisplayName("should return moons for a planet")
        void shouldReturnMoonsForPlanet() {
            List<ExoPlanet> moons = List.of(
                    createMoon("moon-1", "Io"),
                    createMoon("moon-2", "Europa")
            );
            when(exoPlanetRepository.findByParentPlanetId("jupiter-id")).thenReturn(moons);

            List<ExoPlanet> result = service.findMoonsByParentPlanetId("jupiter-id");

            assertEquals(2, result.size());
            verify(exoPlanetRepository).findByParentPlanetId("jupiter-id");
        }
    }

    @Nested
    @DisplayName("Add Planet")
    class AddPlanetTests {

        @Test
        @DisplayName("should save planet and return it")
        void shouldSavePlanetAndReturnIt() {
            ExoPlanet planet = createExoPlanet(null, "New Planet");
            planet.setSolarSystemId("solar-123");

            when(exoPlanetRepository.save(any(ExoPlanet.class))).thenAnswer(inv -> inv.getArgument(0));
            when(solarSystemRepository.findById("solar-123")).thenReturn(Optional.empty());

            ExoPlanet result = service.add(planet);

            assertNotNull(result);
            assertNotNull(result.getId());
            verify(exoPlanetRepository).save(planet);
        }

        @Test
        @DisplayName("should generate ID if not provided")
        void shouldGenerateIdIfNotProvided() {
            ExoPlanet planet = createExoPlanet(null, "New Planet");
            when(exoPlanetRepository.save(any(ExoPlanet.class))).thenAnswer(inv -> inv.getArgument(0));

            ExoPlanet result = service.add(planet);

            assertNotNull(result.getId());
            assertFalse(result.getId().isBlank());
        }

        @Test
        @DisplayName("should keep existing ID if provided")
        void shouldKeepExistingIdIfProvided() {
            ExoPlanet planet = createExoPlanet("existing-id", "New Planet");
            when(exoPlanetRepository.save(any(ExoPlanet.class))).thenAnswer(inv -> inv.getArgument(0));

            ExoPlanet result = service.add(planet);

            assertEquals("existing-id", result.getId());
        }

        @Test
        @DisplayName("should return null for null planet")
        void shouldReturnNullForNullPlanet() {
            ExoPlanet result = service.add(null);

            assertNull(result);
            verify(exoPlanetRepository, never()).save(any());
        }

        @Test
        @DisplayName("should update solar system planet count after add")
        void shouldUpdateSolarSystemPlanetCountAfterAdd() {
            ExoPlanet planet = createExoPlanet(null, "New Planet");
            planet.setSolarSystemId("solar-123");
            planet.setIsMoon(false);

            SolarSystem solarSystem = new SolarSystem();
            solarSystem.setId("solar-123");
            solarSystem.setPlanetCount(0);

            when(exoPlanetRepository.save(any(ExoPlanet.class))).thenAnswer(inv -> inv.getArgument(0));
            when(solarSystemRepository.findById("solar-123")).thenReturn(Optional.of(solarSystem));
            when(exoPlanetRepository.findPlanetsBySolarSystemId("solar-123")).thenReturn(List.of(planet));

            service.add(planet);

            verify(solarSystemRepository).save(any(SolarSystem.class));
        }
    }

    @Nested
    @DisplayName("Update Planet")
    class UpdatePlanetTests {

        @Test
        @DisplayName("should save and return updated planet")
        void shouldSaveAndReturnUpdatedPlanet() {
            ExoPlanet planet = createExoPlanet("planet-1", "Updated Planet");
            planet.setSolarSystemId("solar-123");
            planet.setSemiMajorAxis(1.5);

            when(exoPlanetRepository.save(any(ExoPlanet.class))).thenAnswer(inv -> inv.getArgument(0));
            when(solarSystemRepository.findById("solar-123")).thenReturn(Optional.empty());

            ExoPlanet result = service.update(planet);

            assertNotNull(result);
            assertEquals("Updated Planet", result.getName());
            verify(exoPlanetRepository).save(planet);
        }

        @Test
        @DisplayName("should return null for null planet")
        void shouldReturnNullForNullPlanet() {
            ExoPlanet result = service.update(null);

            assertNull(result);
            verify(exoPlanetRepository, never()).save(any());
        }

        @Test
        @DisplayName("should return null for planet without ID")
        void shouldReturnNullForPlanetWithoutId() {
            ExoPlanet planet = new ExoPlanet();
            planet.setName("No ID");

            ExoPlanet result = service.update(planet);

            assertNull(result);
            verify(exoPlanetRepository, never()).save(any());
        }

        @Test
        @DisplayName("should update habitable zone status when semi-major axis changes")
        void shouldUpdateHabitableZoneStatusWhenSmaChanges() {
            ExoPlanet planet = createExoPlanet("planet-1", "Planet b");
            planet.setSolarSystemId("solar-123");
            planet.setSemiMajorAxis(1.0);

            SolarSystem solarSystem = new SolarSystem();
            solarSystem.setId("solar-123");
            solarSystem.setHabitableZoneInnerAU(0.8);
            solarSystem.setHabitableZoneOuterAU(1.5);

            when(exoPlanetRepository.save(any(ExoPlanet.class))).thenAnswer(inv -> inv.getArgument(0));
            when(solarSystemRepository.findById("solar-123")).thenReturn(Optional.of(solarSystem));
            when(exoPlanetRepository.findBySolarSystemId("solar-123")).thenReturn(List.of(planet));

            service.update(planet);

            verify(solarSystemRepository, atLeastOnce()).save(any(SolarSystem.class));
        }
    }

    @Nested
    @DisplayName("Delete Planet")
    class DeletePlanetTests {

        @Test
        @DisplayName("should delete planet by ID")
        void shouldDeletePlanetById() {
            ExoPlanet planet = createExoPlanet("planet-1", "To Delete");
            planet.setSolarSystemId("solar-123");
            when(exoPlanetRepository.findById("planet-1")).thenReturn(Optional.of(planet));
            when(solarSystemRepository.findById("solar-123")).thenReturn(Optional.empty());

            service.delete("planet-1");

            verify(exoPlanetRepository).deleteById("planet-1");
        }

        @Test
        @DisplayName("should not delete for null ID")
        void shouldNotDeleteForNullId() {
            service.delete(null);

            verify(exoPlanetRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("should not delete for blank ID")
        void shouldNotDeleteForBlankId() {
            service.delete("  ");

            verify(exoPlanetRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("should not delete if planet not found")
        void shouldNotDeleteIfPlanetNotFound() {
            when(exoPlanetRepository.findById("nonexistent")).thenReturn(Optional.empty());

            service.delete("nonexistent");

            verify(exoPlanetRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("should update solar system after delete")
        void shouldUpdateSolarSystemAfterDelete() {
            ExoPlanet planet = createExoPlanet("planet-1", "To Delete");
            planet.setSolarSystemId("solar-123");

            SolarSystem solarSystem = new SolarSystem();
            solarSystem.setId("solar-123");
            solarSystem.setPlanetCount(2);

            when(exoPlanetRepository.findById("planet-1")).thenReturn(Optional.of(planet));
            when(solarSystemRepository.findById("solar-123")).thenReturn(Optional.of(solarSystem));
            when(exoPlanetRepository.countBySolarSystemId("solar-123")).thenReturn(1L);

            service.delete("planet-1");

            ArgumentCaptor<SolarSystem> captor = ArgumentCaptor.forClass(SolarSystem.class);
            verify(solarSystemRepository).save(captor.capture());
            assertEquals(1, captor.getValue().getPlanetCount());
        }
    }

    @Nested
    @DisplayName("Count by Solar System ID")
    class CountBySolarSystemIdTests {

        @Test
        @DisplayName("should return count from repository")
        void shouldReturnCountFromRepository() {
            when(exoPlanetRepository.countBySolarSystemId("solar-123")).thenReturn(5L);

            long count = service.countBySolarSystemId("solar-123");

            assertEquals(5, count);
        }
    }

    @Nested
    @DisplayName("Find by Star Name")
    class FindByStarNameTests {

        @Test
        @DisplayName("should return planets for star name")
        void shouldReturnPlanetsForStarName() {
            List<ExoPlanet> expected = List.of(
                    createExoPlanet("planet-1", "Kepler-442 b")
            );
            when(exoPlanetRepository.findByStarName("Kepler-442")).thenReturn(expected);

            List<ExoPlanet> result = service.findByStarName("Kepler-442");

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("Find by RA and Dec Near")
    class FindByRaAndDecNearTests {

        @Test
        @DisplayName("should return planets near coordinates")
        void shouldReturnPlanetsNearCoordinates() {
            List<ExoPlanet> expected = List.of(
                    createExoPlanet("planet-1", "Nearby Planet")
            );
            when(exoPlanetRepository.findByRaDecNear(123.45, -45.67)).thenReturn(expected);

            List<ExoPlanet> result = service.findByRaDecNear(123.45, -45.67);

            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("Save and Delete Entity")
    class SaveAndDeleteEntityTests {

        @Test
        @DisplayName("should save entity directly")
        void shouldSaveEntityDirectly() {
            ExoPlanet planet = createExoPlanet("planet-1", "Direct Save");
            when(exoPlanetRepository.save(planet)).thenReturn(planet);

            ExoPlanet result = service.save(planet);

            assertEquals(planet, result);
            verify(exoPlanetRepository).save(planet);
        }

        @Test
        @DisplayName("should delete entity directly")
        void shouldDeleteEntityDirectly() {
            ExoPlanet planet = createExoPlanet("planet-1", "Direct Delete");

            service.deleteEntity(planet);

            verify(exoPlanetRepository).delete(planet);
        }
    }

    @Nested
    @DisplayName("Update Habitable Zone Planet Status")
    class UpdateHabitableZonePlanetStatusTests {

        @Test
        @DisplayName("should set hasHabitableZonePlanets to true when planet is in HZ")
        void shouldSetHasHZPlanetsToTrueWhenInHZ() {
            SolarSystem solarSystem = new SolarSystem();
            solarSystem.setId("solar-123");
            solarSystem.setHabitableZoneInnerAU(0.8);
            solarSystem.setHabitableZoneOuterAU(1.5);

            ExoPlanet planetInHZ = createExoPlanet("planet-1", "In HZ");
            planetInHZ.setSemiMajorAxis(1.0); // Within HZ

            when(exoPlanetRepository.findBySolarSystemId("solar-123")).thenReturn(List.of(planetInHZ));

            service.updateHabitableZonePlanetStatus(solarSystem);

            assertTrue(solarSystem.isHasHabitableZonePlanets());
            verify(solarSystemRepository).save(solarSystem);
        }

        @Test
        @DisplayName("should set hasHabitableZonePlanets to false when no planet is in HZ")
        void shouldSetHasHZPlanetsToFalseWhenNotInHZ() {
            SolarSystem solarSystem = new SolarSystem();
            solarSystem.setId("solar-123");
            solarSystem.setHabitableZoneInnerAU(0.8);
            solarSystem.setHabitableZoneOuterAU(1.5);

            ExoPlanet planetOutsideHZ = createExoPlanet("planet-1", "Outside HZ");
            planetOutsideHZ.setSemiMajorAxis(5.0); // Outside HZ

            when(exoPlanetRepository.findBySolarSystemId("solar-123")).thenReturn(List.of(planetOutsideHZ));

            service.updateHabitableZonePlanetStatus(solarSystem);

            assertFalse(solarSystem.isHasHabitableZonePlanets());
        }

        @Test
        @DisplayName("should not update if habitable zone is not defined")
        void shouldNotUpdateIfHZNotDefined() {
            SolarSystem solarSystem = new SolarSystem();
            solarSystem.setId("solar-123");
            solarSystem.setHabitableZoneInnerAU(null);
            solarSystem.setHabitableZoneOuterAU(null);

            service.updateHabitableZonePlanetStatus(solarSystem);

            verify(exoPlanetRepository, never()).findBySolarSystemId(any());
            verify(solarSystemRepository, never()).save(any());
        }
    }

    // Helper methods

    private ExoPlanet createExoPlanet(String id, String name) {
        ExoPlanet planet = new ExoPlanet();
        planet.setId(id);
        planet.setName(name);
        planet.setIsMoon(false);
        return planet;
    }

    private ExoPlanet createMoon(String id, String name) {
        ExoPlanet moon = new ExoPlanet();
        moon.setId(id);
        moon.setName(name);
        moon.setIsMoon(true);
        return moon;
    }
}
