package com.teamgannon.trips.dialogs.search.model;


import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import lombok.Data;

@Data
public class DistanceBetweenStar {

    private StarDisplayRecord fromStar;

    private StarDisplayRecord toStar;

    private double distance;

    /**
     * if this combination already matches another pair in some combination a -> b or b-> a, then we don't
     * need to do it.
     * We won't measure a star to itself as this is always zero
     * Star A to Star B distance is the same as Star B to Star A
     * This logical operation checks each of the above cases
     * A response of true means skip
     *
     * @param a star a
     * @param b star b
     * @return true is this is the same combination
     */
    public boolean same(StarDisplayRecord a, StarDisplayRecord b) {
        return a.getRecordId().equals(b.getRecordId())
                || (a.getRecordId().equals(fromStar.getRecordId())
                || a.getRecordId().equals(toStar.getRecordId()))
                && (b.getRecordId().equals(fromStar.getRecordId())
                || b.getRecordId().equals(toStar.getRecordId()));
    }

}
