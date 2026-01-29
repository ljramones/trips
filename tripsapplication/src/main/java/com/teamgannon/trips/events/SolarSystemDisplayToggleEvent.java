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
        ORBIT_NODES,
        APSIDES,
        ORBITS,
        LABELS,
        HABITABLE_ZONE,
        SCALE_GRID,
        RELATIVE_PLANET_SIZES,
        PLANETARY_RINGS,
        ASTEROID_BELT,
        KUIPER_BELT
    }

    private final ToggleType toggleType;
    private final boolean enabled;

    public SolarSystemDisplayToggleEvent(Object source, ToggleType toggleType, boolean enabled) {
        super(source);
        this.toggleType = toggleType;
        this.enabled = enabled;
    }
}
