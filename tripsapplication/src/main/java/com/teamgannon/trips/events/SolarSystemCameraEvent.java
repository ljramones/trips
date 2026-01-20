package com.teamgannon.trips.events;

import org.springframework.context.ApplicationEvent;

public class SolarSystemCameraEvent extends ApplicationEvent {

    public enum CameraAction {
        TOP_DOWN,
        EDGE_ON,
        OBLIQUE,
        FOCUS_SELECTED,
        RESET_VIEW
    }

    private final CameraAction action;

    public SolarSystemCameraEvent(Object source, CameraAction action) {
        super(source);
        this.action = action;
    }

    public CameraAction getAction() {
        return action;
    }
}
