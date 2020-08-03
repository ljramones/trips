package com.teamgannon.trips.search;

import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;

import java.util.List;

public interface StellarDataUpdater {

    /**
     * do a plot update based on new search query
     *
     * @param searchQuery the search query
     */
    void showNewStellarData(AstroSearchQuery searchQuery);

    /**
     * add the data set descriptor
     *
     * @param dataSetDescriptor the dataset descriptor
     */
    void addDataSet(DataSetDescriptor dataSetDescriptor);

    /**
     * remove the data set descriptor
     *
     * @param dataSetDescriptor the dataset descriptor
     */
    void removeDataSet(DataSetDescriptor dataSetDescriptor);

    /**
     * get the astro object from the db on new search query
     *
     * @return the list of objects
     */
    List<AstrographicObject> getAstrographicObjectsOnQuery();

    /**
     * add a star to the db
     *
     * @param astrographicObject the star
     */
    void addStar(AstrographicObject astrographicObject);

    /**
     * update the star
     *
     * @param astrographicObject the star to update
     */
    void updateStar(AstrographicObject astrographicObject);

    /**
     * remove the specified star
     *
     * @param astrographicObject the star to remove
     */
    void removeStar(AstrographicObject astrographicObject);

}
