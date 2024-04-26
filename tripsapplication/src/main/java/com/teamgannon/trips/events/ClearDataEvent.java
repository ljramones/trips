package com.teamgannon.trips.events;

import org.springframework.context.ApplicationEvent;

public class ClearDataEvent extends ApplicationEvent {

    public ClearDataEvent(Object source) {
        super(source);
    }

}
