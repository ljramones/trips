package com.teamgannon.trips.dialogs.search.model;

import com.teamgannon.trips.jpa.model.StarObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StarDistances {

    /**
     * the star object
     */
    public StarObject starObject;

    /**
     * the distance from the central star
     */
    public double distance;

}
