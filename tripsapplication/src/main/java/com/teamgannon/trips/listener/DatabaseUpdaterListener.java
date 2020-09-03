package com.teamgannon.trips.listener;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.AstrographicObject;

import java.util.UUID;

public interface DatabaseUpdaterListener {

    void astrographicUpdate(UUID recordId, String notes);

    void astrographicUpdate(AstrographicObject record);

    AstrographicObject getStar(UUID starId);

    void removeStar(UUID fromString);
}
