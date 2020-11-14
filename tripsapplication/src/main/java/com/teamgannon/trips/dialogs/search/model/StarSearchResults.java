package com.teamgannon.trips.dialogs.search.model;

import com.teamgannon.trips.jpa.model.AstrographicObject;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class StarSearchResults {

    private boolean starsFound;

    private String dataSetName;

    private String nameToSearch;

}
