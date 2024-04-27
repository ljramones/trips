package com.teamgannon.trips.events;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RemoveDataSetEvent extends ApplicationEvent {
    private final DataSetDescriptor descriptor;

    public RemoveDataSetEvent(Object source, DataSetDescriptor descriptor) {
        super(source);
        this.descriptor = descriptor;
    }
}
