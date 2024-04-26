package com.teamgannon.trips.events;

import org.springframework.context.ApplicationEvent;

public class AddDataSetEvent extends ApplicationEvent {
    public AddDataSetEvent(Object source) {
        super(source);
    }
}
