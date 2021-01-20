package com.teamgannon.trips.dialogs.search.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StarSearchResults {

    private boolean starsFound;

    private String dataSetName;

    private String nameToSearch;

}
