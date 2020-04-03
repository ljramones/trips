package com.teamgannon.trips.elasticsearch.repository;

import com.teamgannon.trips.elasticsearch.model.Star;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

/**
 * The repository for storing stars
 * <p>
 * Created by larrymitchell on 2017-01-25.
 */
public interface StarRepository extends PagingAndSortingRepository<Star, String> {

    ////////////  QUERIES FOR FINDING BY VARIOUS IDs  //////////////

    /**
     * find by the the star's ID in the Hipparcos catalog, if known
     *
     * @param hipparcosId id
     * @return the corresponding star
     */
    public Star findByHipparcosId(String hipparcosId);

    /**
     * find by stellar system id
     *
     * @param stellarId id
     * @return the corresponding star
     */
    public Star findByStellarSystemId(String stellarId);

    public Star findByCatalogId(long catalogId);

    public Star findByHenryDraperId(String henryDraperId);

    public Star findByHarvardRevisedId(String harvardRevisedId);

    public Star findByGlieseId(String glieseId);

    public Star findBySaoId(String saoId);

    public Star findBySimbadId(String simbadId);

    /**
     * find by the constellation name
     *
     * @param constellation the constellation name
     * @return the list of corresponding stars
     */
    public List<Star> findByConstellation(String constellation);

    /**
     * find a star that commons this search segment
     *
     * @param toSearch the string to search
     * @return the list of corresponding stars
     */
    public List<Star> findByCommonNameContains(String toSearch);

    ///////// DISTANCE QUERIES /////////////

    public List<Star> findByDistanceIsLessThanEqual(double distance);

    public List<Star> findByDistanceGreaterThanEqual(double distance);

    public List<Star> findByDistanceGreaterThanEqualAndDistanceLessThanEqual(double lower, double upper);

    ////////// POSITIONAL QUERIES //////////////

    public List<Star> findByXGreaterThanAndXLessThanAndYGreaterThanAndYLessThanAndZGreaterThanAndZLessThan(
            double xLower, double xUpper, double yLower, double yUpper, double zLower, double zUpper);


}
