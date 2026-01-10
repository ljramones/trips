package com.teamgannon.trips.events;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * RecenterStarEvent is an event class that represents the act of recentering the plot view on a specific star.
 * It extends the ApplicationEvent class.
 *
 * The RecenterStarEvent class provides a constructor to create a new RecenterStarEvent object.
 * The constructor takes the source object that the event is generated from, and the star display record to recenter on.
 *
 * This class also provides a getter method for accessing the star display record.
 */
@Getter
public class RecenterStarEvent extends ApplicationEvent {
    private final StarDisplayRecord starDisplayRecord;

    /**
     * Constructs a new RecenterStarEvent object.
     *
     * @param source             the source object that the event is generated from
     * @param starDisplayRecord  the star display record to recenter the plot on
     */
    public RecenterStarEvent(Object source, StarDisplayRecord starDisplayRecord) {
        super(source);
        this.starDisplayRecord = starDisplayRecord;
    }

}
