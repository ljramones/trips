package com.teamgannon.trips.events;

import org.springframework.context.ApplicationEvent;

public class ClearListEvent extends ApplicationEvent {

    public ClearListEvent(Object source) {
        super(source);
    }
}
