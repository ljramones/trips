package com.teamgannon.trips.events;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UpdateSidePanelListEvent extends ApplicationEvent {
    private final StarDisplayRecord starDisplayRecord;

    public UpdateSidePanelListEvent(Object source, StarDisplayRecord starDisplayRecord) {
        super(source);
        this.starDisplayRecord = starDisplayRecord;
    }
}
