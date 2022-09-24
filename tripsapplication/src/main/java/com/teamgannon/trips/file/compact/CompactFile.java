package com.teamgannon.trips.file.compact;

import com.teamgannon.trips.dialogs.dataset.model.Dataset;
import com.teamgannon.trips.jpa.model.StarObject;
import lombok.Data;

@Data
public class CompactFile {

    private Dataset dataset;

    private long numberOfStars;

    private double distanceRange;


    /**
     * update the distance range for this star
     *
     * @param starObject the star to update
     */
    public void updateDistance(StarObject starObject) {
        if (starObject.getDistance() > distanceRange) {
            distanceRange = starObject.getDistance();
        }
    }
}
