package com.teamgannon.trips.dialogs.utility.model;

import com.teamgannon.trips.jpa.model.StarObject;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StarSelectionObject {

    private boolean selected;

    private StarObject star;

}
