package com.teamgannon.trips.report.distance;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.util.StringConverter;

public class StarDisplayRecordConvertor  extends StringConverter<StarDisplayRecord> {


    public StarDisplayRecord fromString(String string) {
        // convert from a string to a myClass instance
        return new StarDisplayRecord();
    }

    public String toString(StarDisplayRecord starDisplayRecord) {
        return starDisplayRecord.getStarName();
    }
}
