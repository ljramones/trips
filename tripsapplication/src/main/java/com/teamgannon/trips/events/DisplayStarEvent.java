package com.teamgannon.trips.events;

import com.teamgannon.trips.jpa.model.StarObject;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * DisplayStarEvent is an event class that represents the act of displaying a star object.
 * It extends the ApplicationEvent class.
 *
 * The DisplayStarEvent class provides a constructor to create a new DisplayStarEvent object.
 * The constructor takes the source object that the event is generated from, and the star object to be displayed.
 *
 * This class also provides a getter method for accessing the star object.
 */
@Getter
public class DisplayStarEvent extends ApplicationEvent {
    private final StarObject starObject;

    /**
     * Constructs a new DisplayStarEvent object.
     *
     * @param source      the source object that the event is generated from
     * @param starObject  the star object to be displayed
     */
    public DisplayStarEvent(Object source, StarObject starObject) {
        super(source);
        this.starObject = starObject;
    }

}
