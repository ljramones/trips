package com.teamgannon.trips.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event fired when a solar system display toggle is changed in the side pane.
 */
@Getter
public class SolarSystemDisplayToggleEvent extends ApplicationEvent {

    public enum ToggleType {
        ECLIPTIC_PLANE,
        ORBIT_NODES
    }

    private final ToggleType toggleType;
    private final boolean enabled;

    public SolarSystemDisplayToggleEvent(Object source, ToggleType toggleType, boolean enabled) {
        super(source);
        this.toggleType = toggleType;
        this.enabled = enabled;
    }
}
