package com.teamgannon.trips.events;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

@Getter
public class ContextSelectorEvent extends ApplicationEvent {
    private final ContextSelectionType contextSelectionType;
    private final StarDisplayRecord starDisplayRecord;
    private final Map<String, String> objectProperties;

    public ContextSelectorEvent(Object source,
                                ContextSelectionType contextSelectionType,
                                StarDisplayRecord starDisplayRecord,
                                Map<String, String> objectProperties) {
        super(source);
        this.contextSelectionType = contextSelectionType;
        this.starDisplayRecord = starDisplayRecord;
        this.objectProperties = objectProperties;
    }
}
