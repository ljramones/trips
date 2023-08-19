package com.teamgannon.trips.dialogs.search.model;

import com.teamgannon.trips.jpa.model.StarObject;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SingleStarSelection {

    /**
     * this tells us if a star is selected
     */
    private boolean selected;

    /**
     * this is the database representation of the star
     */
    private StarObject starObject;

}
