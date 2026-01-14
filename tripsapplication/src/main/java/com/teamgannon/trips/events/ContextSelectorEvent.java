package com.teamgannon.trips.events;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.planetary.PlanetaryContext;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

@Getter
public class ContextSelectorEvent extends ApplicationEvent {
    private final ContextSelectionType contextSelectionType;
    private final StarDisplayRecord starDisplayRecord;
    private final Map<String, String> objectProperties;
    private final PlanetaryContext planetaryContext;

    /**
     * Constructor for INTERSTELLAR and SOLARSYSTEM context types.
     */
    public ContextSelectorEvent(Object source,
                                ContextSelectionType contextSelectionType,
                                StarDisplayRecord starDisplayRecord,
                                Map<String, String> objectProperties) {
        super(source);
        this.contextSelectionType = contextSelectionType;
        this.starDisplayRecord = starDisplayRecord;
        this.objectProperties = objectProperties;
        this.planetaryContext = null;
    }

    /**
     * Constructor for PLANETARY context type.
     */
    public ContextSelectorEvent(Object source,
                                ContextSelectionType contextSelectionType,
                                StarDisplayRecord starDisplayRecord,
                                PlanetaryContext planetaryContext) {
        super(source);
        this.contextSelectionType = contextSelectionType;
        this.starDisplayRecord = starDisplayRecord;
        this.objectProperties = null;
        this.planetaryContext = planetaryContext;
    }
}
