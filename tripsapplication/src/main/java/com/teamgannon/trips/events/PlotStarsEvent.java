package com.teamgannon.trips.events;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * Event published to plot a specific list of stars.
 * This is used when the user wants to plot stars from a table/query result
 * rather than running a new database query.
 */
@Getter
public class PlotStarsEvent extends ApplicationEvent {

    /**
     * The list of stars to plot.
     */
    private final List<StarObject> starObjects;

    /**
     * The dataset descriptor for the stars.
     */
    private final DataSetDescriptor dataSetDescriptor;

    /**
     * Optional description for status messages (e.g., "Constellation: Orion").
     */
    private final String description;

    /**
     * Creates a PlotStarsEvent.
     *
     * @param source            The object that fired the event
     * @param starObjects       The list of stars to plot
     * @param dataSetDescriptor The dataset descriptor
     * @param description       Optional description for status messages
     */
    public PlotStarsEvent(Object source,
                          List<StarObject> starObjects,
                          DataSetDescriptor dataSetDescriptor,
                          String description) {
        super(source);
        this.starObjects = starObjects;
        this.dataSetDescriptor = dataSetDescriptor;
        this.description = description;
    }

    /**
     * Creates a PlotStarsEvent without a description.
     *
     * @param source            The object that fired the event
     * @param starObjects       The list of stars to plot
     * @param dataSetDescriptor The dataset descriptor
     */
    public PlotStarsEvent(Object source,
                          List<StarObject> starObjects,
                          DataSetDescriptor dataSetDescriptor) {
        this(source, starObjects, dataSetDescriptor, null);
    }
}
