package com.teamgannon.trips.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Event triggered when the user wants to filter stars by routes.
 * This allows showing only stars that are part of selected routes,
 * helping visualize route overlaps and crossroads systems.
 */
@Getter
public class RouteStarFilterEvent extends ApplicationEvent {

    /**
     * The action to perform on the route star filter.
     */
    public enum FilterAction {
        /**
         * Filter to show only stars from this single route (clears previous filter).
         */
        FILTER_BY_ROUTE,

        /**
         * Add this route's stars to the existing filter (union).
         */
        ADD_TO_FILTER,

        /**
         * Remove this route's stars from the filter.
         */
        REMOVE_FROM_FILTER,

        /**
         * Clear all filters and show all stars.
         */
        CLEAR_FILTER
    }

    private final FilterAction action;
    private final UUID routeId;
    private final String routeName;

    /**
     * Creates a RouteStarFilterEvent for a specific route.
     *
     * @param source    The object that fired the event
     * @param action    The filter action to perform
     * @param routeId   The route ID (can be null for CLEAR_FILTER)
     * @param routeName The route name for status messages
     */
    public RouteStarFilterEvent(Object source, FilterAction action, UUID routeId, String routeName) {
        super(source);
        this.action = action;
        this.routeId = routeId;
        this.routeName = routeName;
    }

    /**
     * Creates a RouteStarFilterEvent for clearing the filter.
     *
     * @param source The object that fired the event
     */
    public RouteStarFilterEvent(Object source) {
        super(source);
        this.action = FilterAction.CLEAR_FILTER;
        this.routeId = null;
        this.routeName = null;
    }
}
