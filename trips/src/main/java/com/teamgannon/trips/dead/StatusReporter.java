package com.teamgannon.trips.dead;

import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.operators.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class StatusReporter
        implements ListUpdater, StellarPropertiesDisplayer, ContextSelector, RouteUpdater, RedrawListener {


    /**
     * update the list
     *
     * @param listItem the list item
     */
    @Override
    public void updateList(Map<String, String> listItem) {
        log.info("updateList: {}", listItem);
    }

    /**
     * clear the entire list
     */
    @Override
    public void clearList() {
        log.info("clearing list");
    }

    /**
     * select a interstellar system space
     *
     * @param objectProperties the properties of the selected object
     */
    @Override
    public void selectInterstellarSpace(Map<String, String> objectProperties) {
        log.info("select: {}", objectProperties);
    }

    /**
     * select a solar system
     *
     * @param objectProperties the properties of the selected object
     */
    @Override
    public void selectSolarSystemSpace(Map<String, String> objectProperties) {
        log.info("Select solar system: {}", objectProperties);
    }

    @Override
    public void displayStellarProperties(Map<String, String> properties) {
        log.info("stellarProperties: {}", properties);
    }

    /**
     * triggered when a new route is created
     *
     * @param routeDescriptor the route descriptor
     */
    @Override
    public void newRoute(RouteDescriptor routeDescriptor) {
        log.info("Route added: {}", routeDescriptor);
    }

    /**
     * triggered when an existing route changes
     *
     * @param routeDescriptor the route descriptor
     */
    @Override
    public void updateRoute(RouteDescriptor routeDescriptor) {
        log.info("Route updated: {}", routeDescriptor);
    }

    /**
     * triggered when a route is removed
     *
     * @param routeDescriptor the route descriptor
     */
    @Override
    public void deleteRoute(RouteDescriptor routeDescriptor) {
        log.info("Route deleted: {}", routeDescriptor);
    }

    @Override
    public void recenter(StarDisplayRecord star) {
        log.info("Redraw the screen based on this star: {}", star);
    }
}
