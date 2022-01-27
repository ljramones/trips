package com.teamgannon.trips.dialogs.search.model;

import com.teamgannon.trips.jpa.model.StarObject;
import lombok.Data;

import java.util.List;

@Data
public class ConstellationStarResults {

    /**
     * true is at least one star found
     */
    private boolean found;

    /**
     * list of matching stars
     */
    private List<StarObject> starObjectList;

}
