package com.teamgannon.trips.events;

import com.teamgannon.trips.search.AstroSearchQuery;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Event published when a query result needs to be exported.
 */
@Getter
public class ExportQueryEvent extends ApplicationEvent {

    private final AstroSearchQuery searchQuery;

    public ExportQueryEvent(Object source, AstroSearchQuery searchQuery) {
        super(source);
        this.searchQuery = searchQuery;
    }
}
