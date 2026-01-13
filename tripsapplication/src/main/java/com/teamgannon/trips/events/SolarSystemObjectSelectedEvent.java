package com.teamgannon.trips.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event fired when an object (planet or star) is selected in the solar system view.
 * Used to update the side pane properties display.
 */
@Getter
public class SolarSystemObjectSelectedEvent extends ApplicationEvent {

    /**
     * The type of object that was selected
     */
    public enum SelectionType {
        PLANET,
        STAR,
        NONE
    }

    private final Object selectedObject;
    private final SelectionType selectionType;

    public SolarSystemObjectSelectedEvent(Object source, Object selectedObject, SelectionType selectionType) {
        super(source);
        this.selectedObject = selectedObject;
        this.selectionType = selectionType;
    }

    /**
     * Create an event indicating no selection
     */
    public static SolarSystemObjectSelectedEvent none(Object source) {
        return new SolarSystemObjectSelectedEvent(source, null, SelectionType.NONE);
    }
}
