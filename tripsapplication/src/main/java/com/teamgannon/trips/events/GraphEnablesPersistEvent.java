package com.teamgannon.trips.events;

import com.teamgannon.trips.jpa.model.GraphEnablesPersist;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class GraphEnablesPersistEvent extends ApplicationEvent {
    private final GraphEnablesPersist graphEnablesPersist;

    public GraphEnablesPersistEvent(Object source, GraphEnablesPersist graphEnablesPersist) {
        super(source);
        this.graphEnablesPersist = graphEnablesPersist;
    }
}
