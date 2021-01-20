package com.teamgannon.trips.listener;

import com.teamgannon.trips.jpa.model.StarObject;

import java.util.List;
import java.util.UUID;

public interface DatabaseListener {

    /**
     * get the astro object from the db on new search query
     *
     * @return the list of objects
     */
    List<StarObject> getAstrographicObjectsOnQuery();

    /**
     * update the star
     *
     * @param starObject the star to update
     */
    void updateStar(StarObject starObject);

    /**
     * update the star notes field only
     *
     * @param recordId the id
     * @param notes    the fields field
     */
    void updateNotesForStar(UUID recordId, String notes);

    /**
     * get a star by UUID
     *
     * @param starId the id
     * @return the star
     */
    StarObject getStar(UUID starId);

    /**
     * remove the specified star
     *
     * @param starObject the star to remove
     */
    void removeStar(StarObject starObject);

    /**
     * remove by UUID
     *
     * @param id the id
     */
    void removeStar(UUID id);
}
