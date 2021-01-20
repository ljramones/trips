package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.jpa.model.StarObject;
import lombok.Data;

@Data
public class StarEditStatus {

    private StarObject record;

    private boolean changed = false;
}
