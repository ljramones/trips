package com.teamgannon.trips.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BusyStateEvent extends ApplicationEvent {

    private final String taskId;
    private final boolean busy;
    private final String message;
    private final Runnable cancelAction;

    public BusyStateEvent(Object source, String taskId, boolean busy, String message, Runnable cancelAction) {
        super(source);
        this.taskId = taskId;
        this.busy = busy;
        this.message = message;
        this.cancelAction = cancelAction;
    }
}
