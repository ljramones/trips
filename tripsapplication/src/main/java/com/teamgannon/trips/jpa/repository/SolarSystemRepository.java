package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.SolarSystem;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for SolarSystem entities.
 * Provides methods to query solar systems by various criteria.
 */
public interface SolarSystemRepository extends PagingAndSortingRepository<SolarSystem, String> {

    /**
     * Find a solar system by its name
     *
     * @param systemName the system name
     * @return the solar system if found
     */
    Optional<SolarSystem> findBySystemName(String systemName);

    /**
     * Find a solar system by its primary star ID
     *
     * @param primaryStarId the primary star's ID
     * @return the solar system if found
     */
    Optional<SolarSystem> findByPrimaryStarId(String primaryStarId);

    /**
     * Find all solar systems in a dataset
     *
     * @param dataSetName the dataset name
     * @return list of solar systems
     */
    List<SolarSystem> findByDataSetName(String dataSetName);

    /**
     * Find all solar systems within a distance range from Sol
     *
     * @param minDistance minimum distance in light years
     * @param maxDistance maximum distance in light years
     * @return list of solar systems
     */
    @Query("SELECT s FROM SOLAR_SYSTEM s WHERE s.distanceFromSol BETWEEN :minDistance AND :maxDistance ORDER BY s.distanceFromSol ASC")
    List<SolarSystem> findByDistanceRange(@Param("minDistance") Double minDistance, @Param("maxDistance") Double maxDistance);

    /**
     * Find all solar systems that have planets
     *
     * @return list of solar systems with planets
     */
    @Query("SELECT s FROM SOLAR_SYSTEM s WHERE s.planetCount > 0")
    List<SolarSystem> findSystemsWithPlanets();

    /**
     * Find all solar systems that have habitable zone planets
     *
     * @return list of solar systems with habitable zone planets
     */
    List<SolarSystem> findByHasHabitableZonePlanetsTrue();

    /**
     * Find all multi-star systems (binary, trinary, etc.)
     *
     * @return list of multi-star systems
     */
    @Query("SELECT s FROM SOLAR_SYSTEM s WHERE s.starCount > 1")
    List<SolarSystem> findMultiStarSystems();

    /**
     * Find all solar systems controlled by a specific polity
     *
     * @param polity the polity name
     * @return list of solar systems
     */
    List<SolarSystem> findByPolity(String polity);

    /**
     * Find all colonized solar systems
     *
     * @return list of colonized systems
     */
    List<SolarSystem> findByColonizedTrue();

    /**
     * Check if a solar system exists by name
     *
     * @param systemName the system name
     * @return true if exists
     */
    boolean existsBySystemName(String systemName);

    /**
     * Check if a solar system exists for a given primary star
     *
     * @param primaryStarId the primary star's ID
     * @return true if exists
     */
    boolean existsByPrimaryStarId(String primaryStarId);

    /**
     * Count solar systems in a dataset
     *
     * @param dataSetName the dataset name
     * @return count of solar systems
     */
    long countByDataSetName(String dataSetName);

    /**
     * Find systems by name pattern (case-insensitive)
     *
     * @param pattern the name pattern (use % for wildcards)
     * @return list of matching solar systems
     */
    @Query("SELECT s FROM SOLAR_SYSTEM s WHERE LOWER(s.systemName) LIKE LOWER(:pattern)")
    List<SolarSystem> findBySystemNameLike(@Param("pattern") String pattern);

}
