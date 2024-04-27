package com.teamgannon.trips.events;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NewDataSetEvent extends ApplicationEvent {
    private final DataSetDescriptor descriptor;

    public NewDataSetEvent(Object source, DataSetDescriptor descriptor) {
        super(source);
        this.descriptor = descriptor;
    }
}
