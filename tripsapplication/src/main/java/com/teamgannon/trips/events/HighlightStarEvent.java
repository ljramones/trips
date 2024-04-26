package com.teamgannon.trips.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class HighlightStarEvent extends ApplicationEvent {
    private final String recordId;

    /**
     * Represents an event that is triggered when a star is highlighted.
     */
    public HighlightStarEvent(Object source, String recordId) {
        super(source);
        this.recordId = recordId;
    }

}
