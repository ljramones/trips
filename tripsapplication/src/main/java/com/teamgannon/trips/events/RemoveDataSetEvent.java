package com.teamgannon.trips.events;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RemoveDataSetEvent extends ApplicationEvent {
    private final DataSetDescriptor dataSetDescriptor;

    public RemoveDataSetEvent(Object source, DataSetDescriptor dataSetDescriptor) {
        super(source);
        this.dataSetDescriptor = dataSetDescriptor;
    }
}
