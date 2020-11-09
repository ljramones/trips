package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.jpa.model.AstrographicObject;
import lombok.Data;

@Data
public class StarEditStatus {

    private AstrographicObject record;

    private boolean changed = false;
}
