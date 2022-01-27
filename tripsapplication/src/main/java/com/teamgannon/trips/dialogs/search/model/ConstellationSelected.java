package com.teamgannon.trips.dialogs.search.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConstellationSelected {

    private boolean selected;

    private String constellation;

}
