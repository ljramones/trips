package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import lombok.Data;

@Data
public class StarEditStatus {

    private StarDisplayRecord record;

    private boolean changed = false;
}
