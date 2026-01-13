package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExoPlanetRepository extends PagingAndSortingRepository<ExoPlanet, String> {

    ExoPlanet findByName(String name);

    List<ExoPlanet> findByStarName(String starName);

    boolean existsByName(String name);

    @Query("SELECT s FROM EXOPLANET s WHERE s.ra BETWEEN :ra - 0.01 AND :ra + 0.01 AND s.dec BETWEEN :dec - 0.01 AND :dec + 0.01")
    List<ExoPlanet> findByRaAndDecNear(@Param("ra") Double ra, @Param("dec") Double dec);

    /**
     * Find all exoplanets in a solar system
     *
     * @param solarSystemId the solar system ID
     * @return list of exoplanets
     */
    List<ExoPlanet> findBySolarSystemId(String solarSystemId);

    /**
     * Find all exoplanets orbiting a specific star
     *
     * @param hostStarId the host star's ID
     * @return list of exoplanets
     */
    List<ExoPlanet> findByHostStarId(String hostStarId);

    /**
     * Count exoplanets in a solar system
     *
     * @param solarSystemId the solar system ID
     * @return count of exoplanets
     */
    long countBySolarSystemId(String solarSystemId);

    /**
     * Count exoplanets orbiting a specific star
     *
     * @param hostStarId the host star's ID
     * @return count of exoplanets
     */
    long countByHostStarId(String hostStarId);

    /**
     * Find all moons orbiting a specific planet
     *
     * @param parentPlanetId the parent planet's ID
     * @return list of moons
     */
    List<ExoPlanet> findByParentPlanetId(String parentPlanetId);

    /**
     * Count moons orbiting a specific planet
     *
     * @param parentPlanetId the parent planet's ID
     * @return count of moons
     */
    long countByParentPlanetId(String parentPlanetId);

    /**
     * Find only planets (not moons) in a solar system
     *
     * @param solarSystemId the solar system ID
     * @return list of planets (excluding moons)
     */
    List<ExoPlanet> findBySolarSystemIdAndIsMoonFalse(String solarSystemId);

    /**
     * Find only planets (not moons) in a solar system, including those with null isMoon
     *
     * @param solarSystemId the solar system ID
     * @return list of planets
     */
    @Query("SELECT e FROM EXOPLANET e WHERE e.solarSystemId = :solarSystemId AND (e.isMoon IS NULL OR e.isMoon = false)")
    List<ExoPlanet> findPlanetsBySolarSystemId(@Param("solarSystemId") String solarSystemId);

    /**
     * Delete all exoplanets (and moons) for a solar system with a specific status
     *
     * @param solarSystemId the solar system ID
     * @param planetStatus the status (e.g., "Simulated")
     */
    void deleteBySolarSystemIdAndPlanetStatus(String solarSystemId, String planetStatus);

}
