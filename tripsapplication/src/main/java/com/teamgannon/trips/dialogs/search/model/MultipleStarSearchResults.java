package com.teamgannon.trips.dialogs.search.model;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class MultipleStarSearchResults {
    private boolean starsFound;

    private String dataSetName;

    private String nameToSearch;

    private List<StarDistances> starObjects = new ArrayList<>();
}
