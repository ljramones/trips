package com.teamgannon.trips.report.distance;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.util.StringConverter;
import org.jetbrains.annotations.NotNull;

public class StarDisplayRecordConvertor extends StringConverter<StarDisplayRecord> {


    public @NotNull StarDisplayRecord fromString(String string) {
        // convert from a string to a myClass instance
        return new StarDisplayRecord();
    }

    public String toString(@NotNull StarDisplayRecord starDisplayRecord) {
        return starDisplayRecord.getStarName();
    }
}
