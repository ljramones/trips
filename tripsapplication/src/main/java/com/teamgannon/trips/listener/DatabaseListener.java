package com.teamgannon.trips.listener;

import com.teamgannon.trips.jpa.model.AstrographicObject;

import java.util.List;
import java.util.UUID;

public interface DatabaseListener {

    /**
     * get the astro object from the db on new search query
     *
     * @return the list of objects
     */
    List<AstrographicObject> getAstrographicObjectsOnQuery();

    /**
     * update the star
     *
     * @param astrographicObject the star to update
     */
    void updateStar(AstrographicObject astrographicObject);

    /**
     * update the star notes field only
     *
     * @param recordId the id
     * @param notes    the fields field
     */
    void updateStar(UUID recordId, String notes);

    /**
     * get a star by UUID
     *
     * @param starId the id
     * @return the star
     */
    AstrographicObject getStar(UUID starId);

    /**
     * remove the specified star
     *
     * @param astrographicObject the star to remove
     */
    void removeStar(AstrographicObject astrographicObject);

    /**
     * remove by UUID
     *
     * @param id the id
     */
    void removeStar(UUID id);
}
