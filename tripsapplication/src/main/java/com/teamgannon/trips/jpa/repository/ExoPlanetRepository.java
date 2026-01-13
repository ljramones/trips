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

}
