package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.AstrographicObject;

import java.util.UUID;

public interface ListSelectorActionsListener {

    void recenter(StarDisplayRecord starDisplayRecord);

    void astrographicUpdate(AstrographicObject record);

    AstrographicObject getStar(UUID starId);

}
