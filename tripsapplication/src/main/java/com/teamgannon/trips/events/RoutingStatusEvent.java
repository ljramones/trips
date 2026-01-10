package com.teamgannon.trips.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event that indicates a change in routing status.
 */
@Getter
public class RoutingStatusEvent extends ApplicationEvent {

    private final boolean statusFlag;

    /**
     * Creates a new RoutingStatusEvent.
     *
     * @param source     The object that fired the event
     * @param statusFlag The routing status (true = active, false = inactive)
     */
    public RoutingStatusEvent(Object source, boolean statusFlag) {
        super(source);
        this.statusFlag = statusFlag;
    }

}
