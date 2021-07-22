package com.teamgannon.trips.dialogs.search.model;

import com.teamgannon.trips.jpa.model.StarObject;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StarSearchResults {

    private boolean starsFound;

    private String dataSetName;

    private String nameToSearch;

    private StarObject starObject;

}
