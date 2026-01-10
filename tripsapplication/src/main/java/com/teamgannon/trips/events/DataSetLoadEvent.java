package com.teamgannon.trips.events;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * DataSetLoadEvent is an event class that represents the completion of a dataset load operation.
 * It extends the ApplicationEvent class.
 *
 * The DataSetLoadEvent class provides a constructor to create a new DataSetLoadEvent object.
 * The constructor takes the source object that the event is generated from, and the dataset descriptor.
 *
 * This class also provides a getter method for accessing the dataset descriptor.
 */
@Getter
public class DataSetLoadEvent extends ApplicationEvent {
    private final DataSetDescriptor descriptor;

    /**
     * Constructs a new DataSetLoadEvent object.
     *
     * @param source      the source object that the event is generated from
     * @param descriptor  the dataset descriptor that was loaded
     */
    public DataSetLoadEvent(Object source, DataSetDescriptor descriptor) {
        super(source);
        this.descriptor = descriptor;
    }

}
