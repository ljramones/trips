package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;

import java.util.UUID;

public interface DatabaseUpdater {

    void astrographicUpdate(UUID recordId, String notes);

    void astrographicUpdate(StarDisplayRecord record);

}
