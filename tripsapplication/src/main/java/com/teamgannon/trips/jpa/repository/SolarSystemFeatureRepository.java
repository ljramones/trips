package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.SolarSystemFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for SolarSystemFeature entities.
 */
@Repository
public interface SolarSystemFeatureRepository extends JpaRepository<SolarSystemFeature, String> {

    /**
     * Find all features belonging to a solar system.
     */
    List<SolarSystemFeature> findBySolarSystemId(String solarSystemId);

    /**
     * Find all features of a specific type in a solar system.
     */
    List<SolarSystemFeature> findBySolarSystemIdAndFeatureType(String solarSystemId, String featureType);

    /**
     * Find all features of a specific category (NATURAL or ARTIFICIAL) in a solar system.
     */
    List<SolarSystemFeature> findBySolarSystemIdAndFeatureCategory(String solarSystemId, String featureCategory);

    /**
     * Find all natural features in a solar system.
     */
    @Query("SELECT f FROM SOLAR_SYSTEM_FEATURE f WHERE f.solarSystemId = :solarSystemId AND f.featureCategory = 'NATURAL'")
    List<SolarSystemFeature> findNaturalFeatures(@Param("solarSystemId") String solarSystemId);

    /**
     * Find all artificial features in a solar system.
     */
    @Query("SELECT f FROM SOLAR_SYSTEM_FEATURE f WHERE f.solarSystemId = :solarSystemId AND f.featureCategory = 'ARTIFICIAL'")
    List<SolarSystemFeature> findArtificialFeatures(@Param("solarSystemId") String solarSystemId);

    /**
     * Find all belt-type features (asteroid belts, debris disks, etc.) in a solar system.
     */
    @Query("SELECT f FROM SOLAR_SYSTEM_FEATURE f WHERE f.solarSystemId = :solarSystemId AND f.featureType IN " +
            "('ASTEROID_BELT', 'KUIPER_BELT', 'DEBRIS_DISK', 'OORT_CLOUD', 'ZODIACAL_DUST', 'DYSON_SWARM', 'DEFENSE_PERIMETER', 'SENSOR_NETWORK')")
    List<SolarSystemFeature> findBeltFeatures(@Param("solarSystemId") String solarSystemId);

    /**
     * Find all point-type features (stations, gates, etc.) in a solar system.
     */
    @Query("SELECT f FROM SOLAR_SYSTEM_FEATURE f WHERE f.solarSystemId = :solarSystemId AND f.featureType IN " +
            "('ORBITAL_HABITAT', 'JUMP_GATE', 'SHIPYARD', 'RESEARCH_STATION', 'MINING_OPERATION', 'TROJAN_CLUSTER')")
    List<SolarSystemFeature> findPointFeatures(@Param("solarSystemId") String solarSystemId);

    /**
     * Find all features controlled by a specific polity.
     */
    List<SolarSystemFeature> findByControllingPolity(String polity);

    /**
     * Find all features associated with a specific planet (e.g., Trojans, orbital stations).
     */
    List<SolarSystemFeature> findByAssociatedPlanetId(String planetId);

    /**
     * Find all navigation hazards in a solar system.
     */
    @Query("SELECT f FROM SOLAR_SYSTEM_FEATURE f WHERE f.solarSystemId = :solarSystemId AND f.navigationHazard = true")
    List<SolarSystemFeature> findNavigationHazards(@Param("solarSystemId") String solarSystemId);

    /**
     * Find all jump gates in a solar system.
     */
    @Query("SELECT f FROM SOLAR_SYSTEM_FEATURE f WHERE f.solarSystemId = :solarSystemId AND f.featureType = 'JUMP_GATE'")
    List<SolarSystemFeature> findJumpGates(@Param("solarSystemId") String solarSystemId);

    /**
     * Delete all features belonging to a solar system.
     */
    void deleteBySolarSystemId(String solarSystemId);

    /**
     * Count features in a solar system.
     */
    long countBySolarSystemId(String solarSystemId);

    /**
     * Count features by type in a solar system.
     */
    long countBySolarSystemIdAndFeatureType(String solarSystemId, String featureType);
}
