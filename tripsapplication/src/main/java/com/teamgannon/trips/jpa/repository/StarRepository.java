package com.teamgannon.trips.jpa.repository;

import com.teamgannon.trips.jpa.model.Star;
import org.jetbrains.annotations.NotNull;
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
    @NotNull Star findByHipparcosId(String hipparcosId);

    /**
     * find by stellar system id
     *
     * @param stellarId id
     * @return the corresponding star
     */
    @NotNull Star findByStellarSystemId(String stellarId);

    @NotNull Star findByCatalogId(long catalogId);

    @NotNull Star findByHenryDraperId(String henryDraperId);

    @NotNull Star findByHarvardRevisedId(String harvardRevisedId);

    @NotNull Star findByGlieseId(String glieseId);

    @NotNull Star findBySaoId(String saoId);

    @NotNull Star findBySimbadId(String simbadId);

    /**
     * find by the constellation name
     *
     * @param constellation the constellation name
     * @return the list of corresponding stars
     */
    @NotNull List<Star> findByConstellation(String constellation);

    /**
     * find a star that commons this search segment
     *
     * @param toSearch the string to search
     * @return the list of corresponding stars
     */
    @NotNull List<Star> findByCommonNameContains(String toSearch);

    ///////// DISTANCE QUERIES /////////////

    @NotNull List<Star> findByDistanceIsLessThanEqual(double distance);

    @NotNull List<Star> findByDistanceGreaterThanEqual(double distance);

    @NotNull List<Star> findByDistanceGreaterThanEqualAndDistanceLessThanEqual(double lower, double upper);

    ////////// POSITIONAL QUERIES //////////////

    @NotNull List<Star> findByXGreaterThanAndXLessThanAndYGreaterThanAndYLessThanAndZGreaterThanAndZLessThan(
            double xLower, double xUpper, double yLower, double yUpper, double zLower, double zUpper);


}
