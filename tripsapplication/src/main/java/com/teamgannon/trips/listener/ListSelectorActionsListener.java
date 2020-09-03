package com.teamgannon.trips.listener;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.AstrographicObject;

import java.util.UUID;

public interface ListSelectorActionsListener {

    void recenter(StarDisplayRecord starDisplayRecord);

}
