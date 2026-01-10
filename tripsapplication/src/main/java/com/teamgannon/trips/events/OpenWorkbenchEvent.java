package com.teamgannon.trips.events;

import org.springframework.context.ApplicationEvent;

public class OpenWorkbenchEvent extends ApplicationEvent {
    public OpenWorkbenchEvent(Object source) {
        super(source);
    }
}
