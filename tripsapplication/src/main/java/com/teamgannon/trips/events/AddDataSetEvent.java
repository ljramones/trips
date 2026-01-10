package com.teamgannon.trips.events;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * AddDataSetEvent is an event class that represents the addition of a dataset.
 * It extends the ApplicationEvent class.
 *
 * The AddDataSetEvent class provides a constructor to create a new AddDataSetEvent object.
 * The constructor takes the source object that the event is generated from, and the dataset descriptor.
 *
 * This class also provides a getter method for accessing the dataset descriptor.
 */
@Getter
public class AddDataSetEvent extends ApplicationEvent {
    private final DataSetDescriptor dataSetDescriptor;

    /**
     * Constructs a new AddDataSetEvent object.
     *
     * @param source            the source object that the event is generated from
     * @param dataSetDescriptor the dataset descriptor to be added
     */
    public AddDataSetEvent(Object source, DataSetDescriptor dataSetDescriptor) {
        super(source);
        this.dataSetDescriptor = dataSetDescriptor;
    }

}
