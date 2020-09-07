package com.teamgannon.trips.dialogs.search;


import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FindResults {

    private boolean selected;

    private StarDisplayRecord record;

}
