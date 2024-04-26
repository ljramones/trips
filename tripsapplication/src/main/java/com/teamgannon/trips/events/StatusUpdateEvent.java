package com.teamgannon.trips.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;


/**
 * Represents an event that indicates a status update.
 * Extends the ApplicationEvent class.
 */
@Getter
public class StatusUpdateEvent extends ApplicationEvent {

    private final String status;

    /**
     * Creates a new StatusUpdateEvent object.
     *
     * @param source The object that fired the event.
     * @param status The status message to be included in the event.
     */
    public StatusUpdateEvent(Object source, String status) {
        super(source);
        this.status = status;
    }

}
