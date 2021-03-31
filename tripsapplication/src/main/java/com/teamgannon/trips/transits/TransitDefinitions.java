package com.teamgannon.trips.transits;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class TransitDefinitions {

    private  boolean selected;

    /**
     * the data set name
     */
    private String dataSetName;

    /**
     * the list of datasets we want
     */
    private List<TransitRangeDef> transitRangeDefs = new ArrayList<>();

}
